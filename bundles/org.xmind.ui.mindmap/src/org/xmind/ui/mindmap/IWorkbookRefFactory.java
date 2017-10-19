package org.xmind.ui.mindmap;

import java.net.URI;

import org.eclipse.ui.IMemento;

/**
 * 
 * @author Frank Shaka
 * @since 3.6.50
 */
public interface IWorkbookRefFactory {

    /**
     * 
     * @param uri
     * @param state
     * @return
     */
    IWorkbookRef createWorkbookRef(URI uri, IMemento state);

}
