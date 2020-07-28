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
import org.eclipse.swt.SWT;
import org.xmind.gef.draw2d.decoration.IDecoration;
import org.xmind.ui.mindmap.IBoundaryPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.resources.ColorUtils;
import org.xmind.ui.style.StyleUtils;
import org.xmind.ui.style.Styles;

public class DecoratedBoundaryFeedback extends DecoratedLineFeedback {

    public DecoratedBoundaryFeedback(IBoundaryPart part) {
        super(part);
        setLineColor(ColorUtils.getColor(MindMapUI.LINE_COLOR_SELECTION));
        setLineStyle(SWT.LINE_SOLID);
        setLineWidthExpansion(MindMapUI.SELECTION_LINE_WIDTH / 2);
    }

    protected String getNewDecorationId() {
        return StyleUtils.getString(getHost(), StyleUtils
                .getStyleSelector(getHost()), Styles.ShapeClass,
                Styles.BOUNDARY_SHAPE_ROUNDEDRECT);
    }

    protected IDecoration createNewDecoration(IFigure figure,
            String decorationId) {
        return StyleUtils.createBoundaryDecoration(getHost(), decorationId);
    }

}