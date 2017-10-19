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

import org.eclipse.core.runtime.Assert;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.gef.command.CreateCommand;

public class CreateTopicCommand extends CreateCommand {

    private IWorkbook workbook;

    private ITopic topic;

    public CreateTopicCommand(IWorkbook workbook) {
        Assert.isNotNull(workbook);
        this.workbook = workbook;
    }

    public boolean canCreate() {
        if (topic == null) {
            topic = workbook.createTopic();
        }
        return topic != null;
    }

    protected Object create() {
        canCreate();
        return topic;
    }

}