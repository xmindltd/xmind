/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See https://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.ui.commands;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.ISourceProvider;
import org.xmind.core.ITopic;
import org.xmind.core.ITopicExtension;
import org.xmind.core.ITopicExtensionElement;
import org.xmind.core.internal.UserDataConstants;
import org.xmind.gef.command.ModifyCommand;
import org.xmind.ui.internal.MindMapUIPlugin;

public class ModifyTopicStructureCommand extends ModifyCommand {

    private final static String STRUCTUREID_UNBALANCED = "org.xmind.ui.map.unbalanced"; //$NON-NLS-1$
    private final static String EXTENTION_UNBALANCEDSTRUCTURE = "org.xmind.ui.map.unbalanced"; //$NON-NLS-1$
    private final static String EXTENTIONELEMENT_RIGHTNUMBER = "right-number";//$NON-NLS-1$
    private final static String INVALID_RIGHT_NUMBER = "-1"; //$NON-NLS-1$

    private Map<ITopicExtension, String> extToRightNum = new HashMap<ITopicExtension, String>();

    public ModifyTopicStructureCommand(ITopic source,
            String newStructureClass) {
        super(source, newStructureClass);
        ITopicExtension topicExtension = source
                .getExtension(EXTENTION_UNBALANCEDSTRUCTURE);
        if (topicExtension != null) {
            String rightNum = topicExtension.getContent()
                    .getCreatedChild(EXTENTIONELEMENT_RIGHTNUMBER)
                    .getTextContent();
            extToRightNum.put(topicExtension, rightNum);
        }

    }

    public ModifyTopicStructureCommand(Collection<ITopic> sources,
            String newStructureClass) {
        super(sources, newStructureClass);
        for (ITopic topic : sources) {
            ITopicExtension extension = topic
                    .getExtension(EXTENTION_UNBALANCEDSTRUCTURE);
            if (extension != null) {
                String rightNum = extension.getContent()
                        .getCreatedChild(EXTENTIONELEMENT_RIGHTNUMBER)
                        .getTextContent();
                extToRightNum.put(extension, rightNum);
            }
        }
    }

    public ModifyTopicStructureCommand(ISourceProvider sourceProvider,
            String newStructureClass) {
        super(sourceProvider, newStructureClass);
    }

    protected Object getValue(Object source) {
        if (source instanceof ITopic) {
            return ((ITopic) source).getStructureClass();
        }
        return null;
    }

    protected void setValue(Object source, Object value) {
        if (source instanceof ITopic) {
            ITopic topic = (ITopic) source;
            if (value == null || value instanceof String) {
                String oldStructure = topic.getStructureClass();
                if (STRUCTUREID_UNBALANCED.equals(oldStructure)) {
                    ITopicExtension extension = topic
                            .createExtension(EXTENTION_UNBALANCEDSTRUCTURE);
                    ITopicExtensionElement rightNum = extension.getContent()
                            .getCreatedChild(EXTENTIONELEMENT_RIGHTNUMBER);
                    rightNum.setTextContent(INVALID_RIGHT_NUMBER);

                } else if (STRUCTUREID_UNBALANCED.equals(value)) {
                    ITopicExtension extension = topic
                            .createExtension(EXTENTION_UNBALANCEDSTRUCTURE);
                    boolean has = extToRightNum.containsKey(extension);
                    if (has) {
                        String rightNum = extToRightNum.get(extension);
                        extension.getContent()
                                .getCreatedChild(EXTENTIONELEMENT_RIGHTNUMBER)
                                .setTextContent(rightNum);
                    }
                }
                if (value != null) {
                    String vs = value.toString();
                    String ID = vs.replaceAll("\\.", "_");  //$NON-NLS-1$//$NON-NLS-2$
                    MindMapUIPlugin.getDefault().getUsageDataCollector()
                            .trackEvent(UserDataConstants.CATEGORY_STRUCTURE,
                                    String.format(
                                            UserDataConstants.USE_STRUCTURE_TYPE_S,
                                            ID));
                }
                topic.setStructureClass((String) value);
            }
        }
    }

    @Override
    public void execute() {
        MindMapUIPlugin.getDefault().getUsageDataCollector().trackEvent(
                UserDataConstants.CATEGORY_STRUCTURE,
                UserDataConstants.MODIFY_STRUCTURE);
        super.execute();
    }

}
