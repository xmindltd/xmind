package org.xmind.ui.internal.print.multipage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Display;
import org.xmind.gef.GEF;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.image.FigureRenderer;
import org.xmind.gef.image.IExportSourceProvider;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.gef.util.Properties;
import org.xmind.ui.internal.print.PrintConstants;
import org.xmind.ui.internal.print.PrintUtils;
import org.xmind.ui.mindmap.GhostShellProvider;
import org.xmind.ui.mindmap.IMindMap;
import org.xmind.ui.mindmap.IMindMapViewer;
import org.xmind.ui.mindmap.MindMapExportViewer;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.mindmap.MindMapViewerExportSourceProvider;
import org.xmind.ui.resources.ColorUtils;

public class PrintMultipagePreviewImageCreator {

    private static final String IMG_BG_COLOR = "#ffffff"; //$NON-NLS-1$

    private static class MindMapViewerPrintSourceProvider
            extends MindMapViewerExportSourceProvider {

        private IDialogSettings settings;

        public MindMapViewerPrintSourceProvider(IGraphicalViewer viewer,
                IDialogSettings settings) {
            super(viewer);
            this.settings = settings;
        }

        public MindMapViewerPrintSourceProvider(IGraphicalViewer viewer,
                int margins, IDialogSettings settings) {
            super(viewer, margins);
            this.settings = settings;
        }

        @Override
        public IFigure[] getContents() {
            return collectContents();
        }

        private IFigure[] collectContents() {
            List<IFigure> figures = new ArrayList<IFigure>(3);
            collectContents(figures);
            return figures.toArray(new IFigure[figures.size()]);
        }

        @Override
        protected void collectContents(List<IFigure> figures) {
            if (settings != null
                    && !settings.getBoolean(PrintConstants.NO_BACKGROUND)) {
                figures.add(getViewer().getLayer(GEF.LAYER_BACKGROUND));
            }
            figures.add(getViewer().getLayer(GEF.LAYER_CONTENTS));
            figures.add(getViewer().getLayer(MindMapUI.LAYER_TITLE));
        }
    }

    private Display display;

    private IGraphicalEditorPage page;

    private IMindMap mindmap;

    private IDialogSettings settings;

    private IGraphicalViewer exportViewer;

    private IExportSourceProvider sourceProvider;

    private Image fullImage;

    private Image[][] singleImages;

    private boolean isSourceImageValid;

    private List<Image> images = new ArrayList<Image>();

    public PrintMultipagePreviewImageCreator(Display display,
            IGraphicalEditorPage page, IMindMap sourceMindmap,
            IDialogSettings settings) {
        this.display = display;
        this.page = page;
        this.mindmap = sourceMindmap;
        this.settings = settings;

        display.asyncExec(new Runnable() {

            public void run() {
                initSource();
            }
        });
    }

    private void initSource() {
        Properties properties = new Properties();
        properties.set(IMindMapViewer.VIEWER_GRADIENT, Boolean.FALSE);

        //set plus minus visibility
        boolean plusVisible = getBoolean(settings, PrintConstants.PLUS_VISIBLE,
                PrintConstants.DEFAULT_PLUS_VISIBLE);
        boolean minusVisible = getBoolean(settings,
                PrintConstants.MINUS_VISIBLE,
                PrintConstants.DEFAULT_MINUS_VISIBLE);
        properties.set(IMindMapViewer.PLUS_VISIBLE, plusVisible);
        properties.set(IMindMapViewer.MINUS_VISIBLE, minusVisible);

        GhostShellProvider shell = new GhostShellProvider(display);
        exportViewer = new MindMapExportViewer(shell, mindmap, properties);
        sourceProvider = new MindMapViewerPrintSourceProvider(exportViewer, 0,
                settings);
    }

    private boolean getBoolean(IDialogSettings settings, String key,
            boolean defaultValue) {
        boolean value = defaultValue;
        if (settings.get(key) != null) {
            value = settings.getBoolean(key);
        }

        return value;
    }

    private Image getFullImage() {
        if (!isSourceImageValid && fullImage != null) {
            fullImage.dispose();
            fullImage = null;
        }
        if (fullImage == null || fullImage.isDisposed()) {
            fullImage = createFullImage();
        }
        return fullImage;
    }

    private Image createFullImage() {
        final org.eclipse.draw2d.geometry.Rectangle bounds = PrintMultipageUtils
                .getSheetFigureBounds(page, mindmap);
        final Image image = new Image(display, bounds.width, bounds.height);

        final GC gc = new GC(image);
        try {
            Display.getDefault().syncExec(new Runnable() {

                public void run() {
                    FigureRenderer render = new FigureRenderer();

                    render.setFigures(sourceProvider.getContents());
                    int margin = PrintMultipageUtils
                            .getMargin(sourceProvider.getSourceArea());
                    render.setBounds(new org.eclipse.draw2d.geometry.Rectangle(
                            sourceProvider.getSourceArea())
                                    .expand(new Insets(margin)));
                    render.setScale(1);

                    render.render(gc, new Point(0, 0));
                }
            });
        } finally {
            gc.dispose();
        }

        isSourceImageValid = true;
        return image;
    }

    public Image createPrintPreviewSingleImage() {
        final int leftMarginPixel = PrintConstants.toPixel(getDouble(
                PrintConstants.LEFT_MARGIN, PrintConstants.DEFAULT_MARGIN));
        int rightMarginPixel = PrintConstants.toPixel(getDouble(
                PrintConstants.RIGHT_MARGIN, PrintConstants.DEFAULT_MARGIN));
        final int topMarginPixel = PrintConstants.toPixel(getDouble(
                PrintConstants.TOP_MARGIN, PrintConstants.DEFAULT_MARGIN));
        int bottomMarginPixel = PrintConstants.toPixel(getDouble(
                PrintConstants.BOTTOM_MARGIN, PrintConstants.DEFAULT_MARGIN));

        final int headerHeight = PrintUtils.getHeaderHeight(settings,
                PrintConstants.DEFAULT_DPI);
        int footerHeight = PrintUtils.getBottomHeight(settings,
                PrintConstants.DEFAULT_DPI);

        //Calculate the actual needed printed content of the each page according to the default paper size(A4)
        //Then use the print content to fit different actual paper size
        int orientation = getInteger(PrintConstants.ORIENTATION,
                PrintConstants.DEFAULT_ORIENTATION);
        int perPageWidth = orientation == PrinterData.LANDSCAPE
                ? PrintConstants.PAGE_LENGTH : PrintConstants.PAGE_SHORT;
        int usefulPerPageWidth = perPageWidth - leftMarginPixel
                - rightMarginPixel;

        int perPageHeight = orientation == PrinterData.PORTRAIT
                ? PrintConstants.PAGE_LENGTH : PrintConstants.PAGE_SHORT;
        int usefulPerPageHeight = perPageHeight - topMarginPixel
                - bottomMarginPixel - headerHeight - footerHeight;

        final org.eclipse.draw2d.geometry.Rectangle bounds = PrintMultipageUtils
                .getSheetFigureBounds(page, mindmap);
        double widthRatio = (double) usefulPerPageWidth / bounds.width;
        double heightRatio = (double) usefulPerPageHeight / bounds.height;
        final double ratio = Math.min(widthRatio, heightRatio);

        final int actualWidth = (int) (usefulPerPageWidth / ratio);
        final int actualHeight = (int) (usefulPerPageHeight / ratio);

        final Image previewSingleImage = new Image(display,
                (int) (perPageWidth / ratio), (int) (perPageHeight / ratio));
        images.add(previewSingleImage);
        final GC gc = new GC(previewSingleImage);
        try {
            Display.getDefault().syncExec(new Runnable() {

                public void run() {
                    FigureRenderer render = new FigureRenderer();

                    render.setFigures(sourceProvider.getContents());
                    int margin = PrintMultipageUtils
                            .getMargin(sourceProvider.getSourceArea());

                    int widthPadding = (actualWidth - bounds.width) / 2;
                    int heightPadding = (actualHeight - bounds.height) / 2;
                    render.setBounds(new org.eclipse.draw2d.geometry.Rectangle(
                            sourceProvider.getSourceArea())
                                    .expand(new Insets(margin))
                                    .expand(new Insets(heightPadding,
                                            widthPadding, heightPadding,
                                            widthPadding)));
                    render.setScale(1);

                    gc.setClipping(
                            new Rectangle((int) (leftMarginPixel / ratio),
                                    (int) ((topMarginPixel + headerHeight)
                                            / ratio),
                                    actualWidth, actualHeight));
                    render.render(gc,
                            new Point((int) (leftMarginPixel
                                    / ratio),
                            (int) ((topMarginPixel + headerHeight) / ratio)));
                }
            });
        } finally {
            gc.dispose();
        }

        return previewSingleImage;
    }

    public Image createPrintPreviewDetailedImage() {
        if (!checkImage()) {
            return null;
        }
        Image[][] images = getSingleImages2();
        int perWidth = images[0][0].getBounds().width;
        int width = perWidth * images[0].length;

        int perHeight = images[0][0].getBounds().height;
        int height = perHeight * images.length;

        Image detailedImage = new Image(images[0][0].getDevice(), width,
                height);
        this.images.add(detailedImage);
        GC gc = new GC(detailedImage);
        try {
            for (int i = 0; i < images.length; i++) {
                Image[] imageArr = images[i];
                int y = i * perHeight;
                for (int j = 0; j < imageArr.length; j++) {
                    Image image = imageArr[j];
                    int x = j * perWidth;
                    gc.drawImage(image, x, y);
                }
            }
        } finally {
            gc.dispose();
        }

        return detailedImage;
    }

    public Image[] getSingleImages() {
        if (!checkImage()) {
            return new Image[0];
        }
        Image[][] images = getSingleImages2();

        //translate from image[][] to image[]
        Image[] images2 = new Image[images.length * images[0].length];
        for (int i = 0; i < images.length; i++) {
            for (int j = 0; j < images[0].length; j++) {
                images2[i * images[0].length + j] = images[i][j];
            }
        }

        return images2;
    }

    private Image[][] getSingleImages2() {
        if (singleImages == null || singleImages[0] == null
                || singleImages[0][0] == null
                || singleImages[0][0].isDisposed()) {
            if (singleImages != null) {
                for (Image[] images : singleImages) {
                    if (images != null) {
                        for (Image image : images) {
                            if (image != null) {
                                image.dispose();
                            }
                        }
                    }
                }
            }

            singleImages = createSingleImages();
        }
        return singleImages;
    }

    //create detailed single images
    private Image[][] createSingleImages() {
        org.eclipse.draw2d.geometry.Rectangle bounds = PrintMultipageUtils
                .getSheetFigureBounds(page, mindmap);
        int sourceWidth = bounds.width;
        int sourceHeight = bounds.height;

        int leftMarginPixel = PrintConstants.toPixel(getDouble(
                PrintConstants.LEFT_MARGIN, PrintConstants.DEFAULT_MARGIN));
        int rightMarginPixel = PrintConstants.toPixel(getDouble(
                PrintConstants.RIGHT_MARGIN, PrintConstants.DEFAULT_MARGIN));
        int topMarginPixel = PrintConstants.toPixel(getDouble(
                PrintConstants.TOP_MARGIN, PrintConstants.DEFAULT_MARGIN));
        int bottomMarginPixel = PrintConstants.toPixel(getDouble(
                PrintConstants.BOTTOM_MARGIN, PrintConstants.DEFAULT_MARGIN));

        int headerHeight = PrintUtils.getHeaderHeight(settings,
                PrintConstants.DEFAULT_DPI);
        int footerHeight = PrintUtils.getBottomHeight(settings,
                PrintConstants.DEFAULT_DPI);

        //Calculate the actual needed printed content of the each page according to the default paper size(A4)
        //Then use the print content to fit different actual paper size
        int orientation = getInteger(PrintConstants.ORIENTATION,
                PrintConstants.DEFAULT_ORIENTATION);
        int perPageWidth = orientation == PrinterData.LANDSCAPE
                ? PrintConstants.PAGE_LENGTH : PrintConstants.PAGE_SHORT;
        int usefulPerPageWidth = perPageWidth - leftMarginPixel
                - rightMarginPixel;

        int perPageHeight = orientation == PrinterData.PORTRAIT
                ? PrintConstants.PAGE_LENGTH : PrintConstants.PAGE_SHORT;
        int usefulPerPageHeight = perPageHeight - topMarginPixel
                - bottomMarginPixel - headerHeight - footerHeight;

        int widthPages = getInteger(PrintConstants.WIDTH_PAGES, 1);
        int heightPages = getInteger(PrintConstants.HEIGHT_PAGES, 1);
        boolean isAspectRatio = settings
                .getBoolean(PrintConstants.ASPECT_RATIO_LOCKED);
        boolean fullWidth = !settings.getBoolean(PrintConstants.FILL_HEIGHT);

        if (!isAspectRatio) {
            double fillWidthratio = (double) usefulPerPageWidth * widthPages
                    / sourceWidth;
            double fillHeightRatio = (double) usefulPerPageHeight * heightPages
                    / sourceHeight;
            fullWidth = (fillWidthratio <= fillHeightRatio) ? true : false;
        }

        double ratio = fullWidth
                ? ((double) usefulPerPageWidth * widthPages / sourceWidth)
                : ((double) usefulPerPageHeight * heightPages / sourceHeight);

        //The actual needed printed content of the each page calculated by the default paper size(A4)
        int usefulPerPageWidthByRatio = (int) (usefulPerPageWidth / ratio);
        int usefulPerPageHeightByRatio = (int) (usefulPerPageHeight / ratio);
        int usefulWidthPages = widthPages;
        int usefulHeightPages = heightPages;

        if (fullWidth) {
            usefulHeightPages = sourceHeight / usefulPerPageHeightByRatio;
            usefulHeightPages = (sourceHeight % usefulPerPageHeightByRatio == 0)
                    ? usefulHeightPages : usefulHeightPages + 1;
        } else {
            usefulWidthPages = sourceWidth / usefulPerPageWidthByRatio;
            usefulWidthPages = (sourceWidth % usefulPerPageWidthByRatio == 0)
                    ? usefulWidthPages : usefulWidthPages + 1;
        }

        Image fullImage = getFullImage();
        Image[][] images = new Image[heightPages][widthPages];

        //create single images
        for (int j = 0; j < heightPages; j++) {
            int y0 = j * usefulPerPageHeightByRatio;
            int height = sourceHeight - y0;
            height = Math.min(height, usefulPerPageHeightByRatio);

            for (int i = 0; i < widthPages; i++) {
                int x0 = i * usefulPerPageWidthByRatio;
                int width = sourceWidth - x0;
                width = Math.min(width, usefulPerPageWidthByRatio);

                Image image = new Image(display, (int) (perPageWidth / ratio),
                        (int) (perPageHeight / ratio));
                this.images.add(image);

                GC gc = new GC(image);
                try {
                    //draw image background
                    Rectangle imageBounds = image.getBounds();
                    gc.setBackground(ColorUtils.getColor(IMG_BG_COLOR));
                    gc.fillRectangle(0, 0, imageBounds.width - 1,
                            imageBounds.height - 1);

                    int leftMarginByRatio = (int) (leftMarginPixel / ratio);
                    int topMarginByRation = (int) ((topMarginPixel
                            + headerHeight) / ratio);

                    if (i < usefulWidthPages && j < usefulHeightPages) {
                        //draw image content
                        gc.drawImage(fullImage, x0, y0, width, height,
                                leftMarginByRatio, topMarginByRation, width,
                                height);
                    }
                } finally {
                    gc.dispose();
                }
                images[j][i] = image;
            }
        }

        return images;
    }

    //create full image with dash line
    public Image createPrintPreviewRoughImage(int controlWidth,
            int controlHeight) {
        org.eclipse.draw2d.geometry.Rectangle bounds = PrintMultipageUtils
                .getSheetFigureBounds(page, mindmap);
        int sourceWidth = bounds.width;
        int sourceHeight = bounds.height;

        int leftMarginPixel = PrintConstants.toPixel(getDouble(
                PrintConstants.LEFT_MARGIN, PrintConstants.DEFAULT_MARGIN));
        int rightMarginPixel = PrintConstants.toPixel(getDouble(
                PrintConstants.RIGHT_MARGIN, PrintConstants.DEFAULT_MARGIN));
        int topMarginPixel = PrintConstants.toPixel(getDouble(
                PrintConstants.TOP_MARGIN, PrintConstants.DEFAULT_MARGIN));
        int bottomMarginPixel = PrintConstants.toPixel(getDouble(
                PrintConstants.BOTTOM_MARGIN, PrintConstants.DEFAULT_MARGIN));

        int headerHeight = PrintUtils.getHeaderHeight(settings,
                PrintConstants.DEFAULT_DPI);
        int footerHeight = PrintUtils.getBottomHeight(settings,
                PrintConstants.DEFAULT_DPI);

        //Calculate the actual needed printed content of the each page according to the default paper size(A4)
        //Then use the print content to fit different actual paper size
        int orientation = getInteger(PrintConstants.ORIENTATION,
                PrintConstants.DEFAULT_ORIENTATION);
        int perPageWidth = orientation == PrinterData.LANDSCAPE
                ? PrintConstants.PAGE_LENGTH : PrintConstants.PAGE_SHORT;
        int usefulPerPageWidth = perPageWidth - leftMarginPixel
                - rightMarginPixel;

        int perPageHeight = orientation == PrinterData.PORTRAIT
                ? PrintConstants.PAGE_LENGTH : PrintConstants.PAGE_SHORT;
        int usefulPerPageHeight = perPageHeight - topMarginPixel
                - bottomMarginPixel - headerHeight - footerHeight;

        int widthPages = getInteger(PrintConstants.WIDTH_PAGES, 1);
        int heightPages = getInteger(PrintConstants.HEIGHT_PAGES, 1);
        boolean isAspectRatio = settings
                .getBoolean(PrintConstants.ASPECT_RATIO_LOCKED);
        boolean fullWidth = !settings.getBoolean(PrintConstants.FILL_HEIGHT);

        if (!isAspectRatio) {
            double fillWidthratio = (double) usefulPerPageWidth * widthPages
                    / sourceWidth;
            double fillHeightRatio = (double) usefulPerPageHeight * heightPages
                    / sourceHeight;
            fullWidth = (fillWidthratio <= fillHeightRatio) ? true : false;
        }

        double ratio = fullWidth
                ? ((double) usefulPerPageWidth * widthPages / sourceWidth)
                : ((double) usefulPerPageHeight * heightPages / sourceHeight);

        //The actual needed printed content of the each page calculated by the default paper size(A4)
        int usefulPerPageWidthByRatio = (int) (usefulPerPageWidth / ratio);
        int usefulPerPageHeightByRatio = (int) (usefulPerPageHeight / ratio);
        int usefulWidthPages = widthPages;
        int usefulHeightPages = heightPages;

        if (fullWidth) {
            usefulHeightPages = sourceHeight / usefulPerPageHeightByRatio;
            usefulHeightPages = (sourceHeight % usefulPerPageHeightByRatio == 0)
                    ? usefulHeightPages : usefulHeightPages + 1;
        } else {
            usefulWidthPages = sourceWidth / usefulPerPageWidthByRatio;
            usefulWidthPages = (sourceWidth % usefulPerPageWidthByRatio == 0)
                    ? usefulWidthPages : usefulWidthPages + 1;
        }

        Image fullImage = getFullImage();

        Image image = new Image(display,
                (int) (usefulPerPageWidthByRatio) * widthPages,
                (int) (usefulPerPageHeightByRatio) * heightPages);
        images.add(image);

        GC gc = new GC(image);
        try {
            //draw image background
            Rectangle imageBounds = image.getBounds();
            gc.setBackground(ColorUtils.getColor(IMG_BG_COLOR));
            gc.fillRectangle(0, 0, imageBounds.width - 1,
                    imageBounds.height - 1);

            gc.drawImage(fullImage, 0, 0);

            gc.setForeground(ColorUtils.getColor("#90d483")); //$NON-NLS-1$

            double showRatio = Math.min(
                    (double) controlWidth / imageBounds.width,
                    (double) controlHeight / imageBounds.height);
            int lineWidth = (int) (2 / showRatio);
            int showTickLength = 2;
            int tickLength = Math.max(1, (int) (showTickLength / showRatio));
            int showSpaceLength = 4;
            int spaceLength = Math.max(1, (int) (showSpaceLength / showRatio));
            gc.setLineWidth(lineWidth);

            //draw portrait separator dash lines
            for (int i = 1; i < widthPages; i++) {
                PrintMultipagePreviewImageCreator.drawDashLine(gc,
                        i * usefulPerPageWidthByRatio, 0,
                        i * usefulPerPageWidthByRatio, imageBounds.height,
                        tickLength, spaceLength, lineWidth);
            }

            //draw landscape separator dash lines
            for (int i = 1; i < heightPages; i++) {
                PrintMultipagePreviewImageCreator.drawDashLine(gc, 0,
                        i * usefulPerPageHeightByRatio, imageBounds.width,
                        i * usefulPerPageHeightByRatio, tickLength, spaceLength,
                        lineWidth);
            }

            //draw image bounds(include margin) 
            gc.setLineWidth(1);
            gc.setForeground(ColorUtils.getColor("#c0c0c0")); //$NON-NLS-1$
            gc.drawRectangle(0, 0, imageBounds.width - 1,
                    imageBounds.height - 1);
        } finally {
            gc.dispose();
        }

        return image;
    }

    private double getDouble(String key, double defaultValue) {
        try {
            return settings.getDouble(key);
        } catch (NumberFormatException e) {
        }
        return defaultValue;
    }

    private int getInteger(String key, int defaultValue) {
        try {
            return settings.getInt(key);
        } catch (NumberFormatException e) {
        }
        return defaultValue;
    }

    public void releaseResource() {
        if (images != null) {
            Iterator<Image> ite = images.iterator();
            while (ite.hasNext()) {
                Image image = ite.next();
                if (image != null) {
                    image.dispose();
                    ite.remove();
                    image = null;
                }

            }
            images.clear();
        }
    }

    public void dispose() {
        releaseResource();
        if (fullImage != null) {
            fullImage.dispose();
        }
    }

    public boolean checkImage() {
        return !isImageLarge(PrintConstants.MAX_IMAGE_SIZE);
    }

    private boolean isImageLarge(int largeSize) {
        org.eclipse.draw2d.geometry.Rectangle bounds = PrintMultipageUtils
                .getSheetFigureBounds(page, mindmap);
        return bounds.width * bounds.height > largeSize;
    }

    public void setSourceImageValid(boolean isSourceImageValid) {
        this.isSourceImageValid = isSourceImageValid;
    }

    public void setShowBackground(boolean showBackground) {
        settings.put(PrintConstants.NO_BACKGROUND, !showBackground);
    }

    public void setPlusVisible(boolean plusVisible) {
        if (exportViewer != null) {
            Properties properties = exportViewer.getProperties();
            properties.set(IMindMapViewer.PLUS_VISIBLE, plusVisible);
        }
    }

    public void setMinusVisible(boolean minusVisible) {
        if (exportViewer != null) {
            Properties properties = exportViewer.getProperties();
            properties.set(IMindMapViewer.MINUS_VISIBLE, minusVisible);
        }
    }

    /**
     * Draw horizontal or vertical dashed line. (Sloping is unsupported)
     * 
     */
    private static void drawDashLine(GC gc, int x1, int y1, int x2, int y2,
            int tickLength, int spaceLength, int lineWidth) {
        if (x1 != x2 && y1 != y2) {
            throw new IllegalArgumentException(
                    "Must satisfy either 'x1 == x2' or 'y1 == y2'"); //$NON-NLS-1$
        }

        if (x1 == x2) {
            int times = Math.abs(y1 - y2) / (tickLength + spaceLength);
            for (int i = 0; i < times; i++) {
                gc.drawLine(x1, i * (tickLength + spaceLength), x2,
                        i * (tickLength + spaceLength) + tickLength);
            }

            int remainder = Math.abs(y1 - y2) % (tickLength + spaceLength);
            if (remainder > 0) {
                int length = remainder < tickLength ? remainder : tickLength;
                gc.drawLine(x1, times * (tickLength + spaceLength), x2,
                        times * (tickLength + spaceLength) + length);
            }
        } else {
            int times = Math.abs(x1 - x2) / (tickLength + spaceLength);
            for (int i = 0; i < times; i++) {
                gc.drawLine(i * (tickLength + spaceLength), y1,
                        i * (tickLength + spaceLength) + tickLength, y2);
            }

            int remainder = Math.abs(x1 - x2) % (tickLength + spaceLength);
            if (remainder > 0) {
                int length = remainder < tickLength ? remainder : tickLength;
                gc.drawLine(times * (tickLength + spaceLength), y1,
                        times * (tickLength + spaceLength) + length, y2);
            }
        }
    }

}
