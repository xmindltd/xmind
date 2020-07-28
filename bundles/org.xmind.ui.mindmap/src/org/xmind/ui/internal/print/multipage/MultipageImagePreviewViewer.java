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
package org.xmind.ui.internal.print.multipage;

import org.eclipse.draw2d.AbstractLayout;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Layer;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.SWTGraphics;
import org.eclipse.draw2d.Viewport;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;
import org.xmind.gef.draw2d.ITextFigure;
import org.xmind.gef.draw2d.RotatableWrapLabel;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.print.PrintConstants;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.resources.ColorUtils;
import org.xmind.ui.resources.FontUtils;

public class MultipageImagePreviewViewer {

    private static final int PREF_WIDTH = 300;

    private static final int PREF_HEIGHT = 180;

    private static final int BORDER_WIDTH = 1;

    private static final int TEXT_MARGIN = 3;

    private static final double Show_Font_Multiple = 4.0;

    private static final String ENABLED = "enabled"; //$NON-NLS-1$

    private class PreviewTextLayout extends AbstractLayout {

        protected Dimension calculatePreferredSize(IFigure container, int wHint,
                int hHint) {
            return container.getSize();
        }

        public void layout(IFigure container) {
            org.eclipse.draw2d.geometry.Rectangle area = container
                    .getClientArea();
            org.eclipse.draw2d.geometry.Rectangle borderBounds = borderFigure
                    .getBounds();
            int textMargin = Util.isMac() ? 5 : 0;
            int bottomDownValue = 4;
            if (pageNumberLayer != null) {
                Dimension size = pageNumberLayer.getPreferredSize(-1, -1);
                int y = area.y + area.height - size.height
                        - Math.max((area.y + area.height
                                - (borderBounds.y + borderBounds.height)
                                - size.height) / 2, textMargin)
                        + bottomDownValue;

                int x = area.x + (area.width - size.width) / 2;
                //todo set x position for pageNumberFigure when footer's horizontal position changed

                pageNumberLayer
                        .setBounds(new org.eclipse.draw2d.geometry.Rectangle(x,
                                y, area.width, size.height));
            }

            if (headerFigure != null) {
                Dimension size = headerFigure.getPreferredSize(area.width, -1);
                int y = area.y
                        + Math.max((borderBounds.y - area.y - size.height) / 2,
                                textMargin);

                headerFigure.setBounds(
                        new org.eclipse.draw2d.geometry.Rectangle(area.x, y,
                                area.width, size.height));
            }
            if (footerFigure != null) {
                Dimension size = footerFigure.getPreferredSize(area.width, -1);
                int y = area.y + area.height - size.height
                        - Math.max((area.y + area.height
                                - (borderBounds.y + borderBounds.height)
                                - size.height) / 2, textMargin)
                        + bottomDownValue;
                footerFigure.setBounds(
                        new org.eclipse.draw2d.geometry.Rectangle(area.x, y,
                                area.width, size.height));
            }
        }
    }

    private int prefWidth = PREF_WIDTH;

    private int prefHeight = PREF_HEIGHT;

    private double x = 0;

    private double y = 0;

    private double ratio = 1.0d;

    private Composite composite;

    private Composite backgroundImageComposite;

    private Image backgroundImage;

    private FigureCanvas canvas;

    private String title = null;

    private int titlePlacement = 0;

    private Label pageNumberLabel;

    private Label totalPagesLabel;

    private Label firstButton;

    private Label previousButton;

    private Label nextButton;

    private Label lastButton;

    private Image image;

    private Image wholeImage;

    public Image[] singleImages;

    private int imageIndex;

    private Layer feedbackLayer;

    private IFigure borderFigure;

    private ITextFigure headerFigure;

    private ITextFigure footerFigure;

    private IFigure textLayer;

    private IFigure pageNumberLayer;

    private ITextFigure pageNumberFigure;

    private Rectangle imageBorderBounds;

    private Font headerFont;

    private Font footerFont;

    private ResourceManager resources;

    private Listener eventHandler = new Listener() {
        public void handleEvent(Event event) {
            handleWidgetEvent(event);
        }
    };

    public MultipageImagePreviewViewer(boolean fill) {
    }

    public void createControl(Composite parent) {
        composite = new Composite(parent, SWT.NONE);
        resources = new LocalResourceManager(JFaceResources.getResources(),
                composite);

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
//        composite2.setLayoutData(
//                new GridData(fill ? GridData.FILL : GridData.CENTER,
//                        fill ? GridData.FILL : GridData.CENTER, true, true));
        composite2.setLayoutData(
                new GridData(GridData.CENTER, GridData.CENTER, false, false));

        createCanvas(composite2);
        createBarControls(composite2);
    }

    private void createCanvas(Composite parent) {
        //create background image composite
        backgroundImageComposite = new Composite(parent, SWT.NONE);
        backgroundImageComposite.setBackground(parent.getBackground());
        backgroundImageComposite.setLayoutData(
                new GridData(SWT.CENTER, SWT.CENTER, false, false));

        GridLayout layout = new GridLayout(1, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.marginLeft = 2;
        layout.marginTop = 1;
        layout.marginRight = 6;
        layout.marginBottom = 7;
        backgroundImageComposite.setLayout(layout);

        //create canvas
        LightweightSystem lws = new LightweightSystem();
        canvas = new FigureCanvas(backgroundImageComposite, SWT.DOUBLE_BUFFERED,
                lws);
        canvas.setScrollBarVisibility(FigureCanvas.NEVER);

        GridData layoutData = new GridData();
        layoutData.horizontalAlignment = GridData.FILL;
        layoutData.verticalAlignment = GridData.FILL;
        layoutData.grabExcessHorizontalSpace = true;
        layoutData.grabExcessVerticalSpace = true;
        layoutData.widthHint = getPrefWidth() + BORDER_WIDTH + BORDER_WIDTH;
        layoutData.heightHint = getPrefHeight() + BORDER_WIDTH + BORDER_WIDTH;
        canvas.setLayoutData(layoutData);
        hookCanvas(canvas, new Listener() {
            public void handleEvent(Event event) {
                handleCanvasEvent(event);
            }
        });
        updateCanvas();

        createFeedbackFigure(canvas);
    }

    private void createFeedbackFigure(FigureCanvas canvas) {
        canvas.setViewport(new Viewport(true));

        feedbackLayer = new Layer();
        canvas.getViewport().setContents(feedbackLayer);

        borderFigure = new Layer();
        borderFigure.setBorder(new LineBorder(
                new LocalResourceManager(JFaceResources.getResources(), canvas)
                        .createColor(ColorUtils.toRGB("#959595")), //$NON-NLS-1$
                1));
        feedbackLayer.add(borderFigure);

        textLayer = new Layer();
        textLayer.setLayoutManager(new PreviewTextLayout());
        feedbackLayer.add(textLayer);

        headerFigure = new RotatableWrapLabel(RotatableWrapLabel.NORMAL);
        headerFigure.setForegroundColor(
                canvas.getDisplay().getSystemColor(SWT.COLOR_BLACK));
        textLayer.add(headerFigure);

        footerFigure = new RotatableWrapLabel(RotatableWrapLabel.NORMAL);
        footerFigure.setForegroundColor(
                canvas.getDisplay().getSystemColor(SWT.COLOR_BLACK));
        textLayer.add(footerFigure);

        pageNumberLayer = new Layer();
        textLayer.add(pageNumberLayer);

        org.eclipse.draw2d.GridLayout gridLayout = new org.eclipse.draw2d.GridLayout();
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        pageNumberLayer.setLayoutManager(gridLayout);

        pageNumberFigure = new RotatableWrapLabel(RotatableWrapLabel.NORMAL);
        pageNumberFigure.setForegroundColor(
                canvas.getDisplay().getSystemColor(SWT.COLOR_BLACK));
        org.eclipse.draw2d.GridData gridData = new org.eclipse.draw2d.GridData(
                SWT.FILL, SWT.FILL, true, true);
        pageNumberLayer.add(pageNumberFigure, gridData);
    }

    private void createBarControls(Composite parent) {
        Composite bar = new Composite(parent, SWT.NONE);
        GridData layoutData = new GridData(SWT.CENTER, SWT.CENTER, false,
                false);
        bar.setLayoutData(layoutData);
        GridLayout layout = new GridLayout(5, false);
        layout.marginWidth = 30;
        layout.horizontalSpacing = 0;
        layout.marginHeight = 0;
        bar.setLayout(layout);

        firstButton = new Label(bar, SWT.NONE);
        firstButton.setImage(getIcon("nav_first.png", true)); //$NON-NLS-1$
        GridData firstButtonData = new GridData(SWT.CENTER, SWT.CENTER, false,
                false);
        firstButtonData.widthHint = firstButton.getImage().getBounds().width;
        firstButtonData.heightHint = firstButton.getImage().getBounds().height;
        firstButton.setLayoutData(firstButtonData);

        previousButton = new Label(bar, SWT.NONE);
        previousButton.setImage(getIcon("nav_previous.png", true)); //$NON-NLS-1$
        GridData previousButtonData = new GridData(SWT.CENTER, SWT.CENTER,
                false, false);
        previousButtonData.widthHint = previousButton.getImage()
                .getBounds().width;
        previousButtonData.heightHint = previousButton.getImage()
                .getBounds().height;
        previousButton.setLayoutData(previousButtonData);

        Composite labelComposite = new Composite(bar, SWT.NONE);
        labelComposite.setLayoutData(
                new GridData(SWT.CENTER, SWT.CENTER, false, false));
        GridLayout layout2 = new GridLayout(3, false);
        layout2.marginWidth = 20;
        layout2.marginHeight = 0;
        layout2.horizontalSpacing = 10;
        layout2.verticalSpacing = 0;
        labelComposite.setLayout(layout2);

        pageNumberLabel = new Label(labelComposite, SWT.NONE);
        pageNumberLabel.setLayoutData(
                new GridData(SWT.CENTER, SWT.CENTER, false, false));
        pageNumberLabel.setText("0"); //$NON-NLS-1$

        Label label1 = new Label(labelComposite, SWT.NONE);
        label1.setLayoutData(
                new GridData(SWT.CENTER, SWT.CENTER, false, false));
        label1.setText("of"); //$NON-NLS-1$

        totalPagesLabel = new Label(labelComposite, SWT.NONE);
        totalPagesLabel.setLayoutData(
                new GridData(SWT.CENTER, SWT.CENTER, false, false));
        totalPagesLabel.setText("0"); //$NON-NLS-1$

        nextButton = new Label(bar, SWT.NONE);
        nextButton.setImage(getIcon("nav_next.png", true)); //$NON-NLS-1$
        GridData nextButtonData = new GridData(SWT.CENTER, SWT.CENTER, false,
                false);
        nextButtonData.widthHint = nextButton.getImage().getBounds().width;
        nextButtonData.heightHint = nextButton.getImage().getBounds().height;
        nextButton.setLayoutData(nextButtonData);

        lastButton = new Label(bar, SWT.NONE);
        lastButton.setImage(getIcon("nav_last.png", true)); //$NON-NLS-1$
        GridData lastButtonData = new GridData(SWT.CENTER, SWT.CENTER, false,
                false);
        lastButtonData.widthHint = lastButton.getImage().getBounds().width;
        lastButtonData.heightHint = lastButton.getImage().getBounds().height;
        lastButton.setLayoutData(lastButtonData);

        hookWidget(firstButton, SWT.MouseDown);
        hookWidget(previousButton, SWT.MouseDown);
        hookWidget(nextButton, SWT.MouseDown);
        hookWidget(lastButton, SWT.MouseDown);

        updateImageButtons();
    }

    private void hookWidget(Widget widget, int eventType) {
        widget.addListener(eventType, eventHandler);
    }

    private void handleWidgetEvent(Event event) {
        if (!Boolean.TRUE.equals(event.widget.getData(ENABLED))) {
            return;
        }

        if (event.widget == firstButton) {
            setImageIndex(0);
        } else if (event.widget == previousButton) {
            setImageIndex(imageIndex == 0 ? 0 : imageIndex - 1);
        } else if (event.widget == nextButton) {
            setImageIndex(imageIndex == singleImages.length
                    ? singleImages.length : imageIndex + 1);
        } else if (event.widget == lastButton) {
            setImageIndex(singleImages.length);
        }

        updateImageButtons();
        initPreviewImageRatio();
    }

    private void updateImageButtons() {
        firstButton.setData(ENABLED, singleImages != null && imageIndex != 0);
        previousButton.setData(ENABLED,
                singleImages != null && imageIndex != 0);
        nextButton.setData(ENABLED,
                singleImages != null && imageIndex != singleImages.length);
        lastButton.setData(ENABLED,
                singleImages != null && imageIndex != singleImages.length);

        updateButtonImages();
    }

    public void disableImageButtons() {
        firstButton.setData(ENABLED, false);
        previousButton.setData(ENABLED, false);
        nextButton.setData(ENABLED, false);
        lastButton.setData(ENABLED, false);

        updateButtonImages();
    }

    private void updateButtonImages() {
        firstButton.setImage(getIcon("nav_first.png", //$NON-NLS-1$
                Boolean.TRUE.equals(firstButton.getData(ENABLED))));
        previousButton.setImage(getIcon("nav_previous.png", //$NON-NLS-1$
                Boolean.TRUE.equals(previousButton.getData(ENABLED))));
        nextButton.setImage(getIcon("nav_next.png", //$NON-NLS-1$
                Boolean.TRUE.equals(nextButton.getData(ENABLED))));
        lastButton.setImage(getIcon("nav_last.png", //$NON-NLS-1$
                Boolean.TRUE.equals(lastButton.getData(ENABLED))));
    }

    public void setPrefSize(Point prefSize) {
        if (prefSize == null || (prefSize.x == getPrefWidth()
                && prefSize.y == getPrefHeight())) {
            return;
        }

        int oldPrefWidth = this.prefWidth;
        int oldPrefHeight = this.prefHeight;
        this.prefWidth = prefSize.x;
        this.prefHeight = prefSize.y;

        GridData gridData = (GridData) canvas.getLayoutData();
        gridData.widthHint = getPrefWidth() + BORDER_WIDTH + BORDER_WIDTH;
        gridData.heightHint = getPrefHeight() + BORDER_WIDTH + BORDER_WIDTH;

        updatePrefSize(oldPrefWidth, oldPrefHeight);
    }

    public void updateBackgroundImageComposite(boolean showDetails,
            boolean landscape) {
        String backgroundImageName = showDetails
                ? (landscape ? "paper-details-landscape.png" //$NON-NLS-1$
                        : "paper-details-portrait.png") //$NON-NLS-1$
                : (landscape ? "paper-simple-landscape.png" //$NON-NLS-1$
                        : "paper-simple-portrait.png"); //$NON-NLS-1$
        backgroundImage = MindMapUI.getImages()
                .get("icons/pages/" + backgroundImageName).createImage(); //$NON-NLS-1$
    }

    public void updateBackgroundImage() {
        if (imageIndex != 0) {
            if (backgroundImageComposite
                    .getBackgroundImage() == backgroundImage) {
                return;
            }
            backgroundImageComposite.setBackgroundImage(backgroundImage);

            GridData layoutData = (GridData) backgroundImageComposite
                    .getLayoutData();
            layoutData.widthHint = backgroundImageComposite.getBackgroundImage()
                    .getBounds().width;
            layoutData.heightHint = backgroundImageComposite
                    .getBackgroundImage().getBounds().height;
        } else {
            backgroundImageComposite.setBackgroundImage(null);

            GridData layoutData = (GridData) backgroundImageComposite
                    .getLayoutData();
            layoutData.widthHint = SWT.DEFAULT;
            layoutData.heightHint = SWT.DEFAULT;
        }

        backgroundImageComposite.getShell().pack();
        backgroundImageComposite.getShell().layout(true, true);
    }

    private void updateCanvas() {
        if (canvas != null && !canvas.isDisposed()) {
            canvas.setEnabled(getImage() != null);
        }
    }

    private void hookCanvas(Canvas canvas, Listener listener) {
        canvas.addListener(SWT.Paint, listener);
        canvas.addListener(SWT.Dispose, listener);
    }

    private void handleCanvasEvent(Event event) {
        switch (event.type) {
        case SWT.Paint:
            paintCanvas(event);
            break;
        case SWT.Dispose:
            handleWidgetDisposed();
            break;
        }
    }

    private void handleWidgetDisposed() {
        if (wholeImage != null && !wholeImage.isDisposed()) {
            wholeImage.dispose();
        }
        if (singleImages != null) {
            for (Image image : singleImages) {
                if (image != null && !image.isDisposed()) {
                    image.dispose();
                }
            }
        }
        if (image != null && !image.isDisposed()) {
            image.dispose();
        }
    }

    private void paintCanvas(Event event) {
        GC gc = event.gc;
        Rectangle area = canvas.getClientArea();
        gc.setClipping(area);
        drawImage(gc, area);
        drawTitle(gc, area);
        gc.setClipping(area);
        gc.setForeground(event.display.getSystemColor(SWT.COLOR_GRAY));
//        gc.drawRectangle(area.x, area.y, area.width - 1, area.height - 1);
        canvas.getViewport().paint(new SWTGraphics(gc));
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

    private void drawImage(GC gc, Rectangle area) {
        if (image != null && !image.isDisposed()) {
            drawImage(gc, area, image, image.getBounds());
        }
    }

    private void drawImage(GC gc, Rectangle area, Image image,
            Rectangle imgSize) {
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

    public void setPrefWidth(int prefWidth) {
        if (prefWidth == this.prefWidth)
            return;
        int oldPrefWidth = this.prefWidth;
        this.prefWidth = prefWidth;
        updatePrefSize(oldPrefWidth, getPrefHeight());
    }

    public int getPrefWidth() {
        return prefWidth;
    }

    public void setPrefHeight(int prefHeight) {
        if (prefHeight == this.prefHeight)
            return;
        int oldPrefHeight = this.prefHeight;
        this.prefHeight = prefHeight;
        updatePrefSize(getPrefWidth(), oldPrefHeight);
    }

    public int getPrefHeight() {
        return prefHeight;
    }

    private void updatePrefSize(int oldPrefWidth, int oldPrefHeight) {
        if (calculatePrefRatio() != 0) {
            changeRatio(calculatePrefRatio(), oldPrefWidth, oldPrefHeight);
        }
    }

    private double calculatePrefRatio() {
        if (image != null && !image.isDisposed()) {
            Rectangle imgSize = image.getBounds();
            double horizontalRatio = ((double) getPrefWidth()) / imgSize.width;
            double verticalRatio = ((double) getPrefHeight()) / imgSize.height;
            return Math.min(horizontalRatio, verticalRatio);
        }
        return 0;
    }

    private void setRatio(double ratio) {
        this.ratio = ratio;
        calculateFigureBorder();
    }

    private double getRatio() {
        return ratio;
    }

    private void setX(double x) {
        this.x = x;
    }

    private void setY(double y) {
        this.y = y;
    }

    public Control getControl() {
        return composite;
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public void setFocus() {
        if (canvas != null && !canvas.isDisposed()) {
            canvas.setFocus();
        }
    }

    public void changeRatio(double ratio) {
        changeRatio(ratio, getPrefWidth(), getPrefHeight());
    }

    private void changeRatio(double ratio, int oldPrefWidth,
            int oldPrefHeight) {
        double oldRatio = this.ratio;
        setRatio(ratio);
        double newRatio = this.ratio;
        if (image != null) {
            Rectangle imgSize = image.getBounds();
            double oldWidth = Math.min(imgSize.width, oldPrefWidth / oldRatio);
            double oldHeight = Math.min(imgSize.height,
                    oldPrefHeight / oldRatio);
            double oldCenterX = x + oldWidth / 2;
            double oldCenterY = y + oldHeight / 2;
            double newWidth = Math.min(imgSize.width,
                    getPrefWidth() / newRatio);
            double newHeight = Math.min(imgSize.height,
                    getPrefHeight() / newRatio);
            setX(Math.min(imgSize.width - newWidth,
                    Math.max(0, oldCenterX - newWidth / 2)));
            setY(Math.min(imgSize.height - newHeight,
                    Math.max(0, oldCenterY - newHeight / 2)));
        }
        updateFontHeight();
        if (canvas != null && !canvas.isDisposed()) {
            canvas.redraw();
        }
    }

    public void setImage(Image image) {
        if (image != null && !image.isDisposed()) {
            Rectangle imgSize = image.getBounds();
            setImage(image, imgSize.x + imgSize.width / 2,
                    imgSize.y + imgSize.height / 2);
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
            setRatio(Math.min(horizontalRatio, verticalRatio));
            double width = Math.min(imgSize.width, getPrefWidth() / getRatio());
            double height = Math.min(imgSize.height,
                    getPrefHeight() / getRatio());
            setX(Math.max(0,
                    Math.min(imgSize.width - width, centerX - width / 2)));
            setY(Math.max(0,
                    Math.min(imgSize.height - height, centerY - height / 2)));
        }
        if (canvas != null && !canvas.isDisposed()) {
            canvas.redraw();
        }
        updateCanvas();
    }

    public Image getImage() {
        return image;
    }

    public void setTitle(String title) {
        if (title == this.title || (title != null && title.equals(this.title)))
            return;

        this.title = title;
        if (canvas != null && !canvas.isDisposed()) {
            canvas.redraw();
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitlePlacement(int titlePlacement) {
        if (titlePlacement == this.titlePlacement)
            return;

        this.titlePlacement = titlePlacement;
        if (canvas != null && !canvas.isDisposed()) {
            canvas.redraw();
        }
    }

    public int getTitlePlacement() {
        return titlePlacement;
    }

    public void setWholeImage(Image wholeImage) {
        if (this.wholeImage != null) {
            this.wholeImage.dispose();
        }
        this.wholeImage = wholeImage;
    }

    public void setSingleImages(Image[] singleImages) {
        if (this.singleImages != null) {
            for (Image image : this.singleImages) {
                if (image != null) {
                    image.dispose();
                }
            }
        }
        this.singleImages = singleImages;
        totalPagesLabel.setText("" + singleImages.length); //$NON-NLS-1$
        totalPagesLabel.getParent().getParent().layout();
    }

    private void calculateFigureBorder() {
        if (singleImages == null || singleImages.length == 0
                || singleImages[0] == null || singleImages[0].isDisposed()) {
            return;
        }
        Rectangle area = canvas.getClientArea();
        if (imageBorderBounds != null) {
            borderFigure.setBounds(new org.eclipse.draw2d.geometry.Rectangle(
                    (int) (imageBorderBounds.x * ratio),
                    (int) (imageBorderBounds.y * ratio),
                    (int) (imageBorderBounds.width * ratio + 2),
                    (int) (imageBorderBounds.height * ratio + 1)));
        }

        textLayer.setBounds(new org.eclipse.draw2d.geometry.Rectangle(0, 0,
                area.width, area.height));
        ((IFigure) (textLayer.getChildren().get(0))).revalidate();
    }

    //make preview image just right full preview control
    public void initPreviewImageRatio() {
        if (getImage() != null && !getImage().isDisposed()) {
            double widthRatio = (double) getPrefWidth()
                    / getImage().getBounds().width;
            double heightRatio = (double) getPrefHeight()
                    / getImage().getBounds().height;
            double ratio = widthRatio < heightRatio ? widthRatio : heightRatio;
            changeRatio(ratio);
        }
    }

    public void setImageIndex(int imageIndex) {
        feedbackLayer.setVisible(imageIndex != 0);

        this.imageIndex = imageIndex;
        pageNumberLabel.setText("" + imageIndex); //$NON-NLS-1$
        pageNumberLabel.getParent().getParent().layout();
        String pageNumber = (imageIndex == 0 ? "" : "- " + imageIndex + " -"); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
        updatePageNumber(pageNumber);
        updateImageButtons();
        updateBackgroundImage();

        Image image = (imageIndex == 0 ? wholeImage
                : singleImages[imageIndex - 1]);
        setImage(image);
    }

    private void updatePageNumber(String text) {
        if (pageNumberFigure != null) {
            pageNumberFigure.setText(text);
            Font font = Display.getCurrent().getSystemFont();
            int height = (font.getFontData())[0].getHeight();
            font = FontUtils.getNewHeight(font,
                    (int) (height * getFontRatio() * Show_Font_Multiple));
            pageNumberFigure.setFont(font);
            pageNumberFigure.setVisible(!"".equals(text)); //$NON-NLS-1$
            pageNumberFigure.revalidate();
            canvas.layout();
        }
    }

    private double getFontRatio() {
        if (image == null) {
            return ratio;
        } else {
            Rectangle bounds = image.getBounds();
            int pageWidth = PrintConstants.PAGE_LENGTH;
            int pageHeight = PrintConstants.PAGE_SHORT;
            if (bounds.width < bounds.height) {
                pageWidth = PrintConstants.PAGE_SHORT;
                pageHeight = PrintConstants.PAGE_LENGTH;
            }
            double widthRatio = (double) getPrefWidth() / pageWidth;
            double heightRatio = (double) getPrefHeight() / pageHeight;

            return widthRatio < heightRatio ? widthRatio : heightRatio;
        }
    }

    public void setImageBorderBounds(Rectangle bounds) {
        this.imageBorderBounds = new Rectangle(bounds.x, bounds.y, bounds.width,
                bounds.height);
    }

    public void updateHeaderPreview(String text, String alignValue,
            int defaultDraw2DAlign, String fontValue) {
        updateHFPreview(text, alignValue, defaultDraw2DAlign, fontValue,
                headerFigure);
    }

    public void updateFooterPreview(String text, String alignValue,
            int defaultDraw2DAlign, String fontValue) {
        updateHFPreview(text, alignValue, defaultDraw2DAlign, fontValue,
                footerFigure);
    }

    private void updateHFPreview(String text, String alignValue,
            int defaultDraw2DAlign, String fontValue, ITextFigure textFigure) {

        if (textFigure != null) {
            textFigure.setText(text);
            textFigure.setTextAlignment(PrintConstants
                    .toDraw2DAlignment(alignValue, defaultDraw2DAlign));
            Font font = null;
            if (fontValue != null) {
                font = FontUtils.getFont(fontValue);
            }
            if (font == null) {
                font = Display.getCurrent().getSystemFont();
            }
            if (textFigure == headerFigure) {
                headerFont = font;
            } else {
                footerFont = font;
            }
            int height = (font.getFontData())[0].getHeight();
            font = FontUtils.getNewHeight(font,
                    (int) (height * getFontRatio() * Show_Font_Multiple));

            textFigure.setFont(font);

            textFigure.setVisible(!"".equals(text)); //$NON-NLS-1$

            textFigure.revalidate();

            canvas.layout();
        }
    }

    public void setFeedbackVisible(boolean visible) {
        if (feedbackLayer != null) {
            feedbackLayer.setVisible(visible);
        }
    }

    public void setBorderVisible(boolean visible) {
        if (borderFigure != null) {
            borderFigure.setVisible(visible);
            canvas.layout();
        }
    }

    public void setPageNumberVisible(boolean visible) {
        if (pageNumberLayer != null) {
            pageNumberLayer.setVisible(visible);
            canvas.layout();
        }
    }

    private void updateFontHeight() {
        setFontHeightToPreview(headerFigure);
        setFontHeightToPreview(footerFigure);
        setFontHeightToPreview(pageNumberFigure);
    }

    private void setFontHeightToPreview(IFigure target) {
        if (target == null) {
            return;
        }
        Font font = target == pageNumberFigure
                ? Display.getCurrent().getSystemFont()
                : (target == headerFigure ? headerFont : footerFont);
        if (font == null) {
            return;
        }
        int height = (font.getFontData())[0].getHeight();
        font = FontUtils.getNewHeight(font,
                (int) (height * getFontRatio() * Show_Font_Multiple));
        target.setFont(font);
    }

    private Image getIcon(String path, boolean enabled) {
        return (Image) resources.get(
                MindMapUIPlugin.imageDescriptorFromPlugin(MindMapUI.PLUGIN_ID,
                        "icons/nav/" + (enabled ? "e/" : "d/") + path)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

}
