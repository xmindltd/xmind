package org.xmind.core.internal;

import java.util.List;

import org.xmind.core.IWorkbookExtensionElement;

/**
 * @author Jason Wong
 */
public abstract class WorkbookExtensionElement extends AbstractWorkbookComponent
        implements IWorkbookExtensionElement {

    public IWorkbookExtensionElement getCreatedChild(String elementName) {
        List<IWorkbookExtensionElement> children = getChildren(elementName);
        if (!children.isEmpty())
            return children.get(0);
        return createChild(elementName);
    }

}
