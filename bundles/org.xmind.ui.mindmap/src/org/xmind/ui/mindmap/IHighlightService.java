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
package org.xmind.ui.mindmap;

import java.util.List;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.viewers.ISelection;
import org.xmind.gef.draw2d.IRelayeredPane;
import org.xmind.gef.draw2d.ISkylightLayer;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.gef.service.IViewerService;

public interface IHighlightService extends IViewerService {

    ISkylightLayer getHighlightLayer();

    void setHighlightLayer(ISkylightLayer layer);

    void setHighlightArea(Rectangle r);

    void setHighlightArea(Rectangle r, int alpha);

    Rectangle getHighlightArea();

    IRelayeredPane getRelayeredPane();

    void setRelayeredPane(IRelayeredPane layer);

    void highlight(ISelection selection);

    void highlight(List<IGraphicalPart> toHighlight);

    void highlight(ISelection selection, int alpha);

    void highlight(List<IGraphicalPart> toHighlight, int alpha);

}