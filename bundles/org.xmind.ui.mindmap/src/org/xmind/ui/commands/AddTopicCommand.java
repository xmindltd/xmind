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
import org.xmind.core.ITopic;
import org.xmind.gef.ArraySourceProvider;
import org.xmind.gef.ISourceProvider;
import org.xmind.gef.command.SourceCommand;

public class AddTopicCommand extends SourceCommand {

    private ISourceProvider parentProvider;

    private int index;

    private String type;

    public AddTopicCommand(ISourceProvider childProvider, ITopic parent) {
        this(childProvider, parent, -1, ITopic.ATTACHED);
    }

    public AddTopicCommand(ISourceProvider childProvider, ITopic parent,
            int index, String type) {
        super(childProvider);
        Assert.isNotNull(parent);
        this.parentProvider = new ArraySourceProvider(parent);
        this.index = index;
        this.type = type;
    }

    public AddTopicCommand(ITopic child, ITopic parent) {
        this(child, parent, -1, ITopic.ATTACHED);
    }

    public AddTopicCommand(ITopic child, ITopic parent, int index, String type) {
        super(child);
        Assert.isNotNull(parent);
        this.parentProvider = new ArraySourceProvider(parent);
        this.index = index;
        this.type = type;
    }

    public AddTopicCommand(ISourceProvider childProvider,
            ISourceProvider parentProvider) {
        this(childProvider, parentProvider, -1, ITopic.ATTACHED);
    }

    public AddTopicCommand(ISourceProvider childProvider,
            ISourceProvider parentProvider, int index, String type) {
        super(childProvider);
        Assert.isNotNull(parentProvider);
        Assert.isLegal(parentProvider.hasSource());
        this.parentProvider = parentProvider;
        this.index = index;
        this.type = type;
    }

    public AddTopicCommand(ITopic child, ISourceProvider parentProvider) {
        this(child, parentProvider, -1, ITopic.ATTACHED);
    }

    public AddTopicCommand(ITopic child, ISourceProvider parentProvider,
            int index, String type) {
        super(child);
        Assert.isNotNull(parentProvider);
        Assert.isLegal(parentProvider.hasSource());
        this.parentProvider = parentProvider;
        this.index = index;
        this.type = type;
    }

    public void redo() {
        int i = index;
        Object o = parentProvider.getSource();
        if (o instanceof ITopic) {
            ITopic parent = (ITopic) o;
            for (Object source : getSources()) {
                if (source instanceof ITopic) {
                    parent.add((ITopic) source, i, type);
                    if (i >= 0)
                        i++;
                }
            }
        }
        super.redo();
    }

    public void undo() {
        List<Object> sources = getSources();
        Object o = parentProvider.getSource();
        if (o instanceof ITopic) {
            ITopic parent = (ITopic) o;
            for (int i = sources.size() - 1; i >= 0; i--) {
                Object source = sources.get(i);
                if (source instanceof ITopic) {
                    parent.remove((ITopic) source);
                }
            }
        }
        super.undo();
    }

}