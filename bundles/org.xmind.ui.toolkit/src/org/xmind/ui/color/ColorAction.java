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
package org.xmind.ui.color;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.graphics.RGB;

/**
 * @author Frank Shaka
 */
public abstract class ColorAction extends Action implements IColorAction {

    private RGB color;

    public ColorAction() {
        setColor(null);
    }

    public ColorAction(RGB color) {
        setColor(color);
    }

    public ColorAction(RGB color, String text) {
        super(text);
        setColor(color);
    }

    public ColorAction(RGB color, int style) {
        super(null, style);
        setColor(color);
    }

    public RGB getColor() {
        return color;
    }

    public void setColor(RGB color) {
        RGB oldColor = this.color;
        if (color != oldColor && (color == null || !color.equals(oldColor))) {
            this.color = color;
            super.setImageDescriptor(ColorBlockImageDescriptor
                    .createFromRGB(color));
            firePropertyChange(COLOR, oldColor, color);
        }
        if (getImageDescriptor() == null) {
            super.setImageDescriptor(ColorBlockImageDescriptor
                    .createFromRGB(color));
        }
    }

}