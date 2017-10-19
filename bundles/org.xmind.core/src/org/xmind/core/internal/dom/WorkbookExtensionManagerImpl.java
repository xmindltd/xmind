package org.xmind.core.internal.dom;

import static org.xmind.core.internal.dom.DOMConstants.ATTR_PROVIDER;
import static org.xmind.core.internal.dom.DOMConstants.TAG_EXTENSION;
import static org.xmind.core.internal.dom.DOMConstants.TAG_EXTENSIONS;
import static org.xmind.core.internal.zip.ArchiveConstants.PATH_EXTENSIONS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmind.core.IFileEntry;
import org.xmind.core.IManifest;
import org.xmind.core.IWorkbook;
import org.xmind.core.IWorkbookExtension;
import org.xmind.core.internal.WorkbookExtensionManager;
import org.xmind.core.util.DOMUtils;

/**
 * @author Jason Wong
 */
public class WorkbookExtensionManagerImpl extends WorkbookExtensionManager {

    private WorkbookImpl ownedWorkbook;

    private Map<String, WorkbookExtensionImpl> extensions = new HashMap<String, WorkbookExtensionImpl>();

    public WorkbookExtensionManagerImpl() {
    }

    protected void setOwnedWorkbook(WorkbookImpl ownedWorkbook) {
        this.ownedWorkbook = ownedWorkbook;
    }

    public IWorkbook getOwnedWorkbook() {
        return ownedWorkbook;
    }

    public IWorkbookExtension createExtension(String providerName) {
        return createExtension(providerName, null);
    }

    protected IWorkbookExtension createExtension(String providerName,
            Document doc) {
        if (providerName == null || "".equals(providerName)) //$NON-NLS-1$
            return null;

        IWorkbookExtension ext = extensions.get(providerName);
        if (ext == null) {
            if (doc == null)
                doc = createExtensionDocument(providerName);
            ensureExtensionElement(providerName);
            ensureFileEntry(providerName);

            ext = new WorkbookExtensionImpl(doc, ownedWorkbook);
            extensions.put(providerName, (WorkbookExtensionImpl) ext);
            ((WorkbookExtensionImpl) ext).addNotify(ownedWorkbook);
            ownedWorkbook.updateModificationInfo();
        }
        return ext;
    }

    private void ensureExtensionElement(String provider) {
        Element extsEle = DOMUtils.ensureChildElement(getWorkbookElement(),
                TAG_EXTENSIONS);
        Element[] es = DOMUtils.getChildElementsByTag(extsEle, TAG_EXTENSION);
        for (Element e : es) {
            if (provider.equals(e.getAttribute(//
                    ATTR_PROVIDER)))
                return;
        }

        Element e = DOMUtils.createElement(extsEle, TAG_EXTENSION);
        e.setAttribute(ATTR_PROVIDER, provider);
    }

    private Document createExtensionDocument(String provider) {
        Document doc = DOMUtils.createDocument(getExtensionTag(provider));
        doc.getDocumentElement().setAttribute(ATTR_PROVIDER, provider);
        return doc;
    }

    private void ensureFileEntry(String provider) {
        IManifest m = ownedWorkbook.getManifest();
        String path = PATH_EXTENSIONS + provider + ".xml";//$NON-NLS-1$
        IFileEntry entry = m.getFileEntry(path);
        if (entry == null) {
            m.createFileEntry(path).increaseReference();
        }
    }

    private Element getWorkbookElement() {
        return ownedWorkbook.getWorkbookElement();
    }

    private String getExtensionTag(String providerName) {
        if (providerName.contains(".")) { //$NON-NLS-1$
            int index = providerName.lastIndexOf("."); //$NON-NLS-1$
            if (index > 0 && index < providerName.length())
                return providerName.substring(index + 1);
        }
        return providerName;
    }

    public List<IWorkbookExtension> getExtensions() {
        return new ArrayList<IWorkbookExtension>(extensions.values());
    }

    public IWorkbookExtension getExtension(String provider) {
        if (provider != null && !"".equals(provider)) { //$NON-NLS-1$
            for (IWorkbookExtension e : getExtensions()) {
                if (provider.equals(e.getProviderName()))
                    return e;
            }
        }
        return null;
    }

    public void deleteExtension(String providerName) {
        // TODO Auto-generated method stub
    }

    public List<String> getProviders() {
        Element es = DOMUtils.getFirstChildElementByTag(getWorkbookElement(),
                TAG_EXTENSIONS);
        if (es != null) {
            Element[] eles = DOMUtils.getChildElementsByTag(es, TAG_EXTENSION);
            List<String> providers = new ArrayList<String>();
            for (Element ele : eles) {
                String provider = ele.getAttribute(ATTR_PROVIDER);
                if (provider != null && !"".equals(provider)) { //$NON-NLS-1$
                    providers.add(provider);
                }
            }
            return providers;
        }
        return Collections.emptyList();
    }

    public boolean isOrphan() {
        return false;
    }

}
