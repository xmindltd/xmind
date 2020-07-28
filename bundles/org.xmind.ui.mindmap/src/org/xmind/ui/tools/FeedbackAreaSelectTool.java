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
package org.xmind.ui.tools;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.gef.service.IFeedbackService;
import org.xmind.gef.tool.AreaSelectTool;

public abstract class FeedbackAreaSelectTool extends AreaSelectTool {

    private IFeedbackService feedbackService = null;

    protected void start() {
        super.start();
        feedbackService = findFeedbackService();
        if (feedbackService != null) {
            addFeedback(feedbackService);
        }
    }

    protected void end() {
        if (feedbackService != null) {
            removeFeedback(feedbackService);
            feedbackService = null;
        }
        super.end();
    }

    protected void updateArea(Rectangle area, Point currentPos) {
        super.updateArea(area, currentPos);
        if (feedbackService != null)
            updateFeedback(feedbackService);
    }

    protected void addFeedback(IFeedbackService feedbackService) {
    }

    protected void removeFeedback(IFeedbackService feedbackService) {
    }

    protected void updateFeedback(IFeedbackService feedbackService) {
    }

    protected IFeedbackService getFeedbackService() {
        return feedbackService;
    }

    protected IFeedbackService findFeedbackService() {
        return (IFeedbackService) getTargetViewer().getService(
                IFeedbackService.class);
    }
}