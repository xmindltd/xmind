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
package org.xmind.ui.internal.wizards;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.xmind.core.IWorkbook;
import org.xmind.core.internal.UserDataConstants;
import org.xmind.core.util.FileUtils;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.IWorkbookRef;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.wizards.AbstractMindMapExportPage;
import org.xmind.ui.wizards.AbstractMindMapExportWizard;

public class XMind2008ExportWizard extends AbstractMindMapExportWizard {

    private static final String DIALOG_SETTINGS_SECTION_ID = "org.xmind.ui.export.xmind2008"; //$NON-NLS-1$

    private static final String PAGE_NAME = "htmlExportPage"; //$NON-NLS-1$

    private static final String FILE_EXT = ".xmap"; //$NON-NLS-1$

    private static final String FILTER = "*" + FILE_EXT; //$NON-NLS-1$

    private class XMind2008ExportPage extends AbstractMindMapExportPage {

        /**
         * @param pageName
         * @param title
         */
        public XMind2008ExportPage() {
            super(PAGE_NAME, WizardMessages.XMind2008ExportPage_title);
        }

        /*
         * (non-Javadoc)
         * @see
         * org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt
         * .widgets.Composite)
         */
        public void createControl(Composite parent) {
            Composite composite = new Composite(parent, SWT.NONE);
            GridLayout layout = new GridLayout();
            layout.verticalSpacing = 15;
            composite.setLayout(layout);
            setControl(composite);

            Control fileGroup = createFileControls(composite);
            fileGroup.setLayoutData(
                    new GridData(SWT.FILL, SWT.FILL, true, false));
        }

        /*
         * (non-Javadoc)
         * @see
         * org.xmind.ui.wizards.AbstractExportPage#setDialogFilters(org.eclipse
         * .swt.widgets.FileDialog, java.util.List, java.util.List)
         */
        @Override
        protected void setDialogFilters(FileDialog dialog,
                List<String> filterNames, List<String> filterExtensions) {
            filterNames.add(0, WizardMessages.XMind2008ExportPage_filterName);
            filterExtensions.add(0, FILTER);
            super.setDialogFilters(dialog, filterNames, filterExtensions);
        }

    }

    private IWorkbook sourceWorkbook;

    private XMind2008ExportPage page;

    public XMind2008ExportWizard() {
        setWindowTitle(WizardMessages.XMind2008ExportWizard_windowTitle);
        setDefaultPageImageDescriptor(
                MindMapUI.getImages().getWizBan(IMindMapImages.WIZ_EXPORT));
        setDialogSettings(MindMapUIPlugin.getDefault()
                .getDialogSettings(DIALOG_SETTINGS_SECTION_ID));
    }

    /*
     * (non-Javadoc)
     * @see
     * org.xmind.ui.wizards.AbstractMindMapExportWizard#setSourceEditor(org.
     * xmind.gef.ui.editor.IGraphicalEditor)
     */
    @Override
    public void setSourceEditor(IGraphicalEditor sourceEditor) {
        super.setSourceEditor(sourceEditor);
        this.sourceWorkbook = findWorkbook(sourceEditor);
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.ui.wizards.AbstractMindMapExportWizard#hasSource()
     */
    @Override
    public boolean hasSource() {
        return getSourceEditor() != null && sourceWorkbook != null;
    }

    /**
     * @param editor
     * @return
     */
    private IWorkbook findWorkbook(IGraphicalEditor editor) {
        if (editor == null)
            return null;
        return (IWorkbook) editor.getAdapter(IWorkbook.class);
    }

    protected void addValidPages() {
        addPage(page = new XMind2008ExportPage());
    }

    protected void handleExportException(Throwable e) {
        super.handleExportException(e);
        page.setErrorMessage(e.getLocalizedMessage());
    }

    protected String getFormatName() {
        return WizardMessages.XMind2008ExportWizard_formatName;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.xmind.ui.wizards.AbstractMindMapExportWizard#doExport(org.eclipse
     * .core.runtime.IProgressMonitor, org.eclipse.swt.widgets.Display,
     * org.eclipse.swt.widgets.Shell)
     */
    protected void doExport(IProgressMonitor monitor, Display display,
            Shell parentShell)
            throws InvocationTargetException, InterruptedException {
        MindMapUIPlugin.getDefault().getUsageDataCollector().trackEvent(
                UserDataConstants.CATEGORY_EXPORT,
                UserDataConstants.EXPORT_TO_X_MIND2008);
        XMind2008Exporter exporter = new XMind2008Exporter(sourceWorkbook,
                getTargetPath());
        exporter.setMonitor(monitor);
        exporter.export();
    }

    @Override
    protected String getSuggestedFileName() {
        String suggestedName = null;
        IGraphicalEditor editor = getSourceEditor();
        IWorkbookRef workbookRef = editor.getAdapter(IWorkbookRef.class);
        if (workbookRef != null) {
            suggestedName = workbookRef.getName();
        }
        if (suggestedName == null) {
            suggestedName = getSourceEditor().getTitle();
        }
        return FileUtils.getNoExtensionFileName(
                new File(suggestedName).getName()) + FILE_EXT;
    }

}
