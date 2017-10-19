package org.xmind.ui.editor;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * This class is responsible for maintaining a list of unique editor input URIs.
 * <p>
 * Input URIs in an editor history list can be <em>pinned</em> so that it will
 * always stay in the list. Unpinned input URIs will automatically be evicted in
 * the FIFO manner when the count of unpinned ones exceeds
 * {@link #MAX_UNPINNED_SIZE}.
 * </p>
 * <p>
 * <b>NOTE</b> that methods of this class are <b>NOT</b> thread-safe. Undefined
 * behavior may apply when any two methods are called simultaneously.
 * </p>
 * 
 * @author Frank Shaka
 * @since 3.6
 */
public interface IEditorHistory {

    /**
     * The maximum number of unpinned input URIs an editor history list can
     * maintain (value=<code>100</code>).
     */
    int MAX_UNPINNED_SIZE = 100;

    /**
     * A listener for editor history change events.
     * 
     * @author Frank Shaka
     * @since 3.6
     */
    interface IEditorHistoryListener {

        /**
         * Called when the contents of this editor history list has changed.
         */
        void editorHistoryChanged();

    }

    /**
     * Returns a list of input URIs recently added. The result consists of
     * most-recently-added pinned input URIs, least-recently-added pinned input
     * URIs, most-recently-added unpinned input URIs and least-recently-added
     * unpinned input URIs, in the respective order. No more than
     * <code>unpinnedSize</code> unpinned input URIs, or <em>all</em> unpinned
     * input URIs if <code>unpinnedSize</code> is negative, will be included in
     * the result.
     * 
     * @param unpinnedSize
     *            a positive number or zero to specify how many unpinned input
     *            URIs at most are desired, or a negative number for all
     *            unpinned input URIs
     * @return an array or recent input URIs, or an empty array (never
     *         <code>null</code>)
     */
    URI[] getRecentInputURIs(int unpinnedSize);

    /**
     * Returns all input URIs currently maintained by this editor history. The
     * result consists of most-recently-added pinned input URIs,
     * least-recently-added pinned input URIs, most-recently-added unpinned
     * input URIs and least-recently-added unpinned input URIs, in the
     * respective order. All pinned and unpinned input URIs are returned.
     * 
     * @return an array of all input URIs, or an empty array (never
     *         <code>null</code>)
     */
    URI[] getAllInputURIs();

    /**
     * Returns all pinned input URIs.
     * 
     * @return an array of all pinned input URIs, or an empty array (never
     *         <code>null</code>)
     */
    URI[] getPinnedInputURIs();

    /**
     * Returns unpinned input URIs recently added. No more than
     * <code>unpinnedSize</code> unpinned input URIs, or <em>all</em> unpinned
     * input URIs if <code>unpinnedSize</code> is negative, will be included in
     * the result.
     * 
     * @param unpinnedSize
     *            a positive number or zero to specify how many unpinned input
     *            URIs at most are desired, or a negative number for all
     *            unpinned input URIs
     * @return an array or recent input URIs, or an empty array (never
     *         <code>null</code>)
     */
    URI[] getUnpinnedInputURIs(int unpinnedSize);

    /**
     * Adds an input URI to this editor history list. If this input URI already
     * exists in the list, this pinned/unpinned one will be moved ahead of all
     * other pinned/unpinned ones, respectively. Otherwise, the new input URI
     * will be inserted ahead of all other unpinned ones.
     * <p>
     * Note that old input URIs may be evicted during the process of this
     * operation if the size of the unpinned ones exceed
     * {@link #MAX_UNPINNED_SIZE}.
     * </p>
     * 
     * @param inputURI
     *            the input URI to add
     */
    void add(URI inputURI);

    void add(URI uri, IEditorHistoryItem item);

    IEditorHistoryItem getItem(URI inputURI);

    /**
     * Removes an input URI from this editor history list and deletes all its
     * attached information.
     * 
     * @param inputURI
     *            the input URI to remove
     */
    void remove(URI inputURI);

    /**
     * Removes all <em>unpinned</em> input URIs from this editor history list.
     */
    void clear();

    /**
     * Opens a new input stream for the thumbnail image data of a corresponding
     * input URI. The client <b>must</b> close the returned stream in case of
     * resource leak.
     * 
     * @param inputURI
     *            the input URI of which thumbnail image data is being read
     * @return a new input stream to read the thumbnail image data of the
     *         specified input URI, or <code>null</code> if no thumbnail image
     *         has been saved for this input URI
     * @throws IOException
     *             if any I/O exception occurs during opening thumbnail image
     *             data stream
     */
    InputStream loadThumbnailData(URI inputURI) throws IOException;

    /**
     * Writes thumbnail image data of a corresponding input URI from specified
     * input stream. The client <em>must</em> close the given stream after this
     * method returns.
     * <p>
     * Note that calling this method blocks the current thead.
     * </p>
     * 
     * @param inputURI
     *            the input URI of which thumbnail image data is being written
     * @param thumbnailData
     *            an input stream for the thumbnail image data of the specified
     *            input URI
     * @throws IOException
     *             if any I/O exception occurs during saving thumbnail image
     *             data
     */
    void saveThumbnailData(URI inputURI, InputStream thumbnailData)
            throws IOException;

    /**
     * Marks an input URI as <em>pinned</em>. Pinned input URIs will always stay
     * in this editor history list and appears ahead of unpinned input URIs.
     * 
     * @param inputURI
     *            the input URI to mark
     * @see #unPin(URI)
     * @see #isPinned(URI)
     */
    void pin(URI inputURI);

    /**
     * Marks an input URI as <em>unpinned</em>. Unpinned input URIs will be
     * automatically evicted if size limit is reached.
     * 
     * @param inputURI
     *            the input URI to mark
     * @see #pin(URI)
     * @see #isPinned(URI)
     */
    void unPin(URI inputURI);

    /**
     * Checks whether an input URI is pinned or not.
     * 
     * @param inputURI
     *            the input URI to check
     * @return <code>true</code> if the specified input URI is pinned, or
     *         <code>false</code> otherwise
     * @see #pin(URI)
     * @see #unPin(URI)
     */
    boolean isPinned(URI inputURI);

    /**
     * Adds a listener to track editor history changes.
     * 
     * @param listener
     *            the listener to add
     */
    void removeEditorHistoryListener(IEditorHistoryListener listener);

    /**
     * Removes a listener to stop tracking editor history changes.
     * 
     * @param listener
     *            the listener to remove
     */
    void addEditorHistoryListener(IEditorHistoryListener listener);

}
