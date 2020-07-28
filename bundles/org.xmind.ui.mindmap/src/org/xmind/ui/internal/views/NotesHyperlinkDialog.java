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
package org.xmind.ui.internal.views;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.xmind.ui.internal.MindMapMessages;

public class NotesHyperlinkDialog extends Dialog {

    private String href;

    private String displayText;

    public NotesHyperlinkDialog(Shell parentShell, String oldHyperlink,
            String oldText) {
        super(parentShell);
        this.displayText = oldText == null ? "" : oldText; //$NON-NLS-1$
        this.href = oldHyperlink == null ? "" : oldHyperlink; //$NON-NLS-1$
        setShellStyle(getShellStyle() | SWT.RESIZE);
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText(MindMapMessages.NotesHyperlinkDialog_title);
    }

    @Override
    protected Point getInitialSize() {
        return new Point(330, 220);
    }

    @Override
    public void create() {
        super.create();
        updateButtons();
    }

    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        createHrefInputArea(composite);
        createDisplayTextInputArea(composite);
        return composite;
    }

    private void createDisplayTextInputArea(Composite parent) {
        Composite area = new Composite(parent, SWT.NONE);
        area.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        gridLayout.verticalSpacing = 0;
        gridLayout.horizontalSpacing = 0;
        area.setLayout(gridLayout);

        Label label = new Label(area, SWT.NONE);
        label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        label.setText(MindMapMessages.NotesHyperlinkDialog_display_text);

        Text displayTextInput = new Text(area,
                SWT.SINGLE | SWT.LEAD | SWT.BORDER);
        displayTextInput
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        displayTextInput.setText(displayText);
        displayTextInput.addListener(SWT.Modify, new Listener() {
            public void handleEvent(Event event) {
                displayText = ((Text) event.widget).getText();
                updateButtons();
            }
        });
        displayTextInput.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event event) {
                ((Text) event.widget).selectAll();
            }
        });
    }

    private void createHrefInputArea(Composite parent) {
        Composite area = new Composite(parent, SWT.NONE);
        area.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        gridLayout.verticalSpacing = 0;
        gridLayout.horizontalSpacing = 0;
        area.setLayout(gridLayout);

        Label label = new Label(area, SWT.NONE);
        label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        label.setText(
                MindMapMessages.NotesHyperlinkDialog_hyperlinkReference_text);

        Text text = new Text(area, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
        text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        text.setText(href);
        text.addListener(SWT.Modify, new Listener() {
            public void handleEvent(Event event) {
                href = ((Text) event.widget).getText();
                updateButtons();
            }
        });
        text.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event event) {
                ((Text) event.widget).selectAll();
            }
        });
        text.setFocus();
        text.setSelection(text.getCharCount());
    }

    private void updateButtons() {
        Button okButton = getButton(IDialogConstants.OK_ID);
        if (okButton != null) {
            okButton.setEnabled(!"".equals(href)); //$NON-NLS-1$
        }
    }

    public String getDisplayText() {
        return displayText;
    }

    public String getHref() {
        return href;
    }

}
