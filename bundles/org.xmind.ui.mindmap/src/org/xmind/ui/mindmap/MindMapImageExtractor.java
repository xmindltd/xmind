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
public class MindMapImageExtractor {

//    private static final int DEFAULT_MARGIN = 15;
//
//    private Display display;
//
//    private Composite parent;
//
//    private ISheet sheet;
//
//    private ITopic centralTopic;
//
//    private Properties properties;
//
//    private Image image;
//
//    private Integer margin = null;
//
//    private Point origin;
//
//    private int resizeStrategy = ResizeConstants.RESIZE_NONE;
//
//    private int widthHint = -1;
//
//    private int heightHint = -1;
//
//    public MindMapImageExtractor(Composite parent, ISheet sheet,
//            ITopic centralTopic) {
//        this(parent.getDisplay(), parent, sheet, centralTopic);
//    }
//
//    public MindMapImageExtractor(Display display, ISheet sheet,
//            ITopic centralTopic) {
//        this(display, null, sheet, centralTopic);
//    }
//
//    private MindMapImageExtractor(Display display, Composite parent,
//            ISheet sheet, ITopic centralTopic) {
//        this.display = display;
//        this.parent = parent;
//        this.sheet = sheet;
//        this.centralTopic = centralTopic;
//        setProperty(IMindMapViewer.VIEWER_CENTERED, Boolean.TRUE);
//        setProperty(IMindMapViewer.VIEWER_CORNERED, Boolean.TRUE);
//        setProperty(IMindMapViewer.VIEWER_MARGIN,
//                Integer.valueOf(DEFAULT_MARGIN));
//    }
//
//    public void setProperty(String key, Object value) {
//        if (properties == null)
//            properties = new Properties();
//        properties.set(key, value);
//    }
//
//    public Object getProperty(String key) {
//        return properties == null ? null : properties.get(key);
//    }
//
//    /**
//     * Get the extracted image. If no image exists, a new one will be created.
//     * 
//     * @return the extracted image, or <code>null</code> if error occurred
//     */
//    public Image getImage() {
//        if (image == null) {
////            display.syncExec(new Runnable() {
////                public void run() {
//            image = createImage();
////                }
////            });
//        }
//        return image;
//    }
//
//    /**
//     * Get the extracted image. If no image exists, a new one will be created.
//     * 
//     * @return the extracted image
//     * @throws SWTError
//     *             if error occurs
//     */
//    public Image getImage2() throws SWTError {
//        if (image == null) {
////            display.syncExec(new Runnable() {
////                public void run() {
//            image = createImage2();
////                }
////            });
//        }
//        return image;
//    }
//
//    public void setMargin(Integer margin) {
//        this.margin = margin;
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
//    private Image createImage() {
//        try {
//            return createImage2();
//        } catch (Throwable e) {
//            Logger.log(e);
//            return null;
//        }
//    }
//
//    private Image createImage2() {
//        final MindMapExportContentProvider provider;
//        if (parent != null) {
//            provider = new MindMapExportContentProvider(parent, sheet,
//                    centralTopic);
//        } else {
//            provider = new MindMapExportContentProvider(display, sheet,
//                    centralTopic);
//        }
//        provider.setProperties(properties);
//        provider.setMargin(margin);
//        provider.setResizeStrategy(resizeStrategy, widthHint, heightHint);
//        Image image;
//        try {
//            image = FigureImageDescriptor.createFromFigure(
//                    provider.getContents(), provider).createImage(false,
//                    display);
//            origin = provider.getOrigin();
//        } finally {
//            display.asyncExec(new Runnable() {
//                public void run() {
//                    provider.dispose();
//                }
//            });
//        }
//        return image;
//    }
//
//    public Point getOrigin() {
//        return origin;
//    }
//
//    public void dispose() {
//        if (image != null) {
//            image.dispose();
//            image = null;
//        }
//    }
}