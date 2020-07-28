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

import org.xmind.core.ISummary;
import org.xmind.core.ITopic;
import org.xmind.gef.ISourceProvider;
import org.xmind.gef.command.ModifyCommand;

public class ModifySummaryTopicCommand extends ModifyCommand {

    public ModifySummaryTopicCommand(ISummary summary, String newTopicId) {
        super(summary, newTopicId);
    }

    public ModifySummaryTopicCommand(ISourceProvider summaryProvider,
            String newTopicId) {
        super(summaryProvider, newTopicId);
    }

    public ModifySummaryTopicCommand(ISummary summary, ITopic newTopic) {
        super(summary, newTopic);
    }

    public ModifySummaryTopicCommand(ISummary summary,
            ISourceProvider newTopicProvider) {
        super(summary, newTopicProvider);
    }

    public ModifySummaryTopicCommand(ISourceProvider summaryProvider,
            ITopic newTopic) {
        super(summaryProvider, newTopic);
    }

    public ModifySummaryTopicCommand(ISourceProvider summaryProvider,
            ISourceProvider newTopicProvider) {
        super(summaryProvider, newTopicProvider);
    }

    protected Object getValue(Object source) {
        if (source instanceof ISummary)
            return ((ISummary) source).getTopicId();
        return null;
    }

    protected void setValue(Object source, Object value) {
        if (source instanceof ISummary) {
            if (value == null) {
                ((ISummary) source).setTopicId(null);
            } else if (value instanceof String) {
                ((ISummary) source).setTopicId((String) value);
            } else if (value instanceof ITopic) {
                ((ISummary) source).setTopicId(((ITopic) value).getId());
            } else if (value instanceof ISourceProvider) {
                ISourceProvider topicProvider = (ISourceProvider) value;
                if (topicProvider.hasSource()) {
                    Object topic = topicProvider.getSource();
                    if (topic instanceof ITopic) {
                        ((ISummary) source)
                                .setTopicId(((ITopic) topic).getId());
                    }
                }
            }
        }
    }

}