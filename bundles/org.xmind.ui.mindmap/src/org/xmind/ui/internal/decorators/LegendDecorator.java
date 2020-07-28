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
package org.xmind.ui.internal.decorators;

import static org.xmind.ui.style.StyleUtils.getColor;
import static org.xmind.ui.style.StyleUtils.getStyleSelector;

import org.eclipse.draw2d.IFigure;
import org.xmind.gef.draw2d.DecoratedShapeFigure;
import org.xmind.gef.part.Decorator;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.ui.internal.decorations.RoundedRectTopicDecoration;
import org.xmind.ui.internal.mindmap.LegendPart;
import org.xmind.ui.resources.ColorUtils;
import org.xmind.ui.style.Styles;

public class LegendDecorator extends Decorator {

    private static final int H_MARGIN = 10;

    private static final int V_MARGIN = 6;

    private static final LegendDecorator instance = new LegendDecorator();

    @Override
    public void decorate(IGraphicalPart part, IFigure figure) {
        super.decorate(part, figure);
        if (figure instanceof DecoratedShapeFigure) {
            DecoratedShapeFigure fig = (DecoratedShapeFigure) figure;
            RoundedRectTopicDecoration shape = new RoundedRectTopicDecoration();
            shape.setLeftMargin(figure, H_MARGIN);
            shape.setTopMargin(figure, V_MARGIN);
            shape.setRightMargin(figure, H_MARGIN);
            shape.setBottomMargin(figure, V_MARGIN);
            shape.setCornerSize(figure, H_MARGIN);
            if (part instanceof LegendPart)
                shape.setFillColor(
                        figure,
                        getColor(((LegendPart) part).getOwnedSheet(),
                                getStyleSelector(((LegendPart) part)
                                        .getOwnedSheet()),
                                Styles.LegendFillColor, shape.getId(),
                                Styles.LEGEND_FILL_COLOR));
            shape.setLineColor(figure,
                    ColorUtils.getColor(Styles.LEGEND_LINE_COLOR));
            shape.setLineWidth(figure, 1);
            fig.setDecoration(shape);
        }

    }

    public static LegendDecorator getInstance() {
        return instance;
    }
}