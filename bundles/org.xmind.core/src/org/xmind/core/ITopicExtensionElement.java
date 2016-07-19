/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.core;

import java.util.List;
import java.util.Set;

public interface ITopicExtensionElement extends IAdaptable, IWorkbookComponent {

    ITopicExtension getExtension();

    ITopic getTopic();

    List<ITopicExtensionElement> getChildren();

    List<ITopicExtensionElement> getChildren(String elementName);

    ITopicExtensionElement createChild(String elementName);

    ITopicExtensionElement getCreatedChild(String elementName);

    ITopicExtensionElement getFirstChild(String elementName);

    ITopicExtensionElement getParent();

    void addChild(ITopicExtensionElement child, int index);

    void deleteChild(ITopicExtensionElement child);

    void deleteChildren(String elementName);

    void deleteChildren();

    String getName();

    Set<String> getAttributeKeys();

    String getAttribute(String attrName);

    void setAttribute(String attrName, String attrValue);

    String getTextContent();

    void setTextContent(String text);

}