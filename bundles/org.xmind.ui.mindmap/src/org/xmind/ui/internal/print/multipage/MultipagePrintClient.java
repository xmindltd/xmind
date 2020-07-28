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

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Stack;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.SWTGraphics;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.xmind.gef.GEF;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.draw2d.RotatableWrapLabel;
import org.xmind.gef.draw2d.graphics.Rotate90Graphics;
import org.xmind.gef.image.FigureRenderer;
import org.xmind.gef.image.IExportSourceProvider;
import org.xmind.gef.util.Properties;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.print.PrintConstants;
import org.xmind.ui.internal.print.PrintUtils;
import org.xmind.ui.mindmap.GhostShellProvider;
import org.xmind.ui.mindmap.IMindMap;
import org.xmind.ui.mindmap.IMindMapViewer;
import org.xmind.ui.mindmap.MindMapExportViewer;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.mindmap.MindMapViewerExportSourceProvider;
import org.xmind.ui.resources.FontUtils;
import org.xmind.ui.util.Logger;
import org.xmind.ui.util.UnitConvertor;

public class MultipagePrintClient extends FigureRenderer {

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
        protected void collectContents(List<IFigure> figures) {
            if (settings != null
                    && !settings.getBoolean(PrintConstants.NO_BACKGROUND)) {
                figures.add(getViewer().getLayer(GEF.LAYER_BACKGROUND));
            }
            figures.add(getViewer().getLayer(GEF.LAYER_CONTENTS));
            figures.add(getViewer().getLayer(MindMapUI.LAYER_TITLE));
        }
    }

//    private static final int TEXT_MARGIN = 5;
    private static final int TEXT_MARGIN = 0;

    private String jobName;

    private Shell parentShell;

    private PrinterData printerData;

    private IDialogSettings settings;

    private IMindMap sourceMap;

    private Printer printer;

    private Rectangle pageClientArea;

    private Point dpi;

    private IExportSourceProvider source;

    private boolean jobStarted = false;

    private Transform transform;

    public MultipagePrintClient(String jobName, Shell parentShell,
            PrinterData printerData, IDialogSettings settings) {
        this.jobName = jobName;
        this.parentShell = parentShell;
        this.printerData = printerData;
        this.settings = settings;
    }

    public void print(IMindMap sourceMap) {
        this.sourceMap = sourceMap;
        if (!start()) {
            if (Util.isMac()) {
                MindMapUIPlugin.log(null,
                        "[print] printer start job failed..."); //$NON-NLS-1$
            }
            return;
        }

        try {
            new ProgressMonitorDialog(Display.getCurrent().getActiveShell())
                    .run(false, false, new IRunnableWithProgress() {

                        public void run(final IProgressMonitor monitor)
                                throws InvocationTargetException,
                                InterruptedException {
                            parentShell.getDisplay().syncExec(new Runnable() {

                                public void run() {
                                    print(monitor);
                                }
                            });
                        }
                    });
        } catch (InvocationTargetException e) {
            if (Util.isMac()) {
                MindMapUIPlugin.log(e, null);
            }
            e.printStackTrace();
        } catch (InterruptedException e) {
            if (Util.isMac()) {
                MindMapUIPlugin.log(e, null);
            }
            e.printStackTrace();
        }
    }

    private void print(IProgressMonitor monitor) {
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

        GhostShellProvider shell = new GhostShellProvider(
                parentShell.getDisplay());
        IGraphicalViewer exportViewer = new MindMapExportViewer(shell,
                sourceMap, properties);
        source = new MindMapViewerPrintSourceProvider(exportViewer, 0,
                settings);

        setFigures(source.getContents());
        int margin = PrintMultipageUtils.getMargin(source.getSourceArea());
        setBounds(new Rectangle(source.getSourceArea())
                .expand(new Insets(margin)));

        internalPrint(monitor);
    }

    private boolean getBoolean(IDialogSettings settings, String key,
            boolean defaultValue) {
        boolean value = defaultValue;
        if (settings.get(key) != null) {
            value = settings.getBoolean(key);
        }

        return value;
    }

    private void internalPrint(IProgressMonitor monitor) {
        Rectangle bounds = getBounds();
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

        //trim per page print content by header and footer height
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
                - bottomMarginPixel;
        usefulPerPageHeight -= headerHeight + footerHeight;

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

        //use the print content to fit different actual paper size
        //actual scale
        double widthScale = (double) pageClientArea.width
                / usefulPerPageWidthByRatio;
        double heightScale = (double) pageClientArea.height
                / usefulPerPageHeightByRatio;
        double scale = widthScale < heightScale ? widthScale : heightScale;
        setScale(scale);

        //Center then actual printing content after adapting it
        //page origin
        int originX = (int) (pageClientArea.width
                - usefulPerPageWidthByRatio * scale) / 2 + pageClientArea.x;
        int originY = (int) (pageClientArea.height
                - usefulPerPageHeightByRatio * scale) / 2 + pageClientArea.y;
        Rectangle realPageClientArea = new Rectangle(originX, originY,
                (int) (usefulPerPageWidthByRatio * scale),
                (int) (usefulPerPageHeightByRatio * scale));

        monitor.beginTask(MindMapMessages.MultipagePrint_Printing,
                widthPages * heightPages);

        for (int j = 0; j < heightPages; j++) {
            int y = j * usefulPerPageHeightByRatio;
            for (int i = 0; i < widthPages; i++) {
                int x = i * usefulPerPageWidthByRatio;

                int pageNumber = j * widthPages + i + 1;
                boolean isValidPage = i < usefulWidthPages
                        && j < usefulHeightPages;
                render(realPageClientArea, new Point(-x, -y), pageNumber,
                        isValidPage);

                monitor.worked(1);
            }
        }
        monitor.done();
    }

    private void render(Rectangle realPageClientArea, final Point origin,
            int pageNumber, boolean isValidPage) {
        if (!printer.startPage()) {
            if (Util.isMac()) {
                MindMapUIPlugin.log(null,
                        "[print] printer start page failed..."); //$NON-NLS-1$
            }
            return;
        }

        pageClientArea = new Rectangle(realPageClientArea);

        GC gc = new GC(printer);
        try {
            if (isValidPage) {
                pushState(gc);
                render(gc, origin);
                popState(gc);
            }

            //remove clipping
            gc.setClipping((org.eclipse.swt.graphics.Rectangle) null);

            String headerText = settings.get(PrintConstants.HEADER_TEXT);
            if (headerText != null && !"".equals(headerText)) { //$NON-NLS-1$
                drawHeader(gc, headerText);
            }

            String footerText = settings.get(PrintConstants.FOOTER_TEXT);
            if (footerText != null && !"".equals(footerText)) { //$NON-NLS-1$
                drawFooter(gc, footerText);
            }

            //remove clipping
            gc.setClipping((org.eclipse.swt.graphics.Rectangle) null);

            //draw border using adapted bounds
            boolean hasBorder = settings.getBoolean(PrintConstants.BORDER);
            if (hasBorder) {
                gc.setLineWidth(1);
                gc.setForeground(
                        Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
                gc.drawRectangle(pageClientArea.x - 1, pageClientArea.y - 1,
                        pageClientArea.width + 2, pageClientArea.height + 2);
            }

            //draw page number
            drawPageNumber(gc, "- " + pageNumber + " -"); //$NON-NLS-1$ //$NON-NLS-2$
        } finally {
            gc.dispose();
        }

        printer.endPage();
    }

    private void pushState(GC gc) {
        Transform tempTransform = new Transform(gc.getDevice());
        gc.getTransform(tempTransform);
        float[] elements = new float[6];
        tempTransform.getElements(elements);

        if (transform != null && !transform.isDisposed()) {
            transform.dispose();
        }

        transform = new Transform(gc.getDevice(), elements);
        tempTransform.dispose();
    }

    private void popState(GC gc) {
        gc.setTransform(transform);
    }

    private void drawHeader(GC gc, String text) {
        Font font = getFont(PrintConstants.HEADER_FONT);
        try {
            drawText(gc, text, font, getAlign(PrintConstants.HEADER_ALIGN,
                    PositionConstants.CENTER), true);
        } finally {
            font.dispose();
        }
    }

    private void drawFooter(GC gc, String text) {
        Font font = getFont(PrintConstants.FOOTER_FONT);
        try {
            drawText(gc, text, font, getAlign(PrintConstants.FOOTER_ALIGN,
                    PositionConstants.RIGHT), false);
        } finally {
            font.dispose();
        }
    }

    private void drawPageNumber(GC gc, String text) {
        int footerAlign = getAlign(PrintConstants.FOOTER_ALIGN,
                PositionConstants.RIGHT);
        int pageNumberAlign = (footerAlign == PositionConstants.CENTER
                ? PositionConstants.RIGHT : PositionConstants.CENTER);
        drawPageNumber(gc, text, pageNumberAlign);
    }

    private Font getFont(String fontKey) {
        Font font = null;
        String fontValue = settings.get(fontKey);
        if (fontValue != null) {
            FontData[] fontData = FontUtils.toFontData(fontValue);
            if (fontData != null) {
                for (FontData fd : fontData) {
                    fd.setHeight(fd.getHeight() * dpi.y
                            / UnitConvertor.getScreenDpi().y);
                }
                font = new Font(Display.getCurrent(), fontData);
            }
        }
        if (font == null) {
            FontData[] defaultFontData = JFaceResources
                    .getDefaultFontDescriptor().getFontData();
            int defaultHeight = defaultFontData[0].getHeight();
            font = new Font(Display.getCurrent(), FontUtils.newHeight(
                    defaultFontData,
                    defaultHeight * dpi.y / UnitConvertor.getScreenDpi().y));
        }
        return font;
    }

    private int getAlign(String alignKey, int defaultAlign) {
        return PrintConstants.toDraw2DAlignment(settings.get(alignKey),
                defaultAlign);
    }

    private void drawText(GC gc, String text, Font font, int alignment,
            boolean top) {
        RotatableWrapLabel label = new RotatableWrapLabel();
        label.setText(text);
        label.setFont(font);
        label.setTextAlignment(alignment);
        label.setForegroundColor(
                parentShell.getDisplay().getSystemColor(SWT.COLOR_BLACK));
        int width = pageClientArea.width;
        int marginWidth = TEXT_MARGIN * dpi.x / UnitConvertor.getScreenDpi().x;
        width -= marginWidth * 2;

        Dimension size = label.getPreferredSize(width, -1);
        int x = -width / 2;

        org.eclipse.swt.graphics.Rectangle pageBounds = printer.getClientArea();
        int y;
        if (top) {
            y = -pageClientArea.height / 2 - (pageClientArea.y - pageBounds.y)
                    + Math.max(
                            (pageClientArea.y - pageBounds.y - size.height) / 2,
                            marginWidth);
        } else {
            y = pageClientArea.height / 2
                    + (pageBounds.y + pageBounds.height
                            - (pageClientArea.y + pageClientArea.height))
                    - size.height
                    - Math.max((pageBounds.y + pageBounds.height
                            - (pageClientArea.y + pageClientArea.height)
                            - size.height) / 2, marginWidth);

        }
        label.setBounds(new Rectangle(x, y, width, size.height));

        SWTGraphics baseGraphics = new SWTGraphics(gc);
        baseGraphics.translate(pageClientArea.x + pageClientArea.width / 2,
                pageClientArea.y + pageClientArea.height / 2);

        Graphics graphics = baseGraphics;

        Rotate90Graphics rotatedGraphics = null;
        try {
            label.paint(graphics);
        } catch (Throwable e) {
            Logger.log(e, "Error occurred while printing"); //$NON-NLS-1$
        } finally {
            if (rotatedGraphics != null) {
                rotatedGraphics.dispose();
            }
            baseGraphics.dispose();
        }
    }

    private void drawPageNumber(GC gc, String text, int alignment) {
        RotatableWrapLabel label = new RotatableWrapLabel();
        label.setText(text);
        Font font = Display.getCurrent().getSystemFont();
        font = FontUtils.getNewHeight(font, (font.getFontData())[0].getHeight()
                * dpi.y / UnitConvertor.getScreenDpi().y);
        label.setFont(font);
        label.setTextAlignment(alignment);
        label.setForegroundColor(
                parentShell.getDisplay().getSystemColor(SWT.COLOR_BLACK));

        Rectangle pageBounds = new Rectangle(printer.getClientArea());
        int width = pageClientArea.width;
        int marginWidth = TEXT_MARGIN * dpi.x / UnitConvertor.getScreenDpi().x;
        width -= marginWidth * 2;

        Dimension size = label.getPreferredSize(width, -1);
        int x = -width / 2;

        int y = pageBounds.height / 2 - size.height
                - Math.max((pageBounds.y + pageBounds.height
                        - (pageClientArea.y + pageClientArea.height)
                        - size.height) / 2, marginWidth);
        label.setBounds(new Rectangle(x, y, width, size.height));

        SWTGraphics baseGraphics = new SWTGraphics(gc);
        baseGraphics.translate(pageBounds.x + pageBounds.width / 2,
                pageBounds.y + pageBounds.height / 2);

        Graphics graphics = baseGraphics;

        Rotate90Graphics rotatedGraphics = null;
        try {
            label.paint(graphics);
        } catch (Throwable e) {
            Logger.log(e, "Error occurred while printing"); //$NON-NLS-1$
        } finally {
            if (rotatedGraphics != null) {
                rotatedGraphics.dispose();
            }
            baseGraphics.dispose();
        }
    }

    private boolean start() {
        if (printer == null) {
            printer = new Printer(printerData);
        }

        receivePrinterInfo();
        if (pageClientArea.width <= 0 || pageClientArea.height <= 0) {
            Display.getCurrent().asyncExec(new Runnable() {

                public void run() {
                    MessageDialog.openInformation(
                            Display.getDefault().getActiveShell(),
                            MindMapMessages.MultipagePrint_InvalidMargin_title,
                            MindMapMessages.MultipagePrint_InvalidMargin_message);
                }
            });
            return false;
        }

        if (!jobStarted) {
            if (!printer.startJob(jobName))
                return false;
            jobStarted = true;
        }

        return jobStarted;
    }

    private void receivePrinterInfo() {
        dpi = new Point(printer.getDPI());
        pageClientArea = new Rectangle(printer.getClientArea());

        int leftMargin = getUserMargin(PrintConstants.LEFT_MARGIN);
        int rightMargin = getUserMargin(PrintConstants.RIGHT_MARGIN);
        int topMargin = getUserMargin(PrintConstants.TOP_MARGIN);
        int bottomMargin = getUserMargin(PrintConstants.BOTTOM_MARGIN);

        pageClientArea.x += leftMargin;
        pageClientArea.y += topMargin;
        pageClientArea.width -= leftMargin + rightMargin;
        pageClientArea.height -= topMargin + bottomMargin;

        //trim clientArea by header and footer height
        int headerHeight = PrintUtils.getHeaderHeight(settings, dpi.y);
        int footerHeight = PrintUtils.getBottomHeight(settings, dpi.y);

        pageClientArea.expand(new Insets(-headerHeight, 0, -footerHeight, 0));
    }

    private int getUserMargin(String key) {
        double marginInch;
        try {
            marginInch = settings.getDouble(key);
        } catch (NumberFormatException e) {
            marginInch = PrintConstants.DEFAULT_MARGIN;
        }
        double dpi;
        if (PrintConstants.LEFT_MARGIN.equals(key)
                || PrintConstants.RIGHT_MARGIN.equals(key)) {
            dpi = this.dpi.x;
        } else {
            dpi = this.dpi.y;
        }
        return (int) (marginInch * dpi);
    }

    public void dispose() {
        if (printer != null) {
            if (!printer.isDisposed()) {
                printer.endJob();
            }
            printer.dispose();
            printer = null;
        }
        jobStarted = false;
        if (transform != null) {
            transform.dispose();
        }
    }

    protected void createGraphics(Graphics graphics, Stack<Graphics> stack) {
        graphics.clipRect(new Rectangle(pageClientArea.x, pageClientArea.y,
                pageClientArea.width, pageClientArea.height));
        graphics.translate(pageClientArea.x, pageClientArea.y);
        if (getScale() > 0) {
            graphics.scale(getScale());
            stack.push(graphics);
        }
        Rectangle bounds = getBounds();
        graphics.translate(-bounds.x, -bounds.y);
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

}
