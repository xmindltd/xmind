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
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.xmind.gef.draw2d.DecoratedShapeFigure;
import org.xmind.gef.draw2d.decoration.IShapeDecorationEx;
import org.xmind.gef.part.Decorator;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.gef.part.IPart;
import org.xmind.ui.decorations.IInfoDecoration;
import org.xmind.ui.internal.decorations.RectangleInfoDecration;
import org.xmind.ui.internal.mindmap.InfoPart;
import org.xmind.ui.mindmap.ISheetPart;
import org.xmind.ui.style.Styles;

public class InformationDecorator extends Decorator {

    private static final InformationDecorator instance = new InformationDecorator();

    private static final int H_MARGIN = 5;

    private static final int V_MARGIN = 5;

    private static final int LINEWIDTH = 1;

    @Override
    public void decorate(IGraphicalPart part, IFigure figure) {
        super.decorate(part, figure);
        if (part instanceof InfoPart) {
            figure.setVisible(((InfoPart) part).hasActions());
        }
        if (figure instanceof DecoratedShapeFigure) {
            DecoratedShapeFigure fig = (DecoratedShapeFigure) figure;
            IShapeDecorationEx decoration = fig.getDecoration();
            IInfoDecoration shape = null;
            if (decoration instanceof IInfoDecoration)
                shape = (IInfoDecoration) decoration;
            if (shape == null)
                shape = new RectangleInfoDecration();
            shape.setLeftMargin(figure, H_MARGIN);
            shape.setTopMargin(figure, V_MARGIN);
            shape.setRightMargin(figure, H_MARGIN);
            shape.setBottomMargin(figure, V_MARGIN);
            shape.setLineColor(figure,
                    new LocalResourceManager(JFaceResources.getResources())
                            .createColor(new RGB(248, 227, 137)));
            shape.setFillColor(figure,
                    getColor(getSheetPart(part),
                            getStyleSelector(getSheetPart(part)),
                            Styles.YellowBoxFillColor, shape.getId(),
                            Styles.DEF_YELLOWBOX_FILL_COLOR));
            shape.setLineAlpha(figure, 255);
            shape.setLineWidth(figure, LINEWIDTH);
            shape.setLineStyle(figure, SWT.LINE_SOLID);
            fig.setDecoration(shape);
        }
    }

    private ISheetPart getSheetPart(IGraphicalPart part) {
        if (part instanceof ISheetPart)
            return (ISheetPart) part;
        IPart parentPart = part.getParent();
        while (parentPart != null && !(parentPart instanceof ISheetPart)) {
            parentPart = parentPart.getParent();
        }

        return (ISheetPart) parentPart;
    }

    public static InformationDecorator getInstance() {
        return instance;
    }
}
