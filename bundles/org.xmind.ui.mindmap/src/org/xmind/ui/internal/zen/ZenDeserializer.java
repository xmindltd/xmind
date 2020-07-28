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
package org.xmind.ui.internal.zen;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipInputStream;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xmind.core.Core;
import org.xmind.core.CoreException;
import org.xmind.core.IDeserializer;
import org.xmind.core.IFileEntry;
import org.xmind.core.IManifest;
import org.xmind.core.ISheet;
import org.xmind.core.IWorkbook;
import org.xmind.core.internal.AbstractSerializingBase;
import org.xmind.core.internal.dom.ManifestImpl;
import org.xmind.core.internal.dom.SheetImpl;
import org.xmind.core.internal.dom.WorkbookImpl;
import org.xmind.core.io.CoreIOException;
import org.xmind.core.io.IInputSource;
import org.xmind.core.io.IStorage;
import org.xmind.core.io.InvalidChecksumException;
import org.xmind.core.util.DOMUtils;
import org.xmind.core.util.FileUtils;
import org.xmind.core.util.IProgressReporter;

public class ZenDeserializer extends AbstractSerializingBase
        implements IDeserializer {

    private IWorkbook workbook;

    private IStorage storage;

    private IInputSource inputSource;

    private InputStream inputStream;

    private boolean usesWorkbookStorageAsInputSource;

    private IManifest manifest;

    private final Map<String, JSONObject> loadedJsons;

    private final Map<String, JSONArray> loadedJsonArrays;

    public ZenDeserializer(IStorage storage) {
        this.workbook = new WorkbookImpl(DOMUtils.createDocument(), storage);
        this.storage = storage;
        this.inputSource = null;
        this.inputStream = null;
        this.usesWorkbookStorageAsInputSource = false;
        this.manifest = null;
        this.loadedJsons = new HashMap<String, JSONObject>();
        this.loadedJsonArrays = new HashMap<String, JSONArray>();
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.core.IDeserializer#getWorkbook()
     */
    public IWorkbook getWorkbook() {
        return workbook;
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.core.IDeserializer#setWorkbookStorage(org.xmind.core.io.
     * IStorage)
     */
    public void setWorkbookStorage(IStorage storage) {
        if (storage == null)
            throw new IllegalArgumentException("storage is null"); //$NON-NLS-1$
        this.storage = storage;
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.core.IDeserializer#getWorkbookStorage()
     */
    public IStorage getWorkbookStorage() {
        return this.storage;
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.core.IDeserializer#setInputSource(org.xmind.core.io.
     * IInputSource)
     */
    public void setInputSource(IInputSource source) {
        if (source == null)
            throw new IllegalArgumentException("input source is null"); //$NON-NLS-1$
        this.inputSource = source;
        this.inputStream = null;
        this.usesWorkbookStorageAsInputSource = false;
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.core.IDeserializer#setInputStream(java.io.InputStream)
     */
    public void setInputStream(InputStream stream) {
        if (stream == null)
            throw new IllegalArgumentException("input stream is null"); //$NON-NLS-1$
        this.inputStream = stream;
        this.inputSource = null;
        this.usesWorkbookStorageAsInputSource = false;
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.core.IDeserializer#setWorkbookStorageAsInputSource()
     */
    public void setWorkbookStorageAsInputSource() {
        this.usesWorkbookStorageAsInputSource = true;
        this.inputSource = null;
        this.inputStream = null;
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.core.IDeserializer#hasInputSource()
     */
    public boolean hasInputSource() {
        return inputSource != null || inputStream != null
                || usesWorkbookStorageAsInputSource;
    }

    public IManifest getManifest() {
        return manifest;
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.core.IDeserializer#deserialize(org.xmind.core.util.
     * IProgressReporter)
     */
    public void deserialize(IProgressReporter reporter)
            throws IOException, CoreException, IllegalStateException {
        if (manifest == null)
            deserializeManifest(reporter);

        try {
            loadWorkbook();
            purgeStorage();
        } catch (InvalidChecksumException e) {
            throw new CoreException(Core.ERROR_WRONG_PASSWORD, e);
        } catch (CoreIOException e) {
            CoreException ce = e.getCoreException();
            throw new CoreException(ce.getType(), ce.getCodeInfo(), e);
        }
    }

    public void deserializeManifest(IProgressReporter reporter)
            throws IOException, CoreException, IllegalStateException {
        if (inputStream != null) {
            ZipInputStream zin = new ZipInputStream(inputStream);
            try {
                FileUtils.extractZipFile(zin, storage.getOutputTarget());
            } finally {
                zin.close();
            }
        } else if (inputSource != null) {
            FileUtils.transfer(inputSource, storage.getOutputTarget());
        } else if (!usesWorkbookStorageAsInputSource) {
            throw new IllegalStateException("no input source available"); //$NON-NLS-1$
        }

        /// load manifest.json
        JSONObject manifestJson = forceLoadJsonFromEntry(
                ZenConstants.MANIFEST_JSON);
        this.manifest = workbook.getManifest();
        new ManifestDeserializer().deserialize(manifest, manifestJson);
        ((ManifestImpl) manifest)
                .setStreamNormalizer(getEntryStreamNormalizer());
    }

    private void loadWorkbook() throws IOException, CoreException {
        /// load content.json
        JSONArray contentArray = forceLoadJsonArrayFromEntry(
                ZenConstants.CONTENT_JSON);
        new WorkbookDeserializer().deserialize(workbook, contentArray);

        //load meta.json
//        IMeta meta = workbook.getMeta();
//        JSONObject metaJson = forceLoadJsonFromEntry(ZenConstants.META_JSON);
//        new MetaDeserializer().deserialize(meta, metaJson);
//
//        if (meta.getValue(IMeta.CREATED_TIME) == null) {
//            meta.setValue(IMeta.CREATED_TIME,
//                    NumberUtils.formatDate(System.currentTimeMillis()));
//        }
//        meta.setValue(IMeta.CREATOR_NAME, getCreatorName());
//        meta.setValue(IMeta.CREATOR_VERSION, getCreatorVersion());

        /// initialize workbook content
        for (ISheet sheet : workbook.getSheets()) {
            ((SheetImpl) sheet).addNotify((WorkbookImpl) workbook);
        }
    }

    private JSONObject forceLoadJsonFromEntry(String entryPath)
            throws CoreException {
        JSONObject json;
        try {
            json = loadJsonFromEntry(entryPath);
        } catch (IOException e) {
            json = null;
        }

        if (json == null) {
            json = new JSONObject();
            loadedJsons.put(entryPath, json);
        }
        return json;
    }

    private JSONObject loadJsonFromEntry(String entryPath)
            throws IOException, CoreException {
        JSONObject cache = loadedJsons.get(entryPath);
        if (cache != null)
            return cache;

        InputStream stream = openEntryInputStream(entryPath);
        if (stream == null)
            return null;

        JSONObject json;
        try {
            json = loadJsonFromStream(stream);
        } catch (IOException e) {
            if (hasEncryptionData(entryPath)) {
                throw new CoreException(Core.ERROR_WRONG_PASSWORD, e);
            }
            throw e;
        } catch (RuntimeException e) {
            /// catching any runtime exception during json parsing
            if (hasEncryptionData(entryPath)) {
                throw new CoreException(Core.ERROR_WRONG_PASSWORD, e);
            }
            throw e;
        } catch (Error e) {
            /// catching any error during json parsing
            if (hasEncryptionData(entryPath)) {
                throw new CoreException(Core.ERROR_WRONG_PASSWORD, e);
            }
            throw e;
        } finally {
            if (stream != null)
                stream.close();
        }

        if (json != null) {
            loadedJsons.put(entryPath, json);
        }
        return json;
    }

    private InputStream openEntryInputStream(String entryPath)
            throws IOException, CoreException {
        if (manifest == null && storage == null)
            throw new IllegalStateException(
                    "No manifest or input source available"); //$NON-NLS-1$

        if (manifest != null) {
            IFileEntry entry = manifest.getFileEntry(entryPath);
            if (entry != null) {
                if (!entry.canRead())
                    return null;
                return entry.openInputStream();
            }
        }

        if (storage != null) {
            IInputSource source = storage.getInputSource();
            if (source != null && source.hasEntry(entryPath)
                    && source.isEntryAvailable(entryPath)) {
                return source.openEntryStream(entryPath);
            }
        }

        return null;
    }

    private JSONObject loadJsonFromStream(InputStream stream)
            throws IOException {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(stream, "utf-8")); //$NON-NLS-1$
            StringBuilder sb = new StringBuilder();
            String read;
            while ((read = reader.readLine()) != null) {
                sb.append(read).append('\n');
            }

            JSONObject json = new JSONObject(sb.toString());
            return json;
        } finally {
            reader.close();
        }
    }

    private boolean hasEncryptionData(String entryPath) {
        if (manifest != null) {
            IFileEntry entry = manifest.getFileEntry(entryPath);
            return entry != null && entry.getEncryptionData() != null;
        }
        return false;
    }

    /// load json array
    private JSONArray forceLoadJsonArrayFromEntry(String entryPath)
            throws CoreException {
        JSONArray json;
        try {
            json = loadJsonArrayFromEntry(entryPath);
        } catch (IOException e) {
            json = null;
        }

        if (json == null) {
            json = new JSONArray();
            loadedJsonArrays.put(entryPath, json);
        }
        return json;
    }

    private JSONArray loadJsonArrayFromEntry(String entryPath)
            throws IOException, CoreException {
        JSONArray cache = loadedJsonArrays.get(entryPath);
        if (cache != null)
            return cache;

        InputStream stream = openEntryInputStream(entryPath);
        if (stream == null)
            return null;

        JSONArray json;
        try {
            json = loadJsonArrayFromStream(stream);
        } catch (IOException e) {
            if (hasEncryptionData(entryPath)) {
                throw new CoreException(Core.ERROR_WRONG_PASSWORD, e);
            }
            throw e;
        } catch (RuntimeException e) {
            /// catching any runtime exception during json parsing
            if (hasEncryptionData(entryPath)) {
                throw new CoreException(Core.ERROR_WRONG_PASSWORD, e);
            }
            throw e;
        } catch (Error e) {
            /// catching any error during json parsing
            if (hasEncryptionData(entryPath)) {
                throw new CoreException(Core.ERROR_WRONG_PASSWORD, e);
            }
            throw e;
        } finally {
            if (stream != null)
                stream.close();
        }

        if (json != null) {
            loadedJsonArrays.put(entryPath, json);
        }
        return json;
    }

    private JSONArray loadJsonArrayFromStream(InputStream stream)
            throws IOException {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(stream, "utf-8")); //$NON-NLS-1$
            StringBuilder sb = new StringBuilder();
            String read;
            while ((read = reader.readLine()) != null) {
                sb.append(read).append('\n');
            }

            JSONArray json = new JSONArray(sb.toString());
            return json;
        } finally {
            reader.close();
        }
    }

    // only delete top-level's *.json files.
    private void purgeStorage() {
        String rootPath = storage.getFullPath();
        File[] children = new File(rootPath).listFiles();
        if (children != null) {
            for (File child : children) {
                if (child.getName().endsWith(".json")) { //$NON-NLS-1$
                    child.delete();
                }
            }
        }
    }

}
