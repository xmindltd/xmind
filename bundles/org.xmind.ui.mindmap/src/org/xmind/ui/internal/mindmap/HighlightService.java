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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.xmind.core.internal.UserDataConstants;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.draw2d.IRelayerableFigure;
import org.xmind.gef.draw2d.IRelayeredPane;
import org.xmind.gef.draw2d.ISkylightLayer;
import org.xmind.gef.draw2d.IUseTransparency;
import org.xmind.gef.draw2d.SimpleRectangleFigure;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.gef.service.GraphicalViewerService;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.tools.MindMapFeedbackFactory;
import org.xmind.ui.mindmap.IHighlightService;
import org.xmind.ui.mindmap.ISheetPart;
import org.xmind.ui.resources.ColorUtils;

public class HighlightService extends GraphicalViewerService
        implements ISelectionChangedListener, IHighlightService {

    private static final int DEFAULT_ALPHA = 0x80;

    private List<IGraphicalPart> highlights = null;

    private IRelayeredPane relayeredPane = null;

    private ISkylightLayer highlightLayer;

    private SimpleRectangleFigure highlightRect;

    private boolean autoListenSelectionChange;

    private int alpha;

    public HighlightService(IGraphicalViewer viewer,
            boolean autoListenSelectionChange) {
        super(viewer);
        this.autoListenSelectionChange = autoListenSelectionChange;
    }

    protected void activate() {
        if (autoListenSelectionChange)
            getViewer().addSelectionChangedListener(this);
        if (highlights != null && !highlights.isEmpty()) {
            addToLayer(highlights);
        }
    }

    protected void deactivate() {
        if (highlights != null && !highlights.isEmpty()) {
            removeFromLayer(highlights);
        }
        getViewer().removeSelectionChangedListener(this);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.xmind.ui.internal.presentation.IHighlightService#getHighlightLayer()
     */
    public ISkylightLayer getHighlightLayer() {
        return highlightLayer;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.xmind.ui.internal.presentation.IHighlightService#setHighlightLayer
     * (org.xmind.ui.internal.layers.SkylightLayer)
     */
    public void setHighlightLayer(ISkylightLayer layer) {
        this.highlightLayer = layer;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.xmind.ui.internal.presentation.IHighlightService#setHighlightArea
     * (org.eclipse.draw2d.geometry.Rectangle)
     */
    public void setHighlightArea(Rectangle r) {
        setHighlightArea(r, DEFAULT_ALPHA);
    }

    public void setHighlightArea(Rectangle r, int alpha) {
        this.alpha = alpha;
        Object oldRect = highlightRect;
        if (highlightLayer != null) {
            if (isActive()) {
                highlightLayer.setSkylight(r);
                if (highlightLayer instanceof IUseTransparency) {
                    ((IUseTransparency) highlightLayer).setMainAlpha(alpha);
                }
            }
            if (r == null) {
                if (highlightRect != null) {
                    if (highlightRect.getParent() != null)
                        highlightRect.getParent().remove(highlightRect);
                    highlightRect = null;
                }
            } else {
                if (highlightRect == null) {
                    highlightRect = createRect();
                    if (isActive())
                        highlightLayer.add(highlightRect);
                }
                highlightRect.setBounds(r);
            }
        }
        if (relayeredPane != null) {
            if (r != null && oldRect == null) {
                if (highlights != null)
                    removeFromLayer(highlights);
            } else if (r == null && oldRect != null) {
                if (highlights != null)
                    if (isActive())
                        addToLayer(highlights);
            }
        }
    }

    private SimpleRectangleFigure createRect() {
        SimpleRectangleFigure fig = new SimpleRectangleFigure();
        fig.setForegroundColor(ColorUtils
                .getColor(MindMapFeedbackFactory.LINE_COLOR_AREA_SELECT));
        fig.setLineWidth(1);
        return fig;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.xmind.ui.internal.presentation.IHighlightService#getHighlightArea()
     */
    public Rectangle getHighlightArea() {
        return highlightLayer == null ? null : highlightLayer.getSkylight();
    }

    /*
     * (non-Javadoc)
     * @see
     * org.xmind.ui.internal.presentation.IHighlightService#getRelayeredPane()
     */
    public IRelayeredPane getRelayeredPane() {
        return relayeredPane;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.xmind.ui.internal.presentation.IHighlightService#setRelayeredPane
     * (org.xmind.ui.internal.layers.IRelayeredPane)
     */
    public void setRelayeredPane(IRelayeredPane layer) {
        this.relayeredPane = layer;
    }

    public void selectionChanged(SelectionChangedEvent event) {
        highlight(event.getSelection());
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.ui.internal.presentation.IHighlightService#highlight(org.
     * eclipse .jface.viewers.ISelection)
     */
    public void highlight(ISelection selection) {
        highlight(collectPartsToReveal(selection), DEFAULT_ALPHA);
    }

    public void highlight(ISelection selection, int alpha) {
        highlight(collectPartsToReveal(selection), alpha);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.xmind.ui.internal.presentation.IHighlightService#highlight(java.util
     * .List)
     */
    public void highlight(List<IGraphicalPart> toHighlight) {
        highlight(toHighlight, DEFAULT_ALPHA);
    }

    public void highlight(List<IGraphicalPart> toHighlight, int alpha) {
        this.alpha = alpha;

        setHighlightArea(null, alpha);
        if (highlights != null) {
            removeFromLayer(highlights);
            highlights = null;
        }

        highlights = toHighlight;
        if (highlights != null && !highlights.isEmpty() && isActive()) {
            addToLayer(highlights);
            MindMapUIPlugin.getDefault().getUsageDataCollector().trackEvent(
                    UserDataConstants.CATEGORY_ADVANCED_FILTER,
                    UserDataConstants.FILTER_DARKER);
        }
    }

    private List<IGraphicalPart> collectPartsToReveal(ISelection selection) {
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection ss = (IStructuredSelection) selection;
            List<IGraphicalPart> list = new ArrayList<IGraphicalPart>(
                    ss.size());
            for (Object o : ss.toList()) {
                IGraphicalPart p = getViewer().findGraphicalPart(o);
                if (p != null && !exclude(p)) {
                    list.add(p);
                }
            }
            return list;
        }
        return null;
    }

    protected boolean exclude(IGraphicalPart part) {
        return part instanceof ISheetPart;
    }

    private void addToLayer(List<IGraphicalPart> parts) {
        if (relayeredPane != null) {
            if (relayeredPane instanceof IUseTransparency) {
                ((IUseTransparency) relayeredPane).setMainAlpha(alpha);
            }
            for (IGraphicalPart p : parts) {
                IFigure figure = p.getFigure();
                if (figure instanceof IRelayerableFigure) {
                    relayeredPane
                            .addRelayerableFigure((IRelayerableFigure) figure);
                }
            }
        }
    }

    private void removeFromLayer(List<IGraphicalPart> parts) {
        if (relayeredPane != null) {
            for (IGraphicalPart p : parts) {
                IFigure figure = p.getFigure();
                if (figure instanceof IRelayerableFigure) {
                    relayeredPane.removeRelayerableFigure(
                            (IRelayerableFigure) figure);
                }
            }
        }
    }

}
