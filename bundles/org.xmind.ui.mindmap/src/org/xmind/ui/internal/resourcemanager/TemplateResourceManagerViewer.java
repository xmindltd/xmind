package org.xmind.ui.internal.resourcemanager;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.URIUtil;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.xmind.core.util.FileUtils;
import org.xmind.gef.EditDomain;
import org.xmind.gef.GEF;
import org.xmind.gef.IViewer;
import org.xmind.gef.draw2d.SizeableImageFigure;
import org.xmind.gef.part.IPart;
import org.xmind.gef.tool.ITool;
import org.xmind.gef.util.Properties;
import org.xmind.ui.gallery.GalleryEditTool;
import org.xmind.ui.gallery.GallerySelectTool;
import org.xmind.ui.gallery.GalleryViewer;
import org.xmind.ui.gallery.IDecorationContext;
import org.xmind.ui.gallery.ILabelDecorator;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.TemplateGroup;
import org.xmind.ui.internal.wizards.TemplateLabelProvider;
import org.xmind.ui.mindmap.IResourceManager;
import org.xmind.ui.mindmap.ITemplate;
import org.xmind.ui.mindmap.ITemplateGroup;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.texteditor.FloatingTextEditor;

public class TemplateResourceManagerViewer extends ResourceManagerViewer {

    private static final int FRAME_WIDTH = 132;
    private static final int FRAME_HEIGHT = 72;

    private class TemplateGalleryViewer extends GalleryViewer {
        protected boolean isTitleEditable(IPart p) {
            List<ITemplate> userTemplates = MindMapUI.getResourceManager()
                    .getUserTemplates();
            return userTemplates.contains(p.getModel());
        }
    }

    static class TemplateGalleryCore {

        static final String SYSTEM_GROUP_NAME = MindMapMessages.TemplateResourceManagerViewer_SystemGroup_name;
        static final String USER_GROUP_NAME = MindMapMessages.TemplateResourceManagerViewer_UserGroup_name;

        private static TemplateGalleryCore instance = new TemplateGalleryCore();
        List<ITemplateGroup> groups = new ArrayList<ITemplateGroup>();

        public static TemplateGalleryCore getInstance() {
            return instance;
        }

        private TemplateGalleryCore() {

        }

        public List<ITemplateGroup> getElements() {
            groups.clear();
            IResourceManager resourceManager = MindMapUI.getResourceManager();

            groups.addAll(resourceManager.getSystemTemplateGroups());

            ITemplateGroup userGroup = new TemplateGroup(USER_GROUP_NAME,
                    resourceManager.getUserTemplates());
            if (!(userGroup.getTemplates().isEmpty()))
                groups.add(userGroup);
            return groups;
        }

        public ITemplateGroup getGroupByName(String name) {
            for (ITemplateGroup group : groups)
                if (group.getName().equals(name))
                    return group;
            return null;
        }
    }

    private static class TemplateCategorizedContentProvider
            implements ITreeContentProvider {

        public void dispose() {

        }

        public void inputChanged(Viewer viewer, Object oldInput,
                Object newInput) {

        }

        public Object[] getElements(Object inputElement) {
            if (inputElement instanceof TemplateGalleryCore)
                return ((TemplateGalleryCore) inputElement).getElements()
                        .toArray();
            else
                return null;
        }

        @SuppressWarnings("unchecked")
        public Object[] getChildren(Object parentElement) {
            if (parentElement instanceof TemplateGalleryCore)
                return ((TemplateGalleryCore) parentElement).getElements()
                        .toArray();
            else if (parentElement instanceof ITemplateGroup)
                return ((ITemplateGroup) parentElement).getTemplates()
                        .toArray();
            else
                return null;
        }

        public Object getParent(Object element) {
            TemplateGalleryCore templatesGalleryCore = TemplateGalleryCore
                    .getInstance();
            if (element instanceof ITemplate) {
                for (ITemplateGroup group : templatesGalleryCore
                        .getElements()) {
                    if (group.getTemplates().contains(element))
                        return group;
                }
            } else if (element instanceof List)
                return TemplateGalleryCore.getInstance();

            return null;
        }

        public boolean hasChildren(Object element) {
            return element instanceof List
                    || element instanceof TemplateGalleryCore;
        }

    }

    private class TemplateCategorizedLabelProvider extends TemplateLabelProvider
            implements ILabelDecorator, IFontProvider {
        @Override
        public String getText(Object element) {
            if (element instanceof ITemplateGroup)
                return ((ITemplateGroup) element).getName();
            else
                return super.getText(element);
        }

        @Override
        public IFigure decorateFigure(IFigure figure, Object element,
                IDecorationContext context) {
            @SuppressWarnings("rawtypes")
            List children = figure.getChildren();
            boolean needInitFigureContent = children.isEmpty();
            if (needInitFigureContent) {
                SizeableImageFigure contentFigure = new SizeableImageFigure(
                        getImage(element));
                figure.add(contentFigure);

                if (context != null) {
                    figure.setLayoutManager(new Layout(context));
                    boolean imageConstrained = Boolean.TRUE.equals(
                            context.getProperty(GalleryViewer.ImageConstrained,
                                    false));
                    boolean imageStretched = Boolean.TRUE.equals(context
                            .getProperty(GalleryViewer.ImageStretched, false));
                    contentFigure.setConstrained(imageConstrained);
                    contentFigure.setStretched(imageStretched);
                }
            }

            return figure;
        }

        @Override
        protected void fireLabelProviderChanged(
                LabelProviderChangedEvent event) {
            super.fireLabelProviderChanged(event);
            TemplateResourceManagerViewer.this.refresh();
        }

        @Override
        public Font getFont(Object element) {
            FontData fontData = TemplateResourceManagerViewer.this.font
                    .getFontData()[0];
            if (Util.isMac()) {
                fontData.setHeight(12);
            } else {
                fontData.setHeight(9);
            }

            FontDescriptor fontDescriptor = FontDescriptor.createFrom(fontData);

            return getResourceManager().createFont(fontDescriptor);
        }

    }

    private class TemplateNameEditTool extends GalleryEditTool {

        protected IDocument getTextContents(IPart source) {
            return new org.eclipse.jface.text.Document(
                    ((ITemplate) source.getModel()).getName());
        }

        protected void handleTextModified(IPart source, IDocument document) {
            ITemplate template = (ITemplate) source.getModel();
            if (template != null) {
                modifyTemplateName(template, document.get());
            }
        }

        protected void hookEditor(FloatingTextEditor editor) {
            super.hookEditor(editor);
            getHelper().setPrefWidth(FRAME_WIDTH);
        }
    }

    private Font font;

    public TemplateResourceManagerViewer() {
        super();
    }

    @Override
    public void createControl(Composite container) {
        super.createControl(container);
        this.font = container.getFont();

        setContentProvider(new TemplateCategorizedContentProvider());
        setLabelProvider(new TemplateCategorizedLabelProvider());
        EditDomain domain = new EditDomain();
        domain.installTool(GEF.TOOL_SELECT,
                new ResourceCategorizedSelectTool());
        setEditDomain(domain);
        initProperties();
        createControl(container, SWT.WRAP);
        getControl()
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        setInput(TemplateGalleryCore.getInstance());
    }

    @Override
    protected void initProperties() {
        super.initProperties();
        Properties properties = getProperties();
        properties.set(GalleryViewer.FrameContentSize,
                new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
    }

    @Override
    protected void initNestedGalleryViewer(GalleryViewer galleryViewerer) {
        super.initNestedGalleryViewer(galleryViewerer);
        galleryViewerer.getEditDomain().installTool(GEF.TOOL_EDIT,
                new TemplateNameEditTool());
    }

    public void startEditing(ITemplate template) {
        Object input = getInput();
        if (input instanceof TemplateGalleryCore) {
            @SuppressWarnings("unchecked")
            List<ITemplateGroup> groups = (List) getCategories();
            for (ITemplateGroup group : groups) {
                if (group.getTemplates().contains(template)) {
                    GalleryViewer galleryViewer = getNestedViewer(group);
                    EditDomain domain = galleryViewer.getEditDomain();
                    ITool tool = domain.getDefaultTool();

                    ((GallerySelectTool) tool).getStatus()
                            .setStatus(GEF.ST_ACTIVE, true);
                    domain.handleRequest(GEF.REQ_EDIT, (IViewer) galleryViewer);
                    break;
                }
            }
        }
    }

    private boolean modifyTemplateName(ITemplate template, String newName) {
        if (template == null || newName == null || newName.equals("") //$NON-NLS-1$
                || newName.equals(template.getName())) {
            return false;
        }

        //if this name has already exist.
        List<ITemplate> userTemplates = MindMapUI.getResourceManager()
                .getUserTemplates();
        for (ITemplate t : userTemplates) {
            if (newName.equals(t.getName())) {
                return false;
            }
        }

        // delete old file, and create a new.
        URI uri = template.getSourceWorkbookURI();
        File sourceFile = URIUtil.toFile(uri);

        String newPath = sourceFile.getParent()
                + System.getProperty("file.separator") + newName //$NON-NLS-1$
                + FileUtils.getExtension(sourceFile.getAbsolutePath());
        sourceFile.renameTo(new File(newPath));

        ((TemplateCategorizedLabelProvider) getLabelProvider())
                .fireLabelProviderChanged(new LabelProviderChangedEvent(
                        getLabelProvider(), template));
        return true;
    }

    protected GalleryViewer createNestedViewer() {
        return new TemplateGalleryViewer();
    }

}
