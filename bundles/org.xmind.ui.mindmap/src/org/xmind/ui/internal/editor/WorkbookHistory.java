package org.xmind.ui.internal.editor;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.ui.IEditorInput;
import org.xmind.ui.IEditorHistory;

/**
 * 
 * @author Frank Shaka
 * @deprecated Use {@link IEditorHistory} instead.
 */
@Deprecated
public class WorkbookHistory {

    @Deprecated
    public static interface IWorkbookHistoryListener {

        void workbookHistoryUpdated();

    }

    private static final int MAX_SIZE = 100;

    private static WorkbookHistory instance = null;

    private List<WorkbookHistoryItem> items = new ArrayList<WorkbookHistoryItem>(
            MAX_SIZE);

    private long lastModifiedTime = 0;

    private Thread saveRunner = null;

    private ListenerList listeners = new ListenerList();

    private WorkbookHistory() {
        load();
    }

    public void addWorkbookHistoryListener(IWorkbookHistoryListener listener) {
        listeners.add(listener);
    }

    public void removeWorkbookHistoryListener(
            IWorkbookHistoryListener listener) {
        listeners.remove(listener);
    }

    /**
     * Add editor input to this history. Currently only editor inputs adaptable
     * to a File or IFileStore are supported. Other types will be considered
     * later.
     * 
     * @param input
     *            the editor input to add
     */
    public void add(IEditorInput input) {
        File file = MME.getFile(input);
        if (file != null) {
            String uri = WorkbookHistoryItem.toURI(file.getAbsolutePath());
            remove(input, uri);
            items.add(0, new WorkbookHistoryItem(input, uri,
                    System.currentTimeMillis()));
            while (items.size() > MAX_SIZE) {
                items.remove(items.size() - 1);
            }
            lastModifiedTime = System.currentTimeMillis();
            scheduleSave();
            fireUpdated();
        }
    }

    private void remove(IEditorInput input, String uri) {
        Iterator<WorkbookHistoryItem> it = items.iterator();
        while (it.hasNext()) {
            WorkbookHistoryItem item = it.next();
            if ((uri != null && uri.equals(item.getURI()))
                    || (item.getExistingEditorInput() != null
                            && item.getExistingEditorInput().equals(input))) {
                it.remove();
            }
        }
    }

    /**
     * Removes add traces added by the specified editor input.
     * 
     * @param input
     *            the editor input to remove
     */
    public void remove(IEditorInput input) {
        if (input == null)
            return;
        remove(input, null);
        lastModifiedTime = System.currentTimeMillis();
        scheduleSave();
        fireUpdated();
    }

    /**
     * Clears all workbook opening traces.
     */
    public void clear() {
        items.clear();
        lastModifiedTime = System.currentTimeMillis();
        save();
    }

    public WorkbookHistoryItem[] getItems() {
        return items.toArray(new WorkbookHistoryItem[items.size()]);
    }

    public WorkbookHistoryItem[] getTopItems(int size) {
        size = Math.max(0, Math.min(size, items.size()));
        return items.subList(0, size).toArray(new WorkbookHistoryItem[size]);
    }

    public long getLastModifiedTime() {
        return lastModifiedTime;
    }

    public boolean load() {
        File file = getHistoryFile();
        if (file == null)
            return false;
        Properties cache = new Properties();
        try {
            InputStream stream = new BufferedInputStream(
                    new FileInputStream(file), 1024);
            try {
                cache.load(stream);
            } finally {
                try {
                    stream.close();
                } catch (IOException e) {
                }
            }
        } catch (IOException e) {
            return false;
        }

        items.clear();

        if (!cache.isEmpty()) {
            for (int i = 0; i < MAX_SIZE; i++) {
                String value = cache.getProperty("item." + i); //$NON-NLS-1$
                if (value != null) {
                    int sepPos = value.indexOf(',');
                    if (sepPos > 0) {
                        try {
                            long time = Long
                                    .parseLong(value.substring(0, sepPos), 10);
                            String uri = value.substring(sepPos + 1);
                            items.add(new WorkbookHistoryItem(null, uri, time));
                        } catch (NumberFormatException e) {
                        }
                    }
                }
            }
        }
        lastModifiedTime = System.currentTimeMillis();

        return true;
    }

    private synchronized void scheduleSave() {
        Thread t = saveRunner;
        if (t != null) {
            t.interrupt();
        }
        t = new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    return;
                }
                save();
            }
        });
        t.setDaemon(true);
        t.setName("Save Workbook History"); //$NON-NLS-1$
        t.start();
        saveRunner = t;
    }

    public synchronized boolean save() {
        File file = getHistoryFile();
        if (file == null)
            return false;

        File dir = file.getParentFile();
        if (!dir.exists() || !dir.isDirectory()) {
            if (!dir.mkdirs())
                return false;
            if (!dir.exists() || !dir.isDirectory())
                return false;
        }

        Properties cache = new Properties();
        for (int i = 0; i < items.size(); i++) {
            WorkbookHistoryItem item = items.get(i);
            String key = "item." + i; //$NON-NLS-1$
            String value = Long.toString(item.getTime(), 10) + "," //$NON-NLS-1$
                    + item.getURI();
            cache.put(key, value);
        }

        try {
            FileOutputStream stream = new FileOutputStream(file);
            try {
                cache.store(stream,
                        "Generated by org.xmind.ui.internal.editor.WorkbookHistory"); //$NON-NLS-1$
            } finally {
                try {
                    stream.close();
                } catch (IOException e) {
                }
            }
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    private void fireUpdated() {
        if (listeners.isEmpty())
            return;

        Object[] theListeners = listeners.getListeners();
        for (int i = 0; i < theListeners.length; i++) {
            final IWorkbookHistoryListener listener = (IWorkbookHistoryListener) theListeners[i];
            SafeRunner.run(new SafeRunnable() {
                public void run() throws Exception {
                    listener.workbookHistoryUpdated();
                }
            });
        }
    }

    private static File getHistoryFile() {
        Location instanceLocation = Platform.getInstanceLocation();
        if (instanceLocation == null)
            return null;
        URL url = instanceLocation.getURL();
        if (url == null)
            return null;
        try {
            url = FileLocator.toFileURL(url);
        } catch (IOException e) {
            return null;
        }
        String workspace = url.getFile();
        if (workspace == null || "".equals(workspace)) //$NON-NLS-1$
            return null;
        IProduct product = Platform.getProduct();
        if (product != null && "org.xmind.cathy.application".equals(product //$NON-NLS-1$
                .getApplication())) {
            return new File(workspace, "workbookhistory.properties"); //$NON-NLS-1$
        }
        return new File(new File(workspace, ".xmind"), //$NON-NLS-1$
                "workbookhistory.properties"); //$NON-NLS-1$
    }

    public static WorkbookHistory getInstance() {
        if (instance == null)
            instance = new WorkbookHistory();
        return instance;
    }
}
