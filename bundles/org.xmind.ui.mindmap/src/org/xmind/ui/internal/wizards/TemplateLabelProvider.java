package org.xmind.ui.internal.wizards;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.core.runtime.jobs.IJobFunction;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.xmind.core.IWorkbook;
import org.xmind.core.util.FileUtils;
import org.xmind.gef.image.ResizeConstants;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.mindmap.ITemplate;
import org.xmind.ui.mindmap.IWorkbookRef;
import org.xmind.ui.mindmap.MindMap;
import org.xmind.ui.mindmap.MindMapImageExporter;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.ImageFormat;

public class TemplateLabelProvider extends LabelProvider
        implements ISchedulingRule {

    private static final String CACHES_TEMPLATES_DIR = "caches/templates/"; //$NON-NLS-1$

    private static ImageFormat defaultFormat = ImageFormat.PNG;

    private Map<ITemplate, TemplateThumbnailImageLoader> imageLoaders = new HashMap<ITemplate, TemplateThumbnailImageLoader>();

    private Properties cachedImagePathMap = null;

    private static class TemplateThumbnailImageLoader {

        private final TemplateLabelProvider owner;

        private final ITemplate template;

        private final Display display;

        private Image image;

        private boolean disposed;

        public TemplateThumbnailImageLoader(TemplateLabelProvider owner,
                ITemplate template, Display display) {
            this.owner = owner;
            this.template = template;
            this.display = display;
            this.image = null;
            this.disposed = false;

            Job initJob = Job.create(NLS.bind(
                    MindMapMessages.TemplateLabelProvider_loadThumbnail_jobName,
                    template.getName()), new IJobFunction() {
                        @Override
                        public IStatus run(IProgressMonitor monitor) {
                            return doLoad(monitor);
                        }
                    });
            initJob.setSystem(true);
            initJob.setRule(owner);
            MindMapUIPlugin.getDefault().registerJob(initJob);
            initJob.schedule();
        }

        private IStatus doLoad(IProgressMonitor monitor) {
            final URI sourceWorkbookURI = template.getSourceWorkbookURI();
            final Properties imagePathMap = owner.getImagePathMap();
            String cachedImagePath = imagePathMap
                    .getProperty(sourceWorkbookURI.toString());
            File cachedImageFile;
            if (cachedImagePath != null) {
                cachedImageFile = new File(getCacheDirForTemplates(),
                        cachedImagePath);
                if (cachedImageFile.isFile() && cachedImageFile.canRead()) {
                    return loadImage(cachedImageFile);
                }
            }

            cachedImagePath = UUID.randomUUID().toString()
                    + defaultFormat.getExtensions().get(0);
            cachedImageFile = new File(getCacheDirForTemplates(),
                    cachedImagePath);

            if (URIUtil.isFileURI(sourceWorkbookURI)) {
                // try loading thumbnail image from source workbook in local file system
                File thumbnailFile = extractThumbnailImageFromLocalFile(
                        URIUtil.toFile(sourceWorkbookURI), cachedImageFile);
                if (thumbnailFile != null
                        && !thumbnailFile.equals(cachedImageFile)) {
                    // load image directly from the returned file,
                    // and do not save this file to cache
                    return loadImage(thumbnailFile);
                }
            }

            if (cachedImageFile.isFile() && cachedImageFile.canRead()) {
                imagePathMap.put(sourceWorkbookURI.toString(), cachedImagePath);
                owner.saveImagePathMap();
                return loadImage(cachedImageFile);
            }

            if (isDisposed() || monitor.isCanceled())
                return Status.CANCEL_STATUS;

            // generate thumbnail image now
            final String targetImagePath = cachedImagePath;
            final File targetImageFile = cachedImageFile;
            final IWorkbookRef sourceWorkbookRef = MindMapUIPlugin.getDefault()
                    .getWorkbookRefFactory()
                    .createWorkbookRef(sourceWorkbookURI, null);
            if (sourceWorkbookRef == null)
                return new Status(IStatus.ERROR, MindMapUIPlugin.PLUGIN_ID,
                        "Failed to obtain source workbook ref from: " //$NON-NLS-1$
                                + sourceWorkbookURI.toString());

            SubMonitor subMonitor = SubMonitor.convert(monitor, 100);

            try {
                sourceWorkbookRef.open(subMonitor.newChild(30));
            } catch (InterruptedException e) {
                throw new OperationCanceledException();
            } catch (InvocationTargetException e) {
                Throwable cause = e.getTargetException();
                if (cause == null)
                    cause = e;
                if (cause instanceof org.eclipse.core.runtime.CoreException)
                    return ((org.eclipse.core.runtime.CoreException) cause)
                            .getStatus();
                return new Status(IStatus.ERROR, MindMapUIPlugin.PLUGIN_ID,
                        e.getMessage(), e);
            }
            try {
                if (isDisposed() || monitor.isCanceled())
                    throw new OperationCanceledException();

                IWorkbook workbook = sourceWorkbookRef.getWorkbook();
                Assert.isTrue(workbook != null);

                try {
                    InputStream previewImageData = sourceWorkbookRef
                            .getPreviewImageData(
                                    workbook.getPrimarySheet().getId(), null);
                    if (previewImageData != null) {
                        FileUtils.ensureFileParent(targetImageFile);
                        OutputStream output = new FileOutputStream(
                                targetImageFile);
                        try {
                            FileUtils.transfer(previewImageData, output);
                        } finally {
                            output.close();
                        }
                        imagePathMap.put(sourceWorkbookURI.toString(),
                                cachedImagePath);
                        owner.saveImagePathMap();
                        return loadImage(targetImageFile);
                    }
                } catch (IOException e) {
                    /// ignore errors here
                }

                return doGenerate(subMonitor.newChild(65),
                        sourceWorkbookRef.getWorkbook(), targetImageFile,
                        targetImagePath, sourceWorkbookURI, imagePathMap);
            } finally {
                try {
                    sourceWorkbookRef.close(subMonitor.newChild(5));
                } catch (InvocationTargetException e) {
                    // ignore errors here
                } catch (InterruptedException e) {
                    // ignore cancellation here
                }
            }
        }

        private File extractThumbnailImageFromLocalFile(File sourceWorkbookFile,
                File cachedImageFile) {
            String thumbnailArchivePath = MindMapImageExporter
                    .toThumbnailArchivePath(defaultFormat);
            if (sourceWorkbookFile.isDirectory()) {
                File sourceThumbnailFile = new File(sourceWorkbookFile,
                        thumbnailArchivePath);
                if (sourceThumbnailFile.isFile()
                        && sourceThumbnailFile.canRead())
                    // use the source thumbnail image directly
                    return sourceThumbnailFile;

                // source workbook does not contain thumbnail image,
                // generate thumbnail image later
                return null;
            }

            if (!sourceWorkbookFile.isFile() || !sourceWorkbookFile.canRead()) {
                // source workbook is not accessible,
                // generate thumbnail image later
                return null;
            }

            try {
                ZipInputStream sourceInput = new ZipInputStream(
                        new FileInputStream(sourceWorkbookFile));
                try {
                    // find thumbnail image zip entry
                    ZipEntry entry;
                    do {
                        entry = sourceInput.getNextEntry();
                    } while (entry != null
                            && !thumbnailArchivePath.equals(entry.getName()));
                    if (entry == null) {
                        // no thumbnail image entry is found,
                        // generate thumbnail image later
                        return null;
                    }

                    // copy the thumbnail image data to cached folder
                    FileUtils.ensureFileParent(cachedImageFile);
                    OutputStream imageOutput = new FileOutputStream(
                            cachedImageFile);
                    try {
                        byte[] buffer = new byte[2048];
                        int numInBuffer;
                        while ((numInBuffer = sourceInput.read(buffer)) > 0) {
                            imageOutput.write(buffer, 0, numInBuffer);
                        }
                    } finally {
                        imageOutput.close();
                    }
                } finally {
                    sourceInput.close();
                }

                // use the cached thumbnail image file
                return cachedImageFile;

            } catch (Throwable e) {
                MindMapUIPlugin.getDefault().getLog().log(new Status(
                        IStatus.WARNING, MindMapUIPlugin.PLUGIN_ID,
                        "Failed to extract thumbnail image from source workbook file: " //$NON-NLS-1$
                                + sourceWorkbookFile,
                        e));
                // failed to extract thumbnail image,
                // generate thumbnail image later
                return null;
            }
        }

        private IStatus doGenerate(IProgressMonitor monitor,
                final IWorkbook workbook, final File targetImageFile,
                final String targetImagePath, final URI sourceWorkbookURI,
                final Properties imagePathMap) {
            if (isDisposed() || display.isDisposed())
                return Status.CANCEL_STATUS;

            MindMapImageExporter exporter = new MindMapImageExporter(display);
            exporter.setSource(new MindMap(workbook.getPrimarySheet()), null,
                    new Insets(MindMapUI.DEFAULT_EXPORT_MARGIN));
            exporter.setResize(ResizeConstants.RESIZE_MAXPIXELS, 800, 600);
            exporter.setTargetFile(targetImageFile);
            exporter.export();

            if (targetImageFile.isFile() && targetImageFile.canRead()) {
                imagePathMap.put(sourceWorkbookURI.toString(), targetImagePath);
                owner.saveImagePathMap();
                return loadImage(targetImageFile);
            }

            return new Status(IStatus.ERROR, MindMapUIPlugin.PLUGIN_ID,
                    "Failed to generate thumbnail image for template: " //$NON-NLS-1$
                            + sourceWorkbookURI.toString());
        }

        private IStatus loadImage(File imageFile) {
            if (isDisposed() || display.isDisposed())
                return Status.CANCEL_STATUS;

            final Image image = new Image(display, imageFile.getAbsolutePath());

            if (isDisposed() || display.isDisposed()) {
                image.dispose();
                return Status.CANCEL_STATUS;
            }

            display.asyncExec(new Runnable() {
                @Override
                public void run() {
                    if (isDisposed()) {
                        image.dispose();
                        return;
                    }

                    setImage(image);
                }
            });

            return Status.OK_STATUS;
        }

        private void setImage(Image image) {
            Assert.isTrue(Display.getCurrent() != null, "Not in UI thread"); //$NON-NLS-1$
            this.image = image;
            if (!isDisposed()) {
                owner.fireLabelProviderChanged(
                        new LabelProviderChangedEvent(owner, template));
            }
        }

        public void dispose() {
            Assert.isTrue(Display.getCurrent() != null, "Not in UI thread"); //$NON-NLS-1$
            this.disposed = true;
            if (this.image != null) {
                this.image.dispose();
                this.image = null;
            }
        }

        public Image getImage() {
            Assert.isTrue(Display.getCurrent() != null, "Not in UI thread"); //$NON-NLS-1$
            return image;
        }

        public boolean isDisposed() {
            return disposed;
        }

    }

    public TemplateLabelProvider() {
    }

    @Override
    public boolean contains(ISchedulingRule rule) {
        return rule instanceof TemplateLabelProvider;
    }

    @Override
    public boolean isConflicting(ISchedulingRule rule) {
        return rule instanceof TemplateLabelProvider;
    }

    @Override
    public Image getImage(Object element) {
        if (!(element instanceof ITemplate))
            return super.getImage(element);

        ITemplate template = (ITemplate) element;
        TemplateThumbnailImageLoader loader = imageLoaders.get(element);
        if (loader == null) {
            loader = new TemplateThumbnailImageLoader(this, template,
                    Display.getCurrent());
            imageLoaders.put(template, loader);
        }
        return loader.getImage();
    }

    @Override
    public void dispose() {
        super.dispose();

        Object[] loaderArray = imageLoaders.values().toArray();
        imageLoaders.clear();
        for (Object loader : loaderArray) {
            ((TemplateThumbnailImageLoader) loader).dispose();
        }
    }

    private Properties getImagePathMap() {
        if (cachedImagePathMap == null) {
            Properties map = new Properties();
            File mapFile = getImagePathMapFile();
            if (mapFile.isFile() && mapFile.canRead()) {
                try {
                    Reader reader = new BufferedReader(new FileReader(mapFile));
                    try {
                        map.load(reader);
                    } finally {
                        reader.close();
                    }
                } catch (Throwable e) {
                    MindMapUIPlugin.getDefault().getLog().log(new Status(
                            IStatus.WARNING, MindMapUIPlugin.PLUGIN_ID,
                            "Failed to load cached template thumbnail image path map from file: " //$NON-NLS-1$
                                    + mapFile,
                            e));
                }
            }
            cachedImagePathMap = map;
        }
        return cachedImagePathMap;
    }

    private void saveImagePathMap() {
        Properties map = this.cachedImagePathMap;
        if (map == null)
            return;

        File mapFile = getImagePathMapFile();
        FileUtils.ensureFileParent(mapFile);
        try {
            Writer writer = new BufferedWriter(new FileWriter(mapFile));
            try {
                map.store(writer, null);
            } finally {
                writer.close();
            }
        } catch (Throwable e) {
            MindMapUIPlugin.getDefault().getLog().log(new Status(
                    IStatus.WARNING, MindMapUIPlugin.PLUGIN_ID,
                    "Failed to save cached template thumbnail image path map to file: " //$NON-NLS-1$
                            + mapFile,
                    e));
        }
    }

    private static File getImagePathMapFile() {
        return new File(getCacheDirForTemplates(), "images.map"); //$NON-NLS-1$
    }

    private static File getCacheDirForTemplates() {
        return MindMapUIPlugin.getDefault().getStateLocation()
                .append(CACHES_TEMPLATES_DIR).toFile();
    }

    @Override
    public String getText(Object element) {
        if (element instanceof ITemplate) {
            ITemplate template = (ITemplate) element;
            return template.getName();
        }
        return super.getText(element);
    }

}
