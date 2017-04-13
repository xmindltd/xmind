package org.commonmark.renderer.markdown;

import org.commonmark.renderer.NodeRenderer;

public interface MarkDownContentNodeRendererFactory {

	  NodeRenderer create(MarkDownContentNodeRendererContext context);
	
}
