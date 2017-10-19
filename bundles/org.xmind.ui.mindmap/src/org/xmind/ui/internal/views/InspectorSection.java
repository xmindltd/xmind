package org.xmind.ui.internal.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.ui.forms.WidgetFactory;

public abstract class InspectorSection {
    protected class InspectorContentProvider
            implements IStructuredContentProvider {

        public Object[] getElements(Object inputElement) {
            if (inputElement == contributingViewer) {
                return getAllPropertyContents(contributingViewer);
            }
            return new Object[0];
        }

        public void dispose() {
        }

        public void inputChanged(Viewer viewer, Object oldInput,
                Object newInput) {
        }

    }

    protected class InspectorLabelProvider extends LabelProvider {
        public Image getImage(Object element) {
            return getPropertyInspectorImage(element);
        }

        public String getText(Object element) {
            return getPropertyInspectorText(element);
        }

    }

    private String sectionTitle;

    private IGraphicalViewer contributingViewer;

    private Section section;

    private InspectorViewer container = null;

    private ScrolledForm form;

    public InspectorSection() {
    }

    public InspectorSection(Control control) {
    }

    protected void setTitle(String title) {
        this.sectionTitle = title;
        if (section != null)
            section.setText(title == null ? "" : title); //$NON-NLS-1$
    }

    public InspectorViewer getContainer() {
        return container;
    }

    public void setContainer(InspectorViewer container) {
        this.container = container;
    }

    public Composite createSectionControl(Composite parent,
            WidgetFactory factory, ScrolledForm form) {
        if (this.form != form)
            this.form = form;

        Composite composite = createComposite(parent, factory);
        fillSection(composite, factory);

        composite.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                handleDispose();
            }
        });

        return composite;
    }

    private Composite createComposite(Composite parent, WidgetFactory factory) {
        Composite composite = factory.createComposite(parent, SWT.WRAP);
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.marginBottom = 0;
        layout.marginLeft = 0;
        layout.marginRight = 0;
        layout.marginTop = 0;
        layout.verticalSpacing = 7;
        composite.setLayout(layout);

        return composite;
    }

    private void fillSection(Composite parent, WidgetFactory factory) {
        if (parent == null || parent.isDisposed())
            return;

        parent.setRedraw(true);

        section = factory.createSection(parent,
                Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED
                        | SWT.BORDER | Section.NO_TITLE_FOCUS_BOX);
        if (sectionTitle != null) {
            section.setText(sectionTitle);
        }
        section.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        Composite client = factory.createComposite(section, SWT.WRAP);

        GridLayout layout = new GridLayout();
        layout.marginHeight = 2;
        layout.marginWidth = 2;
        layout.verticalSpacing = 2;
        client.setLayout(layout);

        createContent(client);
        section.setClient(client);

    }

    protected Composite createContent(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.marginLeft = 7;
        layout.horizontalSpacing = 5;
        layout.verticalSpacing = 2;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        return composite;
    }

    public void refresh() {
        Display.getCurrent().asyncExec(new Runnable() {
            public void run() {
                if (getControl() == null || getControl().isDisposed())
                    return;
                internalRefresh();
            }
        });
    }

    protected void internalRefresh() {
        refreshAuthorInfo();
        refreshList();
        refreshImageList();
        refreshFileInfo();

        if (section != null && !section.isDisposed()) {
            section.getParent().layout();
        }
    }

    protected void reflow() {
        if (form != null && !form.isDisposed())
            form.reflow(true);
    }

    protected void refreshAuthorInfo() {
    }

    protected void refreshList() {
    }

    protected void refreshImageList() {
    }

    protected void refreshFileInfo() {
    }

    public boolean hasInspector() {
        return true;
    }

    public IGraphicalViewer getContributingViewer() {
        return contributingViewer;
    }

    protected IWorkbook getCurrentWorkbook() {
        if (contributingViewer == null)
            return null;

        return (IWorkbook) contributingViewer.getAdapter(IWorkbook.class);
    }

    protected ISheet getCurrentSheet() {
        if (contributingViewer == null)
            return null;

        return (ISheet) contributingViewer.getAdapter(ISheet.class);
    }

    protected List<ITopic> getAllTopics() {
        IWorkbook workBook = getCurrentWorkbook();
        if (workBook == null)
            return null;

        List<ITopic> allTopics = null;
        for (ISheet sheet : workBook.getSheets()) {
            if (allTopics == null)
                allTopics = new ArrayList<ITopic>();
            allTopics.addAll(getSheet(sheet));
        }

        return allTopics;
    }

    private List<ITopic> getSheet(ISheet sheet) {
        List<ITopic> allTopics = null;
        ITopic root = sheet.getRootTopic();
        if (root != null) {
            if (allTopics == null)
                allTopics = new ArrayList<ITopic>();
            allTopics.add(root);
            allTopics = getAllTopics(root.getAllChildren(), allTopics);
        }
        return allTopics;
    }

    private List<ITopic> getAllTopics(List<ITopic> topics,
            List<ITopic> allTopics) {
        if (topics.size() == 0)
            return allTopics;

        List<ITopic> subs = new ArrayList<ITopic>();
        for (ITopic topic : topics) {
            subs.addAll(topic.getAllChildren());
        }

        allTopics.addAll(topics);
        return getAllTopics(subs, allTopics);
    }

    public void setContributingViewer(IGraphicalViewer contributingViewer) {
        this.contributingViewer = contributingViewer;
        refresh();
    }

    public Control getControl() {
        return section;
    }

    protected abstract void handleDispose();

    protected Object[] getAllPropertyContents(
            IGraphicalViewer contributingViewer2) {
        return new Object[0];
    }

    protected Image getPropertyInspectorImage(Object element) {
        return null;
    }

    protected String getPropertyInspectorText(Object element) {
        return ""; //$NON-NLS-1$
    }

}
