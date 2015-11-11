package org.xmind.ui.internal;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.IActionFilter;
import org.xmind.core.IAdaptable;

@Deprecated
public class CoreUIAdapterFactory implements IAdapterFactory {

    private static Class[] LIST = { IActionFilter.class };

    @SuppressWarnings("unchecked")
    public Object getAdapter(Object adaptableObject, Class adapterType) {
        if (adapterType == IActionFilter.class) {
            if (adaptableObject instanceof IAdaptable) {
                return ElementActionFilter.getInstance();
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public Class[] getAdapterList() {
        return LIST;
    }

}
