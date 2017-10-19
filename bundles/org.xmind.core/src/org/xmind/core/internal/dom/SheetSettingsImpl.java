package org.xmind.core.internal.dom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Element;
import org.xmind.core.Core;
import org.xmind.core.ISettingEntry;
import org.xmind.core.ISheet;
import org.xmind.core.IWorkbook;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.internal.ElementRegistry;
import org.xmind.core.internal.SheetSettings;
import org.xmind.core.util.DOMUtils;

/**
 * @author Jason Wong
 * @since 3.6.50
 */
public class SheetSettingsImpl extends SheetSettings {

    private Element implementation;

    private SheetImpl ownedSheet;

    private ElementRegistry elementRegistry;

    public SheetSettingsImpl(Element implementation, SheetImpl ownedSheet) {
        super();
        this.implementation = implementation;
        this.ownedSheet = ownedSheet;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.ISheetSettings#getPaths()
     */
    public Set<String> getPaths() {
        HashSet<String> paths = new HashSet<String>();
        Element e = DOMUtils.getFirstChildElementByTag(implementation,
                DOMConstants.TAG_SHEET_SETTINGS);
        if (e == null)
            return paths;

        collectPaths(paths, e, null);
        return paths;
    }

    private void collectPaths(Set<String> paths, Element p, String path) {
        Iterator<Element> childIt = DOMUtils.childElementIter(p);
        if (childIt.hasNext()) {
            while (childIt.hasNext()) {
                Element c = childIt.next();
                String subTag = c.getTagName();
                String subPath = path == null ? subTag : path + SEP + subTag;
                collectPaths(paths, c, subPath);
            }
        } else {
            if (path != null) {
                paths.add(path);
            }
        }
    }

    public List<ISettingEntry> getEntries(String path) {
        if (path == null || "".equals(path)) //$NON-NLS-1$
            return Collections.emptyList();

        Element e = DOMUtils.getFirstChildElementByTag(implementation,
                DOMConstants.TAG_SHEET_SETTINGS);
        if (e == null)
            return Collections.emptyList();

        String[] keys = path.split(SEP);
        if (keys.length > 0) {
            String endKey = keys[keys.length - 1];
            Element c = null;
            for (String key : keys) {
                if (key != endKey) {
                    c = DOMUtils.getFirstChildElementByTag(e, key);
                    if (c == null)
                        break;
                    e = c;
                } else {
                    List<ISettingEntry> entries = new ArrayList<ISettingEntry>();
                    Iterator<Element> es = DOMUtils.childElementIterByTag(e,
                            key);
                    while (es.hasNext()) {
                        Element entryEle = es.next();
                        ISettingEntry entry = getEntry(entryEle, path);
                        if (entry != null)
                            entries.add(entry);
                    }
                    return entries;
                }
            }
        }
        return Collections.emptyList();
    }

    protected SettingEntryImpl getEntry(Element entryEle, String path) {
        if (elementRegistry != null) {
            Object entry = elementRegistry.getElement(entryEle);
            if (entry != null && entry instanceof ISettingEntry)
                return (SettingEntryImpl) entry;
        }

        SettingEntryImpl entry = new SettingEntryImpl(entryEle, path,
                ownedSheet);
        getElementRegistry().registerByKey(entryEle, entry);
        return entry;
    }

    public ISettingEntry createEntry(String path) {
        if (path != null) {
            String[] keys = path.split(SEP);
            if (keys.length > 0) {
                String tag = keys[keys.length - 1];
                Element entryEle = implementation.getOwnerDocument()
                        .createElement(tag);
                SettingEntryImpl entry = new SettingEntryImpl(entryEle, path,
                        ownedSheet);
                getElementRegistry().registerByKey(entryEle, entry);
                return entry;
            }
        }
        return null;
    }

    public void addEntry(ISettingEntry entry) {
        SettingEntryImpl entryImpl = (SettingEntryImpl) entry;
        String path = entryImpl.getPath();
        String[] keys = path.split(SEP);

        String lastKey = keys[keys.length - 1];
        Element p = DOMUtils.ensureChildElement(implementation,
                DOMConstants.TAG_SHEET_SETTINGS);
        for (String key : keys) {
            if (key != lastKey) {
                p = DOMUtils.ensureChildElement(p, key);
            } else {
                p.appendChild(entryImpl.getImplementation());
                for (String attrKey : entry.getAttributeKeys()) {
                    CoreEvent event = new CoreEvent(ownedSheet,
                            Core.SheetSettings, attrKey, null,
                            entry.getAttribute(attrKey));
                    event.setData(path);
                    ownedSheet.getCoreEventSupport().dispatch(ownedSheet,
                            event);
                }
            }
        }
    }

    public void removeEntry(ISettingEntry entry) {
        Element settingsEle = DOMUtils.getFirstChildElementByTag(implementation,
                DOMConstants.TAG_SHEET_SETTINGS);
        if (settingsEle == null)
            return;

        String path = entry.getPath();
        Element entryEle = ((SettingEntryImpl) entry).getImplementation();
        settingsEle.removeChild(entryEle);
        for (String attrKey : entry.getAttributeKeys()) {
            CoreEvent event = new CoreEvent(ownedSheet, Core.SheetSettings,
                    attrKey, entry.getAttribute(attrKey), null);
            event.setData(path);
            ownedSheet.getCoreEventSupport().dispatch(ownedSheet, event);
        }
    }

    protected Element getImplementation() {
        return implementation;
    }

    public ElementRegistry getElementRegistry() {
        if (elementRegistry == null) {
            elementRegistry = new ElementRegistry();
        }
        return elementRegistry;
    }

    public ISheet getOwnedSheet() {
        return ownedSheet;
    }

    public IWorkbook getOwnedWorkbook() {
        return ownedSheet.getOwnedWorkbook();
    }

    public boolean isOrphan() {
        return DOMUtils.isOrphanNode(implementation);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof SheetSettingsImpl))
            return false;
        SheetSettingsImpl that = (SheetSettingsImpl) obj;
        return that.implementation == this.implementation;
    }

    @Override
    public int hashCode() {
        return implementation.hashCode();
    }

    @Override
    public String toString() {
        return DOMUtils.toString(implementation);
    }

}
