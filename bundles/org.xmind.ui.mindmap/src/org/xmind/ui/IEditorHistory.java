package org.xmind.ui;

import java.net.URI;

/**
 * @deprecated
 */
@Deprecated
public interface IEditorHistory {

    public static interface IEditorHistoryListener {

        void editorHistoryChanged();

    }

    URI[] getRecentInputURIs(int size);

    URI[] getAllInputURIs();

    void add(URI inputURI);

    void remove(URI inputURI);

    String getInfo(URI inputURI);

    String getThumbnail(URI inputURI);

    void addThumbnail(URI inputURI, String originThumbnailPath);

    void removeThumbnail(URI inputURI);

    void pin(URI inputURI);

    void unPin(URI inputURI);

    boolean isPin(URI inputURI);

    void clear();

    void removeEditorHistoryListener(IEditorHistoryListener listener);

    void addEditorHistoryListener(IEditorHistoryListener listener);

}
