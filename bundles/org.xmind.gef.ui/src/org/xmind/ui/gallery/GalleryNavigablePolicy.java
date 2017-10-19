/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.ui.gallery;

import java.util.List;

import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.gef.GEF;
import org.xmind.gef.Request;
import org.xmind.gef.part.IPart;
import org.xmind.gef.policy.NavigablePolicy;

/**
 * @author frankshaka
 * 
 */
public class GalleryNavigablePolicy extends NavigablePolicy {

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.gef.policy.NavigablePolicy#findNavParts(org.xmind.gef.Request,
     * java.lang.String, java.util.List, java.util.List)
     */
    @Override
    protected void findNavParts(Request request, String navType,
            List<IPart> sources, List<IPart> result) {
        FramePart sourceFrame = findSourceFrame(sources, navType);
        if (sourceFrame != null) {
            IPart p = findNavFrame(request, navType, sourceFrame);
            if (p != null) {
                result.add(p);
                return;
            }
        }
        super.findNavParts(request, navType, sources, result);
    }

    /**
     * @param request
     * @param navType
     * @param sourceFrame
     * @param result
     */
    protected IPart findNavFrame(Request request, String navType,
            FramePart sourceFrame) {
        IPart parent = sourceFrame.getParent();
        if (parent == null)
            return null;

        if (GEF.REQ_NAV_BEGINNING.equals(navType)) {
            return findFirstFrameChild(parent);
        } else if (GEF.REQ_NAV_END.equals(navType)) {
            return findLastFrameChild(parent);
        }

        int index = parent.getChildren().indexOf(sourceFrame);
        if (GEF.REQ_NAV_UP.equals(navType) || GEF.REQ_NAV_LEFT.equals(navType)) {
            IPart p = findFrameChildBackwards(navType, parent, index,
                    sourceFrame);
            if (p != null)
                return p;
            return findFrameChildForwards(navType, parent, index, sourceFrame);
        } else if (GEF.REQ_NAV_DOWN.equals(navType)
                || GEF.REQ_NAV_RIGHT.equals(navType)) {
            IPart p = findFrameChildForwards(navType, parent, index,
                    sourceFrame);
            if (p != null)
                return p;
            return findFrameChildBackwards(navType, parent, index, sourceFrame);
        }
        return null;
    }

    /**
     * @param navType
     * @param parent
     * @param index
     * @param sourceFrame
     * @param result
     * @return
     */
    private IPart findFrameChildForwards(String navType, IPart parent,
            int index, FramePart sourceFrame) {
        List<IPart> children = parent.getChildren();
        for (int i = index + 1; i < children.size(); i++) {
            IPart p = children.get(i);
            if (p instanceof FramePart) {
                FramePart frame = (FramePart) p;
                if (isNavFrame(navType, frame, sourceFrame))
                    return frame;
            }
        }
        return null;
    }

    /**
     * @param navType
     * @param parent
     * @param index
     * @param sourceFrame
     * @param result
     * @return
     */
    private IPart findFrameChildBackwards(String navType, IPart parent,
            int index, FramePart sourceFrame) {
        List<IPart> children = parent.getChildren();
        for (int i = index - 1; i >= 0; i--) {
            IPart p = children.get(i);
            if (p instanceof FramePart) {
                FramePart frame = (FramePart) p;
                if (isNavFrame(navType, frame, sourceFrame))
                    return frame;
            }
        }
        return null;
    }

    /**
     * @param navType
     * @param frame
     * @param sourceFrame
     * @return
     */
    private boolean isNavFrame(String navType, FramePart frame,
            FramePart sourceFrame) {
        Rectangle bounds = frame.getFigure().getBounds();
        Rectangle sourceBounds = sourceFrame.getFigure().getBounds();
        if (GEF.REQ_NAV_UP.equals(navType)) {
            int x = sourceBounds.x + sourceBounds.width / 2;
            return bounds.y < sourceBounds.y
                    && bounds.bottom() < sourceBounds.bottom() && bounds.x < x
                    && bounds.right() > x;
        } else if (GEF.REQ_NAV_DOWN.equals(navType)) {
            int x = sourceBounds.x + sourceBounds.width / 2;
            return bounds.y > sourceBounds.y
                    && bounds.bottom() > sourceBounds.bottom() && bounds.x < x
                    && bounds.right() > x;
        } else if (GEF.REQ_NAV_LEFT.equals(navType)) {
            int y = sourceBounds.y + sourceBounds.height / 2;
            return bounds.x < sourceBounds.x
                    && bounds.right() < sourceBounds.right() && bounds.y < y
                    && bounds.bottom() > y;
        } else if (GEF.REQ_NAV_RIGHT.equals(navType)) {
            int y = sourceBounds.y + sourceBounds.height / 2;
            return bounds.x > sourceBounds.x
                    && bounds.right() > sourceBounds.right() && bounds.y < y
                    && bounds.bottom() > y;
        }
        return false;
    }

    /**
     * @param parent
     * @param result
     * @return
     */
    private IPart findLastFrameChild(IPart parent) {
        List<IPart> children = parent.getChildren();
        for (int i = children.size() - 1; i >= 0; i--) {
            IPart p = children.get(i);
            if (p instanceof FramePart) {
                return p;
            }
        }
        return null;
    }

    /**
     * @param parent
     * @param result
     * @return
     */
    private IPart findFirstFrameChild(IPart parent) {
        List<IPart> children = parent.getChildren();
        for (int i = 0; i < children.size(); i++) {
            IPart p = children.get(i);
            if (p instanceof FramePart) {
                return p;
            }
        }
        return null;
    }

    /**
     * 
     * @param sources
     * @param navType
     * @return
     */
    private FramePart findSourceFrame(List<IPart> sources, String navType) {
        if (sources.isEmpty())
            return null;
        IPart source = sources.get(0);
        return source instanceof FramePart ? (FramePart) source : null;
    }

}
