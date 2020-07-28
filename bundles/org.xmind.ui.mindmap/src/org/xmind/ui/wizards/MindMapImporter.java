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
package org.xmind.ui.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.xmind.core.Core;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.core.style.IStyle;
import org.xmind.core.style.IStyleSheet;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.editor.MME;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.MindMapUtils;

public abstract class MindMapImporter {

    private String sourcePath;

    private IWorkbook targetWorkbook;

    private List<ISheet> targetSheets;

    private final boolean newWorkbook;

    private IProgressMonitor monitor;

    private List<Map.Entry<Throwable, String>> errors = null;

    public MindMapImporter(String sourcePath) {
        this(sourcePath, null);
    }

    public MindMapImporter(String sourcePath, IWorkbook targetWorkbook) {
        this.sourcePath = sourcePath;
        this.targetSheets = new ArrayList<ISheet>();
        this.newWorkbook = targetWorkbook == null;
        if (targetWorkbook == null) {
            this.targetWorkbook = Core.getWorkbookBuilder()
                    .createWorkbook(MME.createTempStorage());
        } else {
            this.targetWorkbook = targetWorkbook;
        }
    }

    public boolean isNewWorkbook() {
        return newWorkbook;
    }

    public IProgressMonitor getMonitor() {
        if (monitor == null)
            monitor = new NullProgressMonitor();
        return monitor;
    }

    public void setMonitor(IProgressMonitor monitor) {
        this.monitor = monitor;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public IWorkbook getTargetWorkbook() {
        return targetWorkbook;
    }

    public abstract void build()
            throws InvocationTargetException, InterruptedException;

    protected void postBuilded() {
        initStyles();
    }

    private void initStyles() {
        for (ISheet sheet : getTargetWorkbook().getSheets()) {
            IStyle appliedTheme = createAppliedTheme(getTargetWorkbook(),
                    MindMapUI.getResourceManager().getDefaultTheme());
            if (appliedTheme != null) {
                sheet.setThemeId(appliedTheme.getId());

                List<ITopic> topics = MindMapUtils.getAllTopics(sheet, true,
                        true);
                for (ITopic topic : topics) {
                    topic.setStyleId(null);
                }
            }
        }
    }

    private IStyle createAppliedTheme(IWorkbook workbook, IStyle sourceTheme) {
        IStyleSheet ss = workbook.getStyleSheet();
        return ss.importStyle(sourceTheme);
    }

    public List<ISheet> getTargetSheets() {
        return targetSheets;
    }

    protected void addTargetSheet(ISheet sheet) {
        if (newWorkbook) {
            targetWorkbook.addSheet(sheet);
            if (targetSheets.isEmpty()) {
                targetWorkbook.removeSheet(targetWorkbook.getPrimarySheet());
            }
        }
        targetSheets.add(sheet);
    }

    protected String getSuggestedSheetTitle() {
        return NLS.bind(MindMapMessages.TitleText_Sheet,
                newWorkbook ? 1 : getTargetWorkbook().getSheets().size() + 1);
    }

    public List<Map.Entry<Throwable, String>> getErrors() {
        return errors;
    }

    protected void log(final Throwable e, final String message) {
        if (errors == null)
            errors = new ArrayList<Map.Entry<Throwable, String>>();
        errors.add(new Map.Entry<Throwable, String>() {

            public String setValue(String value) {
                return message;
            }

            public String getValue() {
                return message;
            }

            public Throwable getKey() {
                return e;
            }
        });
    }

    public void dispose() {
    }
}
