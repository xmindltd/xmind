package org.xmind.ui.internal.views;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.draw2d.AbstractHintLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.xmind.core.Core;
import org.xmind.core.IBoundary;
import org.xmind.core.IControlPoint;
import org.xmind.core.IRelationship;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventRegistration;
import org.xmind.core.event.ICoreEventSource;
import org.xmind.core.internal.command.Logger;
import org.xmind.core.io.ByteArrayStorage;
import org.xmind.core.io.IStorage;
import org.xmind.core.style.IStyle;
import org.xmind.core.style.IStyleSheet;
import org.xmind.core.util.FileUtils;
import org.xmind.gef.draw2d.SizeableImageFigure;
import org.xmind.gef.image.ImageExportUtils;
import org.xmind.gef.image.ResizeConstants;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.gef.util.Properties;
import org.xmind.ui.gallery.GalleryViewer;
import org.xmind.ui.gallery.IDecorationContext;
import org.xmind.ui.gallery.ILabelDecorator;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.IMindMapViewer;
import org.xmind.ui.mindmap.MindMap;
import org.xmind.ui.mindmap.MindMapImageExporter;
import org.xmind.ui.mindmap.MindMapUI;

public class ThemeLabelProvider extends LabelProvider
        implements ILabelDecorator {

    private static final String THEME_PREVIEWS_DIR = ".themePreviews"; //$NON-NLS-1$
    // All images are made the same size
    private static final int IMAGE_WIDTH = 800;
    private static final int IMAGE_HEIGHT = 400;

    // Makes background layer large enough so that it can fill the image
    private static final int BACKGROUND_ENLARGEMENT_MARGINS = 400;
    private static Image defaultImage;

    private static class ThemePreviewImageProviderManager
            implements ICoreEventListener, ISchedulingRule {

        public static final ThemePreviewImageProviderManager INSTANCE = new ThemePreviewImageProviderManager();

        private Map<String, Map<IStyle, Image>> imageGroups = new HashMap<String, Map<IStyle, Image>>();

        private Map<String, Map<IStyle, ThemePreviewImageLoader>> loaderGroups = new HashMap<String, Map<IStyle, ThemePreviewImageLoader>>();

//        private Map<IStyle, Image> images = new HashMap<IStyle, Image>();
//
//        private Map<IStyle, ThemePreviewImageLoader> loaders = new HashMap<IStyle, ThemeLabelProvider.ThemePreviewImageLoader>();

        private ListenerList listeners = new ListenerList();

        private ICoreEventRegistration themeRemovalEventReg = null;

        private IJobChangeListener loaderListener = new JobChangeAdapter() {
            @Override
            public void done(IJobChangeEvent event) {
                super.done(event);
                ThemePreviewImageLoader loader = (ThemePreviewImageLoader) event
                        .getJob();
                Map<IStyle, ThemePreviewImageLoader> loaderGroup = loaderGroups
                        .get(loader.getStructureClass());
                if (loaderGroup != null && !loaderGroup.isEmpty())
                    loaderGroup.remove(loader.getTheme());
                Image image = loader.getPreviewImage();
                if (image != null) {
                    setPreviewImage(loader.getTheme(),
                            loader.getStructureClass(), image);
                }
            }
        };

        private ThemePreviewImageProviderManager() {
            checkLocalLanguage();
        }

        private void checkLocalLanguage() {
            String lang = System.getProperty("osgi.nl"); //$NON-NLS-1$
            File root = MindMapUIPlugin.getDefault().getStateLocation()
                    .toFile();
            File cacheDir = new File(root, THEME_PREVIEWS_DIR);
            FileUtils.ensureDirectory(cacheDir);

            File langFile = new File(cacheDir, lang);
            if (!langFile.exists()) {
                File[] files = cacheDir.listFiles();
                for (File file : files) {
                    if (file.exists()) {
                        file.delete();
                    }
                }
                try {
                    langFile.createNewFile();
                } catch (IOException e) {
                }
            }
        }

        public void addThemePreviewImageListener(ThemeLabelProvider listener) {
            boolean wasEmpty = listeners.isEmpty();
            listeners.add(listener);
            boolean isEmpty = listeners.isEmpty();
            if (wasEmpty && !isEmpty) {
                if (themeRemovalEventReg != null) {
                    themeRemovalEventReg.unregister();
                }
                themeRemovalEventReg = ((ICoreEventSource) MindMapUI
                        .getResourceManager().getUserThemeSheet())
                                .registerCoreEventListener(Core.StyleRemove,
                                        this);
            }
        }

        public void removeThemePreviewImageListener(
                ThemeLabelProvider listener) {
            boolean wasEmpty = listeners.isEmpty();
            listeners.remove(listener);
            boolean isEmpty = listeners.isEmpty();
            if (!wasEmpty && isEmpty) {
                if (themeRemovalEventReg != null) {
                    themeRemovalEventReg.unregister();
                    themeRemovalEventReg = null;
                }
            }
        }

        private void fireThemePreviewImageChanged(final IStyle theme) {
            for (final Object listener : listeners.getListeners()) {
                SafeRunner.run(new SafeRunnable() {
                    public void run() throws Exception {
                        ((ThemeLabelProvider) listener)
                                .themePreviewImageChanged(theme);
                    }
                });
            }
        }

        public void handleCoreEvent(CoreEvent event) {
            if (Core.StyleRemove.equals(event.getType())) {
                IStyle theme = (IStyle) event.getTarget();
                deletePreviewImageCacheForTheme(theme);
            }
        }

        public Image getThemePreviewImage(IStyle theme, String structureClass) {
            Image image = null;

            Map<IStyle, Image> imageGroup = imageGroups.get(structureClass);
            if (imageGroup != null)
                image = imageGroup.get(theme);
            if (image != null)
                return image;

            Map<IStyle, ThemePreviewImageLoader> loaderGroup = loaderGroups
                    .get(structureClass);
            if (loaderGroup == null) {
                loaderGroup = new HashMap<IStyle, ThemeLabelProvider.ThemePreviewImageLoader>();
                loaderGroups.put(structureClass, loaderGroup);
            }
            if (!loaderGroup.containsKey(theme)) {
//            if (!loaders.containsKey(theme)) {
                ThemePreviewImageLoader loader = new ThemePreviewImageLoader(
                        theme, structureClass, Display.getCurrent());
                loader.setRule(this);
                loader.setSystem(true);
                loader.addJobChangeListener(loaderListener);
                loaderGroup.put(theme, loader);
                MindMapUIPlugin.getDefault().registerJob(loader);
                loader.schedule();
            }

            return null;
        }

        private void deletePreviewImageCacheForTheme(IStyle theme) {
            for (String structureClass : imageGroups.keySet()) {
                File cacheFile = getPreviewImageCacheFileForTheme(theme,
                        structureClass);
                if (cacheFile.exists()) {
                    cacheFile.delete();
                }

                deletePreviewImageCacheInMemory(theme, structureClass);
            }
        }

        private void deletePreviewImageCacheInMemory(final IStyle theme,
                String structureClass) {
            Display display = null;

//            final Image image = images.remove(theme);
            Map<IStyle, Image> imageGroup = imageGroups.get(structureClass);
            if (imageGroup != null && !imageGroup.isEmpty()) {
                final Image image = imageGroup.remove(theme);
                if (image != null) {
                    display = (Display) image.getDevice();
                    if (!display.isDisposed()) {
                        display.asyncExec(new Runnable() {
                            public void run() {
                                Display display = Display.getCurrent();
                                if (!display.isDisposed()) {
                                    display.asyncExec(new Runnable() {
                                        public void run() {
                                            image.dispose();
                                        }
                                    });
                                }
                            }
                        });
                    }
                }

            }

//            final ThemePreviewImageLoader loader = loaders.remove(theme);
            Map<IStyle, ThemePreviewImageLoader> loaderGroup = loaderGroups
                    .get(structureClass);
            if (loaderGroup != null && !loaderGroup.isEmpty()) {
                final ThemePreviewImageLoader loader = loaderGroup
                        .remove(theme);
                if (loader != null) {
                    loader.cancel();
                }

                if (display != null && !display.isDisposed()) {
                    display.syncExec(new Runnable() {

                        public void run() {
                            fireThemePreviewImageChanged(theme);
                        }
                    });
                }
            }
        }

        private void setPreviewImage(final IStyle theme, String structureClass,
                Image image) {
            Display display = null;
            Map<IStyle, Image> imageGroup = imageGroups.get(structureClass);
            if (imageGroup == null) {
                imageGroup = new HashMap<IStyle, Image>();
                imageGroups.put(structureClass, imageGroup);
            }
            final Image oldImage = imageGroup.put(theme, image);
            if (oldImage != null) {
                display = (Display) oldImage.getDevice();
                if (!display.isDisposed()) {
                    display.asyncExec(new Runnable() {
                        public void run() {
                            Display display = Display.getCurrent();
                            if (!display.isDisposed()) {
                                display.asyncExec(new Runnable() {
                                    public void run() {
                                        oldImage.dispose();
                                    }
                                });
                            }
                        }
                    });
                }
            }

            display = (Display) image.getDevice();
            if (!display.isDisposed()) {
                display.syncExec(new Runnable() {

                    public void run() {
                        fireThemePreviewImageChanged(theme);
                    }

                });
            }
        }

        public boolean contains(ISchedulingRule rule) {
            return rule == this;
        }

        public boolean isConflicting(ISchedulingRule rule) {
            return rule == this;
        }

    }

    private static class ThemePreviewImageLoader extends Job {

        private IStyle theme;

        private String structureClass;

        private Display display;

        private Image previewImage;

        public ThemePreviewImageLoader(IStyle theme, String structureClass,
                Display display) {
            super(NLS.bind(MindMapMessages.ThemeLabel_LoadTheme,
                    theme.getName()));
            this.theme = theme;
            this.structureClass = structureClass;
            this.display = display;
            this.previewImage = null;
        }

        @Override
        protected IStatus run(IProgressMonitor monitor) {
            Image image = null;

            // If cached in file, load preview image from file.
            File cacheFile = getPreviewImageCacheFile();
            if (cacheFile.exists()) {
                try {
                    image = new Image(display, cacheFile.getAbsolutePath());
                } catch (Throwable error) {
                    Logger.log(
                            "Failed to load cached theme preview image from file " //$NON-NLS-1$
                                    + cacheFile.getAbsolutePath(),
                            error);
                    cacheFile.delete();
                }
            }

            if (monitor.isCanceled()) {
                if (image != null)
                    image.dispose();
                return Status.CANCEL_STATUS;
            }

            // If not cached in file, make a preview image and save it to cache file.
            if (image == null) {
                try {
                    image = createPreviewImage();
                } catch (Throwable fatalError) {
                    return new Status(IStatus.ERROR, MindMapUIPlugin.PLUGIN_ID,
                            "Failed to create preview image for theme (" //$NON-NLS-1$
                                    + theme.getName() + ")", //$NON-NLS-1$
                            fatalError);
                }
                if (image != null) {
                    if (monitor.isCanceled()) {
                        image.dispose();
                        return Status.CANCEL_STATUS;
                    }

                    FileUtils.ensureFileParent(cacheFile);
                    try {
                        OutputStream out = new FileOutputStream(cacheFile);
                        try {
                            ImageExportUtils.saveImage(image, out,
                                    SWT.IMAGE_PNG);
                        } finally {
                            out.close();
                        }
                    } catch (IOException error) {
                        Logger.log(
                                "Failed to save theme preview image to cache file " //$NON-NLS-1$
                                        + cacheFile.getAbsolutePath(),
                                error);
                    }
                }
            }

            if (image != null) {
                if (monitor.isCanceled()) {
                    image.dispose();
                    cacheFile.delete();
                    return Status.CANCEL_STATUS;
                }

                this.previewImage = image;
            }

            return Status.OK_STATUS;
        }

        private Image createPreviewImage() {
            return createPreviewImageForTheme(theme, structureClass, display);
        }

        private File getPreviewImageCacheFile() {
            return getPreviewImageCacheFileForTheme(theme, structureClass);
        }

        public Image getPreviewImage() {
            return previewImage;
        }

        public IStyle getTheme() {
            return theme;
        }

        public String getStructureClass() {
            return structureClass;
        }

    }

    private static class Layout extends AbstractHintLayout {

        private IDecorationContext properties;

        public Layout(IDecorationContext properties) {
            this.properties = properties;
        }

        public void layout(IFigure container) {
            Rectangle area = container.getClientArea();
            for (Object child : container.getChildren()) {
                IFigure figure = (IFigure) child;
                Dimension childSize = figure.getPreferredSize(-1, -1);
                int childWidth = Math.min(area.width, childSize.width);
                int childHeight = Math.min(area.height, childSize.height);
                figure.setBounds(
                        new Rectangle(area.x, area.y, childWidth, childHeight));
            }
        }

        @Override
        protected Dimension calculatePreferredSize(IFigure figure, int wHint,
                int hHint) {
            if (wHint > -1)
                wHint = Math.max(0, wHint - figure.getInsets().getWidth());
            if (hHint > -1)
                hHint = Math.max(0, hHint - figure.getInsets().getHeight());

            Insets insets = figure.getInsets();
            Dimension contentSize = (Dimension) properties
                    .getProperty(GalleryViewer.FrameContentSize, null);
            if (contentSize != null)
                return new Dimension(contentSize.width + insets.getWidth(),
                        contentSize.height + insets.getHeight());
            Dimension d = new Dimension();
            List children = figure.getChildren();
            IFigure child;
            for (int i = 0; i < children.size(); i++) {
                child = (IFigure) children.get(i);
                if (!isObservingVisibility() || child.isVisible())
                    d.union(child.getPreferredSize(wHint, hHint));
            }

            d.expand(figure.getInsets().getWidth(),
                    figure.getInsets().getHeight());
            d.union(getBorderPreferredSize(figure));
            return d;
        }

    }

    private String structureClass;

    public ThemeLabelProvider() {
        ThemePreviewImageProviderManager.INSTANCE
                .addThemePreviewImageListener(this);
    }

    public ThemeLabelProvider(String structureClass) {
        this.structureClass = structureClass;
        ThemePreviewImageProviderManager.INSTANCE
                .addThemePreviewImageListener(this);
    }

    @Override
    public String getText(Object element) {
        if (element instanceof IStyle
                && IStyle.THEME.equals(((IStyle) element).getType())) {
            return ((IStyle) element).getName();
        }
        return super.getText(element);
    }

    @Override
    public Image getImage(Object element) {
        if (element instanceof IStyle
                && IStyle.THEME.equals(((IStyle) element).getType())) {

            return ThemePreviewImageProviderManager.INSTANCE
                    .getThemePreviewImage((IStyle) element,
                            (structureClass == null
                                    || "".equals(structureClass)) //$NON-NLS-1$
                                            ? getCurrentCentralStructure()
                                            : structureClass);
        }
        return super.getImage(element);
    }

    @Override
    public void dispose() {
        ThemePreviewImageProviderManager.INSTANCE
                .removeThemePreviewImageListener(this);
        super.dispose();
    }

    private void themePreviewImageChanged(IStyle theme) {
        fireLabelProviderChanged(new LabelProviderChangedEvent(this, theme));
    }

    private static File getPreviewImageCacheFileForTheme(IStyle theme,
            String structureClass) {
        File root = MindMapUIPlugin.getDefault().getStateLocation().toFile();
        File cacheDir = new File(root, THEME_PREVIEWS_DIR);
        String themeId = theme.getId();
        String parentId;
        IStyleSheet sheet = theme.getOwnedStyleSheet();
        if (sheet == MindMapUI.getResourceManager().getSystemThemeSheet()) {
            parentId = "system"; //$NON-NLS-1$
        } else
            if (sheet == MindMapUI.getResourceManager().getUserThemeSheet()) {
            parentId = "user"; //$NON-NLS-1$
        } else {
            parentId = "other"; //$NON-NLS-1$
        }
        String fileName = String.format("%s-%s-%s.png", parentId, //$NON-NLS-1$
                structureClass, themeId);
        return new File(cacheDir, fileName);
    }

    private static Image createPreviewImageForTheme(IStyle theme,
            String structureClass, Display display) {
        IWorkbook workbook = createTemplateWorkbook(structureClass);
        IStyle appliedTheme = workbook.getStyleSheet().importStyle(theme);
        if (appliedTheme != null) {
            workbook.getPrimarySheet().setThemeId(appliedTheme.getId());
        }

        final MindMapImageExporter exporter = new MindMapImageExporter(display);

        // Enlarge viewer margins to make background large enough
        Properties props = new Properties();
        props.set(IMindMapViewer.VIEWER_MARGIN, BACKGROUND_ENLARGEMENT_MARGINS);
        exporter.setSource(new MindMap(workbook.getPrimarySheet()), props,
                new Insets(MindMapUI.DEFAULT_EXPORT_MARGIN));
        exporter.setResize(ResizeConstants.RESIZE_STRETCH, IMAGE_WIDTH,
                IMAGE_HEIGHT);

        final Image[] image = new Image[1];
        if (!display.isDisposed()) {
            display.syncExec(new Runnable() {
                public void run() {
                    image[0] = exporter.createImage();
                }
            });
        }

        return image[0];
    }

    private static IWorkbook createTemplateWorkbook(String structureClass) {
        IStorage tempStorage = new ByteArrayStorage();
        IWorkbook workbook = Core.getWorkbookBuilder()
                .createWorkbook(tempStorage);
        ISheet sheet = workbook.getPrimarySheet();

        ITopic rootTopic = sheet.getRootTopic();
        rootTopic.setTitleText(MindMapMessages.TitleText_CentralTopic);
        rootTopic.setStructureClass(
                (structureClass == null || "".equals(structureClass)) //$NON-NLS-1$
                        ? "org.xmind.ui.map.clockwise" : structureClass); //$NON-NLS-1$

        ITopic mainTopic1 = workbook.createTopic();
        mainTopic1
                .setTitleText(NLS.bind(MindMapMessages.TitleText_MainTopic, 1));
        rootTopic.add(mainTopic1);

        ITopic subTopic1 = workbook.createTopic();
        subTopic1.setTitleText(NLS.bind(MindMapMessages.TitleText_Subtopic, 1));
        mainTopic1.add(subTopic1);

        ITopic subTopic2 = workbook.createTopic();
        subTopic2.setTitleText(NLS.bind(MindMapMessages.TitleText_Subtopic, 2));
        mainTopic1.add(subTopic2);

        ITopic subTopic3 = workbook.createTopic();
        subTopic3.setTitleText(NLS.bind(MindMapMessages.TitleText_Subtopic, 3));
        mainTopic1.add(subTopic3);

        ITopic floatingTopic = workbook.createTopic();
        floatingTopic.setTitleText(MindMapMessages.TitleText_FloatingTopic);
        floatingTopic.setPosition(0, -120);
        rootTopic.add(floatingTopic, ITopic.DETACHED);

        IBoundary boundary = workbook.createBoundary();
        boundary.setTitleText(MindMapMessages.TitleText_Boundary);
        boundary.setStartIndex(0);
        boundary.setEndIndex(0);
        rootTopic.addBoundary(boundary);

        IRelationship relationship = workbook.createRelationship();
        relationship.setTitleText(MindMapMessages.TitleText_Relationship);
        relationship.setEnd1Id(mainTopic1.getId());
        relationship.setEnd2Id(floatingTopic.getId());
        IControlPoint cp1 = relationship.getControlPoint(0);
        cp1.setPosition(50, -100);
        IControlPoint cp2 = relationship.getControlPoint(1);
        cp2.setPosition(100, 0);
        sheet.addRelationship(relationship);

        return workbook;
    }

    public IFigure decorateFigure(IFigure figure, Object element,
            IDecorationContext context) {
        List children = figure.getChildren();
        boolean needInitFigureContent = children.isEmpty();
        if (needInitFigureContent) {
            SizeableImageFigure themeContentFigure = new SizeableImageFigure(
                    getImage(element));
            SizeableImageFigure defaultImageFigure = new SizeableImageFigure(
                    getDefaultImage());
            figure.add(themeContentFigure);
            figure.add(defaultImageFigure);

            if (context != null) {
                figure.setLayoutManager(new Layout(context));
                boolean imageConstrained = Boolean.TRUE.equals(context
                        .getProperty(GalleryViewer.ImageConstrained, false));
                boolean imageStretched = Boolean.TRUE.equals(context
                        .getProperty(GalleryViewer.ImageStretched, false));
                themeContentFigure.setConstrained(imageConstrained);
                themeContentFigure.setStretched(imageStretched);
                defaultImageFigure.setConstrained(imageConstrained);
                defaultImageFigure.setStretched(imageStretched);
            }
        }

        int supportSize = 2;
        children = figure.getChildren();
        if (children.size() == supportSize) {
            Object themeContentFigure = children.get(0);
            Object defaultImageFigure = children.get(supportSize - 1);
            if (themeContentFigure instanceof SizeableImageFigure
                    && defaultImageFigure instanceof SizeableImageFigure) {
                ((SizeableImageFigure) themeContentFigure)
                        .setImage(getImage(element));

                IStyle defaultTheme = MindMapUI.getResourceManager()
                        .getDefaultTheme();
                ((SizeableImageFigure) defaultImageFigure).setImage(
                        element == defaultTheme ? getDefaultImage() : null);
            }
        }
        return figure;
    }

    private static Image getDefaultImage() {
        if (defaultImage == null) {
            ImageDescriptor desc = MindMapUI.getImages()
                    .get(IMindMapImages.STAR, true);
            if (desc != null) {
                try {
                    defaultImage = desc.createImage(false);
                } catch (Throwable e) {
                    //e.printStackTrace();
                }
            }
        }
        return defaultImage;
    }

    private String getCurrentCentralStructure() {
        if (this.structureClass != null && !"".equals(this.structureClass)) //$NON-NLS-1$
            return this.structureClass;

        String defaultStructureClass = "org.xmind.ui.map.clockwise"; //$NON-NLS-1$
        IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow();
        if (activeWorkbenchWindow == null)
            return defaultStructureClass;

        IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
        if (activePage == null)
            return defaultStructureClass;

        IEditorPart activeEditor = activePage.getActiveEditor();
        if (activeEditor == null || !(activeEditor instanceof IGraphicalEditor))
            return defaultStructureClass;

        IGraphicalEditorPage page = ((IGraphicalEditor) activeEditor)
                .getActivePageInstance();
        if (page == null)
            return defaultStructureClass;

        ISheet sheet = (ISheet) page.getAdapter(ISheet.class);
        if (sheet == null)
            return defaultStructureClass;

        ITopic topic = sheet.getRootTopic();
        return topic.getStructureClass();
    }
}
