/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See https://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/

package org.xmind.ui.internal.editor;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.MindMapUI;

public class EncryptionDailogPane extends DialogPane {

    private Text oldPasswordInputBox;

    private Text newPasswordInputBox;

    private Text verifyNewPasswordInputBox;

    private Label oldPasswordVerificationLabel;

    private Label newPasswordVerificationLabel;

    private Image doneIcon;

    private Image undoneIcon;

    private Image blankIcon;

    private String password;

    private Image getDoneIcon() {
        if (getContainer() == null || getContainer().isDisposed())
            return null;
        if (doneIcon == null || doneIcon.isDisposed()) {
            ImageDescriptor img = MindMapUI.getImages().get(IMindMapImages.DONE,
                    true);
            if (img != null) {
                doneIcon = img.createImage(getContainer().getDisplay());
            }
        }
        return doneIcon;
    }

    private Image getUndoneIcon() {
        if (getContainer() == null || getContainer().isDisposed())
            return null;
        if (undoneIcon == null || undoneIcon.isDisposed()) {
            ImageDescriptor img = MindMapUI.getImages().get(IMindMapImages.DONE,
                    false);
            if (img != null) {
                undoneIcon = img.createImage(getContainer().getDisplay());
            }
        }
        return undoneIcon;
    }

    private Image getBlankIcon() {
        if (getContainer() == null || getContainer().isDisposed())
            return null;
        if (blankIcon == null || blankIcon.isDisposed()) {
            ImageDescriptor img = MindMapUI.getImages()
                    .get(IMindMapImages.BLANK);
            if (img != null) {
                blankIcon = img.createImage(getContainer().getDisplay());
            }
        }
        return blankIcon;
    }

    @Override
    public void dispose() {
        if (doneIcon != null) {
            doneIcon.dispose();
            doneIcon = null;
        }
        if (blankIcon != null) {
            blankIcon.dispose();
            blankIcon = null;
        }
        super.dispose();
    }

    @Override
    protected Control createDialogContents(Composite parent) {
        Composite composite = (Composite) super.createDialogContents(parent);
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        gridLayout.verticalSpacing = 20;
        gridLayout.horizontalSpacing = 0;
        composite.setLayout(gridLayout);

        createMessageArea(composite);
        createPasswordArea(composite);

        verify();

        return composite;
    }

    private void createMessageArea(Composite parent) {
        Composite area = new Composite(parent, SWT.NONE);
        area.setBackground(parent.getBackground());

        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
        gridData.widthHint = SWT.DEFAULT;
        gridData.heightHint = SWT.DEFAULT;
        area.setLayoutData(gridData);

        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        gridLayout.verticalSpacing = 0;
        gridLayout.horizontalSpacing = 10;
        area.setLayout(gridLayout);

        createMessageIcon(area);
        createMessageBoard(area);
    }

    private void createMessageIcon(Composite parent) {
        Label iconLabel = new Label(parent, SWT.NONE);
        iconLabel.setBackground(parent.getBackground());
        iconLabel.setLayoutData(
                new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        Image image = getMessageIcon(iconLabel);
        iconLabel.setImage(image);
    }

    private Image getMessageIcon(Control control) {
        if (control == null)
            return null;
        ImageDescriptor image = MindMapUI.getImages().get(IMindMapImages.LOCK,
                true);
        if (image != null)
            return image.createImage(control.getDisplay());
        return null;
    }

    private void createMessageBoard(Composite parent) {
        Text messageBoard = new Text(parent,
                SWT.READ_ONLY | SWT.MULTI | SWT.WRAP);
        messageBoard.setBackground(parent.getBackground());
        applyFont(messageBoard);

        GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gridData.widthHint = SWT.DEFAULT;
        gridData.heightHint = SWT.DEFAULT;
        messageBoard.setLayoutData(gridData);
        messageBoard.setText(MindMapMessages.EncryptDialogPane_board_message);
    }

    private void createPasswordArea(Composite parent) {
        Composite area = new Composite(parent, SWT.NONE);
        area.setBackground(parent.getBackground());

        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
        gridData.widthHint = SWT.DEFAULT;
        gridData.heightHint = SWT.DEFAULT;
        area.setLayoutData(gridData);

        GridLayout gridLayout = new GridLayout(3, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        gridLayout.verticalSpacing = 5;
        gridLayout.horizontalSpacing = 3;
        area.setLayout(gridLayout);

        if (hasPassword()) {
            createOldPasswordInputBox(area);
        }

        createNewPasswordInputBox(area);
        createVerifyPasswordInputBox(area);

        Listener verifyListener = new Listener() {
            public void handleEvent(Event event) {
                verify();
            }
        };
        if (oldPasswordInputBox != null) {
            oldPasswordInputBox.addListener(SWT.Modify, verifyListener);
        }
        newPasswordInputBox.addListener(SWT.Modify, verifyListener);
        verifyNewPasswordInputBox.addListener(SWT.Modify, verifyListener);

    }

    private void createOldPasswordInputBox(Composite parent) {
        Label assistMessageBox = new Label(parent, SWT.WRAP);
        assistMessageBox.setBackground(parent.getBackground());
        assistMessageBox.setLayoutData(
                new GridData(SWT.FILL, SWT.BEGINNING, true, false));
        ((GridData) assistMessageBox.getLayoutData()).horizontalSpan = 3;
        assistMessageBox
                .setText(MindMapMessages.EncryptDialogPane_assist_message);

        Label label = new Label(parent, SWT.NONE);
        label.setLayoutData(
                new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        label.setText(MindMapMessages.EncryptDialogPane_oldpassword_text);
        label.setBackground(parent.getBackground());
        applyFont(label);

        oldPasswordInputBox = new Text(parent,
                SWT.BORDER | SWT.PASSWORD | SWT.SINGLE);
        applyFont(oldPasswordInputBox);

        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
        gridData.widthHint = SWT.DEFAULT;
        gridData.heightHint = SWT.DEFAULT;
        oldPasswordInputBox.setLayoutData(gridData);

        hookText(oldPasswordInputBox);
        addRefreshDefaultButtonListener(oldPasswordInputBox);
        addTriggerDefaultButtonListener(oldPasswordInputBox,
                SWT.DefaultSelection);

        oldPasswordVerificationLabel = new Label(parent, SWT.NONE);
        oldPasswordVerificationLabel.setBackground(parent.getBackground());
        oldPasswordVerificationLabel
                .setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
        oldPasswordVerificationLabel.setImage(getDoneIcon());

        Label sep = new Label(parent, SWT.NONE);
        sep.setBackground(parent.getBackground());
        sep.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        ((GridData) sep.getLayoutData()).horizontalSpan = 3;
    }

    private void createNewPasswordInputBox(Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        label.setLayoutData(
                new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        String text;
        if (oldPasswordInputBox == null) {
            text = MindMapMessages.EncryptDialogPane_password_text;
        } else {
            text = MindMapMessages.EncryptDialogPane_newpassword_text;
        }
        label.setText(text);
        label.setBackground(parent.getBackground());
        applyFont(label);

        newPasswordInputBox = new Text(parent,
                SWT.BORDER | SWT.PASSWORD | SWT.SINGLE);
        applyFont(newPasswordInputBox);

        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
        gridData.widthHint = SWT.DEFAULT;
        gridData.heightHint = SWT.DEFAULT;
        newPasswordInputBox.setLayoutData(gridData);

        hookText(newPasswordInputBox);
        addRefreshDefaultButtonListener(newPasswordInputBox);
        addTriggerDefaultButtonListener(newPasswordInputBox,
                SWT.DefaultSelection);

        Label blankIcon = new Label(parent, SWT.NONE);
        blankIcon.setBackground(parent.getBackground());
        blankIcon
                .setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
        blankIcon.setImage(getBlankIcon());
    }

    private void createVerifyPasswordInputBox(Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        label.setLayoutData(
                new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        label.setText(MindMapMessages.EncryptDialogPane_confirm_text);
        label.setBackground(parent.getBackground());
        applyFont(label);

        verifyNewPasswordInputBox = new Text(parent,
                SWT.BORDER | SWT.PASSWORD | SWT.SINGLE);
        applyFont(verifyNewPasswordInputBox);

        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
        gridData.widthHint = SWT.DEFAULT;
        gridData.heightHint = SWT.DEFAULT;
        verifyNewPasswordInputBox.setLayoutData(gridData);

        hookText(verifyNewPasswordInputBox);
        addRefreshDefaultButtonListener(verifyNewPasswordInputBox);
        addTriggerDefaultButtonListener(verifyNewPasswordInputBox,
                SWT.DefaultSelection);

        newPasswordVerificationLabel = new Label(parent, SWT.NONE);
        newPasswordVerificationLabel.setBackground(parent.getBackground());
        newPasswordVerificationLabel
                .setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
        newPasswordVerificationLabel.setImage(getDoneIcon());
    }

    @Override
    protected void createButtonsForButtonBar(Composite buttonBar) {
        createButton(buttonBar, IDialogConstants.OK_ID,
                IDialogConstants.OK_LABEL, true);
        createButton(buttonBar, IDialogConstants.CANCEL_ID,
                IDialogConstants.CANCEL_LABEL, false);
        setOKButtonEnabled(false);
    }

    private void setOKButtonEnabled(boolean enabled) {
        Button button = getButton(IDialogConstants.OK_ID);
        if (button != null && !button.isDisposed()) {
            button.setEnabled(enabled);
        }
    }

    private void verify() {
        boolean oldPasswordVerified = false;
        if (!hasPassword()) {
            oldPasswordVerified = !"".equals(newPasswordInputBox.getText()); //$NON-NLS-1$
        } else if (oldPasswordInputBox != null) {
            oldPasswordVerified = testsPassword(oldPasswordInputBox.getText());
            oldPasswordVerificationLabel.setImage(
                    oldPasswordVerified ? getDoneIcon() : getUndoneIcon());
        }
        boolean newPasswordVerified = ((oldPasswordInputBox != null //
                || !"".equals(newPasswordInputBox.getText()))) //$NON-NLS-1$
                && newPasswordInputBox.getText()
                        .equals(verifyNewPasswordInputBox.getText());
        newPasswordVerificationLabel.setImage(
                newPasswordVerified ? getDoneIcon() : getUndoneIcon());
        setOKButtonEnabled(oldPasswordVerified && newPasswordVerified);
    }

    @Override
    protected boolean okPressed() {
        setPassword(newPasswordInputBox.getText());
        setReturnCode(OK);
        close();
//        Display.getCurrent().asyncExec(new Runnable() {
//            public void run() {
//                if (EncryptionDailogPane.this.editor.parent == null
//                        || EncryptionDailogPane.this.editor.parent.isDisposed())
//                    return;
//
//                EncryptionDailogPane.this.editor
//                        .doSave(new NullProgressMonitor());
//            }
//        });
        return true;
    }

    protected void setPassword(String newPassword) {
        if ("".equals(newPassword)) { //$NON-NLS-1$
            newPassword = null;
        }
        this.password = newPassword;
    }

    @Override
    protected boolean cancelPressed() {
        setReturnCode(CANCEL);
        close();
        return true;
    }

//    private void close() {
//        editor.backCover.hideEncryptionDialog();
//        editor.hideBackCover();
//    }
//
    @Override
    public void setFocus() {
        if (oldPasswordInputBox != null && !oldPasswordInputBox.isDisposed()) {
            oldPasswordInputBox.setFocus();
        } else if (newPasswordInputBox != null
                && !newPasswordInputBox.isDisposed()) {
            newPasswordInputBox.setFocus();
        }
    }

    protected String getPassword() {
        return this.password;
    }

    protected boolean hasPassword() {
        return false;
    }

    protected boolean testsPassword(String password) {
        return false;
    }

}