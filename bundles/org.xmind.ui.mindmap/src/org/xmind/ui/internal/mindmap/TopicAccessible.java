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
package org.xmind.ui.internal.mindmap;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.accessibility.ACC;
import org.xmind.gef.acc.AbstractGraphicalAccessible;
import org.xmind.gef.part.IPart;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.ITopicPart;

public class TopicAccessible extends AbstractGraphicalAccessible {

    public TopicAccessible(ITopicPart host) {
        super(host);
    }

    public ITopicPart getHost() {
        return (ITopicPart) super.getHost();
    }

    public String getName() {
        return getHost().getTopic().getTitleText();
    }

    @Override
    protected List<? extends IPart> getChildrenParts() {
        IPart parent = getHost().getParent();
        if (parent instanceof IBranchPart) {
            List<IBranchPart> subBranches = ((IBranchPart) parent)
                    .getSubBranches();
            ArrayList<IPart> subTopics = new ArrayList<IPart>(subBranches
                    .size());
            for (IBranchPart branch : subBranches) {
                subTopics.add(branch.getTopicPart());
            }
            return subTopics;
        }
        return super.getChildrenParts();
    }

    @Override
    public int getRole() {
        return ACC.ROLE_CHECKBUTTON;
    }
}