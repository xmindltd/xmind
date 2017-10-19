/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
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
import org.xmind.core.IEncryptionHandler;
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

    private IEncryptionHandler defaultEncryptionHandler = null;

    public String creatorName;

    public String creatorVersion;

    public synchronized void setDefaultEncryptionHandler(
            IEncryptionHandler encryptionHandler) {
        if (this.defaultEncryptionHandler != null)
            return;

        this.defaultEncryptionHandler = encryptionHandler;
    }

    public synchronized void setCreator(String name, String version) {
        this.creatorName = name;
        this.creatorVersion = version;
    }

    protected IEncryptionHandler getDefaultEncryptionHandler() {
        return this.defaultEncryptionHandler;
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
                getDefaultEncryptionHandler());
    }

    public IWorkbook loadFromPath(String path,
            IEncryptionHandler encryptionHandler)
                    throws IOException, CoreException {
        return loadFromPath(path, new ByteArrayStorage(), encryptionHandler);
    }

    public IWorkbook loadFromPath(String path, IStorage storage,
            IEncryptionHandler encryptionHandler)
                    throws IOException, CoreException {
        if (path == null)
            throw new IllegalArgumentException("Path is null"); //$NON-NLS-1$
        return doLoadFromPath(path, storage, encryptionHandler);
    }

    public IWorkbook loadFromFile(File file) throws IOException, CoreException {
        return loadFromFile(file, new ByteArrayStorage(),
                getDefaultEncryptionHandler());
    }

    public IWorkbook loadFromFile(File file,
            IEncryptionHandler encryptionHandler)
                    throws IOException, CoreException {
        return loadFromFile(file, new ByteArrayStorage(), encryptionHandler);
    }

    public IWorkbook loadFromFile(File file, IStorage storage,
            IEncryptionHandler encryptionHandler)
                    throws IOException, CoreException {
        if (file == null)
            throw new IllegalArgumentException("File is null"); //$NON-NLS-1$
        if (!file.exists())
            throw new FileNotFoundException("File not exists: " + file); //$NON-NLS-1$

        if (file.isDirectory()) {
            return doLoadFromDirectory(file, storage, encryptionHandler);
        }

        if (!file.canRead())
            throw new IOException("File can't be read: " + file); //$NON-NLS-1$

        return doLoadFromFile(file, storage, encryptionHandler);
    }

    public IWorkbook loadFromStream(InputStream in)
            throws IOException, CoreException {
        return loadFromStream(in, new ByteArrayStorage(),
                getDefaultEncryptionHandler());
    }

    public IWorkbook loadFromStream(InputStream in, IStorage storage)
            throws IOException, CoreException {
        return loadFromStream(in, storage, getDefaultEncryptionHandler());
    }

    public IWorkbook loadFromStream(InputStream in, IStorage storage,
            IEncryptionHandler encryptionHandler)
                    throws IOException, CoreException {
        if (in == null)
            throw new IllegalArgumentException("Input stream is null"); //$NON-NLS-1$
        return doLoadFromStream(in, storage, encryptionHandler);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.core.IWorkbookBuilder#loadFromInputSource(org.xmind.core.io
     * .IInputSource)
     */
    public IWorkbook loadFromInputSource(IInputSource source)
            throws IOException, CoreException {
        return loadFromInputSource(source, new ByteArrayStorage(),
                getDefaultEncryptionHandler());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.core.IWorkbookBuilder#loadFromInputSource(org.xmind.core.io
     * .IInputSource, org.xmind.core.IEncryptionHandler)
     */
    public IWorkbook loadFromInputSource(IInputSource source,
            IEncryptionHandler encryptionHandler)
                    throws IOException, CoreException {
        return loadFromInputSource(source, new ByteArrayStorage(),
                encryptionHandler);
    }

    /**
     * 
     * @param source
     * @param storage
     * @param encryptionHandler
     * @return
     * @throws IOException
     * @throws CoreException
     */
    public IWorkbook loadFromInputSource(IInputSource source, IStorage storage,
            IEncryptionHandler encryptionHandler)
                    throws IOException, CoreException {
        if (source == null)
            throw new IllegalArgumentException("Input source is null"); //$NON-NLS-1$
        return doLoadFromInputSource(source, storage, encryptionHandler);
    }

    /*
     * (non-Javadoc)
     * 
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
            IEncryptionHandler encryptionHandler)
                    throws IOException, CoreException {
        if (storage == null)
            throw new IllegalArgumentException("Storage is null"); //$NON-NLS-1$
        return doLoadFromStorage(storage, encryptionHandler);
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
            IEncryptionHandler encryptionHandler)
                    throws IOException, CoreException {
        return loadFromFile(new File(path), storage, encryptionHandler);
    }

    protected IWorkbook doLoadFromDirectory(File dir, IStorage storage,
            IEncryptionHandler encryptionHandler)
                    throws IOException, CoreException {
        return loadFromInputSource(new DirectoryInputSource(dir), storage,
                encryptionHandler);
    }

    protected IWorkbook doLoadFromFile(File file, IStorage storage,
            IEncryptionHandler encryptionHandler)
                    throws IOException, CoreException, FileNotFoundException {
        return loadFromStream(new FileInputStream(file), storage,
                encryptionHandler);
    }

    protected IWorkbook doLoadFromStream(InputStream in, IStorage storage,
            IEncryptionHandler encryptionHandler)
                    throws IOException, CoreException {
        if (storage == null)
            storage = new ByteArrayStorage();
        try {
            extractFromStream(in, storage.getOutputTarget());
        } finally {
            in.close();
        }
        return doLoadFromStorage(storage, encryptionHandler);
    }

    protected IWorkbook doLoadFromInputSource(IInputSource source,
            IStorage storage, IEncryptionHandler encryptionHandler)
                    throws IOException, CoreException {
        if (storage == null)
            storage = new ByteArrayStorage();
        FileUtils.transfer(source, storage.getOutputTarget());
        return doLoadFromStorage(storage, encryptionHandler);
    }

    protected abstract void extractFromStream(InputStream input,
            IOutputTarget target) throws IOException, CoreException;

    protected abstract IWorkbook doLoadFromStorage(IStorage storage,
            IEncryptionHandler encryptionHandler)
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
            IEncryptionHandler encryptionHandler)
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
        return loadFromStream(in, new DirectoryStorage(dir), encryptionHandler);
    }

}
