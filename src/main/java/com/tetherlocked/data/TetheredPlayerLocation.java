package com.tetherlocked.data;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.party.messages.PartyMemberMessage;
@Value
@EqualsAndHashCode(callSuper = true)
public class TetheredPlayerLocation extends PartyMemberMessage {

    private final WorldPoint point;
}
