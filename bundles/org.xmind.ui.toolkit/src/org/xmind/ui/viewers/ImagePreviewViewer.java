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
package org.xmind.ui.viewers;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ToolBar;
import org.xmind.ui.internal.ToolkitImages;

public class ImagePreviewViewer {

    protected static final int PREF_WIDTH = 300;

    protected static final int PREF_HEIGHT = 180;

    protected static final double MAX_RATIO = 5.0d;

    protected static final double MIN_RATIO = 0.1d;

    protected static final double MID_RATIO = 1.0d;

    protected static final int BORDER_WIDTH = 1;

    protected static final int STEPPING_DISTANCE = 5;

    protected static final int TEXT_MARGIN = 3;

    private final class PreviewSliderContentProvider implements
            ISliderContentProvider {

        public double getRatio(Object input, Object value) {
            double v = ((Double) value).doubleValue();
            if (v < MID_RATIO) {
                return (v - getMinRatio()) / (MID_RATIO - getMinRatio()) / 2;
            }
            return (v - MID_RATIO) / (getMaxRatio() - MID_RATIO) / 2 + 0.5d;
        }

        public Object getValue(Object input, double ratio) {
            double v;
            if (ratio > 0.476 && ratio < 0.515) {
                v = MID_RATIO;
            } else if (ratio < 0.5) {
                v = ratio * 2 * (MID_RATIO - getMinRatio()) + getMinRatio();
            } else {
                v = (ratio - 0.5) * 2 * (getMaxRatio() - MID_RATIO) + MID_RATIO;
            }
            return Double.valueOf(v);
        }

//        public Object[] getValues(Object input) {
//            return new Double[] { Double.valueOf(getMinRatio()),
//                    Double.valueOf(MID_RATIO), Double.valueOf(getMaxRatio()) };
//        }

        public void dispose() {
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }

    }

    private static final class PreviewSliderLabelProvider extends LabelProvider {

        public String getText(Object element) {
            if (element instanceof Double) {
                double v = ((Double) element).doubleValue();
                return NLS.bind("{0}%", (int) (v * 100)); //$NON-NLS-1$
            }
            return super.getText(element);
        }

    }

    private class ZoomInAction extends Action {
        public ZoomInAction() {
            super(null, ToolkitImages.get(ToolkitImages.ZOOM_IN, true));
            setDisabledImageDescriptor(ToolkitImages.get(ToolkitImages.ZOOM_IN,
                    false));
            setToolTipText(Messages.ZoomIn_toolTip);
        }

        public void run() {
            zoomIn();
            setFocus();
        }
    }

    private class ZoomOutAction extends Action {
        public ZoomOutAction() {
            super(null, ToolkitImages.get(ToolkitImages.ZOOM_OUT, true));
            setDisabledImageDescriptor(ToolkitImages.get(
                    ToolkitImages.ZOOM_OUT, false));
            setToolTipText(Messages.ZoomOut_toolTip);
        }

        public void run() {
            zoomOut();
            setFocus();
        }
    }

    private boolean fill;

    private int prefWidth = PREF_WIDTH;

    private int prefHeight = PREF_HEIGHT;

    private double maxRatio = MAX_RATIO;

    private double minRatio = MIN_RATIO;

    private double x = 0;

    private double y = 0;

    private double ratio = 1.0d;

    private Image image;

    private Composite composite;

    private Canvas canvas;

    private Point startLoc;

    private double startX;

    private double startY;

    private SliderViewer slider;

    private IAction zoomInAction;

    private IAction zoomOutAction;

    private boolean updatingRatioSelection = false;

    private boolean disabled = false;

    private String title = null;

    private int titlePlacement = 0;

    private ISelectionChangedListener sliderListener = new ISelectionChangedListener() {

        public void selectionChanged(SelectionChangedEvent event) {
            if (updatingRatioSelection)
                return;

            ISelection selection = event.getSelection();
            Object element = ((IStructuredSelection) selection)
                    .getFirstElement();
            if (element instanceof Double) {
                changeRatio(((Double) element).doubleValue());
                setFocus();
            }
        }

    };

    public ImagePreviewViewer() {
        this(false);
    }

    public ImagePreviewViewer(boolean fill) {
        this.fill = fill;
    }

    public void createControl(Composite parent) {
        composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 0;
        composite.setLayout(layout);

        Composite composite2 = new Composite(composite, SWT.NONE);
        GridLayout layout2 = new GridLayout();
        layout2.marginHeight = 0;
        layout2.marginWidth = 0;
        layout2.horizontalSpacing = 0;
        layout2.verticalSpacing = 10;
        composite2.setLayout(layout2);
        composite2.setLayoutData(new GridData(fill ? GridData.FILL
                : GridData.CENTER, fill ? GridData.FILL : GridData.CENTER,
                true, true));

        createCanvas(composite2);
        createRatioControls(composite2);
    }

    public void setBackgroundColor(Color color) {
        if (slider != null && !slider.getControl().isDisposed()) {
            slider.getControl().setBackground(color);
        }
    }

    protected void createCanvas(Composite parent) {
        canvas = new Canvas(parent, SWT.DOUBLE_BUFFERED);
        GridData layoutData = new GridData();
        layoutData.horizontalAlignment = GridData.FILL;
        layoutData.verticalAlignment = GridData.FILL;
        layoutData.grabExcessHorizontalSpace = true;
        layoutData.grabExcessVerticalSpace = true;
        layoutData.widthHint = getPrefWidth() + BORDER_WIDTH + BORDER_WIDTH;
        layoutData.heightHint = getPrefHeight() + BORDER_WIDTH + BORDER_WIDTH;
        layoutData.minimumWidth = layoutData.widthHint;
        layoutData.minimumHeight = layoutData.heightHint;
        canvas.setLayoutData(layoutData);
        hookCanvas(canvas, new Listener() {
            public void handleEvent(Event event) {
                handleCanvasEvent(event);
            }
        });
        canvas.setCursor(parent.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
        updateCanvas();
    }

    protected void createRatioControls(Composite parent) {
        Composite bar = new Composite(parent, SWT.NONE);
        GridData layoutData = new GridData(SWT.CENTER, SWT.CENTER, false, false);
        layoutData.widthHint = getPrefWidth();
        bar.setLayoutData(layoutData);
        GridLayout layout = new GridLayout(3, false);
        layout.marginWidth = 30;
        layout.horizontalSpacing = 1;
        layout.marginHeight = 0;
        bar.setLayout(layout);

        ToolBar zoomOutBar = new ToolBar(bar, SWT.FLAT);
        zoomOutBar.setLayoutData(new GridData(SWT.RIGHT, GridData.CENTER,
                false, false));
        if (zoomOutAction == null) {
            zoomOutAction = new ZoomOutAction();
        }
        new ActionContributionItem(zoomOutAction).fill(zoomOutBar, 0);

        slider = new SliderViewer(bar, SWT.HORIZONTAL);
        GridData sliderLayoutData = new GridData(GridData.CENTER, GridData.CENTER,
                false, false);
        sliderLayoutData.widthHint = 200;
        sliderLayoutData.minimumWidth = 80;
        slider.getControl().setLayoutData(sliderLayoutData);
        slider.setContentProvider(new PreviewSliderContentProvider());
        slider.setLabelProvider(new PreviewSliderLabelProvider());
        slider.setInput(this);
        updateRatioSelection(ratio);
        slider.addSelectionChangedListener(sliderListener);

        ToolBar zoomInBar = new ToolBar(bar, SWT.FLAT);
        zoomInBar.setLayoutData(new GridData(SWT.LEFT, GridData.CENTER, false,
                false));
        if (zoomInAction == null) {
            zoomInAction = new ZoomInAction();
        }
        new ActionContributionItem(zoomInAction).fill(zoomInBar, 0);

        updateRatioControls();
    }

    private void updateCanvas() {
        if (canvas != null && !canvas.isDisposed()) {
            canvas.setEnabled(getImage() != null && !isDisabled());
        }
    }

    protected void updateRatioControls() {
        boolean ratioControlEnabled = getImage() != null && !isDisabled();
        if (zoomOutAction != null)
            zoomOutAction.setEnabled(ratioControlEnabled);
        if (zoomInAction != null)
            zoomInAction.setEnabled(ratioControlEnabled);
        if (slider != null && !slider.getControl().isDisposed()) {
            slider.getControl().setEnabled(ratioControlEnabled);
        }
    }

    protected void hookCanvas(Canvas canvas, Listener listener) {
        canvas.addListener(SWT.Paint, listener);
        canvas.addListener(SWT.MouseDown, listener);
        canvas.addListener(SWT.MouseMove, listener);
        canvas.addListener(SWT.MouseUp, listener);
        canvas.addListener(SWT.KeyDown, listener);
    }

    protected void handleCanvasEvent(Event event) {
        switch (event.type) {
        case SWT.Paint:
            paintCanvas(event);
            break;
        case SWT.MouseDown:
            if (event.button == 1) {
                startDragging(event);
            }
            break;
        case SWT.MouseUp:
            if (event.button == 1) {
                endDragging(event);
            }
            break;
        case SWT.MouseMove:
            if ((event.stateMask & SWT.BUTTON_MASK) == SWT.BUTTON1) {
                drag(event);
            }
            break;
        case SWT.KeyDown:
            handleKeyDown(event);
            break;
        }
    }

    protected void handleKeyDown(Event event) {
        int keyCode = event.keyCode;
        int stateMask = event.stateMask;
        if (SWTUtils.matchKey(stateMask, keyCode, 0, SWT.ARROW_UP)) {
            moveUp();
        } else if (SWTUtils.matchKey(stateMask, keyCode, 0, SWT.ARROW_DOWN)) {
            moveDown();
        } else if (SWTUtils.matchKey(stateMask, keyCode, 0, SWT.ARROW_LEFT)) {
            moveLeft();
        } else if (SWTUtils.matchKey(stateMask, keyCode, 0, SWT.ARROW_RIGHT)) {
            moveRight();
        } else if (SWTUtils.matchKey(stateMask, keyCode, 0, SWT.TAB)) {
            ((Control) event.widget).traverse(SWT.TRAVERSE_TAB_NEXT);
        } else if (SWTUtils.matchKey(stateMask, keyCode, SWT.SHIFT, SWT.TAB)) {
            ((Control) event.widget).traverse(SWT.TRAVERSE_TAB_PREVIOUS);
        }
    }

    public void moveUp() {
        move(x, y, 0, -STEPPING_DISTANCE);
    }

    public void moveDown() {
        move(x, y, 0, STEPPING_DISTANCE);
    }

    public void moveLeft() {
        move(x, y, -STEPPING_DISTANCE, 0);
    }

    public void moveRight() {
        move(x, y, STEPPING_DISTANCE, 0);
    }

    private void paintCanvas(Event event) {
        GC gc = event.gc;
        Rectangle area = canvas.getClientArea();
        drawImage(gc, area);
        drawTitle(gc, area);
        gc.setClipping(area);
        gc.setForeground(event.display.getSystemColor(SWT.COLOR_GRAY));
        gc.drawRectangle(area.x, area.y, area.width - 1, area.height - 1);
    }

    private void drawTitle(GC gc, Rectangle area) {
        if (title == null)
            return;

        gc.setFont(composite.getFont());
        gc.setForeground(composite.getForeground());
        Point size = gc.stringExtent(title);
        int x, y;
        if ((titlePlacement & SWT.LEFT) != 0) {
            x = area.x + TEXT_MARGIN;
        } else if ((titlePlacement & SWT.RIGHT) != 0) {
            x = area.x + area.width - size.x - TEXT_MARGIN;
        } else {
            x = area.x + (area.width - size.x) / 2;
        }
        if ((titlePlacement & SWT.TOP) != 0) {
            y = area.y + TEXT_MARGIN;
        } else if ((titlePlacement & SWT.BOTTOM) != 0) {
            y = area.y + area.height - size.y - TEXT_MARGIN;
        } else {
            y = area.y + (area.height - size.y) / 2;
        }
        gc.drawString(title, x, y, true);
    }

    protected void drawImage(GC gc, Rectangle area) {
        if (image != null && !image.isDisposed()) {
            drawImage(gc, area, image, image.getBounds());
        }
    }

    private void drawImage(GC gc, Rectangle area, Image image, Rectangle imgSize) {
        double srcWidth = Math.min(imgSize.width, area.width / ratio);
        double srcHeight = Math.min(imgSize.height, area.height / ratio);
        double srcX = Math.max(0, Math.min(imgSize.width - srcWidth, x));
        double srcY = Math.max(0, Math.min(imgSize.height - srcHeight, y));
        double destWidth = srcWidth * ratio;
        double destHeight = srcHeight * ratio;
        double destX = area.x + BORDER_WIDTH
                + (area.width - BORDER_WIDTH - BORDER_WIDTH - destWidth) / 2;
        double destY = area.y + BORDER_WIDTH
                + (area.height - BORDER_WIDTH - BORDER_WIDTH - destHeight) / 2;
        gc.setAntialias(SWT.ON);
        gc.drawImage(image, (int) srcX, (int) srcY, (int) srcWidth,
                (int) srcHeight, (int) destX, (int) destY, (int) destWidth,
                (int) destHeight);
    }

    private void startDragging(Event event) {
        if (image == null || image.isDisposed())
            return;

        startLoc = new Point(event.x, event.y);
        startX = x;
        startY = y;
    }

    private void endDragging(Event event) {
        startLoc = null;
    }

    private void drag(Event event) {
        if (startLoc == null || image == null || image.isDisposed())
            return;

        move(startX, startY, event.x - startLoc.x, event.y - startLoc.y);
    }

    public void move(double startX, double startY, int dx, int dy) {
        if (image == null || image.isDisposed())
            return;

        Rectangle imgSize = image.getBounds();
        int refWidth, refHeight;
        if (canvas != null && !canvas.isDisposed()) {
            Rectangle area = canvas.getClientArea();
            refWidth = area.width;
            refHeight = area.height;
        } else {
            refWidth = getPrefWidth();
            refHeight = getPrefHeight();
        }

        double width = Math.min(imgSize.width, refWidth / ratio);
        double height = Math.min(imgSize.height, refHeight / ratio);
        double newX = Math.max(0, Math.min(imgSize.width - width, startX
                - (dx / ratio)));
        double newY = Math.max(0, Math.min(imgSize.height - height, startY
                - (dy / ratio)));
        if (newX != x || newY != y) {
            setX(newX);
            setY(newY);
            if (canvas != null && !canvas.isDisposed()) {
                canvas.redraw();
            }
        }
    }

    public Control getControl() {
        return composite;
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setRatio(double ratio) {
        this.ratio = ratio;
    }

    public double getRatio() {
        return ratio;
    }

    public int getPrefHeight() {
        return prefHeight;
    }

    public void setPrefHeight(int prefHeight) {
        if (prefHeight == this.prefHeight)
            return;
        int oldPrefHeight = this.prefHeight;
        this.prefHeight = prefHeight;
        updatePrefSize(getPrefWidth(), oldPrefHeight);
    }

    public int getPrefWidth() {
        return prefWidth;
    }

    public void setPrefWidth(int prefWidth) {
        if (prefWidth == this.prefWidth)
            return;
        int oldPrefWidth = this.prefWidth;
        this.prefWidth = prefWidth;
        updatePrefSize(oldPrefWidth, getPrefHeight());
    }

    private void updatePrefSize(int oldPrefWidth, int oldPrefHeight) {
//        if (image == null || image.isDisposed())
//            return;
//
//        Rectangle imgSize = image.getBounds();
//        double width = Math.min(imgSize.width, getPrefHeight() / getRatio());
//        double height = Math.min(imgSize.height, getPrefHeight() / getRatio());
//        double centerX = getX() + width * 0.5;
//        double centerY = getY() + height * 0.5;
//        setX(Math.max(0, Math.min(imgSize.width - width, centerX - width / 2)));
//        setY(Math.max(0, Math
//                .min(imgSize.height - height, centerY - height / 2)));
//        if (canvas != null && !canvas.isDisposed()) {
//            canvas.redraw();
//        }
        changeRatio(getRatio(), oldPrefWidth, oldPrefHeight);
    }

    public double getMaxRatio() {
        return maxRatio;
    }

    public void setMaxRatio(double maxRatio) {
        this.maxRatio = maxRatio;
    }

    public double getMinRatio() {
        return minRatio;
    }

    public void setMinRatio(double minRatio) {
        this.minRatio = minRatio;
    }

    public void zoomIn() {
        if (ratio < MID_RATIO) {
            changeRatio(Math.min(MID_RATIO, ratio + 0.1));
        } else {
            changeRatio(ratio + 0.5);
        }
    }

    public void zoomOut() {
        if (ratio <= MID_RATIO) {
            changeRatio(ratio - 0.1);
        } else {
            changeRatio(Math.max(MID_RATIO, ratio - 0.3));
        }
    }

    public void setFocus() {
        if (canvas != null && !canvas.isDisposed()) {
            canvas.setFocus();
        }
    }

    public void changeRatio(double ratio) {
        changeRatio(ratio, getPrefWidth(), getPrefHeight());
    }

    private void changeRatio(double ratio, int oldPrefWidth, int oldPrefHeight) {
        ratio = Math.max(getMinRatio(), Math.min(getMaxRatio(), ratio));
        double oldRatio = this.ratio;
        setRatio(ratio);
        double newRatio = this.ratio;
        if (image != null) {
            Rectangle imgSize = image.getBounds();
            double oldWidth = Math.min(imgSize.width, oldPrefWidth / oldRatio);
            double oldHeight = Math.min(imgSize.height, oldPrefHeight
                    / oldRatio);
            double oldCenterX = x + oldWidth / 2;
            double oldCenterY = y + oldHeight / 2;
            double newWidth = Math
                    .min(imgSize.width, getPrefWidth() / newRatio);
            double newHeight = Math.min(imgSize.height, getPrefHeight()
                    / newRatio);
            setX(Math.min(imgSize.width - newWidth, Math.max(0, oldCenterX
                    - newWidth / 2)));
            setY(Math.min(imgSize.height - newHeight, Math.max(0, oldCenterY
                    - newHeight / 2)));
        }
        if (canvas != null && !canvas.isDisposed()) {
            canvas.redraw();
        }
        updateRatioSelection(newRatio);
    }

    protected void updateRatioSelection(double ratio) {
        if (slider != null && !slider.getControl().isDisposed()) {
            updatingRatioSelection = true;
            slider.setSelection(new StructuredSelection(Double.valueOf(ratio)));
            updatingRatioSelection = false;
        }
    }

    public void setImage(Image image) {
        if (image != null && !image.isDisposed()) {
            Rectangle imgSize = image.getBounds();
            setImage(image, imgSize.x + imgSize.width / 2, imgSize.y
                    + imgSize.height / 2);
        } else {
            setImage(null, 0, 0);
        }
    }

    public void setImage(Image image, double centerX, double centerY) {
        this.image = image;
        if (image != null) {
            Rectangle imgSize = image.getBounds();
            double horizontalRatio = ((double) getPrefWidth()) / imgSize.width;
            double verticalRatio = ((double) getPrefHeight()) / imgSize.height;
            setRatio(Math.max(0.6, Math.min(horizontalRatio, verticalRatio)));
            double width = Math.min(imgSize.width, getPrefWidth() / getRatio());
            double height = Math.min(imgSize.height, getPrefHeight()
                    / getRatio());
            setX(Math.max(0, Math.min(imgSize.width - width, centerX - width
                    / 2)));
            setY(Math.max(0, Math.min(imgSize.height - height, centerY - height
                    / 2)));
        }
        if (canvas != null && !canvas.isDisposed()) {
            canvas.redraw();
        }
        updateCanvas();
        updateRatioControls();
        updateRatioSelection(ratio);
    }

    public Image getImage() {
        return image;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
        updateCanvas();
        updateRatioControls();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if (title == this.title || (title != null && title.equals(this.title)))
            return;

        this.title = title;
        if (canvas != null && !canvas.isDisposed()) {
            canvas.redraw();
        }
    }

    public int getTitlePlacement() {
        return titlePlacement;
    }

    public void setTitlePlacement(int titlePlacement) {
        if (titlePlacement == this.titlePlacement)
            return;

        this.titlePlacement = titlePlacement;
        if (canvas != null && !canvas.isDisposed()) {
            canvas.redraw();
        }
    }

}