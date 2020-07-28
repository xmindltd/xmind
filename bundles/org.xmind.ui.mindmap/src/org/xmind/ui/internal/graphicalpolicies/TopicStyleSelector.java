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
package org.xmind.ui.internal.graphicalpolicies;

import org.xmind.gef.graphicalpolicy.IStyleSelector;
import org.xmind.gef.graphicalpolicy.IStyleValueProvider;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.style.MindMapStyleSelectorBase;
import org.xmind.ui.style.Styles;

public class TopicStyleSelector extends MindMapStyleSelectorBase {

    public String getStyleValue(IGraphicalPart part, String key,
            IStyleValueProvider defaultValueProvider) {
        if (part instanceof ITopicPart) {
            IBranchPart branch = ((ITopicPart) part).getOwnerBranch();
            if (branch != null) {
                return branch.getBranchPolicy().getStyleSelector(branch)
                        .getStyleValue(branch, key, defaultValueProvider);
            }
        }
        return super.getStyleValue(part, key, defaultValueProvider);
    }

    public String getAutoValue(IGraphicalPart part, String key,
            IStyleValueProvider defaultValueProvider) {
        if (part instanceof ITopicPart) {
            IBranchPart branch = ((ITopicPart) part).getOwnerBranch();
            if (branch != null) {
                return branch.getBranchPolicy().getStyleSelector(branch)
                        .getAutoValue(branch, key, defaultValueProvider);
            }
        }
        return super.getAutoValue(part, key, defaultValueProvider);
    }

    public String getUserValue(IGraphicalPart part, String key) {
        if (part instanceof ITopicPart) {
            IBranchPart branch = ((ITopicPart) part).getOwnerBranch();
            if (branch != null) {
                return branch.getBranchPolicy().getStyleSelector(branch)
                        .getUserValue(branch, key);
            }
        }
        return super.getUserValue(part, key);
    }

    public String getFamilyName(IGraphicalPart part) {
        if (part instanceof ITopicPart) {
            IBranchPart branch = ((ITopicPart) part).getOwnerBranch();
            if (branch != null) {
                IStyleSelector ss = branch.getBranchPolicy().getStyleSelector(
                        branch);
                if (ss instanceof MindMapStyleSelectorBase) {
                    return ((MindMapStyleSelectorBase) ss)
                            .getFamilyName(branch);
                }
            }
        }
        return Styles.FAMILY_SUB_TOPIC;
    }

}