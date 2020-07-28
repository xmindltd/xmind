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

import org.eclipse.draw2d.Cursors;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Layer;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.xmind.core.Core;
import org.xmind.gef.GEF;
import org.xmind.gef.Request;
import org.xmind.gef.draw2d.IReferencedFigure;
import org.xmind.gef.event.KeyEvent;
import org.xmind.gef.event.MouseEvent;
import org.xmind.gef.part.IPart;
import org.xmind.gef.tool.ITool;
import org.xmind.ui.internal.mindmap.LegendPart;
import org.xmind.ui.mindmap.ILegendPart;
import org.xmind.ui.mindmap.IMindMapViewer;
import org.xmind.ui.mindmap.ISheetPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.tools.DummyCreateTool;
import org.xmind.ui.viewers.SWTUtils;

public class LegendCreateTool extends DummyCreateTool {

    private ISheetPart sheet = null;

    private ILegendPart dummyLegend = null;

    protected void onActivated(ITool prevTool) {
        super.onActivated(prevTool);
        if (getTargetViewer() instanceof IMindMapViewer) {
            sheet = ((IMindMapViewer) getTargetViewer()).getSheetPart();
            if (sheet != null) {
                createDummy();
            }
        }
    }

    protected void onDeactivated(ITool nextTool) {
        sheet = null;
        super.onDeactivated(nextTool);
    }

    protected IFigure doCreateDummy() {
        if (sheet != null) {
            dummyLegend = new LegendPart();
            dummyLegend.setModel(sheet.getSheet().getLegend());
            dummyLegend.setParent(getTargetViewer().getRootPart());
            addDummyLegendView();
            dummyLegend.addNotify();
            dummyLegend.getStatus().activate();
            dummyLegend.refresh();
            IFigure figure = dummyLegend.getFigure();
            figure.setEnabled(false);
            figure.setSize(figure.getPreferredSize());
            ((IReferencedFigure) figure).setReference(getCursorPosition());
            return figure;
        }
        return null;
    }

    private void addDummyLegendView() {
        Layer layer = getTargetViewer().getLayer(GEF.LAYER_PRESENTATION);
        if (layer != null) {
            layer.add(dummyLegend.getFigure());
        }
    }

    protected void destroyDummy(IFigure dummy) {
        if (dummyLegend != null) {
            dummyLegend.getStatus().deactivate();
            dummyLegend.removeNotify();
        }
        super.destroyDummy(dummy);
        if (dummyLegend != null) {
            dummyLegend.setParent(null);
            dummyLegend = null;
        }
    }

    protected void updateDummyPosition(IFigure dummy, Point pos) {
        ((IReferencedFigure) dummy).setReference(pos);
    }

    protected boolean canCancelOnRightMouseDown(MouseEvent me) {
        return true;
    }

    protected boolean canFinishOnLeftMouseDown(MouseEvent me) {
        return true;
    }

    protected boolean canFinish(String requestType) {
        return false;
    }

    protected boolean shouldCancel(KeyEvent ke) {
        return SWTUtils.matchKey(ke.getState(), ke.keyCode, 0, SWT.ESC);
    }

    protected Request createRequest() {
        if (sheet != null) {
            Point origin = ((IReferencedFigure) sheet.getFigure()).getOrigin();
            Point pos = getCursorPosition();
            Point newPosition = new Point(pos.x - origin.x, pos.y - origin.y);
            Request request = new Request(MindMapUI.REQ_SHOW_LEGEND);
            request.setPrimaryTarget(sheet);
            request.setParameter(MindMapUI.PARAM_PROPERTY_PREFIX
                    + Core.Position, newPosition);
            return request;
        }
        return null;
    }

    public Cursor getCurrentCursor(Point pos, IPart host) {
        return Cursors.HAND;
    }
}