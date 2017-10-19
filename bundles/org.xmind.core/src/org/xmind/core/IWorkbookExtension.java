package org.xmind.core;

import java.util.List;

/**
 * @author Jason Wong
 */
public interface IWorkbookExtension extends IAdaptable, IWorkbookComponent {

    String getProviderName();

    IWorkbookExtensionElement getContent();

    List<IResourceRef> getResourceRefs();

    void addResourceRef(IResourceRef ref);

    void removeResourceRef(IResourceRef ref);

    IResourceRef getResourceRef(String resourceId);

}
