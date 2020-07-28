/* ******************************************************************************
 * Copyright (c) 2006-2015 XMind Ltd. and others.
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
package org.xmind.ui.internal;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.PlatformUI;
import org.xmind.ui.IEditorHistory;
import org.xmind.ui.IEditorHistory.IEditorHistoryListener;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.Logger;

/**
 * 
 * @author Frank Shaka
 * @deprecated
 */
@Deprecated
public class EditorHistoryPersistenceService
        implements IEditorHistoryListener, IWorkbenchListener {

    public static final String VALUE_SEPARATOR = "#$#"; //$NON-NLS-1$

    private static final String KEY_PREFIX = "item."; //$NON-NLS-1$

    private static final String FILE_NAME = "workbookHistory.properties"; //$NON-NLS-1$

    private Thread thread = null;

    private URI[] contentToSave = null;

    private Object contentNotifier = new Object();

    private static EditorHistoryPersistenceService INSTANCE = new EditorHistoryPersistenceService();

    public static EditorHistoryPersistenceService getInstance() {
        return INSTANCE;
    }

    public void preStartup() {
        IWorkbench wb = PlatformUI.getWorkbench();
        wb.addWorkbenchListener(this);

        Thread thread = new Thread(new Runnable() {
            public void run() {
                runLoop();
            }
        });
        thread.setName("EditorHistoryPersistenceThread"); //$NON-NLS-1$
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.setDaemon(true);
        this.thread = thread;
        thread.start();

        MindMapUI.getEditorHistory().addEditorHistoryListener(this);

        /*
         * Manually trigger a save operation on startup, for there might be some
         * changes to editor history before this earlyStartup method is called.
         * For example, files are opened on startup via double click in Finder.
         */
        editorHistoryChanged();
    }

    public void postShutdown(IWorkbench workbench) {
        MindMapUI.getEditorHistory().removeEditorHistoryListener(this);

        Thread thread = this.thread;
        this.thread = null;
        if (thread != null) {
            thread.interrupt();
        }
    }

    public boolean preShutdown(IWorkbench workbench, boolean forced) {
        return true;
    }

    public void editorHistoryChanged() {
        URI[] inputURIs = MindMapUI.getEditorHistory().getAllInputURIs();
        URI[] content = new URI[inputURIs.length];
        System.arraycopy(inputURIs, 0, content, 0, inputURIs.length);
        synchronized (contentNotifier) {
            contentToSave = content;
            contentNotifier.notifyAll();
        }
    }

    private void runLoop() {
        try {
            synchronized (contentNotifier) {
                while (thread != null) {
                    contentNotifier.wait();
                    URI[] content = null;
                    if (contentToSave != null) {
                        content = new URI[contentToSave.length];
                        System.arraycopy(contentToSave, 0, content, 0,
                                contentToSave.length);
                    }
                    if (content != null) {
                        save(content);
                    }
                    Thread.sleep(0);
                }
            }
        } catch (InterruptedException e) {
            // ignore interruptions
        }
    }

    private static void save(URI[] content) {
        Properties repository = new Properties();
        IEditorHistory editorHistory = MindMapUI.getEditorHistory();

        // Push items
        for (int index = 0; index < content.length; index++) {
            URI input = content[index];
            String info = editorHistory.getInfo(input);
            if (input != null && info != null) {
                String key = KEY_PREFIX + index;
                repository.setProperty(key,
                        input.toString() + VALUE_SEPARATOR + info);

            }
        }

        // Save to properties file
        File file = getHistoryFile();
        if (file != null) {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            try {
                FileWriter writer = new FileWriter(file);
                try {
                    repository.store(writer,
                            "Generated by org.xmind.ui.internal.editor.EditorHistoryService"); //$NON-NLS-1$
                } finally {
                    writer.close();
                }
            } catch (IOException e) {
                Logger.log(e, "Failed to save workbook history to " //$NON-NLS-1$
                        + file.getAbsolutePath());
            }
        }
    }

    public static List<String> load() {
        Properties repository = new Properties();

        // Load form properties file
        File file = getHistoryFile();
        if (file != null && file.exists()) {
            try {
                FileReader reader = new FileReader(file);
                try {
                    repository.load(reader);
                } finally {
                    reader.close();
                }
            } catch (IOException e) {
                Logger.log(e, "Failed to load workbook history from " //$NON-NLS-1$
                        + file.getAbsolutePath());
            }
        }

        int count = 0;
        List<String> items = new ArrayList<String>();
        // Parse properties
        int size = repository.size();
        for (int i = 0; i < size; i++) {
            if (count >= EditorHistory.MAX_SIZE)
                return items;

            String key = KEY_PREFIX + i;
            String value = repository.getProperty(key);
            if (value == null)
                continue;
            items.add(value);
            repository.remove(key);

            count++;
        }

        //Compatible with the old version
        Set<Object> oldVersionKeys = repository.keySet();
        for (Object key : oldVersionKeys) {
            if (count >= EditorHistory.MAX_SIZE)
                return items;

            String input = (String) key;
            String info = repository.getProperty(input);

            items.add(input + VALUE_SEPARATOR + info);
            count++;
        }

        return items;

    }

    private static File getHistoryFile() {
        IPath basePath = MindMapUIPlugin.getDefault().getStateLocation();
        if (basePath == null)
            return null;
        IPath filePath = basePath.append(FILE_NAME);
        return filePath.toFile();
    }

}
