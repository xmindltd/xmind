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
package org.xmind.core.internal.dom;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xmind.core.Core;
import org.xmind.core.CoreException;
import org.xmind.core.IEncryptionHandler;
import org.xmind.core.IMeta;
import org.xmind.core.IWorkbook;
import org.xmind.core.internal.AbstractWorkbookBuilder;
import org.xmind.core.io.ByteArrayStorage;
import org.xmind.core.io.DirectoryStorage;
import org.xmind.core.io.IInputSource;
import org.xmind.core.io.IStorage;
import org.xmind.core.util.DOMUtils;
import org.xmind.core.util.FileUtils;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class WorkbookBuilderImpl extends AbstractWorkbookBuilder
        implements ErrorHandler {

    private IEncryptionHandler defaultEncryptionHandler = null;

//    private DocumentBuilderFactory documentBuilderFactory = null;

    public String creatorName;

    public String creatorversion;

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.core.IWorkbookBuilder#setDefaultEncryptionHandler(org.xmind
     * .core.IEncryptionHandler)
     */
    public synchronized void setDefaultEncryptionHandler(
            IEncryptionHandler encryptionHandler) {
        if (this.defaultEncryptionHandler != null)
            return;

        this.defaultEncryptionHandler = encryptionHandler;
    }

//    private synchronized DocumentBuilderFactory getDocumentBuilderFactory() {
//        if (documentBuilderFactory == null) {
//            DocumentBuilderFactory factory = DocumentBuilderFactory
//                    .newInstance();
//            factory.setAttribute(
//                    "http://apache.org/xml/features/continue-after-fatal-error", //$NON-NLS-1$
//                    Boolean.TRUE);
//            factory.setNamespaceAware(true);
//            documentBuilderFactory = factory;
//        }
//        return documentBuilderFactory;
//    }

    private DocumentBuilder getDocumentCreator() {
        try {
            return DOMUtils.getDefaultDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException(e);
        }
    }

    public DocumentBuilder getDocumentLoader() throws CoreException {
        try {
            DocumentBuilder loader = DOMUtils.getDefaultDocumentBuilder();
            loader.setErrorHandler(this);
            return loader;
        } catch (ParserConfigurationException e) {
            throw new CoreException(Core.ERROR_FAIL_ACCESS_XML_PARSER, e);
        }
    }

    public IWorkbook createWorkbook() {
        return newWorkbook(null);
    }

    public IWorkbook createWorkbook(String targetPath) {
        return newWorkbook(targetPath);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.core.IWorkbookBuilder#createWorkbook(org.xmind.core.io.IStorage
     * )
     */
    public IWorkbook createWorkbook(IStorage storage) {
        WorkbookImpl wb = newWorkbook(null);
        wb.setTempStorage(storage);
        return wb;
    }

    private WorkbookImpl newWorkbook(String file) {
        Document impl = createDocument();
        WorkbookImpl workbook = new WorkbookImpl(impl, file);
        workbook.setCreator(creatorName, creatorversion);
        IMeta meta = workbook.getMeta();
        String name = System.getProperty(DOMConstants.AUTHOR_NAME);
        if (name == null)
            name = System.getProperty("user.name"); //$NON-NLS-1$
        if (name != null)
            meta.setValue(IMeta.AUTHOR_NAME, name);

        String email = System.getProperty(DOMConstants.AUTHOR_EMAIL);
        if (email != null)
            meta.setValue(IMeta.AUTHOR_EMAIL, email);

        String org = System.getProperty(DOMConstants.AUTHOR_ORG);
        if (org != null)
            meta.setValue(IMeta.AUTHOR_ORG, org);

        if (meta.getValue(IMeta.CREATED_TIME) == null)
            meta.setValue(IMeta.CREATED_TIME,
                    NumberUtils.formatDate(System.currentTimeMillis()));
        return workbook;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.core.IWorkbookBuilder#createWorkbookOnTemp(java.lang.String)
     */
    public IWorkbook createWorkbookOnTemp(String tempLocation) {
        if (tempLocation == null)
            throw new IllegalArgumentException("Temp location is null"); //$NON-NLS-1$

        File dir = new File(tempLocation);
        if (!dir.exists())
            throw new IllegalArgumentException("Temp location not exists: " //$NON-NLS-1$
                    + tempLocation);

        return createWorkbook(new DirectoryStorage(dir));
    }

    public Document createDocument() {
        return getDocumentCreator().newDocument();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.core.internal.AbstractWorkbookBuilder#doLoadFromSteam(java.
     * io.InputStream, org.xmind.core.io.IStorage,
     * org.xmind.core.IEncryptionHandler)
     */
    protected IWorkbook doLoadFromSteam(InputStream in, IStorage storage,
            IEncryptionHandler encryptionHandler)
                    throws IOException, CoreException {
        // ZipInputStream has some stability issues, so we have to extract
        // contents into a transient folder first.
        File tempDir = new File(Core.getWorkspace()
                .getTempDir("transient/" + Core.getIdFactory().createId())); //$NON-NLS-1$
        FileUtils.ensureDirectory(tempDir);
        IStorage tempStorage = new DirectoryStorage(tempDir);
        try {
            ZipInputStream zin = new ZipInputStream(in);
            try {
                FileUtils.extractZipFile(zin, tempStorage.getOutputTarget());
            } finally {
                zin.close();
            }
            return loadFromInputSource(tempStorage.getInputSource(), storage,
                    encryptionHandler);
        } finally {
            tempStorage.clear();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.core.internal.AbstractWorkbookBuilder#doLoadFromInputSource
     * (org.xmind.core.io.IInputSource, org.xmind.core.io.IStorage,
     * org.xmind.core.IEncryptionHandler)
     */
    public IWorkbook loadFromInputSource(IInputSource source, IStorage storage,
            IEncryptionHandler encryptionHandler)
                    throws IOException, CoreException {
        if (storage == null) {
            storage = new ByteArrayStorage();
        }
        if (encryptionHandler == null) {
            encryptionHandler = this.defaultEncryptionHandler;
        }
        return new WorkbookLoader(this, source, storage, encryptionHandler)
                .load();
    }

    public void error(SAXParseException exception) throws SAXException {
        Core.getLogger().log(exception, "Error while loading workbook"); //$NON-NLS-1$
    }

    public void fatalError(SAXParseException exception) throws SAXException {
        Core.getLogger().log(exception, "Fatal error while loading workbook"); //$NON-NLS-1$
    }

    public void warning(SAXParseException exception) throws SAXException {
        Core.getLogger().log(exception, "Warning while loading workbook"); //$NON-NLS-1$
    }

    public void setCreator(String name, String version) {
        this.creatorName = name;
        this.creatorversion = version;
    }

}