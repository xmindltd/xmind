/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
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
package org.xmind.ui.mindmap;

/**
 * 
 * @author Frank Shaka
 * @deprecated Use {@link MindMapImageExporter} instead
 */
public class MindMapPreviewBuilder {

//    private static boolean DEBUG = false;
//
//    public static final ImageFormat DEFAULT_FORMAT = ImageFormat.PNG;
//
//    /**
//     * @deprecated Use {@link #getThumbnailPath()} instead.
//     */
//    public static String PATH_THUMBNAIL = "Thumbnails/thumbnail" + DEFAULT_FORMAT.getExtensions().get(0); //$NON-NLS-1$
//
//    private IWorkbook workbook;
//
//    private ISheet sheet;
//
//    private ITopic centralTopic;
//
//    private Properties properties;
//
//    private Point origin;
//
//    private ImageFormat format = DEFAULT_FORMAT;
//
//    private int resizeStrategy = ResizeConstants.RESIZE_NONE;
//
//    private int widthHint = -1;
//
//    private int heightHint = -1;
//
//    private String thumbnailPath = null;
//
//    public MindMapPreviewBuilder(IWorkbook workbook) {
//        this.workbook = workbook;
//        this.sheet = workbook.getPrimarySheet();
//        this.centralTopic = sheet.getRootTopic();
//    }
//
//    public MindMapPreviewBuilder(ISheet sheet, ITopic centralTopic) {
//        this.workbook = sheet.getOwnedWorkbook();
//        this.sheet = sheet;
//        this.centralTopic = centralTopic;
//    }
//
//    public void setProperty(String key, Object value) {
//        if (properties == null)
//            properties = new Properties();
//        properties.set(key, value);
//    }
//
//    public ITopic getCentralTopic() {
//        return centralTopic;
//    }
//
//    public ISheet getSheet() {
//        return sheet;
//    }
//
//    public IWorkbook getWorkbook() {
//        return workbook;
//    }
//
//    public void setResizeStrategy(int resizeStrategy, int widthHint,
//            int heightHint) {
//        this.resizeStrategy = resizeStrategy;
//        this.widthHint = widthHint;
//        this.heightHint = heightHint;
//    }
//
//    public int getResizeStrategy() {
//        return resizeStrategy;
//    }
//
//    public int getWidthHint() {
//        return widthHint;
//    }
//
//    public int getHeightHint() {
//        return heightHint;
//    }
//
//    /**
//     * Build a preview image for this workbook and save it on the local file
//     * system.
//     * 
//     * @param display
//     * @param shell
//     * @return An absolute local file path where the preview image is stored
//     */
//    public String build(Display display) throws IOException {
//        String path = Core.getWorkspace().getTempFile(newTempFileName());
//        build(display, path);
//        return path;
//    }
//
//    public void build(Display display, String targetPath) throws IOException {
//        FileOutputStream out = new FileOutputStream(targetPath);
//        try {
//            build(display, null, out);
//        } finally {
//            out.close();
//        }
//    }
//
//    public String build(Composite parent) throws IOException {
//        String path = Core.getWorkspace().getTempFile(newTempFileName());
//        build(parent, path);
//        return path;
//    }
//
//    public void build(Composite parent, String targetPath) throws IOException {
//        FileOutputStream out = new FileOutputStream(targetPath);
//        try {
//            build(null, parent, out);
//        } finally {
//            out.close();
//        }
//    }
//
//    private void build(Display display, Composite parent, OutputStream out)
//            throws IOException {
//        ISheet sheet = getSheet();
//        ITopic centralTopic = getCentralTopic();
//        final MindMapImageExtractor imageExtractor;
//        if (parent != null && !parent.isDisposed()) {
//            display = parent.getDisplay();
//            imageExtractor = new MindMapImageExtractor(parent, sheet,
//                    centralTopic);
//        } else {
//            imageExtractor = new MindMapImageExtractor(display, sheet,
//                    centralTopic);
//        }
//        imageExtractor.setResizeStrategy(resizeStrategy, widthHint, heightHint);
//        if (properties != null) {
//            for (String key : properties.keySet()) {
//                imageExtractor.setProperty(key, properties.get(key));
//            }
//        }
//        Integer margin = (Integer) imageExtractor
//                .getProperty(IMindMapViewer.VIEWER_MARGIN);
//        if (margin == null) {
//            margin = Integer.valueOf(0);
//        }
//        imageExtractor.setMargin(margin);
//        imageExtractor.setProperty(IMindMapViewer.VIEWER_MARGIN,
//                Integer.valueOf(margin.intValue() + MindMapUI.SHEET_MARGIN));
//        try {
//            final ImageLoader saver = new ImageLoader();
//            log("Start building"); //$NON-NLS-1$
//            log("Start building image"); //$NON-NLS-1$
//            try {
////                display.syncExec(new Runnable() {
////                    public void run() {
////                Object start = Profiler.start("prepare");
//                Image image = imageExtractor.getImage();
////                Profiler.end(start);
//                log("End building image"); //$NON-NLS-1$
//                origin = imageExtractor.getOrigin();
//                log("Start getting image data"); //$NON-NLS-1$
////                start = Profiler.start("obtain");
//                saver.data = new ImageData[] { image.getImageData() };
////                Profiler.end(start);
////                    }
////                });
//            } catch (Throwable e) {
//                Logger.log(e);
//                // Unable to generate an image, simply return
//                return;
//            }
//            log("End getting image data"); //$NON-NLS-1$
//            log("Start saving image data"); //$NON-NLS-1$
////            Object start = Profiler.start("save");
//            saver.save(out, getFormat().getSWTFormat());
////            Profiler.end(start);
////            Profiler.printInfo();
////            Profiler.clear();
//            log("End saving image data"); //$NON-NLS-1$
//        } finally {
//            imageExtractor.dispose();
//            try {
//                out.close();
//            } catch (IOException e) {
//            }
//        }
//    }
//
//    public Point getOrigin() {
//        return origin;
//    }
//
//    private static long t = -1;
//
//    private static void log(String message) {
//        if (!DEBUG)
//            return;
//        if (t < 0)
//            t = System.currentTimeMillis();
//        long c = System.currentTimeMillis();
//        System.out.println("[" + (c - t) + "] " + message); //$NON-NLS-1$  //$NON-NLS-2$
//        t = c;
//    }
//
//    public void save(Display display) throws IOException {
//        IManifest manifest = workbook.getManifest();
//        IFileEntry entry = createThumbnailEntry(manifest, false);
//        OutputStream out = entry.getOutputStream();
//        if (out == null)
//            throw new IOException(
//                    "No output stream is available on this entry."); //$NON-NLS-1$
//        build(display, null, out);
//        entry.decreaseReference();
//        entry.increaseReference();
//    }
//
//    public void save(Composite parent) throws IOException {
//        IManifest manifest = workbook.getManifest();
//        IFileEntry entry = createThumbnailEntry(manifest, false);
//        OutputStream out = entry.getOutputStream();
//        if (out == null)
//            throw new IOException(
//                    "No output stream is available on this entry."); //$NON-NLS-1$
//        build(null, parent, out);
//        entry.decreaseReference();
//        entry.increaseReference();
//    }
//
//    public void saveFrom(InputStream previewStream) throws IOException {
//        IManifest manifest = workbook.getManifest();
//
//        IFileEntry entry = createThumbnailEntry(manifest, true);
//        OutputStream out = entry.getOutputStream();
//        if (out == null)
//            throw new IOException(
//                    "No output stream is available on this entry."); //$NON-NLS-1$
//        FileUtils.transfer(previewStream, out, true);
//        entry.decreaseReference();
//        entry.increaseReference();
//    }
//
//    public void saveFrom(String previewLocation) throws IOException {
//        saveFrom(new FileInputStream(previewLocation));
//    }
//
//    /**
//     * @param manifest
//     * @param ignoreEncryption
//     *            TODO
//     * @return
//     */
//    private IFileEntry createThumbnailEntry(IManifest manifest,
//            boolean ignoreEncryption) {
//        IFileEntry entry = manifest.createFileEntry(getThumbnailPath(),
//                getFormat().getMediaType());
//        ((FileEntryImpl) entry).setIgnoreEncryption(ignoreEncryption);
//        return entry;
//    }
//
//    private String newTempFileName() {
//        return "export/" + Core.getIdFactory().createId() + getFormat().getExtensions().get(0); //$NON-NLS-1$ 
//    }
//
//    public ImageFormat getFormat() {
//        if (format == null)
//            format = DEFAULT_FORMAT;
//        return format;
//    }
//
//    public void setFormat(ImageFormat format) {
//        this.format = format;
//    }
//
//    public String getThumbnailPath() {
//        if (thumbnailPath == null) {
//            thumbnailPath = "Thumbnails/thumbnail" + getFormat().getExtensions().get(0); //$NON-NLS-1$
//        }
//        return thumbnailPath;
//    }
//
//    public void setThumbnailPath(String path) {
//        this.thumbnailPath = path;
//    }

}