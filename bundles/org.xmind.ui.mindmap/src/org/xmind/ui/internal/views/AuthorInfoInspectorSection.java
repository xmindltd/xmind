package org.xmind.ui.internal.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.xmind.core.IMeta;

public class AuthorInfoInspectorSection extends InspectorSection {

    private class AuthorInfoNameViewer extends AuthorInfoViewer {

        public AuthorInfoNameViewer(Composite parent) {
            super(parent);
        }

        @Override
        protected void refresh() {
            AuthorInfoInspectorSection.this.refreshAuthorInfoName();
        }

        @Override
        protected void changeContent() {
            String value = getEditor().getInput().getText();
            if (value != null && !"".equals(value) //$NON-NLS-1$
                    && getCurrentWorkbook() != null)
                getCurrentWorkbook().getMeta().setValue(IMeta.AUTHOR_NAME,
                        value);
            AuthorInfoInspectorSection.this.refreshAuthorInfoName();
        }

    }

    private class AuthorInfoEmailViewer extends AuthorInfoViewer {

        public AuthorInfoEmailViewer(Composite parent) {
            super(parent);
        }

        @Override
        protected void refresh() {
            AuthorInfoInspectorSection.this.refreshAuthorInfoEmail();
        }

        @Override
        protected void changeContent() {
            String value = getEditor().getInput().getText();
            if (value != null && getCurrentWorkbook() != null)
                getCurrentWorkbook().getMeta().setValue(IMeta.AUTHOR_EMAIL,
                        value);
            AuthorInfoInspectorSection.this.refreshAuthorInfoEmail();
        }
    }

    private class AuthorInfoOrganizationViewer extends AuthorInfoViewer {

        public AuthorInfoOrganizationViewer(Composite parent) {
            super(parent);
        }

        @Override
        protected void refresh() {
            AuthorInfoInspectorSection.this.refreshAuthorInfoOrg();
            reflow();
        }

        @Override
        protected void changeContent() {
            String value = getEditor().getInput().getText();
            if (value != null && getCurrentWorkbook() != null)
                getCurrentWorkbook().getMeta().setValue(IMeta.AUTHOR_ORG,
                        value);
            AuthorInfoInspectorSection.this.refreshAuthorInfoOrg();
            reflow();
        }

    }

    private AuthorInfoNameViewer nameViewer;

    private AuthorInfoEmailViewer emailViewer;

    private AuthorInfoOrganizationViewer organizationViewer;

    private Color invalidColor;

    private Color validColor;

    public AuthorInfoInspectorSection() {
        setTitle(Messages.AuthorInfoInspectorSection_title);
    }

    @Override
    protected Composite createContent(Composite parent) {
        invalidColor = parent.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY);
        validColor = parent.getDisplay().getSystemColor(SWT.COLOR_BLACK);

        Composite composite = super.createContent(parent);

        createNameItem(composite);
        createEmailItem(composite);
        createOrganizationItem(composite);

        return composite;
    }

    private void createNameItem(Composite parent) {
        Composite item = getItemComposite(parent);
        Label label = new Label(item, SWT.NONE);
        label.setText(Messages.AuthorInfoInspectorSection_Name);

        nameViewer = new AuthorInfoNameViewer(item);
        nameViewer.setEnabled(true);
        fillViewerContent(nameViewer, getCurrentWorkbook().getMeta(),
                IMeta.AUTHOR_NAME,
                Messages.AuthorInfoInspectorSection_Enter_Name);
    }

    private void createEmailItem(Composite parent) {
        Composite item = getItemComposite(parent);
        Label label = new Label(item, SWT.NONE);
        label.setText(Messages.AuthorInfoInspectorSection_Email);

        emailViewer = new AuthorInfoEmailViewer(item);
        emailViewer.setEnabled(true);
        fillViewerContent(emailViewer, getCurrentWorkbook().getMeta(),
                IMeta.AUTHOR_EMAIL,
                Messages.AuthorInfoInspectorSection_Enter_Email);
    }

    private void createOrganizationItem(Composite parent) {
        Composite item = getItemComposite(parent);
        Label label = new Label(item, SWT.NONE);
        label.setText(Messages.AuthorInfoInspectorSection_Organization);

        organizationViewer = new AuthorInfoOrganizationViewer(item);
        organizationViewer.setEnabled(true);
        fillViewerContent(organizationViewer, getCurrentWorkbook().getMeta(),
                IMeta.AUTHOR_ORG,
                Messages.AuthorInfoInspectorSection_Enter_Organization);
    }

    private void fillViewerContent(AuthorInfoViewer viewer, IMeta meta,
            String key, String enterLabel) {
        String value = meta.getValue(key);

        viewer.getEditor().getButton().setTextForeground(
                isContentEmpty(value) ? invalidColor : validColor);
        String text = null;
        if (!isContentEmpty(value)) {
            viewer.getEditor().getInput().setText(value);
            text = value;
        } else {
            viewer.getEditor().getInput().setText(""); //$NON-NLS-1$
            text = enterLabel;
        }
        viewer.getEditor().getButton().setText(text);
        viewer.getDisplay().setText(text);
    }

    private Composite getItemComposite(Composite parent) {
        Composite item = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        item.setLayout(layout);
        item.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        return item;
    }

    private boolean isContentEmpty(String content) {
        return content == null || "".equals(content); //$NON-NLS-1$
    }

    public void refreshAuthorInfoName() {
        if (nameViewer != null && getCurrentWorkbook() != null)
            fillViewerContent(nameViewer, getCurrentWorkbook().getMeta(),
                    IMeta.AUTHOR_NAME,
                    Messages.AuthorInfoInspectorSection_Enter_Name);
    }

    public void refreshAuthorInfoEmail() {
        if (emailViewer != null && getCurrentWorkbook() != null)
            fillViewerContent(emailViewer, getCurrentWorkbook().getMeta(),
                    IMeta.AUTHOR_EMAIL,
                    Messages.AuthorInfoInspectorSection_Enter_Email);
    }

    public void refreshAuthorInfoOrg() {
        if (organizationViewer != null && getCurrentWorkbook() != null)
            fillViewerContent(organizationViewer,
                    getCurrentWorkbook().getMeta(), IMeta.AUTHOR_ORG,
                    Messages.AuthorInfoInspectorSection_Enter_Organization);
    }

    public Color getInvalidColor() {
        return invalidColor;
    }

    public void setInvalidColor(Color invalidColor) {
        this.invalidColor = invalidColor;
    }

    public Color getValidColor() {
        return validColor;
    }

    public void setValidColor(Color validColor) {
        this.validColor = validColor;
    }

    @Override
    protected void handleDispose() {
        if (invalidColor != null && !invalidColor.isDisposed()) {
            invalidColor.dispose();
            invalidColor = null;
        }

        if (validColor != null && !validColor.isDisposed()) {
            validColor.dispose();
            validColor = null;
        }
    }

    @Override
    protected void refreshAuthorInfo() {
        if (getCurrentWorkbook() != null) {
            refreshAuthorInfoName();
            refreshAuthorInfoEmail();
            refreshAuthorInfoOrg();
        }
    }

}
