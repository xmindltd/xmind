package org.xmind.ui.exports.vector.svg;

import static org.xmind.gef.IGraphicalViewer.VIEWER_RENDER_TEXT_AS_PATH;

import java.io.File;
import java.io.InterruptedIOException;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.xmind.core.Core;
import org.xmind.gef.util.Properties;
import org.xmind.ui.internal.exports.vector.svg.SVGExporter;
import org.xmind.ui.mindmap.IMindMap;
import org.xmind.ui.mindmap.IMindMapViewer;
import org.xmind.ui.util.Logger;
import org.xmind.ui.wizards.IExporter;

public class SVGGenerator {

    private IMindMap mindmap;

    private IMindMapViewer sourceViewer;

    private Display display;

    private Shell parentShell;

    private boolean renderAsPath;

    private boolean plusVisible;

    private boolean minusVisible;

    public SVGGenerator(IMindMap mindmap, IMindMapViewer sourceViewer,
            Display display, Shell parentShell, boolean renderAsPath,
            boolean plusVisible, boolean minusVisible) {
        this.mindmap = mindmap;
        this.sourceViewer = sourceViewer;
        this.display = display;
        this.parentShell = parentShell;
        this.renderAsPath = renderAsPath;
        this.plusVisible = plusVisible;
        this.minusVisible = minusVisible;
    }

    //This method may have a long delay, so as best invoke it in a independent thread.
    public File generate() {
        if (doGenerate()) {
            return new File(getTargetPath());
        } else {
            return null;
        }
    }

    private boolean doGenerate() {
        try {
            try {
                doGenerate(display, parentShell);
            } catch (OutOfMemoryError e) {
                try {
                    throw new Exception("Image is too large.", e); //$NON-NLS-1$
                } catch (Exception e2) {
                    throw new InvocationTargetException(e2);
                }
            }
            return true;
        } catch (Throwable e) {
            if (e instanceof InterruptedException
                    || e instanceof InterruptedIOException) {
                return false;
            }
            while (e instanceof InvocationTargetException) {
                Throwable t = ((InvocationTargetException) e).getCause();
                if (t == null)
                    break;
                e = t;
            }
            final Throwable ex = e;
            display.asyncExec(new Runnable() {

                public void run() {
                    handleExportException(ex);
                }

            });
        }
        return false;
    }

    private void doGenerate(Display display, Shell parentShell)
            throws InvocationTargetException, InterruptedException {
        IExporter exporter = createExporter();
        if (!exporter.canStart())
            throw new InterruptedException();

        exporter.start(display, parentShell);
        exporter.end();
    }

    private IExporter createExporter() {
        SVGExporter exporter = new SVGExporter(mindmap.getSheet(),
                mindmap.getCentralTopic(), getTargetPath(), sourceViewer,
                null) {

            @Override
            protected void initProperties(Properties properties) {
                properties.set(IMindMapViewer.PLUS_VISIBLE, plusVisible);
                properties.set(IMindMapViewer.MINUS_VISIBLE, minusVisible);
                properties.set(VIEWER_RENDER_TEXT_AS_PATH, renderAsPath);
            }
        };

        exporter.init();
        return exporter;
    }

    private String getTargetPath() {
        return Core.getWorkspace()
                .getTempFile("svg/" + UUID.randomUUID().toString() + ".svg"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private void handleExportException(Throwable e) {
        Logger.log(e,
                NLS.bind("Error occurred when generating {0} file.", "SVG")); //$NON-NLS-1$//$NON-NLS-2$
    }

}
