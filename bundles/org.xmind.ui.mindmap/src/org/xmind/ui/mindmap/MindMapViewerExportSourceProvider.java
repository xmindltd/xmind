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

package org.xmind.ui.mindmap;

import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.gef.GEF;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.draw2d.IOriginBased;
import org.xmind.gef.draw2d.IReferencedFigure;
import org.xmind.gef.image.ImageExportUtils;
import org.xmind.gef.image.ViewerExportSourceProvider;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.gef.part.IPart;

/**
 * @author Frank Shaka
 * 
 */
public class MindMapViewerExportSourceProvider extends
        ViewerExportSourceProvider {

    public MindMapViewerExportSourceProvider(IGraphicalViewer viewer,
            Insets margins) {
        super(viewer, margins);
    }

    public MindMapViewerExportSourceProvider(IGraphicalViewer viewer,
            int allMargin) {
        super(viewer, allMargin);
    }

    public MindMapViewerExportSourceProvider(IGraphicalViewer viewer) {
        super(viewer, viewer.getProperties().getInteger(
                IMindMapViewer.VIEWER_MARGIN, MindMapUI.DEFAULT_EXPORT_MARGIN));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.gef.image.ViewerExportSourceProvider#collectContents(java.util
     * .List)
     */
    @Override
    protected void collectContents(List<IFigure> figures) {
        figures.add(getViewer().getLayer(GEF.LAYER_BACKGROUND));
        figures.add(getViewer().getLayer(GEF.LAYER_SHADOW));
        figures.add(getViewer().getLayer(GEF.LAYER_CONTENTS));
        figures.add(getViewer().getLayer(MindMapUI.LAYER_TITLE));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.gef.image.ViewerExportSourceProvider#calculateSourceArea(org
     * .eclipse.draw2d.IFigure[])
     */
    @Override
    protected Rectangle calculateSourceArea(IFigure[] contents) {
        IFigure contentsFigure = getContentsFigure();
        if (contentsFigure != null) {
            return ImageExportUtils.getBounds(contentsFigure);
        }
        return super.calculateSourceArea(contents);
    }

    protected IFigure getContentsFigure() {
        IPart contentsPart = getViewer().getRootPart().getContents();
        if (contentsPart instanceof IGraphicalPart) {
            return ((IGraphicalPart) contentsPart).getFigure();
        }
        return null;
    }

    public Point getOriginPoint() {
        IFigure contentsFigure = getContentsFigure();
        if (contentsFigure != null) {
            if (contentsFigure instanceof IOriginBased) {
                return ((IOriginBased) contentsFigure).getOrigin();
            } else if (contentsFigure instanceof IReferencedFigure) {
                return ((IReferencedFigure) contentsFigure).getReference();
            }
        }
        return getSourceArea().getTopLeft();
    }

}
