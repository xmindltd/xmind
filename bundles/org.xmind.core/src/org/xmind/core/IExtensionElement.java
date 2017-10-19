package org.xmind.core;

import java.util.List;
import java.util.Set;

/**
 * @author Jason Wong
 */
public interface IExtensionElement<T extends IExtensionElement> {

    List<T> getChildren();

    List<T> getChildren(String elementName);

    T createChild(String elementName);

    T getCreatedChild(String elementName);

    T getFirstChild(String elementName);

    T getParent();

    void addChild(T child, int index);

    void deleteChild(T child);

    void deleteChildren(String elementName);

    void deleteChildren();

    String getName();

    Set<String> getAttributeKeys();

    String getAttribute(String attrName);

    void setAttribute(String attrName, String attrValue);

    String getTextContent();

    void setTextContent(String text);

}
