package org.xmind.core.internal.sharing;

import static org.xmind.core.internal.sharing.DOMUtils.childElementArrayByTag;
import static org.xmind.core.internal.sharing.DOMUtils.createElement;
import static org.xmind.core.internal.sharing.DOMUtils.getFirstChildElementByTag;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmind.core.sharing.IContactManager;
import org.xmind.core.sharing.ISharedContact;
import org.xmind.core.sharing.SharingEvent;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class ContactManager implements IContactManager {

    private static final String FILE_ACCESS_CONTROLLER = "access_controller.xml"; //$NON-NLS-1$

    private static final String TAG_ACCESS_CONTROLLER = "access-controller"; //$NON-NLS-1$

    private static final String TAG_CONTACTS = "contacts"; //$NON-NLS-1$

    private static final String TAG_CONTACT = "contact"; //$NON-NLS-1$

    private static final String TAG_ID = "id"; //$NON-NLS-1$

    private static final String ATT_ID = "id"; //$NON-NLS-1$

    private static final String ATT_NAME = "name"; //$NON-NLS-1$

    private static final String ATT_ACCESS_LEVEL = "access-level"; //$NON-NLS-1$

    private static boolean DEBUGGING = LocalNetworkSharing
            .isDebugging(LocalNetworkSharing.DEBUG_OPTION);

    private List<ISharedContact> contacts = new ArrayList<ISharedContact>();

    private Map<String, Integer> accessLevelMap = new HashMap<String, Integer>();

    private File metaFile = null;

    private LocalNetworkSharingService service;

    private ISharedContact myselfContact;

    private String verificationCode;

    public ContactManager(LocalNetworkSharingService service) {
        this.service = service;
        load();
    }

    private void load() {
        long start = System.currentTimeMillis();

        try {
            File metaFile = getMetaFile();
            if (!metaFile.exists())
                return;

            try {
                Document doc = getDocumentBuilder().parse(metaFile);
                Element root = doc.getDocumentElement();

                Element idEle = getFirstChildElementByTag(root, TAG_ID);
                if (idEle != null) {
                    myselfContact = new SharedContact(idEle.getTextContent(),
                            service.getLocalLibrary().getName());
                }

                Element contactsEle = getFirstChildElementByTag(root,
                        TAG_CONTACTS);
                if (contactsEle != null) {
                    Element[] contactEles = childElementArrayByTag(contactsEle,
                            TAG_CONTACT);
                    for (Element contactEle : contactEles) {
                        String id = contactEle.getAttribute(ATT_ID);
                        Integer accessLevel = Integer.parseInt(
                                contactEle.getAttribute(ATT_ACCESS_LEVEL));
                        SharedContact contact = new SharedContact(id,
                                contactEle.getAttribute(ATT_NAME));
                        accessLevelMap.put(id, accessLevel);
                        this.contacts.add(contact);
                    }
                }
            } catch (Throwable e) {
                LocalNetworkSharing.log(
                        "Error occurred while loading shared access controller info.", //$NON-NLS-1$
                        e);
            }
        } finally {
            long end = System.currentTimeMillis();
            if (DEBUGGING)
                System.out.println("Local shared access controller loaded: " //$NON-NLS-1$
                        + (end - start) + " ms"); //$NON-NLS-1$
        }
    }

    private File getMetaFile() {
        if (metaFile == null) {
            metaFile = new File(
                    LocalNetworkSharing.getDefault().getDataDirectory(),
                    FILE_ACCESS_CONTROLLER);
        }
        return metaFile;
    }

    private DocumentBuilder getDocumentBuilder() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            factory.setAttribute(
                    "http://apache.org/xml/features/continue-after-fatal-error", //$NON-NLS-1$
                    Boolean.TRUE);
        } catch (Exception e) {
            // ignore
        }
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.setErrorHandler(new ErrorHandler() {
            public void warning(SAXParseException exception)
                    throws SAXException {
            }

            public void fatalError(SAXParseException exception)
                    throws SAXException {
            }

            public void error(SAXParseException exception) throws SAXException {
            }
        });
        return builder;
    }

    private synchronized void save() {
        long start = System.currentTimeMillis();

        try {
            Document doc = getDocumentBuilder().newDocument();
            Element root = createElement(doc, TAG_ACCESS_CONTROLLER);

            Element idELe = createElement(root, TAG_ID);
            idELe.setTextContent(getMyselfContact().getID());

            Element contactsEle = createElement(root, TAG_CONTACTS);
            for (ISharedContact c : contacts) {
                Element contactEle = createElement(contactsEle, TAG_CONTACT);
                contactEle.setAttribute(ATT_ID, c.getID());
                contactEle.setAttribute(ATT_NAME, c.getName());
                contactEle.setAttribute(ATT_ACCESS_LEVEL,
                        Integer.toString(getAccessLevel(c.getID())));
            }

            File metaFile = getMetaFile();
            metaFile.getParentFile().mkdirs();
            FileOutputStream output = new FileOutputStream(metaFile);
            try {
                DOMUtils.save(doc, output, true);
            } finally {
                output.close();
            }
        } catch (Throwable e) {
            LocalNetworkSharing.log(
                    "Error occurred while saving access controller info.", //$NON-NLS-1$
                    e);
        } finally {
            long end = System.currentTimeMillis();
            if (DEBUGGING)
                System.out.println("Local shared library saved: " //$NON-NLS-1$
                        + (end - start) + " ms"); //$NON-NLS-1$
        }
    }

    public ISharedContact getMyselfContact() {
        if (myselfContact == null) {
            myselfContact = new SharedContact(UUID.randomUUID().toString(),
                    service.getLocalLibrary().getName());
            save();
        }
        return myselfContact;
    }

    public boolean isContact(String contactID) {
        if (contactID != null) {
            for (ISharedContact c : contacts) {
                if (contactID.equals(c.getID()))
                    return true;
            }
        }
        return false;
    }

    public int getAccessLevel(String contactID) {
        if (contactID != null) {
            Integer accessLevel = accessLevelMap.get(contactID);
            if (accessLevel != null)
                return accessLevel;
        }
        return IContactManager.NO_ACCESS;
    }

    public synchronized ISharedContact addContact(String contactID, String name,
            int accessLevel) {
        if (contactID == null || "".equals(contactID) //$NON-NLS-1$
                || name == null || "".equals(name)) //$NON-NLS-1$
            return null;

        SharedContact contact = null;
        for (ISharedContact c : contacts) {
            if (contactID.equals(c.getID())) {
                contact = (SharedContact) c;
                contact.setName(name);
                accessLevelMap.put(contactID, accessLevel);
                break;
            }
        }

        if (contact == null) {
            contact = new SharedContact(contactID, name);
            accessLevelMap.put(contactID, accessLevel);
            contacts.add(contact);
        }

        save();

        service.fireSharingEvent(new SharingEvent(
                SharingEvent.Type.CONTACT_ADDED, service.getLocalLibrary(),
                contactID, service.getLocalLibrary().getMaps()));
        return contact;
    }

    public synchronized ISharedContact[] getContacts() {
        return contacts.toArray(new ISharedContact[contacts.size()]);
    }

    public synchronized ISharedContact findContactByID(String contactID) {
        if (contactID == null || "".equals(contactID)) //$NON-NLS-1$
            return null;

        for (ISharedContact c : contacts) {
            if (contactID.equals(c.getID()))
                return c;
        }
        return null;
    }

    public synchronized void updateContactName(String contactID, String name) {
        ISharedContact contact = findContactByID(contactID);
        if (contact == null)
            return;

        if (name == null || "".equals(name) //$NON-NLS-1$
                || name.equals(contact.getName()))
            return;
        ((SharedContact) contact).setName(name);
        save();
    }

    public String getVerificationCode() {
        if (verificationCode == null)
            verificationCode = UUID.randomUUID().toString();
        return verificationCode;
    }

}
