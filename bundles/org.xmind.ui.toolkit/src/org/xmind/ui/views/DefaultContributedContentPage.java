package org.xmind.ui.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.Page;

public class DefaultContributedContentPage extends Page {

    private Composite composite;

    private Label messageLabel;

    private String message = ""; //$NON-NLS-1$

    private boolean centered;

    public DefaultContributedContentPage() {
        this(false);
    }

    public DefaultContributedContentPage(boolean centered) {
        this.centered = centered;
    }

    @Override
    public void createControl(Composite parent) {
        this.composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 3;
        layout.marginHeight = 3;
        layout.horizontalSpacing = 3;
        layout.verticalSpacing = 3;
        this.composite.setLayout(layout);

        this.messageLabel = new Label(this.composite,
                (this.centered ? SWT.CENTER : SWT.LEFT) | SWT.WRAP);
        this.messageLabel.setText(message);
        this.messageLabel.setForeground(
                parent.getDisplay().getSystemColor(SWT.COLOR_GRAY));
        this.messageLabel.setLayoutData(new GridData(SWT.FILL,
                this.centered ? SWT.CENTER : SWT.FILL, true, true));
    }

    @Override
    public Control getControl() {
        return this.composite;
    }

    @Override
    public void setFocus() {
        this.composite.setFocus();
    }

    public String getMessage() {
        return this.message;
    }

    public Label getMessageLabel() {
        return this.messageLabel;
    }

    public void setMessage(String message) {
        if (message == null)
            message = ""; //$NON-NLS-1$
        this.message = message;
        if (this.messageLabel != null && !this.messageLabel.isDisposed()) {
            this.messageLabel.setText(message);
        }
    }

}
