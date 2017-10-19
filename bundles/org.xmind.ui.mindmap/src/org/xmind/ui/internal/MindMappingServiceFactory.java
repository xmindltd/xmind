package org.xmind.ui.internal;

import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.services.AbstractServiceFactory;
import org.eclipse.ui.services.IServiceLocator;
import org.xmind.ui.editor.IEditorHistory;
import org.xmind.ui.internal.editor.EditorHistoryImpl;
import org.xmind.ui.internal.editor.EditorHistoryPersistenceHelper;
import org.xmind.ui.internal.editor.EditorHistoryProxy;

/**
 * 
 * @author Frank Shaka
 * @since 3.6.50
 */
public class MindMappingServiceFactory extends AbstractServiceFactory {

    public MindMappingServiceFactory() {
    }

    @Override
    public Object create(Class serviceInterface, IServiceLocator parentLocator,
            IServiceLocator locator) {
        if (IEditorHistory.class.equals(serviceInterface)) {
            return createEditorHistoryService(IEditorHistory.class,
                    parentLocator, locator);
        }
        return null;
    }

    private IEditorHistory createEditorHistoryService(
            Class<IEditorHistory> serviceInterface,
            IServiceLocator parentLocator, IServiceLocator locator) {
        IEditorHistory parentService = parentLocator == null ? null
                : parentLocator.getService(serviceInterface);
        if (parentService == null) {
            return createMasterEditorHistory(locator);
        } else {
            return new EditorHistoryProxy(parentService);
        }
    }

    /**
     * @return
     */
    private IEditorHistory createMasterEditorHistory(IServiceLocator locator) {
        final IPath basePath = MindMapUIPlugin.getDefault().getStateLocation();
        final EditorHistoryPersistenceHelper loader = new EditorHistoryPersistenceHelper(
                basePath);
        final EditorHistoryImpl service = new EditorHistoryImpl(loader);
        loader.setService(service);
        return service;
    }

}
