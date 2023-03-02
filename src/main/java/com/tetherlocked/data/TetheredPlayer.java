package com.tetherlocked.data;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.google.inject.Inject;
import com.google.inject.Provides;
import net.runelite.api.Client;
import net.runelite.api.VarPlayer;
import net.runelite.client.party.PartyMember;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
@Data
@EqualsAndHashCode
public class TetheredPlayer {
    private transient PartyMember member;
    private String username;
    private int stamina;
    private WorldPoint location;
    private int poison;
    private int disease;
    private int world;

    public TetheredPlayer(final PartyMember member) {
        this.member = member;
        this.username = "";
        this.location = null;

        this.world = 0;
    }

    public TetheredPlayer(final PartyMember member, final Client client) {
        this(member);
        this.world = client.getWorld();

        updatePlayerInfo(client);
    }

    public void updatePlayerInfo(final Client client) {
        if (client.getLocalPlayer() != null) {
            this.username = client.getLocalPlayer().getName();
            this.location = client.getLocalPlayer().getWorldLocation();
        }
    }
}

