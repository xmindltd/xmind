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
package org.xmind.ui.tools;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.xmind.gef.GEF;
import org.xmind.gef.Request;
import org.xmind.gef.draw2d.IReferencedFigure;
import org.xmind.gef.event.MouseDragEvent;
import org.xmind.gef.tool.MoveTool;

public abstract class DummyMoveTool extends MoveTool {

    private IFigure dummy = null;

    private Point dummyStartLoc = null;

    protected void start() {
        if (createsDummyOnActivated()) {
            doCreateDummy();
        }
    }

    protected boolean createsDummyOnActivated() {
        return true;
    }

    protected boolean usesRelativeLocation() {
        return true;
    }

    public IFigure getDummy() {
        return dummy;
    }

    protected void doCreateDummy() {
        if (dummy != null)
            return;

        if (!getStatus().isStatus(GEF.ST_ACTIVE))
            return;

        dummy = createDummy();
    }

    protected abstract IFigure createDummy();

    protected void onMoving(Point currentPos, MouseDragEvent me) {
        updateDummyPosition(currentPos);
    }

    protected void updateDummyPosition(Point pos) {
        IFigure fig = getDummy();
        if (fig != null) {
            Point cursorStart = getStartingPosition();
            Point dummyStart = getDummyStartLoc();
            if (usesRelativeLocation() && cursorStart != null
                    && dummyStart != null) {
                int x = pos.x - cursorStart.x + dummyStart.x;
                int y = pos.y - cursorStart.y + dummyStart.y;
                if (fig instanceof IReferencedFigure) {
                    ((IReferencedFigure) fig).setReference(x, y);
                } else {
                    fig.setLocation(new Point(x, y));
                }
            } else {
                if (fig instanceof IReferencedFigure) {
                    ((IReferencedFigure) fig).setReference(pos);
                } else {
                    fig.setLocation(pos);
                }
            }
        }
    }

    protected Point getDummyStartLoc() {
        if (dummyStartLoc == null) {
            IFigure fig = getDummy();
            if (fig != null) {
                if (fig instanceof IReferencedFigure) {
                    dummyStartLoc = ((IReferencedFigure) fig).getReference();
                } else {
                    dummyStartLoc = fig.getBounds().getLocation();
                }
            }
        }
        return dummyStartLoc;
    }

    public void finish() {
        Request request = createRequest();
        super.finish();
        if (request != null) {
            getDomain().handleRequest(request);
        }
    }

    protected abstract Request createRequest();

    protected void end() {
        destroyDummy();
        dummyStartLoc = null;
        super.end();
    }

    protected void destroyDummy() {
        if (dummy != null) {
            destroyDummy(dummy);
            dummy = null;
        }
    }

    protected void destroyDummy(IFigure dummy) {
        if (dummy.getParent() != null)
            dummy.getParent().remove(dummy);
    }

}