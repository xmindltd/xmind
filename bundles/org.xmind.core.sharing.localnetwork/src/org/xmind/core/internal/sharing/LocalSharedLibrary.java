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
package org.xmind.core.internal.sharing;

import static org.xmind.core.internal.sharing.AbstractSharedMap.MAP_COMPARATOR;
import static org.xmind.core.internal.sharing.DOMUtils.childElementArrayByTag;
import static org.xmind.core.internal.sharing.DOMUtils.createElement;
import static org.xmind.core.internal.sharing.DOMUtils.getFirstChildElementByTag;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.coobird.thumbnailator.Thumbnails;

import org.eclipse.core.runtime.Platform;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmind.core.sharing.ILocalSharedLibrary;
import org.xmind.core.sharing.ISharedMap;
import org.xmind.core.sharing.ISharingService;
import org.xmind.core.sharing.SharingEvent;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * @author Frank Shaka
 * @author Jason Wong
 */
public class LocalSharedLibrary implements ILocalSharedLibrary {

    private static final String FILE_SHARED_MAPS = "shared_maps.xml"; //$NON-NLS-1$

    private static final String TAG_SHARED_MAPS = "shared-maps"; //$NON-NLS-1$

    private static final String TAG_NAME = "name"; //$NON-NLS-1$

    private static final String TAG_MAPS = "maps"; //$NON-NLS-1$

    private static final String TAG_MAP = "map"; //$NON-NLS-1$

    private static final String TAG_THUMBNAIL = "thumbnail"; //$NON-NLS-1$

    private static final String TAG_RECEIVERS = "receivers"; //$NON-NLS-1$

    private static final String TAG_RECEIVER = "receiver"; //$NON-NLS-1$

    private static final String ATT_ID = "id"; //$NON-NLS-1$

    private static final String ATT_RESOURCE = "resource"; //$NON-NLS-1$

    private static final String ATT_MODIFIED_TIME = "modified-time"; //$NON-NLS-1$

    private static final String ATT_ADDED_TIME = "added-time"; //$NON-NLS-1$

    private static final String XMIND_2014_THUMBNAIL = "images/thumbnail.png"; //$NON-NLS-1$

    private static boolean DEBUGGING = LocalNetworkSharing
            .isDebugging(LocalNetworkSharing.DEBUG_OPTION);

    private LocalNetworkSharingService service;

    private String defaultName = System.getProperty("user.name"); //$NON-NLS-1$

    private String name = null;

    private String contactId = null;

    private List<ISharedMap> maps = new ArrayList<ISharedMap>();

    private Thread monitorThread = null;

    private File metaFile = null;

    private String encodedXMind2014Thumbnail = null;

    public LocalSharedLibrary(LocalNetworkSharingService service) {
        this.service = service;

        load();
        runMonitor();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Object getAdapter(Class adapter) {
        if (adapter == ISharingService.class)
            return service;
        return Platform.getAdapterManager().getAdapter(this, adapter);
    }

    public void dispose() {
        Thread t = this.monitorThread;
        this.monitorThread = null;
        if (t != null) {
            t.interrupt();
        }
    }

    private DocumentBuilder getDocumentBuilder() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setAttribute(
                "http://apache.org/xml/features/continue-after-fatal-error", //$NON-NLS-1$
                true);
        factory.setFeature(
                "http://apache.org/xml/features/disallow-doctype-decl", true); //$NON-NLS-1$
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        factory.setFeature(
                "http://xml.org/sax/features/external-parameter-entities", false); //$NON-NLS-1$
        factory.setFeature(
                "http://xml.org/sax/features/external-general-entities", false); //$NON-NLS-1$
        factory.setNamespaceAware(true);
        factory.setIgnoringElementContentWhitespace(true);
        DocumentBuilder documentBuilder = factory.newDocumentBuilder();
        documentBuilder.setErrorHandler(new ErrorHandler() {

            public void warning(SAXParseException exception)
                    throws SAXException {
            }

            public void fatalError(SAXParseException exception)
                    throws SAXException {
            }

            public void error(SAXParseException exception) throws SAXException {
            }

        });
        return documentBuilder;
    }

    private void load() {
        long start = System.currentTimeMillis();

        try {

            /*
             * === Start of loading local shared library ===
             */

            File metaFile = getMetaFile();
            if (!metaFile.exists())
                return;

            try {
                Document doc = getDocumentBuilder().parse(metaFile);
                Element root = doc.getDocumentElement();
                Element nameElement = getFirstChildElementByTag(root, TAG_NAME);
                if (nameElement != null) {
                    this.name = nameElement.getTextContent();
                }

                Element mapsElement = getFirstChildElementByTag(root, TAG_MAPS);
                if (mapsElement != null) {
                    Element[] mapElements = childElementArrayByTag(mapsElement,
                            TAG_MAP);

                    for (Element mapElement : mapElements) {
                        LocalSharedMap map = new LocalSharedMap(this,
                                mapElement.getAttribute(ATT_ID));
                        map.setResourcePath(mapElement
                                .getAttribute(ATT_RESOURCE));
                        String modifiedTime = mapElement
                                .getAttribute(ATT_MODIFIED_TIME);
                        if (modifiedTime != null && !"".equals(modifiedTime)) { //$NON-NLS-1$
                            try {
                                map.setResourceModifiedTime(Long.parseLong(
                                        modifiedTime, 10));
                            } catch (NumberFormatException e) {
                                LocalNetworkSharing.log(
                                        "Invalid 'modified-time': " //$NON-NLS-1$
                                                + modifiedTime, e);
                            }
                        }
                        String addedTime = mapElement
                                .getAttribute(ATT_ADDED_TIME);
                        if (addedTime != null && !"".equals(addedTime)) { //$NON-NLS-1$
                            try {
                                map.setAddedTime(Long.parseLong(addedTime, 10));
                            } catch (NumberFormatException e) {
                                LocalNetworkSharing.log(
                                        "Invalid 'added-time': " //$NON-NLS-1$
                                                + addedTime, e);
                            }
                        }
                        Element thumbnailElement = getFirstChildElementByTag(
                                mapElement, TAG_THUMBNAIL);
                        map.setEncodedThumbnailData(thumbnailElement == null ? null
                                : thumbnailElement.getTextContent());

                        Element receiverIDsEle = getFirstChildElementByTag(
                                mapElement, TAG_RECEIVERS);
                        if (receiverIDsEle != null) {
                            Element[] receiverIdEles = childElementArrayByTag(
                                    receiverIDsEle, TAG_RECEIVER);
                            for (Element receiverIdElement : receiverIdEles) {
                                String id = receiverIdElement
                                        .getAttribute(ATT_ID);
                                if (id != null && !"".equals(id)) { //$NON-NLS-1$
                                    map.addReceiver(id);
                                }
                            }
                        }
                        this.maps.add(map);
                    }
                }
            } catch (Throwable e) {
                LocalNetworkSharing
                        .log("Error occurred while loading local shared library info.", //$NON-NLS-1$
                                e);
            }

            /*
             * === End of loading local shared library ===
             */

        } finally {
            long end = System.currentTimeMillis();
            if (DEBUGGING)
                System.out.println("Local shared library loaded: " //$NON-NLS-1$
                        + (end - start) + " ms"); //$NON-NLS-1$
        }
    }

    private File getMetaFile() {
        if (metaFile == null) {
            metaFile = new File(LocalNetworkSharing.getDefault()
                    .getDataDirectory(), FILE_SHARED_MAPS);
        }
        return metaFile;
    }

    public boolean isLocal() {
        return true;
    }

    public synchronized ISharedMap[] getMaps() {
        return this.maps.toArray(new ISharedMap[this.maps.size()]);
    }

    public boolean hasMaps() {
        return !this.maps.isEmpty();
    }

    public int getMapCount() {
        return this.maps.size();
    }

    public ISharedMap findMapByID(String resourceID) {
        if (resourceID != null) {
            for (ISharedMap map : this.maps) {
                if (resourceID.equals(map.getID()))
                    return map;
            }
        }
        return null;
    }

    public String getName() {
        if (this.name == null) {
            return this.defaultName;
        }
        return this.name;
    }

    public String getDefaultName() {
        return this.defaultName;
    }

    public String getLocalName() {
        return this.name;
    }

    public String getContactID() {
        if (this.contactId == null) {
            this.contactId = service.getContactManager().getMyselfContact()
                    .getID();
        }
        return this.contactId;
    }

    public void setDefaultName(String name) {
        assert name != null;
        this.defaultName = name;
    }

    public void setName(String name) {
        if (name == null || name.equals(this.name))
            return;
        this.name = name;
        save();
        this.service.fireSharingEvent(new SharingEvent(
                SharingEvent.Type.LIBRARY_NAME_CHANGED, this));
    }

    public synchronized ISharedMap addSharedMap(File file) {
        return addSharedMap(file, Collections.<String> emptyList());
    }

    public synchronized ISharedMap addSharedMap(File file,
            List<String> receivers) {
        String resourcePath = file.getAbsolutePath();
        LocalSharedMap map = null;

        // Search for existing shared map:
        for (ISharedMap m : this.maps) {
            if (resourcePath.equals(((LocalSharedMap) m).getResourcePath())) {
                removeSharedMap(m);
                map = (LocalSharedMap) m;
                map.getReceiverIDs().clear();
                map.addReceivers(receivers);
                break;
            }
        }

        // Create descriptor for this new map:
        if (map == null) {
            map = new LocalSharedMap(this, UUID.randomUUID().toString());
            map.setResourcePath(resourcePath);
            map.setAddedTime(System.currentTimeMillis());
            for (String receiver : receivers) {
                map.addReceiver(receiver);
            }
        }

        this.maps.add(map);
        Collections.sort(this.maps, MAP_COMPARATOR);

        // Check and update shared map descriptor:
        checkMap(map);

        // Save library meta info into local file system:
        save();

        // Broadcast sharing event for new map shared:
        this.service.fireSharingEvent(new SharingEvent(
                SharingEvent.Type.SHARED_MAP_ADDED, this, map));

        return map;
    }

    private byte[] createThumbnailData(File file) {
        try {
            ZipInputStream zin = new ZipInputStream(new FileInputStream(file));
            try {
                ZipEntry entry;
                while ((entry = zin.getNextEntry()) != null) {
                    try {
                        String entryName = entry.getName();
                        if ("Thumbnails/thumbnail.png".equals(entryName) //$NON-NLS-1$
                                || "Thumbnails/thumbnail.jpg".equals(entryName)) { //$NON-NLS-1$
                            return createThumbnailData(zin);
                        }
                    } finally {
                        zin.closeEntry();
                    }
                }
            } finally {
                zin.close();
            }
        } catch (IOException e) {
            LocalNetworkSharing.log(
                    "Error occurred while create thumbnail data from local shared map: " //$NON-NLS-1$
                            + file.getAbsolutePath(), e);
        }
        return new byte[0];
    }

    private byte[] createThumbnailData(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream(4096);
        Thumbnails.of(input).size(200, 120).outputFormat("jpeg") //$NON-NLS-1$
                .outputQuality(1).toOutputStream(output);
        return output.toByteArray();
    }

    public synchronized boolean removeSharedMap(ISharedMap map) {
        if (this.maps.remove(map)) {
            save();
            this.service.fireSharingEvent(new SharingEvent(
                    SharingEvent.Type.SHARED_MAP_REMOVED, this, map));
            return true;
        } else {
            return false;
        }
    }

    private synchronized void save() {
        long start = System.currentTimeMillis();

        try {

            /*
             * === Start of saving local shared library ===
             */

            Document doc = getDocumentBuilder().newDocument();
            Element root = createElement(doc, TAG_SHARED_MAPS);

            Element nameElement = createElement(root, TAG_NAME);
            nameElement.setTextContent(getName());

            Element mapsElement = createElement(root, TAG_MAPS);
            for (ISharedMap map : this.maps) {
                Element mapElement = createElement(mapsElement, TAG_MAP);
                LocalSharedMap localMap = (LocalSharedMap) map;
                mapElement.setAttribute(ATT_ID, localMap.getID());
                mapElement.setAttribute(ATT_RESOURCE,
                        localMap.getResourcePath());
                mapElement.setAttribute(ATT_MODIFIED_TIME,
                        Long.toString(localMap.getResourceModifiedTime(), 10));
                mapElement.setAttribute(ATT_ADDED_TIME,
                        Long.toString(localMap.getAddedTime(), 10));
                String thumbnailData = localMap.getEncodedThumbnailData();
                if (thumbnailData != null) {
                    Element thumbnailElement = createElement(mapElement,
                            TAG_THUMBNAIL);
                    thumbnailElement.setTextContent(thumbnailData);
                }

                List<String> receiverIds = localMap.getReceiverIDs();
                if (!receiverIds.isEmpty()) {
                    Element receiversElement = createElement(mapElement,
                            TAG_RECEIVERS);
                    for (String id : receiverIds) {
                        Element receiverElement = createElement(
                                receiversElement, TAG_RECEIVER);
                        receiverElement.setAttribute(ATT_ID, id);
                    }
                }
            }

            File metaFile = getMetaFile();
            metaFile.getParentFile().mkdirs();
            FileOutputStream output = new FileOutputStream(metaFile);
            try {
                DOMUtils.save(doc, output, true);
            } finally {
                output.close();
            }

            /*
             * === End of saving local shared library ===
             */

        } catch (Throwable e) {
            LocalNetworkSharing.log(
                    "Error occurred while saving local shared library info.", //$NON-NLS-1$
                    e);
        } finally {
            long end = System.currentTimeMillis();
            if (DEBUGGING)
                System.out.println("Local shared library saved: " //$NON-NLS-1$
                        + (end - start) + " ms"); //$NON-NLS-1$
        }
    }

    private void runMonitor() {
        Thread t = new Thread(new Runnable() {
            public void run() {
                monitorLoop();
            }
        });
        t.setDaemon(true);
        t.setPriority(Thread.MIN_PRIORITY);
        this.monitorThread = t;
        t.start();
        if (DEBUGGING)
            System.out.println("Local shared maps monitor started."); //$NON-NLS-1$
    }

    private void monitorLoop() {
        try {
            while (monitorThread != null) {
                Thread.sleep(5000);
                if (monitorThread == null)
                    break;
                check();
            }
        } catch (InterruptedException e) {
        } finally {
            if (DEBUGGING)
                System.out.println("Local shared maps monitor stopped."); //$NON-NLS-1$
        }
    }

    private synchronized void check() {
        Collection<ISharedMap> modifiedMaps = null;
        for (ISharedMap map : this.maps) {
            boolean modified = checkMap((LocalSharedMap) map);
            if (modified) {
                if (modifiedMaps == null) {
                    modifiedMaps = new ArrayList<ISharedMap>(this.maps.size());
                }
                modifiedMaps.add(map);
            }
        }
        if (modifiedMaps != null && !modifiedMaps.isEmpty()) {
            save();
            for (ISharedMap map : modifiedMaps) {
                service.fireSharingEvent(new SharingEvent(
                        SharingEvent.Type.SHARED_MAP_UPDATED, this, map));
            }
        }
    }

    private boolean checkMap(LocalSharedMap map) {
        String path = map.getResourcePath();
        boolean oldMissing = map.isMissing();
        long oldModifiedTime = map.getResourceModifiedTime();
        boolean modified = false;
        File file = new File(path);
        boolean newMissing = !file.exists();
        if (oldMissing != newMissing) {
            if (DEBUGGING)
                System.out.println("Local shared map " //$NON-NLS-1$
                        + (newMissing ? "is missing: " : "is found: ") + path); //$NON-NLS-1$ //$NON-NLS-2$
            map.setMissing(newMissing);
            modified = true;
        }
        long newModifiedTime = file.lastModified();
        if (oldModifiedTime < newModifiedTime) {
            if (DEBUGGING)
                System.out.println("Local shared map is modified: " + path); //$NON-NLS-1$
            map.setResourceModifiedTime(newModifiedTime);
            map.setThumbnailData(createThumbnailData(file));
            modified = true;
        }

        return modified;
    }

    public void sortMaps(Comparator<ISharedMap> c) {
        Collections.sort(maps, c);
    }

    public String getEncodedXMind2014Thumbnail() {
        if (encodedXMind2014Thumbnail == null) {
            byte[] thumbnail = createXMind2014Thumbnail();
            encodedXMind2014Thumbnail = Base64.byteArrayToBase64(thumbnail);
        }
        return encodedXMind2014Thumbnail;
    }

    private byte[] createXMind2014Thumbnail() {
        URL url = Platform.getBundle(LocalNetworkSharing.ID).getEntry(
                XMIND_2014_THUMBNAIL);
        if (url != null) {
            try {
                InputStream input = url.openStream();
                ByteArrayOutputStream output = new ByteArrayOutputStream(4096);

                byte[] buff = new byte[4096];
                int len = 0;
                while ((len = input.read(buff, 0, buff.length)) > 0) {
                    output.write(buff, 0, len);
                }
                return output.toByteArray();
            } catch (IOException e) {
                LocalNetworkSharing.log("Create XMind 2014 thumbnail failed: " //$NON-NLS-1$
                        , e);
            }
        }
        return new byte[0];
    }

}
