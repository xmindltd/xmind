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
package org.xmind.gef.service;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.draw2d.ShadowFigure;

public class ShadowService extends AbstractShadowService {

    private Map<IFigure, ShadowFigure> map = new HashMap<IFigure, ShadowFigure>();

    public ShadowService(IGraphicalViewer viewer) {
        super(viewer);
    }

    public void setLayer(IFigure layer) {
        if (layer == getLayer())
            return;

        removeFiguresFromLayer();
        super.setLayer(layer);
        addFiguresToLayer();
    }

    private void addFiguresToLayer() {
        if (getLayer() == null)
            return;
        for (ShadowFigure shadow : map.values()) {
            getLayer().add(shadow);
        }
    }

    private void removeFiguresFromLayer() {
        if (getLayer() == null)
            return;
        for (ShadowFigure shadow : map.values()) {
            if (getLayer() == shadow.getParent())
                getLayer().remove(shadow);
        }
    }

    public void addShadow(IFigure source, int alpha, Dimension offset) {
        removeShadow(source);
        ShadowFigure shadow = new ShadowFigure();
        shadow.setSource(source);
        shadow.setAlpha(alpha);
        shadow.setOffset(offset.width, offset.height);
        map.put(source, shadow);
        if (getLayer() != null && isActive()) {
            getLayer().add(shadow);
        }
    }

    public void removeShadow(IFigure source) {
        ShadowFigure shadow = map.remove(source);
        if (shadow == null)
            return;
        shadow.setSource(null);
        if (getLayer() == shadow.getParent()) {
            getLayer().remove(shadow);
        }
    }

    protected void activate() {
        super.activate();
        if (getLayer() != null) {
            for (ShadowFigure shadow : map.values()) {
                getLayer().add(shadow);
            }
        }
    }

    protected void deactivate() {
        super.deactivate();
        if (getLayer() != null) {
            for (ShadowFigure shadow : map.values()) {
                if (getLayer() == shadow.getParent())
                    getLayer().remove(shadow);
            }
        }
    }

}