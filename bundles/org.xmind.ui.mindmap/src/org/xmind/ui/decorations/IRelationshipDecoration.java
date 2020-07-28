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
import org.eclipse.draw2d.geometry.Point;
import org.xmind.gef.draw2d.decoration.IConnectionDecorationEx;
import org.xmind.gef.draw2d.geometry.PrecisionPoint;

public interface IRelationshipDecoration extends IConnectionDecorationEx {

    PrecisionPoint getSourceControlPoint(IFigure figure);

    PrecisionPoint getTargetControlPoint(IFigure figure);

    void setRelativeSourceControlPoint(IFigure figure, Point point);

    void setRelativeTargetControlPoint(IFigure figure, Point point);

    void setSourceControlPointHint(IFigure figure, Double angle, Double amount);

    void setTargetControlPointHint(IFigure figure, Double angle, Double amount);

    IArrowDecoration getArrow1(); //sourceArrow

    void setArrow1(IFigure figure, IArrowDecoration arrow);

    IArrowDecoration getArrow2();

    void setArrow2(IFigure figure, IArrowDecoration arrow);

    PrecisionPoint getTitlePosition(IFigure figure);

}