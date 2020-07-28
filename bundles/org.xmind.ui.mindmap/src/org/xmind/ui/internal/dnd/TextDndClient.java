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
package org.xmind.ui.internal.dnd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.gef.IViewer;
import org.xmind.gef.Request;
import org.xmind.ui.util.MindMapUtils;

public class TextDndClient extends MindMapDNDClientBase {

    private static final String LINE_DELIMITER = System
            .getProperty("line.separator"); //$NON-NLS-1$

    private TextTransfer transfer = TextTransfer.getInstance();

    public Object toTransferData(Object[] viewerElements, IViewer viewer) {
        StringBuilder sb = new StringBuilder(viewerElements.length * 10);
        for (Object element : viewerElements) {
            append(sb, element);
        }
        if (sb.length() == 0)
            sb.append(' ');
        return sb.toString();
    }

    private void append(StringBuilder sb, Object element) {
        if (element instanceof ITopic) {
            appendTopic(sb, (ITopic) element);
        } else {
            String text = toText(element);
            if (text != null) {
                if (sb.length() > 0) {
                    sb.append(LINE_DELIMITER);
                }
                sb.append(text);
            }
        }
    }

    private void appendTopic(StringBuilder sb, ITopic topic) {
        appendTopic(sb, topic, 0);
    }

    private void appendTopic(StringBuilder sb, ITopic topic, int level) {
        if (sb.length() > 0) {
            sb.append(LINE_DELIMITER);
        }
        for (int i = 0; i < level; i++) {
            sb.append('\t');
        }
        sb.append(topic.getTitleText());
        List<ITopic> children = topic.getAllChildren();
        if (!children.isEmpty()) {
            int nextLevel = level + 1;
            for (ITopic c : children) {
                appendTopic(sb, c, nextLevel);
            }
        }
    }

    private String toText(Object element) {
        return MindMapUtils.getText(element, null);
    }

    @Override
    protected Object[] toViewerElements(Object transferData, Request request,
            IWorkbook workbook, ITopic targetParent, boolean dropInParent) {
        if (transferData instanceof String) {
            String text = (String) transferData;
            if (workbook != null) {
                return buildeTopics(text, workbook);
            }
            return new Object[] { text };
        }
        return null;
    }

    private Object[] buildeTopics(String text, IWorkbook wb) {
        String[] lines = text.split("\\r\\n|\\r|\\n|\u2028|\u2029"); //$NON-NLS-1$
        ArrayList<ITopic> topics = new ArrayList<ITopic>(lines.length);
        HashMap<ITopic, Integer> map = new HashMap<ITopic, Integer>();
        ITopic lastTopic = null;
        int topLevel = -1;
        for (String line : lines) {
            ITopic topic = wb.createTopic();

            int level = 0;
            for (int i = 0; i < line.length(); i++) {
                char c = line.charAt(i);
                if (c == '\t') {
                    level++;
                } else {
                    break;
                }
            }
            String title = line.substring(level);
            topic.setTitleText(title);

            if (topLevel < 0 || level <= topLevel || lastTopic == null) {
                topLevel = level;
                topics.add(topic);
            } else {
                int lastLevel = map.get(lastTopic);
                ITopic parent = null;
                if (level > lastLevel) {
                    parent = lastTopic;
                } else {
                    while (true) {
                        if (level == lastLevel) {
                            ITopic tempTopic = lastTopic.getParent();
                            if (tempTopic != null) {
                                parent = tempTopic;
                                break;
                            }
                        } else if (level < lastLevel) {
                            ITopic tempTopic = lastTopic.getParent();
                            if (tempTopic != null) {
                                lastLevel = map.get(tempTopic);
                                lastTopic = tempTopic;
                            }
                        } else { //if (level>lastLevel)
                            parent = lastTopic;
                            break;
                        }
                    }
                }
                if (parent != null) {
                    parent.add(topic);
                }
            }
            lastTopic = topic;
            map.put(topic, level);
        }
        return topics.toArray();
    }

    public Object getData(Transfer transfer, TransferData data) {
        if (transfer == this.transfer)
            return this.transfer.nativeToJava(data);
        return null;
    }

    public Transfer getTransfer() {
        return transfer;
    }

}
