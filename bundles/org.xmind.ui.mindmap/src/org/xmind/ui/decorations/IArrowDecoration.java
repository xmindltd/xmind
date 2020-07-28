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
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;
import org.xmind.gef.draw2d.decoration.IDecoration;
import org.xmind.gef.draw2d.geometry.PrecisionPoint;

/**
 * @author MANGOSOFT
 * 
 */
public interface IArrowDecoration extends IDecoration {

    Color getColor();

    void setColor(IFigure figure, Color color);

    int getWidth();

    void setWidth(IFigure figure, int width);

    PrecisionPoint getPosition();

    void setPosition(IFigure figure, PrecisionPoint position);

    double getAngle();

    void setAngle(IFigure figure, double angle);

    Rectangle getPreferredBounds(IFigure figure);

    void reshape(IFigure figure);

}