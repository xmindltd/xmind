package org.xmind.core.internal.dom;

import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.xmind.core.Core;
import org.xmind.core.ISheet;
import org.xmind.core.IWorkbook;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.internal.SettingEntry;
import org.xmind.core.util.DOMUtils;

/**
 * @author Jason Wong
 * @since 3.6.50
 */
public class SettingEntryImpl extends SettingEntry {

    private Element implementation;

    private SheetImpl ownedSheet;

    private String path;

    public SettingEntryImpl(Element implementation, String path,
            SheetImpl ownedSheet) {
        this.implementation = implementation;
        this.path = path;
        this.ownedSheet = ownedSheet;
    }

    protected Element getImplementation() {
        return implementation;
    }

    public String getPath() {
        return path;
    }

    public String getAttribute(String key) {
        return DOMUtils.getAttribute(implementation, key);
    }

    public void setAttribute(String key, String value) {
        String oldValue = DOMUtils.getAttribute(implementation, key);
        DOMUtils.setAttribute(implementation, key, value);
        String newValue = DOMUtils.getAttribute(implementation, key);
        if (oldValue != newValue
                && (oldValue == null || !oldValue.equals(newValue))) {
            ISheet sheet = getOwnedSheet();
            if (sheet != null) {
                CoreEvent event = new CoreEvent(ownedSheet, Core.SheetSettings,
                        key, oldValue, newValue);
                event.setData(getPath());
                ownedSheet.getCoreEventSupport().dispatch(ownedSheet, event);
            }
        }
    }

    public Set<String> getAttributeKeys() {
        Set<String> keys = new HashSet<String>();
        NamedNodeMap atts = implementation.getAttributes();
        for (int i = 0; i < atts.getLength(); i++)
            keys.add(atts.item(i).getNodeName());
        return keys;
    }

    public ISheet getOwnedSheet() {
        return ownedSheet;
    }

    public IWorkbook getOwnedWorkbook() {
        return getOwnedSheet().getOwnedWorkbook();
    }

    public boolean isOrphan() {
        return DOMUtils.isOrphanNode(implementation);
    }

    public <T> T getAdapter(Class<T> adapter) {
        if (adapter.isAssignableFrom(Element.class))
            return adapter.cast(getImplementation());
        return super.getAdapter(adapter);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof SettingEntryImpl))
            return false;
        SettingEntryImpl that = (SettingEntryImpl) obj;
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
