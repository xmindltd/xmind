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

import org.eclipse.core.runtime.Assert;
import org.xmind.core.INotesContent;
import org.xmind.core.ITopic;
import org.xmind.gef.ISourceProvider;
import org.xmind.gef.command.ModifyCommand;

public class ModifyNotesCommand extends ModifyCommand {

    private String format;

    public ModifyNotesCommand(Collection<? extends ITopic> topics,
            INotesContent newValue, String format) {
        super(topics, newValue);
        Assert.isNotNull(format);
        this.format = format;
    }

    public ModifyNotesCommand(ISourceProvider topicProvider,
            INotesContent newValue, String format) {
        super(topicProvider, newValue);
        Assert.isNotNull(format);
        this.format = format;
    }

    public ModifyNotesCommand(ITopic topic, INotesContent newValue,
            String format) {
        super(topic, newValue);
        Assert.isNotNull(format);
        this.format = format;
    }

    protected Object getValue(Object source) {
        if (source instanceof ITopic) {
            ITopic topic = (ITopic) source;
            return topic.getNotes().getContent(format);
        }
        return null;
    }

    protected void setValue(Object source, Object value) {
        if (source instanceof ITopic) {
            ITopic topic = (ITopic) source;
            if (value instanceof INotesContent) {
                topic.getNotes().setContent(format, (INotesContent) value);
            } else if (value == null) {
                topic.getNotes().setContent(format, null);
            }
        }
    }

}