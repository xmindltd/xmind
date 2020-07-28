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
package org.xmind.gef.ui.editor;

import org.eclipse.draw2d.FreeformFigure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.SWTGraphics;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.draw2d.IOriginBased;
import org.xmind.gef.draw2d.geometry.Geometry;
import org.xmind.gef.draw2d.graphics.ScaledGraphics;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.gef.part.IPart;

public class GraphicalEditorPagePopupPreviewHelper {

    private static final int SHELL_MAX_WIDTH = 200;

    private static final int SHELL_MAX_HEIGHT = 200;

    private static final int CONTENTS_MAX_WIDTH = 1000;

    private static final int CONTENTS_MAX_HEIGHT = 1000;

    private static final int SPACING = 5;

    private final IGraphicalEditor editor;

    private CTabFolder tabFolder;

    private int index = -1;

    private IFigure paintingContents = null;

    private IFigure boundsContents = null;

    private Rectangle bounds = null;

    private Shell popup = null;

    private int borderWidth = 0;

    public GraphicalEditorPagePopupPreviewHelper(IGraphicalEditor editor,
            CTabFolder tabFolder) {
        this.editor = editor;
        this.tabFolder = tabFolder;
        hookTabFolder();
    }

    public int getBorderWidth() {
        return borderWidth;
    }

    public void setBorderWidth(int borderWidth) {
        this.borderWidth = borderWidth;
    }

    protected int getAppliedBorderWidth() {
        if (Util.isMac())
            return getBorderWidth() + 1;
        return getBorderWidth();
    }

    private void hookTabFolder() {
        final Listener listener = new Listener() {
            public void handleEvent(Event event) {
                switch (event.type) {
                case SWT.MouseHover:
                    showPopup(event);
                    break;
                case SWT.MouseMove:
                    checkPopup(event);
                    break;
                case SWT.MouseExit:
                case SWT.MouseEnter:
                case SWT.MouseDown:
                case SWT.MouseUp:
                case SWT.MouseDoubleClick:
                case SWT.MouseWheel:
                case SWT.Dispose:
                case SWT.Deactivate:
                case SWT.FocusOut:
                    hidePopup();
                    break;
                }
            }
        };
        tabFolder.addListener(SWT.MouseHover, listener);
        tabFolder.addListener(SWT.MouseMove, listener);
        tabFolder.addListener(SWT.MouseEnter, listener);
        tabFolder.addListener(SWT.MouseExit, listener);
        tabFolder.addListener(SWT.MouseDown, listener);
        tabFolder.addListener(SWT.MouseDoubleClick, listener);
        tabFolder.addListener(SWT.MouseUp, listener);
        tabFolder.addListener(SWT.MouseWheel, listener);
        tabFolder.addListener(SWT.Dispose, listener);
        tabFolder.addListener(SWT.FocusOut, listener);
        tabFolder.getShell().addListener(SWT.Deactivate, listener);
        tabFolder.addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                if (tabFolder != null && !tabFolder.isDisposed()) {
                    tabFolder.getShell().removeListener(SWT.Deactivate,
                            listener);
                }

            }
        });
    }

    private void showPopup(Event e) {
        if (popup != null)
            return;

        CTabItem item = tabFolder.getItem(new Point(e.x, e.y));
        if (item != null) {
            int index = tabFolder.indexOf(item);
            if (index >= 0) {
                IGraphicalEditorPage page = editor.getPage(index);
                if (page != null && !page.isDisposed()) {
                    IGraphicalViewer viewer = page.getViewer();
                    if (viewer != null && !viewer.getControl().isDisposed()) {
                        createPopup(item, viewer);
                        this.index = index;
                    }
                }
            }
        }
    }

    private void createPopup(CTabItem item, IGraphicalViewer viewer) {
        this.paintingContents = getPaintingContents(viewer);
        this.boundsContents = getBoundsContents(paintingContents, viewer);
        if (paintingContents != null) {
            this.bounds = calcContentsBounds(boundsContents, viewer);
            createPopup(item);
            if (popup != null && !popup.isDisposed())
                popup.setVisible(true);
        }
    }

    protected IFigure getPaintingContents(IGraphicalViewer viewer) {
        IFigure contents = viewer.getCanvas().getViewport().getContents();
        if (contents != null)
            return contents;
        return viewer.getCanvas().getLightweightSystem().getRootFigure();
    }

    protected IFigure getBoundsContents(IFigure paintingContents,
            IGraphicalViewer viewer) {
        IPart contentsPart = viewer.getRootPart().getContents();
        if (contentsPart instanceof IGraphicalPart
                && contentsPart.getStatus().isActive()) {
            return ((IGraphicalPart) contentsPart).getFigure();
        }
        return paintingContents;
    }

    protected Rectangle calcContentsBounds(IFigure contents,
            IGraphicalViewer viewer) {
        Rectangle bounds = getBaseBounds(contents);
        int width = Math.min(bounds.width, CONTENTS_MAX_WIDTH);
        int height = Math.min(bounds.height, CONTENTS_MAX_HEIGHT);
        org.eclipse.draw2d.geometry.Point origin = getOrigin(contents, viewer);
        if (origin != null) {
            int x = origin.x - width / 2;
            int y = origin.y - height / 2;
            bounds.x = Math.max(bounds.x,
                    Math.min(bounds.x + bounds.width - width, x));
            bounds.y = Math.max(bounds.y,
                    Math.min(bounds.y + bounds.height - height, y));
        } else {
            bounds.x = -width / 2;
            bounds.y = -height / 2;
        }
        bounds.width = width;
        bounds.height = height;
        return bounds;
    }

    protected Rectangle getBaseBounds(IFigure contents) {
        if (contents instanceof FreeformFigure)
            return ((FreeformFigure) contents).getFreeformExtent().getCopy();
        return contents.getBounds().getCopy();
    }

    protected org.eclipse.draw2d.geometry.Point getOrigin(IFigure contents,
            IGraphicalViewer viewer) {
        if (contents instanceof IOriginBased) {
            return ((IOriginBased) contents).getOrigin();
        }
        return null;
    }

    private void createPopup(CTabItem item) {
        this.popup = new Shell(editor.getSite().getShell(),
                getPopupShellStyle());
        hookPopup(popup);
        configurePopup(popup, item);
    }

    protected int getPopupShellStyle() {
        int style = SWT.NO_FOCUS | SWT.ON_TOP | SWT.NO_BACKGROUND;
        if (Util.isMac()) {
            style |= SWT.NO_TRIM;
        } else {
            style |= SWT.RESIZE;
        }
        return style;
    }

    private void hookPopup(Shell popup) {
        Listener listener = new Listener() {
            public void handleEvent(Event event) {
                switch (event.type) {
                case SWT.Paint:
                    paintPopup(event);
                    break;
                case SWT.MouseEnter:
                case SWT.MouseExit:
                case SWT.MouseMove:
                case SWT.MouseDown:
                case SWT.MouseUp:
                case SWT.MouseWheel:
                case SWT.MouseHover:
                case SWT.Activate:
                case SWT.FocusIn:
                case SWT.KeyDown:
                case SWT.KeyUp:
                    hidePopup();
                    break;
                }
            }
        };
        popup.addListener(SWT.Paint, listener);
        popup.addListener(SWT.MouseEnter, listener);
        popup.addListener(SWT.MouseExit, listener);
        popup.addListener(SWT.MouseMove, listener);
        popup.addListener(SWT.MouseDown, listener);
        popup.addListener(SWT.MouseUp, listener);
        popup.addListener(SWT.MouseWheel, listener);
        popup.addListener(SWT.MouseHover, listener);
        popup.addListener(SWT.Activate, listener);
        popup.addListener(SWT.FocusIn, listener);
        popup.addListener(SWT.KeyDown, listener);
        popup.addListener(SWT.KeyUp, listener);
    }

    protected void configurePopup(Shell popup, CTabItem item) {
        int doubleBorderWidth = getAppliedBorderWidth() * 2;
        int maxWidth = SHELL_MAX_WIDTH - doubleBorderWidth;
        int maxHeight = SHELL_MAX_HEIGHT - doubleBorderWidth;
        Dimension size = Geometry.getScaledConstrainedSize(bounds.width,
                bounds.height, maxWidth, maxHeight);
        size.expand(doubleBorderWidth, doubleBorderWidth);
        org.eclipse.swt.graphics.Rectangle trim = popup.computeTrim(0, 0,
                size.width, size.height);
        int width = trim.width;
        int height = trim.height;
        popup.setSize(width, height);

        org.eclipse.swt.graphics.Rectangle itemBounds = item.getBounds();
        Point leftTop = tabFolder.toDisplay(itemBounds.x, itemBounds.y);

        org.eclipse.swt.graphics.Rectangle displayArea = Display.getCurrent()
                .getClientArea();
        int x = leftTop.x + itemBounds.width / 2 - width / 2;
        if (x + width > displayArea.x + displayArea.width) {
            x = displayArea.x + displayArea.width - width;
        }
        if (x < displayArea.x) {
            x = displayArea.x;
        }

        int y;
        if (tabFolder.getTabPosition() == SWT.BOTTOM) {
            y = leftTop.y - SPACING - height;
            if (y < displayArea.y) {
                y = displayArea.y;
            }
        } else {
            y = leftTop.y + itemBounds.height + SPACING;
            if (y + height > displayArea.y + displayArea.height) {
                y = displayArea.y + displayArea.height - height;
            }
        }
        popup.setLocation(x, y);
    }

    protected void paintPopup(Event e) {
        org.eclipse.swt.graphics.Rectangle clientArea = popup.getClientArea();
        int border = getAppliedBorderWidth();
        int x = clientArea.x + border;
        int y = clientArea.y + border;
        double w = clientArea.width - border - border;
        double h = clientArea.height - border - border;

        GC gc = e.gc;
        SWTGraphics swtGraphics = new SWTGraphics(gc);
        swtGraphics.pushState();

        double horizontalScale = w / bounds.width;
        double verticalScale = h / bounds.height;
        double scale = Math.max(horizontalScale, verticalScale);

        swtGraphics.translate(x, y);

        Graphics g = swtGraphics;
        ScaledGraphics sg = null;
        if (ScaledGraphics.SCALED_GRAPHICS_ENABLED) {
            sg = new ScaledGraphics(swtGraphics);
            sg.scale(scale);
            g = sg;
        } else {
            g.scale(scale);
        }

        g.translate(-bounds.x, -bounds.y);

        try {
            paintFigure(paintingContents, g);
        } finally {
            if (sg != null) {
                sg.dispose();
            }
            swtGraphics.popState();
            swtGraphics.dispose();
        }

        if (border > 0) {
            gc.setForeground(e.display.getSystemColor(SWT.COLOR_GRAY));
            gc.setAlpha(0xff);
            gc.setLineWidth(border);
            gc.setLineStyle(SWT.LINE_SOLID);
            gc.setClipping(clientArea.x, clientArea.y, clientArea.width,
                    clientArea.height);
            gc.drawRectangle(clientArea.x + border / 2,
                    clientArea.y + border / 2, clientArea.width - border,
                    clientArea.height - border);
        }
    }

    protected void paintFigure(IFigure figure, Graphics graphics) {
        figure.paint(graphics);
    }

    private void hidePopup() {
        if (popup != null) {
            popup.dispose();
            popup = null;
        }
        paintingContents = null;
        boundsContents = null;
        bounds = null;
        index = -1;
    }

    private void checkPopup(Event e) {
        CTabItem item = tabFolder.getItem(new Point(e.x, e.y));
        if (item == null) {
            hidePopup();
            return;
        }

        int index = tabFolder.indexOf(item);
        if (index < 0) {
            hidePopup();
            return;
        } else if (index != this.index) {
            boolean showing = popup != null;
            hidePopup();
            if (showing)
                showPopup(e);
            return;
        }

        IGraphicalEditorPage page = editor.getPage(index);
        if (page == null || page.isDisposed()) {
            hidePopup();
            return;
        }

        IGraphicalViewer viewer = page.getViewer();
        if (viewer == null || viewer.getControl().isDisposed()) {
            hidePopup();
            return;
        }

    }

}
