package org.xmind.ui.internal;

import org.eclipse.ui.IActionFilter;
import org.xmind.core.INamed;
import org.xmind.core.ITitled;
import org.xmind.core.ITopic;
import org.xmind.core.util.HyperlinkUtils;

/**
 * 
 * @author Frank Shaka
 * @deprecated
 */
public class ElementActionFilter implements IActionFilter {

    private static final ElementActionFilter instance = new ElementActionFilter();

    private ElementActionFilter() {
    }

    public boolean testAttribute(Object target, String name, String value) {
        if ("".equals(value)) //$NON-NLS-1$
            value = null;
        String targetValue = getValue(target, name);
        return targetValue == value
                || (targetValue != null && targetValue.equals(value));
    }

    private String getValue(Object target, String name) {
        if ("title".equals(name)) { //$NON-NLS-1$
            if (target instanceof ITitled)
                return ((ITitled) target).getTitleText();
        } else if ("name".equals(name)) { //$NON-NLS-1$
            if (target instanceof INamed)
                return ((INamed) target).getName();
        } else if ("hyperlinkType".equals(name)) { //$NON-NLS-1$
            if (target instanceof ITopic) {
                ITopic topic = (ITopic) target;
                String hyperlink = topic.getHyperlink();
                if (hyperlink == null || "".equals(hyperlink)) //$NON-NLS-1$
                    return "none"; //$NON-NLS-1$
                if (HyperlinkUtils.isAttachmentURL(hyperlink))
                    return "attachment"; //$NON-NLS-1$
                if (HyperlinkUtils.isInternalURL(hyperlink))
                    return "internal"; //$NON-NLS-1$
                if (hyperlink.startsWith("file:")) //$NON-NLS-1$
                    return "file"; //$NON-NLS-1$
                return "unknown"; //$NON-NLS-1$
            }
        } else if ("topicType".equals(name)) { //$NON-NLS-1$
            if (target instanceof ITopic)
                return ((ITopic) target).getType();
        }
        return null;
    }

    public static ElementActionFilter getInstance() {
        return instance;
    }

}
