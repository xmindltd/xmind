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
import org.xmind.core.IRange;
import org.xmind.core.ITopic;
import org.xmind.gef.command.SourceCommand;

public class ModifyTopicRangeCommand extends SourceCommand {

    private ITopic startTopic;

    private ITopic endTopic;

    private int startIndex;

    private int endIndex;

    private int oldStartIndex;

    private int oldEndIndex;

    public ModifyTopicRangeCommand(IRange r, ITopic startTopic, ITopic endTopic) {
        super(r);
        Assert.isNotNull(startTopic);
        Assert.isNotNull(endTopic);
        this.startTopic = startTopic;
        this.endTopic = endTopic;
        this.startIndex = -1;
        this.endIndex = -1;
    }

    public void execute() {
        this.oldStartIndex = ((IRange) getSource()).getStartIndex();
        this.oldEndIndex = ((IRange) getSource()).getEndIndex();
        this.startIndex = startTopic.getIndex();
        this.endIndex = endTopic.getIndex();
        super.execute();
    }

    public void redo() {
        ((IRange) getSource()).setStartIndex(startIndex);
        ((IRange) getSource()).setEndIndex(endIndex);
        super.redo();
    }

    public void undo() {
        ((IRange) getSource()).setStartIndex(oldStartIndex);
        ((IRange) getSource()).setEndIndex(oldEndIndex);
        super.undo();
    }

}