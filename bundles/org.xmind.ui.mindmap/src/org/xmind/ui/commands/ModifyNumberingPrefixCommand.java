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

import org.xmind.core.ITopic;
import org.xmind.gef.ISourceProvider;
import org.xmind.gef.command.ModifyCommand;

public class ModifyNumberingPrefixCommand extends ModifyCommand {

    public ModifyNumberingPrefixCommand(ITopic topic, String newPrefix) {
        super(topic, newPrefix);
    }

    public ModifyNumberingPrefixCommand(Collection<? extends ITopic> topics,
            String newPrefix) {
        super(topics, newPrefix);
    }

    public ModifyNumberingPrefixCommand(ISourceProvider topicProvider,
            String newPrefix) {
        super(topicProvider, newPrefix);
    }

    protected Object getValue(Object source) {
        if (source instanceof ITopic) {
            return ((ITopic) source).getNumbering().getPrefix();
        }
        return null;
    }

    protected void setValue(Object source, Object value) {
        if (source instanceof ITopic) {
            ITopic topic = (ITopic) source;
            if (value instanceof String) {
                topic.getNumbering().setPrefix((String) value);
            } else if (value == null) {
                topic.getNumbering().setPrefix(null);
            }
        }
    }

}