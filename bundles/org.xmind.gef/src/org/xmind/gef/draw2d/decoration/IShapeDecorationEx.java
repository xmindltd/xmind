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
package org.xmind.gef.draw2d.decoration;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Insets;
import org.xmind.gef.draw2d.geometry.PrecisionPoint;

public interface IShapeDecorationEx extends IShapeDecoration {

    Insets getPreferredInsets(IFigure figure, int width, int height);

    boolean containsPoint(IFigure figure, int x, int y);

    PrecisionPoint getAnchorLocation(IFigure figure, double refX, double refY,
            double expansion);

    PrecisionPoint getAnchorLocation(IFigure figure, int orientation,
            double expansion);

}