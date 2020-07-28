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
package org.xmind.ui.internal.spreadsheet;

import org.xmind.core.internal.UserDataConstants;
import org.xmind.ui.branch.IBranchPolicy;
import org.xmind.ui.branch.IBranchPolicyAdvisor;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.ITopicPart;

public class SpreadsheetPolicyAdvisor implements IBranchPolicyAdvisor {

    public void postActivate(IBranchPart branch, IBranchPolicy policy) {
        ITopicPart topicPart = branch.getTopicPart();
        if (topicPart != null) {
            topicPart.refresh();
        }
        for (IBranchPart child : branch.getSubBranches()) {
            child.getTopicPart().refresh();
        }
        MindMapUIPlugin.getDefault().getUsageDataCollector().trackEvent(
                UserDataConstants.CATEGORY_STRUCTURE,
                UserDataConstants.USE_SPREAD_SHEET);
    }

    public void postDeactivate(IBranchPart branch, IBranchPolicy policy) {
        ITopicPart topicPart = branch.getTopicPart();
        if (topicPart != null) {
            topicPart.refresh();
        }
    }

}
