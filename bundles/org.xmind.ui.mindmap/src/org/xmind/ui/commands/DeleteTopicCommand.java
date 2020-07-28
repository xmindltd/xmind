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

import org.eclipse.core.runtime.Assert;
import org.xmind.core.ITopic;
import org.xmind.gef.GEF;
import org.xmind.gef.command.SourceCommand;

public class DeleteTopicCommand extends SourceCommand {

    private ITopic parent;

    private int index;

    private String type;

    public DeleteTopicCommand(ITopic topic) {
        super(topic);
        ITopic parent = topic.getParent();
        Assert.isNotNull(parent);
        this.parent = parent;
    }

    public int getType() {
        return GEF.CMD_DELETE;
    }

    public void execute() {
        index = ((ITopic) getSource()).getIndex();
        type = ((ITopic) getSource()).getType();
        super.execute();
    }

    public void redo() {
        parent.remove((ITopic) getSource());
        super.redo();
    }

    public void undo() {
        parent.add((ITopic) getSource(), index, type);
        super.undo();
    }

}