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
package org.xmind.gef.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.FigureListener;
import org.eclipse.draw2d.IFigure;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.IZoomListener;
import org.xmind.gef.ZoomManager;
import org.xmind.gef.ZoomObject;
import org.xmind.gef.draw2d.IRotatableFigure;
import org.xmind.gef.draw2d.SelectionFigure;
import org.xmind.gef.draw2d.geometry.PrecisionDimension;
import org.xmind.gef.draw2d.geometry.PrecisionPoint;
import org.xmind.gef.draw2d.geometry.PrecisionRectangle;
import org.xmind.gef.draw2d.geometry.PrecisionRotator;

public class FeedbackService extends GraphicalViewerService implements
        IZoomListener, FocusListener, FigureListener, IFeedbackService {

    private static final List<IFeedback> NO_FEEDBACK = Collections.emptyList();

    private IFigure layer;

    private ZoomManager zoom;

    private List<IFeedback> feedbacks = null;

    private Map<IFigure, SelectionFigure> selections = new HashMap<IFigure, SelectionFigure>();

    private IColorProvider selectionColorProvider = null;

    private int selectionLineWidth = 2;

    private int selectionCorner = 5;

    public FeedbackService(IGraphicalViewer viewer) {
        super(viewer);
    }

    public int getSelectionCorner() {
        return selectionCorner;
    }

    public void setSelectionCorner(int selectionCorner) {
        this.selectionCorner = selectionCorner;
    }

    public int getSelectionLineWidth() {
        return selectionLineWidth;
    }

    public void setSelectionLineWidth(int selectionLineWidth) {
        this.selectionLineWidth = selectionLineWidth;
    }

    public List<IFeedback> getFeedbackParts() {
        if (feedbacks == null)
            return NO_FEEDBACK;
        return feedbacks;
    }

    protected void activate() {
        setZoomManager(getViewer().getZoomManager());
    }

    protected void deactivate() {
        setZoomManager(null);
    }

    protected void hookControl(Control control) {
        control.addFocusListener(this);
    }

    protected void unhookControl(Control canvas) {
        canvas.removeFocusListener(this);
    }

    public void setLayer(IFigure layer) {
        IFigure oldLayer = this.layer;
        this.layer = layer;

        if (feedbacks != null) {
            if (oldLayer == null && layer != null) {
                for (IFeedback part : feedbacks) {
                    part.addToLayer(layer);
                    part.update();
                }
            } else if (oldLayer != null && layer == null) {
                for (IFeedback part : feedbacks) {
                    part.removeFromLayer(oldLayer);
                }
            }
        }
    }

    public void focusGained(FocusEvent e) {
        if (getControl() != null && !getControl().isDisposed()) {
            Display display = getControl().getDisplay();
            if (display != null) {
                display.asyncExec(new Runnable() {
                    public void run() {
                        if (getControl() != null && !getControl().isDisposed())
                            refresh();
                    }
                });
            }
        }
    }

    public void focusLost(FocusEvent e) {
        if (getControl() != null && !getControl().isDisposed()) {
            Display display = getControl().getDisplay();
            if (display != null) {
                display.asyncExec(new Runnable() {
                    public void run() {
                        if (getControl() != null && !getControl().isDisposed())
                            refresh();
                    }
                });
            }
        }
    }

    private void setZoomManager(ZoomManager zoom) {
        if (this.zoom == zoom)
            return;
        if (this.zoom != null)
            this.zoom.removeZoomListener(this);
        this.zoom = zoom;
        if (this.zoom != null)
            this.zoom.addZoomListener(this);
        refresh();
    }

    /**
     * @see cn.brainy.gef.core.IZoomListener#scaleChanged(cn.brainy.gef.core.ZoomObject,
     *      double, double)
     */
    public void scaleChanged(ZoomObject source, double oldValue, double newValue) {
        if (isActive() && !isDisposed()) {
            updateAllFeedback();
            updateAllSelections();
        }
    }

    public void refresh() {
        if (isActive() && !isDisposed()) {
            updateAllFeedback();
            updateAllSelections();
            updateSelectionColors();
        }
    }

    private void updateAllFeedback() {
        if (feedbacks != null) {
            for (Object o : feedbacks.toArray()) {
                ((IFeedback) o).update();
            }
        }
    }

    private void updateAllSelections() {
        for (IFigure source : selections.keySet()) {
            updateSelection(source);
        }
    }

    public void addFeedback(IFeedback feedback) {
        if (feedback == null)
            return;
        if (feedbacks != null && feedbacks.contains(feedback))
            return;

        if (feedbacks == null)
            feedbacks = new ArrayList<IFeedback>();
        feedbacks.add(feedback);
        feedback.setZoomManager(zoom);
        if (layer != null)
            feedback.addToLayer(layer);
        feedback.update();
    }

    public void removeFeedback(IFeedback feedback) {
        if (feedback == null)
            return;
        if (feedbacks == null || !feedbacks.contains(feedback))
            return;

        if (layer != null)
            feedback.removeFromLayer(layer);
        feedback.setZoomManager(null);
        feedbacks.remove(feedback);
    }

    /**
     * @param selectionColorProvider
     *            the selectionColorProvider to set
     */
    public void setSelectionColorProvider(IColorProvider selectionColorProvider) {
        this.selectionColorProvider = selectionColorProvider;
    }

    /**
     * @return the selectionColorProvider
     */
    public IColorProvider getSelectionColorProvider() {
        return selectionColorProvider;
    }

    public SelectionFigure addSelection(IFigure source) {
        removeSelection(source);
        if (layer == null)
            return null;

        SelectionFigure selection = createSelectionFigure();
        selections.put(source, selection);
        updateSelection(source);
//        updateSelectionColors();
        layer.add(selection);
        source.addFigureListener(this);
        return selection;
    }

    private void updateSelectionColors() {
        IColorProvider colorProvider = getSelectionColorProvider();
        if (colorProvider != null) {
            Color focusColor;
            Color selectionColor;
            Color preselectionColor;
            Color focusFillColor;
            Color selectionFillColor;
            Color preselectionFillColor;
            boolean disabled = getControl() != null
                    && !getControl().isDisposed()
                    && !getControl().isFocusControl();
            if (disabled) {
                focusColor = colorProvider.getForeground(DisabledFocusColor);
                selectionColor = colorProvider
                        .getForeground(DisabledSelectionColor);
                preselectionColor = colorProvider
                        .getForeground(DisabledPreselectionColor);
                focusFillColor = colorProvider
                        .getBackground(DisabledFocusColor);
                selectionFillColor = colorProvider
                        .getBackground(DisabledSelectionColor);
                preselectionFillColor = colorProvider
                        .getBackground(DisabledPreselectionColor);
            } else {
                focusColor = colorProvider.getForeground(FocusColor);
                selectionColor = colorProvider.getForeground(SelectionColor);
                preselectionColor = colorProvider
                        .getForeground(PreselectionColor);
                focusFillColor = colorProvider.getBackground(FocusColor);
                selectionFillColor = colorProvider
                        .getBackground(SelectionColor);
                preselectionFillColor = colorProvider
                        .getBackground(PreselectionColor);
            }
            for (SelectionFigure f : selections.values()) {
                f.setFocusColor(focusColor);
                f.setSelectionColor(selectionColor);
                f.setPreselectionColor(preselectionColor);
                f.setFocusFillColor(focusFillColor);
                f.setSelectionFillColor(selectionFillColor);
                f.setPreselectionFillColor(preselectionFillColor);
            }
        }
    }

    /**
     * @return
     */
    private SelectionFigure createSelectionFigure() {
        SelectionFigure f = new SelectionFigure();
        IColorProvider colorProvider = getSelectionColorProvider();
        if (colorProvider != null) {
            Color focusColor;
            Color selectionColor;
            Color preselectionColor;
            Color focusFillColor;
            Color selectionFillColor;
            Color preselectionFillColor;
            boolean disabled = getControl() != null
                    && !getControl().isDisposed()
                    && !getControl().isFocusControl();
            if (disabled) {
                focusColor = colorProvider.getForeground(DisabledFocusColor);
                selectionColor = colorProvider
                        .getForeground(DisabledSelectionColor);
                preselectionColor = colorProvider
                        .getForeground(DisabledPreselectionColor);
                focusFillColor = colorProvider
                        .getBackground(DisabledFocusColor);
                selectionFillColor = colorProvider
                        .getBackground(DisabledSelectionColor);
                preselectionFillColor = colorProvider
                        .getBackground(DisabledPreselectionColor);
            } else {
                focusColor = colorProvider.getForeground(FocusColor);
                selectionColor = colorProvider.getForeground(SelectionColor);
                preselectionColor = colorProvider
                        .getForeground(PreselectionColor);
                focusFillColor = colorProvider.getBackground(FocusColor);
                selectionFillColor = colorProvider
                        .getBackground(SelectionColor);
                preselectionFillColor = colorProvider
                        .getBackground(PreselectionColor);
            }
            f.setFocusColor(focusColor);
            f.setSelectionColor(selectionColor);
            f.setPreselectionColor(preselectionColor);
            f.setFocusFillColor(focusFillColor);
            f.setSelectionFillColor(selectionFillColor);
            f.setPreselectionFillColor(preselectionFillColor);
        }
        f.setLineWidth(selectionLineWidth);
        return f;
    }

    public SelectionFigure removeSelection(IFigure source) {
        SelectionFigure old = selections.remove(source);
        if (old != null && old.getParent() == layer) {
            if (layer != null)
                layer.remove(old);
            disposeSelection(old);
        }
        source.removeFigureListener(this);
        return old;
    }

    /**
     * @param source
     * @return
     */
    public SelectionFigure getSelectionFigure(IFigure source) {
        SelectionFigure figure = selections.get(source);
        return (figure == null) ? addSelection(source) : figure;
    }

    public SelectionFigure setSelected(IFigure source) {
        SelectionFigure figure = getSelectionFigure(source);
        if (figure != null) {
            figure.setFocused(false);
            figure.setPreselected(false);
            figure.setSelected(true);
        }
        return figure;
    }

    public SelectionFigure setPreselected(IFigure source) {
        SelectionFigure figure = getSelectionFigure(source);
        if (figure != null) {
            figure.setFocused(false);
            figure.setSelected(false);
            figure.setPreselected(true);
        }
        return figure;
    }

    public SelectionFigure setFocused(IFigure source) {
        SelectionFigure figure = getSelectionFigure(source);
        if (figure != null) {
            figure.setSelected(false);
            figure.setPreselected(false);
            figure.setFocused(true);
        }
        return figure;
    }

    /**
     * @see org.eclipse.draw2d.FigureListener#figureMoved(org.eclipse.draw2d.IFigure)
     */
    public void figureMoved(IFigure source) {
        updateSelection(source);
    }

    private void updateSelection(IFigure source) {
        SelectionFigure selection = selections.get(source);
        if (selection != null) {
            disposeSelection(selection);
            org.xmind.gef.draw2d.graphics.Path p = new org.xmind.gef.draw2d.graphics.Path(
                    Display.getCurrent());
            int lineWidth = selectionLineWidth;
            if (zoom != null) {
                lineWidth *= zoom.getScale();
                if (lineWidth < 2)
                    lineWidth = 2;
            }
            selection.setLineWidth(lineWidth);
            int exp = lineWidth;
            double halfExp = aZoom(exp * 0.5 + 0.5);
            PrecisionRectangle bounds = new PrecisionRectangle(source
                    .getBounds());//.resize(-1, -1);

            if ("win32".equals(SWT.getPlatform())) { //$NON-NLS-1$
                bounds.resize(-1, -1);
            }
            if (source instanceof IRotatableFigure
                    && ((IRotatableFigure) source).getRotationDegrees() != 0) {
                IRotatableFigure rf = (IRotatableFigure) source;
                PrecisionDimension size = rf.getNormalPreferredSize(-1, -1);
                PrecisionPoint c = bounds.getCenter();
                PrecisionRotator r = new PrecisionRotator(c);
                r.setAngle(rf.getRotationDegrees());
                PrecisionRectangle b = r.r(bounds, -1, size.height);
                b.expand(halfExp, halfExp);
                p.addRoundedPolygon(selectionCorner, zoom(r.t(b.getTopLeft())),
                        zoom(r.t(b.getTopRight())), zoom(r
                                .t(b.getBottomRight())), zoom(r.t(b
                                .getBottomLeft())));
            } else {

                p.addRoundedRectangle(zoom(bounds.expand(halfExp, halfExp)),
                        selectionCorner);
            }
            selection.setPath(p);
//            selection.setBounds(selection.getPreferredBounds());
        }
    }

    private double aZoom(double d) {
        return zoom == null ? d : d / zoom.getScale();
    }

    private PrecisionPoint zoom(PrecisionPoint p) {
        return zoom == null ? p : p.scale(zoom.getScale());
    }

    private PrecisionRectangle zoom(PrecisionRectangle r) {
        return zoom == null ? r : r.scale(zoom.getScale());
    }

    private void disposeSelection(SelectionFigure s) {
        Path p = s.getPath();
        if (p != null && !p.isDisposed()) {
            s.setPath(null);
            p.dispose();
        }
    }

    public void dispose() {
        if (selections != null) {
            for (Object key : selections.keySet().toArray()) {
                removeSelection((IFigure) key);
            }
            selections.clear();
        }
        super.dispose();
    }

    public void addSkylight(IFigure figure) {
    }

    public void removeSkylight(IFigure figure) {
    }

}