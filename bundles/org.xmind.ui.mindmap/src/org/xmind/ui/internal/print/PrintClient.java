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
package org.xmind.ui.internal.print;

import java.util.List;
import java.util.Stack;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.SWTGraphics;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.dialogs.IDialogSettings;
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
import org.xmind.gef.draw2d.graphics.ScaledGraphics;
import org.xmind.gef.image.FigureRenderer;
import org.xmind.gef.image.IExportAreaProvider;
import org.xmind.gef.image.IExportSourceProvider;
import org.xmind.gef.image.ImageExportUtils;
import org.xmind.gef.image.ResizeConstants;
import org.xmind.gef.util.Properties;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.mindmap.GhostShellProvider;
import org.xmind.ui.mindmap.IMindMap;
import org.xmind.ui.mindmap.IMindMapViewer;
import org.xmind.ui.mindmap.MindMapExportViewer;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.mindmap.MindMapViewerExportSourceProvider;
import org.xmind.ui.resources.FontUtils;
import org.xmind.ui.util.Logger;
import org.xmind.ui.util.UnitConvertor;

public class PrintClient extends FigureRenderer {

    private class MindMapViewerPrintSourceProvider
            extends MindMapViewerExportSourceProvider {

        /**
         * @param viewer
         */
        public MindMapViewerPrintSourceProvider(IGraphicalViewer viewer) {
            super(viewer);
        }

        /**
         * 
         */
        public MindMapViewerPrintSourceProvider(IGraphicalViewer viewer,
                int margins) {
            super(viewer, margins);
        }

        /*
         * (non-Javadoc)
         * @see org.xmind.ui.mindmap.MindMapViewerExportSourceProvider#
         * collectContents (java.util.List)
         */
        @Override
        protected void collectContents(List<IFigure> figures) {
            if (!settings.getBoolean(PrintConstants.NO_BACKGROUND)) {
                figures.add(getViewer().getLayer(GEF.LAYER_BACKGROUND));
            }
            figures.add(getViewer().getLayer(GEF.LAYER_CONTENTS));
            figures.add(getViewer().getLayer(MindMapUI.LAYER_TITLE));
        }
    }

    private static final int VIEWER_MARGIN = 15;

//    private static final int TEXT_MARGIN = 5;
    private static final int TEXT_MARGIN = 0;

    private String jobName;

    private PrinterData printerData;

    private IDialogSettings settings;

    private Shell parentShell;

    private Printer printer = null;

    private Rectangle pageBounds = null;

    private Rectangle pageClientArea = null;

    private Point dpi = null;

    private boolean needRotate = false;

    private boolean jobStarted = false;

//    private List<MindMapExportContentProvider> providers = null;

    private IExportSourceProvider source = null;

    private IExportAreaProvider area = null;

    private GhostShellProvider shell = null;

    private Transform transform;

    public PrintClient(String jobName, Shell parentShell,
            PrinterData printerData, IDialogSettings settings) {
        this.jobName = jobName;
        this.parentShell = parentShell;
        this.printerData = printerData;
        this.settings = settings;
    }

    public void print(IMindMap sourceMap) {
        if (!start()) {
            if (Util.isMac()) {
                MindMapUIPlugin.log(null,
                        "[print] printer start page failed..."); //$NON-NLS-1$
            }
            return;
        }
        printMap(sourceMap);
    }

    protected void printMap(IMindMap sourceMap) {
        if (shell == null) {
            shell = new GhostShellProvider(parentShell.getDisplay());
        }
        Properties properties = new Properties();
        properties.set(IMindMapViewer.VIEWER_MARGIN,
                Math.max(pageBounds.width, pageBounds.height));
        int margin = VIEWER_MARGIN * dpi.x / UnitConvertor.getScreenDpi().x;
//        properties.set(IMindMapViewer.VIEWER_MARGIN, margin);
        properties.set(IMindMapViewer.VIEWER_GRADIENT, Boolean.FALSE);

        boolean plusVisible = getBoolean(settings, PrintConstants.PLUS_VISIBLE,
                PrintConstants.DEFAULT_PLUS_VISIBLE);
        boolean minusVisible = getBoolean(settings,
                PrintConstants.MINUS_VISIBLE,
                PrintConstants.DEFAULT_MINUS_VISIBLE);
        properties.set(IMindMapViewer.PLUS_VISIBLE, plusVisible);
        properties.set(IMindMapViewer.MINUS_VISIBLE, minusVisible);

        IGraphicalViewer exportViewer = new MindMapExportViewer(shell,
                sourceMap, properties);
        this.source = new MindMapViewerPrintSourceProvider(exportViewer,
                margin);
        render();
    }

    private boolean getBoolean(IDialogSettings settings, String key,
            boolean defaultValue) {
        boolean value = defaultValue;
        if (settings.get(key) != null) {
            value = settings.getBoolean(key);
        }

        return value;
    }

    public void print(IGraphicalViewer sourceViewer) {
        if (!start()) {
            if (Util.isMac()) {
                MindMapUIPlugin.log(null,
                        "[print] printer start page failed..."); //$NON-NLS-1$
            }
            return;
        }

//        IMindMap map = (IMindMap) sourceViewer.getAdapter(IMindMap.class);
//        if (map == null
//                || map.getCentralTopic() == map.getSheet().getRootTopic()) {
        int margin = VIEWER_MARGIN * dpi.x / UnitConvertor.getScreenDpi().x;
        this.source = new MindMapViewerPrintSourceProvider(sourceViewer,
                margin);
        render();
//        } else {
//            printMap(new MindMap(map.getSheet()));
//        }
    }

    private boolean start() {
        if (printer == null) {
            printer = new Printer(printerData);
        }

        receivePrinterInfo();

        if (!jobStarted) {
            if (!printer.startJob(jobName))
                return false;
            jobStarted = true;
        }
        return printer.startPage();
    }

    /**
     * 
     */
    private void render() {
        this.area = ImageExportUtils.createExportAreaProvider(
                source.getSourceArea(), ResizeConstants.RESIZE_STRETCH,
                needRotate ? pageClientArea.height : pageClientArea.width,
                needRotate ? pageClientArea.width : pageClientArea.height,
                source.getMargins());
        init(source, area);
        GC gc = new GC(printer);
        try {
            render(gc);
        } finally {
            gc.dispose();
        }

        printer.endPage();
    }

    /*
     * (non-Javadoc)
     * @see
     * org.xmind.gef.image.FigureRenderer#render(org.eclipse.swt.graphics.GC)
     */
    @Override
    public void render(GC gc) {
        //draw source content
        gc.setClipping(pageClientArea.x, pageClientArea.y, pageClientArea.width,
                pageClientArea.height);
        pushState(gc);
        drawSourceContent(gc);
        popState(gc);

        //draw border
        gc.setClipping(pageClientArea.x, pageClientArea.y, pageClientArea.width,
                pageClientArea.height);
        if (settings.getBoolean(PrintConstants.BORDER)) {
            drawBorder(gc);
        }

        //draw header/footer
        org.eclipse.swt.graphics.Rectangle pageBounds = printer.getClientArea();
        gc.setClipping(pageBounds.x, pageBounds.y, pageBounds.width,
                pageBounds.height);
        String headerText = settings.get(PrintConstants.HEADER_TEXT);
        if (headerText != null && !"".equals(headerText)) { //$NON-NLS-1$
            drawHeader(gc, headerText);
        }

        String footerText = settings.get(PrintConstants.FOOTER_TEXT);
        if (footerText != null && !"".equals(footerText)) { //$NON-NLS-1$
            drawFooter(gc, footerText);
        }
    }

    public void drawSourceContent(GC gc) {
        super.render(gc);
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

    /*
     * (non-Javadoc)
     * @see
     * org.xmind.gef.image.FigureRenderer#createGraphics(org.eclipse.draw2d.
     * Graphics, java.util.Stack)
     */
    @Override
    protected void createGraphics(Graphics graphics, Stack<Graphics> stack) {
        Rectangle bounds = getBounds();
        graphics.translate(
                pageClientArea.x
                        - (needRotate ? (-bounds.y - bounds.height) : bounds.x),
                pageClientArea.y - (needRotate ? bounds.x : bounds.y));

        if (getScale() > 0) {
            if (ScaledGraphics.SCALED_GRAPHICS_ENABLED) {
                ScaledGraphics scaledGraphics = new ScaledGraphics(graphics);
                scaledGraphics.scale(getScale());
                stack.push(scaledGraphics);
                graphics = scaledGraphics;
            } else {
                graphics.scale(getScale());
            }
        }

        if (needRotate) {
            Rotate90Graphics rotatedGraphics = new Rotate90Graphics(graphics);
            stack.push(rotatedGraphics);
            graphics = rotatedGraphics;
        }
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

    private int getAlign(String alignKey, int defaultAlign) {
        return PrintConstants.toDraw2DAlignment(settings.get(alignKey),
                defaultAlign);
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

    private void drawText(GC gc, String text, Font font, int alignment,
            boolean top) {
        RotatableWrapLabel label = new RotatableWrapLabel();
        label.setText(text);
        label.setFont(font);
        label.setTextAlignment(alignment);
        label.setForegroundColor(
                parentShell.getDisplay().getSystemColor(SWT.COLOR_BLACK));

        Rectangle pageBounds = new Rectangle(printer.getClientArea());
        int width = needRotate ? pageClientArea.height : pageClientArea.width;
        int marginWidth = TEXT_MARGIN * dpi.x / UnitConvertor.getScreenDpi().x;
        int marginHeight = TEXT_MARGIN * dpi.y / UnitConvertor.getScreenDpi().y;
        if (needRotate) {
            int temp = marginWidth;
            marginWidth = marginHeight;
            marginHeight = temp;
        }
        width -= marginWidth * 2;

        Dimension size = label.getPreferredSize(width, -1);
        int x = -width / 2;

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
        if (needRotate) {
            rotatedGraphics = new Rotate90Graphics(graphics);
            graphics = rotatedGraphics;
        }
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

    private void drawBorder(GC gc) {
        gc.setForeground(
                parentShell.getDisplay().getSystemColor(SWT.COLOR_BLACK));
        gc.setLineWidth(1);
        gc.setLineStyle(SWT.LINE_SOLID);
        gc.drawRectangle(pageClientArea.x, pageClientArea.y,
                pageClientArea.width - 1, pageClientArea.height - 1);
    }

//    private void drawSourceContent(GC gc, IMindMap source) {
//        MindMapImageExporter exporter = new MindMapImageExporter(
//                parentShell.getDisplay());
//        exporter.setSource(source, properties, null);
//
//        MindMapExportContentProvider provider = new MindMapExportContentProvider(
//                parentShell, source);
//        if (providers == null) {
//            providers = new ArrayList<MindMapExportContentProvider>();
//        }
//        providers.add(provider);
//        provider.setProperty(IMindMapViewer.VIEWER_MARGIN,
//                Math.max(pageBounds.width, pageBounds.height));
//        provider.setMargin(VIEWER_MARGIN * dpi.x
//                / UnitConvertor.getScreenDpi().x);
//        provider.setProperty(IMindMapViewer.VIEWER_GRADIENT, Boolean.FALSE);
//        provider.setResizeStrategy(ResizeConstants.RESIZE_STRETCH,
//                needRotate ? pageClientArea.height : pageClientArea.width,
//                needRotate ? pageClientArea.width : pageClientArea.height);
//        try {
//            Rectangle exportArea = provider.getExportArea();
//            double scale = provider.getScale();
//            IFigure contents = provider.getContents();
//            if (settings.getBoolean(PrintConstants.NO_BACKGROUND)) {
//                Layer layer = provider.getViewer().getLayer(
//                        GEF.LAYER_BACKGROUND);
//                if (layer != null) {
//                    layer.setOpaque(false);
//                }
//            }
//
//            SWTGraphics baseGraphcis = new SWTGraphics(gc);
//            baseGraphcis.translate(pageClientArea.x
//                    - (needRotate ? (-exportArea.y - exportArea.height)
//                            : exportArea.x), pageClientArea.y
//                    - (needRotate ? exportArea.x : exportArea.y));
//            Graphics graphics = baseGraphcis;
//            ScaledGraphics scaledGraphics = null;
//            Rotate90Graphics rotatedGraphics = null;
//            if (scale > 0) {
//                scaledGraphics = new ScaledGraphics(graphics);
//                scaledGraphics.scale(scale);
//                graphics = scaledGraphics;
//            }
//            if (needRotate) {
//                rotatedGraphics = new Rotate90Graphics(graphics);
//                graphics = rotatedGraphics;
//            }
//            try {
//                contents.paint(graphics);
//            } catch (Throwable e) {
//                Logger.log(e, "Error occurred while painting mind map: " //$NON-NLS-1$
//                        + source.getCentralTopic().getTitleText());
//            } finally {
//                if (rotatedGraphics != null) {
//                    rotatedGraphics.dispose();
//                }
//                if (scaledGraphics != null) {
//                    scaledGraphics.dispose();
//                }
//                baseGraphcis.dispose();
//            }
//        } catch (Throwable e) {
//            Logger.log(e, "Error occurred while painting mind map: " //$NON-NLS-1$
//                    + source.getCentralTopic().getTitleText());
//        }
//    }

    private void receivePrinterInfo() {
        dpi = new Point(printer.getDPI());
        pageBounds = new Rectangle(printer.getBounds());
//        Rectangle trim = new Rectangle(printer.computeTrim(0, 0, 0, 0));
//        pageMargins = new Insets(-trim.y, -trim.x, trim.right(), trim.bottom());

        pageClientArea = new Rectangle(printer.getClientArea());
//        pageClientArea.x = pageBounds.x
//                + (pageBounds.width - pageClientArea.width) / 2;
//        pageClientArea.y = pageBounds.y
//                + (pageBounds.height - pageClientArea.height) / 2;
        int leftMargin = getUserMargin(PrintConstants.LEFT_MARGIN);
        int rightMargin = getUserMargin(PrintConstants.RIGHT_MARGIN);
        int topMargin = getUserMargin(PrintConstants.TOP_MARGIN);
        int bottomMargin = getUserMargin(PrintConstants.BOTTOM_MARGIN);
//        pageClientArea.expand(pageMargins);
        pageClientArea.x += leftMargin;
        pageClientArea.y += topMargin;
        pageClientArea.width -= leftMargin + rightMargin;
        pageClientArea.height -= topMargin + bottomMargin;

//        needRotate = pageBounds.height > pageBounds.width;
        needRotate = false;

        int headerHeight = PrintUtils.getHeaderHeight(settings, dpi.y);
        int footerHeight = PrintUtils.getBottomHeight(settings, dpi.y);

        pageClientArea.expand(new Insets(-headerHeight, 0, -footerHeight, 0));
    }

    private int getUserMargin(String key) {
        double margin;
        try {
            margin = settings.getDouble(key);
        } catch (NumberFormatException e) {
            margin = PrintConstants.DEFAULT_MARGIN;
        }
        double dpi;
        if (PrintConstants.LEFT_MARGIN.equals(key)
                || PrintConstants.RIGHT_MARGIN.equals(key)) {
            dpi = this.dpi.x;
        } else {
            dpi = this.dpi.y;
        }
        return (int) (margin * dpi);
    }

    public void dispose() {
        if (shell != null) {
            shell.dispose();
            shell = null;
        }
        if (printer != null) {
            if (!printer.isDisposed()) {
                printer.endJob();
            }
            printer.dispose();
            printer = null;
        }
        if (transform != null) {
            transform.dispose();
            transform = null;
        }
        jobStarted = false;

//        if (providers != null) {
//            for (Object o : providers.toArray()) {
//                ((MindMapExportContentProvider) o).dispose();
//            }
//            providers = null;
//        }
    }

}
