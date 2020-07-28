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

import static org.xmind.ui.style.StyleUtils.getAlign;
import static org.xmind.ui.style.StyleUtils.getCompositeFont;
import static org.xmind.ui.style.StyleUtils.getTextStyle;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.TextStyle;
import org.xmind.core.ITitled;
import org.xmind.gef.GEF;
import org.xmind.gef.draw2d.IMinimizable;
import org.xmind.gef.draw2d.ITextFigure;
import org.xmind.gef.graphicalpolicy.IStyleSelector;
import org.xmind.gef.part.Decorator;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.ui.style.StyleUtils;
import org.xmind.ui.style.Styles;
import org.xmind.ui.util.MindMapUtils;

public class TitleTextDecorator extends Decorator {

    public void activate(IGraphicalPart part, IFigure figure) {
        super.activate(part, figure);
        figure.setMinimumSize(IMinimizable.DEFAULT_MIN_SIZE);
    }

    public void decorate(IGraphicalPart part, IFigure figure) {
        if (figure instanceof ITextFigure) {
            ITextFigure textFigure = (ITextFigure) figure;
            ITitled titled = getTitledModel(part);
            String text = getText(part, titled);
            if (text != null)
                textFigure.setText(text);
            textFigure.setVisible(isVisible(part, titled));
            decorateTextFigure(part, textFigure);
            decorateTextAlignment(part, textFigure);
            decorateTextCase(part, textFigure);
        }
    }

    private void decorateTextAlignment(IGraphicalPart part,
            ITextFigure textFigure) {
        // TODO Auto-generated method stub
        IGraphicalPart parent = getOwnerPart(part);
        if (parent != null)
            part = parent;
        decorateTextAlignment(part, StyleUtils.getStyleSelector(part),
                textFigure);
    }

    private void decorateTextAlignment(IGraphicalPart part, IStyleSelector ss,
            ITextFigure textFigure) {
        // TODO Auto-generated method stub
        int align = getAlign(part, ss, null);
        if (align != 0)
            textFigure.setTextAlignment(align);
        else
            textFigure.setTextAlignment(PositionConstants.LEFT);
    }

    private void decorateTextCase(IGraphicalPart part, ITextFigure textFigure) {
        IGraphicalPart parent = getOwnerPart(part);
        if (parent != null)
            part = parent;
        decorateTextCase(part, StyleUtils.getStyleSelector(part), textFigure);
    }

    private void decorateTextCase(IGraphicalPart part, IStyleSelector ss,
            ITextFigure textFigure) {
        int textCase = StyleUtils.getCaseValue(part, ss, Styles.TextCase);
        if (textCase != GEF.CASE_EMPTY)
            textFigure.setTextCase(textCase);
        else
            textFigure.setTextCase(GEF.MANUAL);
    }

    private ITitled getTitledModel(IGraphicalPart part) {
        Object model = MindMapUtils.getRealModel(part);
        if (model == null || !(model instanceof ITitled)) {
            model = part.getAdapter(ITitled.class);
        }
        if (model instanceof ITitled)
            return (ITitled) model;
        return null;
    }

    protected String getText(IGraphicalPart part, ITitled titled) {
        if (titled != null) {
            if (!hasTitle(titled))
                return getUntitledText(part, titled);
            return titled.getTitleText();
        }
        return null;
    }

    protected boolean hasTitle(ITitled titled) {
        return titled.hasTitle();
    }

    protected String getUntitledText(IGraphicalPart part, ITitled titled) {
        return titled.getTitleText();
    }

    protected boolean isVisible(IGraphicalPart part, ITitled titled) {
        return titled != null && hasTitle(titled);
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
            figure.setFont(getCompositeFont(ownerPart, ss,
                    JFaceResources.getDefaultFont()));
            figure.setForegroundColor(StyleUtils.getColor(ownerPart, ss,
                    Styles.TextColor, null, Styles.DEF_TEXT_COLOR));
        }
    }

    protected IGraphicalPart getOwnerPart(IGraphicalPart part) {
        return null;
    }

}