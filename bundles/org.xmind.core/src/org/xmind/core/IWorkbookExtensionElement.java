package org.xmind.core;

/**
 * @author Jason Wong
 */
public interface IWorkbookExtensionElement extends IAdaptable,
        IWorkbookComponent, IExtensionElement<IWorkbookExtensionElement> {

    IWorkbookExtension getExtension();

    void setResourcePath(String resourcePath);

    String getResourcePath();

    void setObjectId(String objectId);

    String getObjectId();

}
