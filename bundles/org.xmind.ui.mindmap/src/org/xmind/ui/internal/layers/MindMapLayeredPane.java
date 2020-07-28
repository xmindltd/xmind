/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See https://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.ui.internal.layers;

import org.eclipse.draw2d.Graphics;
import org.eclipse.swt.widgets.Display;
import org.xmind.gef.GEF;
import org.xmind.gef.draw2d.graphics.AlphaGraphics;
import org.xmind.ui.mindmap.MindMapUI;

public class MindMapLayeredPane extends DefaultLayeredPane {

    private int targetAlpha = 0xff;

    private boolean schedulePaint = MindMapUI.isAnimationEnabled();

    private int alpha = schedulePaint ? 0 : targetAlpha;

    protected void addLayers() {
        super.addLayers();
        getScalableLayeredPane().addLayerAfter(new TitleLayer(),
                MindMapUI.LAYER_TITLE, GEF.LAYER_CONTENTS);
        getScalableLayeredPane().addLayerAfter(new UndoRedoTipsLayer(),
                MindMapUI.LAYER_UNDO, MindMapUI.LAYER_TITLE);
        CoverLayer coverLayer = new CoverLayer();
        getScalableLayeredPane().add(coverLayer, MindMapUI.LAYER_COVER);
        coverLayer.addBackgroundLayer(getLayer(GEF.LAYER_BACKGROUND));
        coverLayer.addBackgroundLayer(getLayer(GEF.LAYER_SHADOW));
        add(new SkylightLayer(), MindMapUI.LAYER_SKYLIGHT);
    }

    public int getAlpha() {
        return alpha;
    }

    public void setAlpha(int alpha) {
        if (alpha == this.alpha)
            return;
        this.alpha = alpha;
        repaint();
    }

    public int getTargetAlpha() {
        return targetAlpha;
    }

    public void setTargetAlpha(int targetAlpha) {
        targetAlpha = Math.max(0, Math.min(0xff, targetAlpha));
        this.targetAlpha = targetAlpha;
        checkSchedule();
    }

    public void paint(Graphics graphics) {
        super.paint(graphics);
        if (schedulePaint) {
            Display.getCurrent().timerExec(50, new Runnable() {
                public void run() {
                    setAlpha(getNewAlpha());
                    checkSchedule();
                }
            });
        }
        schedulePaint = false;
    }

    private void checkSchedule() {
        schedulePaint = shouldSchedulePaint();
        if (!schedulePaint) {
            setAlpha(getTargetAlpha());
        } else {
            repaint();
        }
    }

    private boolean shouldSchedulePaint() {
        return getAlpha() != getTargetAlpha() && MindMapUI.isAnimationEnabled();
    }

    private int getNewAlpha() {
        if (getAlpha() < getTargetAlpha())
            return Math.min(getTargetAlpha(), getAlpha() + 30);
        else if (getAlpha() > getTargetAlpha())
            return Math.max(getTargetAlpha(), getAlpha() - 30);
        return getTargetAlpha();
    }

    protected void paintChildren(Graphics graphics) {
        if (getAlpha() <= 0) {
            return;
        } else if (getAlpha() >= 0xff) {
            super.paintChildren(graphics);
        } else {
            AlphaGraphics ag = new AlphaGraphics(graphics);
            ag.setMainAlpha(getAlpha());
            super.paintChildren(ag);
            ag.dispose();
        }
    }

}