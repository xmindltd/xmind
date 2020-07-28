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
/**
 * 
 */
package org.xmind.ui.color;

import org.eclipse.swt.graphics.RGB;

/**
 * @author Frank Shaka
 */
public class ColorPickerConfigurer {

    /**
     * Style bit for normal popup color picker representation (value is
     * <code>0</code>).
     */
    public static final int NORMAL = ColorPicker.NORMAL;

    /**
     * Style bit for showing "Automatic" selection in popup color picker (value
     * is <code>1</code>).
     */
    public static final int SHOW_AUTO = ColorPicker.AUTO;

    /**
     * Style bit for showing "None" selection in popup color picker (value is
     * <code>1 << 1</code>).
     */
    public static final int SHOW_NONE = ColorPicker.NONE;

    /**
     * Style bit for showing "Custom" selection in popup color picker (value is
     * <code>1 << 2</code>).
     */
    public static final int SHOW_CUSTOM = ColorPicker.CUSTOM;

    /**
     * Special value to determine whether a value is set or not.
     */
    private static final Object NOT_SET = new Object();

    private int popupStyle = NORMAL;

    private PaletteContents palette = PaletteContents.getDefault();

    private Object autoValue = NOT_SET;

    private Object noneValue = NOT_SET;

    private RGB autoColor = null;

    /**
     * Constructs a color picker configurer with default values.
     */
    public ColorPickerConfigurer() {
    }

    public ColorPickerConfigurer popupStyle(int popupStyle) {
        this.popupStyle = popupStyle;
        return this;
    }

    public ColorPickerConfigurer palette(PaletteContents palette) {
        this.palette = palette;
        return this;
    }

    public ColorPickerConfigurer autoValue(Object autoValue) {
        this.autoValue = autoValue;
        return this;
    }

    public ColorPickerConfigurer noneValue(Object noneValue) {
        this.noneValue = noneValue;
        return this;
    }

    public ColorPickerConfigurer autoColor(RGB autoColor) {
        this.autoColor = autoColor;
        return this;
    }

    public boolean isAutoValueSet() {
        return this.autoValue != NOT_SET;
    }

    public boolean isNoneValueSet() {
        return this.noneValue != NOT_SET;
    }

    /**
     * @return the popupStyle
     */
    public int getPopupStyle() {
        return popupStyle;
    }

    /**
     * @return the autoColor
     */
    public RGB getAutoColor() {
        return autoColor;
    }

    /**
     * @return the autoValue
     */
    public Object getAutoValue() {
        return autoValue;
    }

    /**
     * @return the noneValue
     */
    public Object getNoneValue() {
        return noneValue;
    }

    /**
     * @return the palette
     */
    public PaletteContents getPalette() {
        return palette;
    }

}
