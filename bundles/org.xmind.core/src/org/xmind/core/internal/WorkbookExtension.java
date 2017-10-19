package org.xmind.core.internal;

import java.util.Collections;
import java.util.List;

import org.xmind.core.IResourceRef;
import org.xmind.core.IWorkbook;
import org.xmind.core.IWorkbookExtension;

/**
 * @author Jason Wong
 */
public abstract class WorkbookExtension implements IWorkbookExtension {

    public <T> T getAdapter(Class<T> adapter) {
        if (IWorkbook.class.equals(adapter))
            return adapter.cast(getOwnedWorkbook());
        return null;
    }

    protected static final List<IResourceRef> EMPTY_REFS = Collections
            .emptyList();

}
