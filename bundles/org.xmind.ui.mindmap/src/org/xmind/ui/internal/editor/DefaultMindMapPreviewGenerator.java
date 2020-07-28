/* ******************************************************************************
 * Copyright (c) 2006-2016 XMind Ltd. and others.
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
/**
 * 
 */
package org.xmind.ui.internal.editor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Properties;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.internal.util.BundleUtility;
import org.xmind.core.ISheet;
import org.xmind.core.util.FileUtils;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.image.ResizeConstants;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.mindmap.GhostShellProvider;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.IWorkbookRef;
import org.xmind.ui.mindmap.MindMap;
import org.xmind.ui.mindmap.MindMapExportViewer;
import org.xmind.ui.mindmap.MindMapImageExporter;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.mindmap.MindMapViewerExportSourceProvider;
import org.xmind.ui.prefs.PrefConstants;

/**
 * @author Frank Shaka
 * @since 3.6.50
 */
public class DefaultMindMapPreviewGenerator
        implements IMindMapPreviewGenerator {

    private static final int DEFAULT_EXPORT_MARGIN = 5;

    private static final int MINIMUM_PREVIEW_WIDTH = 420;
    private static final int MAXIMUM_PREVIEW_WIDTH = MINIMUM_PREVIEW_WIDTH * 4;

    private final Display display;

    /**
     * 
     */
    public DefaultMindMapPreviewGenerator(Display display) {
        this.display = display;
    }

    @Override
    public Properties generateMindMapPreview(final IWorkbookRef workbookRef,
            final ISheet sheet, final OutputStream output,
            final MindMapPreviewOptions options) throws IOException {
        Assert.isLegal(output != null);

        final Properties properties = new Properties();
        if (sheet == null || MindMapUIPlugin.getDefault().getPreferenceStore()
                .getBoolean(PrefConstants.PREVIEW_SKIPPED)) {
            URL url = BundleUtility.find(MindMapUI.PLUGIN_ID,
                    IMindMapImages.DEFAULT_THUMBNAIL);
            if (url != null) {
                InputStream input = url.openStream();
                try {
                    FileUtils.transfer(input, output, false);
                } finally {
                    input.close();
                }
            }
            return properties;
        }

        final Exception[] error = new Exception[1];
        display.syncExec(new Runnable() {
            public void run() {
                try {
                    generate(sheet, output);
                } catch (SWTException e) {
                    error[0] = e;
                }
            }
        });
        if (error[0] != null)
            throw new IOException(error[0]);

        return properties;
    }

    private void generate(ISheet sheet, OutputStream output) {
        GhostShellProvider ghostShellProvider = new GhostShellProvider(display);
        IGraphicalViewer viewer = new MindMapExportViewer(ghostShellProvider,
                new MindMap(sheet), null);
        MindMapViewerExportSourceProvider sourceProvider = new MindMapViewerExportSourceProvider(
                viewer, DEFAULT_EXPORT_MARGIN);

        org.eclipse.draw2d.geometry.Rectangle sourceArea = sourceProvider
                .getSourceArea();

        int resizeWidth = Math.max(
                (sourceArea.width % 21 == 0) ? sourceArea.width
                        : (sourceArea.width + 21 - sourceArea.width % 21),
                (sourceArea.height % 13 == 0) ? sourceArea.height * 21 / 13
                        : (sourceArea.height + 13 - sourceArea.height % 13) * 21
                                / 13);
        if (resizeWidth < MINIMUM_PREVIEW_WIDTH) {
            resizeWidth = MINIMUM_PREVIEW_WIDTH;
        } else if (resizeWidth > MAXIMUM_PREVIEW_WIDTH) {
            resizeWidth = MAXIMUM_PREVIEW_WIDTH;
        }
        int resizeHeight = resizeWidth * 13 / 21;

        MindMapImageExporter exporter = new MindMapImageExporter(display);
        exporter.setSourceProvider(sourceProvider);
        exporter.setResize(ResizeConstants.RESIZE_STRETCH, resizeWidth,
                resizeHeight);
        exporter.setTargetStream(output);

        exporter.export();
    }

}
