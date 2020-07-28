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

import org.eclipse.swt.widgets.Display;
import org.xmind.gef.GEF;
import org.xmind.gef.part.IGraphicalEditPart;
import org.xmind.gef.part.IPartStatus;
import org.xmind.gef.service.IFeedback;
import org.xmind.gef.service.IFeedbackService;
import org.xmind.gef.status.IStatusListener;
import org.xmind.gef.status.StatusEvent;
import org.xmind.gef.status.StatusMachine2;
import org.xmind.ui.mindmap.ISelectionFeedbackHelper;

public class SelectionFeedbackHelper
        implements ISelectionFeedbackHelper, IStatusListener {

    private IGraphicalEditPart host;

    private IFeedbackService feedbackService = null;

    private IFeedback currentFeedback = null;

    private int lastStatus = 0;

    private boolean updating = false;

    private int overrideStatus = -1;

    private int overrideMask = -1;

    public void setHost(IGraphicalEditPart host) {
        if (host == this.host)
            return;

        if (this.host != null) {
            this.host.getStatus().removeStatusListener(this);
        }
        this.host = host;
        if (host != null) {
            host.getStatus().addStatusListener(this);
        }
    }

    protected IGraphicalEditPart getHost() {
        return host;
    }

    public void setFeedbackService(IFeedbackService feedbackService) {
        if (feedbackService == this.feedbackService)
            return;

        if (this.feedbackService != null) {
            removeAllFeedback(this.feedbackService);
        }
        this.feedbackService = feedbackService;
        if (feedbackService != null)
            update(feedbackService);
    }

    protected IFeedbackService getFeedbackService() {
        return feedbackService;
    }

    protected int getCalculatedSelectionStatus() {
        if (overrideMask >= 0) {
            return (overrideStatus & overrideMask)
                    | (getRealSelectionStatus() & ~overrideMask);
        }
        return getRealSelectionStatus();
    }

    protected int getRealSelectionStatus() {
        if (getHost() == null)
            return 0;
        IPartStatus status = getHost().getStatus();
        if (status instanceof StatusMachine2) {
            return ((StatusMachine2) status).getStatus() & GEF.PART_SEL_MASK;
        }
        return calcSelectionStatus(status.isPreSelected(), status.isSelected(),
                status.isFocused());
    }

    public void forceFeedback(int key, boolean value) {
        if (overrideMask < 0)
            overrideMask = 0;
        overrideMask |= key;
        if (overrideStatus < 0)
            overrideStatus = 0;
        if (value) {
            overrideStatus |= key;
        } else {
            overrideStatus &= ~key;
        }
        updateFeedback(false);
    }

    public void resetFeedback(int key) {
        overrideMask &= ~key;
        updateFeedback(false);
    }

    public void resetAllFeedback() {
        overrideMask = -1;
        overrideStatus = -1;
        updateFeedback(false);
    }

    public void updateFeedback(boolean async) {
        if (async) {
            asyncUpdate();
        } else {
            update();
        }
    }

    protected void asyncUpdate() {
        if (updating)
            return;

        Display.getCurrent().asyncExec(new Runnable() {
            public void run() {
                update();
                updating = false;
            }

        });
    }

    private void update() {
        if (getHost() == null || !getHost().getStatus().isActive())
            return;

        if (feedbackService != null && !feedbackService.isDisposed()) {
            update(feedbackService);
        }
    }

    private void update(IFeedbackService feedbackService) {
        IFeedback newFeedback = (IFeedback) getHost()
                .getAdapter(IFeedback.class);
        if (newFeedback != currentFeedback) {
            if (currentFeedback != null) {
                feedbackService.removeFeedback(currentFeedback);
            }
            currentFeedback = newFeedback;
        }

        int newStatus = getCalculatedSelectionStatus();
        if (newStatus != lastStatus) {
            if (currentFeedback != null) {
                if (lastStatus == 0 && newStatus > 0) {
                    feedbackService.addFeedback(currentFeedback);
                } else if (lastStatus > 0 && newStatus == 0) {
                    feedbackService.removeFeedback(currentFeedback);
                }
            }
            lastStatus = newStatus;
        }

        if (currentFeedback != null) {
            updateFeedback(currentFeedback, newStatus);
            currentFeedback.update();
        }
        updateOtherFeedback(feedbackService, newStatus);
    }

    protected void updateFeedback(IFeedback feedback, int newStatus) {
    }

    protected void updateOtherFeedback(IFeedbackService feedbackService,
            int newStatus) {
    }

    private void removeAllFeedback(IFeedbackService feedbackService) {
        if (currentFeedback != null) {
            feedbackService.removeFeedback(currentFeedback);
        }
        removeOtherFeedback(feedbackService);
    }

    protected void removeOtherFeedback(IFeedbackService feedbackService) {
    }

    public void statusChanged(StatusEvent event) {
        if ((event.key & GEF.PART_SEL_MASK) != 0) {
            updateFeedback(false);
        }
    }

    private static int calcSelectionStatus(boolean preselected,
            boolean selected, boolean focused) {
        int s = 0;
        if (preselected)
            s |= GEF.PART_PRESELECTED;
        if (selected)
            s |= GEF.PART_SELECTED;
        if (focused)
            s |= GEF.PART_FOCUSED;
        return s;
    }

}
