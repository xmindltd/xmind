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
import org.xmind.gef.draw2d.decoration.IShapeDecorationEx;
import org.xmind.gef.draw2d.graphics.Path;

public interface ITopicDecoration extends IShapeDecorationEx {

    /**
     * @return the leftMargin
     */
    public int getLeftMargin();

    /**
     * @param margin
     *            the leftMargin to set
     */
    public void setLeftMargin(IFigure figure, int value);

    /**
     * @return the topMargin
     */
    public int getTopMargin();

    /**
     * @param value
     *            the topMargin to set
     */
    public void setTopMargin(IFigure figure, int value);

    public int getRightMargin();

    public void setRightMargin(IFigure figure, int value);

    public int getBottomMargin();

    public void setBottomMargin(IFigure figure, int value);

    public Path createClippingPath(IFigure figure);

}