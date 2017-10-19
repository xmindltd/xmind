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
package org.xmind.ui.internal.mindmap;

import org.eclipse.draw2d.IFigure;
import org.xmind.gef.part.GraphicalEditPart;
import org.xmind.gef.part.IPart;
import org.xmind.ui.internal.figures.LegendSeparatorFigure;

public class LegendSeparatorPart extends GraphicalEditPart {

    protected IFigure createFigure() {
        return new LegendSeparatorFigure();
    }

    public void setParent(IPart parent) {
        if (getParent() instanceof LegendPart) {
            LegendPart legend = (LegendPart) getParent();
            if (legend.getSeparator() == this)
                legend.setSeparator(null);
        }
        super.setParent(parent);
        if (getParent() instanceof LegendPart) {
            LegendPart legend = (LegendPart) getParent();
            legend.setSeparator(this);
        }
    }

}