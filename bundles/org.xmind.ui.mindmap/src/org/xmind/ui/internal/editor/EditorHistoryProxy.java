package org.xmind.ui.internal.editor;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.eclipse.ui.services.IDisposable;
import org.xmind.ui.editor.IEditorHistory;
import org.xmind.ui.editor.IEditorHistoryItem;

/**
 * @author Frank Shaka
 * @since 3.6.50
 */
public class EditorHistoryProxy implements IEditorHistory, IDisposable {

    private IEditorHistory history;

    public EditorHistoryProxy(IEditorHistory history) {
        this.history = history;
    }

    public URI[] getRecentInputURIs(int unpinnedSize) {
        return this.history.getRecentInputURIs(unpinnedSize);
    }

    public URI[] getAllInputURIs() {
        return this.history.getAllInputURIs();
    }

    public URI[] getPinnedInputURIs() {
        return this.history.getPinnedInputURIs();
    }

    public URI[] getUnpinnedInputURIs(int unpinnedSize) {
        return this.history.getUnpinnedInputURIs(unpinnedSize);
    }

    public void add(URI inputURI) {
        this.history.add(inputURI);
    }

    public void remove(URI inputURI) {
        this.history.remove(inputURI);
    }

    public void clear() {
        this.history.clear();
    }

    public InputStream loadThumbnailData(URI inputURI) throws IOException {
        return this.history.loadThumbnailData(inputURI);
    }

    public void saveThumbnailData(URI inputURI, InputStream thumbnailData)
            throws IOException {
        this.history.saveThumbnailData(inputURI, thumbnailData);
    }

    @Override
    public void add(URI uri, IEditorHistoryItem item) {
        history.add(uri, item);
    }

    @Override
    public IEditorHistoryItem getItem(URI inputURI) {
        return history.getItem(inputURI);
    }

    public void pin(URI inputURI) {
        this.history.pin(inputURI);
    }

    public void unPin(URI inputURI) {
        this.history.unPin(inputURI);
    }

    public boolean isPinned(URI inputURI) {
        return this.history.isPinned(inputURI);
    }

    public void removeEditorHistoryListener(IEditorHistoryListener listener) {
        this.history.removeEditorHistoryListener(listener);
    }

    public void addEditorHistoryListener(IEditorHistoryListener listener) {
        this.history.addEditorHistoryListener(listener);
    }

    public void dispose() {
        this.history = null;
    }

}
