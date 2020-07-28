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
package org.xmind.ui.internal.wizards;

import java.io.File;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.xmind.ui.browser.BrowserSupport;
import org.xmind.ui.internal.MarkerImpExpUtils;
import org.xmind.ui.internal.dialogs.DialogMessages;
import org.xmind.ui.internal.e4models.IModelConstants;
import org.xmind.ui.internal.prefs.MarkerManagerPrefPage;
import org.xmind.ui.internal.utils.E4Utils;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.PrefUtils;

public class MarkerImportWizard extends Wizard implements IImportWizard {

    private static final String PAGE_NAME = "org.xmind.ui.MarkerImportWizardPage"; //$NON-NLS-1$

    private class MarkerImportWizardPage extends WizardPage {

        private class WidgetListener implements Listener {

            public void handleEvent(Event event) {
                handleWidgetEvent(event);
            }

        }

        private Button fromFileButton;

        private Button fromDirectoryButton;

        private Text fileInput;

        private Button fileBrowseButton;

        private Text folderInput;

        private Button folderBrowseButton;

        private FormToolkit formToolkit;

        private boolean modifyingPathInput = false;

        private boolean settingTargetPath = false;

        private boolean pathModified = false;

        private Listener widgetListener;

        protected MarkerImportWizardPage() {
            super(PAGE_NAME, WizardMessages.MarkerImportPage_title, null);
            setDescription(WizardMessages.MarkerImportPage_description);
        }

        public void createControl(Composite parent) {
            Composite composite = new Composite(parent, SWT.NONE);
            composite.setLayoutData(
                    new GridData(SWT.FILL, SWT.FILL, true, true));
            GridLayout gridLayout = new GridLayout(1, false);
            gridLayout.marginWidth = 5;
            gridLayout.marginHeight = 5;
            gridLayout.verticalSpacing = 5;
            gridLayout.horizontalSpacing = 5;
            composite.setLayout(gridLayout);
            setControl(composite);

            formToolkit = new FormToolkit(parent.getDisplay());
            composite.addDisposeListener(new DisposeListener() {
                public void widgetDisposed(DisposeEvent e) {
                    formToolkit.dispose();
                }
            });
            formToolkit.setBackground(null);

            fromFileButton = new Button(composite, SWT.RADIO);
            fromFileButton.setText(WizardMessages.ImportPage_FromFile_text);
            hookWidget(fromFileButton, SWT.Selection);

            Control fileGroup = createFileControls(composite);
            fileGroup.setLayoutData(
                    new GridData(SWT.FILL, SWT.FILL, true, false));

            Label blank = new Label(composite, SWT.NONE);
            blank.setLayoutData(
                    new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
            blank.setText(" "); //$NON-NLS-1$

            fromDirectoryButton = new Button(composite, SWT.RADIO);
            fromDirectoryButton
                    .setText(WizardMessages.MarkerImportPage_FromFolder_text);
            hookWidget(fromDirectoryButton, SWT.Selection);

            Control directoryGroup = createFolderGroup(composite);
            directoryGroup.setLayoutData(
                    new GridData(SWT.FILL, SWT.FILL, true, false));

            setFileOrFolder(true);
        }

        protected Control createFileControls(Composite parent) {
            Composite group = new Composite(parent, SWT.NONE);
            GridLayout layout = new GridLayout(3, false);
            layout.marginWidth = 0;
            layout.marginHeight = 0;
            group.setLayout(layout);

            Label blank = new Label(group, SWT.WRAP);
            blank.setLayoutData(new GridData(GridData.BEGINNING,
                    GridData.CENTER, false, false));
            blank.setText("  "); //$NON-NLS-1$

            fileInput = new Text(group, SWT.SINGLE | SWT.BORDER);
            if (getSourcePath() != null) {
                fileInput.setText(getSourcePath());
            }
            fileInput.setLayoutData(
                    new GridData(GridData.FILL, GridData.FILL, true, false));
            hookWidget(fileInput, SWT.Modify);
            hookWidget(fileInput, SWT.FocusIn);

            fileBrowseButton = new Button(group, SWT.PUSH);
            fileBrowseButton.setText(WizardMessages.ImportPage_Browse_text);
            int width = fileBrowseButton.computeSize(SWT.DEFAULT,
                    SWT.DEFAULT).x;
            GridData layoutData = new GridData(GridData.END, GridData.CENTER,
                    false, false);
            layoutData.widthHint = Math.max(93, width);
            fileBrowseButton.setLayoutData(layoutData);
            hookWidget(fileBrowseButton, SWT.Selection);

            FormText descriptionText = createDescriptionText(group);
            descriptionText.setText(
                    WizardMessages.MarkerImportPage_FromFile_description, true,
                    true);

            return group;
        }

        private FormText createDescriptionText(Composite parent) {
            FormText descriptionText = formToolkit.createFormText(parent, true);
            descriptionText.addHyperlinkListener(new HyperlinkAdapter() {
                public void linkActivated(HyperlinkEvent e) {
                    final Object href = e.getHref();
                    if (href instanceof String) {
                        SafeRunner.run(new SafeRunnable() {
                            public void run() throws Exception {
                                BrowserSupport.getInstance().createBrowser()
                                        .openURL((String) href);
                            }
                        });
                    }
                }
            });
            descriptionText.setLayoutData(
                    new GridData(SWT.FILL, SWT.FILL, true, false));
            ((GridData) descriptionText.getLayoutData()).horizontalSpan = 3;
            ((GridData) descriptionText.getLayoutData()).widthHint = 400;
            return descriptionText;
        }

        private Control createFolderGroup(Composite parent) {
            Composite group = new Composite(parent, SWT.NONE);
            GridLayout layout = new GridLayout(3, false);
            layout.marginWidth = 0;
            layout.marginHeight = 0;
            group.setLayout(layout);

            Label blank = new Label(group, SWT.WRAP);
            blank.setLayoutData(new GridData(GridData.BEGINNING,
                    GridData.CENTER, false, false));
            blank.setText("  "); //$NON-NLS-1$

            folderInput = new Text(group, SWT.SINGLE | SWT.BORDER);
            if (getSourcePath() != null) {
                folderInput.setText(getSourcePath());
            }
            folderInput.setLayoutData(
                    new GridData(GridData.FILL, GridData.FILL, true, false));
            hookWidget(folderInput, SWT.Modify);
            hookWidget(folderInput, SWT.FocusIn);

            folderBrowseButton = new Button(group, SWT.PUSH);
            folderBrowseButton.setText(WizardMessages.ImportPage_Browse_text);
            int width = folderBrowseButton.computeSize(SWT.DEFAULT,
                    SWT.DEFAULT).x;
            GridData layoutData = new GridData(GridData.END, GridData.CENTER,
                    false, false);
            layoutData.widthHint = Math.max(93, width);
            folderBrowseButton.setLayoutData(layoutData);
            hookWidget(folderBrowseButton, SWT.Selection);

            FormText descriptionText = createDescriptionText(group);
            descriptionText.setText(
                    WizardMessages.MarkerImportPage_FromFolder_description,
                    true, true);

            return group;
        }

        protected void hookWidget(Widget widget, int eventType) {
            if (widgetListener == null) {
                widgetListener = new WidgetListener();
            }
            widget.addListener(eventType, widgetListener);
        }

        protected void handleWidgetEvent(Event event) {
            if (event.widget == fileInput) {
                if (event.type == SWT.Modify) {
                    pathModified = true;
                    if (!settingTargetPath) {
                        modifyingPathInput = true;
                        setSourcePath(fileInput.getText());
                        modifyingPathInput = false;
                    }
                    updateStatus();
                } else if (event.type == SWT.FocusIn) {
                    fileInput.setSelection(
                            new Point(0, fileInput.getText().length()));
                }
            } else if (event.widget == fileBrowseButton) {
                openFileDialog();
                fileInput.setFocus();
            } else if (event.widget == folderInput) {
                if (event.type == SWT.Modify) {
                    pathModified = true;
                    if (!settingTargetPath) {
                        modifyingPathInput = true;
                        setSourcePath(folderInput.getText());
                        modifyingPathInput = false;
                    }
                    updateStatus();
                } else if (event.type == SWT.FocusIn) {
                    folderInput.setSelection(
                            new Point(0, folderInput.getText().length()));
                }
            } else if (event.widget == folderBrowseButton) {
                openDirectoryDialog();
                folderInput.setFocus();
            } else if (event.widget == fromFileButton) {
                setFileOrFolder(true);
            } else if (event.widget == fromDirectoryButton) {
                setFileOrFolder(false);
            }
        }

        private void setFileOrFolder(boolean fileOrFolder) {
            fromFileButton.setSelection(fileOrFolder);
            fromDirectoryButton.setSelection(!fileOrFolder);
            fileInput.setEnabled(fileOrFolder);
            fileBrowseButton.setEnabled(fileOrFolder);
            folderInput.setEnabled(!fileOrFolder);
            folderBrowseButton.setEnabled(!fileOrFolder);
            Text input = fileOrFolder ? fileInput : folderInput;
            input.setFocus();
            setSourcePath(input.getText());
        }

        public void dispose() {
            super.dispose();
            fileInput = null;
            fileBrowseButton = null;
            folderBrowseButton = null;
            folderInput = null;
            modifyingPathInput = false;
            settingTargetPath = false;
            pathModified = false;
        }

        protected void openFileDialog() {
            FileDialog dialog = createFileDialog();
            String path = dialog.open();
            if (path != null) {
                setSourcePath(path);
            }
        }

        protected FileDialog createFileDialog() {
            FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
            dialog.setText(WizardMessages.ImportPage_FileDialog_text);
            String ext = "*" + MindMapUI.FILE_EXT_MARKER_PACKAGE; //$NON-NLS-1$
            dialog.setFilterExtensions(new String[] { ext });
            dialog.setFilterNames(new String[] { NLS.bind("{0} ({1})", //$NON-NLS-1$
                    DialogMessages.MarkerPackageFilterName, ext) });
            if (getSourcePath() != null) {
                File file = new File(getSourcePath());
                dialog.setFilterPath(file.getParent());
                dialog.setFileName(file.getName());
            }
            return dialog;
        }

        protected void openDirectoryDialog() {
            DirectoryDialog dialog = createDirectoryDialog();
            String path = dialog.open();
            if (path != null) {
                setSourcePath(path);
            }
        }

        private DirectoryDialog createDirectoryDialog() {
            DirectoryDialog dialog = new DirectoryDialog(getShell(), SWT.OPEN);
            dialog.setText(WizardMessages.MarkerImportPage_FolderDialog_title);
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
            MarkerImportWizard.this.setSourcePath(path);
            if (!modifyingPathInput) {
                settingTargetPath = true;
                if (fileInput.isEnabled()) {
                    fileInput.setText(path);
                } else {
                    folderInput.setText(path);
                }
                settingTargetPath = false;
            }
        }

    }

    private boolean openMarkerManagerPageOnFinish;

    private String sourcePath;

    private IWorkbench workbench;

    public MarkerImportWizard() {
        this(true);
    }

    public MarkerImportWizard(boolean openMarkerManagerPageOnFinish) {
        this.openMarkerManagerPageOnFinish = openMarkerManagerPageOnFinish;
        setWindowTitle(WizardMessages.MarkerImportWizard_windowTitle);
    }

    public void addPages() {
        addPage(new MarkerImportWizardPage());
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public boolean hasSourcePath() {
        return sourcePath != null;
    }

    public boolean performFinish() {
        final boolean[] finished = new boolean[1];
        SafeRunner.run(new SafeRunnable() {
            public void run() throws Exception {
                MarkerImpExpUtils.importMarkerPackage(sourcePath);
                finished[0] = true;
                if (openMarkerManagerPageOnFinish) {
                    openMarkerManagerPage();
                } else {
                    openMarkerView();
                }
            }

            public void handleException(Throwable e) {
                finished[0] = false;
                super.handleException(e);
            }
        });
        return finished[0];
    }

    private void openMarkerView() {
        if (workbench != null) {
            IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
            if (window != null) {
                E4Utils.showPart(IModelConstants.COMMAND_SHOW_MODEL_PART,
                        window, IModelConstants.PART_ID_MARKERS, null,
                        IModelConstants.PART_STACK_ID_RIGHT);
            }
        }
    }

    private void openMarkerManagerPage() {
        Display.getCurrent().asyncExec(new Runnable() {
            public void run() {
                PrefUtils.openPrefDialog(null, MarkerManagerPrefPage.ID);
            }
        });
    }

    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.workbench = workbench;
    }

}
