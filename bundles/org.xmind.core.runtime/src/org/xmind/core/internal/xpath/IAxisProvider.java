package org.xmind.core.internal.xpath;

import java.util.List;

public interface IAxisProvider {

    Object getAttribute(Object node, String name);

    List<?> getChildNodes(Object node, String name);

    Object getParentNode(Object node);

    String getTextContent(Object node);

}
