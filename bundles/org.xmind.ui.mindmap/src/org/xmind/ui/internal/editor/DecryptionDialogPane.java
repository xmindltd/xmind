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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.MindMapUI;

public class DecryptionDialogPane extends DialogPane {

    private Text messageBoard;

    private Text passwordInputBox;

    private Label iconLabel;

    private String password;

    private String message = null;

    private boolean errorOccurred = false;

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
        createPasswordInputBox(composite);
        return composite;
    }

    protected void createButtonsForButtonBar(Composite buttonBar) {
        createOkButton(buttonBar);
        createCloseButton(buttonBar);
    }

    private void createOkButton(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
                true);
    }

    private void createCloseButton(Composite parent) {
        createButton(parent, IDialogConstants.CANCEL_ID,
                IDialogConstants.CANCEL_LABEL, false);
    }

    private void createPasswordInputBox(Composite parent) {
        passwordInputBox = new Text(parent,
                SWT.BORDER | SWT.PASSWORD | SWT.SINGLE);
        applyFont(passwordInputBox);

        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
        gridData.widthHint = SWT.DEFAULT;
        gridData.heightHint = SWT.DEFAULT;
        passwordInputBox.setLayoutData(gridData);

        hookText(passwordInputBox);
        addRefreshDefaultButtonListener(passwordInputBox);
        addTriggerDefaultButtonListener(passwordInputBox, SWT.DefaultSelection);
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

        createIcon(area);
        createMessageBoard(area);
    }

    private void createIcon(Composite parent) {
        iconLabel = new Label(parent, SWT.NONE);
        iconLabel.setBackground(parent.getBackground());
        iconLabel.setLayoutData(
                new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        iconLabel.setImage(getImage(iconLabel));
    }

    private Image getImage(Control control) {
        if (errorOccurred)
            return control.getDisplay().getSystemImage(SWT.ICON_ERROR);

        ImageDescriptor image = MindMapUI.getImages().get(IMindMapImages.UNLOCK,
                true);
        if (image != null)
            return image.createImage(control.getDisplay());
        return null;
    }

    private void createMessageBoard(Composite parent) {
        messageBoard = new Text(parent, SWT.READ_ONLY | SWT.MULTI | SWT.WRAP);
        messageBoard.setBackground(parent.getBackground());
        applyFont(messageBoard);

        GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gridData.widthHint = SWT.DEFAULT;
        gridData.heightHint = SWT.DEFAULT;
        messageBoard.setLayoutData(gridData);
        if (message != null) {
            messageBoard.setText(message);
        }
    }

    @Override
    protected boolean cancelPressed() {
        setReturnCode(CANCEL);
        close();
        return true;
    }

    protected boolean okPressed() {
        this.password = passwordInputBox.getText();
        setReturnCode(OK);
        close();
        return true;
    }

    public void dispose() {
        super.dispose();
        passwordInputBox = null;
        messageBoard = null;
    }

    public void setFocus() {
        if (passwordInputBox != null && !passwordInputBox.isDisposed()) {
            passwordInputBox.setFocus();
        }
    }

    public void setContent(String message, boolean errorOrWarning) {
        this.message = message;
        this.errorOccurred = errorOrWarning;
        if (messageBoard != null && !messageBoard.isDisposed()) {
            messageBoard.setText(message);
        }
        if (iconLabel != null && !iconLabel.isDisposed()) {
            iconLabel.setImage(getImage(iconLabel));
        }
        relayout();
    }

    @Override
    protected void escapeKeyPressed() {
        triggerButton(IDialogConstants.CLOSE_ID);
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

}