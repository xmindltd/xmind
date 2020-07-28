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

package org.xmind.ui.mindmap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Layer;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.xmind.core.IFileEntry;
import org.xmind.core.IManifest;
import org.xmind.core.IWorkbook;
import org.xmind.core.util.FileUtils;
import org.xmind.gef.GEF;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.image.FigureRenderer;
import org.xmind.gef.image.IExportAreaProvider;
import org.xmind.gef.image.ImageExportUtils;
import org.xmind.gef.image.ResizeConstants;
import org.xmind.gef.util.Properties;
import org.xmind.ui.internal.layers.BackgroundLayer;
import org.xmind.ui.resources.ColorUtils;
import org.xmind.ui.util.ImageFormat;
import org.xmind.ui.viewers.ICompositeProvider;

/**
 * @author Frank Shaka
 */
public class MindMapImageExporter {

    private Display display = null;

    /* ==== What to Export? ==== */
    private MindMapViewerExportSourceProvider sourceProvider = null;
    private Object input = null;
    private ICompositeProvider parent = null;
    private Insets margins = null;
    private Properties properties = null;

    /* ==== How to Export? ==== */
    private int resizeStrategy = ResizeConstants.RESIZE_NONE;
    private int wHint = -1;
    private int hHint = -1;
    private ImageFormat format = ImageFormat.PNG;

    /* ==== Where to Export to? ==== */
    private OutputStream targetStream = null;
    private File targetFile = null;
    private IWorkbook targetWorkbook = null;
    private String targetEntryPath = null;
    private boolean ignoreEntryEncryption = true;

    /* ==== What to Clean Out After Exporting? ==== */
    private GhostShellProvider ghostShellProvider = null;
    private IGraphicalViewer exportViewer = null;
    private OutputStream streamToClose = null;
    private IFileEntry fileEntry = null;

    private IExportAreaProvider area = null;

    /**
     *
     */
    public MindMapImageExporter(Display display) {
        this.display = display;
    }

    public void setResize(int strategy, int wHint, int hHint) {
        this.resizeStrategy = strategy;
        this.wHint = wHint;
        this.hHint = hHint;
    }

    public void setImageFormat(ImageFormat format) {
        this.format = format;
    }

    public void setSourceProvider(
            MindMapViewerExportSourceProvider sourceProvider) {
        setSources(sourceProvider, null, null, null, null);
    }

    public void setSourceViewer(IGraphicalViewer viewer) {
        setSources(
                new MindMapViewerExportSourceProvider(viewer,
                        MindMapUI.DEFAULT_EXPORT_MARGIN),
                null, null, null, null);
    }

    public void setSourceViewer(IGraphicalViewer viewer, Insets margins) {
        setSources(new MindMapViewerExportSourceProvider(viewer, margins), null,
                null, null, null);
    }

    public void setSourceViewer(IGraphicalViewer viewer,
            ICompositeProvider parent, Properties properties, Insets margins) {
        // Check if viewer has been drilled down:
        IMindMap map = (IMindMap) viewer.getAdapter(IMindMap.class);
        if (map == null
                || map.getCentralTopic() == map.getSheet().getRootTopic()) {
            setSourceViewer(viewer, margins);
        } else {
            setSource(new MindMap(map.getSheet()), parent, properties, margins);
        }
    }

    public void setSource(Object input, Properties properties, Insets margins) {
        setSources(null, input, null, properties, margins);
    }

    public void setSource(Object input, ICompositeProvider parent,
            Properties properties, Insets margins) {
        setSources(null, input, parent, properties, margins);
    }

    protected void setSources(MindMapViewerExportSourceProvider sourceProvider,
            Object input, ICompositeProvider parent, Properties properties,
            Insets margins) {
        this.sourceProvider = sourceProvider;
        this.input = input;
        this.parent = parent;
        this.properties = properties;
        this.margins = margins;
        cleanUpSources();
    }

    public void setTargetStream(OutputStream stream) {
        setTargets(stream, null, null, null, true);
    }

    public void setTargetFile(File file) {
        setTargets(null, file, null, null, true);
    }

    public void setTargetFileEntry(IWorkbook workbook, String entryPath) {
        setTargets(null, null, workbook, entryPath, true);
    }

    public void setTargetWorkbook(IWorkbook workbook) {
        setTargets(null, null, workbook, null, true);
    }

    public void setTargetFileEntry(IWorkbook workbook, String entryPath,
            boolean ignoreEncryption) {
        setTargets(null, null, workbook, entryPath, ignoreEncryption);
    }

    public void setTargetFileEntry(IWorkbook workbook,
            boolean ignoreEncryption) {
        setTargets(null, null, workbook, null, ignoreEncryption);
    }

    protected void setTargets(OutputStream stream, File file,
            IWorkbook workbook, String entryPath, boolean ignoreEncryption) {
        this.targetStream = stream;
        this.targetFile = file;
        this.targetWorkbook = workbook;
        this.targetEntryPath = entryPath;
        this.ignoreEntryEncryption = ignoreEncryption;
        cleanUpTargets();
    }

    /**
     * @return the sourceProvider
     */
    public MindMapViewerExportSourceProvider getSourceProvider() {
        return sourceProvider;
    }

    public Point calcRelativeOrigin() {
        if (sourceProvider == null || area == null)
            return null;
        Point origin = sourceProvider.getOriginPoint();
        Rectangle bounds = area.getExportArea();
        return bounds.getLocation().negate().translate(origin);
    }

    public void initRenderer(FigureRenderer renderer) {
        if (this.sourceProvider == null) {
            prepareSourceProvider();
        }
        if (this.area == null) {
            this.area = ImageExportUtils.createExportAreaProvider(
                    sourceProvider.getSourceArea(), resizeStrategy, wHint,
                    hHint, sourceProvider.getMargins());
        }
        renderer.init(sourceProvider, area);
    }

    public Image createImage() {
        prepareSourceProvider();
        try {
            final FigureRenderer renderer = new FigureRenderer();
            initRenderer(renderer);
            final Image[] image = new Image[1];
            final Throwable[] error = new Throwable[1];
            display.syncExec(new Runnable() {
                @Override
                public void run() {
                    try {
                        Rectangle bounds = renderer.getBounds();
                        Image img = new Image(display, bounds.width,
                                bounds.height);
                        GC gc = new GC(img);
                        if (resizeStrategy == ResizeConstants.RESIZE_STRETCH) {
                            //fill background color when stretching
                            for (IFigure figure : renderer.getFigures()) {
                                if (figure instanceof BackgroundLayer) {
                                    Rectangle figureBounds = figure.getBounds();
                                    int longerSideLength = (figureBounds.width > figureBounds.height)
                                            ? figureBounds.width
                                            : figureBounds.height;
                                    figure.setBounds(
                                            new Rectangle(-longerSideLength,
                                                    -longerSideLength,
                                                    longerSideLength * 2,
                                                    longerSideLength * 2));
                                }
                            }
                        }
                        try {
                            renderer.render(gc);
                        } finally {
                            gc.dispose();
                        }

                        image[0] = img;
                    } catch (RuntimeException e) {
                        error[0] = e;
                    } catch (Error e) {
                        error[0] = e;
                    }
                }
            });
            if (error[0] != null)
                throw new RuntimeException(error[0].getMessage(), error[0]);
            return image[0];
        } finally {
            cleanUpSources();
        }
    }

    public void export() {
        Image image = createImage();
        try {
            export(image);
        } finally {
            image.dispose();
        }
    }

    public void export(Image image) {
        prepareTargetStream();
        try {
            ImageExportUtils.saveImage(image, targetStream,
                    format.getSWTFormat());
            if (fileEntry != null) {
                fileEntry.decreaseReference();
                fileEntry.increaseReference();
                fileEntry = null;
            }
        } finally {
            cleanUpTargets();
        }
    }

    public void export(InputStream sourceStream) throws IOException {
        prepareTargetStream();
        try {
            FileUtils.transfer(sourceStream, targetStream, true);
            if (fileEntry != null) {
                fileEntry.decreaseReference();
                fileEntry.increaseReference();
                fileEntry = null;
            }
        } finally {
            cleanUpTargets();
        }
    }

    public void prepareSourceProvider() {
        cleanUpSources();
        if (input != null) {
            display.syncExec(new Runnable() {
                public void run() {
                    recreateSourceProviderFromInput();
                }
            });
        }
        if (sourceProvider == null)
            throw new IllegalArgumentException(
                    "No source to export image from"); //$NON-NLS-1$
    }

    private void recreateSourceProviderFromInput() {
        if (parent != null && parent.getParent() != null) {
            this.exportViewer = new MindMapExportViewer(parent, input,
                    properties);
        } else {
            this.ghostShellProvider = new GhostShellProvider(display);
            this.exportViewer = new MindMapExportViewer(ghostShellProvider,
                    input, properties);
        }
        if (margins != null) {
            this.sourceProvider = new MindMapViewerExportSourceProvider(
                    this.exportViewer, margins);
        } else {
            this.sourceProvider = new MindMapViewerExportSourceProvider(
                    this.exportViewer);
        }
    }

    /**
     *
     */
    public void prepareTargetStream() {
        cleanUpTargets();
        if (targetFile != null) {
            recreateTargetStreamFromFile();
        } else if (targetWorkbook != null) {
            recreateTargetStreamFromFileEntry();
        }
        if (targetStream == null)
            throw new IllegalArgumentException("No target to export image to"); //$NON-NLS-1$
    }

    /**
     *
     */
    private void recreateTargetStreamFromFileEntry() {
        IManifest manifest = targetWorkbook.getManifest();
        String entryPath = this.targetEntryPath;
        if (entryPath == null) {
            entryPath = toThumbnailArchivePath(format);
        }
        this.fileEntry = manifest.createFileEntry(entryPath,
                format.getMediaType());
        try {
            this.targetStream = this.fileEntry.openOutputStream();
        } catch (IOException e) {
        }
        this.streamToClose = this.targetStream;
    }

    /**
     *
     */
    private void recreateTargetStreamFromFile() {
        FileUtils.ensureFileParent(targetFile);
        try {
            this.targetStream = new FileOutputStream(targetFile);
            this.streamToClose = this.targetStream;
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    /**
     *
     */
    public void cleanUpSources() {
        if (exportViewer != null) {
            if (exportViewer.getControl() != null) {
                display.syncExec(new Runnable() {
                    public void run() {
                        exportViewer.getControl().dispose();
                    }
                });
            }
            exportViewer = null;
        }
        if (ghostShellProvider != null) {
            display.syncExec(new Runnable() {
                public void run() {
                    ghostShellProvider.dispose();
                }
            });
            ghostShellProvider = null;
        }
    }

    /**
     *
     */
    public void cleanUpTargets() {
        if (streamToClose != null) {
            try {
                streamToClose.close();
            } catch (IOException e) {
                //ignore
            }
            streamToClose = null;
        }
    }

    public Properties getProperties() {
        return properties;
    }

    /**
     * Must be called after export.
     *
     * @return
     */
    public String getBackgroundColor() {
        return getBackgroundColor(getSourceProvider().getViewer());
    }

    public static String toThumbnailArchivePath(ImageFormat format) {
        return "Thumbnails/thumbnail" + format.getExtensions().get(0); //$NON-NLS-1$
    }

    private static String getBackgroundColor(IGraphicalViewer sourceViewer) {
        if (sourceViewer != null) {
            Layer layer = sourceViewer.getLayer(GEF.LAYER_BACKGROUND);
            if (layer != null) {
                Color color = layer.getBackgroundColor();
                if (color != null)
                    return ColorUtils.toString(color);
            }
        }
        return "#ffffff"; //$NON-NLS-1$
    }

}
