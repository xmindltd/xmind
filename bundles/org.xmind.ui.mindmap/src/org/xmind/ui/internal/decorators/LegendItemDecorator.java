/*
 * *****************************************************************************
 * * Copyright (c) 2006-2012 XMind Ltd. and others. This file is a part of XMind
 * 3. XMind releases 3 and above are dual-licensed under the Eclipse Public
 * License (EPL), which is available at
 * http://www.eclipse.org/legal/epl-v10.html and the GNU Lesser General Public
 * License (LGPL), which is available at http://www.gnu.org/licenses/lgpl.html
 * See https://www.xmind.net/license.html for details. Contributors: XMind Ltd. -
 * initial API and implementation
 *******************************************************************************/
package org.xmind.ui.internal.decorators;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.Util;
import org.xmind.gef.part.Decorator;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.ui.internal.figures.LegendItemFigure;
import org.xmind.ui.mindmap.ILegendItemPart;
import org.xmind.ui.resources.FontUtils;

public class LegendItemDecorator extends Decorator {

    private static final LegendItemDecorator instance = new LegendItemDecorator();

    public void activate(IGraphicalPart part, IFigure figure) {
        super.activate(part, figure);
        figure.setFont(FontUtils.getNewHeight(JFaceResources.getDefaultFont(),
                Util.isMac() ? 10 : 8));
        figure.setForegroundColor(ColorConstants.black);
        if (figure instanceof LegendItemFigure) {
            ((LegendItemFigure) figure).getIcon().setPreferredSize(15, 15);
        }
    }

    public void decorate(IGraphicalPart part, IFigure figure) {
        super.decorate(part, figure);
        if (figure instanceof LegendItemFigure) {
            LegendItemFigure itemFigure = (LegendItemFigure) figure;
            if (part instanceof ILegendItemPart) {
                ILegendItemPart item = (ILegendItemPart) part;
                itemFigure.setIconImage(item.getIconImage());
                itemFigure.setSVGData(item.getSVGData());
                itemFigure.setText(item.getDescription());
            }
        }
    }

    public void deactivate(IGraphicalPart part, IFigure figure) {
        if (figure instanceof LegendItemFigure) {
            ((LegendItemFigure) figure).setIconImage(null);
            ((LegendItemFigure) figure).setSVGData(null);
        }
        super.deactivate(part, figure);
    }

    public static LegendItemDecorator getInstance() {
        return instance;
    }

}
