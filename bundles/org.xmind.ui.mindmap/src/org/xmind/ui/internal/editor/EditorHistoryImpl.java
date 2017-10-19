package org.xmind.ui.internal.editor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.ui.services.IDisposable;
import org.xmind.ui.editor.EditorHistoryItem;
import org.xmind.ui.editor.IEditorHistory;
import org.xmind.ui.editor.IEditorHistoryItem;
import org.xmind.ui.internal.editor.IEditorHistoryLoader.IEditorHistoryLoaderCallback;

/**
 * @author Ren Siu
 * @author Frank Shaka
 * @since 3.6.50
 */
public final class EditorHistoryImpl implements IEditorHistory, IDisposable {

    private final IEditorHistoryLoader loader;

    private final List<URI> unpinnedInputURIs;

    private final List<URI> pinnedInputURIs;

    private final Map<URI, URI> inputToThumbnail;

    private final Map<URI, IEditorHistoryItem> editorHistoryItems;

    private final ListenerList listeners;

    public EditorHistoryImpl(IEditorHistoryLoader loader) {
        this.loader = loader;
        this.unpinnedInputURIs = new ArrayList<URI>();
        this.pinnedInputURIs = new ArrayList<URI>();
        this.inputToThumbnail = new HashMap<URI, URI>();
        this.listeners = new ListenerList();
        this.editorHistoryItems = new HashMap<URI, IEditorHistoryItem>();
        init();
    }

    private void init() {
        loader.load(new IEditorHistoryLoaderCallback() {

            @Override
            public void thumbnailURILoaded(URI inputURI, URI thumbnailURI) {
                inputToThumbnail.put(inputURI, thumbnailURI);
            }

            @Override
            public void pinnedInputURILoaded(URI inputURI) {
                pinnedInputURIs.add(inputURI);
            }

            @Override
            public void inputURILoaded(URI inputURI) {
                unpinnedInputURIs.add(inputURI);
            }

            @Override
            public void editorHistoryItemsLoaded(URI inputURI,
                    IEditorHistoryItem item) {
                editorHistoryItems.put(inputURI, item);
            }

        });
        while (unpinnedInputURIs.size() > MAX_UNPINNED_SIZE) {
            unpinnedInputURIs.remove(unpinnedInputURIs.size() - 1);
        }
    }

    public URI[] getRecentInputURIs(int unpinnedSize) {
        if (unpinnedSize < 0)
            unpinnedSize = MAX_UNPINNED_SIZE;

        unpinnedSize = Math.max(0,
                Math.min(unpinnedSize, unpinnedInputURIs.size()));
        ArrayList<URI> recentInputURIs = new ArrayList<URI>();
        recentInputURIs.addAll(pinnedInputURIs);
        recentInputURIs.addAll(unpinnedInputURIs.subList(0, unpinnedSize));
        return recentInputURIs.toArray(new URI[recentInputURIs.size()]);
    }

    public URI[] getAllInputURIs() {
        int unpinnedSize = Math.max(0,
                Math.min(MAX_UNPINNED_SIZE, unpinnedInputURIs.size()));
        List<URI> allInputURIs = new ArrayList<URI>();
        allInputURIs.addAll(pinnedInputURIs);
        allInputURIs.addAll(unpinnedInputURIs.subList(0, unpinnedSize));
        return allInputURIs.toArray(new URI[unpinnedSize]);
    }

    public URI[] getPinnedInputURIs() {
        return pinnedInputURIs.toArray(new URI[pinnedInputURIs.size()]);
    }

    public URI[] getUnpinnedInputURIs(int unpinnedSize) {
        if (unpinnedSize < 0)
            unpinnedSize = MAX_UNPINNED_SIZE;

        unpinnedSize = Math.max(0,
                Math.min(unpinnedSize, unpinnedInputURIs.size()));
        return unpinnedInputURIs.subList(0, unpinnedSize)
                .toArray(new URI[unpinnedSize]);
    }

    public void add(URI inputURI) {
        if (inputURI == null)
            return;

        this.add(inputURI, new EditorHistoryItem(inputURI.getScheme(),
                System.currentTimeMillis()));

        boolean pinned = pinnedInputURIs.contains(inputURI);

        remove(inputURI);

        if (pinned) {
            pinnedInputURIs.add(0, inputURI);
        } else {
            unpinnedInputURIs.add(0, inputURI);
            while (unpinnedInputURIs.size() > MAX_UNPINNED_SIZE) {
                unpinnedInputURIs.remove(unpinnedInputURIs.size() - 1);
            }
        }
        fireChanged();
    }

    public void remove(URI inputURI) {
        if (inputURI == null)
            return;

        int oldPinnedSize = pinnedInputURIs.size();
        Iterator<URI> iter = pinnedInputURIs.iterator();
        while (iter.hasNext()) {
            URI oldURI = iter.next();
            if (inputURI.equals(oldURI)) {
                iter.remove();
            }
        }

        int oldUnpinnedSize = unpinnedInputURIs.size();
        Iterator<URI> unpinnedIterator = unpinnedInputURIs.iterator();
        while (unpinnedIterator.hasNext()) {
            URI oldURI = unpinnedIterator.next();
            if (inputURI.equals(oldURI)) {
                unpinnedIterator.remove();
            }
        }

        //REMOVE THUMBNAIL
        removeThumbnail(inputURI);

        removeEditorHistoryItem(inputURI);

        if (oldPinnedSize != pinnedInputURIs.size()
                || oldUnpinnedSize != unpinnedInputURIs.size()) {
            fireChanged();
        }
    }

    private void removeThumbnail(URI inputURI) {
        URI thumbnailURI = inputToThumbnail.get(inputURI);
        if (thumbnailURI != null) {
            File existedThumbnailFile = new File(thumbnailURI);
            if (existedThumbnailFile.exists()) {
                existedThumbnailFile.delete();
            }
        }
        inputToThumbnail.remove(inputURI);
    }

    public void clear() {
        int oldSize = unpinnedInputURIs.size();
        for (URI unpinnedInputURI : unpinnedInputURIs) {
            removeThumbnail(unpinnedInputURI);
            removeEditorHistoryItem(unpinnedInputURI);
        }
        unpinnedInputURIs.clear();
        if (oldSize != unpinnedInputURIs.size()) {
            fireChanged();
        }
    }

    public InputStream loadThumbnailData(URI inputURI) throws IOException {
        URI thumbnailURI = getThumbnail(inputURI);
        if (thumbnailURI == null)
            return null;
        return thumbnailURI.toURL().openStream();
    }

    public void saveThumbnailData(URI inputURI, InputStream thumbnailData)
            throws IOException {
        //REMOVE EXPIRED THUMBNAIL
        removeThumbnail(inputURI);

        URI thumbnailURI = loader.saveThumbnail(thumbnailData);
        if (thumbnailURI == null)
            return;

        inputToThumbnail.put(inputURI, thumbnailURI);

        fireChanged();
    }

    public URI getThumbnail(URI inputURI) {
        return inputToThumbnail.get(inputURI);
    }

    @Override
    public void add(URI inputURI, IEditorHistoryItem item) {
        if (inputURI == null)
            return;

        boolean pinned = pinnedInputURIs.contains(inputURI);
        remove(inputURI);

        if (pinned) {
            pinnedInputURIs.add(0, inputURI);
        } else {
            unpinnedInputURIs.add(0, inputURI);
            while (unpinnedInputURIs.size() > MAX_UNPINNED_SIZE) {
                unpinnedInputURIs.remove(unpinnedInputURIs.size() - 1);
            }
        }

        removeEditorHistoryItem(inputURI);
        editorHistoryItems.put(inputURI, item);

        fireChanged();
    }

    private void removeEditorHistoryItem(URI inputURI) {
        // do remove EditorHistoryItem by uri.
        IEditorHistoryItem historyItemUri = editorHistoryItems.get(inputURI);
        if (historyItemUri != null) {
            historyItemUri = null;
        }
        editorHistoryItems.remove(inputURI);
    }

    @Override
    public IEditorHistoryItem getItem(URI inputURI) {
        return editorHistoryItems.get(inputURI);
    }

    public void pin(URI inputURI) {
        unpinnedInputURIs.remove(inputURI);
        pinnedInputURIs.remove(inputURI);
        pinnedInputURIs.add(0, inputURI);
        fireChanged();
    }

    public void unPin(URI inputURI) {
        pinnedInputURIs.remove(inputURI);
        unpinnedInputURIs.remove(inputURI);
        unpinnedInputURIs.add(0, inputURI);
        fireChanged();
    }

    public boolean isPinned(URI inputURI) {
        return pinnedInputURIs.contains(inputURI);
    }

    public void removeEditorHistoryListener(IEditorHistoryListener listener) {
        listeners.remove(listener);
    }

    public void addEditorHistoryListener(IEditorHistoryListener listener) {
        listeners.add(listener);
    }

    private void fireChanged() {
        for (final Object listener : listeners.getListeners()) {
            SafeRunner.run(new SafeRunnable() {
                public void run() throws Exception {
                    ((IEditorHistoryListener) listener).editorHistoryChanged();
                }
            });
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.services.IDisposable#dispose()
     */
    @Override
    public void dispose() {
        loader.dispose();
    }

}
