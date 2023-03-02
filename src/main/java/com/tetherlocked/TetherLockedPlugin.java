package com.tetherlocked;

import com.google.inject.Provides;
import javax.inject.Inject;

import net.runelite.client.plugins.party.messages.LocationUpdate;
import com.tetherlocked.data.TetheredPlayer;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import lombok.Getter;
import net.runelite.client.party.*;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.plugins.party.data.PartyData;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.party.PartyService;
import net.runelite.client.party.WSClient;
import net.runelite.client.party.messages.UserSync;

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
	private ClientThread clientThread;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private TetherLockedConfig config;

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
	private boolean addedButton = false;
	private Instant lastLogout;
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

		lastLogout = Instant.now();
		wsClient.registerMessage(LocationUpdate.class);
	}

	@Override
	protected void shutDown() throws Exception
	{
		clientToolbar.removeNavigation(navButton);
		wsClient.unregisterMessage(LocationUpdate.class);
		addedButton = false;
		tetheredPlayers.clear();
		lastLogout = null;
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Example says " + config.greeting(), null);
		}
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
		if (myPlayer.getLocation() == null)
		{
			myPlayer.updatePlayerInfo(client);
		}
		double tetherDistance=calcDistance();
		if (tetherDistance > config.getMaxTetherLength())
		{
			tetherTooLong();
		}
	}

	public double calcDistance()
	{

		List<PartyMember> linkedPlayers = partyService.getMembers();
		WorldPoint myLocation = myPlayer.getLocation();
		double longestDistance = 0;
		final PartyData partyData = getPartyData(event.getMemberId)
		tetheredPlayers.get(partyService.getMembers());

		for (int i = 0; i < linkedPlayers.size(); i++)
		{
			long memberID = linkedPlayers.get(i).getMemberId();
			final TetheredPlayer tetheredPlayer = tetheredPlayers.get(partyService.getMemberById(memberID));
			WorldPoint linkedLocation = tetheredPlayer.getLocation();
			if (myLocation.getPlane() != linkedLocation.getPlane())
			{
				longestDistance = 20000;
			}
			else
			{
				double currentXDistance = myLocation.getX() - linkedLocation.getX();
				double currentYDistance = myLocation.getY() - linkedLocation.getY();
				if (abs(currentYDistance) > abs(currentXDistance))
				{
					if (longestDistance < currentYDistance)
					{
						longestDistance = currentYDistance;
					}

				}
				else
				{
					if (longestDistance < currentXDistance)
					{
						longestDistance = currentXDistance;
					}
				}
			}

		}
		return longestDistance;
	}
	public void tetherTooLong()
	{
		System.out.print("your out of tether range");
	}

}
