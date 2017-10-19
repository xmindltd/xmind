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

public class ModifyTopicHyperlinkCommand extends ModifyCommand {

    public ModifyTopicHyperlinkCommand(ITopic source, String newValue) {
        super(source, newValue);
    }

    public ModifyTopicHyperlinkCommand(Collection<? extends ITopic> sources,
            String newValue) {
        super(sources, newValue);
    }

    public ModifyTopicHyperlinkCommand(ISourceProvider sourceProvider,
            String newValue) {
        super(sourceProvider, newValue);
    }

    protected Object getValue(Object source) {
        if (source instanceof ITopic)
            return ((ITopic) source).getHyperlink();
        return null;
    }

    protected void setValue(Object source, Object value) {
        if (source instanceof ITopic
                && (value == null || value instanceof String)) {
            ITopic topic = (ITopic) source;
            String href = (String) value;
            topic.setHyperlink(href);
        }
    }
}