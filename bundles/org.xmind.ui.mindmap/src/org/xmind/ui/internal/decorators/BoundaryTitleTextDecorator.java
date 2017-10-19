/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.ui.internal.decorators;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.swt.graphics.Color;
import org.xmind.core.ITitled;
import org.xmind.gef.draw2d.ITextFigure;
import org.xmind.gef.graphicalpolicy.IStyleSelector;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.mindmap.IBoundaryPart;
import org.xmind.ui.style.StyleUtils;
import org.xmind.ui.style.Styles;

public class BoundaryTitleTextDecorator extends TitleTextDecorator {

    private static final BoundaryTitleTextDecorator instance = new BoundaryTitleTextDecorator();

    public void activate(IGraphicalPart part, IFigure figure) {
        super.activate(part, figure);
        figure.setBorder(new LineBorder());
        figure.setOpaque(true);
    }

    @Override
    public void decorate(IGraphicalPart part, IFigure figure) {
        super.decorate(part, figure);
    }

    protected String getUntitledText(IGraphicalPart part, ITitled titled) {
        return MindMapMessages.TitleText_Boundary;
    }

    @Override
    protected void decorateTextFigure(IGraphicalPart ownerPart,
            IStyleSelector ss, ITextFigure figure) {
        super.decorateTextFigure(ownerPart, ss, figure);
        String decorationId = StyleUtils.getString(ownerPart, ss,
                Styles.ShapeClass, Styles.BOUNDARY_SHAPE_RECT);
        figure.setBackgroundColor(getFillColor(ownerPart, ss, decorationId));
        LineBorder border = (LineBorder) figure.getBorder();
        Color oldLineColor = border.getColor();
        Color newLineColor = getLineColor(ownerPart, ss, decorationId);
        if (!newLineColor.equals(oldLineColor)) {
            border.setColor(newLineColor);
            figure.repaint();
        }
        int oldLineWidth = border.getWidth();
        int newLineWidth = getLineWidth(ownerPart, ss, decorationId);
        if (newLineWidth != oldLineWidth) {
            border.setWidth(newLineWidth);
            figure.revalidate();
            figure.repaint();
        }
    }

    private int getLineWidth(IGraphicalPart part, IStyleSelector ss,
            String decorationId) {
        return StyleUtils.getInteger(part, ss, Styles.LineWidth, decorationId,
                Styles.DEF_BOUNDARY_LINE_WIDTH);
    }

    protected Color getLineColor(IGraphicalPart part, IStyleSelector ss,
            String decorationId) {
        return StyleUtils.getColor(part, ss, Styles.LineColor, decorationId,
                Styles.DEF_BOUNDARY_LINE_COLOR);
    }

    protected Color getFillColor(IGraphicalPart part, IStyleSelector ss,
            String decorationId) {
        return StyleUtils.getColor(part, ss, Styles.FillColor, decorationId,
                Styles.DEF_BOUNDARY_FILL_COLOR);
    }

    protected IGraphicalPart getOwnerPart(IGraphicalPart part) {
        if (part.getParent() instanceof IBoundaryPart)
            return (IBoundaryPart) part.getParent();
        return super.getOwnerPart(part);
    }

    public static BoundaryTitleTextDecorator getInstance() {
        return instance;
    }
}