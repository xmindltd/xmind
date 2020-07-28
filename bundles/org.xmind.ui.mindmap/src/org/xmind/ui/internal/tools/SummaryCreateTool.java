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
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.graphics.Cursor;
import org.xmind.gef.Request;
import org.xmind.gef.part.IPart;
import org.xmind.ui.mindmap.MindMapUI;

public class SummaryCreateTool extends TopicAreaSelectTool {

    Request request = null;

    public void finish() {
        super.finish();
        if (request != null && request.hasTargets()) {
            getDomain().handleRequest(request);
            request = null;
        }
    }

    protected void applySelection() {
        super.applySelection();
        if (getMergedSelection().isEmpty()) {
            request = null;
        } else {
            request = createRequest();
        }
    }

    protected Request createRequest() {
        return createTargetedRequest(MindMapUI.REQ_CREATE_SUMMARY,
                getTargetViewer(), false);
    }

    public Cursor getCurrentCursor(Point pos, IPart host) {
        return Cursors.CROSS;
    }
}