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
package org.xmind.ui.wizards;

import java.io.File;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.xmind.ui.internal.wizards.WizardMessages;

public abstract class AbstractMindMapImportPage extends WizardPage {

    private class WidgetListener implements Listener {

        public void handleEvent(Event event) {
            handleWidgetEvent(event);
        }

    }

    private Text pathInput;

    private Button browseButton;

    private Button currentWorkbookWidget;

    private Button newWorkbookWidget;

    private boolean modifyingPathInput = false;

    private boolean settingTargetPath = false;

    private boolean pathModified = false;

    private Listener widgetListener = null;

    protected AbstractMindMapImportPage(String pageName, String title) {
        super(pageName, title, null);
    }

    protected AbstractMindMapImportWizard getCastedWizard() {
        return (AbstractMindMapImportWizard) super.getWizard();
    }

    protected Control createFileControls(Composite parent) {
        Composite group = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(3, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        group.setLayout(layout);

        Label toFileLabel = new Label(group, SWT.WRAP);
        toFileLabel.setLayoutData(new GridData(GridData.BEGINNING,
                GridData.CENTER, false, true));
        toFileLabel.setText(WizardMessages.ImportPage_FromFile_text);

        pathInput = new Text(group, SWT.SINGLE | SWT.BORDER);
        if (getSourcePath() != null) {
            pathInput.setText(getSourcePath());
        }
        pathInput.setLayoutData(new GridData(GridData.FILL, GridData.CENTER,
                true, true));
        hookWidget(pathInput, SWT.Modify);
        hookWidget(pathInput, SWT.FocusIn);

        browseButton = new Button(group, SWT.PUSH);
        browseButton.setText(WizardMessages.ImportPage_Browse_text);
        int width = browseButton.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
        GridData layoutData = new GridData(GridData.END, GridData.CENTER,
                false, true);
        layoutData.widthHint = Math.max(93, width);
        browseButton.setLayoutData(layoutData);
        hookWidget(browseButton, SWT.Selection);

        return group;
    }

    protected Control createDestinationControl(Composite parent) {
        Group group = new Group(parent, SWT.NONE);
        group.setLayout(new GridLayout(1, false));
        group.setText(WizardMessages.ImportPage_DestinationGroup_title);

        currentWorkbookWidget = new Button(group, SWT.RADIO);
        currentWorkbookWidget
                .setText(WizardMessages.ImportPage_CurrentWorkbook_text);
        currentWorkbookWidget.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
                true, false));
        hookWidget(currentWorkbookWidget, SWT.Selection);

        newWorkbookWidget = new Button(group, SWT.RADIO);
        newWorkbookWidget.setText(WizardMessages.ImportPage_NewWorkbook_text);
        newWorkbookWidget.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                false));
        hookWidget(newWorkbookWidget, SWT.Selection);

        currentWorkbookWidget.setEnabled(getCastedWizard().hasTargetWorkbook());
        boolean toNewWorkbook = getCastedWizard().isToNewWorkbook();
        currentWorkbookWidget.setSelection(!toNewWorkbook);
        newWorkbookWidget.setSelection(toNewWorkbook);

        return group;
    }

    public void dispose() {
        super.dispose();
        pathInput = null;
        browseButton = null;
        currentWorkbookWidget = null;
        newWorkbookWidget = null;
        modifyingPathInput = false;
        settingTargetPath = false;
        pathModified = false;
    }

    protected void hookWidget(Widget widget, int eventType) {
        if (widgetListener == null) {
            widgetListener = new WidgetListener();
        }
        widget.addListener(eventType, widgetListener);
    }

    protected void handleWidgetEvent(Event event) {
        if (event.widget == pathInput) {
            if (event.type == SWT.Modify) {
                pathModified = true;
                if (!settingTargetPath) {
                    modifyingPathInput = true;
                    setSourcePath(pathInput.getText());
                    modifyingPathInput = false;
                }
                updateStatus();
            } else if (event.type == SWT.FocusIn) {
                pathInput.setSelection(new Point(0, pathInput.getText()
                        .length()));
            }
        } else if (event.widget == browseButton) {
            openBrowseDialog();
            pathInput.setFocus();
        } else if (event.widget == currentWorkbookWidget) {
            getCastedWizard().setToNewWorkbook(false);
        } else if (event.widget == newWorkbookWidget) {
            getCastedWizard().setToNewWorkbook(true);
        }
    }

    protected void openBrowseDialog() {
        FileDialog dialog = createBrowseDialog();
        String path = dialog.open();
        if (path != null) {
            setSourcePath(path);
        }
    }

    protected FileDialog createBrowseDialog() {
        FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
        dialog.setText(WizardMessages.ImportPage_FileDialog_text);
        if (getSourcePath() != null) {
            File file = new File(getSourcePath());
            dialog.setFilterPath(file.getParent());
            dialog.setFileName(file.getName());
        }
        return dialog;
    }

    protected void updateStatus() {
        setPageComplete(isPageCompletable());
        String warningMessage = generateWarningMessage();
        if (warningMessage != null) {
            setMessage(warningMessage, WARNING);
        } else {
            setMessage(null);
        }
        setErrorMessage(generateErrorMessage());
    }

    protected String generateWarningMessage() {
        return null;
    }

    protected String generateErrorMessage() {
        if (pathModified && !hasSourcePath()) {
            return WizardMessages.ImportPage_FileNotExists_message;
        }
        return null;
    }

    protected boolean isPageCompletable() {
        return hasSourcePath();
    }

    protected void setSourcePath(String path) {
        getCastedWizard().setSourcePath(path);
        if (!modifyingPathInput) {
            settingTargetPath = true;
            pathInput.setText(path);
            settingTargetPath = false;
        }
    }

    protected String getSourcePath() {
        return getCastedWizard().getSourcePath();
    }

    protected boolean hasSourcePath() {
        return getCastedWizard().hasSourcePath();
    }

}