package org.xmind.gef.ui.internal;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.xmind.gef.IViewer;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.gef.part.IPart;

public class ModelToPartAdapterFactory implements IAdapterFactory {

    public Class<?>[] getAdapterList() {
        return new Class<?>[] { IPart.class, IGraphicalPart.class };
    }

    public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
        if (isSubclass(adapterType, IPart.class)) {
            IViewer viewer = findViewer();
            if (viewer != null) {
                IPart part = viewer.getPartRegistry()
                        .getPartByModel(adaptableObject);
                if (adapterType.isInstance(part))
                    return adapterType.cast(part);
            }
        }
        return null;
    }

    private static IViewer findViewer() {
        if (PlatformUI.isWorkbenchRunning()) {
            IWorkbenchWindow window = PlatformUI.getWorkbench()
                    .getActiveWorkbenchWindow();
            if (window != null) {
                IWorkbenchPart part = window.getPartService().getActivePart();
                if (part != null) {
                    return GEFPlugin.getAdapter(part, IViewer.class);
                }
            }
        }
        return null;
    }

    private static boolean isSubclass(Class<?> cls, Class<?> superCls) {
        if (cls == null)
            return false;

        if (cls.equals(superCls))
            return true;

        if (superCls.isInterface()) {
            Class<?>[] interfaces = cls.getInterfaces();
            for (int i = 0; i < interfaces.length; i++) {
                if (isSubclass(interfaces[i], superCls))
                    return true;
            }
        }

        return isSubclass(cls.getSuperclass(), superCls);
    }

}
