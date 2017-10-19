package org.xmind.core.internal;

import org.xmind.core.IAdaptable;
import org.xmind.core.IWorkbook;
import org.xmind.core.IWorkbookComponent;

public abstract class AbstractWorkbookComponent
        implements IAdaptable, IWorkbookComponent {

    public <T> T getAdapter(Class<T> adapter) {
        if (adapter == IWorkbook.class)
            return adapter.cast(getOwnedWorkbook());

        return null;
    }

}
