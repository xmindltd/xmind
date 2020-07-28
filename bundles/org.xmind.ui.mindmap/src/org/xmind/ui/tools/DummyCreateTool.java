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
import org.xmind.gef.event.MouseEvent;
import org.xmind.gef.tool.CreateTool;

public abstract class DummyCreateTool extends CreateTool {

    private IFigure dummy = null;

    protected boolean handleMouseMove(MouseEvent me) {
        if (getDummy() != null) {
            updateDummyPosition(getDummy(), getCursorPosition());
            return true;
        }
        return super.handleMouseMove(me);
    }

    protected abstract void updateDummyPosition(IFigure dummy, Point pos);

    public IFigure getDummy() {
        return dummy;
    }

    protected IFigure createDummy() {
        if (dummy == null) {
            if (getStatus().isStatus(GEF.ST_ACTIVE)) {
                dummy = doCreateDummy();
            }
        }
        return dummy;
    }

    protected abstract IFigure doCreateDummy();

    public void finish() {
        Request request = createRequest();
        recover();
        super.finish();
        if (request != null) {
            handleTargetedRequest(request);
            request = null;
        }
    }

    public void cancel() {
        recover();
        super.cancel();
    }

    protected void recover() {
        destroyDummy();
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

    protected abstract Request createRequest();

}