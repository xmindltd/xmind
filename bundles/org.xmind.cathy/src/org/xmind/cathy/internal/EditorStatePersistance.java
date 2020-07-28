/* ******************************************************************************
 * Copyright (c) 2006-2016 XMind Ltd. and others.
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
/**
 * 
 */
package org.xmind.cathy.internal;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.xmind.ui.util.Cancelable;
import org.xmind.ui.util.ICancelable;

/**
 * @author Frank Shaka
 */
public class EditorStatePersistance {

    private static final String PATH_SESSION = "editorstates.xml"; //$NON-NLS-1$

    private class EditorStateLabelProvider extends LabelProvider {

        private LocalResourceManager resources;

        /**
         * 
         */
        public EditorStateLabelProvider() {
            this.resources = new LocalResourceManager(
                    JFaceResources.getResources());
        }

        public String getText(Object element) {
            if (element instanceof IMemento) {
                IMemento state = (IMemento) element;
                String name = state.getString(IWorkbenchConstants.TAG_TITLE);
                if (name == null) {
                    name = state.getString(IWorkbenchConstants.TAG_NAME);
                }
                if (name == null) {
                    name = state.getString(IWorkbenchConstants.TAG_PART_NAME);
                }
                if (name == null) {
                    String editorId = state
                            .getString(IWorkbenchConstants.TAG_ID);
                    if (editorId != null) {
                        IEditorDescriptor editor = workbench.getEditorRegistry()
                                .findEditor(editorId);
                        if (editor != null) {
                            name = editor.getLabel();
                        }
                    }
                }
                if (name == null) {
                    name = ""; //$NON-NLS-1$
                }
                return name;
            }
            return super.getText(element);
        }

        public Image getImage(Object element) {
            if (element instanceof IMemento) {
                IMemento state = (IMemento) element;
                String editorId = state.getString(IWorkbenchConstants.TAG_ID);
                if (editorId != null) {
                    IEditorDescriptor editor = workbench.getEditorRegistry()
                            .findEditor(editorId);
                    if (editor != null) {
                        ImageDescriptor icon = editor.getImageDescriptor();
                        if (icon != null)
                            return (Image) resources.get(icon);
                    }
                }
            }
            return super.getImage(element);
        }

        /*
         * (non-Javadoc)
         * @see org.eclipse.jface.viewers.BaseLabelProvider#dispose()
         */
        @Override
        public void dispose() {
            resources.dispose();
            super.dispose();
        }

    }

    private final IWorkbench workbench;
    private final IPath basePath;
    private final ILogger logger;
    private final int autoSaveIntervals;
    private ICancelable autoSaveTask;

    /**
     * 
     */
    public EditorStatePersistance(IWorkbench workbench, IPath basePath,
            ILogger logger, int autoSaveIntervals) {
        this.workbench = workbench;
        this.basePath = basePath;
        this.logger = logger == null ? ILogger.DEFAULT : logger;
        this.autoSaveIntervals = autoSaveIntervals;
        this.autoSaveTask = null;
    }

    /**
     * Must be called within the UI thread.
     * 
     * @throws WorkbenchException
     */
    public void startUp() throws WorkbenchException {
        final Display display = workbench.getDisplay();
        Assert.isNotNull(display);
        Assert.isTrue(display == Display.getCurrent());

        try {
            recoverLastSession();
        } finally {
            schedule(display);
            workbench.addWorkbenchListener(new IWorkbenchListener() {
                public boolean preShutdown(IWorkbench workbench,
                        boolean forced) {
                    return true;
                }

                public void postShutdown(IWorkbench workbench) {
                    shutDown();
                }
            });
        }
    }

    /**
     * @return
     */
    private File getSessionFile() {
        return basePath.append(PATH_SESSION).toFile();
    }

    /**
     * @throws WorkbenchException
     */
    private void recoverLastSession() throws WorkbenchException {
        File sessionFile = getSessionFile();
        if (sessionFile == null || !sessionFile.exists())
            return;

        XMLMemento root;
        try {
            Reader reader = new BufferedReader(new FileReader(sessionFile));
            root = XMLMemento.createReadRoot(reader);
        } catch (IOException e) {
            logger.logError(null, e);
            return;
        } finally {
            sessionFile.delete();
        }

        IMemento[] states = root.getChildren(IWorkbenchConstants.TAG_EDITOR);
        if (states.length == 0)
            return;

        ListSelectionDialog dialog = new ListSelectionDialog(null, states,
                new ArrayContentProvider(), new EditorStateLabelProvider(),
                WorkbenchMessages.appWindow_ListSelectionDialog_Text);
        dialog.setTitle(WorkbenchMessages.appWindow_ListSelectionDialog_Title);
        dialog.setInitialSelections(states);
        int ret = dialog.open();
        if (ret == ListSelectionDialog.CANCEL)
            return;

        Object[] result = dialog.getResult();
        if (result == null || result.length == 0)
            return;

        states = new IMemento[result.length];
        System.arraycopy(result, 0, states, 0, result.length);

        IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
        if (window == null) {
            window = workbench.openWorkbenchWindow(null);
        }
        IWorkbenchPage page = window.getActivePage();
        if (page == null) {
            page = window.openPage(null);
        }
        page.openEditors(null, null, states, 0, 0);
    }

    public void shutDown() {
        ICancelable task = this.autoSaveTask;
        this.autoSaveTask = null;
        if (task != null) {
            task.cancel();
        }

        File sessionFile = getSessionFile();
        if (sessionFile != null) {
            sessionFile.delete();
        }
    }

    private void schedule(final Display display) {
        ICancelable oldTask = this.autoSaveTask;
        ICancelable task = new Cancelable() {
            @Override
            protected void doJob() {
                autoSave(display);
            }
        };
        this.autoSaveTask = task;
        display.timerExec(autoSaveIntervals, task);
        if (oldTask != null) {
            oldTask.cancel();
        }
    }

    private void autoSave(final Display display) {
        try {
            save();
        } catch (Throwable e) {
            logger.logError(null, e);
        }

        schedule(display);
    }

    /**
     * 
     */
    private void save() throws IOException {
        File sessionFile = getSessionFile();
        if (sessionFile == null)
            return;

        List<IMemento> states = new ArrayList<IMemento>();
        for (IWorkbenchWindow window : workbench.getWorkbenchWindows()) {
            IWorkbenchPage page = window.getActivePage();
            if (page == null)
                continue;
            IMemento[] editorState = page
                    .getEditorState(page.getEditorReferences(), true);
            states.addAll(Arrays.asList(editorState));
        }

        XMLMemento root = XMLMemento
                .createWriteRoot(IWorkbenchConstants.TAG_EDITORS);
        if (root == null) {
            return;
        }

        for (IMemento state : states) {
            IMemento st = root.createChild(state.getType());
            st.putMemento(state);
        }

        File dir = sessionFile.getParentFile();
        if (dir != null && !dir.exists()) {
            dir.mkdirs();
        }
        BufferedWriter writer = new BufferedWriter(new FileWriter(sessionFile));
        try {
            root.save(writer);
        } finally {
            writer.close();
        }
    }

}
