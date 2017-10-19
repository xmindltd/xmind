package org.xmind.ui.internal.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.forms.HyperlinkGroup;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.part.PageBook;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.ui.forms.WidgetFactory;

public class InspectorViewer {

    private static final int PAGE_NO_CONTENT = 0;
    private static final int PAGE_CONTENT = 1;

    private IGraphicalViewer contributingViewer;

    private WidgetFactory factory;

    private PageBook pageBook;

    private Composite emptyPage;

    private Composite contentPage;

    private ScrolledForm contentForm;

    private List<InspectorSection> sections;

    private ImageInspectorSection imageSection;

    private boolean schedulingReflow = false;

    public void addSection(InspectorSection section) {
        if (section instanceof ImageInspectorSection)
            imageSection = (ImageInspectorSection) section;
        if (sections == null)
            sections = new ArrayList<InspectorSection>();
        sections.add(section);
//        section.setContainer(this);
    }

    public void createControl(Composite parent) {
        factory = new WidgetFactory(parent.getDisplay());
        factory.getHyperlinkGroup()
                .setHyperlinkUnderlineMode(HyperlinkGroup.UNDERLINE_HOVER);

        pageBook = new PageBook(parent, SWT.NONE);
        pageBook.setBackground(
                parent.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
        pageBook.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                handleDispose();
            }
        });

        createEmptyPage(pageBook);
        pageBook.showPage(emptyPage);
    }

    private void createEmptyPage(Composite parent) {
        emptyPage = new Composite(parent, SWT.NONE);
        emptyPage.setBackground(parent.getBackground());
        FillLayout layout = new FillLayout();
        layout.marginWidth = 10;
        layout.marginHeight = 10;
        emptyPage.setLayout(layout);

        Label text = new Label(emptyPage, SWT.WRAP);
        text.setBackground(emptyPage.getBackground());
        text.setText(Messages.NoContent_message);
        final Color color = new Color(parent.getDisplay(), 0x40, 0x40, 0x40);
        text.setForeground(color);
        text.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                color.dispose();
            }
        });
    }

    public IGraphicalViewer getContributingViewer() {
        return contributingViewer;
    }

    public void setContributingViewer(IGraphicalViewer contributedViewer) {
        IGraphicalViewer oldViewer = this.contributingViewer;
        this.contributingViewer = contributedViewer;
        contributingViewerChanged(oldViewer, contributedViewer);
    }

    private void contributingViewerChanged(IGraphicalViewer oldViewer,
            IGraphicalViewer newViewer) {
        if (sections != null) {
            for (InspectorSection section : sections) {
                section.setContributingViewer(newViewer);
            }
        }
        showPage(newViewer != null ? PAGE_CONTENT : PAGE_NO_CONTENT);

        if (schedulingReflow)
            return;

        Display.getCurrent().asyncExec(new Runnable() {
            public void run() {
                if (contentPage != null && !contentPage.isDisposed())
                    contentPage.layout();
                if (pageBook != null && pageBook.isDisposed())
                    reflow();
                schedulingReflow = false;
            }
        });
        schedulingReflow = true;
    }

    protected void reflow() {
        if (contentForm != null && !contentForm.isDisposed())
            contentForm.reflow(true);
    }

    private void showPage(int pageId) {
        if (pageBook == null || pageBook.isDisposed())
            return;

        if (pageId == PAGE_CONTENT) {
            if (contentPage == null) {
                createContentPage(pageBook);
            }
            pageBook.showPage(contentPage);
        } else if (pageId == PAGE_NO_CONTENT) {
            pageBook.showPage(emptyPage);
        }
    }

    private void createContentPage(Composite parent) {
        contentPage = factory.createComposite(parent, SWT.NO_FOCUS);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 0;
        contentPage.setLayout(layout);

        contentForm = factory.createScrolledForm(contentPage);
        contentForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        final Composite contentBody = contentForm.getBody();
        final GridLayout contentLayout = new GridLayout(1, true);
        layout.marginHeight = 3;
        layout.marginWidth = 3;
        layout.verticalSpacing = 7;
        contentBody.setLayout(contentLayout);

        if (sections != null) {
            for (InspectorSection section : sections) {
                Composite composite = section.createSectionControl(contentBody,
                        factory, contentForm);
                if (section.getControl() != null)
                    composite.setLayoutData(
                            new GridData(SWT.FILL, SWT.FILL, true, false));
            }
        }

        contentForm.addListener(SWT.Resize, new Listener() {
            public void handleEvent(Event event) {
                int width = contentForm.getClientArea().width;
                width -= contentLayout.marginLeft + contentLayout.marginRight
                        + contentLayout.marginWidth * 2;
                if (imageSection != null
                        && !imageSection.getControl().isDisposed())
                    ((GridData) imageSection.getControl()
                            .getLayoutData()).widthHint = width;
                reflow();
            }
        });
    }

    public Control getControl() {
        return pageBook;
    }

    protected void handleDispose() {
        if (factory != null) {
            factory.dispose();
            factory = null;
        }
        pageBook = null;
    }

}
