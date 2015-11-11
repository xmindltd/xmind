/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.ui.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.osgi.util.NLS;
import org.xmind.core.Core;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.core.style.IStyle;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.style.StyleUtils;
import org.xmind.ui.util.Logger;

public class WorkbookFactory {

    public static IWorkbook createEmptyWorkbook() {
        return createEmptyWorkbook(null, null);
    }

    public static IWorkbook createEmptyWorkbook(String initialPath) {
        return createEmptyWorkbook(initialPath, null);
    }

    public static IWorkbook createEmptyWorkbook(String initialPath,
            IStyle theme) {
        IWorkbook workbook = Core.getWorkbookBuilder()
                .createWorkbook(initialPath);
        ISheet sheet = workbook.getPrimarySheet();
        sheet.setTitleText(NLS.bind(MindMapMessages.TitleText_Sheet,
                workbook.getSheets().size()));
        ITopic rootTopic = sheet.getRootTopic();
        rootTopic.setTitleText(MindMapMessages.TitleText_CentralTopic);
        rootTopic.setStructureClass("org.xmind.ui.map.unbalanced"); //$NON-NLS-1$

        if (theme == null) {
            theme = MindMapUI.getResourceManager().getDefaultTheme();
        }
        StyleUtils.setTheme(sheet, theme);

        return workbook;
    }

    public static InputStream createEmptyWorkbookStream() {
        return createEmptyWorkbookStream(null, null);
    }

    public static InputStream createEmptyWorkbookStream(String initialPath,
            IStyle theme) {
        IWorkbook workbook = createEmptyWorkbook(initialPath, theme);
        return new ByteArrayInputStream(getWorkbookContent(workbook));
    }

    public static byte[] getWorkbookContent(IWorkbook workbook) {
        ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
        try {
            workbook.save(out);
        } catch (Throwable e) {
            Logger.log(e);
        } finally {
            try {
                out.close();
            } catch (IOException e) {
            }
        }
        return out.toByteArray();
    }

}