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
package org.xmind.core.internal.dom;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xmind.core.Core;
import org.xmind.core.CoreException;
import org.xmind.core.IDeserializer;
import org.xmind.core.IEntryStreamNormalizer;
import org.xmind.core.IMeta;
import org.xmind.core.ISerializer;
import org.xmind.core.IWorkbook;
import org.xmind.core.internal.AbstractWorkbookBuilder;
import org.xmind.core.io.ByteArrayStorage;
import org.xmind.core.io.IInputSource;
import org.xmind.core.io.IOutputTarget;
import org.xmind.core.io.IStorage;
import org.xmind.core.util.DOMUtils;
import org.xmind.core.util.FileUtils;

@SuppressWarnings("deprecation")
public class WorkbookBuilderImpl extends AbstractWorkbookBuilder {

    protected synchronized Document createDocument() {
        DocumentBuilder docBuilder;
        try {
            docBuilder = DOMUtils.getDefaultDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException(e);
        }
        return docBuilder.newDocument();
    }

    protected synchronized DocumentBuilder getDocumentLoader()
            throws CoreException {
        try {
            DocumentBuilder loader = DOMUtils.getDefaultDocumentBuilder();
            return loader;
        } catch (ParserConfigurationException e) {
            throw new CoreException(Core.ERROR_FAIL_ACCESS_XML_PARSER, e);
        }
    }

    @Override
    protected IWorkbook doCreateWorkbook(IStorage storage) {
        if (storage == null)
            storage = new ByteArrayStorage();
        storage.clear();

        Document contentDoc = createDocument();
        WorkbookImpl workbook = new WorkbookImpl(contentDoc, storage);

        /*
         * ------------------------------------------------------ NEED REFACTOR:
         */
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

        meta.setValue(IMeta.CREATOR_NAME, getCreatorName());
        meta.setValue(IMeta.CREATOR_VERSION, getCreatorVersion());

        return workbook;
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.core.IWorkbookBuilder#newDeserializer()
     */
    public IDeserializer newDeserializer() {
        DeserializerImpl deserializer = new DeserializerImpl();
        deserializer.setCreatorName(getCreatorName());
        deserializer.setCreatorVersion(getCreatorVersion());
        return deserializer;
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.core.IWorkbookBuilder#newSerializer()
     */
    public ISerializer newSerializer() {
        SerializerImpl serializer = new SerializerImpl();
        serializer.setCreatorName(getCreatorName());
        serializer.setCreatorVersion(getCreatorVersion());
        return serializer;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.xmind.core.internal.AbstractWorkbookBuilder#doLoadFromInputSource(org
     * .xmind.core.io.IInputSource, org.xmind.core.io.IStorage,
     * org.xmind.core.IEncryptionHandler)
     */
    @Override
    protected IWorkbook doLoadFromInputSource(IInputSource source,
            IStorage storage, IEntryStreamNormalizer normalizer)
            throws IOException, CoreException {
        IDeserializer deserializer = Core.getWorkbookBuilder()
                .newDeserializer();
        deserializer.setCreatorName(getCreatorName());
        deserializer.setCreatorVersion(getCreatorVersion());
        deserializer.setWorkbookStorage(storage);
        deserializer.setEntryStreamNormalizer(normalizer);
        deserializer.setInputSource(source);
        deserializer.deserialize(null);
        return deserializer.getWorkbook();
    }

    /*
     * (non-Javadoc)
     * @see
     * org.xmind.core.internal.AbstractWorkbookBuilder#doLoadFromStream(java.io.
     * InputStream, org.xmind.core.io.IStorage,
     * org.xmind.core.IEncryptionHandler)
     */
    @Override
    protected IWorkbook doLoadFromStream(InputStream in, IStorage storage,
            IEntryStreamNormalizer normalizer)
            throws IOException, CoreException {
        IDeserializer deserializer = Core.getWorkbookBuilder()
                .newDeserializer();
        deserializer.setCreatorName(getCreatorName());
        deserializer.setCreatorVersion(getCreatorVersion());
        deserializer.setWorkbookStorage(storage);
        deserializer.setEntryStreamNormalizer(normalizer);
        deserializer.setInputStream(in);
        deserializer.deserialize(null);
        return deserializer.getWorkbook();
    }

    @Override
    protected IWorkbook doLoadFromStorage(IStorage storage,
            IEntryStreamNormalizer normalizer)
            throws IOException, CoreException {
        IDeserializer deserializer = Core.getWorkbookBuilder()
                .newDeserializer();
        deserializer.setCreatorName(getCreatorName());
        deserializer.setCreatorVersion(getCreatorVersion());
        deserializer.setWorkbookStorage(storage);
        deserializer.setEntryStreamNormalizer(normalizer);
        deserializer.setWorkbookStorageAsInputSource();
        deserializer.deserialize(null);
        return deserializer.getWorkbook();
    }

    /**
     * @deprecated This method should NOT be called any more.
     */
    @Override
    @Deprecated
    protected void extractFromStream(InputStream input, IOutputTarget target)
            throws IOException, CoreException {
        ZipInputStream zip = new ZipInputStream(input);
        try {
            FileUtils.extractZipFile(zip, target);
        } finally {
            zip.close();
        }
    }

}
