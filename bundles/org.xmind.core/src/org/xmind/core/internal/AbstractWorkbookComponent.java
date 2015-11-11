package org.xmind.core.internal;

import org.xmind.core.IAdaptable;
import org.xmind.core.IWorkbook;
import org.xmind.core.IWorkbookComponent;

public abstract class AbstractWorkbookComponent
        implements IAdaptable, IWorkbookComponent {

    public Object getAdapter(Class adapter) {
        if (adapter == IWorkbook.class)
            return getOwnedWorkbook();

        return null;
    }

}
