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
import org.xmind.ui.internal.spreadsheet.structures.RowHead;
import org.xmind.ui.internal.spreadsheet.structures.RowOrder;

public class ModifyRowOrderCommand extends ModifyCommand {

    public ModifyRowOrderCommand(ITopic topic, RowOrder newRowOrder) {
        super(topic, newRowOrder);
    }

    protected Object getValue(Object source) {
        if (source instanceof ITopic)
            return RowOrder.createFromTopic((ITopic) source);
        return null;
    }

    protected void setValue(Object source, Object value) {
        if (source instanceof ITopic) {
            ITopic topic = (ITopic) source;
            if (value == null || value instanceof RowOrder) {
                RowOrder order = (RowOrder) value;
                if (order == null || order.isEmpty()) {
                    deleteRowOrder(topic);
                } else {
                    setRowOrder(topic, order);
                }
            }
        }
    }

    private void deleteRowOrder(ITopic topic) {
        topic.deleteExtension(SpreadsheetUIPlugin.PLUGIN_ID);
        fireEvent(topic);
    }

    private void setRowOrder(ITopic topic, RowOrder order) {
        ITopicExtension extension = topic
                .createExtension(SpreadsheetUIPlugin.PLUGIN_ID);
        ITopicExtensionElement content = extension.getContent();
        List<ITopicExtensionElement> oldValues = content
                .getChildren(Spreadsheet.TAG_ROWS);
        for (Object o : oldValues.toArray()) {
            content.deleteChild((ITopicExtensionElement) o);
        }
        ITopicExtensionElement rowsEle = content
                .createChild(Spreadsheet.TAG_ROWS);
        for (RowHead head : order.getHeads()) {
            ITopicExtensionElement row = rowsEle
                    .createChild(Spreadsheet.TAG_ROW);
            row.setTextContent(head.toString());
        }
        fireEvent(topic);
    }

    private void fireEvent(ITopic topic) {
        if (topic instanceof ICoreEventSource) {
            ICoreEventSource source = (ICoreEventSource) topic;
            source.getCoreEventSupport().dispatchTargetChange(source,
                    Spreadsheet.EVENT_MODIFY_ROW_ORDER, null);
        }
    }

}