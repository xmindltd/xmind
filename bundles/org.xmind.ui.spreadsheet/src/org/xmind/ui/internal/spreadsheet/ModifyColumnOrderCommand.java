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
package org.xmind.ui.internal.spreadsheet;

import java.util.List;

import org.xmind.core.ITopic;
import org.xmind.core.ITopicExtension;
import org.xmind.core.ITopicExtensionElement;
import org.xmind.core.event.ICoreEventSource;
import org.xmind.gef.command.ModifyCommand;
import org.xmind.ui.internal.spreadsheet.structures.ColumnHead;
import org.xmind.ui.internal.spreadsheet.structures.ColumnOrder;

public class ModifyColumnOrderCommand extends ModifyCommand {

    public ModifyColumnOrderCommand(ITopic topic, ColumnOrder newColumnOrder) {
        super(topic, newColumnOrder);
    }

    protected Object getValue(Object source) {
        if (source instanceof ITopic)
            return ColumnOrder.createFromTopic((ITopic) source);
        return null;
    }

    protected void setValue(Object source, Object value) {
        if (source instanceof ITopic) {
            ITopic topic = (ITopic) source;
            if (value == null || value instanceof ColumnOrder) {
                ColumnOrder order = (ColumnOrder) value;
                if (order == null || order.isEmpty()) {
                    deleteColumnOrder(topic);
                } else {
                    setColumnOrder(topic, order);
                }
            }
        }
    }

    private void deleteColumnOrder(ITopic topic) {
        topic.deleteExtension(SpreadsheetUIPlugin.PLUGIN_ID);
        fireEvent(topic);
    }

    private void setColumnOrder(ITopic topic, ColumnOrder order) {
        ITopicExtension extension = topic
                .createExtension(SpreadsheetUIPlugin.PLUGIN_ID);
        ITopicExtensionElement content = extension.getContent();
        List<ITopicExtensionElement> oldValues = content
                .getChildren(Spreadsheet.TAG_COLUMNS);
        for (Object o : oldValues.toArray()) {
            content.deleteChild((ITopicExtensionElement) o);
        }
        ITopicExtensionElement columnsEle = content
                .createChild(Spreadsheet.TAG_COLUMNS);
        for (ColumnHead head : order.getHeads()) {
            ITopicExtensionElement column = columnsEle
                    .createChild(Spreadsheet.TAG_COLUMN);
            column.setTextContent(head.toString());
        }
        fireEvent(topic);
    }

    private void fireEvent(ITopic topic) {
        if (topic instanceof ICoreEventSource) {
            ICoreEventSource source = (ICoreEventSource) topic;
            source.getCoreEventSupport().dispatchTargetChange(source,
                    Spreadsheet.EVENT_MODIFY_COLUMN_ORDER, null);
        }
    }

}