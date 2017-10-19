/* ******************************************************************************
 * Copyright (c) 2006-2016 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
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
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.internal.util.BundleUtility;
import org.xmind.core.ISheet;
import org.xmind.core.util.FileUtils;
import org.xmind.gef.image.ResizeConstants;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.IWorkbookRef;
import org.xmind.ui.mindmap.MindMap;
import org.xmind.ui.mindmap.MindMapImageExporter;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.prefs.PrefConstants;

/**
 * @author Frank Shaka
 * @since 3.6.50
 */
public class DefaultMindMapPreviewGenerator
        implements IMindMapPreviewGenerator {

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

        final MindMapImageExporter exporter = new MindMapImageExporter(display);
        exporter.setSource(new MindMap(sheet), null,
                new Insets(MindMapUI.DEFAULT_EXPORT_MARGIN));
        exporter.setResize(ResizeConstants.RESIZE_MAXPIXELS, 1280, 1024);
        exporter.setTargetStream(output);

        final Exception[] error = new Exception[1];
        display.syncExec(new Runnable() {
            public void run() {
                try {
                    exporter.export();
                } catch (SWTException e) {
                    error[0] = e;
                }
            }
        });
        if (error[0] != null)
            throw new IOException(error[0]);

        return properties;
    }

}
