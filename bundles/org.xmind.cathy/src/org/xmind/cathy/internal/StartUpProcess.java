package org.xmind.cathy.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.xmind.cathy.internal.dashboard.DashboardAutomationAddon;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.mindmap.IWorkbookRef;
import org.xmind.ui.mindmap.MindMapUI;

public class StartUpProcess {

    private static final boolean DEBUG_CHECK_OPEN_FILE = CathyPlugin
            .getDefault().isDebugging("/debug/checkopenfile"); //$NON-NLS-1$

    private IWorkbench workbench;

    public StartUpProcess(IWorkbench workbench) {
        this.workbench = workbench;
    }

    public void startUp() {
        hideRightStack();
        checkAndRecoverFiles();

        if (DEBUG_CHECK_OPEN_FILE) {
            checkAndOpenFiles();
        } else {
            //delete file paths which need to open from command line
            Log openFile = Log.get(Log.OPENING);
            if (openFile.exists())
                openFile.delete();
        }
        openStartupMap();

        Display display = workbench.getDisplay();
        if (display != null && !display.isDisposed()) {
            display.asyncExec(new Runnable() {
                public void run() {
                    System.setProperty("org.xmind.cathy.app.status", //$NON-NLS-1$
                            "workbenchReady"); //$NON-NLS-1$
                }
            });
        }
    }

    private void hideRightStack() {
        MApplication application = workbench.getService(MApplication.class);
        for (MWindow window : application.getChildren()) {
            DashboardAutomationAddon.hideVisiblePart(window,
                    "org.xmind.ui.stack.right"); //$NON-NLS-1$
        }
    }

    private void checkAndOpenFiles() {
        new CheckOpenFilesProcess(workbench).doCheckAndOpenFiles();
    }

    private void checkAndRecoverFiles() {
        SafeRunner.run(new SafeRunnable() {
            public void run() throws Exception {
                new EditorStatePersistance(workbench,
                        CathyPlugin.getDefault().getStateLocation(),
                        CathyPlugin.getDefault().getLogger(),
                        CathyPlugin.getDefault().getDebugValue(
                                CathyPlugin.OPTION_AUTO_SAVE_EDITOR_STATE_INTERVALS,
                                CathyPlugin.AUTO_SAVE_EDITOR_STATE_INTERVALS))
                                        .startUp();
            }
        });
    }

    private void openStartupMap() {
        if (!hasOpenedEditors()) {
            int action = CathyPlugin.getDefault().getPreferenceStore()
                    .getInt(CathyPlugin.STARTUP_ACTION);
            if (action == CathyPlugin.STARTUP_ACTION_LAST) {
                doOpenLastSession();
            }
            if (!hasOpenedEditors()) {
                closeOpenedDashboard();
            }
        }
    }

    private void doOpenDashboard() {
        final EModelService modelService = workbench
                .getService(EModelService.class);
        final MApplication application = workbench
                .getService(MApplication.class);
        if (modelService == null || application == null)
            return;

        final DashboardAutomationAddon automator = new DashboardAutomationAddon();
        automator.setModelService(modelService);
        automator.setApplication(application);
        workbench.getDisplay().asyncExec(new Runnable() {
            public void run() {
                for (MWindow window : application.getChildren()) {
                    automator.showDashboard(window);
                }
            }
        });
    }

    private void closeOpenedDashboard() {
        final EModelService modelService = workbench
                .getService(EModelService.class);
        final MApplication application = workbench
                .getService(MApplication.class);
        if (modelService == null || application == null)
            return;

        workbench.getDisplay().asyncExec(new Runnable() {
            public void run() {
                for (MWindow window : application.getChildren()) {
                    if (window.getTags()
                            .contains(ICathyConstants.TAG_SHOW_DASHBOARD)) {
                        window.getTags()
                                .remove(ICathyConstants.TAG_SHOW_DASHBOARD);
                    }
                }
            }
        });
    }

    private void doOpenLastSession() {
        IPath editorStatusPath = WorkbenchPlugin.getDefault().getDataLocation()
                .append("XMind_Editors.xml"); //$NON-NLS-1$
        //open unclosed editors in the last session.
        final File stateFile = editorStatusPath.toFile();
        if (stateFile.exists())
            workbench.getDisplay().syncExec(new Runnable() {
                public void run() {
                    SafeRunner.run(new SafeRunnable() {
                        public void run() throws Exception {
                            IWorkbenchWindow window = workbench
                                    .getActiveWorkbenchWindow();
                            if (window != null) {
                                IWorkbenchPage page = window.getActivePage();
                                if (page != null) {
                                    openUnclosedMapLastSession(stateFile, page);
                                }
                            }
                        }
                    });
                }
            });
    }

    private void openUnclosedMapLastSession(File statusFile,
            final IWorkbenchPage page)
            throws FileNotFoundException, UnsupportedEncodingException,
            WorkbenchException, CoreException, PartInitException {
        FileInputStream input = new FileInputStream(statusFile);
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(input, "utf-8")); //$NON-NLS-1$
        IMemento memento = XMLMemento.createReadRoot(reader);
        IMemento childMem = memento.getChild(IWorkbenchConstants.TAG_EDITORS);
        IMemento[] childrenEditor = childMem
                .getChildren(IWorkbenchConstants.TAG_EDITOR);
        IEditorPart activeEditorPart = null;
        for (IMemento childEditor : childrenEditor) {
            IMemento inputMemeto = childEditor.getChild("input"); //$NON-NLS-1$
            if (inputMemeto == null)
                continue;

            String uri = inputMemeto.getString("uri"); //$NON-NLS-1$
            if (uri != null) {
                IWorkbookRef workbookRef = null;
                try {
                    workbookRef = MindMapUIPlugin.getDefault()
                            .getWorkbookRefFactory()
                            .createWorkbookRef(new URI(uri), null);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                if (workbookRef != null) {
                    IEditorInput editorInput = MindMapUI.getEditorInputFactory()
                            .createEditorInput(workbookRef);
                    IEditorPart editorPart = page.openEditor(editorInput,
                            MindMapUI.MINDMAP_EDITOR_ID);
                    if ("true".equals(childEditor //$NON-NLS-1$
                            .getString(IWorkbenchConstants.TAG_ACTIVE_PART))) {
                        activeEditorPart = editorPart;
                    }
                }
            }
        }
        if (activeEditorPart != null) {
            page.activate(activeEditorPart);
        }
    }

    private boolean hasOpenedEditors() {
        final boolean[] ret = new boolean[1];
        ret[0] = false;
        workbench.getDisplay().syncExec(new Runnable() {
            public void run() {
                for (IWorkbenchWindow window : workbench
                        .getWorkbenchWindows()) {
                    IWorkbenchPage page = window.getActivePage();
                    if (page != null) {
                        if (page.getEditorReferences().length > 0) {
                            ret[0] = true;
                            return;
                        }
                    }
                }
            }
        });
        return ret[0];
    }

}
