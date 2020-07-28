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

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.jface.resource.JFaceResources;
import org.xmind.gef.draw2d.ITextFigure;
import org.xmind.gef.part.Decorator;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.resources.FontUtils;

public class LegendTitleTextDecorator extends Decorator {

    private static final LegendTitleTextDecorator instance = new LegendTitleTextDecorator();

    public void activate(IGraphicalPart part, IFigure figure) {
        super.activate(part, figure);
        if (figure instanceof ITextFigure) {
            ITextFigure fig = (ITextFigure) figure;
            fig.setText(MindMapMessages.Legend);
            fig.setFont(FontUtils.getBold(JFaceResources.DEFAULT_FONT));
            fig.setForegroundColor(ColorConstants.black);
        }
//        if (figure instanceof RotatableWrapLabel) {
//            ((RotatableWrapLabel) figure)
//                    .setRenderStyle(RotatableWrapLabel.NORMAL);
//        }
    }

    public static LegendTitleTextDecorator getInstance() {
        return instance;
    }

}