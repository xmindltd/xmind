package org.xmind.ui.internal.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.xmind.ui.texteditor.MEmbeddedEditor;
import org.xmind.ui.viewers.MButton;

public class AuthorInfoEditor extends MEmbeddedEditor implements Listener {

    private Text input;

    public AuthorInfoEditor(Composite parent) {
        super(parent, MButton.NO_ARROWS);
    }

    @Override
    protected void createEditor(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(parent.getBackground());
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout gridLayout = new GridLayout();
        gridLayout.marginWidth = 2;
        gridLayout.marginHeight = 2;
        gridLayout.verticalSpacing = 0;
        gridLayout.horizontalSpacing = 3;
        composite.setLayout(gridLayout);

        createInput(composite);
    }

    private void createInput(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(parent.getBackground());
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout gridLayout = new GridLayout();
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        gridLayout.verticalSpacing = 0;
        gridLayout.horizontalSpacing = 2;
        composite.setLayout(gridLayout);

        input = new Text(composite, SWT.SINGLE | SWT.BORDER);
        input.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
        input.addListener(SWT.Traverse, this);
        input.addListener(SWT.DefaultSelection, this);
        input.addListener(SWT.FocusOut, this);
    }

    public Text getInput() {
        return input;
    }

    public void setInput(Text input) {
        this.input = input;
    }

    @Override
    protected void setEditorFocus() {
        input.setFocus();
        input.setSelection(input.getText().length(), input.getText().length());
    }

    public void handleEvent(Event event) {
        if (event.type == SWT.Traverse) {
            if (event.detail == SWT.TRAVERSE_ESCAPE) {
                cancelEditing();
            }
        } else if (event.type == SWT.DefaultSelection) {
            endEditing();
        } else if (event.type == SWT.FocusOut) {
            endEditingWhenFocusOut();
        }
    }

}
