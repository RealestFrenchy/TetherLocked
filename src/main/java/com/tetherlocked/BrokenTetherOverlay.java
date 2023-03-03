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
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPriority;

import java.awt.*;
import java.awt.geom.Area;
import java.util.ArrayList;
public class BrokenTetherOverlay extends Overlay{
    private Client client;
    public int gameWidth;
    public int gameHeight;
    private TetherLockedConfig config;
    private TetherLockedPlugin plugin;

    public Color overlayColor;
    public int padding =0;
    public double tetherLength;

    public BrokenTetherOverlay(TetherLockedPlugin plugin, Client client, int padding)
    {
        super(plugin);
        gameWidth = client.getCanvasWidth();
        gameHeight = client.getCanvasHeight();
        setBounds(new Rectangle(0,0,gameWidth, gameHeight));
        setDragTargetable(false);
        setPriority(OverlayPriority.LOW);
        setLayer(OverlayLayer.UNDER_WIDGETS);
        setColor(overlayColor);
        this.padding = padding;
        this.tetherLength = tetherLength;
    }
    public Rectangle getRectangle(Polygon gon) {
        if (gon == null) {
            return new Rectangle(0, 0, 0, 0);
        }
        Rectangle bounds = gon.getBounds();
        if (bounds == null) {
            return new Rectangle(0, 0, 0, 0);
        }
        return new Rectangle(bounds.x - padding, bounds.y - padding, bounds.width + (padding * 2), bounds.height + (padding * 2));
    }
    public Rectangle getRectangle(Rectangle bounds) {
        return new Rectangle(bounds.x-padding,bounds.y-padding,bounds.width + (padding*2),bounds.height + (padding*2));
    }

    public void setColor(Color unlitColor) {
        this.overlayColor = unlitColor;
    }

    @Override
    public Dimension render(Graphics2D graphics) {

        Dimension returnDimension = new Dimension(gameWidth,gameHeight);

        Area areaToRender = new Area(new Rectangle(0,0,gameWidth,gameHeight));

        graphics.setClip(areaToRender);

        graphics.setColor(new Color(0, 19, 36, 127));

        graphics.fillRect(0,0,gameWidth,gameHeight);

        return returnDimension;
    }
}
