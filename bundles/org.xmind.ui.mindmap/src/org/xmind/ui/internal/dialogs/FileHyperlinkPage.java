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
package org.xmind.ui.internal.dialogs;

import java.io.File;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;
import org.xmind.ui.dialogs.HyperlinkPage;
import org.xmind.ui.internal.editor.MME;
import org.xmind.ui.internal.protocols.FilePathParser;

/**
 * 
 * @author Frank Shaka
 */
public class FileHyperlinkPage extends HyperlinkPage implements Listener {

//    private IEditorPart editor;

    private String basePath = null;

    private String path;

    private boolean relative;

    private File file;

    private Composite composite;

    private Text pathInput;

    private Button relativeButton;

    private Button absoluteButton;

    private Text absolutePathPreview;

    private Button fileChooser;

    private Button folderChooser;

    private boolean ignoreModify = false;

    private boolean warningFileNotExists = false;

//    private boolean warningRelative = false;

    public FileHyperlinkPage() {
    }

    public void init(IEditorPart editor, IStructuredSelection selection) {
//        this.editor = editor;
        File workbookFile = MME.getFile(editor.getEditorInput());
        if (workbookFile != null) {
            this.basePath = workbookFile.getParent();
        }
        if (this.basePath == null)
            this.basePath = FilePathParser.ABSTRACT_FILE_BASE;
    }

    public void createControl(Composite parent) {
        composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));

        createLabel(composite);
        createPathInput(composite);
        createOptionsArea(composite);
        createPathChoosers(composite);
    }

    private void createLabel(Composite parent) {
        Label label = new Label(parent, SWT.WRAP);
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        ((GridData) label.getLayoutData()).widthHint = 380;
        label.setText(DialogMessages.FileHyperlinkPage_label);
    }

    private void createPathInput(Composite parent) {
        pathInput = new Text(parent, SWT.SINGLE | SWT.BORDER);
        pathInput.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        pathInput.addListener(SWT.Modify, this);
    }

    private void createOptionsArea(Composite parent) {
        Group group = new Group(parent, SWT.NONE);
        group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        GridLayout layout = new GridLayout(2, true);

        group.setLayout(layout);
        group.setText(DialogMessages.FileHyperlinkPage_HrefGroup_Text);

        absoluteButton = new Button(group, SWT.RADIO);
        absoluteButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                false));
        absoluteButton
                .setText(DialogMessages.FileHyperlinkPage_AbsoluteButton_Text);
        absoluteButton.setSelection(true);
        absoluteButton.addListener(SWT.Selection, this);

        relativeButton = new Button(group, SWT.RADIO);
        relativeButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                false));
        relativeButton
                .setText(DialogMessages.FileHyperlinkPage_RelativeButton_Text);
        relativeButton.addListener(SWT.Selection, this);

        absolutePathPreview = new Text(group, SWT.BORDER | SWT.SINGLE
                | SWT.READ_ONLY);
        absolutePathPreview.setBackground(group.getBackground());
        GridData absolutePathPreviewLayoutData = new GridData(SWT.FILL,
                SWT.FILL, true, false);
        absolutePathPreviewLayoutData.horizontalSpan = 2;
        absolutePathPreview.setLayoutData(absolutePathPreviewLayoutData);
    }

    private void createPathChoosers(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        GridLayout layout = new GridLayout(3, false);
        layout.marginWidth = 5;
        layout.marginHeight = 5;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 5;
        composite.setLayout(layout);

        Label label = new Label(composite, SWT.NONE);
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        createFileChooser(composite);
        createFolderChooser(composite);

        int maxWidth = 98;
        for (Control c : composite.getChildren()) {
            if (c instanceof Button) {
                maxWidth = Math.max(maxWidth,
                        c.computeSize(SWT.DEFAULT, SWT.DEFAULT).x);
            }
        }
        for (Control c : composite.getChildren()) {
            if (c instanceof Button && c.getLayoutData() instanceof GridData) {
                ((GridData) c.getLayoutData()).widthHint = maxWidth;
            }
        }
    }

    private void createFileChooser(Composite parent) {
        fileChooser = new Button(parent, SWT.PUSH);
        fileChooser.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
                false));
        fileChooser.setText(DialogMessages.FileHyperlinkPage_ChooseFile_text);
        fileChooser.addListener(SWT.Selection, this);
    }

    private void createFolderChooser(Composite parent) {
        folderChooser = new Button(parent, SWT.PUSH);
        folderChooser.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
                false));
        folderChooser
                .setText(DialogMessages.FileHyperlinkPage_ChooseFolder_text);
        folderChooser.addListener(SWT.Selection, this);
    }

    @Override
    public void setValue(String value) {
        super.setValue(value);
        setPath(value == null ? null : FilePathParser.toPath(value));
    }

    protected void update() {
        if (!ignoreModify) {
            if (pathInput != null && !pathInput.isDisposed()) {
                ignoreModify = true;
                pathInput.setText(path == null ? "" : path); //$NON-NLS-1$
                ignoreModify = false;
            }
        }
        if (absoluteButton != null && !absoluteButton.isDisposed()) {
            absoluteButton.setSelection(!relative);
        }
        if (relativeButton != null && !relativeButton.isDisposed()) {
            relativeButton.setSelection(relative);
        }
        if (absolutePathPreview != null && !absolutePathPreview.isDisposed()) {
            absolutePathPreview
                    .setText(file == null ? "" : file.getAbsolutePath()); //$NON-NLS-1$
        }
        warningFileNotExists = (file != null && !file.exists());
//        warningRelative = (relative && basePath == null);
        updateWarningMessage();

        super.setValue(computeURI());

        if (getValue() != null) {
            if (composite != null && !composite.isDisposed()) {
                composite.setFocus();
            }
        }

        setCanFinish(getValue() != null);
    }

//    @Override
//    public boolean tryFinish() {
//        if (file != null && relative) {
//            if (basePath == null) {
//                editor.doSaveAs();
//                File newFilePath = MME.getFile(editor.getEditorInput());
//                if (newFilePath == null)
//                    return false;
//                basePath = newFilePath.getParent();
//                String relativePath = FilePathParser.toRelativePath(basePath,
//                        file.getAbsolutePath());
//                if (relativePath != null) {
//                    super.setValue(FilePathParser.toURI(relativePath, relative));
//                }
//            }
//        }
//        return super.tryFinish();
//    }

    public void handleEvent(Event event) {
        if (event.widget == pathInput) {
            if (event.type == SWT.Modify) {
                if (!ignoreModify) {
                    ignoreModify = true;
                    setPath(pathInput.getText());
                    ignoreModify = false;
                }
            }
        } else if (event.widget == fileChooser) {
            FileDialog dialog = createFileDialog();
            String path = dialog.open();
            if (path != null) {
                setFile(path);
            }
        } else if (event.widget == folderChooser) {
            DirectoryDialog dialog = createFolderDialog();
            String path = dialog.open();
            if (path != null) {
                setFile(path);
            }
        } else if (event.widget == relativeButton) {
            setRelative(true);
        } else if (event.widget == absoluteButton) {
            setRelative(false);
        }
    }

    protected DirectoryDialog createFolderDialog() {
        DirectoryDialog dialog = new DirectoryDialog(composite.getShell(),
                SWT.OPEN | SWT.SINGLE);
        dialog.setText(DialogMessages.FileHyperlinkPage_OpenFileDialog_windowTitle);
        return dialog;
    }

    protected FileDialog createFileDialog() {
        FileDialog dialog = new FileDialog(composite.getShell(), SWT.OPEN
                | SWT.SINGLE);
        dialog.setText(DialogMessages.FileHyperlinkPage_OpenFileDialog_windowTitle);
        return dialog;
    }

    protected void setPath(String path) {
        this.path = (path == null || "".equals(path)) ? null : path; //$NON-NLS-1$
        this.relative = path == null ? false : FilePathParser
                .isPathRelative(path);
        this.file = path == null ? null
                : (relative ? new File(FilePathParser.toAbsolutePath(basePath,
                        path)) : new File(path));
        update();
    }

    protected void setFile(String fullPath) {
        this.file = fullPath == null || "".equals(fullPath) ? null : new File(fullPath); //$NON-NLS-1$
        this.path = this.file == null ? null : (relative ? FilePathParser
                .toRelativePath(basePath, fullPath) : fullPath);
        update();
    }

    protected void setRelative(boolean relative) {
        this.relative = relative;
//        if (basePath != null) {
        this.path = this.file == null ? null : (relative ? FilePathParser
                .toRelativePath(basePath, file.getAbsolutePath()) : file
                .getAbsolutePath());
//        }
        update();
    }

    private void updateWarningMessage() {
        setMessage(
                warningFileNotExists ? DialogMessages.FileHyperlinkPage_FileNotExists_message
                        : null, WARNING);
    }

    private String computeURI() {
        return path == null ? null : FilePathParser.toURI(path, relative);
    }

    public void dispose() {
    }

    public Control getControl() {
        return composite;
    }

    public void setFocus() {
        if (pathInput != null && !pathInput.isDisposed())
            pathInput.setFocus();
    }
}
