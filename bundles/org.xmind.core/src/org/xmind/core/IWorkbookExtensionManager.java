package org.xmind.core;

import java.util.List;

/**
 * @author Jason Wong
 */
public interface IWorkbookExtensionManager extends IWorkbookComponent {

    List<IWorkbookExtension> getExtensions();

    IWorkbookExtension getExtension(String providerName);

    IWorkbookExtension createExtension(String providerName);

    void deleteExtension(String providerName);

    List<String> getProviders();

}
