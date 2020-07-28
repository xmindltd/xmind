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
package org.xmind.ui.internal.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.util.Util;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Layout;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.ui.actions.PageAction;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.gef.ui.editor.IPanel;
import org.xmind.gef.ui.editor.PanelContribution;
import org.xmind.gef.ui.editor.PanelContributor;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.mindmap.IDrillDownTraceListener;
import org.xmind.ui.mindmap.IDrillDownTraceService;
import org.xmind.ui.mindmap.IMindMap;
import org.xmind.ui.mindmap.MindMap;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.resources.ColorUtils;
import org.xmind.ui.resources.FontUtils;
import org.xmind.ui.util.MindMapUtils;

public class MindMapEditorPagePanelContributor extends PanelContributor {

    protected static class CrumbItem implements IPropertyChangeListener {

        private static final int H_MARGIN = 8;

        private static final int V_MARGIN = 2;

        private static final int M = 2;

        private static final int M2 = M + 1;

        private static final int W = M * 2 + 1;

        private static final int W2 = M2 * 2;

        private static final int C2 = W2 * 2 + 1;

        private static final int C = C2 + 1;

        private static final int SPACING = 1;

        private CrumbsBar bar;

        private IAction action;

        private Rectangle bounds;

        private boolean mouseOver;

        private boolean pressed;

        private Image image = null;

        public CrumbItem() {
            this(null);
        }

        public CrumbItem(IAction action) {
            this.action = action;
        }

        void setParent(CrumbsBar bar) {
            if (bar == this.bar)
                return;
            if (action != null) {
                if (this.bar == null && bar != null) {
                    action.addPropertyChangeListener(this);
                } else if (this.bar != null && bar == null) {
                    action.removePropertyChangeListener(this);
                }
            }
            this.bar = bar;
        }

        public void setMouseOver(boolean mouseOver) {
            if (mouseOver == this.mouseOver)
                return;
            this.mouseOver = mouseOver;
            redraw();
        }

        public boolean isMouseOver() {
            return mouseOver;
        }

        public void setPressed(boolean pressed) {
            if (pressed == this.pressed)
                return;
            this.pressed = pressed;
            redraw();
        }

        public boolean isPressed() {
            return pressed;
        }

        public void setBounds(Rectangle bounds) {
            if (bounds == this.bounds
                    || (bounds != null && bounds.equals(this.bounds)))
                return;
            this.bounds = bounds;
            redraw();
        }

        public Rectangle getBounds() {
            if (bounds == null)
                bounds = new Rectangle(0, 0, 0, 0);
            return bounds;
        }

        public boolean isEnabled() {
            return action != null && action.isEnabled();
        }

        private void redraw() {
            if (barExists())
                bar.redraw(this);
        }

        public boolean isSeparator() {
            return action == null;
        }

        Point getPrefSize() {
            if (!barExists())
                return new Point(0, 0);
            GC gc = new GC(Display.getCurrent());
            gc.setFont(bar.getFont());
            Point s = gc.textExtent(getText());
            gc.dispose();
            int h = getHMargin();
            int v = getVMargin();

            Image img = getImage();
            if (img != null) {
                Rectangle r = img.getBounds();
                s.x += r.width + SPACING;
                s.y = Math.max(s.y, r.height);
            }

            s.x += h + h + 1;
            s.y += v + v + 1;
            return s;
        }

        private int getHMargin() {
            if (isSeparator())
                return 0;
            return H_MARGIN;
        }

        private int getVMargin() {
            if (isSeparator())
                return 0;
            return V_MARGIN;
        }

        protected void paint(GC gc) {
            gc.setAntialias(SWT.ON);
            gc.setLineStyle(SWT.LINE_SOLID);
            gc.setLineWidth(1);

            if (pressed || mouseOver) {
                if (pressed && mouseOver) {
                    gc.setBackground(ColorUtils.getColor("#a0a0a0")); //$NON-NLS-1$
                } else if (pressed) {
                    gc.setBackground(ColorUtils.getColor("#707070")); //$NON-NLS-1$
                } else {
                    gc.setBackground(ColorUtils.getColor("#e0e0e0")); //$NON-NLS-1$
                }
                gc.fillRoundRectangle(bounds.x + M2, bounds.y + M2,
                        bounds.width - W2, bounds.height - W2, C2, C2);

                if (pressed && mouseOver) {
                    gc.setForeground(ColorUtils.getColor("#909090")); //$NON-NLS-1$
                } else if (pressed) {
                    gc.setForeground(ColorUtils.getColor("#606060")); //$NON-NLS-1$
                } else {
                    gc.setForeground(ColorUtils.getColor("#d0d0d0")); //$NON-NLS-1$
                }
                gc.drawRoundRectangle(bounds.x + M, bounds.y + M,
                        bounds.width - W, bounds.height - W, C, C);
            }

            int h = getHMargin();
            int v = getVMargin();
            int x = bounds.x + h;
            int y = bounds.y + v;
            int height = bounds.height - v - v;

            Image img = getImage();
            if (img != null) {
                Rectangle r = img.getBounds();
                gc.drawImage(img, x, y + (height - r.height) / 2);
                x += r.width + SPACING;
            }

            if (isSeparator() || !isEnabled()) {
                gc.setForeground(ColorUtils.getColor("#a0a0a0")); //$NON-NLS-1$
            } else if (pressed) {
                gc.setForeground(ColorUtils.getColor("#f0f0f0")); //$NON-NLS-1$
            } else {
                gc.setForeground(ColorUtils.getColor("#000000")); //$NON-NLS-1$
            }
            String text = getText();
            Point s = gc.textExtent(text);
            gc.drawText(text, x, y + (height - s.y) / 2, true);
        }

        protected String getText() {
            if (action != null) {
                String text = action.getText();
                if (text != null) {
                    text = MindMapUtils.trimSingleLine(text);
                    return text;
                }
                return ""; //$NON-NLS-1$
            }
            return ">"; //$NON-NLS-1$
        }

        protected Image getImage() {
            if (image == null && barExists()) {
                if (action != null) {
                    ImageDescriptor imgDesc = null;
                    Display display = bar.getDisplay();
                    if (!isEnabled()) {
                        imgDesc = action.getDisabledImageDescriptor();
                        if (imgDesc != null) {
                            image = imgDesc.createImage(display);
                        }
                        if (image == null) {
                            imgDesc = action.getImageDescriptor();
                            if (imgDesc != null) {
                                Image img = imgDesc.createImage(display);
                                image = new Image(display, img,
                                        SWT.IMAGE_DISABLE);
                                img.dispose();
                            }
                        }
                    } else {
                        imgDesc = action.getImageDescriptor();
                        if (imgDesc != null) {
                            image = imgDesc.createImage(display);
                        }
                    }
                }
            }
            return image;
        }

        protected void run() {
            if (action != null)
                action.run();
        }

        public void update(String id) {
            boolean textChange = id == null || IAction.TEXT.equals(id);
            boolean tooltipChange = id == null
                    || IAction.TOOL_TIP_TEXT.equals(id);
            boolean enabledChange = id == null || IAction.ENABLED.equals(id);
            boolean imageChange = id == null || IAction.IMAGE.equals(id);
            boolean needUpdateBar = textChange || imageChange;

            if (barExists()) {
                bar.setRedraw(false);
            }

            if (enabledChange) {
                redraw();
            }

            if (tooltipChange) {
                if (action != null && barExists())
                    bar.setToolTipText(action.getToolTipText());
            }

            if (imageChange) {
                if (image != null) {
                    image.dispose();
                    image = null;
                }
            }

            if (needUpdateBar) {
                if (barExists())
                    bar.updateLayout();
            }

            if (barExists()) {
                bar.setRedraw(true);
            }
        }

        public void propertyChange(PropertyChangeEvent event) {
            update(event.getProperty());
        }

        public IAction getAction() {
            return action;
        }

        protected boolean barExists() {
            return bar != null && !bar.isDisposed();
        }

        protected void releaseResources() {
            if (image != null) {
                image.dispose();
                image = null;
            }
        }

    }

    protected static class CrumbsBar extends Composite {

        private static final int SPACING = 1;

        private class CrumbsBarListener
                implements PaintListener, MouseListener, MouseMoveListener,
                MouseTrackListener, ControlListener, DisposeListener {

            private CrumbItem sourceItem = null;

            private CrumbItem targetItem = null;

            public void paintControl(PaintEvent e) {
                GC gc = e.gc;
                if (!Util.isMac()) {
                    gc.setBackground(e.display
                            .getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
                    gc.fillRectangle(getBounds());
                }

                Rectangle clipping = gc.getClipping();
                for (CrumbItem item : items) {
                    if (clipping.intersects(item.getBounds())) {
                        item.paint(gc);
                    }
                }
            }

            public void mouseDoubleClick(MouseEvent e) {
            }

            public void mouseDown(MouseEvent e) {
                CrumbItem item = findItem(e.x, e.y);
                if (item != null && item.isEnabled() && !item.isSeparator()) {
                    sourceItem = item;
                    item.setPressed(true);
                }
            }

            public void mouseUp(MouseEvent e) {
                CrumbItem item = findItem(e.x, e.y);
                CrumbItem source = sourceItem;
                if (sourceItem != null) {
                    sourceItem.setPressed(false);
                    sourceItem = null;
                }
                if (item != null && item == source) {
                    item.run();
                }
            }

            private void receiveTarget(int x, int y) {
                CrumbItem item = findItem(x, y);
                if (item != targetItem) {
                    if (targetItem != null && targetItem.isEnabled()
                            && !targetItem.isSeparator()) {
                        targetItem.setMouseOver(false);
                    }
                    targetItem = item;
                    if (item != null && item.isEnabled()
                            && !item.isSeparator()) {
                        if (sourceItem == null || item == sourceItem) {
                            item.setMouseOver(true);
                        }
                    }
                    String tooltip = null;
                    if (targetItem != null) {
                        IAction action = targetItem.getAction();
                        if (action != null) {
                            tooltip = action.getToolTipText();
                        }
                    }
                    setToolTipText(tooltip);
                }
            }

            public void mouseMove(MouseEvent e) {
                receiveTarget(e.x, e.y);
            }

            public void mouseEnter(MouseEvent e) {
                receiveTarget(e.x, e.y);
            }

            public void mouseExit(MouseEvent e) {
                receiveTarget(e.x, e.y);
            }

            public void mouseHover(MouseEvent e) {
                receiveTarget(e.x, e.y);
            }

            public void controlMoved(ControlEvent e) {
                updateLayout();
            }

            public void controlResized(ControlEvent e) {
                updateLayout();
            }

            public void widgetDisposed(DisposeEvent e) {
                releaseItems();
            }

        }

        private List<CrumbItem> items = new ArrayList<CrumbItem>();

        public CrumbsBar(Composite parent, int style) {
            super(parent, style);
            CrumbsBarListener eventHandler = new CrumbsBarListener();
            addPaintListener(eventHandler);
            addMouseListener(eventHandler);
            addMouseMoveListener(eventHandler);
            addMouseTrackListener(eventHandler);
            addControlListener(eventHandler);
            setFont(FontUtils.getRelativeHeight(JFaceResources.DEFAULT_FONT,
                    -1));
        }

        private void releaseItems() {
            for (CrumbItem item : items) {
                item.releaseResources();
            }
        }

        protected void redraw(CrumbItem item) {
            checkWidget();
            Rectangle r = item.getBounds();
            redraw(r.x, r.y, r.width, r.height, false);
        }

        public void addItem(CrumbItem item) {
            checkWidget();
            addItem(item, -1);
        }

        public void addItem(CrumbItem item, int index) {
            checkWidget();
            items.remove(item);
            if (index < 0)
                items.add(item);
            else
                items.add(index, item);
            itemAdded(item);
        }

        public void removeItem(CrumbItem item) {
            checkWidget();
            items.remove(item);
            itemRemoved(item);
        }

        private void itemAdded(CrumbItem item) {
            item.setParent(this);
            layout();
        }

        private void itemRemoved(CrumbItem item) {
            item.setParent(null);
            item.releaseResources();
            layout();
        }

        public void removeAllItems() {
            checkWidget();
            setRedraw(false);
            CrumbItem[] oldItems = getItems();
            items.clear();
            for (CrumbItem item : oldItems) {
                itemRemoved(item);
            }
            layout();
            setRedraw(true);
        }

        public CrumbItem[] getItems() {
            checkWidget();
            return items.toArray(new CrumbItem[items.size()]);
        }

        public CrumbItem findItem(Point p) {
            checkWidget();
            return findItem(p.x, p.y);
        }

        public CrumbItem findItem(int x, int y) {
            checkWidget();
            for (CrumbItem item : items) {
                Rectangle r = item.getBounds();
                if (r.contains(x, y))
                    return item;
            }
            return null;
        }

        public void setLayout(Layout layout) {
        }

        public void layout(boolean changed) {
            updateLayout();
        }

        public void layout(boolean changed, boolean all) {
            updateLayout();
        }

        protected void updateLayout() {
            checkWidget();
            Point p = new Point(0, 0);
            int h = getSize().y;
            for (CrumbItem item : items) {
                Point s = item.getPrefSize();
                Rectangle r = new Rectangle(p.x, p.y + (h - s.y) / 2, s.x, s.y);
                item.setBounds(r);
                p.x += s.x + SPACING;
            }
            redraw();
        }

        public Point computeSize(int wHint, int hHint, boolean changed) {
            checkWidget();
            if (wHint >= 0 && hHint >= 0)
                return new Point(wHint, hHint);

            Point size = new Point(0, 0);
            if (wHint >= 0) {
                size.x = wHint;
            }
            for (CrumbItem item : items) {
                Point s = item.getPrefSize();
                if (wHint < 0) {
                    if (size.x > 0)
                        size.x += SPACING;
                    size.x += s.x;
                }
                size.y = Math.max(size.y, s.y);
            }
            return size;
        }
    }

    class Crumbs extends PanelContribution implements IDrillDownTraceListener {

        private class QuickDrillUpAction extends PageAction {

            private ITopic newCentralTopic;

            public QuickDrillUpAction(IGraphicalEditorPage page, ITopic topic) {
                super(page);
                this.newCentralTopic = topic;
                String title = topic.getTitleText();
                setText(title);
                setToolTipText(NLS.bind(
                        MindMapMessages.BreadCrumb_ViewAsCentral_text, title));
                setImageDescriptor(MindMapUtils.getImageDescriptor(topic));
            }

            public void run() {
                super.run();
                IGraphicalViewer viewer = getViewer();
                if (viewer != null) {
                    ISheet sheet = (ISheet) viewer.getAdapter(ISheet.class);
                    if (sheet != null) {
                        IMindMap newInput = new MindMap(sheet, newCentralTopic);
                        viewer.setInput(newInput);
                        if (viewer.getEditDomain() != null) {
                            viewer.getEditDomain().handleRequest(
                                    MindMapUI.REQ_SELECT_CENTRAL, viewer);
                        }
                    }
                }
            }

        }

        private CrumbsBar bar = null;

        private IDrillDownTraceService service = null;

        public void createControl(Composite parent) {
            if (!barExists()) {
                bar = new CrumbsBar(parent, SWT.NONE);
            }
        }

        public Control getControl() {
            return bar;
        }

        public void update() {
            boolean hasNewItems = service != null && service.canDrillUp();
            if (barExists()) {
                bar.removeAllItems();
                bar.setRedraw(false);
                if (hasNewItems) {
                    List<ITopic> topics = service.getCentralTopics();
                    if (!topics.isEmpty()) {
                        for (int i = 0; i < topics.size(); i++) {
                            ITopic t = topics.get(i);
                            QuickDrillUpAction action = new QuickDrillUpAction(
                                    getPage(), t);
                            if (i == topics.size() - 1) {
                                action.setEnabled(false);
                                action.setImageDescriptor(null);
                                action.setToolTipText(NLS.bind(
                                        MindMapMessages.BreadCrumb_CurrentCentral_text,
                                        action.newCentralTopic.getTitleText()));
                            }

                            bar.addItem(new CrumbItem(action));
                            if (i < topics.size() - 1) {
                                bar.addItem(new CrumbItem());
                            }
                        }
                    }
                }
                bar.setRedraw(true);
                setVisible(hasNewItems);
            }
        }

        private boolean barExists() {
            return bar != null && !bar.isDisposed();
        }

        public void traceChanged(IDrillDownTraceService traceService) {
            update();
        }

        public void setTraceService(IDrillDownTraceService service) {
            if (service == this.service)
                return;

            if (this.service != null) {
                this.service.removeTraceListener(this);
            }
            this.service = service;
            if (service != null) {
                service.addTraceListener(this);
            }
            update();
        }

    }

    private Crumbs crumbs = null;

    protected void init(IPanel panel) {
        super.init(panel);
        crumbs = new Crumbs();
        crumbs.setVisible(false);
        panel.addContribution(IPanel.TOP, crumbs);
    }

    public void setViewer(IGraphicalViewer viewer) {
        super.setViewer(viewer);

        IDrillDownTraceService traceService = (IDrillDownTraceService) viewer
                .getService(IDrillDownTraceService.class);
        crumbs.setTraceService(traceService);
    }

    public void dispose() {
        crumbs.setTraceService(null);
        super.dispose();
    }
}
