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
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.xmind.core.internal.UserDataConstants;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.mindmap.IMindMap;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.wizards.AbstractMindMapExportPage;
import org.xmind.ui.wizards.AbstractMindMapExportWizard;

/**
 * @author Karelun huang
 */
public class FreeMindExportWizard extends AbstractMindMapExportWizard {

    private static final String PAGE_NAME = "org.xmind.ui.export.freemindeExportWizard"; //$NON-NLS-1$

    private static final String SELECTION_NAME = "org.xmind.ui.export.freemind"; //$NON-NLS-1$

    private static final String FILE_EXT = ".mm"; //$NON-NLS-1$

    private class FreeMindExportWizardPage extends AbstractMindMapExportPage {

        protected FreeMindExportWizardPage() {
            super(PAGE_NAME, WizardMessages.FreeMindPage_title);
            setDescription(WizardMessages.FreeMindPage_description);
        }

        protected FreeMindExportWizard getCastedWizard() {
            return (FreeMindExportWizard) super.getCastedWizard();
        }

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

        @Override
        protected void setDialogFilters(FileDialog dialog,
                List<String> filterNames, List<String> filterExtensions) {
            filterNames.add(0, WizardMessages.FreeMindPage_filterName);
            filterExtensions.add(0, "*" + FILE_EXT); //$NON-NLS-1$
            super.setDialogFilters(dialog, filterNames, filterExtensions);
        }
    }

    private FreeMindExportWizardPage page;

    public FreeMindExportWizard() {
        setWindowTitle(WizardMessages.FreeMindWizard_windowTitle);
        setDialogSettings(
                MindMapUIPlugin.getDefault().getDialogSettings(SELECTION_NAME));
        setDefaultPageImageDescriptor(
                MindMapUI.getImages().getWizBan(IMindMapImages.WIZ_EXPORT));
    }

    protected void addValidPages() {
        addPage(page = new FreeMindExportWizardPage());
    }

    @Override
    protected void doExport(IProgressMonitor monitor, Display display,
            Shell parentShell)
            throws InvocationTargetException, InterruptedException {
        MindMapUIPlugin.getDefault().getUsageDataCollector().trackEvent(
                UserDataConstants.CATEGORY_EXPORT,
                UserDataConstants.EXPORT_TO_FREE_MIND);
        IMindMap mindMap = getSourceMindMap();
        FreeMindExporter exporter = new FreeMindExporter(mindMap.getSheet(),
                getTargetPath());
        monitor.beginTask(null, 100);
        exporter.setMonitor(new SubProgressMonitor(monitor, 99));
        exporter.build();
        launchTargetFile(true, monitor, display, parentShell);
    }

    @Override
    protected String getFormatName() {
        return WizardMessages.FreeMindWizard_formatName;
    }

    @Override
    protected boolean isExtensionCompatible(String path, String extension) {
        return super.isExtensionCompatible(path, extension)
                && FILE_EXT.equalsIgnoreCase(extension);
    }

    @Override
    protected void handleExportException(Throwable e) {
        super.handleExportException(e);
        page.setErrorMessage(e.getLocalizedMessage());
    }

    @Override
    protected String getSuggestedFileName() {
        String fileName = super.getSuggestedFileName();
        return fileName + FILE_EXT;
    }
}
