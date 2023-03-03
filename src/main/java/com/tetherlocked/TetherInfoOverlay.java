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
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.OverlayMenuEntry;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.LineComponent;

import javax.inject.Inject;
import java.awt.*;

import static net.runelite.api.MenuAction.RUNELITE_OVERLAY_CONFIG;
import static net.runelite.client.ui.overlay.OverlayManager.OPTION_CONFIGURE;

public class TetherInfoOverlay extends OverlayPanel {
    private Client client;
    private TetherLockedConfig config;
    private TetherLockedPlugin plugin;
    private final static String TETHER_DISTANCE_STRING = "Tether Distance:";
    private final static String TETHER_DISTANCE_BROKE = "In Another Area";

    @Inject
    private TetherInfoOverlay(Client client, TetherLockedConfig config, TetherLockedPlugin plugin) {
        super(plugin);
        this.plugin = plugin;
        this.client = client;
        this.config = config;
        setPosition(OverlayPosition.TOP_LEFT);
        setPriority(OverlayPriority.MED);
        getMenuEntries().add(new OverlayMenuEntry(RUNELITE_OVERLAY_CONFIG, OPTION_CONFIGURE, "TetherLocked Overlay"));
    }

    public Dimension render(Graphics2D graphics) {
        String currentTetherLength;
        if (plugin.getTetherLength() == 20000) {
            currentTetherLength = TETHER_DISTANCE_BROKE;
        } else {
            currentTetherLength = addCommasToNumber((int) plugin.getTetherLength());
        }

        panelComponent.getChildren().add(LineComponent.builder()
                .left(TETHER_DISTANCE_STRING)
                .leftColor(getTextColor())
                .right(currentTetherLength)
                .rightColor(getTextColor())
                .build());

        panelComponent.setPreferredSize(new Dimension(
                getLongestStringWidth(TETHER_DISTANCE_STRING, graphics)
                        + getLongestStringWidth(currentTetherLength.toString(), graphics)+10,
                0));

        return super.render(graphics);
    }

    private Color getTextColor() {
        if (config.getMaxTetherLength() - plugin.tetherLength<= 0) {
            return Color.RED;
        } else if (config.getMaxTetherLength() - plugin.tetherLength <= (config.getMaxTetherLength() * .25)) {
            return Color.ORANGE;
        }
        return Color.WHITE;
    }

    private int getLongestStringWidth(String string, Graphics2D graphics) {
        int longest = graphics.getFontMetrics().stringWidth("000000");
        int currentItemWidth = graphics.getFontMetrics().stringWidth(string);
        return longest;
    }

    private String addCommasToNumber(int number) {
        String input = Integer.toString(number);
        StringBuilder output = new StringBuilder();
        for (int x = input.length() - 1; x >= 0; x--) {
            int lastPosition = input.length() - x - 1;
            if (lastPosition != 0 && lastPosition % 3 == 0) {
                output.append(",");
            }
            output.append(input.charAt(x));
        }
        return output.reverse().toString();
    }
}
