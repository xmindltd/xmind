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

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.gef.event.MouseDragEvent;
import org.xmind.gef.service.BendPointsFeedback;
import org.xmind.gef.service.IBendPointsFeedback;
import org.xmind.gef.service.IFeedback;
import org.xmind.gef.service.IFeedbackService;
import org.xmind.gef.service.IRectangleProvider;
import org.xmind.gef.tool.ResizeTool;

public abstract class FeedbackResizeTool extends ResizeTool {

    private class BoundsProvider implements IRectangleProvider {

        public Rectangle getRectangle() {
            return getResultArea();
        }

    }

    private IBendPointsFeedback feedback;

    private boolean newFeedback;

    private IRectangleProvider oldBoundsProvider;

    private IRectangleProvider newBoundsProvider;

    protected void updateArea(Rectangle area, Point cursorPosition,
            MouseDragEvent me) {
        if (feedback != null) {
            updateAreaBounds(area, cursorPosition);
            feedback.update();
        }
    }

    protected void start() {
        IFeedback feedback = getSourceFeedback();
        if (feedback instanceof IBendPointsFeedback) {
            this.feedback = (IBendPointsFeedback) feedback;
            newFeedback = false;
        } else {
            this.feedback = createBendPointFeedbackPart();
            newFeedback = true;
        }
        int orientation = this.feedback.getOrientation(getStartingPosition());
        setOrientation(orientation);
        if (orientation != PositionConstants.NONE) {
            initFeedback(this.feedback);
        } else {
            this.feedback = null;
        }
    }

    protected abstract IFeedback getSourceFeedback();

    protected void initFeedback(IBendPointsFeedback feedback) {
        if (newFeedback) {
            IFeedbackService feedbackService = (IFeedbackService) getTargetViewer()
                    .getService(IFeedbackService.class);
            if (feedbackService != null) {
                feedbackService.addFeedback(feedback);
            }
        }
        oldBoundsProvider = feedback.getBoundsProvider();
        if (oldBoundsProvider != null) {
            setInitArea(oldBoundsProvider.getRectangle());
        } else {
            setInitArea(getSourceArea());
        }
        newBoundsProvider = new BoundsProvider();
        feedback.setBoundsProvider(newBoundsProvider);
        feedback.update();
    }

    protected abstract Rectangle getSourceArea();

    protected IBendPointsFeedback createBendPointFeedbackPart() {
        return new BendPointsFeedback();
    }

    protected void end() {
        if (feedback != null) {
            removeFeedback(feedback);
        }
    }

    protected void removeFeedback(IBendPointsFeedback feedback) {
        feedback.setBoundsProvider(oldBoundsProvider);
        if (newFeedback) {
            IFeedbackService feedbackService = (IFeedbackService) getTargetViewer()
                    .getService(IFeedbackService.class);
            if (feedbackService != null) {
                feedbackService.removeFeedback(feedback);
            }
        } else {
            feedback.update();
        }
        feedback = null;
    }

}