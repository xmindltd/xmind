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
package org.xmind.ui.internal.wizards;

@Deprecated
public class TemplateImageLoader {

//    private static final String THUMBNAIL_JPEG = "Thumbnails/thumbnail.jpg"; //$NON-NLS-1$
//    private static final String THUMBNAIL_PNG = "Thumbnails/thumbnail.png"; //$NON-NLS-1$
//
//    /**
//     * Image cache are automatically cleared out in TIMEOUT milliseconds after
//     * the last loading. Default CACHE_TIMEOUT is 5 minutes.
//     */
//    private static final int CACHE_TIMEOUT = 1000 * 300;
//
//    /**
//     * Job will be automatically stopped in FINISH_TIMEOUT milliseconds after
//     * the last loading. Default FINISH_TIMEOUT is 30 minutes.
//     */
//    private static final int FINISH_TIMEOUT = 1000 * 1800;
//
//    private Display display;
//
//    private ICompositeProvider compositeProvider = null;
//
//    private MindMapImageExporter exporter;
//
//    private Queue<ITemplate> templateQueue = new LinkedList<ITemplate>();
//
//    private Map<ITemplate, ImageDescriptor> loadedImages = new HashMap<ITemplate, ImageDescriptor>();
//
//    private int cacheTimeout = 0;
//
//    public TemplateImageLoader(Display display) {
//        super("Template Image Loader"); //$NON-NLS-1$
//        this.display = display;
//        this.exporter = new MindMapImageExporter(display);
//        setSystem(true);
//        schedule();
//    }
//
//    public void loadImage(ITemplate template) {
//        templateQueue.offer(template);
//    }
//
//    @Override
//    protected IStatus run(IProgressMonitor monitor) {
//        try {
//            final GhostShellProvider shellProvider = new GhostShellProvider(
//                    display);
//            this.compositeProvider = shellProvider;
//            try {
//                loop(monitor);
//            } finally {
//                shellProvider.dispose();
//            }
//            if (monitor.isCanceled()) {
//                return Status.CANCEL_STATUS;
//            }
//            return Status.OK_STATUS;
//        } catch (InterruptedException e) {
//            return Status.CANCEL_STATUS;
//        }
//    }
//
//    private void loop(IProgressMonitor monitor) throws InterruptedException {
//        long lastLoadTimestamp = System.currentTimeMillis();
//        do {
//            ITemplate template = templateQueue.poll();
//
//            if (monitor.isCanceled())
//                return;
//
//            if (template != null) {
//                loadImage(template, monitor);
//                lastLoadTimestamp = System.currentTimeMillis();
//                cacheTimeout = CACHE_TIMEOUT;
//            } else {
//                if (cacheTimeout > 0 && System.currentTimeMillis()
//                        - lastLoadTimestamp > cacheTimeout) {
//                    clearCache();
//                    cacheTimeout = 0;
//                } else if (cacheTimeout == 0 && System.currentTimeMillis()
//                        - lastLoadTimestamp > FINISH_TIMEOUT) {
//                    clearCache();
//                    return;
//                }
//            }
//
//            if (monitor.isCanceled())
//                return;
//
//            if (template == null) {
//                Thread.sleep(100);
//            }
//
//            if (monitor.isCanceled())
//                return;
//
//        } while (true);
//    }
//
//    private void loadImage(final ITemplate template,
//            final IProgressMonitor monitor) {
//        ImageDescriptor image = loadedImages.get(template);
//        if (image != null) {
////            if (template instanceof ITemplateAddon) {
////                ((ITemplateAddon) template).withModule(image);
////            }
//        } else {
////            if (template instanceof ITemplateAddon) {
//            ITemplate originTemplate = template;
//
//            URI workbookURI = originTemplate.getURI();
//            if (originTemplate instanceof ISourceWorkbookProvider) {
//                workbookURI = ((ISourceWorkbookProvider) originTemplate)
//                        .getSourceWorkbookURI();
//            }
//            if (workbookURI == null)
//                return;
//
//            InputStream stream;
//            try {
//                URL url = new URL(workbookURI.getScheme(),
//                        workbookURI.getHost(), workbookURI.getPath());
//                stream = url.openStream();
//                if (stream != null) {
//                    try {
//                        image = loadImageFromExistingThumbnail(
//                                template.getName(), stream);
//                    } catch (Throwable e) {
//                        Logger.log(e, "Failed to load image: " //$NON-NLS-1$
//                                + template.getName());
//                    } finally {
//                        try {
//                            stream.close();
//                        } catch (IOException e) {
//                        }
//                    }
//                }
//                if (image == null) {
//                    stream = new URL(workbookURI.getScheme(),
//                            workbookURI.getHost(), workbookURI.getPath())
//                                    .openStream();
//                    if (stream != null) {
//                        try {
//                            image = loadImageFromThumbnailExporter(stream);
//                        } catch (Throwable e) {
//                            Logger.log(e, NLS.bind("Failed to load image: {0}", //$NON-NLS-1$
//                                    template.getName()));
//                        } finally {
//                            try {
//                                stream.close();
//                            } catch (IOException e) {
//                            }
//                        }
//                    }
//                }
//            } catch (MalformedURLException e) {
//                Logger.log(e, NLS.bind("Failed to transfer uri to url: {0}", //$NON-NLS-1$
//                        workbookURI));
//            } catch (IOException e) {
//                Logger.log(e, NLS.bind("Failed to open stream for: {0}", //$NON-NLS-1$
//                        template.getName()));
//            }
//            if (image != null) {
//                loadedImages.put(template, image);
////                    ((ITemplateAddon) template).withModule(image);
////                }
//            }
//        }
//    }
//
//    protected ImageDescriptor loadImageFromExistingThumbnail(
//            String templateName, InputStream stream) throws IOException {
//        ZipInputStream zin = new ZipInputStream(stream);
//        ZipEntry entry = zin.getNextEntry();
//        while (entry != null) {
//            if (THUMBNAIL_PNG.equals(entry.getName())
//                    || THUMBNAIL_JPEG.equals(entry.getName())) {
//                try {
//                    return loadImageFromThumbnail(zin);
//                } catch (Throwable e) {
//                    Logger.log(e, NLS.bind(
//                            "Failed to load thumbnail image from template stream: {0}", //$NON-NLS-1$
//                            templateName));
//                }
//            }
//            entry = zin.getNextEntry();
//        }
//        return null;
//    }
//
//    private ImageDescriptor loadImageFromThumbnail(final InputStream stream) {
//        if (display.isDisposed())
//            return null;
//        final ImageDescriptor[] imageDescriptor = new ImageDescriptor[1];
//        display.syncExec(new Runnable() {
//            public void run() {
//                final ImageLoader loader = new ImageLoader();
//                loader.load(stream);
//                imageDescriptor[0] = ImageDescriptor
//                        .createFromImageData(loader.data[0]);
//            }
//        });
//        return imageDescriptor[0];
//    }
//
//    protected ImageDescriptor loadImageFromThumbnailExporter(InputStream stream)
//            throws Exception {
//        if (display.isDisposed())
//            return null;
//        IStorage storage = new ByteArrayStorage();
//        try {
//            IWorkbook workbook = Core.getWorkbookBuilder()
//                    .loadFromStream(stream, storage);
//            exporter.setSource(new MindMap(workbook.getPrimarySheet()),
//                    compositeProvider, null, new Insets(40));
//            final ImageDescriptor[] imageDescriptor = new ImageDescriptor[1];
//            display.syncExec(new Runnable() {
//                public void run() {
//                    Image image = exporter.createImage();
//                    imageDescriptor[0] = ImageDescriptor.createFromImage(image);
//                }
//            });
//            return imageDescriptor[0];
//        } catch (CoreException e) {
//            if (e.getType() == Core.ERROR_WRONG_PASSWORD
//                    || e.getType() == Core.ERROR_CANCELLATION) {
//                return MindMapUI.getImages()
//                        .get(IMindMapImages.DEFAULT_THUMBNAIL);
//            }
//            throw e;
//        }
//    }
//
//    private void clearCache() {
//        if (loadedImages.isEmpty())
//            return;
//
//        loadedImages.clear();
//    }
//
//    @Override
//    protected void canceling() {
//        Thread thread = getThread();
//        if (thread != null) {
//            thread.interrupt();
//        }
//        super.canceling();
//    }

}