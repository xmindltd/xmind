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
package org.xmind.ui.decorations;

import org.eclipse.draw2d.IFigure;
import org.xmind.gef.draw2d.IAnchor;
import org.xmind.gef.draw2d.decoration.ICompoundDecoration;
import org.xmind.gef.draw2d.decoration.ICorneredDecoration;
import org.xmind.gef.draw2d.decoration.ILineDecoration;

public interface IBranchConnections extends ICompoundDecoration,
        ILineDecoration, ICorneredDecoration {

    void setSourceOrientation(IFigure figure, int orientation);

    void setSourceExpansion(IFigure figure, int expansion);

    void setTapered(IFigure figure, boolean tapered);

    int getSourceExpansion();

    int getSourceOrientation();

    boolean isTapered();

    IAnchor getSourceAnchor();

    void setSourceAnchor(IFigure figure, IAnchor anchor);

    void rerouteAll(IFigure figure);

//    int getMinimumMajorSpacing(IFigure figure);

}