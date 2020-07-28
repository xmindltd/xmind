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

public class ModifyNumberingSuffixCommand extends ModifyCommand {

    public ModifyNumberingSuffixCommand(ITopic topic, String newSuffix) {
        super(topic, newSuffix);
    }

    public ModifyNumberingSuffixCommand(Collection<? extends ITopic> topics,
            String newSuffix) {
        super(topics, newSuffix);
    }

    public ModifyNumberingSuffixCommand(ISourceProvider topicProvider,
            String newSuffix) {
        super(topicProvider, newSuffix);
    }

    protected Object getValue(Object source) {
        if (source instanceof ITopic) {
            return ((ITopic) source).getNumbering().getSuffix();
        }
        return null;
    }

    protected void setValue(Object source, Object value) {
        if (source instanceof ITopic) {
            ITopic topic = (ITopic) source;
            if (value instanceof String) {
                topic.getNumbering().setSuffix((String) value);
            } else if (value == null) {
                topic.getNumbering().setSuffix(null);
            }
        }
    }

}