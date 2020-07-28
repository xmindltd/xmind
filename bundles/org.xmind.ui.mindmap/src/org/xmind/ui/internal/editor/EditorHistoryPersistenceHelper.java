package org.xmind.ui.internal.editor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.core.runtime.IPath;
import org.xmind.core.util.FileUtils;
import org.xmind.ui.editor.EditorHistoryItem;
import org.xmind.ui.editor.IEditorHistory;
import org.xmind.ui.editor.IEditorHistory.IEditorHistoryListener;
import org.xmind.ui.editor.IEditorHistoryItem;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.util.Logger;

/**
 * @author Ren Siu
 * @author Frank Shaka
 * @since 3.6.50
 */
public class EditorHistoryPersistenceHelper
        implements IEditorHistoryLoader, IEditorHistoryListener {

    private static final String VALUE_SEPARATOR = "#$#"; //$NON-NLS-1$

    private static final String KEY_PREFIX = "item."; //$NON-NLS-1$

    private static final String PINNED_KEY_HISTORY_ITEM_PREFIX = "pinned.editor.history.item."; //$NON-NLS-1$

    private static final String UNPINNED_KEY_HISTORY_ITEM_PREFIX = "unpinned.editor.history.item."; //$NON-NLS-1$

    private static final String PINNED_KEY_PREFIX = "pinned.item."; //$NON-NLS-1$

    private static final String THUMBNAIL_PREFIX = "thumbnail."; //$NON-NLS-1$

    private static final String OLD_FILE_NAME = "workbookHistory.properties"; //$NON-NLS-1$

    private static final String FILE_NAME = ".workbookHistory.properties"; //$NON-NLS-1$

    private static final String THUMBNAIL_DIR_NAME = ".thumbnailHistory"; //$NON-NLS-1$

    private static final Object END_OF_QUEUE = new Object();

    private static class EditorHistoryState {

        private final URI[] unpinnedInputURIs;
        private final URI[] pinnedInputURIs;
        private final Map<URI, URI> thumbnailURIs;
        private final Map<URI, IEditorHistoryItem> editorHistoryItems;

        /**
         * 
         */
        private EditorHistoryState(EditorHistoryImpl service) {
            this.unpinnedInputURIs = service
                    .getUnpinnedInputURIs(IEditorHistory.MAX_UNPINNED_SIZE);
            this.pinnedInputURIs = service.getPinnedInputURIs();
            this.thumbnailURIs = new HashMap<URI, URI>();
            this.editorHistoryItems = new HashMap<URI, IEditorHistoryItem>();

            for (URI uri : pinnedInputURIs) {
                URI thumbnailURI = service.getThumbnail(uri);
                if (thumbnailURI != null) {
                    thumbnailURIs.put(uri, thumbnailURI);
                }
                IEditorHistoryItem item = service.getItem(uri);
                if (item != null)
                    editorHistoryItems.put(uri, item);
            }
            for (URI uri : unpinnedInputURIs) {
                URI thumbnailURI = service.getThumbnail(uri);
                if (thumbnailURI != null) {
                    thumbnailURIs.put(uri, thumbnailURI);
                }
                IEditorHistoryItem item = service.getItem(uri);
                if (item != null)
                    editorHistoryItems.put(uri, item);
            }
        }

        /**
         * @param service
         * @return
         */
        public static EditorHistoryState createFrom(EditorHistoryImpl service) {
            return new EditorHistoryState(service);
        }

        /**
         * @return
         */
        public URI[] getPinnedInputURIs() {
            return pinnedInputURIs;
        }

        /**
         * @return
         */
        public URI[] getUnpinnedInputURIs() {
            return unpinnedInputURIs;
        }

        /**
         * @param input
         * @return
         */
        public URI getThumbnail(URI input) {
            return thumbnailURIs.get(input);
        }

        /**
         * @param input
         * @return
         */
        public IEditorHistoryItem getEditorHistoryItem(URI input) {
            return editorHistoryItems.get(input);
        }
    }

    private final IPath basePath;

    private final BlockingQueue<Object> stateQueue;

    private Thread thread;

    private EditorHistoryImpl service;

    /**
     * 
     */
    public EditorHistoryPersistenceHelper(IPath basePath) {
        this.basePath = basePath;
        this.stateQueue = new LinkedBlockingQueue<Object>();
        this.thread = null;
        this.service = null;
    }

    public void setService(EditorHistoryImpl service) {
        IEditorHistory oldService = this.service;
        if (service == oldService)
            return;

        this.service = service;
        if (oldService != null) {
            oldService.removeEditorHistoryListener(this);
        }
        if (service != null) {
            service.addEditorHistoryListener(this);
        }

        if (oldService == null && service != null) {
            startThread();
        } else if (oldService != null && service == null) {
            stopThread();
        }
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.ui.internal.editor.IEditorHistoryLoader#load(org.xmind.ui.
     * internal.editor.IEditorHistoryLoader.IEditorHistoryLoaderCallback)
     */
    @Override
    public void load(IEditorHistoryLoaderCallback callback) {
        Properties historyRepository = load();
        for (int index = 0; index < historyRepository.size(); index++) {
            String unpinnedKey = KEY_PREFIX + index;
            String pinnedKey = PINNED_KEY_PREFIX + index;
            String pinnedThumbnailKey = THUMBNAIL_PREFIX + pinnedKey;
            String unpinnedThumbnailKey = THUMBNAIL_PREFIX + unpinnedKey;
            String pinedEditorHistoryItemKey = PINNED_KEY_HISTORY_ITEM_PREFIX
                    + index;
            String unPinedEditorHistoryItemKey = UNPINNED_KEY_HISTORY_ITEM_PREFIX
                    + index;

            String unpinnedInputURI = historyRepository
                    .getProperty(unpinnedKey);
            String pinnedInputURI = historyRepository.getProperty(pinnedKey);
            String unpinnedThumbnailURI = historyRepository
                    .getProperty(unpinnedThumbnailKey);
            String pinnedThumbnailURI = historyRepository
                    .getProperty(pinnedThumbnailKey);

            unpinnedInputURI = fixFileUri(unpinnedInputURI);
            pinnedInputURI = fixFileUri(pinnedInputURI);
            unpinnedThumbnailURI = fixFileUri(unpinnedThumbnailURI);
            pinnedThumbnailURI = fixFileUri(pinnedThumbnailURI);

            String pinedItemJson = historyRepository
                    .getProperty(pinedEditorHistoryItemKey);
            IEditorHistoryItem pinnedItem = EditorHistoryItem
                    .readEditorHistoryItem(pinnedInputURI, pinedItemJson);

            String unpinedItemJson = historyRepository
                    .getProperty(unPinedEditorHistoryItemKey);
            IEditorHistoryItem unpinedItem = EditorHistoryItem
                    .readEditorHistoryItem(unpinnedInputURI, unpinedItemJson);

            try {
                if (unpinnedInputURI != null) {
                    URI unpinnedURI = new URI(unpinnedInputURI);
                    callback.inputURILoaded(unpinnedURI);

                    if (unpinedItem != null)
                        callback.editorHistoryItemsLoaded(unpinnedURI,
                                unpinedItem);

                    if (unpinnedThumbnailURI != null
                            && !unpinnedInputURI.isEmpty()) {
                        callback.thumbnailURILoaded(unpinnedURI,
                                new URI(unpinnedThumbnailURI));
                    }
                }
            } catch (URISyntaxException e) {
            }

            try {
                if (pinnedInputURI != null) {
                    URI pinnedURI = new URI(pinnedInputURI);
                    callback.pinnedInputURILoaded(pinnedURI);

                    if (pinnedItem != null)
                        callback.editorHistoryItemsLoaded(pinnedURI,
                                pinnedItem);

                    if (pinnedThumbnailURI != null
                            && !pinnedInputURI.isEmpty()) {
                        callback.thumbnailURILoaded(pinnedURI,
                                new URI(pinnedThumbnailURI));
                    }
                }
            } catch (URISyntaxException e) {
            }
        }

    }

    private String fixFileUri(String uri) {
        if (uri != null && uri.startsWith("file:")) { //$NON-NLS-1$
            String specialPart = uri.substring(5);
            boolean error = specialPart.startsWith("//") //$NON-NLS-1$
                    && !specialPart.startsWith("///"); //$NON-NLS-1$
            if (error) {
                return "file:" + "/" + specialPart; //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        return uri;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.xmind.ui.internal.editor.IEditorHistoryLoader#saveThumbnail(java.io.
     * InputStream)
     */
    @Override
    public URI saveThumbnail(InputStream thumbnailData) throws IOException {
        File thumbnailDir = getThumbnailDir();
        if (thumbnailDir == null)
            return null;

        if (!thumbnailDir.exists()) {
            thumbnailDir.mkdirs();
        }
        String thumbnailName = UUID.randomUUID().toString();
        File thumbnailFile = new File(thumbnailDir, thumbnailName);

        OutputStream output = new FileOutputStream(thumbnailFile);
        try {
            FileUtils.transfer(thumbnailData, output, false);
        } finally {
            output.close();
        }

        return thumbnailFile.toURI();
    }

    private File getThumbnailDir() {
        IPath basePath = MindMapUIPlugin.getDefault().getStateLocation();
        if (basePath == null)
            return null;
        IPath filePath = basePath.append(THUMBNAIL_DIR_NAME);
        return filePath.toFile();
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.ui.internal.editor.IEditorHistoryLoader#dispose()
     */
    @Override
    public void dispose() {
        setService(null);
    }

    private void startThread() {
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

        /*
         * Manually trigger a save operation on startup, for there might be some
         * changes to editor history before this earlyStartup method is called.
         * For example, files are opened on startup via double click in Finder.
         */
        editorHistoryChanged();
    }

    private void stopThread() {
        if (service != null)
            service.removeEditorHistoryListener(this);

        stateQueue.offer(END_OF_QUEUE);

        Thread thread = this.thread;
        this.thread = null;
        if (thread != null) {
            thread.interrupt();
        }
    }

    public void editorHistoryChanged() {
        if (service == null)
            return;

        EditorHistoryState state = EditorHistoryState.createFrom(service);
        stateQueue.offer(state);
    }

    private void runLoop() {
        try {
            while (thread != null) {
                Object state = stateQueue.take();
                if (state == END_OF_QUEUE)
                    break;
                save((EditorHistoryState) state);
                Thread.sleep(0);
            }
        } catch (InterruptedException e) {
            // interruption means stop
        }
    }

    private void save(EditorHistoryState persistable) {
        Properties repository = new Properties();

        URI[] pinnedInputURIs = persistable.getPinnedInputURIs();
        URI[] unpinnedInputURIs = persistable.getUnpinnedInputURIs();

        //Push pinned items
        for (int index = 0; index < pinnedInputURIs.length; index++) {
            URI input = pinnedInputURIs[index];
            if (input != null) {
                String key = PINNED_KEY_PREFIX + index;
                repository.setProperty(key, input.toString());

                String pinnedEditorHistoryItemKey = PINNED_KEY_HISTORY_ITEM_PREFIX
                        + index;
                IEditorHistoryItem pinnedItem = persistable
                        .getEditorHistoryItem(input);
                if (pinnedItem != null)
                    repository.setProperty(pinnedEditorHistoryItemKey,
                            pinnedItem.toJson());

                URI thumbnail = persistable.getThumbnail(input);
                String thumbnailKey = THUMBNAIL_PREFIX + key;
                if (thumbnail != null)
                    repository.setProperty(thumbnailKey, thumbnail.toString());
            }
        }

        // Push unpinned items
        for (int index = 0; index < unpinnedInputURIs.length; index++) {
            URI input = unpinnedInputURIs[index];
            if (input != null) {
                String key = KEY_PREFIX + index;
                repository.setProperty(key, input.toString());

                String unpinedEditorHistoryItemKey = UNPINNED_KEY_HISTORY_ITEM_PREFIX
                        + index;
                IEditorHistoryItem unpinnedItem = persistable
                        .getEditorHistoryItem(input);
                if (unpinnedItem != null)
                    repository.setProperty(unpinedEditorHistoryItemKey,
                            unpinnedItem.toJson());

                URI thumbnail = persistable.getThumbnail(input);
                String thumbnailKey = THUMBNAIL_PREFIX + key;
                if (thumbnail != null)
                    repository.setProperty(thumbnailKey, thumbnail.toString());
            }
        }

        // Save to properties file
        File file = getHistoryFile();
        if (file != null) {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            try {
                OutputStreamWriter out = new OutputStreamWriter(
                        new FileOutputStream(file), "UTF-8"); //$NON-NLS-1$
//                FileWriter writer = new FileWriter(file);
                try {
                    repository.store(out,
                            "Generated by org.xmind.ui.internal.editor.EditorHistoryService"); //$NON-NLS-1$
                } finally {
                    out.close();
                }
            } catch (IOException e) {
                Logger.log(e, "Failed to save workbook history to " //$NON-NLS-1$
                        + file.getAbsolutePath());
            }
        }

        File oldHistoryFile = getOldHistoryFile();
        if (oldHistoryFile != null && oldHistoryFile.exists()) {
            oldHistoryFile.delete();
        }
    }

    public Properties load() {
        Properties repository = new Properties();

        // Load form properties file
        File file = getHistoryFile();
        if (file != null && file.exists()) {
            try {
                InputStreamReader reader = new InputStreamReader(
                        new FileInputStream(file), "UTF-8"); //$NON-NLS-1$
//                FileReader reader = new FileReader(file);
                try {
                    repository.load(reader);
                } finally {
                    reader.close();
                }
            } catch (IOException e) {
                Logger.log(e, "Failed to load workbook history from " //$NON-NLS-1$
                        + file.getAbsolutePath());
            }
        } else {
            repository = loadOldHistoryRepository();
        }

        return repository;
    }

    private Properties loadOldHistoryRepository() {
        Properties repository = new Properties();

        // Load form old properties file
        File file = getOldHistoryFile();
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
            int count = 0;
            List<String> items = new ArrayList<String>();
            // Parse properties
            int size = repository.size();
            for (int i = 0; i < size; i++) {
                if (count >= IEditorHistory.MAX_UNPINNED_SIZE)
                    break;

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
                if (count >= IEditorHistory.MAX_UNPINNED_SIZE)
                    break;

                String input = (String) key;
                String info = repository.getProperty(input);

                items.add(input + VALUE_SEPARATOR + info);
                count++;
            }

            //Transfer old format into new format
            repository = new Properties();
            int countForPinned = 0;
            int countForUnpinned = 0;

            for (String inputAndInfo : items) {
                int index = inputAndInfo.indexOf(VALUE_SEPARATOR);
                if (index < 0)
                    continue;

                String input = inputAndInfo.substring(0, index);
                String info = inputAndInfo
                        .substring(index + VALUE_SEPARATOR.length());
                if (info == null || info.isEmpty())
                    continue;

                String thumbnail = extractThumbnailFromOldInfo(info);
                String thumbnailKey = THUMBNAIL_PREFIX;
                boolean isPinned = isPinnedBasedOldInfo(info);
                if (isPinned) {
                    String pinnedKey = PINNED_KEY_PREFIX + countForPinned;
                    thumbnailKey = THUMBNAIL_PREFIX + pinnedKey;
                    repository.setProperty(pinnedKey, input);
                    countForPinned++;
                } else {
                    String unpinnedKey = KEY_PREFIX + countForUnpinned;
                    thumbnailKey = THUMBNAIL_PREFIX + unpinnedKey;
                    repository.setProperty(unpinnedKey, input);
                    countForUnpinned++;
                }

                if (thumbnail != null && !thumbnail.isEmpty()) {
                    URI thumbnailURI = new File(thumbnail).toURI();
                    repository.setProperty(thumbnailKey,
                            thumbnailURI.toString());
                }
            }
        }

        return repository;

    }

    private static String extractThumbnailFromOldInfo(String info) {
        String thumbnail = info;
        if (info != null) {
            int sepPos = info.indexOf(',');
            if (sepPos > 0) {
                thumbnail = info.substring(0, sepPos);
            }
        }
        return thumbnail;
    }

    private static boolean isPinnedBasedOldInfo(String info) {
        return info != null && info.endsWith(",favor"); //$NON-NLS-1$
    }

    private File getHistoryFile() {
        if (basePath == null)
            return null;
        IPath filePath = basePath.append(FILE_NAME);
        return filePath.toFile();
    }

    private File getOldHistoryFile() {
        if (basePath == null)
            return null;
        IPath filePath = basePath.append(OLD_FILE_NAME);
        return filePath.toFile();
    }

}
