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
package org.xmind.ui.internal.editpolicies;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.ZoomManager;
import org.xmind.gef.policy.ScalableEditPolicy;

public class SheetScalablePolicy extends ScalableEditPolicy {

    @Override
    protected Dimension getViewportSize(IGraphicalViewer viewer,
            IFigure viewport, ZoomManager zoomManager) {
        Dimension viewportSize = super.getViewportSize(viewer, viewport,
                zoomManager);
        viewportSize = viewportSize.getExpanded(-100, -100);
        return viewportSize;
    }

}