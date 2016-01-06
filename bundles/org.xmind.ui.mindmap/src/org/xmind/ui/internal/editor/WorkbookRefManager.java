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
package org.xmind.ui.internal.editor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.XMLMemento;
import org.xmind.core.Core;
import org.xmind.core.IWorkbook;
import org.xmind.core.IWorkspace;
import org.xmind.core.internal.InternalCore;
import org.xmind.gef.command.CommandStack;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.mindmap.IWorkbookRef;
import org.xmind.ui.mindmap.IWorkbookRefManager;
import org.xmind.ui.util.Logger;

public class WorkbookRefManager implements IWorkbookRefManager {

    private static final String TAG_OPENED_EDITORS = "editors"; //$NON-NLS-1$
    private static final String TAG_OPENED_EDITOR = "editor"; //$NON-NLS-1$
    private static final String TAG_OPENED_INPUT = "input"; //$NON-NLS-1$
    private static final String ATTR_OPENED_ID = "factoryID"; //$NON-NLS-1$
    private static final String ATTR_OPENED_TITLE = "title"; //$NON-NLS-1$
    private static final String TAG_OPENED_TEMPLOCATION = "tempLocation"; //$NON-NLS-1$
    private static final String ATTR_OPENED_FILE = "path"; //$NON-NLS-1$
    private static final String ATTR_SKIP_REVISIONS = "skip-revisions"; //$NON-NLS-1$

    private static class EditorState {
        String tempLocation;
        boolean skipRevisions;
    }

    private class AutoHibernateHandler implements Runnable {

        public void run() {
            final Thread thread = Thread.currentThread();
            long delay = getAutoHibernateIntervals();
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
            }

            while (autoHibernateThread == thread) {

                hibernateAll();

                if (autoHibernateThread != thread)
                    break;

                try {
                    Thread.sleep(getAutoHibernateIntervals());
                } catch (InterruptedException e) {
                }
            }
        }

    }

    private Map<Object, WorkbookRef> registry = new HashMap<Object, WorkbookRef>();

    private Map<IEditorInput, EditorState> lastSession = null;

    private static WorkbookRefManager instance;

    private Thread autoHibernateThread = null;

    /**
     * One minute delay between two hibernating operations.
     */
    private long autoHibernateIntervals = 60000;

    private File sessionFile = null;

    private Object sessionFileLock = new Object();

    private WorkbookRefManager() {
    }

    public Collection<IWorkbookRef> getWorkbookRefs() {
        return new ArrayList<IWorkbookRef>(registry.values());
    }

    public synchronized WorkbookRef addReferrer(Object source,
            IWorkbookReferrer referrer) throws CoreException {
        WorkbookRef ref = registry.get(source);
        if (ref != null) {
            ref.addReferrer(referrer);
            return ref;
        }
        ref = createWorkbookRef(source, referrer);
        ref.addReferrer(referrer);
        ensureAutoHibernateStarted();
        return ref;
    }

    private synchronized WorkbookRef createWorkbookRef(Object source,
            IWorkbookReferrer referrer) throws CoreException {
        WorkbookRef ref = new WorkbookRef();
        WorkbookRefInitializer.getInstance().initialize(ref, source, referrer);
        if (!ref.isReady()) {
            IStatus status = new Status(IStatus.ERROR,
                    MindMapUIPlugin.PLUGIN_ID,
                    "Failed to create a workbook reference with valid source."); //$NON-NLS-1$
            throw new CoreException(status);
        }
        initializeRef((WorkbookRef) ref, source);

        if (lastSession != null) {
            EditorState state = lastSession.get(source);
            if (state != null) {
                File file = MME.getFile(source);
                ref.setWorkbookLoader(
                        new TempWorkbookLoader(ref, state.tempLocation,
                                file == null ? null : file.getAbsolutePath(),
                                state.skipRevisions));
            }
        }

        registry.put(source, ref);
        WorkbookBackupManager.getInstance().addWorkbook(ref);
        return ref;
    }

    private void initializeRef(WorkbookRef ref, Object source)
            throws CoreException {
        if (ref.getCommandStack() == null) {
            ref.setCommandStack(new CommandStack());
        }
        if (ref.getKey() == null) {
            ref.setKey(source);
        }
    }

    public synchronized void removeReferrer(Object source,
            IWorkbookReferrer referrer) {
        WorkbookRef ref = registry.get(source);
        try {
            if (ref != null) {
                try {
                    ref.removeReferrer(referrer);
                } finally {
                    if (!ref.isReferred()) {
                        registry.remove(source);

                        // Have to check if there's still other references
                        // relying on this workbook's resources.
                        boolean noRelatedRef = findRef(
                                ref.getWorkbook()) == null;

                        ref.dispose(noRelatedRef);

                        WorkbookBackupManager.getInstance().removeWorkbook(ref);

                        hibernateAll();
                    }
                }
            }
        } finally {
            if (registry.isEmpty()) {
                stopAutoHibernate();
            }
        }
    }

    public synchronized IWorkbookRef createRef(IEditorInput editorInput,
            IEditorPart editor) {
        IWorkbookReferrer referrer = findWorkbookReferrer(editor);
        if (referrer == null)
            return null;
        try {
            return addReferrer(editorInput, referrer);
        } catch (CoreException e) {
            Logger.log(e);
            return null;
        }
    }

    private IWorkbookReferrer findWorkbookReferrer(IEditorPart editor) {
        IWorkbookReferrer referrer = null;
        if (editor instanceof IWorkbookReferrer)
            referrer = (IWorkbookReferrer) editor;
        else
            referrer = (IWorkbookReferrer) editor
                    .getAdapter(IWorkbookReferrer.class);
        return referrer;
    }

    public synchronized void disposeRef(IEditorInput editorInput,
            IEditorPart editor) {
        IWorkbookReferrer referrer = findWorkbookReferrer(editor);
        if (referrer != null)
            removeReferrer(editorInput, referrer);
    }

    public IWorkbookRef findRef(IWorkbook workbook) {
        if (workbook == null)
            return null;
        for (IWorkbookRef ref : registry.values()) {
            if (workbook.equals(ref.getWorkbook()))
                return ref;
        }
        return null;
    }

    public static WorkbookRefManager getInstance() {
        if (instance == null) {
            instance = new WorkbookRefManager();
        }
        return instance;
    }

    void changeKey(WorkbookRef ref, Object oldKey, Object newKey) {
        WorkbookRef oldRef = registry.remove(oldKey);
        if (oldRef != ref)
            return;
        registry.put(newKey, ref);
    }

    public long getAutoHibernateIntervals() {
        return autoHibernateIntervals;
    }

    public void setAutoHibernateIntervals(long autoHibernateIntervals) {
        this.autoHibernateIntervals = Math.max(60000, autoHibernateIntervals);
    }

    private void ensureAutoHibernateStarted() {
        if (autoHibernateThread != null)
            return;

        Thread newThread = new Thread(new AutoHibernateHandler());
        newThread.setDaemon(true);
        newThread.setName("XMind: Auto Save Temporary Workbooks"); //$NON-NLS-1$
        newThread.setPriority(Thread.MIN_PRIORITY);
        autoHibernateThread = newThread;
        newThread.start();
    }

    public void hibernateAll() {
        if (registry.isEmpty()) {
            stopAutoHibernate();
            synchronized (sessionFileLock) {
                getSessionFile().delete();
            }
        } else {
            synchronized (sessionFileLock) {
                saveSession();
            }
        }
    }

    private void stopAutoHibernate() {
        Thread oldThread = autoHibernateThread;
        autoHibernateThread = null;
        if (oldThread != null) {
            oldThread.interrupt();
        }
    }

    protected void saveSession() {
        try {
            XMLMemento memento = XMLMemento.createWriteRoot(TAG_OPENED_EDITORS);
            for (WorkbookRef ref : registry.values()) {
                IWorkbook workbook = ref.getWorkbook();
                if (workbook != null) {
                    Object key = ref.getKey();
                    try {
                        workbook.saveTemp();
                    } catch (Throwable e) {
                        Logger.log(e,
                                "WorkbookRefManager: Error occurred while saving workbook to a temporary location in background: " //$NON-NLS-1$
                                        + key.toString());
                    }
                    if (InternalCore.DEBUG_WORKBOOK_SAVE)
                        Logger.log(
                                "WorkbookRefManager: Finished saving opened workbook " //$NON-NLS-1$
                                        + ref.getKey().toString()
                                        + " to its temp location."); //$NON-NLS-1$
                    IMemento editorMem = memento.createChild(TAG_OPENED_EDITOR);
                    if (key instanceof IEditorInput) {
                        saveEditorInput(editorMem, (IEditorInput) key);
                    }
                    saveTempLocation(editorMem, workbook.getTempLocation());
                    editorMem.putBoolean(ATTR_SKIP_REVISIONS,
                            workbook.isSkipRevisionsWhenSaving());
                }
            }
            File sf = getSessionFile();
            Writer writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(sf), "utf-8"), //$NON-NLS-1$
                    1024);
            try {
                memento.save(writer);
            } finally {
                writer.close();
            }
        } catch (Throwable e) {
            Logger.log(e,
                    "WorkbookRefManager: Error occurred while saving opened workbook session info."); //$NON-NLS-1$
        }
    }

    private File getSessionFile() {
        if (sessionFile == null) {
            sessionFile = new File(
                    Core.getWorkspace().getTempFile(IWorkspace.FILE_OPENED));
        }
        return sessionFile;
    }

    private void saveEditorInput(IMemento memento, IEditorInput input) {
        IMemento inputMem = memento.createChild(TAG_OPENED_INPUT);
        IPersistableElement p = input.getPersistable();
        if (p != null) {
            p.saveState(inputMem);
            String id = p.getFactoryId();
            inputMem.putString(ATTR_OPENED_ID, id);
        } else {
            inputMem.putString(ATTR_OPENED_TITLE, input.getName());
        }
    }

    private IEditorInput loadEditorInput(IMemento memento) {
        IMemento inputMem = memento.getChild(TAG_OPENED_INPUT);
        if (inputMem == null)
            return new WorkbookEditorInput();
        String factoryId = inputMem.getString(ATTR_OPENED_ID);
        if (factoryId != null) {
            IElementFactory factory = PlatformUI.getWorkbench()
                    .getElementFactory(factoryId);
            return (IEditorInput) factory.createElement(inputMem);
        } else {
            String title = inputMem.getString(ATTR_OPENED_TITLE);
            return new WorkbookEditorInput(title);
        }
    }

    private void saveTempLocation(IMemento memento, String path) {
        IMemento pathMem = memento.createChild(TAG_OPENED_TEMPLOCATION);
        pathMem.putString(ATTR_OPENED_FILE, path);
    }

    private EditorState loadEditorState(IMemento memento) {
        IMemento tempMem = memento.getChild(TAG_OPENED_TEMPLOCATION);
        if (tempMem == null)
            return null;
        EditorState state = new EditorState();
        state.tempLocation = tempMem.getString(ATTR_OPENED_FILE);
        Boolean skipRevisions = tempMem.getBoolean(ATTR_SKIP_REVISIONS);
        state.skipRevisions = skipRevisions != null
                && skipRevisions.booleanValue();
        return state;
    }

    public List<IEditorInput> loadLastSession() {
        File file = new File(
                Core.getWorkspace().getTempFile(IWorkspace.FILE_TO_RECOVER));
        if (!file.exists() || !file.isFile() || !file.canRead())
            return null;
        lastSession = null;
        List<IEditorInput> session = loadSessionFromFile(file);
        file.delete();
        return session;
    }

    private List<IEditorInput> loadSessionFromFile(File file) {
        List<IEditorInput> list = null;
        FileInputStream fileStream = null;
        InputStreamReader reader = null;
        try {
            fileStream = new FileInputStream(file);
            reader = new InputStreamReader(fileStream, "utf-8"); //$NON-NLS-1$
            XMLMemento memento = XMLMemento.createReadRoot(reader);
            IMemento[] elements = memento.getChildren(TAG_OPENED_EDITOR);
            for (IMemento editorMem : elements) {
                IEditorInput input = loadEditorInput(editorMem);
                EditorState state = loadEditorState(editorMem);
                if (state != null) {
                    if (lastSession == null)
                        lastSession = new HashMap<IEditorInput, EditorState>();
                    lastSession.put(input, state);
                }
                if (list == null)
                    list = new ArrayList<IEditorInput>();
                list.add(input);
            }
        } catch (Exception e) {
            Logger.log(e, "Failed to load session log."); //$NON-NLS-1$
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e2) {
                }
            }
            if (fileStream != null) {
                try {
                    fileStream.close();
                } catch (IOException e2) {
                }
            }
        }
        return list;
    }

    public void clearLastSession() {
        lastSession = null;
    }

}