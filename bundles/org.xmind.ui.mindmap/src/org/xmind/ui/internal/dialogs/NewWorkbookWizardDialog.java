/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.ui.internal.dialogs;

import static org.eclipse.jface.dialogs.IDialogConstants.OPEN_ID;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbenchWindow;
import org.xmind.ui.internal.wizards.NewFromTemplateWizard;

@Deprecated
public class NewWorkbookWizardDialog extends WizardDialog {

    private IWorkbenchWindow window;

    private boolean withOpenButton;

    private NewWorkbookWizardDialog(IWorkbenchWindow window, IWizard newWizard,
            boolean withOpenButton) {
        super(window.getShell(), newWizard);
        this.window = window;
        this.withOpenButton = withOpenButton;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Control area = super.createDialogArea(parent);
        Control progressPart = (Control) getProgressMonitor();
        GridData data = (GridData) progressPart.getLayoutData();
        data.exclude = true;
        return area;
    }

    @Override
    protected Control createButtonBar(Composite parent) {
        Control bar = super.createButtonBar(parent);
        if (withOpenButton) {
            GridData data = (GridData) bar.getLayoutData();
            data.grabExcessHorizontalSpace = true;
            data.horizontalAlignment = SWT.FILL;
        }
        return bar;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        if (withOpenButton) {
            createButton(parent, OPEN_ID,
                    DialogMessages.NewWorkbookWizardDialog_OpenExistingFile_text,
                    false);
        }
        super.createButtonsForButtonBar(parent);
        Button finishButton = getButton(IDialogConstants.FINISH_ID);
        if (finishButton != null) {
            finishButton.setText(
                    DialogMessages.NewWorkbookWizardDialog_Choose_text);
        }
        if (withOpenButton) {
            GridData data = (GridData) parent.getLayoutData();
            data.grabExcessHorizontalSpace = true;
            data.horizontalAlignment = SWT.FILL;
        }
    }

    @Override
    protected void setButtonLayoutData(Button button) {
        super.setButtonLayoutData(button);
        if (button == getButton(OPEN_ID)) {
            GridData data = (GridData) button.getLayoutData();
            data.horizontalAlignment = SWT.LEFT;
            data.grabExcessHorizontalSpace = true;
        }
    }

    @Override
    protected void buttonPressed(int buttonId) {
        if (buttonId == OPEN_ID) {
            openButtonPressed();
        } else {
            super.buttonPressed(buttonId);
        }
    }

    private void openButtonPressed() {
        Shell shell = getShell();
        shell.setVisible(false);
        IEditorPart[] editors = new OpenWorkbookDialog(window).open();
        if (editors == null || editors.length == 0) {
            if (!shell.isDisposed()) {
                shell.setVisible(true);
                shell.setActive();
            }
        } else {
            close();
        }
    }

    public static int openWizard(IWorkbenchWindow window,
            boolean withOpenButton) {
        INewWizard wizard = new NewFromTemplateWizard();
        ISelection selection = window.getSelectionService().getSelection();
        if (selection instanceof IStructuredSelection) {
            wizard.init(window.getWorkbench(),
                    (IStructuredSelection) selection);
        } else {
            wizard.init(window.getWorkbench(), StructuredSelection.EMPTY);
        }
        NewWorkbookWizardDialog dialog = new NewWorkbookWizardDialog(window,
                wizard, withOpenButton);
        return dialog.open();
    }

}
