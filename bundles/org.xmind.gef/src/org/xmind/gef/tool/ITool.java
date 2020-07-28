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
package org.xmind.gef.tool;

import org.xmind.gef.EditDomain;
import org.xmind.gef.IViewer;
import org.xmind.gef.Request;
import org.xmind.gef.event.KeyEvent;
import org.xmind.gef.event.MouseDragEvent;
import org.xmind.gef.event.MouseEvent;
import org.xmind.gef.event.MouseWheelEvent;

/**
 * @author Brian Sun
 */
public interface ITool {

    String getContextId();

    void activate(ITool prevTool);

    void deactivate(ITool nextTool);

    EditDomain getDomain();

    void setDomain(EditDomain domain);

    void focusGained(IViewer viewer);

    void focusLost(IViewer viewer);

    void keyDown(KeyEvent ke, IViewer viewer);

    void keyUp(KeyEvent ke, IViewer viewer);

    void keyTraversed(KeyEvent ke, IViewer viewer);

    void mouseDoubleClick(MouseEvent me, IViewer viewer);

    void mouseDown(MouseEvent me, IViewer viewer);

    void mouseLongPressed(MouseEvent me, IViewer viewer);

    void mouseDrag(MouseDragEvent me, IViewer viewer);

    void mouseHover(MouseEvent me, IViewer viewer);

    void mouseMove(MouseEvent me, IViewer viewer);

    void mouseUp(MouseEvent me, IViewer viewer);

    void mouseEntered(MouseEvent me, IViewer viewer);

    void mouseExited(MouseEvent me, IViewer viewer);

    void mouseWheelScrolled(MouseWheelEvent me, IViewer viewer);

    /**
     * 
     * @param requestType
     * @param targetViewer
     * @deprecated use
     *             {@link org.xmind.gef.EditDomain#handleRequest(String, IViewer)}
     *             instead
     */
    void handleRequest(String requestType, IViewer targetViewer);

    void handleRequest(Request request);

}