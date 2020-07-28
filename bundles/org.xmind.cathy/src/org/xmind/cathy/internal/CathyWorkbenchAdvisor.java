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
package org.xmind.cathy.internal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistable;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.UIPlugin;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.xmind.cathy.internal.jobs.OpenFilesJob;
import org.xmind.core.internal.InternalCore;
import org.xmind.core.licensing.ILicenseAgent;
import org.xmind.core.licensing.ILicenseChangedListener;
import org.xmind.core.licensing.ILicenseKeyHeader;
import org.xmind.gef.ui.editor.IEditingContext;
import org.xmind.ui.internal.PasswordProvider;
import org.xmind.ui.internal.dialogs.DialogMessages;
import org.xmind.ui.internal.editor.AbstractWorkbookRef;
import org.xmind.ui.internal.editor.DefaultMindMapPreviewGenerator;
import org.xmind.ui.internal.editor.IMindMapPreviewGenerator;
import org.xmind.ui.internal.editor.IPasswordProvider;
import org.xmind.ui.mindmap.MindMapUI;

public class CathyWorkbenchAdvisor extends WorkbenchAdvisor
        implements ILicenseChangedListener {

    public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(
            IWorkbenchWindowConfigurer configurer) {
        return new CathyWorkbenchWindowAdvisor(configurer);
    }

    public String getInitialWindowPerspectiveId() {
        return MindMapUI.PERSPECTIVE_ID;
    }

    @Override
    public String getMainPreferencePageId() {
        return "org.xmind.ui.prefPage.General"; //$NON-NLS-1$
    }

    public void initialize(IWorkbenchConfigurer configurer) {
        super.initialize(configurer);
        configurer.setSaveAndRestore(true);
        configurer.setExitOnLastWindowClose(true);
    }

    @Override
    public void preStartup() {
        super.preStartup();

        CathyPlugin.getDefault().getLicenseAgent()
                .addLicenseChangedListener(this);
        licenseChanged(CathyPlugin.getDefault().getLicenseAgent());

        /**
         * This hack requires workbench to exist. See
         * {@link org.eclipse.ui.internal.PlatformUIPreferenceListener}.
         */
        UIPlugin.getDefault().getPreferenceStore().setValue(
                IWorkbenchPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS,
                true);
    }

    @Override
    public void postStartup() {
        super.postStartup();

        IWorkbench workbench = getWorkbenchConfigurer().getWorkbench();

        AbstractWorkbookRef.setDefaultEditingContext(
                createDefaultEditingContext(workbench));

        new StartUpProcess(workbench).startUp();
    }

    private IEditingContext createDefaultEditingContext(
            final IWorkbench workbench) {
        final IMindMapPreviewGenerator previewGenerator = new DefaultMindMapPreviewGenerator(
                workbench.getDisplay());

        final IPasswordProvider passwordProvider = new PasswordProvider();

        return new IEditingContext() {
            public <T> T getAdapter(Class<T> adapter) {

                if (IMindMapPreviewGenerator.class.equals(adapter))
                    return adapter.cast(previewGenerator);

                if (IPasswordProvider.class.equals(adapter))
                    return adapter.cast(passwordProvider);

                T result;

                result = workbench.getService(adapter);
                if (result != null)
                    return result;

                result = workbench.getAdapter(adapter);
                if (result != null)
                    return result;

                return result;
            }
        };
    }

    @Override
    public void postShutdown() {
        CathyPlugin.getDefault().getLicenseAgent()
                .removeLicenseChangedListener(this);

        AbstractWorkbookRef.setDefaultEditingContext(null);
        super.postShutdown();
    }

    public boolean preShutdown() {
        boolean readyToShutdown = super.preShutdown();
        if (readyToShutdown) {
            readyToShutdown = saveAllEditorsOnClose();
        }
        return readyToShutdown;
    }

    private boolean saveAllEditorsOnClose() {
        IWorkbench workbench = getWorkbenchConfigurer().getWorkbench();
        final ArrayList<IEditorReference> unClosedEditorRefs = new ArrayList<IEditorReference>();
        IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
        for (IWorkbenchWindow window : windows) {
            IWorkbenchPage page = window.getActivePage();
            for (IEditorReference editorRef : page.getEditorReferences()) {
                unClosedEditorRefs.add(editorRef);
                final IEditorPart editor = editorRef.getEditor(false);
                if (editor != null && editor.isDirty()) {
                    int answer = promptToSaveOnClose(window, page, editor);
                    if (answer == ISaveablePart2.CANCEL)
                        return false;
                    if (answer == ISaveablePart2.YES) {
                        if (!doSaveEditor(window, editor)) {
                            return false;
                        }
                    }
                }
            }
        }
        SafeRunner.run(new SafeRunnable() {
            public void run() {
                XMLMemento mem = recordEditorsState(unClosedEditorRefs);
                saveMementoToFile(mem);
            }
        });
        return closeAllEditors();
    }

    private XMLMemento recordEditorsState(
            ArrayList<IEditorReference> editorRefs) {
        XMLMemento memento = XMLMemento.createWriteRoot("xmind"); //$NON-NLS-1$
        saveEditorsState(memento, editorRefs);
        return memento;
    }

    private void saveEditorsState(IMemento memento,
            ArrayList<IEditorReference> editorRefs) {
        IEditorPart activeEditor = null;

        IWorkbenchWindow window = PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow();
        if (window != null && window.getActivePage() != null) {
            activeEditor = window.getActivePage().getActiveEditor();
        }

        IMemento childrenMemento = memento
                .createChild(IWorkbenchConstants.TAG_EDITORS);
        if (!editorRefs.isEmpty())
            for (IEditorReference ref : editorRefs) {
                IEditorPart editor = ref.getEditor(false);
                if (editor == null) {
                    continue;
                }
                IMemento editorMemento = childrenMemento
                        .createChild(IWorkbenchConstants.TAG_EDITOR);
                editorMemento.putBoolean(IWorkbenchConstants.TAG_ACTIVE_PART,
                        editor == activeEditor);
                IPersistable editorPersistable = CathyPlugin.getAdapter(editor,
                        IPersistable.class);
                if (editorPersistable != null) {
                    editorPersistable.saveState(editorMemento);
                }

                IEditorInput input = editor.getEditorInput();
                IMemento inputMemento = editorMemento
                        .createChild(IWorkbenchConstants.TAG_INPUT);
                IPersistableElement inputPersistable = CathyPlugin
                        .getAdapter(input, IPersistableElement.class);
                if (inputPersistable != null) {
                    inputMemento.putString(IWorkbenchConstants.TAG_FACTORY_ID,
                            inputPersistable.getFactoryId());
                    inputPersistable.saveState(inputMemento);
                }
            }
    }

    private boolean saveMementoToFile(XMLMemento memento) {
        // Save it to a file.
        File stateFile = getEditorsStateFile();
        if (stateFile == null) {
            return false;
        }
        try {
            FileOutputStream stream = new FileOutputStream(stateFile);
            OutputStreamWriter writer = new OutputStreamWriter(stream, "utf-8"); //$NON-NLS-1$
            memento.save(writer);
            writer.close();
        } catch (IOException e) {
            stateFile.delete();
            return false;
        }

        // Success !
        return true;
    }

    private File getEditorsStateFile() {
        IPath path = WorkbenchPlugin.getDefault().getDataLocation();
        if (path == null) {
            return null;
        }
        path = path.append("XMind_Editors.xml"); //$NON-NLS-1$
        return path.toFile();
    }

    private int promptToSaveOnClose(IWorkbenchWindow window,
            IWorkbenchPage page, IEditorPart editor) {
        if (editor instanceof ISaveablePart2) {
            int answer = ((ISaveablePart2) editor).promptToSaveOnClose();
            if (answer != ISaveablePart2.DEFAULT)
                return answer;
        }
        page.activate(editor);
        MessageDialog dialog = new MessageDialog(window.getShell(),
                DialogMessages.Save_title, null,
                NLS.bind(WorkbenchMessages.PromptSaveEditorOnClosing_message,
                        editor.getTitle()),
                MessageDialog.QUESTION,
                new String[] { IDialogConstants.YES_LABEL,
                        IDialogConstants.NO_LABEL,
                        IDialogConstants.CANCEL_LABEL },
                0);
        int answerIndex = dialog.open();
        switch (answerIndex) {
        case 0:
            return ISaveablePart2.YES;
        case 1:
            return ISaveablePart2.NO;
        default:
            return ISaveablePart2.CANCEL;
        }
    }

    private boolean doSaveEditor(final IWorkbenchWindow window,
            final IEditorPart editor) {
        final boolean[] saved = new boolean[1];
        saved[0] = false;
        window.getShell().getDisplay().syncExec(new Runnable() {
            public void run() {
                SafeRunner.run(new SafeRunnable() {
                    public void run() throws Exception {
                        final IProgressMonitor monitor = new NullProgressMonitor();
                        if (InternalCore.DEBUG_WORKBOOK_SAVE)
                            CathyPlugin.log(
                                    "CathyWorkbenchAdvisor: About to save workbook on workbench close: " //$NON-NLS-1$
                                            + editor.getEditorInput()
                                                    .toString());
                        editor.doSave(monitor);
                        if (!monitor.isCanceled()) {
                            saved[0] = true;
                        } else {
                            if (InternalCore.DEBUG_WORKBOOK_SAVE)
                                CathyPlugin.log(
                                        "CathyWorkbenchAdvisor: Finished saving workbook on workbench close: " //$NON-NLS-1$
                                                + editor.getEditorInput()
                                                        .toString());
                        }
                    }
                });
            }
        });
        return saved[0];
    }

    private boolean closeAllEditors() {
        boolean closed = false;
        IWorkbench workbench = getWorkbenchConfigurer().getWorkbench();
        for (IWorkbenchWindow window : workbench.getWorkbenchWindows()) {
            closed |= window.getActivePage().closeAllEditors(false);
        }
        return closed;
    }

    public void licenseChanged(ILicenseAgent agent) {
        int type = agent.getLicenseType();
        ILicenseKeyHeader header = agent.getLicenseKeyHeader();
        String brandingVersion = System
                .getProperty("org.xmind.product.brandingVersion", ""); //$NON-NLS-1$ //$NON-NLS-2$
        String licenseType;
        if ((type & ILicenseAgent.PRO_LICENSE_KEY) != 0) {
            licenseType = NLS.bind(WorkbenchMessages.About_ProTitle,
                    brandingVersion);
        } else if ((type & ILicenseAgent.PLUS_LICENSE_KEY) != 0) {
            licenseType = NLS.bind(WorkbenchMessages.About_PlusTitle,
                    brandingVersion);
        } else if ((type & ILicenseAgent.PRO_SUBSCRIPTION) != 0) {
            licenseType = WorkbenchMessages.About_ProSubscriptionTitle;
        } else {
            licenseType = null;
        }

        if (header != null && ((type & ILicenseAgent.PLUS_LICENSE_KEY) != 0
                || (type & ILicenseAgent.PRO_LICENSE_KEY) != 0)) {
            String licenseeType = header.getLicenseeType();
            if (ILicenseKeyHeader.LICENSEE_FAMILY.equals(licenseeType)) {
                licenseType = NLS.bind("{0} (Family License)", licenseType); //$NON-NLS-1$
            } else if (ILicenseKeyHeader.LICENSEE_EDU.equals(licenseeType)) {
                licenseType = NLS.bind("{0} (Academia License)", licenseType); //$NON-NLS-1$
            } else if (ILicenseKeyHeader.LICENSEE_GOV.equals(licenseeType)) {
                licenseType = NLS.bind("{0} (Gov/NPO License)", licenseType); //$NON-NLS-1$
            } else if (ILicenseKeyHeader.LICENSEE_TEAM_5U.equals(licenseeType)
                    || ILicenseKeyHeader.LICENSEE_TEAM_10U.equals(licenseeType)
                    || ILicenseKeyHeader.LICENSEE_TEAM_20U
                            .equals(licenseeType)) {
                licenseType = NLS.bind("{0} (Team License)", licenseType); //$NON-NLS-1$
            } else if (ILicenseKeyHeader.LICENSEE_VLE.equals(licenseeType)) {
                licenseType = NLS.bind("{0} (Volume License)", licenseType); //$NON-NLS-1$
            }
        }
        if (licenseType == null) {
            licenseType = WorkbenchMessages.About_LicenseType_Unactivated;
        } else {
            licenseType = NLS.bind(WorkbenchMessages.About_LicenseTypePattern,
                    licenseType);
        }
        System.setProperty("org.xmind.product.license.type", //$NON-NLS-1$
                licenseType);

        String name = agent.getLicenseeName();
        if (name != null && !"".equals(name)) { //$NON-NLS-1$
            name = NLS.bind(WorkbenchMessages.About_LicensedTo, name);
        } else {
            name = ""; //$NON-NLS-1$
        }
        System.setProperty("org.xmind.product.license.licensee", name); //$NON-NLS-1$
    }

    @Override
    public void eventLoopIdle(Display display) {
        String[] paths = OpenDocumentQueue.getInstance().drain();
        if (paths.length > 0) {
            CathyPlugin.log("Ready to open files: " + Arrays.toString(paths)); //$NON-NLS-1$
            openFiles(paths);
            IWorkbenchWindow window = getWorkbenchConfigurer().getWorkbench()
                    .getActiveWorkbenchWindow();
            if (window != null) {
                Shell shell = window.getShell();
                if (shell != null && !shell.isDisposed()) {
                    shell.forceActive();
                }
            }
        } else {
            super.eventLoopIdle(display);
        }
    }

    private void openFiles(String[] paths) {
        OpenFilesJob job = new OpenFilesJob(
                getWorkbenchConfigurer().getWorkbench(),
                WorkbenchMessages.CheckOpenFilesJob_CheckFiles_name,
                Arrays.asList(paths));
        job.setRule(Log.get(Log.OPENING));
        job.schedule();
    }

}
