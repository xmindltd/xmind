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
package org.xmind.ui.internal.tools;

import org.eclipse.core.runtime.Assert;
import org.eclipse.draw2d.Cursors;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.Layer;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.xmind.core.Core;
import org.xmind.gef.GEF;
import org.xmind.gef.Request;
import org.xmind.gef.event.MouseDragEvent;
import org.xmind.gef.event.MouseEvent;
import org.xmind.gef.part.IGraphicalEditPart;
import org.xmind.gef.part.IPart;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.resources.ColorUtils;
import org.xmind.ui.texteditor.FloatingTextEditor;
import org.xmind.ui.tools.TitleEditTool;

public class TopicTitleEditTool extends TitleEditTool {

    private static class DragHandle extends Figure {

        private Color triangleColor = ColorUtils.getColor("#606060"); //$NON-NLS-1$

        public DragHandle() {
            setForegroundColor(ColorUtils.getColor("#009900")); //$NON-NLS-1$
            setBackgroundColor(ColorUtils.getColor("#33cc33")); //$NON-NLS-1$
        }

        protected void paintFigure(Graphics graphics) {
            super.paintFigure(graphics);
            graphics.setAntialias(SWT.ON);
            graphics.setAlpha(0xd0);
            Rectangle r = getBounds();
            graphics.fillRectangle(r.x, r.y, r.width, r.height);

            int cy = r.y + r.height / 2;
            int cx = r.x + r.width / 2;
            graphics.setBackgroundColor(triangleColor);
            graphics.fillPolygon(new int[] { cx - 2, cy - 1, cx - 2, cy - 8,
                    cx + 2, cy - 5 });
            graphics.fillPolygon(new int[] { cx - 2, cy + 5, cx + 2, cy + 8,
                    cx + 2, cy + 1 });

            graphics.drawRectangle(r.x, r.y, r.width - 1, r.height - 1);
        }

        public boolean containsPoint(int x, int y) {
            Rectangle r = getBounds();
            return y >= r.y - 2 && y < r.y + r.height + 2 && x >= r.x - 2
                    && x < r.x + r.width + 2;
        }

    }

    private static final int HANDLE_WIDTH = 8;

    private IFigure widthHandle;

    private boolean locatingHandle = false;

    private boolean mouseDownOnHandle = false;

    private boolean draggingHandle = false;

    private boolean widthChanged = false;

    private int width = -1;

    public String getType() {
        return MindMapUI.TOOL_EDIT_TOPIC_TITLE;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.xmind.gef.tool.EditTool#canEdit(org.xmind.gef.part.IGraphicalEditPart
     * )
     */
    @Override
    protected boolean canEdit(IGraphicalEditPart target) {
        return target instanceof ITopicPart;
    }

    public void setSource(IGraphicalEditPart source) {
        Assert.isTrue(source instanceof ITopicPart);
        super.setSource(source);
    }

    protected void hookEditorControl(FloatingTextEditor editor,
            ITextViewer textViewer) {
        super.hookEditorControl(editor, textViewer);
        widthHandle = createHandle();
        if (widthHandle != null) {
            editor.getControl().addControlListener(new ControlListener() {

                public void controlResized(ControlEvent e) {
                    locateHandle((Control) e.widget);
                }

                public void controlMoved(ControlEvent e) {
                    locateHandle((Control) e.widget);
                }
            });
            locateHandle(editor.getControl());
        }
    }

    private void locateHandle(final Control control) {
        if (widthHandle == null)
            return;

        if (locatingHandle)
            return;

        locatingHandle = true;
        Display.getCurrent().asyncExec(new Runnable() {
            public void run() {
                if (widthHandle == null || getTargetViewer() == null
                        || getTargetViewer().getControl() == null
                        || getTargetViewer().getControl().isDisposed()
                        || control == null || control.isDisposed()) {
                    locatingHandle = false;
                    return;
                }

                if (width < 0 && control.getBounds().width > 500) {
                    widthChanged = true;
                    getHelper().setPrefWidth(500);
                }
                Rectangle bounds = new Rectangle(control.getBounds());
                Point loc = getTargetViewer()
                        .computeToLayer(bounds.getLocation(), false);
                widthHandle.setBounds(new Rectangle(loc.x + bounds.width, loc.y,
                        HANDLE_WIDTH, bounds.height));
                locatingHandle = false;
            }
        });
    }

    private IFigure createHandle() {
        Layer layer = getTargetViewer().getLayer(GEF.LAYER_FEEDBACK);
        if (layer == null)
            return null;

        DragHandle handle = new DragHandle();
        layer.add(handle);
        return handle;
    }

    private boolean handleContains(IFigure handle, Point p) {
        return handle.containsPoint(getScaled(p));
    }

    protected boolean shouldFinishOnMouseDown(MouseEvent me) {
        if (widthHandle != null) {
            if (handleContains(widthHandle, me.cursorLocation)) {
                mouseDownOnHandle = true;
                return false;
            }
        }
        mouseDownOnHandle = false;
        return super.shouldFinishOnMouseDown(me);
    }

    @Override
    protected boolean openEditor(FloatingTextEditor editor,
            IDocument document) {
        boolean opened = super.openEditor(editor, document);
        mouseDownOnHandle = false;
        draggingHandle = false;
        widthChanged = false;
        return opened;
    }

    @Override
    protected void hookEditor(FloatingTextEditor editor) {
        super.hookEditor(editor);
        width = ((ITopicPart) getSource()).getTopic().getTitleWidth();
        if (getHelper() != null) {
            getHelper().setPrefWidth(width);
        }
    }

    @Override
    protected void unhookEditor(FloatingTextEditor editor) {
        super.unhookEditor(editor);
        if (widthHandle != null) {
            if (widthHandle.getParent() != null) {
                widthHandle.getParent().remove(widthHandle);
            }
            widthHandle = null;
        }
    }

    @Override
    protected void closeEditor(FloatingTextEditor editor, boolean finish) {
        super.closeEditor(editor, finish);
        mouseDownOnHandle = false;
        draggingHandle = false;
    }

    protected boolean handleMouseDrag(MouseDragEvent me) {
        if (mouseDownOnHandle) {
            draggingHandle = true;
        }
        if (draggingHandle) {
            Point p = getScaled(me.cursorLocation);
            Point leftTop = getTargetViewer().computeToLayer(
                    new Point(getEditor().getControl().getLocation()), false);
            width = Math.max(20, (int) ((p.x - leftTop.x) / getScale()));
            getHelper().setPrefWidth(width);
            getHelper().refreshEditor(false);
            return true;
        }
        return super.handleMouseDrag(me);
    }

    protected boolean handleMouseEntered(MouseEvent me) {
        if (mouseDownOnHandle || draggingHandle)
            return true;
        return super.handleMouseEntered(me);
    }

    protected boolean handleMouseUp(MouseEvent me) {
        if (!widthChanged && draggingHandle) {
            widthChanged = true;
        }
        draggingHandle = false;
        mouseDownOnHandle = false;
        return super.handleMouseUp(me);
    }

    protected Request createTextRequest(IPart source, IDocument document) {
        Request request = super.createTextRequest(source, document);
        if (widthChanged) {
            request.setParameter(
                    MindMapUI.PARAM_PROPERTY_PREFIX + Core.TitleWidth,
                    getHelper().getPrefWidth());
        }
        return request;
    }

    protected boolean shouldIgnoreTextChange(IPart source, IDocument document,
            String oldText) {
        return !widthChanged
                && super.shouldIgnoreTextChange(source, document, oldText);
    }

    private double getScale() {
        return getTargetViewer().getZoomManager().getScale();
    }

    private Point getScaled(Point p) {
        return getTargetViewer().getZoomManager().getScaled(p);
    }

    public IFigure findToolTip(IPart source, Point position) {
        if (!mouseDownOnHandle && !draggingHandle && (widthHandle != null
                && handleContains(widthHandle, position))) {
            return new Label(MindMapMessages.ModifyWrapWidth_toolTip0);
        }
        return super.getToolTip(source, position);
    }

    public Cursor getCurrentCursor(Point pos, IPart host) {
        if (mouseDownOnHandle || draggingHandle
                || (widthHandle != null && handleContains(widthHandle, pos))) {
            return Cursors.SIZEWE;
        }
        return super.getCurrentCursor(pos, host);
    }
}
