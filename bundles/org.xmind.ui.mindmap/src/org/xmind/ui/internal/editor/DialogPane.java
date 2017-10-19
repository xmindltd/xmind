package org.xmind.ui.internal.editor;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.graphics.Rectangle;
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
import org.eclipse.ui.part.Page;

public abstract class DialogPane extends Page implements IDialogPane {

    private static final float CORNER_OFFSET = 20;

    private static final float CORNER_CONTROL_OFFSET = 5;

    private static final String DEFAULT_BUTTON_TRIGGER_EVENT_ID = "DEFAULT_BUTTON_TRIGGER_EVENT_ID"; //$NON-NLS-1$

    private int returnCode = OK;

    private IDialogPaneContainer paneContainer;

    private Map<Integer, Button> buttons;

    private int defaultButtonId = -1;

    private Font defaultFont;

    private Composite container;

    private Listener defaultButtonListener;

    private Listener escapeKeyListener;

    public void init(IDialogPaneContainer paneContainer) {
        this.paneContainer = paneContainer;
    }

    protected IDialogPaneContainer getPaneContainer() {
        return paneContainer;
    }

    public void createControl(Composite parent) {
        this.container = new Composite(parent, SWT.NONE);
        container.setBackground(parent.getDisplay().getSystemColor(
                SWT.COLOR_LIST_BACKGROUND));
        paintCornersFor(container, parent.getBackground());

        GridData gridData = new GridData(SWT.CENTER, SWT.CENTER, true, true);
        gridData.widthHint = getPreferredWidth();
        gridData.heightHint = SWT.DEFAULT;
        container.setLayoutData(gridData);

        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 30;
        gridLayout.marginHeight = 20;
        gridLayout.marginTop = 10;
        gridLayout.verticalSpacing = 20;
        gridLayout.horizontalSpacing = 0;
        container.setLayout(gridLayout);

        Control contents = createDialogContents(container);
        GridData gridData2 = new GridData(SWT.FILL, SWT.FILL, true, false);
        gridData2.widthHint = SWT.DEFAULT;
        gridData2.heightHint = SWT.DEFAULT;
        contents.setLayoutData(gridData2);

        createButtonBar(container);

        container.addListener(SWT.Traverse, getEscapeKeyListener());
    }

    protected int getPreferredWidth() {
        return 400;
    }

    protected void paintCornersFor(Composite control, final Color background) {
        if (background == null)
            return;
        control.addPaintListener(new PaintListener() {
            public void paintControl(PaintEvent e) {
                Control c = (Control) e.widget;
                Rectangle b = c.getBounds();
                float x1 = 0;
                float y1 = 0;
                float x2 = x1 + b.width;
                float y2 = y1 + b.height;

                Path p = new Path(e.display);

                p.moveTo(x1, y1);
                p.lineTo(x1 + CORNER_OFFSET, y1);
                p.cubicTo(x1 + CORNER_CONTROL_OFFSET, y1, x1, y1
                        + CORNER_CONTROL_OFFSET, x1, y1 + CORNER_OFFSET);
                p.close();

                p.moveTo(x2, y2);
                p.lineTo(x2 - CORNER_OFFSET, y2);
                p.cubicTo(x2 - CORNER_CONTROL_OFFSET, y2, x2, y2
                        - CORNER_CONTROL_OFFSET, x2, y2 - CORNER_OFFSET);
                p.close();

                e.gc.setAntialias(SWT.ON);
                e.gc.setBackground(background);
                e.gc.fillPath(p);
                p.dispose();
            }
        });
    }

    protected Control createDialogContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(parent.getBackground());
        return composite;
    }

    protected Composite getContainer() {
        return container;
    }

    public Control getControl() {
        return container;
    }

    public abstract void setFocus();

    public void dispose() {
        if (container != null) {
            container.dispose();
            container = null;
        }
        buttons = null;
    }

    protected abstract void createButtonsForButtonBar(Composite buttonBar);

    protected boolean buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {
            return okPressed();
        } else if (buttonId == IDialogConstants.CLOSE_ID) {
            return closePressed();
        } else if (buttonId == IDialogConstants.CANCEL_ID) {
            return cancelPressed();
        }
        return false;
    }

    /**
     * 
     */
    protected boolean cancelPressed() {
        return false;
    }

    /**
     * 
     */
    protected boolean closePressed() {
        return false;
    }

    /**
     * 
     */
    protected boolean okPressed() {
        return false;
    }

    protected void createButtonBar(Composite parent) {
        Composite buttonBar = new Composite(parent, SWT.NONE);
        buttonBar.setBackground(parent.getBackground());

        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
        gridData.widthHint = SWT.DEFAULT;
        gridData.heightHint = SWT.DEFAULT;
        buttonBar.setLayoutData(gridData);

        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        gridLayout.verticalSpacing = 0;
        gridLayout.horizontalSpacing = 5;
        buttonBar.setLayout(gridLayout);

        createBlankArea(buttonBar);
        createButtonsForButtonBar(buttonBar);
        adjustButtonWidths(buttonBar);
    }

    protected void createBlankArea(Composite buttonBar) {
        Label blank = new Label(buttonBar, SWT.NONE);
        blank.setBackground(buttonBar.getBackground());
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.widthHint = SWT.DEFAULT;
        gridData.heightHint = 1;
        blank.setLayoutData(gridData);
    }

    protected Button createButton(Composite buttonBar, int id, String label,
            boolean defaultButton) {
        ((GridLayout) buttonBar.getLayout()).numColumns++;
        Button button = new Button(buttonBar, SWT.PUSH);
        button.setText(label);
        button.setFont(JFaceResources.getDialogFont());
        button.setData(new Integer(id));
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                int pressId = (Integer) event.widget.getData();
                buttonPressed(pressId);
//                buttonPressed(((Integer) event.widget.getData()).intValue());
            }
        });
        if (defaultButton) {
            this.defaultButtonId = id;
        }
        if (buttons == null)
            buttons = new HashMap<Integer, Button>();
        buttons.put(new Integer(id), button);
        setButtonLayoutData(button);

        return button;
    }

    protected Button getButton(int id) {
        return buttons == null ? null : buttons.get(id);
    }

    protected void setButtonLayoutData(Button button) {
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, false, false);
        gridData.widthHint = SWT.DEFAULT;
        gridData.heightHint = SWT.DEFAULT;
        button.setLayoutData(gridData);
    }

    private void adjustButtonWidths(Composite buttonBar) {
        if (buttons == null)
            return;
        int maxWidth = 90;
        for (Button b : buttons.values()) {
            int width = b.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
            maxWidth = Math.max(maxWidth, width);
        }
        for (Button b : buttons.values()) {
            ((GridData) b.getLayoutData()).widthHint = maxWidth + 2;
        }
    }

    protected void addRefreshDefaultButtonListener(final Control focusControl) {
        focusControl.addListener(SWT.FocusIn, getDefaultButtonListener());
        focusControl.addListener(SWT.FocusOut, getDefaultButtonListener());
    }

    protected void addTriggerDefaultButtonListener(Control control,
            int triggerEvent) {
        control.addListener(triggerEvent, getDefaultButtonListener());
        control.setData(DEFAULT_BUTTON_TRIGGER_EVENT_ID,
                Integer.valueOf(triggerEvent));
    }

    private Listener getDefaultButtonListener() {
        if (defaultButtonListener == null) {
            defaultButtonListener = new Listener() {

                private Button savedDefaultButton = null;

                public void handleEvent(Event event) {
                    Object triggerEvent = event.widget
                            .getData(DEFAULT_BUTTON_TRIGGER_EVENT_ID);
                    if (triggerEvent instanceof Integer
                            && event.type == ((Integer) triggerEvent)
                                    .intValue()) {
                        triggerDefaultButton();
                        return;
                    }

                    if (event.type == SWT.FocusIn) {
                        changeDefaultButton();
                    } else if (event.type == SWT.FocusOut) {
                        restoreDefaultButton();
                    }
                }

                private void restoreDefaultButton() {
                    if (defaultButtonId >= 0) {
                        Shell shell = container.getShell();
                        if (savedDefaultButton != null
                                && savedDefaultButton.isDisposed()) {
                            savedDefaultButton = null;
                        }
                        shell.setDefaultButton(savedDefaultButton);
                    }
                }

                private void changeDefaultButton() {
                    if (defaultButtonId >= 0) {
                        final Shell shell = container.getShell();
                        savedDefaultButton = shell.getDefaultButton();
                        shell.getDisplay().asyncExec(new Runnable() {
                            public void run() {
                                Button button = getButton(defaultButtonId);
                                if (button != null && !button.isDisposed()) {
                                    shell.setDefaultButton(button);
                                }
                            }
                        });
                    }
                }
            };
        }
        return defaultButtonListener;
    }

    private Listener getEscapeKeyListener() {
        if (escapeKeyListener == null) {
            escapeKeyListener = new Listener() {
                public void handleEvent(Event event) {
                    if (isEscapeKeyPressed(event)) {
                        escapeKeyPressed();
                        event.doit = false;
                    }
                }

                private boolean isEscapeKeyPressed(Event event) {
                    return (event.type == SWT.Traverse && event.detail == SWT.TRAVERSE_ESCAPE)
                            || (event.type == SWT.KeyDown
                                    && event.keyCode == SWT.ESC && event.stateMask == 0);
                }
            };
        }
        return escapeKeyListener;
    }

    protected void escapeKeyPressed() {
        triggerButton(IDialogConstants.CANCEL_ID);
    }

    protected Button getDefaultButton() {
        if (buttons != null && defaultButtonId >= 0) {
            return getButton(defaultButtonId);
        }
        return null;
    }

    protected void triggerDefaultButton() {
        triggerButton(defaultButtonId);
    }

    protected boolean triggerButton(int buttonId) {
        if (buttonId >= 0) {
            Button button = getButton(buttonId);
            if (button != null && !button.isDisposed() && button.isEnabled()) {
                return buttonPressed(buttonId);
            }
        }
        return false;
    }

    protected void applyFont(Control control) {
        if (defaultFont != null) {
            control.setFont(defaultFont);
        }
    }

    protected void hookText(final Text text) {
        text.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event event) {
                text.selectAll();
            }
        });
    }

    public void setDefaultFont(Font font) {
        this.defaultFont = font;
    }

    protected void relayout() {
        if (container == null || container.isDisposed())
            return;
        container.getParent().layout(true);
    }

    public void setReturnCode(int code) {
        this.returnCode = code;
    }

    public int getReturnCode() {
        return returnCode;
    }

    protected boolean close() {
        return paneContainer.close();
    }

}
