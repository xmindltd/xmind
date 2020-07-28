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
package org.xmind.ui.internal.tools;

import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.gef.part.IPart;
import org.xmind.gef.service.IFeedback;
import org.xmind.gef.service.IFeedbackService;
import org.xmind.gef.service.IRectangleProvider;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.tools.FeedbackAreaSelectTool;

public class TopicAreaSelectTool extends FeedbackAreaSelectTool implements
        IRectangleProvider {

    private IFeedback feedback = null;

    protected void addFeedback(IFeedbackService feedbackService) {
        super.addFeedback(feedbackService);
        if (feedback == null) {
            feedback = MindMapFeedbackFactory.createAreaSelectFeedback(this);
        }
        feedbackService.addFeedback(feedback);
    }

    protected void removeFeedback(IFeedbackService feedbackService) {
        if (feedback != null) {
            feedbackService.removeFeedback(feedback);
            feedback = null;
        }
        super.removeFeedback(feedbackService);
    }

    protected void updateFeedback(IFeedbackService feedbackService) {
        super.updateFeedback(feedbackService);
        if (feedback != null) {
            feedback.update();
        }
    }

    protected void areaSelect(IPart part, Rectangle area) {
        if (part instanceof IBranchPart) {
            IBranchPart branch = (IBranchPart) part;
            select(branch.getTopicPart(), area);
            if (branch.canSearchChild()) {
                for (IBranchPart subBranch : branch.getSubBranches()) {
                    areaSelect(subBranch, area);
                }
                for (IBranchPart calloutBranch : branch.getCalloutBranches()) {
                    areaSelect(calloutBranch, area);
                }
                for (IBranchPart summaryBranch : branch.getSummaryBranches()) {
                    areaSelect(summaryBranch, area);
                }
            }
        } else {
            for (IPart child : part.getChildren()) {
                areaSelect(child, area);
            }
        }
    }

    public Rectangle getRectangle() {
        return getResult();
    }

}