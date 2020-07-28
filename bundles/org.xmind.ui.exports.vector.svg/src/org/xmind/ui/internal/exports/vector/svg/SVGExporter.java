/* ******************************************************************************
 * Copyright (c) 2006-2013 XMind Ltd. and others.
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
package org.xmind.ui.internal.exports.vector.svg;

import static org.xmind.gef.IGraphicalViewer.VIEWER_RENDER_TEXT_AS_PATH;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.de.erichseifert.vectorgraphics2d.SVGGraphics2D;
import org.xmind.gef.GEF;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.util.Properties;
import org.xmind.ui.exports.vector.graphics.GraphicsToGraphics2DAdaptor;
import org.xmind.ui.internal.figures.SheetFigure;
import org.xmind.ui.mindmap.GhostShellProvider;
import org.xmind.ui.mindmap.IMindMap;
import org.xmind.ui.mindmap.IMindMapViewer;
import org.xmind.ui.mindmap.MindMapExportViewer;
import org.xmind.ui.viewers.ICompositeProvider;
import org.xmind.ui.wizards.ExportContants;
import org.xmind.ui.wizards.Exporter;
import org.xmind.ui.wizards.IExportPart;

/**
 * @author Jason Wong
 */
public class SVGExporter extends Exporter {

    private static final int DEFAULT_MARGIN = 15;

    private ISheet sheet;

    private String targetPath;

    private IGraphicalViewer viewer;

    private IGraphicalViewer exportViewer;

    private ICompositeProvider compositeProvider;

    private Rectangle bounds;

    private SVGGraphics2D svgGenerator;

    private GraphicsToGraphics2DAdaptor adaptor;

    private IDialogSettings settings;

    public SVGExporter(ISheet sheet, ITopic centralTopic, String targetPath,
            IGraphicalViewer viewer, IDialogSettings settings) {
        super(sheet, centralTopic);
        this.sheet = sheet;
        this.targetPath = targetPath;
        this.viewer = viewer;
        this.settings = settings;
    }

    public void init() {
        bounds = getFigureBounds();
        svgGenerator = new SVGGraphics2D(0, 0, bounds.width, bounds.height);
    }

    private Rectangle getFigureBounds() {
        Rectangle extent = getSheetFigure().getFreeformExtent();
        return new Rectangle(0, 0, extent.width + DEFAULT_MARGIN * 2,
                extent.height + DEFAULT_MARGIN * 2);
    }

    @Override
    public void start(final Display display, Shell shell)
            throws InvocationTargetException {
        super.start(display, shell);

        adaptor = new GraphicsToGraphics2DAdaptor(svgGenerator, bounds,
                display);
        setTranslate(adaptor);
        compositeProvider = new GhostShellProvider(display);

        display.syncExec(new Runnable() {

            public void run() {
                exportViewer = new MindMapExportViewer(compositeProvider,
                        viewer.getAdapter(IMindMap.class),
                        viewer.getProperties());
                Properties properties = exportViewer.getProperties();
                initProperties(properties);

                exportViewer.getCanvas().getLightweightSystem()
                        .getUpdateManager().performValidation();
                exportViewer.getLayer(GEF.LAYER_BACKGROUND).paint(adaptor);
                exportViewer.getLayer(GEF.LAYER_CONTENTS).paint(adaptor);
            }
        });
    }

    protected void initProperties(Properties properties) {
        //set plus minus visibility
        boolean plusVisible = getBoolean(settings, ExportContants.PLUS_VISIBLE,
                ExportContants.DEFAULT_PLUS_VISIBLE);
        boolean minusVisible = getBoolean(settings,
                ExportContants.MINUS_VISIBLE,
                ExportContants.DEFAULT_MINUS_VISIBLE);
        properties.set(IMindMapViewer.PLUS_VISIBLE, plusVisible);
        properties.set(IMindMapViewer.MINUS_VISIBLE, minusVisible);

        if (Platform.OS_LINUX.equals(Platform.getOS())) {
            properties.set(VIEWER_RENDER_TEXT_AS_PATH, false);
        } else {
            properties.set(VIEWER_RENDER_TEXT_AS_PATH, true);
        }
    }

    private boolean getBoolean(IDialogSettings settings, String key,
            boolean defaultValue) {
        boolean value = defaultValue;
        if (settings.get(key) != null) {
            value = settings.getBoolean(key);
        }

        return value;
    }

    @Override
    protected void write(IProgressMonitor monitor, IExportPart part)
            throws InvocationTargetException, InterruptedException {
    }

    @Override
    public void end() throws InvocationTargetException {
        try {
            Writer out = new OutputStreamWriter(
                    new FileOutputStream(targetPath), "UTF-8"); //$NON-NLS-1$
            out.write(svgGenerator.toString());
            out.close();
            adaptor.dispose();
            cleanUpSources();
        } catch (Exception e) {
            throw new InvocationTargetException(e);
        }
    }

    private void cleanUpSources() {
        if (exportViewer != null) {
            if (exportViewer.getControl() != null) {
                getDisplay().syncExec(new Runnable() {
                    public void run() {
                        exportViewer.getControl().dispose();
                    }
                });
            }
            exportViewer = null;
        }
        if (compositeProvider instanceof GhostShellProvider) {
            getDisplay().syncExec(new Runnable() {
                public void run() {
                    ((GhostShellProvider) compositeProvider).dispose();
                }
            });
            compositeProvider = null;
        }
    }

    @Override
    public boolean canStart() {
        return true;
    }

    private SheetFigure getSheetFigure() {
        return (SheetFigure) viewer.findGraphicalPart(sheet).getContentPane();
    }

    private void setTranslate(GraphicsToGraphics2DAdaptor graphicsAdaptor) {
        Rectangle extent = getSheetFigure().getFreeformExtent();
        int translateX = DEFAULT_MARGIN - extent.x;
        int translateY = DEFAULT_MARGIN - extent.y;

        graphicsAdaptor.translate(translateX, translateY);
    }

}
