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

package org.xmind.gef.image;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.gef.GraphicalViewer;
import org.xmind.gef.IGraphicalViewer;

/**
 * @author Frank Shaka
 * 
 */
public class ViewerExportSourceProvider implements IExportSourceProvider {

    private IGraphicalViewer viewer;

    private Insets margins;

    private IFigure[] contents = null;

    private Rectangle sourceArea = null;

    /**
     * 
     */
    public ViewerExportSourceProvider(IGraphicalViewer viewer, Insets margins) {
        this.viewer = viewer;
        this.margins = margins;
    }

    /**
     * 
     */
    public ViewerExportSourceProvider(IGraphicalViewer viewer, int allMargin) {
        this(viewer, new Insets(allMargin));
    }

    public ViewerExportSourceProvider(IGraphicalViewer viewer) {
        this(viewer, new Insets(0));
    }

    protected LightweightSystem getLightweightSystem() {
        if (viewer instanceof GraphicalViewer)
            return ((GraphicalViewer) viewer).getLightweightSystem();
        return viewer.getCanvas().getLightweightSystem();
    }

    public IGraphicalViewer getViewer() {
        return this.viewer;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.gef.image.IExportSourceProvider#getContents()
     */
    public IFigure[] getContents() {
        if (contents == null) {
            contents = collectContents();
        }
        return contents;
    }

    private IFigure[] collectContents() {
        List<IFigure> figures = new ArrayList<IFigure>(5);
        collectContents(figures);
        return figures.toArray(new IFigure[figures.size()]);
    }

    /**
     * @param figures
     */
    protected void collectContents(List<IFigure> figures) {
        figures.add(getDefaultFigure());
    }

    /**
     * @return
     */
    protected IFigure getDefaultFigure() {
        IFigure contents = getViewer().getCanvas().getViewport().getContents();
        if (contents != null)
            return contents;
        IFigure rootFigure = getLightweightSystem().getRootFigure();
        List children = rootFigure.getChildren();
        if (children.size() > 0) {
            return (IFigure) children.get(0);
        }
        return rootFigure;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.gef.image.IExportSourceProvider#getSourceArea()
     */
    public Rectangle getSourceArea() {
        if (sourceArea == null) {
            sourceArea = calculateSourceArea(
                    getContentsForCalculatingSourceArea());
        }
        return sourceArea;
    }

    protected IFigure[] getContentsForCalculatingSourceArea() {
        List<IFigure> figures = new ArrayList<IFigure>(5);
        collectContentsForCalculatingSourceArea(figures);
        return figures.toArray(new IFigure[figures.size()]);
    }

    protected void collectContentsForCalculatingSourceArea(
            List<IFigure> figures) {
        collectContents(figures);
    }

    /**
     * @param contents2
     * @return
     */
    protected Rectangle calculateSourceArea(IFigure[] contents) {
        return ImageExportUtils.calcBoundsUnion(contents);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.gef.image.IExportSourceProvider#getMargins()
     */
    public Insets getMargins() {
        return margins;
    }

}
