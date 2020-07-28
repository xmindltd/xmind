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
package org.xmind.gef.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.xmind.gef.ZoomManager;

public class CompositeFeedback extends AbstractFeedback {

    private List<IFeedback> innerFeedbacks;

    public CompositeFeedback(List<IFeedback> feedbacks) {
        this.innerFeedbacks = new ArrayList<IFeedback>(feedbacks);
    }

    public CompositeFeedback(IFeedback... feedbacks) {
        this.innerFeedbacks = new ArrayList<IFeedback>(Arrays.asList(feedbacks));
    }

    public CompositeFeedback() {
        this.innerFeedbacks = new ArrayList<IFeedback>();
    }

    public void addFeedback(IFeedback feedback) {
        this.innerFeedbacks.add(feedback);
    }

    public void removeFeedback(IFeedback feedback) {
        this.innerFeedbacks.remove(feedback);
    }

    public IFeedback getFeedback(int index) {
        return innerFeedbacks.get(index);
    }

    public List<IFeedback> getFeedbackParts() {
        return innerFeedbacks;
    }

    public void setZoomManager(ZoomManager zoomManager) {
        super.setZoomManager(zoomManager);
        for (IFeedback p : innerFeedbacks) {
            p.setZoomManager(zoomManager);
        }
    }

    public void addToLayer(IFigure layer) {
        for (IFeedback p : innerFeedbacks) {
            p.addToLayer(layer);
        }
    }

    public boolean containsPoint(Point point) {
        for (int i = innerFeedbacks.size() - 1; i >= 0; i--)
            if (innerFeedbacks.get(i).containsPoint(point))
                return true;
        return false;
    }

    public void removeFromLayer(IFigure layer) {
        for (int i = innerFeedbacks.size() - 1; i >= 0; i--)
            innerFeedbacks.get(i).removeFromLayer(layer);
    }

    public void update() {
        for (IFeedback p : innerFeedbacks) {
            p.update();
        }
    }

}