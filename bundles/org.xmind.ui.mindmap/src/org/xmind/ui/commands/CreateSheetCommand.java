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

import java.util.Arrays;

import org.eclipse.core.runtime.Assert;
import org.xmind.core.ICloneData;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.gef.command.CreateCommand;

public class CreateSheetCommand extends CreateCommand {

    private IWorkbook parent;

    private int index;

    private ISheet sheet;

    private ITopic topic;

    private ICloneData cloneData;

    public CreateSheetCommand(IWorkbook parent, ITopic topic) {
        this(parent, -1);
        this.topic = topic;

    }

    public CreateSheetCommand(IWorkbook parent, int index) {
        Assert.isNotNull(parent);
        Assert.isTrue(index <= parent.getSheets().size());
        this.parent = parent;
        this.index = index;
    }

    protected boolean canCreate() {
        if (sheet == null) {
            sheet = parent.createSheet();
            if (topic != null) {
                ICloneData clone = parent.clone(Arrays.asList(topic));
                cloneData = clone;
                ITopic cloneTopic = (ITopic) clone.get(topic);
                sheet.replaceRootTopic(cloneTopic);
            }
        }
        return sheet != null;
    }

    protected Object create() {
        canCreate();
        return sheet;
    }

    public void redo() {
        parent.addSheet((ISheet) getSource(), index);
        super.redo();
    }

    public void undo() {
        parent.removeSheet((ISheet) getSource());
        super.undo();
    }

    public ICloneData getCloneData() {
        return cloneData;
    }
}