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
package org.xmind.ui.internal.spreadsheet.structures;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.xmind.core.ITopic;
import org.xmind.core.ITopicExtension;
import org.xmind.core.ITopicExtensionElement;
import org.xmind.ui.internal.spreadsheet.Spreadsheet;
import org.xmind.ui.internal.spreadsheet.SpreadsheetUIPlugin;

public class ColumnOrder {

    public static final ColumnOrder EMPTY = new ColumnOrder(true);

    private List<ColumnHead> heads;

    public ColumnOrder() {
        this(false);
    }

    private ColumnOrder(boolean empty) {
        if (empty) {
            heads = Collections.emptyList();
        } else {
            heads = new ArrayList<ColumnHead>();
        }
    }

    public void addColumnHead(ColumnHead head) {
        if (this != EMPTY) {
            heads.add(head);
        }
    }

    public boolean isEmpty() {
        return heads.isEmpty();
    }

    public List<ColumnHead> getHeads() {
        return heads;
    }

    public int compareColumns(ColumnHead head1, ColumnHead head2) {
        int index1 = heads.indexOf(head1);
        int index2 = heads.indexOf(head2);
        if (index1 < 0) {
            if (index2 < 0)
                return head1.compareTo(head2);
            return 1;
        }
        if (index2 < 0)
            return -1;
        return index1 - index2;
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof ColumnOrder))
            return false;
        ColumnOrder that = (ColumnOrder) obj;
        if (this.heads.size() != that.heads.size())
            return false;
        for (int i = 0; i < this.heads.size(); i++) {
            ColumnHead h1 = this.heads.get(i);
            ColumnHead h2 = that.heads.get(i);
            if (!h1.equals(h2))
                return false;
        }
        return true;
    }

    public int hashCode() {
        return heads.hashCode();
    }

    public static ColumnOrder createFromTopic(ITopic topic) {
        ITopicExtension ext = topic.getExtension(SpreadsheetUIPlugin.PLUGIN_ID);
        if (ext != null) {
            ITopicExtensionElement content = ext.getContent();
            List<ITopicExtensionElement> children = content
                    .getChildren(Spreadsheet.TAG_COLUMNS);
            if (!children.isEmpty()) {
                List<ITopicExtensionElement> columns = children.get(0)
                        .getChildren(Spreadsheet.TAG_COLUMN);
                if (!columns.isEmpty()) {
                    ColumnOrder order = new ColumnOrder();
                    for (ITopicExtensionElement col : columns) {
                        String text = col.getTextContent();
                        if (text != null) {
                            order.addColumnHead(new ColumnHead(text));
                        }
                    }
                    return order;
                }
            }
        }
        return EMPTY;
    }

}