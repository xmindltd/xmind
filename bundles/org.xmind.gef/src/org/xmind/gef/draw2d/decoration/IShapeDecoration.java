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

public interface IShapeDecoration extends ILineDecoration {

    /**
     * 
     * @return
     */
    int getFillAlpha();

    /**
     * 
     * @return
     */
    Color getFillColor();

    /**
     * 
     * @return
     */
    int getLineAlpha();

    /**
     * 
     * @return
     */
    boolean isGradient();

    /**
     * 
     * @param figure
     * @param ahpla
     */
    void setFillAlpha(IFigure figure, int ahpla);

    /**
     * 
     * @param figure
     * @param c
     */
    void setFillColor(IFigure figure, Color c);

    /**
     * 
     * @param figure
     * @param gradient
     */
    void setGradient(IFigure figure, boolean gradient);

    /**
     * 
     * @param figure
     * @param alpha
     */
    void setLineAlpha(IFigure figure, int alpha);

}