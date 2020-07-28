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

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.xmind.core.Core;
import org.xmind.gef.IViewer;
import org.xmind.gef.draw2d.IMinimizable;
import org.xmind.gef.part.Decorator;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.gef.part.IPart;
import org.xmind.gef.part.IRootPart;
import org.xmind.gef.util.Properties;
import org.xmind.ui.internal.figures.PlusMinusFigure;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.IMindMapViewer;
import org.xmind.ui.mindmap.IPlusMinusPart;
import org.xmind.ui.style.Styles;

public class PlusMinusDecorator extends Decorator {

    private static final PlusMinusDecorator instance = new PlusMinusDecorator();

    public void activate(IGraphicalPart part, IFigure figure) {
        super.activate(part, figure);
        figure.setMinimumSize(IMinimizable.DEFAULT_MIN_SIZE);
        figure.setPreferredSize(new Dimension(Styles.PLUS_MINUS_HEIGHT,
                Styles.PLUS_MINUS_HEIGHT));
    }

    public void decorate(IGraphicalPart part, IFigure figure) {
        if (figure instanceof PlusMinusFigure) {
            PlusMinusFigure pmFigure = (PlusMinusFigure) figure;
            if (part instanceof IPlusMinusPart) {
                IPlusMinusPart pm = (IPlusMinusPart) part;
                decorate(pm, pmFigure);
            }
        }
    }

    protected void decorate(IPlusMinusPart pm, PlusMinusFigure figure) {
        IBranchPart branch = pm.getOwnerBranch();
        if (branch != null) {
            figure.setValue(branch.isFolded());
            figure.setBorderValue(branch.getConnections().getLineColor());
            boolean canFold = branch.isPropertyModifiable(Core.TopicFolded);
            if (canFold) {
                Properties properties = getProperties(pm);
                if (properties != null) {

                    boolean isPlus = figure.getValue();
                    boolean plusVisible = properties
                            .getBoolean(IMindMapViewer.PLUS_VISIBLE, true);
                    boolean minusVisible = properties
                            .getBoolean(IMindMapViewer.MINUS_VISIBLE, true);
                    boolean visible = (isPlus && plusVisible)
                            || (!isPlus && minusVisible);

                    figure.setVisible(visible);
                }
            } else {
                figure.setVisible(false);
            }
        } else {
            figure.setValue(false);
        }
    }

    private Properties getProperties(IPlusMinusPart pm) {
        if (pm == null) {
            return null;
        }
        IPart parent = pm.getParent();
        while (parent != null && !(parent instanceof IRootPart)) {
            parent = parent.getParent();
        }
        if (parent != null) {
            IViewer viewer = ((IRootPart) parent).getViewer();
            if (viewer != null) {
                return viewer.getProperties();
            }
        }
        return null;
    }

    public static PlusMinusDecorator getInstance() {
        return instance;
    }

}