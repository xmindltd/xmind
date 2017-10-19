package org.xmind.ui.internal.views;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public abstract class InspectorContentSection extends InspectorSection {

    private ContentListViewer list;

    @Override
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
        list = new ContentListViewer(composite, SWT.NONE);
        list.setContentProvider(new InspectorContentProvider());
        list.setLabelProvider(new InspectorLabelProvider());

        list.getControl().addListener(SWT.FocusOut, new Listener() {
            public void handleEvent(Event event) {
                list.setSelection(StructuredSelection.EMPTY);
            }
        });

        list.getControl().setLayoutData(
                new GridData(SWT.FILL, SWT.FILL, true, false));

        return composite;
    }

    @Override
    protected void refreshList() {
        if (list == null || list.getControl() == null)
            return;
        if (list.getInput() != getContributingViewer())
            list.setInput(getContributingViewer());

        list.refresh();
        reflow();
    }

    public ContentListViewer getList() {
        return list;
    }

}
