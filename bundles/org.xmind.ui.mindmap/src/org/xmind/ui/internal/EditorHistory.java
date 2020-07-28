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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.xmind.core.util.FileUtils;
import org.xmind.ui.IEditorHistory;

/**
 * 
 * @author Frank Shaka
 * @deprecated
 */
@Deprecated
public class EditorHistory implements IEditorHistory {

    private static final String THUMBNAIL_DIR_NAME = "thumbnailHistory"; //$NON-NLS-1$

    private static final String PIN = "favor"; //$NON-NLS-1$

    protected static final int MAX_SIZE = 100;

    private List<URI> inputURIs = new ArrayList<URI>();

    private Map<URI, String> inputToInfo = new HashMap<URI, String>();

    private ListenerList listeners = new ListenerList();

    protected EditorHistory() {
        List<String> historyRepository = EditorHistoryPersistenceService.load();
        for (String inputAndInfo : historyRepository) {
            int index = inputAndInfo
                    .indexOf(EditorHistoryPersistenceService.VALUE_SEPARATOR);
            if (index < 0)
                continue;

            String input = inputAndInfo.substring(0, index);
            String info = inputAndInfo.substring(index
                    + EditorHistoryPersistenceService.VALUE_SEPARATOR.length());

            try {
                URI inputURI = new URI(input);
                inputURIs.add(inputURI);
                inputToInfo.put(inputURI, info);
            } catch (URISyntaxException e) {
            }

        }

    }

    public synchronized URI[] getAllInputURIs() {
        return inputURIs.toArray(new URI[inputURIs.size()]);
    }

    public synchronized URI[] getRecentInputURIs(int size) {
        size = Math.max(0, Math.min(size, inputURIs.size()));
        return inputURIs.subList(0, size).toArray(new URI[size]);
    }

    public void pin(URI inputURI) {
        if (inputURI == null)
            return;

        boolean isPin = isPin(inputURI);
        String info = getInfo(inputURI);
        if (!isPin) {
            inputToInfo.put(inputURI, appendPinContent(info));
        }
        add(inputURI);
    }

    public void unPin(URI inputURI) {
        if (inputURI == null)
            return;

        boolean isPin = isPin(inputURI);
        String thumbnail = getThumbnail(inputURI);
        if (isPin) {
            inputToInfo.put(inputURI, thumbnail);
        }
        fireChanged();

    }

    public boolean isPin(URI inputURI) {
        boolean isPin = false;
        if (inputURI == null)
            return isPin;

        String info = inputToInfo.get(inputURI);
        if (info != null) {
            isPin = info.contains(PIN);
        }
        return isPin;
    }

    public void add(URI inputURI) {
        if (inputURI == null)
            return;

        remove(inputURI);
        inputURIs.add(0, inputURI);
        while (inputURIs.size() > MAX_SIZE) {
            inputURIs.remove(inputURIs.size() - 1);
        }

        fireChanged();
    }

    public String getInfo(URI inputURI) {
        if (inputURI == null)
            return null;
        return inputToInfo.get(inputURI);
    }

    public String getThumbnail(URI inputURI) {
        if (inputURI == null)
            return null;
        String info = inputToInfo.get(inputURI);
        String thumbnail = info;
        if (info != null) {
            int sepPos = info.indexOf(',');
            if (sepPos > 0) {
                thumbnail = info.substring(0, sepPos);
            }
        }
        return thumbnail;
    }

    public void addThumbnail(URI inputURI, String originThumbnailPath) {
        if (inputURI == null)
            return;

        File originThumbnailFile = new File(originThumbnailPath);
        if (!originThumbnailFile.exists())
            return;

        String thumbnailName = UUID.randomUUID().toString()
                + originThumbnailFile.getName();
        File thumbnailDir = getThumbnailDir();
        if (thumbnailDir == null)
            return;
        if (!thumbnailDir.exists()) {
            thumbnailDir.mkdirs();
        }

        String thumbnailPath = thumbnailDir.getAbsolutePath() + File.separator
                + thumbnailName;
        File thumbnailFile = new File(thumbnailPath);

        boolean isPin = isPin(inputURI);

        //remove existed thumbnail 
        removeThumbnail(inputURI);

        inputToInfo.put(inputURI,
                isPin ? appendPinContent(thumbnailPath) : thumbnailPath);

        saveThumbnailFile(originThumbnailFile, thumbnailFile);

        while (inputToInfo.values().size() > MAX_SIZE) {
            removeThumbnail(inputURI);
        }
        fireChanged();
    }

    private String appendPinContent(String source) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(source);
        buffer.append(","); //$NON-NLS-1$
        buffer.append(PIN);
        return buffer.toString();
    }

    private File getThumbnailDir() {
        IPath basePath = MindMapUIPlugin.getDefault().getStateLocation();
        if (basePath == null)
            return null;
        IPath filePath = basePath.append(THUMBNAIL_DIR_NAME);
        return filePath.toFile();
    }

    private void saveThumbnailFile(File source, File targetFile) {
        try {
            FileUtils.transfer(new FileInputStream(source),
                    new FileOutputStream(targetFile), true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeThumbnail(URI inputURI) {
        if (inputURI == null)
            return;

        String existedThumbnail = getThumbnail(inputURI);
        if (existedThumbnail == null)
            return;
        inputToInfo.remove(inputURI);
        File existedThumbnailFile = new File(existedThumbnail);
        if (existedThumbnailFile.exists())
            existedThumbnailFile.delete();

    }

    public synchronized void remove(URI inputURI) {
        if (inputURI == null)
            return;

        int oldSize = inputURIs.size();
        Iterator<URI> iter = inputURIs.iterator();
        while (iter.hasNext()) {
            URI oldURI = iter.next();
            if (inputURI.equals(oldURI)) {
                iter.remove();
            }
        }
        if (oldSize != inputURIs.size()) {
            fireChanged();
        }
    }

    public synchronized void clear() {
        int oldSize = inputURIs.size();
        inputURIs.clear();
        if (oldSize != inputURIs.size()) {
            fireChanged();
        }
        //REMOVE THUMBNAILS
        inputToInfo.clear();
        File thumbnailDir = getThumbnailDir();
        if (thumbnailDir != null && thumbnailDir.exists()) {
            File[] listFiles = thumbnailDir.listFiles();
            for (File file : listFiles) {
                if (file.exists())
                    file.delete();
            }
        }

    }

    public void addEditorHistoryListener(IEditorHistoryListener listener) {
        listeners.add(listener);
    }

    public void removeEditorHistoryListener(IEditorHistoryListener listener) {
        listeners.remove(listener);
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
}
