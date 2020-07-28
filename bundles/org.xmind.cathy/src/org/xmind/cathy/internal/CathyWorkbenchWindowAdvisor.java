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
import java.util.List;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.IPartListener;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.tweaklets.TitlePathUpdater;
import org.eclipse.ui.internal.tweaklets.Tweaklets;
import org.xmind.core.licensing.ILicenseAgent;
import org.xmind.core.licensing.ILicenseChangedListener;
import org.xmind.core.util.FileUtils;
import org.xmind.ui.internal.editor.MME;
import org.xmind.ui.internal.workbench.Util;

public class CathyWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor
        implements IPartListener2, IPropertyListener, ILicenseChangedListener {

    private String licenseName = null;

    private IWorkbenchPartReference activePartRef = null;

//    private boolean checkingNewWorkbookEditor = false;

    private TitlePathUpdater titlePathUpdater;

    private boolean homeShowing = false;

    public CathyWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
        super(configurer);
        this.titlePathUpdater = (TitlePathUpdater) Tweaklets
                .get(TitlePathUpdater.KEY);
    }

    public ActionBarAdvisor createActionBarAdvisor(
            IActionBarConfigurer configurer) {
        return new CathyWorkbenchActionBuilder(configurer);
    }

    public void preWindowOpen() {
        IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
        configurer.setInitialSize(Util.getInitialWindowSize());
        configurer.setShowCoolBar(true);
        configurer.setShowStatusLine(true);
        configurer.setShowProgressIndicator(true);
        configurer.setTitle(WorkbenchMessages.AppWindowTitle);

        CathyPlugin.getDefault().getLicenseAgent()
                .addLicenseChangedListener(this);
    }

    public void postWindowOpen() {
        final IWorkbenchWindow window = getWindowConfigurer().getWindow();
        if (window != null) {
            window.getPartService().addPartListener(this);

            Shell shell = window.getShell();
            if (shell != null && !shell.isDisposed()) {
                shell.addShellListener(new ShellAdapter() {
                    @Override
                    public void shellActivated(ShellEvent e) {
                        Display display = Display.getCurrent();
                        if (display != null && !display.isDisposed()) {
                            display.asyncExec(new Runnable() {
                                public void run() {
                                    SafeRunner.run(new SafeRunnable() {
                                        public void run() throws Exception {
                                            new CheckOpenFilesProcess(
                                                    window.getWorkbench())
                                                            .doCheckAndOpenFiles();
                                        }
                                    });
                                }
                            });
                        }
                    }
                });
            }
        }

        addE4PartListener();
    }

    private void addE4PartListener() {
        IWorkbench workbench = getWindowConfigurer().getWorkbenchConfigurer()
                .getWorkbench();
        EModelService modelService = workbench.getService(EModelService.class);
        MApplication application = ((Workbench) workbench).getApplication();

        if (modelService == null || application == null) {
            return;
        }

        final List<MWindow> windows = modelService.findElements(application,
                ICathyConstants.ID_MAIN_WINDOW, MWindow.class, null);
        if (windows.isEmpty()) {
            return;
        }
        for (MWindow window : windows) {
            Shell shell = (Shell) window.getContext().get("localActiveShell"); //$NON-NLS-1$
            if (shell != null)
                shell.setMinimumSize(1000, 700);
        }

        EPartService partService = windows.get(0).getContext()
                .get(EPartService.class);
        if (partService == null) {
            return;
        }

        partService.addPartListener(new IPartListener() {

            public void partVisible(MPart part) {
            }

            public void partHidden(MPart part) {
            }

            public void partDeactivated(MPart part) {
                if (ICathyConstants.ID_DASHBOARD_PART
                        .equals(part.getElementId())) {
                    homeShowing = false;
                    updateWindowTitle();
                }
            }

            public void partBroughtToTop(MPart part) {
            }

            public void partActivated(MPart part) {
                if (ICathyConstants.ID_DASHBOARD_PART
                        .equals(part.getElementId())) {
                    homeShowing = true;
                    updateWindowTitle();
                }
            }
        });
    }

    @Override
    public void postWindowClose() {
        CathyPlugin.getDefault().getLicenseAgent()
                .removeLicenseChangedListener(this);
    }

    public void licenseChanged(ILicenseAgent agent) {
        int licenseType = agent.getLicenseType();
        if ((licenseType & ILicenseAgent.PRO_LICENSE_KEY) != 0) {
            licenseName = "Pro"; //$NON-NLS-1$
        } else if ((licenseType & ILicenseAgent.PLUS_LICENSE_KEY) != 0) {
            licenseName = "Plus"; //$NON-NLS-1$
        } else if ((licenseType & ILicenseAgent.PRO_SUBSCRIPTION) != 0) {
            licenseName = "Pro"; //$NON-NLS-1$
        } else {
            licenseName = null;
        }
        updateWindowTitle();
    }

    public void partActivated(IWorkbenchPartReference partRef) {
        if (partRef instanceof IEditorReference) {
            if (activePartRef != null) {
                activePartRef.removePropertyListener(this);
            }
            activePartRef = partRef;
            activePartRef.addPropertyListener(this);
        }
        updateWindowTitle();
    }

    public void partBroughtToTop(IWorkbenchPartReference partRef) {
    }

    public void partClosed(IWorkbenchPartReference partRef) {
        if (partRef == activePartRef) {
            activePartRef = null;
            partRef.removePropertyListener(this);
        }
        updateWindowTitle();
//        checkNewWorkbookEditor();
    }

    public void partDeactivated(IWorkbenchPartReference partRef) {
    }

    public void partHidden(IWorkbenchPartReference partRef) {
        updateWindowTitle();
    }

    public void partInputChanged(IWorkbenchPartReference partRef) {
        updateWindowTitle();
    }

    public void partOpened(IWorkbenchPartReference partRef) {
//        if (partRef instanceof IEditorReference
//                && !NewWorkbookEditor.EDITOR_ID.equals(partRef.getId())) {
//            checkNewWorkbookEditor();
//        }
    }

    public void partVisible(IWorkbenchPartReference partRef) {
        updateWindowTitle();
    }

    private void updateWindowTitle() {
        Display display = PlatformUI.getWorkbench().getDisplay();
        if (display != null && !display.isDisposed()) {
            display.asyncExec(new Runnable() {
                public void run() {
                    doUpdateWindowTitle();
                }
            });
        }
    }

    private void doUpdateWindowTitle() {
        IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
        IWorkbenchWindow window = configurer.getWindow();
        if (window == null)
            return;

        Shell shell = window.getShell();
        if (shell == null || shell.isDisposed())
            return;

        StringBuffer sb = new StringBuffer(20);

        if (homeShowing) {
            sb.append(
                    WorkbenchMessages.CathyWorkbenchWindowAdvisor_windowTitle_home_prefix);
        }

        IWorkbenchPage page = window.getActivePage();
        IEditorPart editor = null;
        if (page != null) {
            editor = page.getActiveEditor();
        }

        if (editor == null) {
            sb.append(WorkbenchMessages.AppWindowTitle);
            if (licenseName != null) {
                sb.append(' ');
                sb.append(licenseName);
            }
        } else {
            String text = editor.getClass().toString()
                    .contains("org.xmind.ui.internal.browser") ? null //$NON-NLS-1$
                            : editor.getTitleToolTip();
            if (text == null) {
                text = editor.getTitle();
            } else {
                text = FileUtils.getFileName(text);
            }
            sb.append(text);
        }

        configurer.setTitle(sb.toString());

        if (titlePathUpdater != null) {
            titlePathUpdater.updateTitlePath(shell, computeTitlePath(page));
        }
    }

    private String computeTitlePath(IWorkbenchPage page) {
        IEditorPart activeEditor = page.getActiveEditor();
        if (activeEditor != null) {
            IEditorInput editorInput = activeEditor.getEditorInput();
            if (editorInput != null) {
                File file = MME.getFile(editorInput);
                if (file != null)
                    return file.getAbsolutePath();
            }
        }
        return null;
    }

    public void propertyChanged(Object source, int propId) {
        updateWindowTitle();
    }

//    private void checkNewWorkbookEditor() {
//        if (checkingNewWorkbookEditor)
//            return;
//        checkingNewWorkbookEditor = true;
//        Display.getCurrent().asyncExec(new Runnable() {
//            public void run() {
//                try {
//                    IWorkbenchWindow window = getWindowConfigurer().getWindow();
//                    Shell shell = window.getShell();
//                    if (shell == null || shell.isDisposed())
//                        return;
//
//                    IWorkbenchPage page = window.getActivePage();
//                    if (page == null)
//                        return;
//
//                    int numEditors = 0;
//                    IEditorReference[] editors = page.getEditorReferences();
//                    for (int i = 0; i < editors.length; i++) {
//                        IEditorReference editor = editors[i];
//                        if (!NewWorkbookEditor.EDITOR_ID
//                                .equals(editor.getId())) {
//                            numEditors++;
//                        }
//                    }
//
//                    if (numEditors > 0) {
//                        // Has normal editors, hide NewWorkbookEditor:
//                        NewWorkbookEditor.hideFrom(window);
//                    } else {
//                        // No normal editors, show NewWorkbookEditor:
//                        NewWorkbookEditor.showIn(window);
//                    }
//
//                } finally {
//                    checkingNewWorkbookEditor = false;
//                }
//            }
//        });
//    }

}
