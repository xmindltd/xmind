package org.commonmark.renderer.markdown;

import org.commonmark.node.Node;

public interface MarkDownContentNodeRendererContext {

    /**
     * @return the writer to use
     */
    MarkDownContentWriter getWriter();
	
    void render(Node node);
}
