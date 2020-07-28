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
import org.eclipse.swt.graphics.Color;

public interface ILineDecoration extends IDecoration {

    /**
     * @return the color
     */
    Color getLineColor();

    /**
     * 
     * @return
     */
    int getLineStyle();

    /**
     * @return the width
     */
    int getLineWidth();

    /**
     * 
     * @param figure
     * @param color
     */
    void setLineColor(IFigure figure, Color color);

    /**
     * 
     * @param figure
     * @param width
     */
    void setLineWidth(IFigure figure, int width);

    /**
     * 
     * @param figure
     * @param style
     */
    void setLineStyle(IFigure figure, int style);

}