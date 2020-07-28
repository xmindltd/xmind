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
package org.xmind.gef.part;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Layer;
import org.eclipse.draw2d.LayeredPane;
import org.eclipse.draw2d.StackLayout;
import org.eclipse.draw2d.Viewport;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.xmind.gef.GEF;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.ILayerManager;
import org.xmind.gef.IViewer;

/**
 * @author Administrator
 */
public class GraphicalRootEditPart extends GraphicalEditPart
        implements IGraphicalRootPart, ILayerManager {

    private IViewer viewer = null;

    private IGraphicalEditPart contents = null;

    private LayeredPane layeredPane = null;

    public IPart getContents() {
        return contents;
    }

    public void setContents(IPart part) {
        if (contents != null)
            removeChild(contents);
        contents = (IGraphicalEditPart) part;
        if (contents != null)
            addChild(contents, 0);
    }

    public IViewer getViewer() {
        return viewer;
    }

    public void setViewer(IViewer viewer) {
        IViewer oldViewer = getViewer();
        this.viewer = viewer;
        viewerChanged(viewer, oldViewer);
    }

    /**
     * @param newViewer
     * @param oldViewer
     */
    protected void viewerChanged(IViewer newViewer, IViewer oldViewer) {
        if (oldViewer instanceof IGraphicalViewer
                && ((IGraphicalViewer) oldViewer).getLayerManager() == this) {
            ((IGraphicalViewer) oldViewer).setLayerManager(null);
        }
        if (newViewer instanceof IGraphicalViewer) {
            ((IGraphicalViewer) newViewer).setLayerManager(this);
        }
    }

    protected IFigure createFigure() {
        Viewport viewport = createViewport();
        layeredPane = createLayeredPane();
        viewport.setContents(layeredPane);
        addLayers(layeredPane);
        return viewport;
    }

    protected Viewport createViewport() {
        return new Viewport(true);
    }

    protected LayeredPane createLayeredPane() {
        return new LayeredPane();
    }

    protected void addLayers(LayeredPane layeredPane) {
        final Layer contentsLayer = new Layer();
        contentsLayer.setLayoutManager(new StackLayout());
        layeredPane.add(contentsLayer, GEF.LAYER_CONTENTS);
        Layer presentationLayer = new Layer() {
            @Override
            public Dimension getPreferredSize(int wHint, int hHint) {
                return contentsLayer.getPreferredSize(wHint, hHint);
            }
        };
        layeredPane.add(presentationLayer, GEF.LAYER_PRESENTATION);
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.ui.gantt2.gefext.GraphicalEditPart#getContentPane()
     */
    @Override
    public IFigure getContentPane() {
        return getLayer(GEF.LAYER_CONTENTS);
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.gef.ILayerManager#getLayer(java.lang.Object)
     */
    public Layer getLayer(Object key) {
        return layeredPane.getLayer(key);
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.gef.ILayerManager#insertLayer(java.lang.Object,
     * org.eclipse.draw2d.Layer, java.lang.Object, boolean)
     */
    public void insertLayer(Object key, Layer layer, Object before,
            boolean scalable) {
        if (before == null) {
            layeredPane.add(layer, key);
        } else {
            layeredPane.addLayerBefore(layer, key, before);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.gef.ILayerManager#removeLayer(java.lang.Object)
     */
    public void removeLayer(Object key) {
        layeredPane.removeLayer(key);
    }

    /**
     * @see org.xmind.gef.part.GraphicalEditPart#findAt(org.eclipse.draw2d.geometry.Point)
     */
    @Override
    public IPart findAt(Point position) {
        IPart ret = super.findAt(position);
        if (ret != null)
            return ret;
        return this;
    }

//    protected IFigure createFigure(IGenre genre) {
//        return genre.createRootFigure(this, (IGraphicalViewer) getViewer());
//    }
//
//    protected void addChildView(IPart child, int index) {
//        if (getContentPane() instanceof Viewport) {
//            ((Viewport) getContentPane())
//                    .setContents(((IGraphicalPart) child).getFigure());
//        } else {
//            super.addChildView(child, index);
//        }
//    }
//
//    protected void removeChildView(IPart child) {
//        if (getContentPane() instanceof Viewport) {
//            Viewport viewport = (Viewport) getContentPane();
//            IFigure childFigure = ((IGraphicalPart) child).getFigure();
//            if (childFigure == viewport.getContents()) {
//                viewport.setContents(null);
//                return;
//            }
//        }
//        super.removeChildView(child);
//    }

}
