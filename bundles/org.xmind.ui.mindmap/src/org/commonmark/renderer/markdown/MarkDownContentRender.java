package org.commonmark.renderer.markdown;

import java.util.ArrayList;
import java.util.List;

import org.commonmark.internal.renderer.NodeRendererMap;
import org.commonmark.node.Node;
import org.commonmark.renderer.NodeRenderer;
import org.commonmark.renderer.Renderer;
import org.commonmark.renderer.text.TextContentRenderer;

public class MarkDownContentRender implements Renderer{

	
	  private final List<MarkDownContentNodeRendererFactory> nodeRendererFactories;

	  private MarkDownContentRender(Builder builder) 
	  {
	      this.nodeRendererFactories = new ArrayList<MarkDownContentNodeRendererFactory>(builder.nodeRendererFactories.size() + 1);
	      this.nodeRendererFactories.addAll(builder.nodeRendererFactories);
	  
	      this.nodeRendererFactories.add(new MarkDownContentNodeRendererFactory() {

				public NodeRenderer create(MarkDownContentNodeRendererContext context) {
					 return new CoreMarkDownContentNodeRender(context);
				}
	        });
	  }
	  
	  public static Builder builder() {
	        return new Builder();
	    }
	  
	public void render(Node node, Appendable output) {
		RendererContext context = new RendererContext(new MarkDownContentWriter(output));
		context.render(node);
	}

	public String render(Node node) {

		StringBuilder sb = new StringBuilder();
        render(node, sb);
        return sb.toString();
	}
	
	  public static class Builder
	  {
	      private List<MarkDownContentNodeRendererFactory> nodeRendererFactories = new ArrayList<MarkDownContentNodeRendererFactory>();

	      /**
	         * @return the configured {@link TextContentRenderer}
	         */
	        public MarkDownContentRender build() {
	            return new MarkDownContentRender(this);
	        }
	        
	        public Builder nodeRendererFactory(MarkDownContentNodeRendererFactory nodeRendererFactory) {
	            this.nodeRendererFactories.add(nodeRendererFactory);
	            return this;
	        }
	        
	        
	  }
	  
	  private class RendererContext implements MarkDownContentNodeRendererContext {
	        private final MarkDownContentWriter markDownContentWriter;
	        private final NodeRendererMap nodeRendererMap = new NodeRendererMap();

	        private RendererContext(MarkDownContentWriter markDownContentWriter) {
	            this.markDownContentWriter = markDownContentWriter;

	            // The first node renderer for a node type "wins".
	            for (int i = nodeRendererFactories.size() - 1; i >= 0; i--) {
	            	MarkDownContentNodeRendererFactory nodeRendererFactory = nodeRendererFactories.get(i);
	                NodeRenderer nodeRenderer = nodeRendererFactory.create(this);
	                nodeRendererMap.add(nodeRenderer);
	            }
	        }

			public MarkDownContentWriter getWriter() {
				return markDownContentWriter;
			}

			public void render(Node node) {
				nodeRendererMap.render(node);
			}

	   

	    }
	
	
	



}
