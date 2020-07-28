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
package org.xmind.ui.internal.mindmap;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.gef.ZoomManager;

public class SummaryFeedback extends RangeFeedback {

    private DecoratedLineFeedback decoration;

    public SummaryFeedback(SummaryPart host) {
        super(host);
        decoration = new DecoratedSummaryFeedback(host);
    }

    public void addToLayer(IFigure layer) {
        decoration.addToLayer(layer);
        super.addToLayer(layer);
    }

    public void removeFromLayer(IFigure layer) {
        super.removeFromLayer(layer);
        decoration.removeFromLayer(layer);
    }

    public void update() {
        super.update();
        decoration.update();
    }

    public void setZoomManager(ZoomManager zoomManager) {
        super.setZoomManager(zoomManager);
        decoration.setZoomManager(zoomManager);
    }

    protected Rectangle getRangeBounds() {
        return ((SummaryPart) getHost()).getEnclosure().getBounds();
    }

}