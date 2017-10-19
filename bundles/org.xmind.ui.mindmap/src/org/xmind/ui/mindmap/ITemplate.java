package org.xmind.ui.mindmap;

import java.net.URI;

/**
 * 
 * @author Frank Shaka
 * @since 3.6.50
 */
public interface ITemplate {

    /**
     * 
     * @return
     */
    String getName();

    /**
     * 
     * @return
     */
    URI getSourceWorkbookURI();

    /**
     * 
     * @return
     */
    IWorkbookRef createWorkbookRef();

}
