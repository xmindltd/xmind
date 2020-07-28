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

import org.eclipse.draw2d.Border;
import org.eclipse.draw2d.IFigure;
import org.eclipse.jface.resource.JFaceResources;
import org.xmind.gef.draw2d.IRotatable;
import org.xmind.gef.draw2d.ITextFigure;
import org.xmind.gef.draw2d.ITitledFigure;
import org.xmind.gef.draw2d.RotatableLineBorder;
import org.xmind.gef.draw2d.RotatableWrapLabel;
import org.xmind.gef.graphicalpolicy.IStyleSelector;
import org.xmind.gef.part.Decorator;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.ILabelPart;
import org.xmind.ui.style.StyleUtils;
import org.xmind.ui.style.Styles;
import org.xmind.ui.util.MindMapUtils;

public class LabelDecorator extends Decorator {

    private static final LabelDecorator instance = new LabelDecorator();

    public void activate(IGraphicalPart part, IFigure figure) {
        super.activate(part, figure);
        figure.setOpaque(true);
        RotatableLineBorder border = new RotatableLineBorder(1);
        figure.setBorder(border);
        figure.setFont(JFaceResources.getDefaultFont());
        if (figure instanceof RotatableWrapLabel) {
            ((RotatableWrapLabel) figure).setSingleLine(true);
        }
    }

    public void decorate(IGraphicalPart part, IFigure figure) {
        super.decorate(part, figure);
        figure.setVisible(isLabelVisible(part, figure));
        IStyleSelector ss = StyleUtils.getStyleSelector(part);
        figure.setBackgroundColor(StyleUtils.getColor(part, ss,
                Styles.FillColor, null, Styles.LABEL_FILL_COLOR));
        figure.setForegroundColor(StyleUtils.getColor(part, ss,
                Styles.TextColor, null, Styles.LABEL_TEXT_COLOR));
        Border border = figure.getBorder();
        if (border instanceof RotatableLineBorder) {
            ((RotatableLineBorder) border).setColor(StyleUtils.getColor(part,
                    ss, Styles.LineColor, null, Styles.LABEL_BORDER_COLOR));
        }
        if (part instanceof ILabelPart) {
            ILabelPart label = (ILabelPart) part;
            ITextFigure textFigure = getTextFigure(figure);
            if (textFigure != null) {
                textFigure.setText(label.getLabelText());
            }
            if (figure instanceof IRotatable) {
                IBranchPart branch = label.getOwnedBranch();
                if (branch != null) {
                    IStyleSelector bss = branch.getBranchPolicy()
                            .getStyleSelector(branch);
                    double angle = StyleUtils.getDouble(branch, bss,
                            Styles.RotateAngle, 0);
                    ((IRotatable) figure).setRotationDegrees(angle);
                }
            }
        }
    }

    private boolean isLabelVisible(IGraphicalPart part, IFigure figure) {
        IBranchPart branch = MindMapUtils.findBranch(part);
        if (branch != null) {
            IBranchPart parent = branch.getParentBranch();
            if (parent != null) {
                IStyleSelector ss = StyleUtils.getStyleSelector(parent);
                String value = ss.getStyleValue(parent,
                        Styles.HideChildrenLabels);
                if (Boolean.TRUE.toString().equals(value)) {
                    return false;
                }
            }
        }
        return true;
    }

    private ITextFigure getTextFigure(IFigure figure) {
        if (figure instanceof ITextFigure)
            return (ITextFigure) figure;
        if (figure instanceof ITitledFigure)
            return ((ITitledFigure) figure).getTitle();
        return null;
    }

    public static LabelDecorator getInstance() {
        return instance;
    }
}