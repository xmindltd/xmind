package org.xmind.ui.internal.dialogs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Hex;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.xmind.core.IFileEntry;
import org.xmind.core.ISheet;
import org.xmind.core.internal.UserDataConstants;
import org.xmind.core.style.IStyle;
import org.xmind.core.util.FileUtils;
import org.xmind.core.util.HyperlinkUtils;
import org.xmind.gef.EditDomain;
import org.xmind.gef.GEF;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.Request;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.gef.part.IPart;
import org.xmind.gef.util.Properties;
import org.xmind.ui.commands.CommandMessages;
import org.xmind.ui.gallery.FrameDecorator;
import org.xmind.ui.gallery.FrameFigure;
import org.xmind.ui.gallery.FramePart;
import org.xmind.ui.gallery.GalleryLayout;
import org.xmind.ui.gallery.GalleryPartFactory;
import org.xmind.ui.gallery.GallerySelectTool;
import org.xmind.ui.gallery.GalleryViewer;
import org.xmind.ui.gallery.ShadowedLayer;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.properties.PropertyMessages;
import org.xmind.ui.mindmap.IMindMap;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.resources.ColorUtils;
import org.xmind.ui.style.Styles;
import org.xmind.ui.util.ImageFormat;

public class WallpaperDialog extends PopupDialog implements IOpenListener {

    private static final String LOCAL_WALLPAPER_DIALOG_PATH = "org.xmind.ui.localWallpaperDialogPath"; //$NON-NLS-1$
    private static final Dimension FRAME_IMAGE_SIZE = new Dimension(64, 64);

    private class WallpaperLabelProvider extends LabelProvider {

        public Image getImage(Object element) {
            return getWallpaperPreviewImage(element);
        }
    }

    private static class WallpaperFrameDecorator extends FrameDecorator {
        public static final WallpaperFrameDecorator DEFAULT = new WallpaperFrameDecorator();

        @Override
        public void decorate(IGraphicalPart part, IFigure figure) {
            FrameFigure frame = (FrameFigure) part.getFigure();
            ShadowedLayer layer = frame.getContentPane();

            if (checkWallpaper(part)) {
                layer.setBorderWidth(2);
                layer.setBorderAlpha(0xFF);
                layer.setBorderColor(ColorUtils.getColor("#e05236")); //$NON-NLS-1$
            } else {
                layer.setBorderWidth(1);
                layer.setBorderAlpha(0x20);
                layer.setBorderColor(ColorUtils.getColor("#cccccc")); //$NON-NLS-1$
            }

            super.decorate(part, figure);
        }

        private boolean checkWallpaper(IGraphicalPart part) {
            String modeMD5 = getModelMD5(part);
            String currentMD5 = getCurrentWallpaperMD5();
            return currentMD5 != null && !"".equals(currentMD5) //$NON-NLS-1$
                    && currentMD5.equals(modeMD5);
        }

        private String getModelMD5(IGraphicalPart part) {
            Object model = part.getModel();
            if (model != null && model instanceof String) {
                try {
                    FileInputStream fis = new FileInputStream((String) model);
                    return getFileMD5(fis);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

            }
            return ""; //$NON-NLS-1$
        }

        private String getCurrentWallpaperMD5() {
            if (mindMapViewer == null)
                return ""; //$NON-NLS-1$
            ISheet sheet = (ISheet) mindMapViewer.getAdapter(ISheet.class);
            if (sheet != null) {
                IStyle style = getStyle(sheet);
                if (style != null) {
                    String url = style.getProperty(Styles.Background);
                    if (url == null)
                        return ""; //$NON-NLS-1$
                    String path = HyperlinkUtils.toAttachmentPath(url);
                    IFileEntry fe = sheet.getOwnedWorkbook().getManifest()
                            .getFileEntry(path);
                    if (fe != null) {
                        InputStream is = fe.getInputStream();
                        return getFileMD5(is);
                    }
                }
            }

            return ""; //$NON-NLS-1$
        }

        private IStyle getStyle(ISheet sheet) {
            if (sheet != null) {
                String styleId = sheet.getStyleId();
                if (styleId == null)
                    return null;
                return sheet.getOwnedWorkbook().getStyleSheet()
                        .findStyle(styleId);
            }
            return null;
        }

        private String getFileMD5(InputStream input) {
            try {
                MessageDigest md = MessageDigest.getInstance("MD5"); //$NON-NLS-1$
                md.reset();
                byte[] bytes = new byte[2048];
                int numBytes;
                try {
                    while ((numBytes = input.read(bytes)) != -1)
                        md.update(bytes, 0, numBytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                byte[] digest = md.digest();
                return new String(Hex.encodeHex(digest));
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            return ""; //$NON-NLS-1$
        }
    }

    private static class WallpaperItemPart extends FramePart {
        public WallpaperItemPart(Object model) {
            super(model);
            setDecorator(WallpaperFrameDecorator.DEFAULT);
        }
    }

    private static class WallpaperPartFactory extends GalleryPartFactory {
        @Override
        protected IPart createFramePart(IPart parent, Object model) {
            return new WallpaperItemPart(model);
        }
    }

    private static String WallpapersPath = null;

    private static String PatternPath = null;

    private Control initLocationControl;

    private static IGraphicalViewer mindMapViewer;

    private GalleryViewer patternViewer;

    private GalleryViewer paperViewer;

    private Map<Object, Image> wallpaperPreviewImages;

    private Job patternLoader;

    private Job imageLoader;

    private List<String> allPatternImageFiles;

    private List<String> loadedPatternImageFiles;

    private List<String> allImageFiles;

    private List<String> loadedImageFiles;

    private String selectedWallpaperPath;

    public WallpaperDialog(Shell parent, Control initLocationControl) {
        super(parent, SWT.RESIZE, true, true, true, false, false, null, null);
        this.initLocationControl = initLocationControl;
        initLocationControl.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                releaseWallpaperPreviewImages();
            }
        });
    }

    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);

        createPatternArea(composite);

        createWallpaperArea(composite);

        return composite;
    }

    private void createPatternArea(Composite parent) {
        Composite top = new Composite(parent, SWT.NONE);
        GridLayout topLayout = new GridLayout();
        topLayout.marginLeft = 5;
        topLayout.marginRight = 0;
        topLayout.marginTop = 0;
        topLayout.marginBottom = 1;
        top.setLayout(topLayout);
        top.setLayoutData(
                new GridData(GridData.FILL, GridData.FILL, true, false));
        Label label = new Label(top, SWT.NONE);
        label.setText(PropertyMessages.PatternCategory_title);

        patternViewer = new GalleryViewer();
        Properties properties = patternViewer.getProperties();
        properties.set(GalleryViewer.Horizontal, Boolean.TRUE);
        properties.set(GalleryViewer.Wrap, Boolean.TRUE);
        properties.set(GalleryViewer.FlatFrames, Boolean.TRUE);
        properties.set(GalleryViewer.ImageConstrained, Boolean.TRUE);
        properties.set(GalleryViewer.Layout,
                new GalleryLayout(GalleryLayout.ALIGN_TOPLEFT,
                        GalleryLayout.ALIGN_FILL, 1, 1, new Insets(5)));
        properties.set(GalleryViewer.FrameContentSize, new Dimension(48, 48));
        properties.set(GalleryViewer.TitlePlacement,
                GalleryViewer.TITLE_BOTTOM);
        properties.set(GalleryViewer.SingleClickToOpen, Boolean.TRUE);
        properties.set(GalleryViewer.HideTitle, Boolean.TRUE);
        properties.set(GalleryViewer.SolidFrames, Boolean.FALSE);

        patternViewer.setPartFactory(new WallpaperPartFactory());
        patternViewer.setLabelProvider(new WallpaperLabelProvider());
        patternViewer.addOpenListener(this);

        EditDomain editDomain = new EditDomain();
        editDomain.installTool(GEF.TOOL_SELECT, new GallerySelectTool());
        patternViewer.setEditDomain(editDomain);

        patternViewer.createControl(parent);
        GridData galleryData = new GridData(GridData.FILL, GridData.FILL, true,
                true);
        galleryData.widthHint = 360;
        galleryData.heightHint = 300;
        patternViewer.getControl().setLayoutData(galleryData);

        final Display display = parent.getDisplay();
        patternViewer.getControl().setBackground(
                display.getSystemColor(SWT.COLOR_LIST_BACKGROUND));
        if (allPatternImageFiles != null && loadedPatternImageFiles != null
                && loadedPatternImageFiles.containsAll(allPatternImageFiles)) {
            patternViewer.setInput(loadedPatternImageFiles.toArray());
        } else {
            patternViewer.setInput(new Object[0]);
            display.asyncExec(new Runnable() {
                public void run() {
                    if (patternViewer.getControl() != null
                            && !patternViewer.getControl().isDisposed()) {
                        startLoadingPatternImages(display);
                    }
                }
            });
        }

        Composite bottom = new Composite(parent, SWT.NONE);
        GridLayout bottomLayout = new GridLayout();
        bottomLayout.marginWidth = 0;
        bottomLayout.marginHeight = 0;
        bottomLayout.marginBottom = 10;
        bottom.setLayout(bottomLayout);
        bottom.setLayoutData(
                new GridData(GridData.FILL, GridData.FILL, true, false));
    }

    private void createWallpaperArea(Composite parent) {
        Composite top = new Composite(parent, SWT.NONE);
        GridLayout topLayout = new GridLayout();
        topLayout.marginLeft = 5;
        topLayout.marginRight = 0;
        topLayout.marginTop = 0;
        topLayout.marginBottom = 1;
        top.setLayout(topLayout);
        top.setLayoutData(
                new GridData(GridData.FILL, GridData.FILL, true, false));
        Label label = new Label(top, SWT.NONE);
        label.setText(PropertyMessages.WallpaperCategory_title);

        paperViewer = new GalleryViewer();
        Properties properties = paperViewer.getProperties();
        properties.set(GalleryViewer.Horizontal, Boolean.TRUE);
        properties.set(GalleryViewer.Wrap, Boolean.TRUE);
        properties.set(GalleryViewer.FlatFrames, Boolean.TRUE);
        properties.set(GalleryViewer.ImageConstrained, Boolean.TRUE);
        properties.set(GalleryViewer.Layout,
                new GalleryLayout(GalleryLayout.ALIGN_TOPLEFT,
                        GalleryLayout.ALIGN_FILL, 1, 1, new Insets(5)));
        properties.set(GalleryViewer.FrameContentSize, new Dimension(48, 48));
        properties.set(GalleryViewer.TitlePlacement,
                GalleryViewer.TITLE_BOTTOM);
        properties.set(GalleryViewer.SingleClickToOpen, Boolean.TRUE);
        properties.set(GalleryViewer.HideTitle, Boolean.TRUE);
        properties.set(GalleryViewer.SolidFrames, Boolean.FALSE);

        paperViewer.setPartFactory(new WallpaperPartFactory());
        paperViewer.setLabelProvider(new WallpaperLabelProvider());
        paperViewer.addOpenListener(this);

        EditDomain editDomain = new EditDomain();
        editDomain.installTool(GEF.TOOL_SELECT, new GallerySelectTool());
        paperViewer.setEditDomain(editDomain);

        paperViewer.createControl(parent);
        GridData galleryData = new GridData(GridData.FILL, GridData.FILL, true,
                true);
        galleryData.widthHint = 360;
        galleryData.heightHint = 300;
        paperViewer.getControl().setLayoutData(galleryData);

        final Display display = parent.getDisplay();
        paperViewer.getControl().setBackground(
                display.getSystemColor(SWT.COLOR_LIST_BACKGROUND));
        if (allImageFiles != null && loadedImageFiles != null
                && loadedImageFiles.containsAll(allImageFiles)) {
            paperViewer.setInput(loadedImageFiles.toArray());
        } else {
            paperViewer.setInput(new Object[0]);
            display.asyncExec(new Runnable() {
                public void run() {
                    if (paperViewer.getControl() != null
                            && !paperViewer.getControl().isDisposed()) {
                        startLoadingImages(display);
                    }
                }
            });
        }

        Composite bottom = new Composite(parent, SWT.NONE);
        GridLayout bottomLayout = new GridLayout();
        bottomLayout.marginWidth = 0;
        bottomLayout.marginHeight = 0;
        bottom.setLayout(bottomLayout);
        bottom.setLayoutData(
                new GridData(GridData.FILL, GridData.FILL, true, false));

        Button chooseFromLocalButton = new Button(bottom, SWT.PUSH);
        chooseFromLocalButton.setText(PropertyMessages.LocalImage_text);
        chooseFromLocalButton.setLayoutData(
                new GridData(GridData.CENTER, GridData.CENTER, true, false));
        chooseFromLocalButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                openLocalImageFileDialog();
                Shell shell = getShell();
                if (shell != null && !shell.isDisposed())
                    shell.close();
                close();
            }
        });

    }

    private void startLoadingImages(final Display display) {
        if (imageLoader != null) {
            imageLoader.cancel();
            imageLoader = null;
        }

        imageLoader = new Job(PropertyMessages.LoadWallpapers_jobName) {

            private Runnable refreshJob = null;

            protected IStatus run(IProgressMonitor monitor) {
                if (allImageFiles == null) {
                    collectImageFiles(getWallpapersPath());
                    if (allImageFiles == null) {
                        allImageFiles = Collections.emptyList();
                    }
                }

                if (allImageFiles.isEmpty()) {
                    if (loadedImageFiles == null || !loadedImageFiles.isEmpty())
                        loadedImageFiles = Collections.emptyList();
                    refreshViewer(display);
                } else if (loadedImageFiles != null
                        && loadedImageFiles.containsAll(allImageFiles)) {
                    refreshViewer(display);
                } else {
                    monitor.beginTask(null, allImageFiles.size());
                    if (loadedImageFiles == null)
                        loadedImageFiles = new ArrayList<String>(
                                allImageFiles.size());
                    long lastRefresh = System.currentTimeMillis();
                    for (Object o : allImageFiles.toArray()) {
                        if (monitor.isCanceled()) {
                            break;
                        }

                        if (!loadedImageFiles.contains(o)) {
                            final String path = (String) o;
                            monitor.subTask(new File(path).getName());

                            Image image = getWallpaperPreviewImage(display,
                                    path);
                            if (image != null) {
                                loadedImageFiles.add(path);
                            } else {
                                allImageFiles.remove(path);
                            }
                        }

                        monitor.worked(1);

                        if ((System.currentTimeMillis() - lastRefresh) > 50) {
                            refreshViewer(display);
                            lastRefresh = System.currentTimeMillis();
                        }

                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                }

                if (!monitor.isCanceled()) {
                    monitor.done();
                }
                imageLoader = null;
                refreshViewer(display);
                return new Status(IStatus.OK, MindMapUIPlugin.PLUGIN_ID,
                        IStatus.OK, "Wallpaper images loaded.", null); //$NON-NLS-1$
            }

            private void refreshViewer(final Display display) {
                if (refreshJob != null)
                    return;

                refreshJob = new Runnable() {
                    public void run() {
                        if (paperViewer != null
                                && paperViewer.getControl() != null
                                && !paperViewer.getControl().isDisposed()
                                && loadedImageFiles != null) {
                            paperViewer.setInput(loadedImageFiles.toArray());
                            paperViewer.getControl().getParent().layout();
                        }
                        refreshJob = null;
                    }
                };
                display.asyncExec(refreshJob);
            }

            private void collectImageFiles(String path) {
                File file = new File(path);
                if (file.isDirectory() && path.equals(getWallpapersPath())) {
                    for (String name : file.list()) {
                        collectImageFiles(
                                new File(file, name).getAbsolutePath());
                    }
                } else if (file.isFile()) {
                    String ext = FileUtils.getExtension(path);
                    ImageFormat format = ImageFormat.findByExtension(ext, null);
                    if (format != null) {
                        if (allImageFiles == null)
                            allImageFiles = new ArrayList<String>();
                        allImageFiles.add(path);
                    }
                }
            }
        };
        imageLoader.schedule();
    }

    private void startLoadingPatternImages(final Display display) {
        if (patternLoader != null) {
            patternLoader.cancel();
            patternLoader = null;
        }

        patternLoader = new Job(PropertyMessages.LoadWallpapers_jobName) {

            private Runnable refreshJob = null;

            protected IStatus run(IProgressMonitor monitor) {
                if (allPatternImageFiles == null) {
                    collectImageFiles(getPatternsPath());
                    if (allPatternImageFiles == null) {
                        allPatternImageFiles = Collections.emptyList();
                    }
                }
                if (allPatternImageFiles.isEmpty()) {
                    if (loadedPatternImageFiles == null
                            || !loadedPatternImageFiles.isEmpty())
                        loadedPatternImageFiles = Collections.emptyList();
                    refreshViewer(display);
                } else if (loadedPatternImageFiles != null
                        && loadedPatternImageFiles
                                .containsAll(allPatternImageFiles)) {
                    refreshViewer(display);
                } else {
                    monitor.beginTask(null, allPatternImageFiles.size());
                    if (loadedPatternImageFiles == null)
                        loadedPatternImageFiles = new ArrayList<String>(
                                allPatternImageFiles.size());
                    long lastRefresh = System.currentTimeMillis();
                    for (Object o : allPatternImageFiles.toArray()) {
                        if (monitor.isCanceled()) {
                            break;
                        }

                        if (!loadedPatternImageFiles.contains(o)) {
                            final String path = (String) o;
                            monitor.subTask(new File(path).getName());

                            Image image = getWallpaperPreviewImage(display,
                                    path);
                            if (image != null) {
                                loadedPatternImageFiles.add(path);
                            } else {
                                allPatternImageFiles.remove(path);
                            }
                        }

                        monitor.worked(1);

                        if ((System.currentTimeMillis() - lastRefresh) > 50) {
                            refreshViewer(display);
                            lastRefresh = System.currentTimeMillis();
                        }

                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                }

                if (!monitor.isCanceled()) {
                    monitor.done();
                }
                patternLoader = null;
                refreshViewer(display);
                return new Status(IStatus.OK, MindMapUIPlugin.PLUGIN_ID,
                        IStatus.OK, "Wallpaper images loaded.", null); //$NON-NLS-1$
            }

            private void refreshViewer(final Display display) {
                if (refreshJob != null)
                    return;

                refreshJob = new Runnable() {
                    public void run() {
                        if (patternViewer != null
                                && patternViewer.getControl() != null
                                && !patternViewer.getControl().isDisposed()
                                && loadedPatternImageFiles != null) {
                            patternViewer.setInput(
                                    loadedPatternImageFiles.toArray());
                            patternViewer.getControl().getParent().layout();
                        }
                        refreshJob = null;
                    }
                };
                display.asyncExec(refreshJob);
            }

            private void collectImageFiles(String path) {
                File file = new File(path);
                if (file.isDirectory() && path.equals(getPatternsPath())) {
                    for (String name : file.list()) {
                        collectImageFiles(
                                new File(file, name).getAbsolutePath());
                    }
                } else if (file.isFile()) {
                    String ext = FileUtils.getExtension(path);
                    ImageFormat format = ImageFormat.findByExtension(ext, null);
                    if (format != null) {
                        if (allPatternImageFiles == null)
                            allPatternImageFiles = new ArrayList<String>();
                        allPatternImageFiles.add(path);
                    }
                }
            }
        };
        patternLoader.schedule();
    }

    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.addListener(SWT.Deactivate, new Listener() {
            public void handleEvent(Event event) {
                event.display.asyncExec(new Runnable() {
                    public void run() {
                        close();
                    }
                });
            }
        });
        shell.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                if (imageLoader != null) {
                    imageLoader.cancel();
                    imageLoader = null;
                }
                if (patternLoader != null) {
                    patternLoader.cancel();
                    patternLoader = null;
                }
            }
        });
    }

    public void setMindMapViewer(IGraphicalViewer viewer) {
        mindMapViewer = viewer;
    }

    @SuppressWarnings("unchecked")
    protected List getBackgroundColorExclusions() {
        List list = super.getBackgroundColorExclusions();
        if (paperViewer != null) {
            list.add(paperViewer.getControl());
        }

        if (patternViewer != null) {
            list.add(patternViewer.getControl());
        }
        return list;
    }

    protected Point getInitialLocation(Point initialSize) {
        if (initLocationControl != null && !initLocationControl.isDisposed()) {
            Point loc = initLocationControl
                    .toDisplay(initLocationControl.getLocation());
            return new Point(loc.x,
                    loc.y + initLocationControl.getBounds().height);
        } else if (mindMapViewer != null) {
            return new Point(50, 50);
        }
        return super.getInitialLocation(initialSize);
    }

    protected IDialogSettings getDialogSettings() {
        return MindMapUIPlugin.getDefault()
                .getDialogSettings(MindMapUI.POPUP_DIALOG_SETTINGS_ID);
    }

    public void open(OpenEvent event) {
        Object o = ((IStructuredSelection) event.getSelection())
                .getFirstElement();
        if (o instanceof String) {
            String path = (String) o;
            selectedWallpaperPath = path;
            Shell shell = getShell();
            if (shell != null && !shell.isDisposed())
                shell.close();
            close();
            changeWallpaper(path);
        }
    }

    public String getSelectedWallpaperPath() {
        return selectedWallpaperPath;
    }

    private Image getWallpaperPreviewImage(Object element) {
        return getWallpaperPreviewImage(Display.getCurrent(), element);
    }

    private Image getWallpaperPreviewImage(Display display, Object element) {
        Image image = null;
        if (wallpaperPreviewImages != null) {
            image = wallpaperPreviewImages.get(element);
        }
        if (image == null) {
            if (element instanceof String) {
                String path = (String) element;
                try {
                    image = new Image(display, path);
                } catch (IllegalArgumentException e) {
                } catch (SWTException e) {
                } catch (SWTError e) {
                }
                if (image != null) {
                    Image filled = createFilledImage(display, image,
                            FRAME_IMAGE_SIZE);
                    if (filled != null) {
                        image.dispose();
                        image = filled;
                    }
                }
            }
            if (image != null) {
                cacheWallpaperPreviewImage(element, image);
            }
        }
        return image;
    }

    private void cacheWallpaperPreviewImage(Object element, Image image) {
        if (wallpaperPreviewImages == null)
            wallpaperPreviewImages = new HashMap<Object, Image>();
        wallpaperPreviewImages.put(element, image);
    }

    private void releaseWallpaperPreviewImages() {
        if (wallpaperPreviewImages != null) {
            for (Image image : wallpaperPreviewImages.values()) {
                image.dispose();
            }
            wallpaperPreviewImages = null;
        }
    }

    private void openLocalImageFileDialog() {

        FileDialog dialog = new FileDialog(getParentShell(),
                SWT.OPEN | SWT.SINGLE);
        DialogUtils.makeDefaultImageSelectorDialog(dialog, true);
        dialog.setText(PropertyMessages.WallpaperDialog_title);

        IDialogSettings settings = MindMapUIPlugin.getDefault()
                .getDialogSettings();
        String filterPath = settings.get(LOCAL_WALLPAPER_DIALOG_PATH);
        if (filterPath == null || "".equals(filterPath) //$NON-NLS-1$
                || !new File(filterPath).exists()) {
            filterPath = getWallpapersPath();
        }
        dialog.setFilterPath(filterPath);
        String path = dialog.open();
        if (path == null)
            return;

        selectedWallpaperPath = path;

        filterPath = new File(path).getParent();
        settings.put(LOCAL_WALLPAPER_DIALOG_PATH, filterPath);
        changeWallpaper(path);
    }

    private void changeWallpaper(String path) {
        if (mindMapViewer == null)
            return;

        MindMapUIPlugin.getDefault().getUsageDataCollector().trackEvent(
                UserDataConstants.CATEGORY_FORMAT,
                UserDataConstants.CHANGE_WALLPAPER);
        Request request = new Request(MindMapUI.REQ_MODIFY_STYLE)
                .setViewer(mindMapViewer);
        request.setParameter(MindMapUI.PARAM_COMMAND_LABEL,
                CommandMessages.Command_ModifySheetBackgroundColor);
        request.setParameter(MindMapUI.PARAM_STYLE_PREFIX + Styles.Background,
                path);
        int value;
        if (isPattern(path))
            value = 10;
        else
            value = 100;
        request.setParameter(MindMapUI.PARAM_STYLE_PREFIX + Styles.Opacity,
                String.valueOf((double) value * 1.0 / 100));
        request.setTargets(fillParts());
        mindMapViewer.getEditDomain().handleRequest(request);
    }

    private boolean isPattern(String path) {
        int index;
        if (Util.isWin32())
            index = path.lastIndexOf('\\');
        else
            index = path.lastIndexOf('/');
        if (index < 7)
            return false;
        String category = path.substring(index - 7, index);

        if ("pattern".equals(category)) //$NON-NLS-1$
            return true;

        return false;
    }

    private List<IPart> fillParts() {
        List<IPart> parts = new ArrayList<IPart>();
        Object input = mindMapViewer.getInput();
        if (input instanceof IMindMap) {
            ISheet sheet = ((IMindMap) input).getSheet();
            IPart part = mindMapViewer.findPart(sheet);
            parts.add(part);
        }
        return parts;
    }

    private static String getWallpapersPath() {
        if (WallpapersPath == null) {
            WallpapersPath = createWallpapersPath("wallpaper"); //$NON-NLS-1$
        }
        return WallpapersPath;
    }

    private static String getPatternsPath() {
        if (PatternPath == null)
            PatternPath = createWallpapersPath("pattern"); //$NON-NLS-1$
        return PatternPath;
    }

    private static String createWallpapersPath(String category) {
        URL url = FileLocator.find(Platform.getBundle(MindMapUI.PLUGIN_ID),
                new Path("wallpaper/" + category), null); //$NON-NLS-1$
        try {
            url = FileLocator.toFileURL(url);
        } catch (IOException e) {
        }
        String path = url.getFile();
        if ("".equals(path)) { //$NON-NLS-1$
            path = new File(System.getProperty("user.home"), "Pictures") //$NON-NLS-1$ //$NON-NLS-2$
                    .getAbsolutePath();
        }
        return path;
    }

    private static Image createFilledImage(Display display, Image src,
            Dimension size) {
        int height = size.height;
        int width = size.width;

        ImageData srcData = src.getImageData();
        int srcWidth = srcData.width;
        int srcHeight = srcData.height;

        if (srcWidth == width && srcHeight == height)
            return null;

        ImageData destData = new ImageData(width, height, srcData.depth,
                srcData.palette);
        destData.type = srcData.type;
        destData.transparentPixel = srcData.transparentPixel;
        destData.alpha = srcData.alpha;

        if (srcData.transparentPixel != -1) {
            for (int x = 0; x < width; x++)
                for (int y = 0; y < height; y++)
                    destData.setPixel(x, y, srcData.transparentPixel);
        } else {
            for (int x = 0; x < width; x++)
                for (int y = 0; y < height; y++)
                    destData.setAlpha(x, y, 0);
        }

        int[] pixels = new int[srcWidth];
        byte[] alphas = null;
        for (int startX = 0; startX < width; startX += srcWidth) {
            int length = Math.min(srcWidth, width - startX);
            if (length > 0) {
                for (int startY = 0; startY < height; startY += srcHeight) {
                    for (int y = 0; y < srcHeight && startY + y < height; y++) {
                        srcData.getPixels(0, y, srcWidth, pixels, 0);
                        destData.setPixels(startX, startY + y, length, pixels,
                                0);
                        if (srcData.alpha == -1 && srcData.alphaData != null) {
                            if (alphas == null)
                                alphas = new byte[srcWidth];
                            srcData.getAlphas(0, y, srcWidth, alphas, 0);
                        } else if (srcData.alpha != -1 && alphas == null) {
                            alphas = new byte[srcWidth];
                            for (int i = 0; i < alphas.length; i++)
                                alphas[i] = (byte) srcData.alpha;
                        } else if (alphas == null) {
                            alphas = new byte[srcWidth];
                            for (int i = 0; i < alphas.length; i++)
                                alphas[i] = (byte) 0xff;
                        }
                        destData.setAlphas(startX, startY + y, length, alphas,
                                0);
                    }
                }
            }
        }

        Image image = new Image(display, destData);
        return image;
    }

}
