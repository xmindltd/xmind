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

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;

public interface IDecoration {

    /**
     * 
     * @return
     */
    String getId();

    /**
     * 
     * @param id
     */
    void setId(String id);

    /**
     * 
     * @param figure
     * @param graphics
     */
    void paint(IFigure figure, Graphics graphics);

    /**
     * 
     * @return
     */
    int getAlpha();

    /**
     * 
     * @param figure
     * @param alpha
     */
    void setAlpha(IFigure figure, int alpha);

    /**
     * 
     * @return
     */
    boolean isVisible();

    /**
     * 
     * @param figure
     * @param visible
     */
    void setVisible(IFigure figure, boolean visible);

    /**
     * 
     */
    void invalidate();

    /**
     * 
     * @param figure
     */
    void validate(IFigure figure);

}