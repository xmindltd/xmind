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

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.internal.UserDataConstants;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.mindmap.IMindMap;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.Logger;
import org.xmind.ui.wizards.AbstractMindMapExportPage;
import org.xmind.ui.wizards.AbstractMindMapExportWizard;
import org.xmind.ui.wizards.IExporter;

public class TextExportWizard extends AbstractMindMapExportWizard {

    private static final String TEXT_EXPORT_PAGE_NAME = "textExportPage"; //$NON-NLS-1$

    private static final String DIALOG_SETTINGS_SECTION_ID = "org.xmind.ui.export.text"; //$NON-NLS-1$

    private static final String EXT_TEXT_FILE = ".txt"; //$NON-NLS-1$

    private static final String FILTER_TEXT = "*.txt"; //$NON-NLS-1$

    private static class UnicodePrintStream extends PrintStream {
        public UnicodePrintStream(OutputStream out)
                throws UnsupportedEncodingException {
            super(out, false, "utf-8"); //$NON-NLS-1$
        }
    }

    private class TextExportPage extends AbstractMindMapExportPage {

        private class GeneratePreviewJob extends Job {

            private Display display;

            public GeneratePreviewJob(Display display) {
                super(WizardMessages.TextExportPage_GeneratePreview_jobName);
                this.display = display;
            }

            protected IStatus run(IProgressMonitor monitor) {
                monitor.beginTask(null, 100);

                monitor.subTask(
                        WizardMessages.TextExportPage_GeneratePreview_CollectingMapInfo);
                IExporter exporter = createExporter();
                if (exporter == null) {
                    return new Status(IStatus.CANCEL, MindMapUIPlugin.PLUGIN_ID,
                            WizardMessages.TextExportPage_GeneratePreview_NoContent);
                }
                monitor.worked(10);

                ByteArrayOutputStream byteOut = new ByteArrayOutputStream(50);
                OutputStream out = wrapMonitor(byteOut, monitor);
                PrintStream ps = createPrintStream(out);
                ((TextExporter) exporter).setPrintStream(ps);
                if (!exporter.canStart()) {
                    return new Status(IStatus.CANCEL, MindMapUIPlugin.PLUGIN_ID,
                            WizardMessages.TextExportPage_GeneratePreview_NoContent);
                }

                int total = exporter.getTotalWork();
                int worked = 0;
                int uiTotal = 90;
                int uiWorked = 0;
                long lastRefresh = System.currentTimeMillis();

                try {
                    exporter.start(display, null);
                } catch (InvocationTargetException e1) {
                    return new Status(IStatus.CANCEL, MindMapUIPlugin.PLUGIN_ID,
                            WizardMessages.TextExportPage_GeneratePreview_Canceled);
                }

                try {
                    while (exporter.hasNext()) {
                        if (monitor.isCanceled()) {
                            return new Status(IStatus.CANCEL,
                                    MindMapUIPlugin.PLUGIN_ID,
                                    WizardMessages.TextExportPage_GeneratePreview_Canceled);
                        }

                        monitor.subTask(cleanFileName(exporter.getNextName()));
                        exporter.writeNext(monitor);

                        worked++;
                        int newUIWorked = worked * uiTotal / total;
                        if (newUIWorked > uiWorked) {
                            monitor.worked(newUIWorked - uiWorked);
                            uiWorked = newUIWorked;
                        }

                        if (System.currentTimeMillis() - lastRefresh > 100) {
                            refreshControl(byteOut.toString());
                            lastRefresh = System.currentTimeMillis();
                        }
                    }
                } catch (Throwable e) {
                    return new Status(IStatus.CANCEL, MindMapUIPlugin.PLUGIN_ID,
                            WizardMessages.TextExportPage_GeneratePreview_Canceled);
                } finally {
                    ps.close();
                    try {
                        out.close();
                    } catch (IOException e) {
                    }
                }

                try {
                    exporter.end();
                } catch (InvocationTargetException e) {
                    return new Status(IStatus.CANCEL, MindMapUIPlugin.PLUGIN_ID,
                            WizardMessages.TextExportPage_GeneratePreview_Canceled);
                }

                if (ps instanceof UnicodePrintStream) {
                    try {
                        refreshControl(byteOut.toString("utf-8")); //$NON-NLS-1$
                    } catch (UnsupportedEncodingException e) {
                        refreshControl(byteOut.toString());
                    }
                } else {
                    refreshControl(byteOut.toString());
                }
                monitor.done();
                return new Status(IStatus.OK, MindMapUIPlugin.PLUGIN_ID,
                        WizardMessages.TextExportPage_GeneratePreview_Completed);
            }

            private void refreshControl(final String text) {
                display.asyncExec(new Runnable() {
                    public void run() {
                        if (previewControl != null
                                && !previewControl.isDisposed()) {
                            previewControl.setText(text);
                        }
                    }
                });
            }
        }

        private Text previewControl;

        private Job generatePreviewJob = null;

        public TextExportPage() {
            super(TEXT_EXPORT_PAGE_NAME, WizardMessages.TextExportPage_title);
            setDescription(WizardMessages.TextExportPage_description);
        }

        public void createControl(Composite parent) {
            Composite composite = new Composite(parent, SWT.NONE);
            GridLayout layout = new GridLayout();
            layout.verticalSpacing = 15;
            composite.setLayout(layout);
            setControl(composite);

            createPreviewGroup(composite);

            Control fileGroup = createFileControls(composite);
            fileGroup.setLayoutData(
                    new GridData(GridData.FILL, GridData.FILL, true, false));
        }

        private void createPreviewGroup(Composite parent) {
            Group group = new Group(parent, SWT.NONE);
            group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
            group.setLayout(new GridLayout(1, false));
            group.setText(WizardMessages.TextExportPage_PreviewGroup_title);

            previewControl = new Text(group, SWT.MULTI | SWT.BORDER
                    | SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL);
            previewControl.setEditable(false);
            previewControl.setLayoutData(
                    new GridData(SWT.FILL, SWT.FILL, true, true));

            generatePreview();
        }

        private void generatePreview() {
            cancel();
            previewControl.setText(""); //$NON-NLS-1$

            generatePreviewJob = new GeneratePreviewJob(Display.getCurrent());
            generatePreviewJob.schedule();
        }

        public void cancel() {
            if (generatePreviewJob != null) {
                generatePreviewJob.cancel();
                generatePreviewJob = null;
            }
        }

        protected FileDialog createBrowseDialog() {
            FileDialog dialog = super.createBrowseDialog();
            dialog.setFilterNames(new String[] {
                    WizardMessages.TextExportPage_FileDialog_TextFile,
                    WizardMessages.ExportPage_FileDialog_AllFiles });
            dialog.setFilterExtensions(
                    new String[] { FILTER_TEXT, FILTER_ALL_FILES });
            return dialog;
        }

        public void dispose() {
            cancel();
            super.dispose();
            previewControl = null;
        }

    }

    private TextExportPage page;

    public TextExportWizard() {
        setWindowTitle(WizardMessages.TextExportWizard_windowTitle);
        setDefaultPageImageDescriptor(
                MindMapUI.getImages().getWizBan(IMindMapImages.WIZ_EXPORT));
        setDialogSettings(MindMapUIPlugin.getDefault()
                .getDialogSettings(DIALOG_SETTINGS_SECTION_ID));
    }

    protected void addValidPages() {
        addPage(page = new TextExportPage());
    }

    protected void doExport(IProgressMonitor monitor, Display display,
            Shell parentShell)
            throws InvocationTargetException, InterruptedException {
        monitor.beginTask(null, 100);

        monitor.subTask(WizardMessages.Export_Initializing);
        IExporter exporter = createExporter();
        if (exporter == null) {
            page.setErrorMessage(
                    WizardMessages.TextExportPage_NoContentToExport_message);
            throw new InterruptedException();
        }
        monitor.worked(10);

        int total = exporter.getTotalWork();
        int worked = 0;
        int uiTotal = 88;
        int uiWorked = 0;

        OutputStream fileOut;
        try {
            fileOut = new FileOutputStream(getTargetPath());
        } catch (IOException e) {
            throw new InvocationTargetException(e);
        }
        try {
            OutputStream out = wrapMonitor(fileOut, monitor);
            try {
                PrintStream ps = createPrintStream(out);
                try {
                    ((TextExporter) exporter).setPrintStream(ps);
                    if (!exporter.canStart()) {
                        page.setErrorMessage(
                                WizardMessages.TextExportPage_NoContentToExport_message);
                        throw new InterruptedException();
                    }

                    exporter.start(display, parentShell);

                    while (exporter.hasNext()) {
                        monitor.subTask(cleanFileName(exporter.getNextName()));
                        exporter.writeNext(monitor);

                        worked++;
                        int newUIWorked = worked * uiTotal / total;
                        if (newUIWorked > uiWorked) {
                            monitor.worked(newUIWorked - uiWorked);
                            uiWorked = newUIWorked;
                        }
                    }
                } finally {
                    ps.close();
                }
            } finally {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
        } finally {
            try {
                fileOut.close();
            } catch (IOException e) {
            }
        }

        if (uiWorked < uiTotal) {
            monitor.worked(uiTotal - uiWorked);
        }

        monitor.subTask(WizardMessages.Export_Finishing);
        exporter.end();
        monitor.worked(1);

        launchTargetFile(true, monitor, display, parentShell);
        monitor.done();
    }

    private static PrintStream createPrintStream(OutputStream out) {
        try {
            return new UnicodePrintStream(out);
        } catch (UnsupportedEncodingException e) {
            Logger.log(e, "Unable to export text with utf-8 encoding."); //$NON-NLS-1$
        }
        return new PrintStream(out);
    }

    protected IExporter createExporter() {
        IMindMap map = getSourceMindMap();
        ISheet sheet = map.getSheet();
        ITopic centralTopic = map.getCentralTopic();
        TextExporter exporter = new TextExporter(sheet, centralTopic);
        exporter.setDialogSettings(getDialogSettings());
        exporter.init();
        return exporter;
    }

    protected String getFormatName() {
        return WizardMessages.TextExportWizard_formatName;
    }

    protected String getSuggestedFileName() {
        return super.getSuggestedFileName() + EXT_TEXT_FILE;
    }

    @Override
    protected boolean doExport() {
        MindMapUIPlugin.getDefault().getUsageDataCollector().trackEvent(
                UserDataConstants.CATEGORY_EXPORT,
                UserDataConstants.EXPORT_TO_TEXT);
        return super.doExport();
    }

}
