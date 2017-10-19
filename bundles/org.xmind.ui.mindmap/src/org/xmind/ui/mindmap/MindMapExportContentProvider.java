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

import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.gef.image.IExportAreaProvider;

/**
 * 
 * @author Frank Shaka
 * @deprecated Use {@link MindMapImageExporter} instead
 */
public class MindMapExportContentProvider implements IExportAreaProvider {

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.gef.image.IExportAreaProvider#getExportArea()
     */
    public Rectangle getExportArea() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.gef.image.IExportAreaProvider#getScale()
     */
    public double getScale() {
        // TODO Auto-generated method stub
        return 0;
    }

//    private static final int DEFAULT_MARGIN = 15;
//
//    private Display display;
//
//    private Composite parent;
//
//    private IMindMap source;
//
//    private Properties properties;
//
//    private Integer margin = null;
//
//    private Point origin;
//
//    private IExportAreaProvider delegate = null;
//
//    private Shell shell;
//
//    private Control control;
//
//    private IFigure contents;
//
//    private IMindMapViewer viewer;
//
//    private int resizeStrategy = ResizeConstants.RESIZE_NONE;
//
//    private int widthHint = -1;
//
//    private int heightHint = -1;
//
//    public MindMapExportContentProvider(Composite parent, IMindMap source) {
//        this(parent.getDisplay(), parent, source);
//    }
//
//    public MindMapExportContentProvider(Display display, IMindMap source) {
//        this(display, null, source);
//    }
//
//    public MindMapExportContentProvider(Composite parent, ISheet sheet,
//            ITopic centralTopic) {
//        this(parent.getDisplay(), parent, new MindMap(sheet, centralTopic));
//    }
//
//    public MindMapExportContentProvider(Display display, ISheet sheet,
//            ITopic centralTopic) {
//        this(display, null, new MindMap(sheet, centralTopic));
//    }
//
//    private MindMapExportContentProvider(Display display, Composite parent,
//            IMindMap source) {
//        this.display = display;
//        this.parent = parent;
//        this.source = source;
//        setProperty(IMindMapViewer.VIEWER_CENTERED, Boolean.TRUE);
//        setProperty(IMindMapViewer.VIEWER_CORNERED, Boolean.TRUE);
//        setProperty(IMindMapViewer.VIEWER_MARGIN,
//                Integer.valueOf(DEFAULT_MARGIN));
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
//    public void setProperty(String key, Object value) {
//        if (properties == null)
//            properties = new Properties();
//        properties.set(key, value);
//    }
//
//    public void setProperties(Properties properties) {
//        this.properties = properties;
//    }
//
//    public void setMargin(Integer margin) {
//        this.margin = margin;
//    }
//
//    public Rectangle getExportArea() {
//        return getDelegate().getExportArea();
//    }
//
//    private IExportAreaProvider getDelegate() {
//        if (delegate == null) {
//            delegate = createDelegate();
//        }
//        return delegate;
//    }
//
//    private IExportAreaProvider createDelegate() {
//        final MindMapViewer viewer = new MindMapViewer();
//        this.viewer = viewer;
//        viewer.setProperties(properties);
//
//        display.syncExec(new Runnable() {
//            public void run() {
//                final Composite container = getContainer();
//                if (container != parent && container instanceof Shell) {
//                    container.setBounds(-300, -300, 180, 180);
//                    if (!"cocoa".equals(SWT.getPlatform())) { //$NON-NLS-1$
//                        container.setVisible(true);
//                    }
//                }
//                GridLayout layout = new GridLayout();
//                layout.marginWidth = 0;
//                layout.marginHeight = 0;
//                container.setLayout(layout);
//                viewer.createControl(container);
//                MindMapExportContentProvider.this.control = viewer.getControl();
//                viewer.getControl().setLayoutData(
//                        new GridData(GridData.FILL, GridData.FILL, true, true));
//                viewer.setInput(source);
//                container.layout();
//            }
//        });
//
//        // Fix bug: 96
//        viewer.getCanvas().getLightweightSystem().getUpdateManager()
//                .performValidation();
//
//        this.contents = getPaintingContents(viewer);
//        IFigure boundsContents = getBoundsContents(contents, viewer);
//        Rectangle bounds = calcContentsBounds(boundsContents, viewer);
//        int margin = getMargin();
//
//        Point realOrigin = getOrigin(boundsContents);
//        this.origin = new Point(realOrigin.x - bounds.x + margin, realOrigin.y
//                - bounds.y + margin);
//
//        return ImageExportUtils.createExportAreaProvider(bounds,
//                resizeStrategy, widthHint, heightHint, new Insets(margin));
//    }
//
//    public IMindMapViewer getViewer() {
//        getDelegate();
//        return viewer;
//    }
//
//    public IFigure getContents() {
//        getDelegate();
//        return contents;
//    }
//
//    public double getScale() {
//        return getDelegate().getScale();
//    }
//
//    private Composite getContainer() {
//        if (parent != null && !parent.isDisposed()
//                && !(parent instanceof Shell)) {
//            return parent;
//        }
//
//        if (shell != null && !shell.isDisposed())
//            return shell;
//
//        display.syncExec(new Runnable() {
//            public void run() {
//                shell = new Shell(display, SWT.NO_TRIM);
//            }
//        });
//        return shell;
//    }
//
//    public Point getOrigin() {
//        return origin;
//    }
//
//    private int getMargin() {
//        if (margin != null)
//            return margin.intValue();
//        if (properties != null) {
//            Object margin = properties.get(IMindMapViewer.VIEWER_MARGIN);
//            if (margin instanceof Integer)
//                return ((Integer) margin).intValue();
//        }
//        return 0;
//    }
//
//    protected IFigure getPaintingContents(IGraphicalViewer viewer) {
//        IFigure contents = viewer.getCanvas().getViewport().getContents();
//        if (contents != null)
//            return contents;
//        return viewer.getCanvas().getLightweightSystem().getRootFigure();
//    }
//
//    protected IFigure getBoundsContents(IFigure paintingContents,
//            IGraphicalViewer viewer) {
//        IPart contentsPart = viewer.getRootPart().getContents();
//        if (contentsPart instanceof IGraphicalPart
//                && contentsPart.getStatus().isActive()) {
//            return ((IGraphicalPart) contentsPart).getFigure();
//        }
//        return paintingContents;
//    }
//
//    protected Rectangle calcContentsBounds(IFigure contents,
//            IGraphicalViewer viewer) {
//        if (contents instanceof FreeformFigure)
//            return ((FreeformFigure) contents).getFreeformExtent().getCopy();
//        return contents.getBounds().getCopy();
//    }
//
//    protected Point getOrigin(IFigure figure) {
//        if (figure instanceof IOriginBased)
//            return ((IOriginBased) figure).getOrigin();
//        if (figure instanceof IReferencedFigure)
//            return ((IReferencedFigure) figure).getReference();
//        return new Point();
//    }
//
//    public void dispose() {
//        if (control != null) {
//            control.dispose();
//            control = null;
//        }
//        if (shell != null) {
//            shell.dispose();
//            shell = null;
//        }
//    }
}