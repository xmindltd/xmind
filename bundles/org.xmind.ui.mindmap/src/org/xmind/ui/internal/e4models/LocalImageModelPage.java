package org.xmind.ui.internal.e4models;

import javax.inject.Inject;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ColorDescriptor;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.xmind.core.ITopic;
import org.xmind.gef.EditDomain;
import org.xmind.gef.GEF;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.IViewer;
import org.xmind.gef.Request;
import org.xmind.gef.part.IPart;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.dialogs.DialogMessages;
import org.xmind.ui.internal.dialogs.DialogUtils;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.resources.ColorUtils;
import org.xmind.ui.resources.FontUtils;
import org.xmind.ui.util.MindMapUtils;

public class LocalImageModelPage extends ModelPage
        implements ISelectionChangedListener, IPartListener {

    public static final String PAGE_ID = "org.xmind.ui.modelPart.image.pages.local"; //$NON-NLS-1$

    private class InsertImageAction extends Action {

        public void run() {
            IGraphicalEditorPage page = (getEditor() == null ? null
                    : getEditor().getActivePageInstance());
            if (page == null || page.isDisposed()) {
                return;
            }

            EditDomain domain = page.getEditDomain();
            if (domain == null) {
                return;
            }

            IGraphicalViewer viewer = page.getViewer();
            if (viewer == null) {
                return;
            }

            Control control = viewer.getControl();
            if (control == null || control.isDisposed()) {
                return;
            }

            ISelection selection = viewer.getSelection();
            if (selection.isEmpty()
                    || !(selection instanceof IStructuredSelection)) {
                return;
            }

            Object o = ((IStructuredSelection) selection).getFirstElement();
            IPart part = viewer.findPart(o);
            ITopic topic = (ITopic) part.getAdapter(ITopic.class);
            if (topic == null) {
                return;
            }

            IPart topicPart = viewer.findPart(topic);
            if (topicPart == null) {
                return;
            }

            FileDialog dialog = new FileDialog(control.getShell(), SWT.OPEN);
            DialogUtils.makeDefaultImageSelectorDialog(dialog, true);
            dialog.setText(DialogMessages.SelectImageDialog_title);
            String path = dialog.open();
            if (path == null) {
                return;
            }

            insertImage(path, topicPart, viewer, domain);
        }

        private void insertImage(String path, IPart topicPart, IViewer viewer,
                EditDomain domain) {
            Request request = new Request(MindMapUI.REQ_ADD_IMAGE);
            request.setViewer(viewer);
            request.setPrimaryTarget(topicPart);
            request.setParameter(GEF.PARAM_PATH, new String[] { path });
            domain.handleRequest(request);
        }
    }

    @Inject
    private IWorkbenchWindow workbenchWindow;

    private ResourceManager resources;

    private Button button;

    private IGraphicalEditor editor;

    @Override
    public String getModelPageId() {
        return PAGE_ID;
    }

    @Override
    public String getModelPageTitle() {
        return MindMapMessages.LocalImageModelPage_title;
    }

    @Override
    protected Control doCreateControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        resources = new LocalResourceManager(JFaceResources.getResources(),
                container);
        container.setBackground(
                (Color) resources.get(ColorUtils.toDescriptor("#ffffff"))); //$NON-NLS-1$
        container.setLayoutData(new GridData(GridData.FILL_BOTH));

        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 0;
        container.setLayout(layout);

        createContent(container);

        //add dispose listener
        container.addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent e) {
                dispose();
            }
        });

        return container;
    }

    private void createContent(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(parent.getBackground());
        composite.setLayoutData(
                new GridData(SWT.CENTER, SWT.CENTER, true, true));

        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.verticalSpacing = 60;
        layout.horizontalSpacing = 0;
        composite.setLayout(layout);

        createImageSection(composite);
        createButtonSection(composite);
    }

    private void createImageSection(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(parent.getBackground());
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.verticalSpacing = 20;
        layout.horizontalSpacing = 0;
        composite.setLayout(layout);

        Label image = new Label(composite, SWT.NONE);
        image.setBackground(composite.getBackground());
        image.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));
        image.setImage((Image) resources.get(MindMapUI.getImages()
                .get("insert_local_image_page.png", true))); //$NON-NLS-1$

        Composite composite2 = new Composite(composite, SWT.NONE);
        composite2.setBackground(parent.getBackground());
        composite2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        GridLayout layout2 = new GridLayout(1, false);
        layout2.marginWidth = 10;
        layout2.marginHeight = 0;
        composite2.setLayout(layout2);

        Label text = new Label(composite2, SWT.WRAP);
        text.setBackground(composite2.getBackground());
        text.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));
        text.setForeground((Color) resources
                .get(ColorDescriptor.createFrom(ColorUtils.toRGB("#aaaaaa")))); //$NON-NLS-1$
        text.setAlignment(SWT.CENTER);
        text.setFont((Font) resources.get(FontDescriptor.createFrom(
                FontUtils.relativeHeight(text.getFont().getFontData(), 2))));
        text.setText(
                MindMapMessages.LocalImageModelPage_ImageSection_description);

    }

    private void createButtonSection(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(parent.getBackground());
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 0;
        composite.setLayout(layout);

        button = new Button(composite, SWT.PUSH);
        button.setBackground(composite.getBackground());
        GridData layoutData = new GridData(SWT.CENTER, SWT.CENTER, true, false);
        layoutData.widthHint = 90;
        button.setLayoutData(layoutData);
        button.setText(MindMapMessages.LocalImageModelPage_Insert_button);

        final InsertImageAction insertImageAction = new InsertImageAction();

        button.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                insertImageAction.run();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

        //set button state
        setTargetEditor(getEditor());
        workbenchWindow.getActivePage().addPartListener(this);
    }

    public void setFocus() {
        if (button != null && !button.isDisposed()) {
            button.setFocus();
        }
    }

    private void setTargetEditor(IGraphicalEditor editor) {
        if (editor == this.editor) {
            return;
        }

        if (this.editor != null) {
            this.editor.getSite().getSelectionProvider()
                    .removeSelectionChangedListener(this);
        }
        this.editor = editor;
        if (this.editor != null) {
            this.editor.getSite().getSelectionProvider()
                    .addSelectionChangedListener(this);
            setSelection(
                    editor.getSite().getSelectionProvider().getSelection());
        } else {
            setSelection(null);
        }
    }

    @Override
    public void selectionChanged(SelectionChangedEvent event) {
        setSelection(event.getSelection());
    }

    private void setSelection(ISelection selection) {
        boolean isEnabled = MindMapUtils.isSingleTopic(selection);
        if (button != null && !button.isDisposed()) {
            button.setEnabled(isEnabled);
        }
    }

    @Override
    public void partActivated(IWorkbenchPart part) {
        if (part instanceof IGraphicalEditor) {
            setTargetEditor((IGraphicalEditor) part);
        }
    }

    @Override
    public void partBroughtToTop(IWorkbenchPart part) {
    }

    @Override
    public void partClosed(IWorkbenchPart part) {
        setTargetEditor(null);
    }

    @Override
    public void partDeactivated(IWorkbenchPart part) {
    }

    @Override
    public void partOpened(IWorkbenchPart part) {
    }

    private void dispose() {
        setTargetEditor(null);
        workbenchWindow.getActivePage().removePartListener(this);
    }

    private IGraphicalEditor getEditor() {
        IEditorPart activeEditor = workbenchWindow.getActivePage()
                .getActiveEditor();
        if (activeEditor instanceof IGraphicalEditor) {
            return (IGraphicalEditor) activeEditor;
        }
        return null;
    }

}
