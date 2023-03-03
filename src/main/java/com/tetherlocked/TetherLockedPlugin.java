/*
 * Copyright (c) 2018, Adam <Adam@sigterm.info>
 * Copyright (c) 2022, RealestFrenchy <https://github.com/RealestFrenchy>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.tetherlocked;

import com.google.inject.Provides;
import javax.inject.Inject;
import javax.swing.*;

import com.tetherlocked.data.TetheredPlayerLocation;
import net.runelite.client.events.PartyChanged;
import net.runelite.client.plugins.party.messages.LocationUpdate;
import com.tetherlocked.data.TetheredPlayer;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import lombok.Getter;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.party.PartyService;
import net.runelite.client.party.WSClient;
import net.runelite.client.party.messages.UserSync;
import net.runelite.client.ui.overlay.OverlayManager;

import java.time.Instant;
import java.util.*;

import static java.lang.Math.abs;

@Slf4j
@PluginDescriptor(
	name = "TetherLocked"
)
public class TetherLockedPlugin extends Plugin
{
	@Inject
	private Client client;
	@Inject
	private PartyService party;
	@Inject
	private ClientThread clientThread;
	@Inject
	private ClientToolbar clientToolbar;
	@Inject
	private TetherLockedConfig config;
	@Inject
	private OverlayManager overlayManager;
	@Inject
	private TetherInfoOverlay overlay;

	private BrokenTetherOverlay flash;
	@Inject
	private PartyService partyService;
	@Inject
	private PluginManager pluginManager;
	@Inject
	private WSClient wsClient;
	@Getter
	private final Map<Long, TetheredPlayer> tetheredPlayers = new HashMap<>();
	private TetheredPlayer myPlayer = null;
	private NavigationButton navButton;
	private HashMap<Long, WorldPoint> TetherLocations = new HashMap<>();
	private boolean addedButton = false;
	private Instant lastLogout;
	protected double tetherLength;
	private WorldPoint lastLocation ;
	private boolean overlayFlag;

	@Override
	protected void startUp() throws Exception
	{

		if (isInParty())
		{
			clientToolbar.addNavigation(navButton);
			addedButton = true;
		}

		if (isInParty())
		{
			clientThread.invokeLater(() ->
			{
				myPlayer = new TetheredPlayer(partyService.getLocalMember(), client);
				partyService.send(new UserSync());
			});
		}

		final Optional<Plugin> partyPlugin = pluginManager.getPlugins().stream().filter(p -> p.getName().equals("Party")).findFirst();
		if (partyPlugin.isPresent() && !pluginManager.isPluginEnabled(partyPlugin.get()))
		{
			pluginManager.setPluginEnabled(partyPlugin.get(), true);
		}
		overlayManager.add(overlay);

		lastLogout = Instant.now();
		wsClient.registerMessage(LocationUpdate.class);
		wsClient.registerMessage(TetheredPlayerLocation.class);
	}

	@Override
	protected void shutDown() throws Exception
	{
		clientToolbar.removeNavigation(navButton);
		wsClient.unregisterMessage(LocationUpdate.class);
		wsClient.unregisterMessage(TetheredPlayerLocation.class);
		addedButton = false;
		tetheredPlayers.clear();
		lastLogout = null;
	}

	@Subscribe
	public void onPartyChanged(final PartyChanged event)
	{
		TetherLocations.clear();
	}

	@Provides
	TetherLockedConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TetherLockedConfig.class);
	}

	public boolean isInParty()
	{
		return partyService.isInParty();
	}

	public boolean isLocalPlayer(long id)
	{
		return partyService.getLocalMember() != null && partyService.getLocalMember().getMemberId() == id;
	}
	@Subscribe
	public void onGameTick(final GameTick tick)
	{
		if (!isInParty() || client.getLocalPlayer() == null || partyService.getLocalMember() == null)
		{
			return;
		}
		if (myPlayer == null || !Objects.equals(client.getLocalPlayer().getName(), myPlayer.getUsername()))
		{
			myPlayer = new TetheredPlayer(partyService.getLocalMember(), client);
			return;
		}
		if (myPlayer.getLocation() == null) {
			myPlayer.updatePlayerInfo(client);
		}
		myPlayer.updatePlayerInfo(client);
		if (!myPlayer.getLocation().equals(lastLocation)) {
			WorldPoint p = myPlayer.getLocation();
			final TetheredPlayerLocation locationUpdate = new TetheredPlayerLocation(myPlayer.getLocation());
			party.send(locationUpdate);
			lastLocation = myPlayer.getLocation();
		}
		updateTetherLength();
		if (tetherLength< (config.getMaxTetherLength()+1) && overlayFlag == true && config.screenDarken()) {
			overlayManager.remove(flash);
			overlayFlag = false;
		}
		if (tetherLength > config.getMaxTetherLength())
		{
			tetherTooLong();
		}
	}
	@Subscribe
	public void onTetheredPlayerLocation(TetheredPlayerLocation event)
	{
		long memberId = event.getMemberId();
		WorldPoint point = event.getPoint();
		updateTetherHash(memberId, point);
		updateTetherLength();
	}
/*	@Subscribe
	public void onLocationUpdate(final LocationUpdate event)
	{
		long memberId = event.getMemberId();
		WorldPoint point = event.getWorldPoint();
		updateTetherLength(point);
	}*/

	@Subscribe
	public void onUserSync(final UserSync event)
	{
		clientThread.invoke(() ->
		{
			myPlayer = new TetheredPlayer(partyService.getLocalMember(), client);
			final TetheredPlayerLocation locationUpdate = new TetheredPlayerLocation(myPlayer.getLocation());
				partyService.send(locationUpdate);
		});
	}

	public void tetherTooLong()
	{ //TODO: make it so you can't interact with objects if outside of tether range.
		if (overlayFlag == false && config.screenDarken()) {
			flash = new BrokenTetherOverlay(this, client, 0);
			overlayManager.add(flash);
			overlayFlag = true;
		}
		System.out.print("your out of tether range");
	}
	public double updateTetherLength()
	{
		double maxValue = -1;
		WorldPoint myLocation = client.getLocalPlayer().getWorldLocation();
		Iterator playerIterator = TetherLocations.entrySet().iterator();
		List<Double> tetherLengthsList = new ArrayList<>();
		while (playerIterator.hasNext()) {
			double currentTetherLength = 0;
			Map.Entry mapElement
					= (Map.Entry) playerIterator.next();
			WorldPoint linkedLocation = (WorldPoint)mapElement.getValue();
			if (myLocation.getPlane() != linkedLocation.getPlane()) {
				currentTetherLength = 20000;
			} else {
				double currentXDistance = myLocation.getX() - linkedLocation.getX();
				double currentYDistance = myLocation.getY() - linkedLocation.getY();
				if (abs(currentYDistance) > abs(currentXDistance)) {
					if (currentTetherLength < abs(currentYDistance)) {
						currentTetherLength = abs(currentYDistance);
					}

				} else {
					if (currentTetherLength < abs(currentXDistance)) {
						currentTetherLength = abs(currentXDistance);
					}
				}
				tetherLengthsList.add(abs(currentTetherLength));
				// Check maximum element using for loop
				for (Double integer : tetherLengthsList) {
					if (integer > maxValue)
						maxValue = integer;
				}
			}
		}
		tetherLength = maxValue;
		if(tetherLength >config.getMaxTetherLength())
		{
			tetherTooLong();
		}
		if (tetherLength<= config.getMaxTetherLength() && overlayFlag == true && config.screenDarken()) {
			overlayManager.remove(flash);
			overlayFlag = false;
		}
		return tetherLength;
	}
	public double getTetherLength()
	{
		return tetherLength;
	}

	public void updateTetherHash(long memberId, WorldPoint linkedLocation)
	{
		TetherLocations.put(memberId, linkedLocation);
	}

}