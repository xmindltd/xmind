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
package org.xmind.gef.draw2d;

import org.eclipse.draw2d.IFigure;
import org.eclipse.swt.graphics.TextStyle;

public interface ITextFigure extends IFigure {

    /**
     * Returns the text of this figure.
     * 
     * @return the text of this figure
     */
    public String getText();

    /**
     * Sets the text of this figure.
     * 
     * @param text
     *            the text to set
     */
    public void setText(String text);

    /**
     * Gets the horizontal alignment of the text.
     * 
     * @return one of the PositionConstants value
     * @see PositionConstants#LEFT;
     * @see PositionConstants#RIGHT;
     * @see PositionConstants#CENTER;
     */
    public int getTextAlignment();

    /**
     * Sets the horizontal alignment of the text.
     * 
     * @param align
     *            one of the PositionConstants value
     * @see PositionConstants#LEFT;
     * @see PositionConstants#RIGHT;
     * @see PositionConstants#CENTER;
     */
    public void setTextAlignment(int align);

    /**
     * Gets the case of the text.
     * 
     * @return one of the text case value
     * 
     * @see org.xmind.gef.GEF#MANUAL
     * @see org.xmind.gef.GEF#UPPERCASE
     * @see org.xmind.gef.GEF#LOWERCASE
     * @see org.xmind.gef.GEF#CAPITALIZE
     */
    public int getTextCase();

    /**
     * Sets the case of the text.
     * 
     * @param textCase
     * 
     * @see org.xmind.gef.GEF#MANUAL
     * @see org.xmind.gef.GEF#UPPERCASE
     * @see org.xmind.gef.GEF#LOWERCASE
     * @see org.xmind.gef.GEF#CAPITALIZE
     */
    public void setTextCase(int textCase);

    /**
     * Returns the currently used line spacing of multiple lines of text.
     * 
     * @return the currently used line spacing of multiple lines of text.
     */
    public int getLineSpacing();

    /**
     * Sets the line spacing of multiple lines of text.
     * 
     * @param spacing
     *            The spacing to set.
     */
    public void setLineSpacing(int spacing);

    /**
     * @return the style
     */
    public TextStyle getStyle();

    /**
     * @param style
     *            the style to set
     */
    public void setStyle(TextStyle style);

}