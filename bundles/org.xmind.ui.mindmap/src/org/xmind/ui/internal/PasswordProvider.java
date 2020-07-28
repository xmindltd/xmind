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
package org.xmind.ui.internal;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.xmind.ui.internal.editor.IPasswordProvider;
import org.xmind.ui.mindmap.IWorkbookRef;

/**
 * @author Frank Shaka
 * 
 */
public class PasswordProvider implements IPasswordProvider {

    private static class PasswordDialog extends Dialog {

        private String value;

        private String message;

        /**
         * @param parentShell
         */
        protected PasswordDialog(Shell parentShell, String message) {
            super(parentShell);
            setShellStyle(getShellStyle() | SWT.RESIZE);
            this.value = null;
            this.message = message;
        }

        protected void configureShell(Shell shell) {
            super.configureShell(shell);
            shell.setText(MindMapMessages.EncryptDialog_title);
        }

        @Override
        protected Point getInitialSize() {
            return new Point(300, 200);
        }

        @Override
        protected Control createDialogArea(Composite parent) {
            Composite composite = (Composite) super.createDialogArea(parent);

            Label messageLabel = new Label(composite, SWT.WRAP);
            messageLabel.setText(message);
            messageLabel.setLayoutData(
                    new GridData(SWT.FILL, SWT.FILL, true, false));

            final Text passwordInput = new Text(composite,
                    SWT.BORDER | SWT.SINGLE | SWT.PASSWORD);
            passwordInput.setLayoutData(
                    new GridData(SWT.FILL, SWT.FILL, true, false));
            passwordInput.addListener(SWT.Modify, new Listener() {
                public void handleEvent(Event event) {
                    value = passwordInput.getText();
                }
            });

            Display.getCurrent().asyncExec(new Runnable() {
                @Override
                public void run() {
                    passwordInput.setFocus();
                }
            });

            return composite;
        }

        public String getValue() {
            return value;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.ui.internal.editor.IPasswordProvider#askForPassword(org.xmind.
     * ui.mindmap.IWorkbookRef, java.lang.String)
     */
    @Override
    public String askForPassword(IWorkbookRef workbookRef, String message) {
        final String[] password = new String[1];
        password[0] = null;

        final String theMessage;
        if (message != null) {
            theMessage = message;
        } else if (workbookRef != null) {
            theMessage = NLS.bind(
                    MindMapMessages.PasswordProvider_askPassword_message,
                    workbookRef.getName());
        } else {
            theMessage = MindMapMessages.EncryteDialog_label_message;
        }

        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                PasswordDialog dialog = new PasswordDialog(null, theMessage);
                int ret = dialog.open();
                if (ret == PasswordDialog.OK) {
                    password[0] = dialog.getValue();
                }
            }
        });

        return password[0];
    }
}
