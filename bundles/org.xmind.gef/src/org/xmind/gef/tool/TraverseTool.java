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
package org.xmind.gef.tool;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.StructuredSelection;
import org.xmind.gef.GEF;
import org.xmind.gef.Request;
import org.xmind.gef.part.IGraphicalEditPart;
import org.xmind.gef.part.IPart;
import org.xmind.gef.status.IStatusListener;
import org.xmind.gef.status.StatusEvent;

public class TraverseTool extends GraphicalTool implements ISourceTool,
        IStatusListener {

    private IGraphicalEditPart source;

    private IPart lastSource = null;

    private IPart lastTarget = null;

    private IPart[] traversables = null;

    private int currentIndex = -1;

    private boolean smartMode = false;

    private boolean oldSourceSelected = false;

    private boolean oldSourceFocused = false;

    private boolean oldSourcePreselected = false;

    public String getType() {
        return GEF.TOOL_TRAVERSE;
    }

    protected void onActivated(ITool prevTool) {
        super.onActivated(prevTool);
        Assert.isTrue(getSource() != null);
        getTargetViewer().setSelection(new StructuredSelection(getSource()),
                true);
        start();
    }

    protected void start() {
        Request request = new Request(GEF.REQ_GET_TRAVERSABLES);
        request.setPrimaryTarget(getSource());
        request.setDomain(getDomain());
        request.setViewer(getTargetViewer());
        getSource().handleRequest(request, GEF.ROLE_TRAVERSABLE);
        Object result = request.getResult(GEF.RESULT_TRAVERSE);
        if (result != null && result instanceof IPart[]
                && ((IPart[]) result).length > 0) {
            traversables = (IPart[]) result;
            if (traversables.length == 0) {
                cancel();
            } else if (traversables.length == 1) {
                traverseTo(traversables[0]);
            } else {
                if (getStatus().isStatus(GEF.ST_MODIFIER_MASK)) {
                    getStatus().addStatusListener(this);
                    smartMode = true;
                } else {
                    smartMode = false;
                }
                showFeedback();
            }
        } else {
            finish();
        }
    }

    protected void handleSingleRequest(Request request) {
        String reqType = request.getType();
        if (GEF.REQ_TRAVERSE.equals(reqType)) {
            assistTraverseNext();
        } else if (GEF.REQ_FINISH.equals(reqType)) {
            if (traversables != null && currentIndex >= 0) {
                traverseTo(traversables[currentIndex]);
            } else {
                cancel();
            }
        } else if (GEF.REQ_CANCEL.equals(reqType)) {
            cancel();
        } else {
            if (traversables != null && currentIndex >= 0) {
                traverseTo(traversables[currentIndex]);
            } else {
                cancel();
            }
            getDomain().handleRequest(request);
        }
    }

    private void showFeedback() {
        showSourceFeedback(getSource());
        for (IPart p : traversables) {
            showAssistFeedback(p);
        }
        int lastIndex = findLastSource();
        lastSource = null;
        lastTarget = null;
        if (lastIndex >= 0) {
            currentIndex = lastIndex;
        } else {
            currentIndex = 0;
        }
        showTargetFeedback(traversables[currentIndex]);
    }

    private int findLastSource() {
        if (getSource() == lastTarget && lastSource != null) {
            for (int i = 0; i < traversables.length; i++) {
                if (traversables[i] == lastSource)
                    return i;
            }
        }
        return -1;
    }

    protected void showSourceFeedback(IGraphicalEditPart source) {
        oldSourceSelected = source.getStatus().isSelected();
        oldSourceFocused = source.getStatus().isFocused();
        oldSourcePreselected = source.getStatus().isPreSelected();
        source.getStatus().deSelect();
        source.getStatus().dePreSelect();
        source.getStatus().lostFocus();
    }

    protected void hideSourceFeedback(IGraphicalEditPart source) {
        if (oldSourceSelected)
            source.getStatus().select();
        else
            source.getStatus().deSelect();
        if (oldSourceFocused)
            source.getStatus().setFocus();
        else
            source.getStatus().lostFocus();
        if (oldSourcePreselected)
            source.getStatus().preSelect();
        else
            source.getStatus().dePreSelect();
    }

    protected void showAssistFeedback(IPart part) {
        part.getStatus().preSelect();
    }

    protected void hideAssistFeedback(IPart part) {
        part.getStatus().dePreSelect();
    }

    protected void showTargetFeedback(IPart part) {
        part.getStatus().select();
    }

    protected void hideTargetFeedback(IPart part) {
        part.getStatus().deSelect();
    }

    private void assistTraverseNext() {
        if (currentIndex >= 0) {
            hideTargetFeedback(traversables[currentIndex]);
        }
        currentIndex++;
        if (currentIndex >= traversables.length)
            currentIndex = 0;
        showTargetFeedback(traversables[currentIndex]);
    }

    protected void traverseTo(IPart target) {
        eraseFeedback();
        lastSource = getSource();
        lastTarget = target;
        getTargetViewer().setSelection(new StructuredSelection(target), true);
        finish();
    }

    protected void onDeactivated(ITool nextTool) {
        getStatus().removeStatusListener(this);
        super.onDeactivated(nextTool);
    }

    public IGraphicalEditPart getSource() {
        return source;
    }

    public void setSource(IGraphicalEditPart source) {
        Assert.isTrue(source == null || source.getStatus().isActive());
        Assert.isTrue(source == null || source.hasRole(GEF.ROLE_TRAVERSABLE));
        this.source = source;
    }

    public void statusChanged(StatusEvent event) {
        if (smartMode && (event.key & GEF.ST_MODIFIER_MASK) != 0) {
            if (currentIndex >= 0) {
                traverseTo(traversables[currentIndex]);
            } else {
                cancel();
            }
        }
    }

    protected void eraseFeedback() {
        if (traversables != null) {
            for (IPart p : traversables) {
                hideAssistFeedback(p);
                hideTargetFeedback(p);
            }
            traversables = null;
        }
        currentIndex = -1;
        hideSourceFeedback(getSource());
    }

    protected void finish() {
        setSource(null);
        changeActiveTool(GEF.TOOL_DEFAULT);
    }

    protected void cancel() {
        eraseFeedback();
        finish();
    }

}