package org.xmind.cathy.internal.dashboard;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.xmind.cathy.internal.ICathyConstants;
import org.xmind.cathy.internal.WorkbenchMessages;
import org.xmind.core.util.FileUtils;
import org.xmind.gef.EditDomain;
import org.xmind.gef.GEF;
import org.xmind.gef.IViewer;
import org.xmind.gef.event.KeyEvent;
import org.xmind.gef.part.IPart;
import org.xmind.gef.tool.ITool;
import org.xmind.gef.ui.internal.SpaceCollaborativeEngine;
import org.xmind.gef.util.Properties;
import org.xmind.ui.gallery.CategorizedGalleryViewer;
import org.xmind.ui.gallery.GalleryEditTool;
import org.xmind.ui.gallery.GalleryLayout;
import org.xmind.ui.gallery.GallerySelectTool;
import org.xmind.ui.gallery.GalleryViewer;
import org.xmind.ui.internal.ClonedTemplate;
import org.xmind.ui.internal.TemplateGroup;
import org.xmind.ui.internal.wizards.TemplateLabelProvider;
import org.xmind.ui.mindmap.ITemplate;
import org.xmind.ui.mindmap.ITemplateGroup;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.resources.ColorUtils;
import org.xmind.ui.texteditor.FloatingTextEditor;

@SuppressWarnings("restriction")
public class CategorizedTemplateViewer extends CategorizedGalleryViewer
        implements IAdaptable {

    private static final int FRAME_WIDTH = 210;

    private static final int FRAME_HEIGHT = 130;

    private class CategorizedTemplateContentProvider
            implements ITreeContentProvider {

        public void dispose() {
        }

        public void inputChanged(Viewer viewer, Object oldInput,
                Object newInput) {
        }

        public Object[] getElements(Object inputElement) {
            if (inputElement instanceof ITemplateGroup[]) {
                return (ITemplateGroup[]) inputElement;
            }

            return null;
        }

        public Object[] getChildren(Object parentElement) {
            if (parentElement instanceof ITemplateGroup) {
                return ((ITemplateGroup) parentElement).getTemplates()
                        .toArray();
            }
            return null;
        }

        public Object getParent(Object element) {
            if (element instanceof ITemplate) {
                Object input = getInput();
                if (input instanceof ITemplateGroup[]) {
                    ITemplateGroup[] groups = (ITemplateGroup[]) input;
                    for (ITemplateGroup group : groups) {
                        for (ITemplate template : group.getTemplates()) {
                            if (template.equals(element)) {
                                return group;
                            }
                        }
                    }
                }
            }

            return null;
        }

        public boolean hasChildren(Object element) {
            return (element instanceof ITemplateGroup
                    && ((ITemplateGroup) element).getTemplates().size() > 0);
        }
    }

    private static class CategorizedTemplateLabelProvider
            extends TemplateLabelProvider {

        public String getText(Object element) {
            if (element instanceof ITemplateGroup) {
                String name = ((ITemplateGroup) element).getName();
                if (name == null)
                    name = WorkbenchMessages.CategorizedTemplateViewer_group_untitiledName;
                return name.length() <= 20 ? name
                        : name.substring(0, 20) + "..."; //$NON-NLS-1$
            } else if (element instanceof ITemplate) {
                String name = ((ITemplate) element).getName();
                if (name == null)
                    name = WorkbenchMessages.CategorizedTemplateViewer_template_untitiledName;
                return name.length() <= 20 ? name
                        : name.substring(0, 20) + "..."; //$NON-NLS-1$
            }

            return super.getText(element);
        }
    }

    private class TemplateGallerySelectTool extends GallerySelectTool {
        @Override
        protected boolean handleKeyUp(KeyEvent ke) {
            int state = ke.getState();
            int key = ke.keyCode;
            if (state == 0 && key == SWT.DEL) {
                ISelection selection = getSelection();
                if (selection instanceof IStructuredSelection) {
                    Object element = ((IStructuredSelection) selection)
                            .getFirstElement();
                    if (element instanceof ITemplate) {
                        ITemplate template = (ITemplate) element;
                        if (MindMapUI.getResourceManager()
                                .isUserTemplate(template)) {
                            if (MessageDialog.openConfirm(
                                    getControl().getShell(),
                                    WorkbenchMessages.ConfirmDeleteTemplateDialog_title,
                                    NLS.bind(
                                            WorkbenchMessages.ConfirmDeleteTemplateDialog_message_withTemplateName,
                                            template.getName()))) {
                                MindMapUI.getResourceManager()
                                        .removeUserTemplate(template);
                            }
                        }
                    }
                }
            }
            return super.handleKeyUp(ke);
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
            getHelper().setPrefWidth(130);
        }
    }

    private List<ITemplateGroup> sysTemplateGroups;

    private ResourceManager localResourceManager;

    public CategorizedTemplateViewer(Composite container) {
        super();
        setSectionStyle(Section.COMPACT | Section.TWISTIE | Section.EXPANDED
                | Section.NO_TITLE_FOCUS_BOX);
        create(container);
    }

    private void create(Composite parent) {
        localResourceManager = new LocalResourceManager(
                JFaceResources.getResources(), parent);
        setContentProvider(new CategorizedTemplateContentProvider());
        setLabelProvider(new CategorizedTemplateLabelProvider());

        EditDomain domain = new EditDomain();
        domain.installTool(GEF.TOOL_SELECT, new TemplateGallerySelectTool());
        domain.installTool(GEF.TOOL_EDIT, new TemplateNameEditTool());
        setEditDomain(domain);

        initProperties();
        createControl(parent, SWT.WRAP);

        getControl().addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                handleDispose();
            }
        });

        setInput(getViewerInput());

        registerHelper(parent.getShell());
    }

    private void handleDispose() {
        unregisterHelper(getControl().getShell());
    }

    private void initProperties() {
        Properties properties = getProperties();

        properties.set(GalleryViewer.Horizontal, Boolean.TRUE);
        properties.set(GalleryViewer.Wrap, Boolean.TRUE);
        properties.set(GalleryViewer.TitlePlacement,
                GalleryViewer.TITLE_BOTTOM);

        properties.set(GalleryViewer.SingleClickToOpen, Boolean.TRUE);
        properties.set(GalleryViewer.SolidFrames, true);
        properties.set(GalleryViewer.FlatFrames, true);

        properties.set(GalleryViewer.ImageConstrained, true);
        properties.set(GalleryViewer.ImageStretched, true);

        properties.set(GalleryViewer.Layout,
                new GalleryLayout(GalleryLayout.ALIGN_TOPLEFT,
                        GalleryLayout.ALIGN_TOPLEFT, 30, 0,
                        new Insets(10, 0, 20, 65)));
        properties.set(GalleryViewer.ContentPaneBorderWidth, 1);
        properties.set(GalleryViewer.ContentPaneBorderColor,
                (Color) localResourceManager
                        .get(ColorUtils.toDescriptor("#cccccc"))); //$NON-NLS-1$
        properties.set(GalleryViewer.FrameContentSize,
                new Dimension(FRAME_WIDTH, FRAME_HEIGHT));

        properties.set(GalleryViewer.ContentPaneSpaceCollaborativeEngine,
                new SpaceCollaborativeEngine());
    }

    @Override
    protected void configureContainer(ScrolledForm container) {
        super.configureContainer(container);
        container.setBackground(container.getParent().getBackground());
    }

    private Object[] getViewerInput() {
        List<ITemplateGroup> groups = new ArrayList<ITemplateGroup>();
        if (sysTemplateGroups == null || sysTemplateGroups.isEmpty())
            sysTemplateGroups = MindMapUI.getResourceManager()
                    .getSystemTemplateGroups();

        groups.addAll(sysTemplateGroups);

        List<ITemplate> userTemplates = MindMapUI.getResourceManager()
                .getUserTemplates();
        if (userTemplates.size() != 0) {
            ITemplateGroup userGroup = new TemplateGroup(
                    WorkbenchMessages.TemplateViewer_UserGroup_title,
                    userTemplates);
            groups.add(userGroup);
        }

        return groups.toArray(new ITemplateGroup[groups.size()]);
    }

    @Override
    protected Control createSectionContent(Composite parent, Object category) {
        parent.setBackground(parent.getParent().getBackground());
        getWidgetFactory().setBackground(parent.getBackground());

        return super.createSectionContent(parent, category);
    }

    protected GalleryViewer createNestedViewer() {
        return new GalleryViewer();
    }

    private void registerHelper(Shell shell) {
        shell.setData(ICathyConstants.HELPER_TEMPLATE_RENAME, new Runnable() {

            public void run() {
                ISelection selection = getSelection();
                if (selection instanceof IStructuredSelection) {
                    Object obj = ((IStructuredSelection) selection)
                            .getFirstElement();
                    if (obj instanceof ITemplate) {
                        ITemplate template = (ITemplate) obj;
                        startEditing(template);
                    }
                }
            }
        });
    }

    private void unregisterHelper(Shell shell) {
        shell.setData(ICathyConstants.HELPER_TEMPLATE_RENAME, null);
    }

    public void userTemplateAdded(ITemplate template) {
        if (template == null || getControl() == null
                || getControl().isDisposed()) {
            return;
        }

        setInput(getViewerInput());
        refresh();
        reveal(new ClonedTemplate(template.getSourceWorkbookURI(), null));
    }

    public void userTemplateRemoved(ITemplate template) {
        if (template == null || getControl() == null
                || getControl().isDisposed()) {
            return;
        }
        setInput(getViewerInput());
    }

    private void startEditing(ITemplate template) {
        Object input = getInput();
        if (input instanceof ITemplateGroup[]) {
            ITemplateGroup[] groups = (ITemplateGroup[]) input;
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

        List<ITemplate> userTemplates = MindMapUI.getResourceManager()
                .getUserTemplates();
        for (ITemplate t : userTemplates) {
            if (newName.equals(t.getName())) {
                return false;
            }
        }

        URI uri = template.getSourceWorkbookURI();
        File sourceFile = URIUtil.toFile(uri);

        File targetFile = new File(sourceFile.getParent(),
                newName + FileUtils.getExtension(sourceFile.getAbsolutePath()));
        boolean renameSuccess = sourceFile.renameTo(targetFile);
        if (!renameSuccess) {
            //TODO
        }

        Object[] input = getViewerInput();
        setInput(input);

        setSelection(new StructuredSelection(
                new ClonedTemplate(targetFile.toURI(), null)), true);

        return true;
    }

    public <T> T getAdapter(Class<T> adapter) {
        if (ISelectionProvider.class.equals(adapter)) {
            return adapter.cast(this);
        }

        return null;
    }

    @Override
    protected void unmapAllElements() {
    }

}
