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
package org.xmind.ui.internal.editor;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.xmind.ui.editor.IEditorHistoryItem;

/**
 * @author Frank Shaka
 */
public interface IEditorHistoryLoader {

    public interface IEditorHistoryLoaderCallback {

        void inputURILoaded(URI inputURI);

        void pinnedInputURILoaded(URI inputURI);

        void thumbnailURILoaded(URI inputURI, URI thumbnailURI);

        void editorHistoryItemsLoaded(URI inputURI, IEditorHistoryItem item);

    }

    void load(IEditorHistoryLoaderCallback callback);

    URI saveThumbnail(InputStream thumbnailData) throws IOException;

    void dispose();

}
