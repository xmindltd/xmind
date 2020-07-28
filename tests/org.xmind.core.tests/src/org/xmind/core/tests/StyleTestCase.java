/* ******************************************************************************
 * Copyright (c) 2006-2016 XMind Ltd. and others.
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
package org.xmind.core.tests;

import static org.junit.Assert.*;

import org.junit.*;
import org.xmind.core.*;
import org.xmind.core.style.*;

/**
 * @author Frank Shaka
 *
 */
public class StyleTestCase {

    @Test
    public void testStyleAutoManagementByReferences() {
        IWorkbook workbook = Core.getWorkbookBuilder().createWorkbook();

        ISheet sheet = workbook.getPrimarySheet();

        IStyleSheet styleSheet = workbook.getStyleSheet();

        IStyle style = styleSheet.createStyle(IStyle.MAP);
        assertNotNull(style);
        style.setProperty("svg:fill", "#ff8800");
        assertNull(styleSheet.findOwnedGroup(style));

        /// style not added to style sheet yet,
        /// => reference to it does not auto add it to style sheet
        sheet.setStyleId(style.getId());
        assertNull(styleSheet.findOwnedGroup(style));
        sheet.setStyleId(null);
        assertNull(styleSheet.findOwnedGroup(style));

        /// manually add style to style sheet
        styleSheet.addStyle(style, IStyleSheet.NORMAL_STYLES);
        assertEquals(IStyleSheet.NORMAL_STYLES, styleSheet.findOwnedGroup(style));

        /// now reference to the style will cause workbook remember which group
        /// it should be in
        sheet.setStyleId(style.getId());
        assertEquals(IStyleSheet.NORMAL_STYLES, styleSheet.findOwnedGroup(style));

        /// dereference to the style will cause it auto removed
        sheet.setStyleId(null);
        assertNull(styleSheet.findOwnedGroup(style));

        /// style has been added to style sheet before
        /// => reference to it should auto add it to style sheet
        sheet.setStyleId(style.getId());
        assertEquals(IStyleSheet.NORMAL_STYLES, styleSheet.findOwnedGroup(style));

    }

}
