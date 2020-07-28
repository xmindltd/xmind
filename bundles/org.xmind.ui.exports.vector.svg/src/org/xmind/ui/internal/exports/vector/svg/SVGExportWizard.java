/* ******************************************************************************
 * Copyright (c) 2006-2013 XMind Ltd. and others.
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
package org.xmind.ui.internal.exports.vector.svg;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.xmind.core.internal.UserDataConstants;
import org.xmind.ui.mindmap.IMindMap;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.wizards.AbstractMindMapExportPage;
import org.xmind.ui.wizards.DocumentExportWizard;
import org.xmind.ui.wizards.ExportContants;
import org.xmind.ui.wizards.IExporter;

/**
 * @author Jason Wong
 */
public class SVGExportWizard extends DocumentExportWizard {

    private static final String PAGE_NAME = "org.xmind.ui.export.svgExportPage"; //$NON-NLS-1$

    private static final String SECTION_NAME = "org.xmind.ui.export.svg"; //$NON-NLS-1$

    private static final String SVG_EXT = ".svg"; //$NON-NLS-1$

    private class SVGExportPage extends AbstractMindMapExportPage {

        private Button showPlusCheck;

        private Button showMinusCheck;

        public SVGExportPage() {
            super(PAGE_NAME, Messages.SVGPage_Title);
            setDescription(Messages.SVGPage_Description);
        }

        protected void setDialogFilters(FileDialog dialog,
                List<String> filterNames, List<String> filterExtensions) {
            filterNames.add(0, Messages.SVGPage_FilterName);
            filterExtensions.add(0, "*" + SVG_EXT); //$NON-NLS-1$
            super.setDialogFilters(dialog, filterNames, filterExtensions);
        }

        public void createControl(Composite parent) {
            Composite composite = new Composite(parent, SWT.NONE);
            GridLayout layout = new GridLayout();
            layout.verticalSpacing = 25;
            layout.marginTop = 20;
            composite.setLayout(layout);
            setControl(composite);

            Control setupGroup = createSetupControls(composite);
            setupGroup.setLayoutData(
                    new GridData(SWT.FILL, SWT.FILL, true, true));

            Control fileGroup = createFileControls(composite);
            GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
            gridData.widthHint = 210;
            fileGroup.setLayoutData(gridData);
        }

        private Control createSetupControls(Composite parent) {
            Composite composite = new Composite(parent, SWT.NONE);
            GridLayout gridLayout = new GridLayout(1, false);
            gridLayout.marginWidth = 0;
            gridLayout.marginHeight = 0;
            gridLayout.verticalSpacing = 20;
            gridLayout.horizontalSpacing = 0;
            composite.setLayout(gridLayout);

            createShowPlusMinusControls(composite);
            return composite;
        }

        private void createShowPlusMinusControls(Composite parent) {
            Composite composite = new Composite(parent, SWT.NONE);
            GridLayout gridLayout = new GridLayout(1, false);
            gridLayout.marginWidth = 0;
            gridLayout.marginHeight = 0;
            gridLayout.verticalSpacing = 15;
            gridLayout.horizontalSpacing = 0;
            composite.setLayout(gridLayout);

            Label label = new Label(composite, SWT.NONE);
            label.setLayoutData(
                    new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
            label.setText(Messages.ExportWizard_Collapse_Expand_text);

            Composite rightGroup = new Composite(composite, SWT.NONE);
            GridLayout gridLayout2 = new GridLayout(1, false);
            gridLayout2.marginWidth = 0;
            gridLayout2.marginHeight = 0;
            gridLayout2.verticalSpacing = 15;
            gridLayout2.horizontalSpacing = 0;
            gridLayout2.marginLeft = 15;
            rightGroup.setLayout(gridLayout2);
            GridData gridData = new GridData(SWT.FILL, SWT.FILL, false, false);
            gridData.widthHint = 300;
            gridData.heightHint = SWT.DEFAULT;
            rightGroup.setLayoutData(gridData);

            createShowPlusCheck(rightGroup);
            createShowMinusCheck(rightGroup);

            initPlusMinusCheckState();
        }

        private void createShowPlusCheck(Composite parent) {
            showPlusCheck = createPlusMinusCheck(parent,
                    Messages.SVGExportWizard_showPlusCheck_text,
                    MindMapUI.getImages().get("plus.png", true).createImage()); //$NON-NLS-1$
        }

        private void createShowMinusCheck(Composite parent) {
            showMinusCheck = createPlusMinusCheck(parent,
                    Messages.SVGExportWizard_showMinusCheck_text,
                    MindMapUI.getImages().get("minus.png", true).createImage()); //$NON-NLS-1$
        }

        private Button createPlusMinusCheck(Composite parent, String text,
                Image image) {
            Composite composite = new Composite(parent, SWT.NONE);
            composite.setBackground(parent.getBackground());
            composite.setLayoutData(
                    new GridData(SWT.LEFT, SWT.CENTER, false, false));

            GridLayout gridLayout = new GridLayout(2, false);
            gridLayout.marginWidth = 0;
            gridLayout.marginHeight = 0;
            gridLayout.verticalSpacing = 0;
            gridLayout.horizontalSpacing = 5;
            composite.setLayout(gridLayout);

            Button check = new Button(composite, SWT.CHECK);
            check.setBackground(composite.getBackground());
            check.setLayoutData(
                    new GridData(SWT.CENTER, SWT.CENTER, false, false));
            check.setText(text);

            Label imageLabel = new Label(composite, SWT.NONE);
            imageLabel.setBackground(composite.getBackground());
            imageLabel.setLayoutData(
                    new GridData(SWT.CENTER, SWT.CENTER, false, false));
            imageLabel.setImage(image);

            hookWidget(check, SWT.Selection);

            return check;
        }

        private void initPlusMinusCheckState() {
            boolean plusVisible = getBoolean(getDialogSettings(),
                    ExportContants.PLUS_VISIBLE,
                    ExportContants.DEFAULT_PLUS_VISIBLE);
            boolean minusVisible = getBoolean(getDialogSettings(),
                    ExportContants.MINUS_VISIBLE,
                    ExportContants.DEFAULT_MINUS_VISIBLE);

            showPlusCheck.setSelection(plusVisible);
            showMinusCheck.setSelection(minusVisible);
        }

        private boolean getBoolean(IDialogSettings settings, String key,
                boolean defaultValue) {
            boolean value = defaultValue;
            if (settings.get(key) != null) {
                value = settings.getBoolean(key);
            }

            return value;
        }

        @Override
        protected void handleWidgetEvent(Event event) {
            if (event.widget == showPlusCheck) {
                setProperty(ExportContants.PLUS_VISIBLE,
                        showPlusCheck.getSelection());
            } else if (event.widget == showMinusCheck) {
                setProperty(ExportContants.MINUS_VISIBLE,
                        showMinusCheck.getSelection());
            } else {
                super.handleWidgetEvent(event);
            }
        }

        private void setProperty(String key, boolean value) {
            getDialogSettings().put(key, value);
        }
    }

    private SVGExportPage page;

    public SVGExportWizard() {
        setWindowTitle(Messages.SVGWizard_WindowTitle);
        setDialogSettings(SvgPlugin.getDialogSettings(SECTION_NAME));
        setDefaultPageImageDescriptor(
                MindMapUI.getImages().getWizBan(IMindMapImages.WIZ_EXPORT));
    }

    @Override
    protected IExporter createExporter() {
        IMindMap mindmap = getSourceMindMap();
        SVGExporter exporter = new SVGExporter(mindmap.getSheet(),
                mindmap.getCentralTopic(), getTargetPath(), getSourceViewer(),
                getDialogSettings());
        exporter.setDialogSettings(getDialogSettings());
        exporter.init();
        return exporter;
    }

    @Override
    public void openFile(String path, IProgressMonitor monitor) {
        boolean edgeExcuteFlag = false;
        if (new File(path).exists()) {
            monitor.subTask(Messages.ExportPage_Launching);
            if (isWin10OrHigher()) {
                try {
                    edgeExcuteFlag = true;
                    Runtime.getRuntime()
                            .exec("cmd.exe /c \"start microsoft-edge:" //$NON-NLS-1$
                                    + new File(path).toURI().toString() + "\""); //$NON-NLS-1$
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (!edgeExcuteFlag)
                Program.launch(path);
        }
    }

    public boolean isWin10OrHigher() {
        String osVersion = System.getProperty("os.version"); //$NON-NLS-1$
        String osName = System.getProperty("os.name"); //$NON-NLS-1$
        if (osName.indexOf("Windows") != -1) //$NON-NLS-1$
            if (osVersion.indexOf("10.") != -1 //$NON-NLS-1$
                    || Double.valueOf(osVersion) > 6.2
                            && Double.valueOf(osVersion) < 7.0)
                return true;
        return false;
    }

    @Override
    protected void addValidPages() {
        addPage(page = new SVGExportPage());
    }

    @Override
    protected String getFormatName() {
        return Messages.SVGWizard_FormatName;
    }

    @Override
    protected boolean isExtensionCompatible(String path, String extension) {
        return super.isExtensionCompatible(path, extension)
                && SVG_EXT.equalsIgnoreCase(extension);
    }

    @Override
    protected void handleExportException(Throwable e) {
        super.handleExportException(e);
        page.setErrorMessage(e.getLocalizedMessage());
    }

    protected String getSuggestedFileName() {
        return super.getSuggestedFileName() + SVG_EXT;
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

        SvgPlugin.getDefault().getUsageDataCollector().trackEvent(
                UserDataConstants.CATEGORY_EXPORT,
                UserDataConstants.EXPORT_TO_SVG);
        super.doExport(monitor, display, parentShell);
    }

}
