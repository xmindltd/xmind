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
package org.xmind.ui.internal.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.mindmap.IMindMap;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.wizards.DocumentExportPageBase;
import org.xmind.ui.wizards.DocumentExportWizard;
import org.xmind.ui.wizards.IExporter;

public class MarkDownExportWizard extends DocumentExportWizard {

    private static final String DIALOG_SETTINGS_SECTION_ID = "org.xmind.ui.export.html"; //$NON-NLS-1$

    private static final String MARKDOWN_EXPORT_PAGE_NAME = "markDownExportPage"; //$NON-NLS-1$

    private static final List<String> EXTENSIONS = Arrays.asList(".md"//$NON-NLS-1$
    ); //$NON-NLS-1$

    private static final String FILTER_MARKDOWN = "*.md"; //$NON-NLS-1$

    private class MarkDownExportPage extends DocumentExportPageBase {

        public MarkDownExportPage() {
            super(MARKDOWN_EXPORT_PAGE_NAME,
                    WizardMessages.MarkDownExportPage_title);
            setDescription(WizardMessages.MarkDownExportPage_description);
        }

        protected void setDialogFilters(FileDialog dialog,
                List<String> filterNames, List<String> filterExtensions) {
            filterNames.add(0,
                    WizardMessages.MarkDownExportPage_FileDialog_HTMLFile);
            filterExtensions.add(0, FILTER_MARKDOWN);
            super.setDialogFilters(dialog, filterNames, filterExtensions);
        }

    }

    private MarkDownExportPage page;

    public MarkDownExportWizard() {
        setWindowTitle(WizardMessages.MarkDownExportWizard_windowTitle);
        setDefaultPageImageDescriptor(
                MindMapUI.getImages().getWizBan(IMindMapImages.WIZ_EXPORT));
        setDialogSettings(MindMapUIPlugin.getDefault()
                .getDialogSettings(DIALOG_SETTINGS_SECTION_ID));
    }

    protected void addValidPages() {
        addPage(page = new MarkDownExportPage());
    }

    protected void handleExportException(Throwable e) {
        super.handleExportException(e);
        page.setErrorMessage(e.getLocalizedMessage());
    }

    protected IExporter createExporter() {
        IMindMap mindmap = getSourceMindMap();
        ITopic centralTopic = mindmap.getCentralTopic();
        ISheet sheet = mindmap.getSheet();
        MarkDownExporter exporter = new MarkDownExporter(sheet, centralTopic,
                getTargetPath(), centralTopic.getTitleText());
        exporter.setDialogSettings(getDialogSettings());
        exporter.init();
        return exporter;
    }

    protected String getFormatName() {
        return WizardMessages.MarkDownExportWizard_formatName;
    }

    protected boolean isExtensionCompatible(String path, String extension) {
        return super.isExtensionCompatible(path, extension)
                && EXTENSIONS.contains(extension.toLowerCase());
    }

    protected String getSuggestedFileName() {
        return super.getSuggestedFileName() + EXTENSIONS.get(0);
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.ui.wizards.DocumentExportWizard#doExport(org.eclipse.core.
     * runtime.IProgressMonitor, org.eclipse.swt.widgets.Display,
     * org.eclipse.swt.widgets.Shell)
     */
    @Override
    protected void doExport(IProgressMonitor monitor, Display display,
            Shell parentShell)
            throws InvocationTargetException, InterruptedException {
        MindMapUIPlugin.getDefault().getUsageDataCollector()
                .increase("ExportToHtmlCount"); //$NON-NLS-1$
        super.doExport(monitor, display, parentShell);
    }

}
