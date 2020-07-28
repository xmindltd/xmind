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
package org.xmind.ui.internal.findreplace;

import static org.eclipse.jface.dialogs.IDialogConstants.CLIENT_ID;
import static org.eclipse.jface.dialogs.IDialogConstants.CLOSE_ID;
import static org.eclipse.jface.dialogs.IDialogConstants.CLOSE_LABEL;
import static org.eclipse.jface.dialogs.IDialogConstants.HORIZONTAL_SPACING;
import static org.eclipse.jface.dialogs.IDialogConstants.VERTICAL_SPACING;
import static org.xmind.ui.internal.findreplace.IFindReplaceOperationProvider.PARAM_CURRENT_MAP;
import static org.xmind.ui.internal.findreplace.IFindReplaceOperationProvider.PARAM_FORWARD;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.dialogs.DialogMessages;
import org.xmind.ui.resources.FontUtils;

/**
 * @author Frank Shaka
 */
public class FindReplaceDialog extends Dialog implements IPartListener {

    private static final String SETTINGS_SECTION_NAME = "org.xmind.ui.findreplace"; //$NON-NLS-1$

    private static final String P_PARAMETER = "parameter"; //$NON-NLS-1$

    private static final String EMPTY = ""; //$NON-NLS-1$

    private static final String STRING_NOT_FOUND = DialogMessages.FindReplaceDialog_StringNotFound;

    private static Map<IWorkbenchWindow, FindReplaceDialog> SINGLETONS = new HashMap<IWorkbenchWindow, FindReplaceDialog>();

    private static final int TEXT_WIDTH = 120;

    private static final int FIND_ID = CLIENT_ID + 1;
    private static final int FIND_ALL_ID = CLIENT_ID + 2;
    private static final int REPLACE_ID = CLIENT_ID + 3;
    private static final int REPLACE_ALL_ID = CLIENT_ID + 4;

    private static List<String> FindHistory = new ArrayList<String>();
    private static List<String> ReplaceHistory = new ArrayList<String>();

    private class EventHandler implements Listener {

        public void handleEvent(Event event) {
            if (event.type == SWT.Modify) {
                updateOperationButtons();
                infoLabel.setText(EMPTY);
            } else if (event.type == SWT.FocusIn) {
                if (event.widget instanceof Combo) {
                    Combo input = (Combo) event.widget;
                    input.setSelection(new Point(0, input.getText().length()));
                }
            } else if (event.type == SWT.Selection) {
                Button b = (Button) event.widget;
                if (opButtons != null && opButtons.containsValue(b)) {
                    buttonPressed((Integer) b.getData());
                } else if (paramWidgets != null) {
                    List<Button> list = paramWidgets.get(b.getData());
                    if (list.contains(b)) {
                        int param = ((Integer) b.getData()).intValue();
                        if (b.getSelection()) {
                            parameter |= param;
                        } else {
                            parameter &= ~param;
                        }
                        if (operationProvider != null) {
                            operationProvider.setParameter(param,
                                    b.getSelection());
                        }
                        updateOperationButtons();
                    }
                }
            }
        }

    }

    private IWorkbenchWindow window;

    private IWorkbenchPart currentPart;

    private Label contextLabel;

    private Combo findInput;

    private Combo replaceInput;

    private Label infoLabel;

    private Map<Integer, Button> opButtons = new HashMap<Integer, Button>();

    private Map<Integer, List<Button>> paramWidgets = new HashMap<Integer, List<Button>>();

    private Listener eventHandler = new EventHandler();

    private IFindReplaceOperationProvider operationProvider;

    private int parameter = PARAM_FORWARD | PARAM_CURRENT_MAP;

    private String initialFindText = null;

    protected FindReplaceDialog(final IWorkbenchWindow window) {
        super(window.getShell());
        this.window = window;
        setShellStyle(SWT.TITLE | SWT.MODELESS | SWT.CLOSE);
        SINGLETONS.put(window, this);
        window.getShell().addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                close();
                SINGLETONS.remove(window);
            }
        });
    }

    /**
     * 
     */
    private void startListeningToPartChanges() {
        window.getPartService().addPartListener(this);
    }

    private void stopListeningToPartChanges() {
        window.getPartService().removePartListener(this);
    }

    public void setInitialFindText(String initialFindText) {
        this.initialFindText = initialFindText;
    }

    private void loadParameter() {
        IDialogSettings ds = MindMapUIPlugin.getDefault().getDialogSettings(
                SETTINGS_SECTION_NAME);
        if (ds != null) {
            try {
                parameter = ds.getInt(P_PARAMETER);
            } catch (Exception e) {
            }
        }
    }

    private void saveParameter() {
        IDialogSettings ds = MindMapUIPlugin.getDefault().getDialogSettings(
                SETTINGS_SECTION_NAME);
        if (ds != null) {
            ds.put(P_PARAMETER, parameter);
        }
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#getInitialLocation(org.eclipse.swt.graphics.Point)
     */
    @Override
    protected Point getInitialLocation(Point initialSize) {
        Composite parent = getShell().getParent();

        Monitor monitor = getShell().getDisplay().getPrimaryMonitor();
        if (parent != null) {
            monitor = parent.getMonitor();
        }

        Rectangle monitorBounds = monitor.getClientArea();
        Point centerPoint;
        if (parent != null) {
            centerPoint = getDialogCenter(parent.getBounds());
        } else {
            centerPoint = getDialogCenter(monitorBounds);
        }

        return new Point(centerPoint.x - (initialSize.x / 2), Math.max(
                monitorBounds.y,
                Math.min(centerPoint.y - (initialSize.y / 2), monitorBounds.y
                        + monitorBounds.height - initialSize.y)));
    }

    private static Point getDialogCenter(Rectangle rect) {
        return new Point(rect.x + rect.width * 2 / 3, rect.y + rect.height / 2);
    }

    public void create() {
        super.create();
        IWorkbenchPart activePart = window.getActivePage().getActivePart();
        partActivated(activePart);
        updateContextName();
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        GridLayout layout = (GridLayout) composite.getLayout();
        layout.verticalSpacing = 10;
        layout.marginTop = layout.marginHeight;
        layout.marginHeight = 0;
        layout.marginBottom = 0;

        createContextLabel(composite);
        createInputGroup(composite);
        createParameterGroups(composite);
        createOperationButtonGroup(composite);

        return composite;
    }

    protected void createContextLabel(Composite parent) {
        contextLabel = new Label(parent, SWT.NONE);
        contextLabel.setFont(FontUtils.getNewHeight(
                JFaceResources.DEFAULT_FONT, 11));
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
        contextLabel.setLayoutData(layoutData);
    }

    /**
     * @param inputArea
     * @param factory
     */
    protected void createInputGroup(Composite parent) {
        Composite composite = new Composite(parent, SWT.NO_FOCUS);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout inputLayout = new GridLayout(2, false);
        inputLayout.marginWidth = 0;
        inputLayout.marginHeight = 0;
        composite.setLayout(inputLayout);

        findInput = createInputWidget(composite,
                DialogMessages.FindReplaceDialog_Find_label, FindHistory,
                initialFindText);
        replaceInput = createInputWidget(composite,
                DialogMessages.FindReplaceDialog_ReplaceWith_label,
                ReplaceHistory, null);
    }

    /**
     * @param parent
     * @param factory
     * @param label
     * @param history
     * @param initText
     */
    private Combo createInputWidget(Composite parent, String label,
            List<String> history, String initText) {
        Label labelWidget = new Label(parent, SWT.NONE);
        labelWidget.setText(label);
        labelWidget
                .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));

        final Combo input = new Combo(parent, SWT.SINGLE | SWT.BORDER
                | SWT.DROP_DOWN);
        GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, true, true);
        layoutData.widthHint = TEXT_WIDTH;
        input.setLayoutData(layoutData);
        for (String t : history) {
            input.add(t);
        }
        if (initText != null)
            input.setText(initText);
        else if (!history.isEmpty())
            input.setText(history.get(0));
        input.addListener(SWT.Modify, eventHandler);
        input.addListener(SWT.FocusIn, eventHandler);
        return input;
    }

    /**
     * @param optionsGroup
     * @param factory
     */
    protected void createParameterGroups(Composite parent) {
        Composite composite = new Composite(parent, SWT.NO_FOCUS);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout layout = new GridLayout(2, true);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.verticalSpacing = convertVerticalDLUsToPixels(VERTICAL_SPACING);
        layout.horizontalSpacing = convertHorizontalDLUsToPixels(HORIZONTAL_SPACING);
        composite.setLayout(layout);

        Composite optionGroup = createParameterGroup(composite,
                DialogMessages.FindReplaceDialog_OptionGroup);
        ((GridData) optionGroup.getLayoutData()).horizontalSpan = 2;
        createParameterWidget(optionGroup, SWT.CHECK,
                DialogMessages.FindReplaceDialog_CaseSensitive,
                IFindReplaceOperationProvider.PARAM_CASE_SENSITIVE);
        createParameterWidget(optionGroup, SWT.CHECK,
                DialogMessages.FindReplaceDialog_WholeWord,
                IFindReplaceOperationProvider.PARAM_WHOLE_WORD);

        Composite directionGroup = createParameterGroup(composite,
                DialogMessages.FindReplaceDialog_DirectionGroup);
        createParameterWidget(directionGroup, SWT.RADIO,
                DialogMessages.FindReplaceDialog_Forward,
                IFindReplaceOperationProvider.PARAM_FORWARD);
        createParameterWidget(directionGroup, SWT.RADIO,
                DialogMessages.FindReplaceDialog_Backward,
                IFindReplaceOperationProvider.PARAM_BACKWARD);

        Composite scopeGroup = createParameterGroup(composite,
                DialogMessages.FindReplaceDialog_ScopeGroup);
        createParameterWidget(scopeGroup, SWT.RADIO,
                DialogMessages.FindReplaceDialog_CurrentMap,
                IFindReplaceOperationProvider.PARAM_CURRENT_MAP);
        createParameterWidget(scopeGroup, SWT.RADIO,
                DialogMessages.FindReplaceDialog_Workbook,
                IFindReplaceOperationProvider.PARAM_WORKBOOK);
    }

    private Composite createParameterGroup(Composite composite, String text) {
        Group group = new Group(composite, SWT.NO_FOCUS);
        group.setText(text);
        GridData groupLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        group.setLayoutData(groupLayoutData);
        GridLayout groupLayout = new GridLayout();
        groupLayout.horizontalSpacing = 7;
        groupLayout.verticalSpacing = 7;
        group.setLayout(groupLayout);
        return group;
    }

    protected Button createParameterWidget(Composite parent, int style,
            String text, int paramId) {
        Button button = new Button(parent, style);
        button.setText(text);
        button.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        addParameterWidget(paramId, button);
        button.setData(Integer.valueOf(paramId));
        button.addListener(SWT.Selection, eventHandler);
        return button;
    }

    private void addParameterWidget(final int paramId, final Button widget) {
        Integer key = Integer.valueOf(paramId);
        List<Button> widgets = paramWidgets.get(key);
        if (widgets == null) {
            widgets = new ArrayList<Button>();
            paramWidgets.put(key, widgets);
        }
        widgets.add(widget);
        paramWidgets.put(key, widgets);
        widget.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                removeParameterWidget(paramId, widget);
            }
        });
    }

    private void removeParameterWidget(int paramId, Button widget) {
        Integer key = Integer.valueOf(paramId);
        List<Button> widgets = paramWidgets.get(key);
        if (widgets != null) {
            widgets.remove(widget);
            if (widgets.isEmpty()) {
                paramWidgets.remove(key);
            }
        }
    }

    private void updateParameterWidgets() {
        for (Integer paramId : paramWidgets.keySet()) {
            updateParameterWidget(paramId);
        }
    }

    private void updateParameterWidget(Integer paramId) {
        List<Button> widgets = paramWidgets.get(paramId);
        if (widgets != null) {
            boolean enabled = operationProvider != null
                    && operationProvider.understandsPatameter(paramId
                            .intValue());
            for (Button widget : widgets) {
                widget.setEnabled(enabled);
                widget.setSelection(hasParameter(paramId.intValue()));
            }
        }
    }

    private boolean hasParameter(int paramId) {
        if (operationProvider == null)
            return false;
        int bit = parameter & paramId;
        return bit != 0;
    }

    /**
     * @param buttons
     * @param factory
     */
    protected void createOperationButtonGroup(Composite parent) {
        Composite buttons = new Composite(parent, SWT.NO_FOCUS);
        buttons.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout buttonsLayout = new GridLayout(2, true);
        buttonsLayout.marginHeight = 0;
        buttonsLayout.marginWidth = 0;
        buttons.setLayout(buttonsLayout);
        createOperationButton(buttons, FIND_ID,
                DialogMessages.FindReplaceDialog_Find_text, true);
        createOperationButton(buttons, FIND_ALL_ID,
                DialogMessages.FindReplaceDialog_FindAll_text, false);
        createOperationButton(buttons, REPLACE_ID,
                DialogMessages.FindReplaceDialog_Replace_text, false);
        createOperationButton(buttons, REPLACE_ALL_ID,
                DialogMessages.FindReplaceDialog_ReplaceAll_text, false);
    }

    protected Button createOperationButton(Composite parent, int id,
            String text, boolean defaultButton) {
        Button button = new Button(parent, SWT.PUSH);
        button.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        button.setText(text);
        button.setData(id);
        button.addListener(SWT.Selection, eventHandler);
        if (defaultButton) {
            Shell shell = parent.getShell();
            if (shell != null) {
                shell.setDefaultButton(button);
            }
        }
        registerOperationButton(id, button);
        return button;
    }

    protected void registerOperationButton(final int id, final Button widget) {
        opButtons.put(Integer.valueOf(id), widget);
        widget.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                unregisterOperationButton(id, widget);
            }

        });
    }

    private void unregisterOperationButton(int id, Button widget) {
        Integer key = Integer.valueOf(id);
        if (opButtons.get(key) == widget) {
            opButtons.remove(key);
        }
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#getButton(int)
     */
    @Override
    protected Button getButton(int id) {
        Button button = super.getButton(id);
        if (button == null)
            button = opButtons.get(Integer.valueOf(id));
        return button;
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#createButtonBar(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createButtonBar(Composite parent) {
        Composite container = new Composite(parent, SWT.NO_FOCUS);
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout containerLayout = new GridLayout(2, false);
        containerLayout.horizontalSpacing = 0;
        containerLayout.verticalSpacing = 0;
        containerLayout.marginHeight = 7;
        containerLayout.marginWidth = 7;
        container.setLayout(containerLayout);

        infoLabel = new Label(container, SWT.NONE);
        infoLabel.setBackground(container.getBackground());
        infoLabel.setFont(container.getFont());
        infoLabel.setForeground(container.getDisplay().getSystemColor(
                SWT.COLOR_RED));
        infoLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true,
                true));

        Composite composite = (Composite) super.createButtonBar(container);
        GridLayout layout = (GridLayout) composite.getLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        return composite;
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, CLOSE_ID, CLOSE_LABEL, false);
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
     */
    @Override
    protected void buttonPressed(int buttonId) {
        if (CLOSE_ID == buttonId) {
            closePressed();
        } else if (FIND_ID == buttonId) {
            findPressed();
        } else if (FIND_ALL_ID == buttonId) {
            findAllPressed();
        } else if (REPLACE_ID == buttonId) {
            replacePressed();
        } else if (REPLACE_ALL_ID == buttonId) {
            replaceAllPressed();
        } else
            super.buttonPressed(buttonId);
    }

    /**
     * 
     */
    protected void findPressed() {
        saveHistories();
        if (operationProvider == null)
            return;
        if (operationProvider.find(getFindText())) {
            setInfoText(EMPTY);
        } else {
            setInfoText(STRING_NOT_FOUND);
        }
        final Shell shell = getShell();
        final Display display = shell.getDisplay();
        display.asyncExec(new Runnable() {
            public void run() {
                if (!shell.isDisposed()) {
                    shell.setActive();
                    findInput.setFocus();
                }
            }
        });
    }

    /**
     * 
     */
    protected void findAllPressed() {
        saveHistories();
        if (operationProvider == null)
            return;
        boolean all = (parameter & IFindReplaceOperationProvider.PARAM_ALL) != 0;
        operationProvider.setParameter(IFindReplaceOperationProvider.PARAM_ALL,
                true);
        if (operationProvider.find(getFindText())) {
            setInfoText(EMPTY);
        } else {
            setInfoText(STRING_NOT_FOUND);
        }
        operationProvider.setParameter(IFindReplaceOperationProvider.PARAM_ALL,
                all);
        findInput.setFocus();
    }

    /**
     * 
     */
    protected void replacePressed() {
        saveHistories();
        if (operationProvider == null)
            return;
        if (operationProvider.replace(getFindText(), getReplaceText())) {
            setInfoText(EMPTY);
        } else {
            setInfoText(STRING_NOT_FOUND);
        }
        findInput.setFocus();
    }

    /**
     * 
     */
    protected void replaceAllPressed() {
        saveHistories();
        if (operationProvider == null)
            return;
        boolean all = (parameter & IFindReplaceOperationProvider.PARAM_ALL) != 0;
        operationProvider.setParameter(IFindReplaceOperationProvider.PARAM_ALL,
                true);
        if (operationProvider.replace(getFindText(), getReplaceText())) {
            setInfoText(EMPTY);
        } else {
            setInfoText(STRING_NOT_FOUND);
        }
        operationProvider.setParameter(IFindReplaceOperationProvider.PARAM_ALL,
                all);
        findInput.setFocus();
    }

    protected void closePressed() {
        close();
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#close()
     */
    @Override
    public boolean close() {
        saveHistories();
        return super.close();
    }

    /**
     * @return
     */
    public String getFindText() {
        return findInput == null || findInput.isDisposed() ? EMPTY : findInput
                .getText();
    }

    /**
     * @return
     */
    public String getReplaceText() {
        return replaceInput == null || replaceInput.isDisposed() ? EMPTY
                : replaceInput.getText();
    }

    protected void setInfoText(String text) {
        if (infoLabel != null && !infoLabel.isDisposed()) {
            infoLabel.setText(text);
            infoLabel.getParent().layout();
        }
    }

    protected void saveHistories() {
        if (getShell() == null || getShell().isDisposed())
            return;
        saveHistory(FindHistory, findInput);
        saveHistory(ReplaceHistory, replaceInput);
    }

    protected void saveHistory(List<String> history, Combo input) {
        String text = input.getText();
        if (text == null || EMPTY.equals(text))
            return;
        if (history.remove(text) && !input.isDisposed())
            input.remove(text);
        history.add(0, text);
        if (!input.isDisposed()) {
            input.add(text, 0);
            input.setText(text);
        }
    }

    protected void updateOperationButtons() {
        if (getShell() == null || getShell().isDisposed())
            return;

        getButton(FIND_ID).setEnabled(canFind());
        getButton(FIND_ALL_ID).setEnabled(canFindAll());
        getButton(REPLACE_ID).setEnabled(canReplace());
        getButton(REPLACE_ALL_ID).setEnabled(canReplaceAll());
    }

    protected boolean canFind() {
        return getFindText() != null && !EMPTY.equals(getFindText())
                && operationProvider != null
                && operationProvider.canFind(getFindText());
    }

    protected boolean canReplace() {
        return canFind()
                && getReplaceText() != null
                && operationProvider != null
                && operationProvider
                        .canReplace(getFindText(), getReplaceText());
    }

    protected boolean canFindAll() {
        return getFindText() != null && !EMPTY.equals(getFindText())
                && operationProvider != null
                && operationProvider.canFindAll(getFindText());
    }

    protected boolean canReplaceAll() {
        return getFindText() != null
                && !EMPTY.equals(getFindText())
                && getReplaceText() != null
                && operationProvider != null
                && operationProvider.canReplaceAll(getFindText(),
                        getReplaceText());
    }

    /**
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(DialogMessages.FindReplaceDialog_windowTitle);
        loadParameter();
        startListeningToPartChanges();
        newShell.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                saveParameter();
                stopListeningToPartChanges();
            }
        });
    }

    private void updateContextName() {
        if (getShell() == null || getShell().isDisposed())
            return;

        String contextName = operationProvider == null ? null
                : operationProvider.getContextName(contextLabel.getSize().x,
                        contextLabel.getFont());
        if (contextName == null || "".equals(contextName)) { //$NON-NLS-1$
            contextLabel.setText(""); //$NON-NLS-1$
        } else {
            contextLabel.setText(contextName);
        }
        contextLabel.getParent().getParent().layout();
    }

    /**
     * @param operationProvider
     *            the operationProvider to set
     */
    public void setOperationProvider(
            IFindReplaceOperationProvider operationProvider) {
        this.operationProvider = operationProvider;
        if (operationProvider != null) {
            operationProvider.setParameter(parameter);
        }
    }

    /**
     * @return the operationProvider
     */
    public IFindReplaceOperationProvider getOperationProvider() {
        return operationProvider;
    }

    public void partActivated(IWorkbenchPart part) {
        this.currentPart = part;
        setOperationProvider(getOperationProvider(part));
        updateContextName();
        updateOperationButtons();
        updateParameterWidgets();
    }

    public void partBroughtToTop(IWorkbenchPart part) {
    }

    public void partClosed(IWorkbenchPart part) {
        if (part != this.currentPart)
            return;

        setOperationProvider(null);
        updateContextName();
        updateOperationButtons();
        updateParameterWidgets();
    }

    public void partDeactivated(IWorkbenchPart part) {
    }

    public void partOpened(IWorkbenchPart part) {
    }

    private IFindReplaceOperationProvider getOperationProvider(
            IWorkbenchPart part) {
        if (part == null)
            return null;
        return (IFindReplaceOperationProvider) part
                .getAdapter(IFindReplaceOperationProvider.class);
    }

    public static FindReplaceDialog getInstance(IWorkbenchWindow window) {
        FindReplaceDialog instance = SINGLETONS.get(window);
        if (instance != null)
            return instance;
        return new FindReplaceDialog(window);
    }
}