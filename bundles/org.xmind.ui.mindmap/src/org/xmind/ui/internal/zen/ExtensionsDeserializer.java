package org.xmind.ui.internal.zen;

import java.util.Iterator;

import org.eclipse.core.runtime.Assert;
import org.json.JSONArray;
import org.json.JSONObject;
import org.xmind.core.IResourceRef;
import org.xmind.core.IWorkbookExtension;
import org.xmind.core.IWorkbookExtensionElement;
import org.xmind.core.IWorkbookExtensionManager;
import org.xmind.core.util.HyperlinkUtils;

/**
 * @author Jason Wong
 */
public class ExtensionsDeserializer {

    public ExtensionsDeserializer() {
    }

    public void deserialize(IWorkbookExtensionManager extensionManager,
            JSONObject extensionsObject) {
        JSONArray extArray = extensionsObject
                .optJSONArray(ZenConstants.KEY_EXTENSIONS);
        if (extArray != null) {
            for (Object extObject : extArray) {
                if (extObject instanceof JSONObject) {
                    deserializeWorkbookExtension(extensionManager,
                            (JSONObject) extObject);
                }
            }
        }
    }

    private void deserializeWorkbookExtension(
            IWorkbookExtensionManager extensionManager, JSONObject extObject) {
        String providerName = extObject.optString(ZenConstants.KEY_PROVIDER,
                null);
        Assert.isNotNull(providerName);
        IWorkbookExtension ext = extensionManager.createExtension(providerName);

        deserializeWorkbookExtensionElement(ext.getContent(), extObject);
        JSONArray resourceRefArray = extObject
                .optJSONArray(ZenConstants.KEY_RESOURCE_REFS);
        if (resourceRefArray != null) {
            for (Object resourceRefArrayElement : resourceRefArray) {
                if (resourceRefArrayElement instanceof String) {
                    String refURL = (String) resourceRefArrayElement;
                    if (HyperlinkUtils.isAttachmentURL(refURL)) {
                        IResourceRef ref = ext.getOwnedWorkbook()
                                .createResourceRef(IResourceRef.FILE_ENTRY,
                                        HyperlinkUtils
                                                .toAttachmentPath(refURL));
                        ext.addResourceRef(ref);
                    }
                }
            }
        }
    }

    private void deserializeWorkbookExtensionElement(
            IWorkbookExtensionElement ele, JSONObject eleObject) {
        JSONObject attrMapObject = eleObject
                .optJSONObject(ZenConstants.KEY_ATTRS);
        if (attrMapObject != null) {
            Iterator<String> attrKeyIt = attrMapObject.keys();
            while (attrKeyIt.hasNext()) {
                String attrKey = attrKeyIt.next();
                ele.setAttribute(attrKey, attrMapObject.getString(attrKey));
            }
        }

        Object content = eleObject.opt(ZenConstants.KEY_CONTENT);
        if (content instanceof String) {
            ele.setTextContent((String) content);
        } else if (content instanceof JSONArray) {
            JSONArray childElementArray = (JSONArray) content;
            for (Object childElementObject : childElementArray) {
                if (childElementObject instanceof JSONObject) {
                    String childName = ((JSONObject) childElementObject)
                            .optString(ZenConstants.KEY_NAME, null);
                    Assert.isNotNull(childName);
                    deserializeWorkbookExtensionElement(
                            ele.createChild(childName),
                            (JSONObject) childElementObject);
                }
            }
        } else {
            /// TODO bad file format
        }
    }

}
