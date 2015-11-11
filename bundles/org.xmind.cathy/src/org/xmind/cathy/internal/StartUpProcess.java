package org.xmind.cathy.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledItem;
import org.eclipse.e4.ui.workbench.Selector;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
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
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.xmind.ui.internal.editor.MME;
import org.xmind.ui.mindmap.MindMapUI;

public class StartUpProcess {
    private static final boolean DEBUG_CHECK_OPEN_FILE = CathyPlugin
            .getDefault().isDebugging("/debug/checkopenfile"); //$NON-NLS-1$

    private static class DashBoardProcessor {

        private Selector itemMatcher = new HandledItemMatcher(
                ICathyConstants.COMMAND_TOGGLE_DASHBOARD);

        void doOpenDashboard(IWorkbench workbench) {
            final EModelService modelService = workbench
                    .getService(EModelService.class);
            final MApplication application = ((Workbench) workbench)
                    .getApplication();

            if (application == null || modelService == null)
                return;

            workbench.getDisplay().syncExec(new Runnable() {
                public void run() {
                    final List<MWindow> windows = modelService.findElements(
                            application, ICathyConstants.ID_MAIN_WINDOW,
                            MWindow.class, null);
                    if (windows.isEmpty())
                        return;

                    MWindow window = windows.get(0);
                    if (!window.getTags()
                            .contains(ICathyConstants.TAG_SHOW_DASHBOARD)) {
                        window.getTags()
                                .add(ICathyConstants.TAG_SHOW_DASHBOARD);
                    } else {
                        if (showDashboard(window, application, modelService)) {
                            updateDashboardToolItems(window, modelService);
                        } else {
                            window.getTags()
                                    .remove(ICathyConstants.TAG_SHOW_DASHBOARD);
                        }
                    }
                }
            });

        }

        private boolean showDashboard(MWindow window, MApplication application,
                EModelService modelService) {
            MPart dashboardPart = findReferencedDashboardPartIn(window,
                    application, modelService);
            if (dashboardPart == null)
                return false;

            EPartService partService = window.getContext()
                    .get(EPartService.class);
            if (partService == null)
                return false;

            partService.activate(dashboardPart, true);
            return partService.getActivePart() == dashboardPart;
        }

        private MPart findReferencedDashboardPartIn(MWindow window,
                MApplication application, EModelService modelService) {
            MPart dashboardPart = null;

            /*
             * Find Dashboard instance in window model tree.
             */
            List<MPart> dashboardParts = modelService.findElements(window,
                    ICathyConstants.ID_DASHBOARD_PART, MPart.class, null);
            if (!dashboardParts.isEmpty()) {
                dashboardPart = dashboardParts.get(0);
            } else {
                /*
                 * Find Dashboard instance in shared elements.
                 */
                for (MUIElement p : window.getChildren()) {
                    if (p instanceof MPart && ICathyConstants.ID_DASHBOARD_PART
                            .equals(p.getElementId())) {
                        dashboardPart = (MPart) p;
                        break;
                    }
                }
            }

            if (dashboardPart == null) {
                /*
                 * Create Dashboard part from snippet.
                 */
                MUIElement part = modelService.cloneSnippet(application,
                        ICathyConstants.ID_DASHBOARD_PART, window);
                if (part != null && part instanceof MPart
                        && ICathyConstants.ID_DASHBOARD_PART
                                .equals(part.getElementId())) {
                    dashboardPart = (MPart) part;
                    window.getChildren().add(dashboardPart);
                }
            }

            return dashboardPart;
        }

        private void updateDashboardToolItems(MWindow window,
                EModelService modelService) {
            String tooltip;
            boolean selected;
            if (window.getTags().contains(ICathyConstants.TAG_SHOW_DASHBOARD)) {
                tooltip = WorkbenchMessages.DashboardHideHome_tooltip;
                selected = true;
            } else {
                tooltip = WorkbenchMessages.DashboardShowHome_tooltip;
                selected = false;
            }

            List<MHandledItem> items = modelService.findElements(window,
                    MHandledItem.class, EModelService.ANYWHERE, itemMatcher);
            for (MHandledItem item : items) {
                item.setTooltip(tooltip);
                item.setSelected(selected);
            }
        }
    }

    private IWorkbench workbench;

    public StartUpProcess(IWorkbench workbench) {
        this.workbench = workbench;
    }

    public void startUp() {
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

    private void checkAndOpenFiles() {
        new CheckOpenFilesProcess(workbench).doCheckAndOpenFiles();
    }

    private void checkAndRecoverFiles() {
        new CheckRecoverFilesProcess(workbench).checkAndRecoverFiles();
    }

    private void openStartupMap() {
        if (!hasOpenedEditors()) {
            int action = CathyPlugin.getDefault().getPreferenceStore()
                    .getInt(CathyPlugin.STARTUP_ACTION);
            if (action == CathyPlugin.STARTUP_ACTION_LAST) {
                doOpenLastSession();
            }
            if (!hasOpenedEditors()) {
                doOpenDashboard();
            }
        }
    }

    private void doOpenDashboard() {
        new DashBoardProcessor().doOpenDashboard(workbench);
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
            String path = childEditor.getString(IWorkbenchConstants.TAG_PATH);
            if (path != null) {
                IEditorInput editorInput = MME.createFileEditorInput(path);
                IEditorPart editorPart = page.openEditor(editorInput,
                        MindMapUI.MINDMAP_EDITOR_ID);
                if ("true".equals(childEditor //$NON-NLS-1$
                        .getString(IWorkbenchConstants.TAG_ACTIVE_PART))) {
                    activeEditorPart = editorPart;
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
