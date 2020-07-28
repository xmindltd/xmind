/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
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
package org.xmind.core.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.xmind.core.CoreException;
import org.xmind.core.IEntryStreamNormalizer;
import org.xmind.core.IWorkbook;
import org.xmind.core.IWorkbookBuilder;
import org.xmind.core.io.ByteArrayStorage;
import org.xmind.core.io.DirectoryInputSource;
import org.xmind.core.io.DirectoryStorage;
import org.xmind.core.io.IInputSource;
import org.xmind.core.io.IOutputTarget;
import org.xmind.core.io.IStorage;
import org.xmind.core.util.FileUtils;

@SuppressWarnings("deprecation")
public abstract class AbstractWorkbookBuilder implements IWorkbookBuilder {

    public String creatorName;

    public String creatorVersion;

    private IEntryStreamNormalizer normalizer;

    public synchronized void setCreator(String name, String version) {
        this.creatorName = name;
        this.creatorVersion = version;
    }

    public String getCreatorName() {
        return this.creatorName;
    }

    public String getCreatorVersion() {
        return this.creatorVersion;
    }

    public IWorkbook createWorkbook() {
        return createWorkbook(new ByteArrayStorage());
    }

    public IWorkbook createWorkbook(IStorage storage) {
        return doCreateWorkbook(storage);
    }

    public IWorkbook loadFromPath(String path)
            throws IOException, CoreException {
        return loadFromPath(path, new ByteArrayStorage(),
                getEntryStreamNormalizer());
    }

    public IWorkbook loadFromPath(String path,
            IEntryStreamNormalizer normalizer)
            throws IOException, CoreException {
        return loadFromPath(path, new ByteArrayStorage(), normalizer);
    }

    public IWorkbook loadFromPath(String path, IStorage storage,
            IEntryStreamNormalizer normalizer)
            throws IOException, CoreException {
        if (path == null)
            throw new IllegalArgumentException("Path is null"); //$NON-NLS-1$
        return doLoadFromPath(path, storage, normalizer);
    }

    public IWorkbook loadFromFile(File file) throws IOException, CoreException {
        return loadFromFile(file, new ByteArrayStorage(),
                getEntryStreamNormalizer());
    }

    public IWorkbook loadFromFile(File file, IEntryStreamNormalizer normalizer)
            throws IOException, CoreException {
        return loadFromFile(file, new ByteArrayStorage(), normalizer);
    }

    public IWorkbook loadFromFile(File file, IStorage storage,
            IEntryStreamNormalizer normalizer)
            throws IOException, CoreException {
        if (file == null)
            throw new IllegalArgumentException("File is null"); //$NON-NLS-1$
        if (!file.exists())
            throw new FileNotFoundException("File not exists: " + file); //$NON-NLS-1$

        if (file.isDirectory()) {
            return doLoadFromDirectory(file, storage, normalizer);
        }

        if (!file.canRead())
            throw new IOException("File can't be read: " + file); //$NON-NLS-1$

        return doLoadFromFile(file, storage, normalizer);
    }

    public IWorkbook loadFromStream(InputStream in)
            throws IOException, CoreException {
        return loadFromStream(in, new ByteArrayStorage(),
                getEntryStreamNormalizer());
    }

    public IWorkbook loadFromStream(InputStream in, IStorage storage)
            throws IOException, CoreException {
        return loadFromStream(in, storage, getEntryStreamNormalizer());
    }

    public IWorkbook loadFromStream(InputStream in, IStorage storage,
            IEntryStreamNormalizer normalizer)
            throws IOException, CoreException {
        if (in == null)
            throw new IllegalArgumentException("Input stream is null"); //$NON-NLS-1$
        return doLoadFromStream(in, storage, getEntryStreamNormalizer());
    }

    /*
     * (non-Javadoc)
     * @see
     * org.xmind.core.IWorkbookBuilder#loadFromInputSource(org.xmind.core.io
     * .IInputSource)
     */
    public IWorkbook loadFromInputSource(IInputSource source)
            throws IOException, CoreException {
        return loadFromInputSource(source, new ByteArrayStorage(),
                getEntryStreamNormalizer());
    }

    /*
     * (non-Javadoc)
     * @see
     * org.xmind.core.IWorkbookBuilder#loadFromInputSource(org.xmind.core.io
     * .IInputSource, org.xmind.core.IEntryStreamNormalizer)
     */
    public IWorkbook loadFromInputSource(IInputSource source,
            IEntryStreamNormalizer normalizer)
            throws IOException, CoreException {
        return loadFromInputSource(source, new ByteArrayStorage(), normalizer);
    }

    /**
     * @param source
     * @param storage
     * @param normalizer
     * @return
     * @throws IOException
     * @throws CoreException
     */
    public IWorkbook loadFromInputSource(IInputSource source, IStorage storage,
            IEntryStreamNormalizer normalizer)
            throws IOException, CoreException {
        if (source == null)
            throw new IllegalArgumentException("Input source is null"); //$NON-NLS-1$
        return doLoadFromInputSource(source, storage, normalizer);
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.core.IWorkbookBuilder#loadFromStorage(org.xmind.core.io.
     * IStorage )
     */
    public IWorkbook loadFromStorage(IStorage storage)
            throws IOException, CoreException {
        if (storage == null)
            throw new IllegalArgumentException("Storage is null"); //$NON-NLS-1$
        return loadFromStorage(storage, null);
    }

    public IWorkbook loadFromStorage(IStorage storage,
            IEntryStreamNormalizer normalizer)
            throws IOException, CoreException {
        if (storage == null)
            throw new IllegalArgumentException("Storage is null"); //$NON-NLS-1$
        return doLoadFromStorage(storage, normalizer);
    }

    @Deprecated
    public IWorkbook loadFromTempLocation(String tempLocation)
            throws IOException, CoreException {
        if (tempLocation == null)
            throw new IllegalArgumentException("Temp location is null"); //$NON-NLS-1$
        File dir = new File(tempLocation);
        if (!dir.exists())
            throw new FileNotFoundException(
                    "Temp location not found: " + tempLocation); //$NON-NLS-1$
        if (!dir.isDirectory())
            throw new FileNotFoundException(
                    "Temp location is not directory: " + tempLocation); //$NON-NLS-1$
        DirectoryStorage storage = new DirectoryStorage(dir);
//        return loadFromInputSource(storage.getInputSource(), storage, null);
        return doLoadFromStorage(storage, null);
    }

    ////////////////////////////////////////////////////////////////
    //
    // Methods That Subclasses Can Override
    //
    ////////////////////////////////////////////////////////////////

    protected abstract IWorkbook doCreateWorkbook(IStorage storage);

    protected IWorkbook doLoadFromPath(String path, IStorage storage,
            IEntryStreamNormalizer normalizer)
            throws IOException, CoreException {
        return loadFromFile(new File(path), storage, normalizer);
    }

    protected IWorkbook doLoadFromDirectory(File dir, IStorage storage,
            IEntryStreamNormalizer normalizer)
            throws IOException, CoreException {
        return loadFromInputSource(new DirectoryInputSource(dir), storage,
                normalizer);
    }

    protected IWorkbook doLoadFromFile(File file, IStorage storage,
            IEntryStreamNormalizer normalizer)
            throws IOException, CoreException, FileNotFoundException {
        return loadFromStream(new FileInputStream(file), storage, normalizer);
    }

    protected IWorkbook doLoadFromStream(InputStream in, IStorage storage,
            IEntryStreamNormalizer normalizer)
            throws IOException, CoreException {
        if (storage == null)
            storage = new ByteArrayStorage();
        try {
            extractFromStream(in, storage.getOutputTarget());
        } finally {
            in.close();
        }
        return doLoadFromStorage(storage, normalizer);
    }

    protected IWorkbook doLoadFromInputSource(IInputSource source,
            IStorage storage, IEntryStreamNormalizer normalizer)
            throws IOException, CoreException {
        if (storage == null)
            storage = new ByteArrayStorage();
        FileUtils.transfer(source, storage.getOutputTarget());
        return doLoadFromStorage(storage, normalizer);
    }

    protected abstract void extractFromStream(InputStream input,
            IOutputTarget target) throws IOException, CoreException;

    protected abstract IWorkbook doLoadFromStorage(IStorage storage,
            IEntryStreamNormalizer normalizer)
            throws IOException, CoreException;

    ////////////////////////////////////////////////////////////////
    //
    // Deprecated Methods
    //
    ////////////////////////////////////////////////////////////////

    @Deprecated
    public IWorkbook createWorkbook(String targetPath) {
        return createWorkbook(new ByteArrayStorage());
    }

    @Deprecated
    public IWorkbook createWorkbookOnTemp(String tempLocation) {
        return createWorkbook(new DirectoryStorage(new File(tempLocation)));
    }

    @Deprecated
    public IWorkbook loadFromStream(InputStream in, String tempLocation)
            throws IOException, CoreException {
        return loadFromStream(in, tempLocation, null);
    }

    @Deprecated
    public IWorkbook loadFromStream(InputStream in, String tempLocation,
            IEntryStreamNormalizer normalizer)
            throws IOException, CoreException {
        if (tempLocation == null)
            throw new IllegalArgumentException("Temp location is null"); //$NON-NLS-1$
        File dir = new File(tempLocation);
        if (!dir.exists())
            throw new FileNotFoundException(
                    "Temp location not found: " + tempLocation); //$NON-NLS-1$
        if (!dir.isDirectory())
            throw new FileNotFoundException(
                    "Temp location is not directory: " + tempLocation); //$NON-NLS-1$
        return loadFromStream(in, new DirectoryStorage(dir), normalizer);
    }

    public void setEntryStreamNormalizer(IEntryStreamNormalizer normalizer) {
        this.normalizer = normalizer;
    }

    public IEntryStreamNormalizer getEntryStreamNormalizer() {
        return normalizer;
    }

}
