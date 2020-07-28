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

import static org.xmind.ui.style.StyleUtils.getCompositeFont;
import static org.xmind.ui.style.StyleUtils.getTextStyle;

import org.eclipse.draw2d.IFigure;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.TextStyle;
import org.xmind.gef.draw2d.IMinimizable;
import org.xmind.gef.draw2d.ITextFigure;
import org.xmind.gef.graphicalpolicy.IStyleSelector;
import org.xmind.gef.part.Decorator;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.ui.mindmap.INumberingPart;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.style.StyleUtils;
import org.xmind.ui.style.Styles;

public class NumberingDecorator extends Decorator {

    private static final NumberingDecorator instance = new NumberingDecorator();

    protected NumberingDecorator() {
    }

    public void activate(IGraphicalPart part, IFigure figure) {
        super.activate(part, figure);
        figure.setMinimumSize(IMinimizable.DEFAULT_MIN_SIZE);
    }

    public void decorate(IGraphicalPart part, IFigure figure) {
        super.decorate(part, figure);
        if (figure instanceof ITextFigure && part instanceof INumberingPart) {
            ITextFigure textFigure = (ITextFigure) figure;
            String text = ((INumberingPart) part).getFullNumberingText();
            if (text != null)
                textFigure.setText(text);
            decorateTextFigure(part, textFigure);
        }
    }

    protected void decorateTextFigure(IGraphicalPart part, ITextFigure figure) {
        IGraphicalPart parent = getOwnerPart(part);
        if (parent != null)
            part = parent;
        decorateTextFigure(part, StyleUtils.getStyleSelector(part), figure);
    }

    protected void decorateTextFigure(IGraphicalPart ownerPart,
            IStyleSelector ss, ITextFigure figure) {
        TextStyle style = getTextStyle(ownerPart, ss);
        if (style != null) {
            figure.setStyle(style);
        } else {
            figure.setFont(getCompositeFont(ownerPart, ss, JFaceResources
                    .getDefaultFont()));
            figure.setForegroundColor(StyleUtils.getColor(ownerPart, ss,
                    Styles.TextColor, null, Styles.DEF_TEXT_COLOR));
        }
    }

    protected IGraphicalPart getOwnerPart(IGraphicalPart part) {
        if (part.getParent() instanceof ITopicPart)
            return ((ITopicPart) part.getParent()).getOwnerBranch();
        return null;
    }

    public static NumberingDecorator getInstance() {
        return instance;
    }
}