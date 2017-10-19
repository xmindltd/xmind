package org.xmind.ui.internal.e4models;

import org.eclipse.e4.core.contexts.IEclipseContext;

public interface IContextRunnable extends Runnable {

    default boolean canExecute(IEclipseContext context, String contextKey) {
        return true;
    }

}
