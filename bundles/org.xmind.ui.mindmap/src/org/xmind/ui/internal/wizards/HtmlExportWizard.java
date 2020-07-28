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

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.internal.UserDataConstants;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.mindmap.IMindMap;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.wizards.DocumentExportPageBase;
import org.xmind.ui.wizards.DocumentExportWizard;
import org.xmind.ui.wizards.IExporter;

public class HtmlExportWizard extends DocumentExportWizard {

    private static final String DIALOG_SETTINGS_SECTION_ID = "org.xmind.ui.export.html"; //$NON-NLS-1$

    private static final String HTML_EXPORT_PAGE_NAME = "htmlExportPage"; //$NON-NLS-1$

    private static final List<String> EXTENSIONS = Arrays.asList(".html", //$NON-NLS-1$
            ".htm"); //$NON-NLS-1$

    private static final String FILTER_HTML = "*.html;*.htm"; //$NON-NLS-1$

    private class HtmlExportPage extends DocumentExportPageBase {

        public HtmlExportPage() {
            super(HTML_EXPORT_PAGE_NAME, WizardMessages.HtmlExportPage_title);
            setDescription(WizardMessages.HtmlExportPage_description);
        }

        protected void setDialogFilters(FileDialog dialog,
                List<String> filterNames, List<String> filterExtensions) {
            filterNames.add(0,
                    WizardMessages.HtmlExportPage_FileDialog_HTMLFile);
            filterExtensions.add(0, FILTER_HTML);
            super.setDialogFilters(dialog, filterNames, filterExtensions);
        }

    }

    private HtmlExportPage page;

    public HtmlExportWizard() {
        setWindowTitle(WizardMessages.HtmlExportWizard_windowTitle);
        setDefaultPageImageDescriptor(
                MindMapUI.getImages().getWizBan(IMindMapImages.WIZ_EXPORT));
        setDialogSettings(MindMapUIPlugin.getDefault()
                .getDialogSettings(DIALOG_SETTINGS_SECTION_ID));
    }

    protected void addValidPages() {
        addPage(page = new HtmlExportPage());
    }

    protected void handleExportException(Throwable e) {
        super.handleExportException(e);
        page.setErrorMessage(e.getLocalizedMessage());
    }

    protected IExporter createExporter() {
        IMindMap mindmap = getSourceMindMap();
        ITopic centralTopic = mindmap.getCentralTopic();
        ISheet sheet = mindmap.getSheet();
        HtmlExporter exporter = new HtmlExporter(sheet, centralTopic,
                getTargetPath(), centralTopic.getTitleText());
        exporter.setDialogSettings(getDialogSettings());
        exporter.init();
        return exporter;
    }

    protected String getFormatName() {
        return WizardMessages.HtmlExportWizard_formatName;
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
        MindMapUIPlugin.getDefault().getUsageDataCollector().trackEvent(
                UserDataConstants.CATEGORY_EXPORT,
                UserDataConstants.EXPORT_TO_HTML);
        super.doExport(monitor, display, parentShell);
    }

}
