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
package org.xmind.ui.commands;

import java.util.Collection;

import org.xmind.core.ITopic;
import org.xmind.gef.ISourceProvider;
import org.xmind.gef.command.ModifyCommand;

public class ModifyTopicTitleWidthCommand extends ModifyCommand {

    public ModifyTopicTitleWidthCommand(ITopic topic, int newWidth) {
        super(topic, newWidth < 0 ? null : Integer.valueOf(newWidth));
    }

    public ModifyTopicTitleWidthCommand(Collection<? extends ITopic> topics,
            int newWidth) {
        super(topics, newWidth < 0 ? null : Integer.valueOf(newWidth));
    }

    public ModifyTopicTitleWidthCommand(ISourceProvider topicProvider,
            int newWidth) {
        super(topicProvider, newWidth < 0 ? null : Integer.valueOf(newWidth));
    }

    protected Object getValue(Object source) {
        if (source instanceof ITopic) {
            int width = ((ITopic) source).getTitleWidth();
            if (width != ITopic.UNSPECIFIED)
                return Integer.valueOf(width);
        }
        return null;
    }

    protected void setValue(Object source, Object value) {
        if (source instanceof ITopic) {
            ITopic t = (ITopic) source;
            if (value == null || value instanceof Integer) {
                if (value == null) {
                    t.setTitleWidth(ITopic.UNSPECIFIED);
                } else {
                    t.setTitleWidth(((Integer) value).intValue());
                }
            }
        }
    }
}