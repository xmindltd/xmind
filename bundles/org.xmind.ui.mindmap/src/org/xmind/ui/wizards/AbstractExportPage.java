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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;
import org.xmind.ui.internal.wizards.WizardMessages;

public abstract class AbstractExportPage extends WizardPage {

    private class WidgetListener implements Listener {

        public void handleEvent(Event event) {
            handleWidgetEvent(event);
        }

    }

    protected static final String FILTER_ALL_FILES = "*.*"; //$NON-NLS-1$

    private Combo pathInput;

    private Button browseButton;

    private Button overwriteCheckButton;

    private boolean modifyingPathInput = false;

    private boolean settingTargetPath = false;

    private Listener widgetListener = null;

    protected AbstractExportPage(String pageName) {
        super(pageName);
    }

    protected AbstractExportPage(String pageName, String title,
            ImageDescriptor titleImage) {
        super(pageName, title, titleImage);
    }

    protected AbstractExportWizard getCastedWizard() {
        return (AbstractExportWizard) super.getWizard();
    }

    protected String getTargetPath() {
        return getCastedWizard().getTargetPath();
    }

    protected List<String> getPathHistory() {
        return getCastedWizard().getPathHistory();
    }

    protected boolean hasTargetPath() {
        return getCastedWizard().hasTargetPath();
    }

    protected Control createFileControls(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        gridLayout.verticalSpacing = 10;
        gridLayout.horizontalSpacing = 0;
        composite.setLayout(gridLayout);

        Composite group = new Composite(composite, SWT.NONE);
        GridLayout layout = new GridLayout(3, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        group.setLayout(layout);
        group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        Label toFileLabel = new Label(group, SWT.WRAP);
        toFileLabel.setLayoutData(
                new GridData(GridData.BEGINNING, GridData.CENTER, false, true));
        toFileLabel.setText(WizardMessages.ExportPage_ToFile_text);

        pathInput = new Combo(group,
                SWT.DROP_DOWN | SWT.SIMPLE | SWT.SINGLE | SWT.BORDER);

        Point preferredSize = pathInput.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        GridData comboLayoutData = new GridData(
                org.eclipse.draw2d.GridData.FILL_HORIZONTAL);
        comboLayoutData.widthHint = preferredSize.x;
        pathInput.setLayoutData(comboLayoutData);

        for (String path : getCastedWizard().getPathHistory()) {
            pathInput.add(path, 0);
        }

        String targetPath = getTargetPath();
        if (targetPath != null)
            pathInput.setText(targetPath);

        hookWidget(pathInput, SWT.Modify);
        hookWidget(pathInput, SWT.FocusIn);

        browseButton = new Button(group, SWT.PUSH);
        browseButton.setText(WizardMessages.ExportPage_Browse_text);
        int width = browseButton.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
        GridData layoutData = new GridData(GridData.END, GridData.CENTER, false,
                true);
        layoutData.widthHint = Math.max(93, width);
        browseButton.setLayoutData(layoutData);
        hookWidget(browseButton, SWT.Selection);

        overwriteCheckButton = new Button(composite, SWT.CHECK);
        overwriteCheckButton.setLayoutData(
                new GridData(GridData.FILL, GridData.CENTER, true, false));
        overwriteCheckButton.setText(
                WizardMessages.ExportPage_OverwriteWithoutWarning_text);
        overwriteCheckButton
                .setSelection(getCastedWizard().isOverwriteWithoutPrompt());
        hookWidget(overwriteCheckButton, SWT.Selection);

        updateStatus();

        return composite;
    }

    public void dispose() {
        super.dispose();
        pathInput = null;
        browseButton = null;
        overwriteCheckButton = null;
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
                if (!settingTargetPath) {
                    modifyingPathInput = true;
                    setTargetPath(pathInput.getText());
                    modifyingPathInput = false;
                }
                updateStatus();
            } else if (event.type == SWT.FocusIn) {
                pathInput.setSelection(
                        new Point(0, pathInput.getText().length()));
            }
        } else if (event.widget == browseButton) {
            openBrowseDialog();
            pathInput.setFocus();
        } else if (event.widget == overwriteCheckButton) {
            setOverwriteWithoutPrompt(overwriteCheckButton.getSelection());
            updateStatus();
        }
    }

    protected void setOverwriteWithoutPrompt(boolean selection) {
        getCastedWizard().setOverwriteWithoutPrompt(selection);
    }

    protected void openBrowseDialog() {
        FileDialog dialog = createBrowseDialog();
        String path = dialog.open();
        if (path != null)
            setTargetPath(path);
    }

    protected FileDialog createBrowseDialog() {
        FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
        dialog.setText(WizardMessages.ExportPage_FileDialog_title);

        List<String> filterNames = new ArrayList<String>(4);
        List<String> filterExtensions = new ArrayList<String>(4);

        filterNames.add(WizardMessages.ExportPage_FileDialog_AllFiles);
        filterExtensions.add(FILTER_ALL_FILES);

        setDialogFilters(dialog, filterNames, filterExtensions);

        if (getTargetPath() != null) {
            File file = new File(getTargetPath());
            dialog.setFilterPath(file.getParent());
            dialog.setFileName(file.getName());
        } else {
            dialog.setFileName(getCastedWizard().getSuggestedFileName());
        }
        return dialog;
    }

    protected void setDialogFilters(FileDialog dialog, List<String> filterNames,
            List<String> filterExtensions) {
        dialog.setFilterNames(
                filterNames.toArray(new String[filterNames.size()]));
        dialog.setFilterExtensions(
                filterExtensions.toArray(new String[filterExtensions.size()]));
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
        if (hasTargetPath()) {
            if (!getCastedWizard().isOverwriteWithoutPrompt()
                    && new File(getTargetPath()).exists())
                return WizardMessages.ExportPage_FileExists_message;
        }
        return null;
    }

    protected String generateErrorMessage() {
        return null;
    }

    protected boolean isPageCompletable() {
        return hasTargetPath();
    }

    protected void setTargetPath(String path) {
        getCastedWizard().setTargetPath(path);
        if (!modifyingPathInput) {
            settingTargetPath = true;
            pathInput.setText(path);
            settingTargetPath = false;
        }
    }

}
