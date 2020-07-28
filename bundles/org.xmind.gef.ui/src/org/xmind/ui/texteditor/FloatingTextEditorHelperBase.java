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
package org.xmind.ui.texteditor;

import org.eclipse.draw2d.Viewport;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import org.xmind.gef.GraphicalViewer;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.IZoomListener;
import org.xmind.gef.ZoomManager;
import org.xmind.gef.ZoomObject;

public abstract class FloatingTextEditorHelperBase
        implements IFloatingTextEditorListener, IZoomListener, Listener {

    private final static int GAP = 20;

    private FloatingTextEditor editor = null;

    private IGraphicalViewer viewer = null;

    private ZoomManager zoomManager = null;

    private int minWidth = -1;

    private int maxWidth = -1;

    private int prefWidth = -1;

    private boolean extendsBidirectionalHorizontal;

    private int expansion = 100;

    public FloatingTextEditorHelperBase() {
        this(false);
    }

    public FloatingTextEditorHelperBase(
            boolean extendsBidirectionalHorizontal) {
        this.extendsBidirectionalHorizontal = extendsBidirectionalHorizontal;
    }

    public boolean isExtendsBidirectionalHorizontal() {
        return extendsBidirectionalHorizontal;
    }

    public void setExtendsBidirectionalHorizontal(
            boolean extendsBidirectionalHorizontal) {
        this.extendsBidirectionalHorizontal = extendsBidirectionalHorizontal;
    }

    public FloatingTextEditor getEditor() {
        return editor;
    }

    public IGraphicalViewer getViewer() {
        return viewer;
    }

    public ZoomManager getZoomManager() {
        return zoomManager;
    }

    public int getMinWidth() {
        return minWidth;
    }

    public void setMinWidth(int minWidth) {
        this.minWidth = minWidth;
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public void setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
    }

    public void setEditor(FloatingTextEditor editor) {
        this.editor = editor;
    }

    public void setViewer(IGraphicalViewer viewer) {
        this.viewer = viewer;
    }

    public int getPrefWidth() {
        return prefWidth;
    }

    public void setPrefWidth(int prefWidth) {
        this.prefWidth = prefWidth;
    }

    public int getExpansion() {
        return expansion;
    }

    public void setExpansion(int expansion) {
        this.expansion = expansion;
    }

    public void activate() {
        if (editor != null) {
            editor.addFloatingTextEditorListener(this);
        }

        if (viewer != null) {
            Control control = viewer.getControl();
            if (control != null && !control.isDisposed()) {
                control.addListener(SWT.Resize, this);
            }
            this.zoomManager = viewer.getZoomManager();
            this.zoomManager.addZoomListener(this);
        }
    }

    public void deactivate() {
        if (editor != null) {
            editor.removeFloatingTextEditorListener(this);
            editor = null;
        }
        if (zoomManager != null) {
            zoomManager.removeZoomListener(this);
            zoomManager = null;
        }
        if (viewer != null) {
            Control control = viewer.getControl();
            if (control != null && !control.isDisposed()) {
                control.removeListener(SWT.Resize, this);
            }
            viewer = null;
        }
    }

    public void refreshEditor(boolean updateFont) {
        if (editor == null || editor.isClosed())
            return;

        if (updateFont) {
            updateEditorFont();
        }
        updateEditorBounds();
        ensureEditorVisible();
    }

    private void updateEditorBounds() {
        if (editor == null || editor.isClosed())
            return;

        Rectangle r = getPreferredBounds();
        if (r == null)
            return;

        translateToControl(r);

        StyledText textWidget = editor.getTextViewer().getTextWidget();
        org.eclipse.swt.graphics.Rectangle trim = textWidget.computeTrim(0, 0,
                0, 0);
        int widthHint;
        if (getPrefWidth() < 0) {
            widthHint = -1;
        } else {
            widthHint = Math.max(1,
                    (int) (getPrefWidth() * getScale()) - trim.width);
        }
        org.eclipse.swt.graphics.Point prefSize = textWidget
                .computeSize(widthHint, -1);
        int prefWidth = prefSize.x - trim.width;
        int prefHeight = prefSize.y - trim.height;

        Rectangle clientArea = getViewerClientArea();
        int maxWidth = -1;
        int maxHeight = -1;
        if (clientArea != null) {
            org.eclipse.swt.graphics.Rectangle trim2 = editor.computeTrim(0, 0,
                    0, 0);
            int gap = (int) (GAP * getScale());
            maxWidth = clientArea.width - trim2.width - gap;
            maxHeight = clientArea.height - trim2.height - gap;

            if (getMaxWidth() > 0 && getMaxWidth() < maxWidth) {
                maxWidth = (int) (getMaxWidth() * getScale());
            }

            if (prefWidth > maxWidth) {
                prefWidth = maxWidth;
            }
            if (prefHeight > maxHeight) {
                prefHeight = maxHeight;
            }
        }

        int minWidth;
        if (getMinWidth() > 0) {
            minWidth = (int) (getMinWidth() * getScale());
        } else {
            if (getPrefWidth() < 0) {
                minWidth = r.width;
            } else {
                minWidth = (int) (20 * getScale());
            }
        }
        if (prefWidth < minWidth) {
            prefWidth = minWidth;
        }

        prefSize = textWidget.computeSize(prefWidth, -1);
        prefSize.x -= trim.width;
        prefSize.y -= trim.height;
        if (getPrefWidth() < 0) {
            int f = (int) (getExpansion() * getScale());
            prefWidth = (prefSize.x + f - 1) / f * f + f * 2 / 5;
        }
        prefHeight = prefSize.y;
        if (maxWidth > 0) {
            prefWidth = Math.min(maxWidth, prefWidth);
        }
        if (maxHeight > 0) {
            prefHeight = Math.min(maxHeight, prefHeight);
        }

        if (extendsBidirectionalHorizontal) {
            r.x += (r.width - prefWidth) / 2;
        }
        r.y += (r.height - prefHeight) / 2;
        r.width = prefWidth;
        r.height = prefHeight;

        editor.getControl()
                .setBounds(editor.computeTrim(r.x, r.y, r.width, r.height));

        updateScrollBars(prefWidth < prefSize.x, prefHeight < prefSize.y);
    }

    protected Rectangle getViewerClientArea() {
        if (viewer != null) {
            return new Rectangle(viewer.getCanvas().getClientArea());
        }
        return null;
    }

    private void updateScrollBars(boolean hVisible, boolean vVisible) {
        if (editor == null)
            return;

        StyledText textWidget = editor.getTextViewer().getTextWidget();
        ScrollBar hBar = textWidget.getHorizontalBar();
        if (hBar != null) {
            hBar.setVisible(hVisible);
        }
        ScrollBar vBar = textWidget.getVerticalBar();
        if (vBar != null) {
            vBar.setVisible(vVisible);
        }
    }

    private void translateToControl(Rectangle r) {
        r.scale(getScale());
        Viewport viewport = getViewport();
        if (viewport != null) {
            Point viewLocation = viewport.getViewLocation();
            r.translate(-viewLocation.x, -viewLocation.y);
        }
    }

    protected Viewport getViewport() {
        if (viewer != null) {
            return viewer.getCanvas().getViewport();
        }
        return null;
    }

    private void updateEditorFont() {
        if (editor == null)
            return;

        ITextViewer textViewer = editor.getTextViewer();
        if (textViewer == null)
            return;

        Font font = getPreferredFont();
        if (font != null && !font.isDisposed()) {
            textViewer.getTextWidget().setFont(font);
        }
    }

    protected abstract Font getPreferredFont();

    protected StyleRange createStyleRange(TextStyle style, IDocument document) {
        if (style == null || document == null)
            return new StyleRange();

        StyleRange range = new StyleRange(0, document.getLength(), null, null);
        range.font = style.font;
        range.strikeout = style.strikeout;
        range.underline = style.underline;
        return range;
    }

    protected void ensureEditorVisible() {
        if (editor != null && viewer instanceof GraphicalViewer) {
            Rectangle r = new Rectangle(editor.getControl().getBounds());
            int gap = (int) (GAP / getScale());
            r.expand(gap, gap);
            ((GraphicalViewer) viewer).ensureControlVisible(r);
        }
    }

    protected double getScale() {
        if (zoomManager != null)
            return zoomManager.getScale();
        return 1;
    }

    protected abstract Rectangle getPreferredBounds();

    public void editingStarted(TextEvent e) {
        refreshEditor(true);
    }

    public void textChanged(TextEvent e) {
        refreshEditor(false);
    }

    public void editingAboutToCancel(TextEvent e) {
    }

    public void editingAboutToFinish(TextEvent e) {
    }

    public void editingAboutToStart(TextEvent e) {
    }

    public void editingCanceled(TextEvent e) {
    }

    public void editingFinished(TextEvent e) {
    }

    public void textAboutToChange(TextEvent e) {
    }

    public void scaleChanged(ZoomObject source, double oldValue,
            double newValue) {
        refreshEditor(true);
    }

    public void handleEvent(Event event) {
        refreshEditor(false);
    }

}
