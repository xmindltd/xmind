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
package org.xmind.ui.internal.tools;

import org.eclipse.core.runtime.Assert;
import org.eclipse.draw2d.Cursors;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.graphics.Cursor;
import org.xmind.gef.GEF;
import org.xmind.gef.Request;
import org.xmind.gef.draw2d.IReferencedFigure;
import org.xmind.gef.draw2d.IUseTransparency;
import org.xmind.gef.part.IGraphicalEditPart;
import org.xmind.gef.part.IPart;
import org.xmind.ui.mindmap.ILegendItemPart;
import org.xmind.ui.mindmap.ILegendPart;
import org.xmind.ui.tools.DummyMoveTool;

public class LegendMoveTool extends DummyMoveTool {

    private int oldAlpha = -1;

    private Point oldPosition = null;

    public void setSource(IGraphicalEditPart source) {
        Assert.isTrue(source instanceof ILegendPart
                || source instanceof ILegendItemPart);
        super.setSource(source);
    }

    protected ILegendPart getLegendPart() {
        if (getSource() instanceof ILegendPart)
            return (ILegendPart) getSource();
        if (getSource() instanceof ILegendItemPart)
            return ((ILegendItemPart) getSource()).getOwnedLegend();
        return null;
    }

    protected IFigure createDummy() {
        ILegendPart legend = getLegendPart();
        IFigure figure = legend.getFigure();
        if (figure instanceof IReferencedFigure) {
            IReferencedFigure refFigure = (IReferencedFigure) figure;
            Point ref = refFigure.getReference();
            Point origin = refFigure.getOrigin();
            oldPosition = new Point(ref.x - origin.x, ref.y - origin.y);
        }
        if (figure instanceof IUseTransparency) {
            oldAlpha = ((IUseTransparency) figure).getMainAlpha();
            ((IUseTransparency) figure).setMainAlpha(0x80);
        }
        figure.setEnabled(false);
        return figure;
    }

    protected void destroyDummy(IFigure dummy) {
        dummy.setEnabled(true);
        if (dummy instanceof IUseTransparency && oldAlpha >= 0) {
            ((IUseTransparency) dummy).setMainAlpha(oldAlpha);
        }
        dummy.revalidate();
    }

    protected Request createRequest() {
        Request request = null;
        if (oldPosition != null) {
            request = new Request(GEF.REQ_MOVETO);
            request.setPrimaryTarget(getLegendPart());
            Dimension diff = getCursorPosition().getDifference(
                    getStartingPosition());
            Point newPosition = oldPosition.getTranslated(diff);
            request.setParameter(GEF.PARAM_POSITION, newPosition);
            oldPosition = null;
        }
        return request;
    }

    public Cursor getCurrentCursor(Point pos, IPart host) {
        return Cursors.HAND;
    }
}