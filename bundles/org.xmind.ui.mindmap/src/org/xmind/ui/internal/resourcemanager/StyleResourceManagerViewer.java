package org.xmind.ui.internal.resourcemanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.SWTGraphics;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.handlers.IHandlerService;
import org.xmind.core.style.IStyle;
import org.xmind.core.style.IStyleSheet;
import org.xmind.gef.EditDomain;
import org.xmind.gef.GEF;
import org.xmind.gef.IViewer;
import org.xmind.gef.part.IPart;
import org.xmind.gef.tool.ITool;
import org.xmind.gef.util.Properties;
import org.xmind.ui.gallery.GalleryEditTool;
import org.xmind.ui.gallery.GallerySelectTool;
import org.xmind.ui.gallery.GalleryViewer;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.handlers.IMindMapCommandConstants;
import org.xmind.ui.internal.views.StyleFigure;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.texteditor.FloatingTextEditor;
import org.xmind.ui.viewers.IToolTipProvider;

public class StyleResourceManagerViewer extends ResourceManagerViewer {

    private static final int FRAME_HEIGHT = 72;

    private static final int FRAME_WIDTH = 132;

    //FIXME how to dispose cachedImageData???
    public static Map<String, ImageData> cachedImageData = new HashMap<String, ImageData>();

    private static final String EDIT_STYLE_COMMAND_ID = "org.xmind.ui.command.style.edit2"; //$NON-NLS-1$

    private class StyleGalleryViewer extends GalleryViewer {
        protected boolean isTitleEditable(IPart p) {
            IStyleSheet styleSheet = MindMapUI.getResourceManager()
                    .getUserStyleSheet();
            return styleSheet == null ? false
                    : styleSheet.getAllStyles().contains(p.getModel());
        }
    }

    private static class StyleGroup {
        public StyleGroup(String type) {
            this.type = type;
        }

        private String type;
        private List<IStyle> styles = new ArrayList<IStyle>();

        public String getType() {
            return type;
        }

        public String getName() {
            if (IStyle.TOPIC.equals(type))
                return MindMapMessages.StyleResourceManagerViewer_Topic;
            if (IStyle.BOUNDARY.equals(type))
                return MindMapMessages.StyleResourceManagerViewer_Boundary;
            if (IStyle.MAP.equals(type))
                return MindMapMessages.StyleResourceManagerViewer_Map;
            if (IStyle.PARAGRAPH.equals(type))
                return MindMapMessages.StyleResourceManagerViewer_Paragraph;
            if (IStyle.RELATIONSHIP.equals(type))
                return MindMapMessages.StyleResourceManagerViewer_Relationship;
            if (IStyle.SUMMARY.equals(type))
                return MindMapMessages.StyleResourceManagerViewer_Summary;
            if (IStyle.TEXT.equals(type))
                return MindMapMessages.StyleResourceManagerViewer_Text;
            if (IStyle.THEME.equals(type))
                return MindMapMessages.StyleResourceManagerViewer_Theme;
            return ""; //$NON-NLS-1$
        }

        public List<IStyle> getItems() {
            return styles;
        }

        public void addStyle(IStyle style) {
            styles.add(style);
        }

        public int hashCode() {
            return type.hashCode();
        }

        public boolean equals(Object obj) {
            if (obj == this)
                return true;
            if (obj == null || !(obj instanceof StyleGroup))
                return false;
            if (((StyleGroup) obj).type.equals(this.type))
                return true;
            return false;
        }
    }

    private static class StyleGalleryCore {
        private List<StyleGroup> groups = new ArrayList<StyleGroup>();
        private static StyleGalleryCore instance = new StyleGalleryCore();

        public StyleGalleryCore() {
        }

        private StyleGroup getGroup(IStyle style) {
            String type = style.getType();
            if (type != null)
                for (StyleGroup group : groups) {
                    if (type.equals(group.type))
                        return group;
                }

            StyleGroup group = new StyleGroup(type);
            groups.add(group);
            return group;
        }

        private List<StyleGroup> getElements() {
            groups.clear();
            Set<IStyle> systemStyles = MindMapUI.getResourceManager()
                    .getSystemStyleSheet()
                    .getStyles(IStyleSheet.AUTOMATIC_STYLES);
            Set<IStyle> userStyles = MindMapUI.getResourceManager()
                    .getUserStyleSheet().getAllStyles();

            for (IStyle style : systemStyles) {
                StyleGroup group = getGroup(style);
                group.addStyle(style);
            }
            for (IStyle style : userStyles) {
                StyleGroup group = getGroup(style);
                group.addStyle(style);
            }
            return groups;
        }

        public static StyleGalleryCore getInstance() {
            return instance;
        }
    }

    private class StyleCategorizedLabelProvider extends CategorizedLabelProvider
            implements IToolTipProvider, IFontProvider {

        @Override
        public String getText(Object element) {
            if (element instanceof IStyle) {
                IStyle style = (IStyle) element;
                return style.getName();
            } else if (element instanceof StyleGroup)
                return ((StyleGroup) element).getName();
            return super.getText(element);
        }

        @Override
        public Image getImage(Object element) {
            if (element instanceof IStyle) {
                final IStyle style = (IStyle) element;
                Properties properties = StyleResourceManagerViewer.this
                        .getProperties();

                ImageData imageData = cachedImageData.get(style.getId());
                if (imageData == null) {
                    final Dimension size = (Dimension) properties
                            .get(GalleryViewer.FrameContentSize);
                    Image image = new Image(Display.getDefault(), size.width,
                            size.height);
                    GC gc = new GC(image);
                    Graphics graphics = new SWTGraphics(gc);
                    StyleFigure figure = new StyleFigure();
                    figure.setStyle(style);
                    figure.setSize(size);
                    figure.paint(graphics);

                    imageData = image.getImageData();
                    cachedImageData.put(style.getId(), imageData);
                    image.dispose();
                    gc.dispose();
                    graphics.dispose();
                }

                ImageDescriptor imageDescriptor = ImageDescriptor
                        .createFromImageData(imageData);
                return getResourceManager().createImage(imageDescriptor);
            }
            return super.getImage(element);
        }

        public String getToolTip(Object element) {

            if (element != null && element instanceof IStyle) {
                IStyle style = (IStyle) element;
                String typeName = style.getType();
                String styleName = getText(style);
                return typeName + "-" + styleName; //$NON-NLS-1$
            }

            return ""; //$NON-NLS-1$
        }

        @Override
        public Font getFont(Object element) {

            FontData data = getContainer().getFont().getFontData()[0];
            if (Util.isMac()) {
                data.setHeight(12);
            } else {
                data.setHeight(9);
            }
            data.setStyle(SWT.NONE);

            FontDescriptor fontDescriptor = FontDescriptor.createFrom(data);

            return getResourceManager().createFont(fontDescriptor);
        }
    }

    private static class StyleCategorizedContentProvider
            implements ITreeContentProvider {

        public void dispose() {

        }

        public void inputChanged(Viewer viewer, Object oldInput,
                Object newInput) {

        }

        public Object[] getElements(Object inputElement) {
            if (inputElement instanceof StyleGalleryCore)
                return ((StyleGalleryCore) inputElement).getElements()
                        .toArray();
            else
                return null;
        }

        public Object[] getChildren(Object parentElement) {
            if (parentElement instanceof StyleGalleryCore)
                return ((StyleGalleryCore) parentElement).getElements()
                        .toArray();
            else if (parentElement instanceof StyleGroup)
                return ((StyleGroup) parentElement).getItems().toArray();
            else
                return null;
        }

        public Object getParent(Object element) {
            StyleGalleryCore styleGalleryCore = StyleGalleryCore.getInstance();
            if (element instanceof IStyle)
                return styleGalleryCore.getGroup((IStyle) element);
            else if (element instanceof StyleGroup)
                return styleGalleryCore;
            else
                return null;
        }

        public boolean hasChildren(Object element) {
            return element instanceof StyleGroup
                    || element instanceof StyleGalleryCore;
        }

    }

    private class StyleNestedViewerNameEditTool extends GalleryEditTool {

        protected IDocument getTextContents(IPart source) {
            return new org.eclipse.jface.text.Document(
                    ((IStyle) source.getModel()).getName());
        }

        protected void handleTextModified(IPart source, IDocument document) {
            //FIXME
            ((IStyle) source.getModel()).setName(document.get());
            MindMapUI.getResourceManager().saveUserStyleSheet();
        }

        protected void hookEditor(FloatingTextEditor editor) {
            super.hookEditor(editor);
            getHelper().setPrefWidth(
                    FRAME_WIDTH + DEFAULT_FLOATING_TEXT_EDITOR_WIDTH_EXPAND);
        }

    }

    @Override
    public void createControl(Composite container) {
        super.createControl(container);
        setContentProvider(new StyleCategorizedContentProvider());
        setLabelProvider(new StyleCategorizedLabelProvider());
        EditDomain domain = new EditDomain();
        domain.installTool(GEF.TOOL_SELECT,
                new ResourceCategorizedSelectTool());
        setEditDomain(domain);
        initProperties();
        createControl(container, SWT.WRAP);
        getControl()
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        setInput(StyleGalleryCore.getInstance());

    }

    @Override
    protected void initNestedGalleryViewer(GalleryViewer galleryViewerer) {
        super.initNestedGalleryViewer(galleryViewerer);
        Properties properties = galleryViewerer.getProperties();
        properties.set(GalleryViewer.HideTitle, Boolean.FALSE);
        galleryViewerer.getEditDomain().installTool(GEF.TOOL_EDIT,
                new StyleNestedViewerNameEditTool());
    }

    @Override
    protected void initProperties() {
        super.initProperties();
        Properties properties = getProperties();
        properties.set(GalleryViewer.FrameContentSize,
                new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
    }

    @Override
    protected void configureSection(Section section, Object category) {
        if (category instanceof StyleGroup) {
            createSectionTextClient(section,
                    MindMapMessages.StyleResourceManagerViewer_AddSection_title,
                    category);
        }
    }

    @Override
    protected void handleClickSectionTextClient(Object category) {
        if (category instanceof StyleGroup) {
            StyleGroup group = (StyleGroup) category;

            final IStyle dummyStyle = MindMapUI.getResourceManager()
                    .getSystemStyleSheet().createStyle(group.type);
            openStyleEditDialog(dummyStyle);
        }
    }

    protected void editStyle() {
        IStructuredSelection selection = (IStructuredSelection) getSelection();
        for (Object obj : selection.toArray()) {
            if (obj instanceof IStyle) {
                openStyleEditDialog((IStyle) obj);
            }
        }
    }

    private void openStyleEditDialog(final IStyle style) {
        IWorkbenchWindow window = PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow();
        final IHandlerService handlers = (IHandlerService) window
                .getService(IHandlerService.class);
        final ICommandService commands = (ICommandService) window
                .getService(ICommandService.class);
        if (handlers == null || commands == null)
            return;

        final Command command = commands.getCommand(EDIT_STYLE_COMMAND_ID);
        if (command == null || !command.isDefined())
            return;

        SafeRunner.run(new SafeRunnable() {
            public void run() throws Exception {
                IParameter param = command
                        .getParameter(IMindMapCommandConstants.RESOURCE_URI);
                if (param == null)
                    return;

                ParameterizedCommand pc = new ParameterizedCommand(command,
                        new Parameterization[] { new Parameterization(param,
                                MindMapUI.getResourceManager()
                                        .toResourceURI(style)) });
                handlers.executeCommand(pc, null);
            }
        });
    }

    public void startEditing(IStyle style) {
        List<StyleGroup> styleGroups = StyleGalleryCore.getInstance()
                .getElements();
        for (StyleGroup styleGroup : styleGroups) {
            List<IStyle> styles = styleGroup.getItems();
            if (styles.contains(style)) {
                GalleryViewer galleryViewer = getNestedViewer(styleGroup);
                EditDomain domain = galleryViewer.getEditDomain();
                ITool tool = domain.getDefaultTool();

                ((GallerySelectTool) tool).getStatus().setStatus(GEF.ST_ACTIVE,
                        true);
                domain.handleRequest(GEF.REQ_EDIT, (IViewer) galleryViewer);
                break;
            }
        }
    }

    protected GalleryViewer createNestedViewer() {
        return new StyleGalleryViewer();
    }

}
