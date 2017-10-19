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
package org.xmind.ui.internal.mindmap;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Layer;
import org.eclipse.draw2d.LayeredPane;
import org.eclipse.draw2d.LayoutListener;
import org.eclipse.draw2d.Viewport;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.widgets.Composite;
import org.xmind.gef.GEF;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.ILayerManager;
import org.xmind.gef.IViewer;
import org.xmind.gef.IZoomListener;
import org.xmind.gef.ZoomObject;
import org.xmind.gef.part.GraphicalRootEditPart;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.gef.part.IPart;
import org.xmind.gef.service.IAnimationService;
import org.xmind.gef.util.Properties;
import org.xmind.ui.internal.layers.ContentsLayer;
import org.xmind.ui.internal.layers.FeedbackLayer;
import org.xmind.ui.internal.layers.MindMapLayeredPane;
import org.xmind.ui.internal.layers.MindMapViewport;
import org.xmind.ui.mindmap.IMindMapViewer;
import org.xmind.ui.mindmap.MindMapUI;

public class MindMapRootPart extends GraphicalRootEditPart implements
        IZoomListener, ILayerManager, ControlListener, LayoutListener,
        PropertyChangeListener {

    public void setViewer(IViewer viewer) {
        if (getViewer() != null) {
            getViewer().getProperties().removePropertyChangeListener(this);
            if (getViewer() instanceof IGraphicalViewer) {
                ((IGraphicalViewer) getViewer()).setLayerManager(null);
            }
        }
        super.setViewer(viewer);
        if (getViewer() != null) {
            if (getViewer() instanceof IGraphicalViewer) {
                ((IGraphicalViewer) getViewer()).setLayerManager(this);
            }
            getViewer().getProperties().addPropertyChangeListener(this);
        }
    }

    protected IFigure createFigure() {
        Viewport viewport = new MindMapViewport();
        viewport.setBackgroundColor(ColorConstants.white);
        MindMapLayeredPane layeredPane = new MindMapLayeredPane();
        viewport.setContents(layeredPane);
        ContentsLayer contentsLayer = (ContentsLayer) layeredPane
                .getLayer(GEF.LAYER_CONTENTS);
        Properties properties = getViewer().getProperties();
        contentsLayer.setCentered(properties.getBoolean(
                IMindMapViewer.VIEWER_CENTERED, false));
        boolean constrained = properties.getBoolean(
                IMindMapViewer.VIEWER_CONSTRAINED, false);
        contentsLayer.setConstrained(constrained);
        if (properties.getBoolean(IMindMapViewer.VIEWER_CORNERED, false)) {
            contentsLayer.addCorners();
        }
        Object margin = properties.get(IMindMapViewer.VIEWER_MARGIN);
        if (margin != null && margin instanceof Integer) {
            contentsLayer.setMargin(((Integer) margin).intValue());
        }
        if (!constrained) {
            layeredPane.getScalableLayeredPane().setScale(
                    ((IGraphicalViewer) getViewer()).getZoomManager()
                            .getScale());
        }
        viewport.addLayoutListener(this);
        return viewport;
    }

    public Viewport getViewport() {
        return (Viewport) super.getFigure();
    }

    public MindMapLayeredPane getLayeredPane() {
        return (MindMapLayeredPane) getViewport().getContents();
    }

    public ContentsLayer getContentsLayer() {
        return (ContentsLayer) getLayer(GEF.LAYER_CONTENTS);
    }

    public FeedbackLayer getFeedbackLayer() {
        return (FeedbackLayer) getLayer(GEF.LAYER_FEEDBACK);
    }

    public IFigure getContentPane() {
        return getContentsLayer();
    }

    public void setContents(IPart part) {
        boolean contentsChanged = part != getContents();
        super.setContents(part);
        MindMapLayeredPane layeredPane = getLayeredPane();
        if (contentsChanged && MindMapUI.isAnimationEnabled()
                && getSite().getViewer().hasService(IAnimationService.class)) {
            layeredPane.setAlpha(0);
            layeredPane.setTargetAlpha(0xff);
        } else {
            layeredPane.setAlpha(0xff);
            layeredPane.setTargetAlpha(0xff);
        }
    }

    protected void addChildView(IPart child, int index) {
        getContentsLayer().setContents(((IGraphicalPart) child).getFigure());
    }

    protected void removeChildView(IPart child) {
        getContentsLayer().setContents(null);
    }

    protected void onActivated() {
        ((IGraphicalViewer) getViewer()).getZoomManager().addZoomListener(this);
        getViewer().getControl().addControlListener(this);
        super.onActivated();
    }

    protected void onDeactivated() {
        super.onDeactivated();
        getViewer().getControl().removeControlListener(this);
        ((IGraphicalViewer) getViewer()).getZoomManager().removeZoomListener(
                this);
    }

    public void scaleChanged(ZoomObject source, double oldValue, double newValue) {
        getLayeredPane().getScalableLayeredPane().setScale(newValue);
//        getLayeredPane().getCoverLayer().setScale(newValue);
    }

    public void insertLayer(Object key, Layer layer, Object before,
            boolean scalable) {
        LayeredPane pane;
        if (scalable)
            pane = getLayeredPane().getScalableLayeredPane();
        else
            pane = getLayeredPane();

        if (before == null)
            pane.add(layer, key);
        else
            pane.addLayerBefore(layer, key, before);
    }

    public void removeLayer(Object key) {
        getLayeredPane().removeLayer(key);
    }

    public Layer getLayer(Object key) {
        return getLayeredPane().getLayer(key);
    }

    public void controlMoved(ControlEvent e) {
    }

    public void controlResized(ControlEvent e) {
        if (getViewer().getProperties().getBoolean(
                IMindMapViewer.VIEWER_CONSTRAINED, false)) {
            ((IGraphicalViewer) getViewer()).getZoomManager().setScale(
                    calculateConstrainedScale());
        }
    }

    private double calculateConstrainedScale() {
        org.eclipse.swt.graphics.Rectangle maxSize = ((Composite) getViewer()
                .getControl()).getClientArea();
        ContentsLayer layer = (ContentsLayer) getLayer(GEF.LAYER_CONTENTS);
        IFigure contents = layer.getContents();
        Dimension size = contents.getPreferredSize();
        double scale = Math.min((maxSize.width - layer.getMargin()) * 1.0d
                / size.width, (maxSize.height - layer.getMargin()) * 1.0d
                / size.height);
        return scale;
    }

    public void invalidate(IFigure container) {
    }

    public boolean layout(IFigure container) {
        if (getViewer().getProperties().getBoolean(
                IMindMapViewer.VIEWER_CONSTRAINED, false)) {
            ((IGraphicalViewer) getViewer()).getZoomManager().setScale(
                    calculateConstrainedScale());
        }
        return false;
    }

    public void postLayout(IFigure container) {
    }

    public void remove(IFigure child) {
    }

    public void setConstraint(IFigure child, Object constraint) {
    }

    /*
     * (non-Javadoc)
     * 
     * @seejava.beans.PropertyChangeListener#propertyChange(java.beans.
     * PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {
        ContentsLayer contentsLayer = (ContentsLayer) getLayer(GEF.LAYER_CONTENTS);
        Properties properties = getViewer().getProperties();
        contentsLayer.setCentered(properties.getBoolean(
                IMindMapViewer.VIEWER_CENTERED, false));
        boolean constrained = properties.getBoolean(
                IMindMapViewer.VIEWER_CONSTRAINED, false);
        contentsLayer.setConstrained(constrained);
        if (properties.getBoolean(IMindMapViewer.VIEWER_CORNERED, false)) {
            contentsLayer.addCorners();
        }
        Object margin = properties.get(IMindMapViewer.VIEWER_MARGIN);
        if (margin != null && margin instanceof Integer) {
            contentsLayer.setMargin(((Integer) margin).intValue());
        }
    }

}