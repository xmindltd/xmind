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

import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.xmind.core.ISummary;
import org.xmind.core.ITopic;
import org.xmind.gef.ArraySourceProvider;
import org.xmind.gef.ISourceProvider;
import org.xmind.gef.command.SourceCommand;

public class AddSummaryCommand extends SourceCommand {

    private ISourceProvider parentProvider;

    public AddSummaryCommand(ISummary summary, ITopic parent) {
        super(summary);
        Assert.isNotNull(parent);
        this.parentProvider = new ArraySourceProvider(parent);
    }

    public AddSummaryCommand(ISourceProvider summaryProvider, ITopic parent) {
        super(summaryProvider);
        Assert.isNotNull(parent);
        this.parentProvider = new ArraySourceProvider(parent);
    }

    public AddSummaryCommand(ISourceProvider summaryProvider,
            ISourceProvider parentProvider) {
        super(summaryProvider);
        Assert.isNotNull(parentProvider);
        this.parentProvider = parentProvider;
    }

    public void redo() {
        Object p = parentProvider.getSource();
        if (p instanceof ITopic) {
            ITopic parent = (ITopic) p;
            for (Object source : getSources()) {
                if (source instanceof ISummary) {
                    parent.addSummary((ISummary) source);
                }
            }
        }
        super.redo();
    }

    public void undo() {
        Object p = parentProvider.getSource();
        if (p instanceof ITopic) {
            ITopic parent = (ITopic) p;
            List<Object> sources = getSources();
            for (int i = sources.size() - 1; i >= 0; i--) {
                Object source = sources.get(i);
                if (source instanceof ISummary) {
                    parent.removeSummary((ISummary) source);
                }
            }
        }
        super.undo();
    }
}