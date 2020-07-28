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
package org.xmind.ui.internal.print;

import java.util.Arrays;
import java.util.List;

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.swt.printing.PrinterData;

public class PrintConstants {

    public static final String PRINT_DIALOG_ID = "org.xmind.ui.PrintDialog"; //$NON-NLS-1$

    public static final String CONTENTWHOLE = "contentWholeWorkbook"; //$NON-NLS-1$

    public static final String NO_BACKGROUND = "noBackground"; //$NON-NLS-1$

    public static final String BORDER = "border"; //$NON-NLS-1$

    public static final String LEFT_MARGIN = "leftMargin"; //$NON-NLS-1$

    public static final String RIGHT_MARGIN = "rightMargin"; //$NON-NLS-1$

    public static final String TOP_MARGIN = "topMargin"; //$NON-NLS-1$

    public static final String BOTTOM_MARGIN = "bottomMargin"; //$NON-NLS-1$

    public static final String MARGIN_UNIT = "marginUnit"; //$NON-NLS-1$

    public static final String HEADER_TEXT = "headerText"; //$NON-NLS-1$

    public static final String HEADER_ALIGN = "headerAlign"; //$NON-NLS-1$

    public static final String HEADER_FONT = "headerFont"; //$NON-NLS-1$

    public static final String FOOTER_TEXT = "footerText"; //$NON-NLS-1$

    public static final String FOOTER_ALIGN = "footerAlign"; //$NON-NLS-1$

    public static final String FOOTER_FONT = "footerFont"; //$NON-NLS-1$

    public static final String LEFT = "left"; //$NON-NLS-1$

    public static final String CENTER = "center"; //$NON-NLS-1$

    public static final String RIGHT = "right"; //$NON-NLS-1$

    public static final double DEFAULT_MARGIN = 0.2d;

    public static final String INCH = "inch"; //$NON-NLS-1$

    public static final String MILLIMETER = "milliter"; //$NON-NLS-1$

    public static final List<String> UNITS = Arrays.asList(INCH, MILLIMETER);

    public static final String DEFAULT_HEADER_ALIGN = CENTER;

    public static final String DEFAULT_FOOTER_ALIGN = RIGHT;

    public static final String DEFAULT_HEADER_TEXT = ""; //$NON-NLS-1$

    public static final String DEFAULT_FOOTER_TEXT = ""; //$NON-NLS-1$

    public static final String ORIENTATION = "orientation"; //$NON-NLS-1$

    public static final String PLUS_VISIBLE = "plusVisible"; //$NON-NLS-1$

    public static final String MINUS_VISIBLE = "minusVisible"; //$NON-NLS-1$

    public static final boolean DEFAULT_PLUS_VISIBLE = true;

    public static final boolean DEFAULT_MINUS_VISIBLE = true;

    public static final String NO_NUMBER = "noPageNumber"; //$NON-NLS-1$

    public static final String WIDTH_PAGES = "widthPages"; //$NON-NLS-1$

    public static final String HEIGHT_PAGES = "heightPages"; //$NON-NLS-1$

    public static final String FILL_HEIGHT = "fillHeight"; //$NON-NLS-1$

    public static final String ASPECT_RATIO_LOCKED = "aspectRatioLocked"; //$NON-NLS-1$

    public static final int DEFAULT_DPI = 120;

    public static final int PAGE_SHORT = (int) ((210.0 / 25.4) * DEFAULT_DPI);

    public static final int PAGE_LENGTH = (int) ((297.0 / 25.4) * DEFAULT_DPI);

    public static final int DEFAULT_ORIENTATION = PrinterData.LANDSCAPE;

    public static final int DEFAULT_WIDTH_PAGES = 1;

    public static final int DEFAULT_HEIGHT_PAGES = 1;

    public static final int MAX_IMAGE_SIZE = 10000 * 10000;

    public static final String MULTI_PAGES = "multiPages"; //$NON-NLS-1$

    public static final String HIDE_DETAILS = "hideDetails"; //$NON-NLS-1$

    public static int toDraw2DAlignment(String alignValue,
            int defaultAlignment) {
        if (LEFT.equals(alignValue))
            return PositionConstants.LEFT;
        if (CENTER.equals(alignValue))
            return PositionConstants.CENTER;
        if (RIGHT.equals(alignValue))
            return PositionConstants.RIGHT;
        return defaultAlignment;
    }

    public static int toPixel(double inch) {
        return (int) (inch * DEFAULT_DPI);
    }

}