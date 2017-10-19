package org.xmind.ui.internal.views;

import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.IContributedContentsView;
import org.eclipse.ui.part.ViewPart;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;

public class InspectorView extends ViewPart implements IContributedContentsView,
        IPartListener, IPageChangedListener {

    private IGraphicalEditor contributingPart;

    private Composite container;

    private Composite contentsPage;

    private InspectorViewer viewer;

    @Override
    public void createPartControl(Composite parent) {
        container = new Composite(parent, SWT.NONE);
        StackLayout layout = new StackLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        container.setLayout(layout);

        createContentsPage(container);

        showPage(contentsPage);
        if (getSite().getPage() != null
                && getSite().getPage().getActivePart() == this) {
            setFocus();
        }
    }

    private void createContentsPage(Composite parent) {
        contentsPage = new Composite(parent, SWT.NONE);
        contentsPage.setLayout(new FillLayout());

        doCreatePartControl(contentsPage);
    }

    private void showPage(Composite page) {
        if (page == null || page.isDisposed())
            return;
        if (container == null || container.isDisposed())
            return;

        ((StackLayout) container.getLayout()).topControl = page;
        container.layout();
    }

    private void doCreatePartControl(Composite parent) {
        viewer = new InspectorViewer();

        viewer.addSection(new AuthorInfoInspectorSection());
        viewer.addSection(new AttachmentsInspectorSection());
        viewer.addSection(new ExternalFilesInspectorSection());
        viewer.addSection(new HyperlinkInspectorSection());
        viewer.addSection(new ImageInspectorSection());
        viewer.addSection(new FileInfoInspectorSection());

        viewer.createControl(parent);

        getSite().getPage().addPartListener(this);
        partActivated(getSite().getPage().getActivePart());
    }

    @Override
    public void dispose() {
        setContributingPart(null);
        getSite().getPage().removePartListener(this);
        super.dispose();
    }

    public IWorkbenchPart getContributingPart() {
        return contributingPart;
    }

    private void setContributingPart(IGraphicalEditor part) {
        if (this.contributingPart != null) {
            this.contributingPart.removePageChangedListener(this);
        }
        this.contributingPart = part;
        if (viewer != null) {
            viewer.setContributingViewer(getContributingViewer(part));
        }
        if (part != null) {
            part.addPageChangedListener(this);
        }
    }

    private IGraphicalViewer getContributingViewer(IGraphicalEditor editor) {
        if (editor != null) {
            IGraphicalEditorPage page = editor.getActivePageInstance();
            if (page != null)
                return page.getViewer();
        }
        return null;
    }

    public void setFocus() {
        if (viewer != null && viewer.getControl() != null
                && !viewer.getControl().isDisposed()) {
            viewer.getControl().setFocus();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object getAdapter(Class adapter) {
        if (adapter == IContributedContentsView.class)
            return this;
        return super.getAdapter(adapter);
    }

    public void partActivated(IWorkbenchPart part) {
        if (part instanceof IGraphicalEditor) {
            setContributingPart((IGraphicalEditor) part);
        }
    }

    public void partBroughtToTop(IWorkbenchPart part) {
    }

    public void partClosed(IWorkbenchPart part) {
        if (part == contributingPart) {
            setContributingPart(null);
        }
    }

    public void partDeactivated(IWorkbenchPart part) {
    }

    public void partOpened(IWorkbenchPart part) {
    }

    public void pageChanged(PageChangedEvent event) {
        if (viewer != null) {
            viewer.setContributingViewer(
                    getContributingViewer(contributingPart));
        }
    }

}
