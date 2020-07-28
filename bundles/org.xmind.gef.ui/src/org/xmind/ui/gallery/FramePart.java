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
package org.xmind.ui.gallery;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.draw2d.AbstractLayout;
import org.eclipse.draw2d.Cursors;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LayoutManager;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Cursor;
import org.xmind.gef.EditDomain;
import org.xmind.gef.GEF;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.draw2d.RotatableWrapLabel;
import org.xmind.gef.part.GraphicalEditPart;
import org.xmind.gef.part.IPart;
import org.xmind.gef.part.IPartSite;
import org.xmind.gef.part.IRequestHandler;
import org.xmind.gef.policy.NullEditPolicy;
import org.xmind.gef.status.StatusEvent;
import org.xmind.gef.util.Properties;

public class FramePart extends GraphicalEditPart
        implements PropertyChangeListener {

    private static class FrameContentLayout extends AbstractLayout {

        private static Rectangle BOUNDS = new Rectangle();

        private static Rectangle CHILD_BOUNDS = new Rectangle();

        private IPartSite site;

        public FrameContentLayout(IPartSite site) {
            this.site = site;
        }

        public void layout(IFigure container) {
            Rectangle area = container.getClientArea(BOUNDS);
            int childX, childY, childWidth, childHeight;
            for (Object child : container.getChildren()) {
                IFigure figure = (IFigure) child;
                Dimension childSize = figure.getPreferredSize(area.width,
                        area.height);
                childWidth = Math.min(childSize.width, area.width);
                childHeight = Math.min(childSize.height, area.height);
                childX = area.x + (area.width - childWidth) / 2;
                childY = area.y + (area.height - childHeight) / 2;
                CHILD_BOUNDS.setBounds(childX, childY, childWidth, childHeight);
                figure.setBounds(CHILD_BOUNDS);
            }
        }

        @Override
        protected Dimension calculatePreferredSize(IFigure container, int wHint,
                int hHint) {
            Insets insets = container.getInsets();
            Properties properties = site.getProperties();
            Dimension contentSize = (Dimension) properties
                    .get(GalleryViewer.FrameContentSize);
            boolean pack = properties.getBoolean(GalleryViewer.PackFrameContent,
                    false);
            if (contentSize != null && !pack)
                return new Dimension(contentSize.width + insets.getWidth(),
                        contentSize.height + insets.getHeight());

            int childWHint = contentSize != null ? contentSize.width
                    : (wHint < 0 ? wHint
                            : Math.max(0, wHint - insets.getWidth()));
            int childHHint = contentSize != null ? contentSize.height
                    : (hHint < 0 ? hHint
                            : Math.max(0, hHint - insets.getHeight()));
            int childWidth = 0, childHeight = 0;
            for (Object child : container.getChildren()) {
                Dimension childSize = ((IFigure) child)
                        .getPreferredSize(childWHint, childHHint);
                childWidth = Math.max(childWidth, childSize.width);
                childHeight = Math.max(childHeight, childSize.height);
            }

            if (contentSize != null) {
                childWidth = Math.min(childWidth, contentSize.width);
                childHeight = Math.min(childHeight, contentSize.height);
            }

            return new Dimension(childWidth + insets.getWidth(),
                    childHeight + insets.getHeight());
        }

    }

    public FramePart(Object model) {
        setModel(model);
        setDecorator(FrameDecorator.DEFAULT);
    }

    @Override
    protected LayoutManager createLayoutManager() {
        return new FrameContentLayout(getSite());
    }

    protected IFigure createFigure() {
        FrameFigure figure = new FrameFigure();
        Properties properties = getSite().getViewer().getProperties();
        boolean useAdvancedRenderer = properties
                .getBoolean(IGraphicalViewer.VIEWER_RENDER_TEXT_AS_PATH, false);
        figure.setTitleRenderStyle(useAdvancedRenderer
                ? RotatableWrapLabel.ADVANCED : RotatableWrapLabel.NORMAL);

        return figure;
    }

    public FrameFigure getFigure() {
        return (FrameFigure) super.getFigure();
    }

    public IFigure getContentPane() {
        return ((FrameFigure) super.getFigure()).getContentPane();
    }

    /**
     * Least element has no child.
     */
    protected Object[] getModelChildren(Object model) {
        boolean isLeastElement = getSite().getViewer().getProperties()
                .getBoolean(GalleryViewer.CustomContentPaneDecorator, false);
        return isLeastElement ? new Object[0] : new Object[] { model };
    }

    protected void declareEditPolicies(IRequestHandler reqHandler) {
        super.declareEditPolicies(reqHandler);
        reqHandler.installEditPolicy(GEF.ROLE_SELECTABLE,
                NullEditPolicy.getInstance());
        reqHandler.installEditPolicy(GEF.ROLE_NAVIGABLE,
                GalleryViewer.POLICY_NAVIGABLE);
        reqHandler.installEditPolicy(GEF.ROLE_MOVABLE,
                GalleryViewer.POLICY_MOVABLE);
    }

    protected void register() {
        registerModel(getModel());
        super.register();
    }

    protected void unregister() {
        super.unregister();
        unregisterModel(getModel());
    }

    protected void handleStatusChanged(StatusEvent event) {
        if ((event.key & GEF.PART_SELECTED) != 0) {
            setSelected(event.newValue);
        } else if ((event.key & GEF.PART_PRESELECTED) != 0) {
            setPreselected(event.newValue);
        } else if ((event.key & GEF.PART_FOCUSED) != 0) {
            getFigure().repaint();
        } else {
            super.handleStatusChanged(event);
        }
    }

    protected void setSelected(boolean selected) {
        getFigure().setSelected(selected);
    }

    protected void setPreselected(boolean preselected) {
        getFigure().setPreselected(preselected);
    }

    protected IPart findChildAt(IPart child, Point position) {
        if (!child.hasRole(GEF.ROLE_SELECTABLE))
            return null;
        return super.findChildAt(child, position);
    }

    public Cursor getCursor(Point pos) {
        if (getContentPane().containsPoint(pos) && (!getSite().getProperties()
                .getBoolean(GalleryViewer.SolidFrames, false)
                || getSite().getProperties()
                        .getBoolean(GalleryViewer.SingleClickToOpen, false)))
            return Cursors.HAND;
        if (getFigure().getTitle().containsPoint(pos)) {
            EditDomain domain = getSite().getDomain();
            if (domain != null && domain.hasTool(GEF.TOOL_EDIT))
                return Cursors.HAND;
        }
        return null;
    }

    @Override
    protected void updateChildren() {
        super.updateChildren();
        for (IPart child : getChildren()) {
            child.update();
        }
    }

    protected void onActivated() {
        super.onActivated();
        getSite().getViewer().getProperties().addPropertyChangeListener(
                IGraphicalViewer.VIEWER_RENDER_TEXT_AS_PATH, this);
    }

    protected void onDeactivated() {
        getSite().getViewer().getProperties().removePropertyChangeListener(
                IGraphicalViewer.VIEWER_RENDER_TEXT_AS_PATH, this);
        super.onDeactivated();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        boolean useAdvancedRenderer = getSite().getViewer().getProperties()
                .getBoolean(IGraphicalViewer.VIEWER_RENDER_TEXT_AS_PATH, false);
        getFigure().setTitleRenderStyle(useAdvancedRenderer
                ? RotatableWrapLabel.ADVANCED : RotatableWrapLabel.NORMAL);
    }
}
