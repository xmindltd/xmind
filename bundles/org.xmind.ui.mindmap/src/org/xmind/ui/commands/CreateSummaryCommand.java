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
import org.xmind.core.ISummary;
import org.xmind.core.IWorkbook;
import org.xmind.core.internal.UserDataConstants;
import org.xmind.gef.command.CreateCommand;
import org.xmind.ui.internal.MindMapUIPlugin;

public class CreateSummaryCommand extends CreateCommand {

    private IWorkbook workbook;

    private ISummary summary;

    public CreateSummaryCommand(IWorkbook workbook) {
        Assert.isNotNull(workbook);
        this.workbook = workbook;
    }

    protected boolean canCreate() {
        if (summary == null) {
            summary = workbook.createSummary();
        }
        return summary != null;
    }

    protected Object create() {
        canCreate();
        return summary;
    }

    @Override
    public void execute() {
        MindMapUIPlugin.getDefault().getUsageDataCollector().trackEvent(
                UserDataConstants.CATEGORY_INSERT,
                UserDataConstants.INSERT_SUMMARY);
        super.execute();
    }
}
