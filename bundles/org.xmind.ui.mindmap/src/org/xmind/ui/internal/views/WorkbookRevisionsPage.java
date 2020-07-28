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

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.Page;
import org.xmind.core.Core;
import org.xmind.core.IMeta;
import org.xmind.core.IWorkbook;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventRegistration;
import org.xmind.core.event.ICoreEventSource;
import org.xmind.gef.command.Command;
import org.xmind.gef.command.ICommandStack;
import org.xmind.gef.ui.actions.IActionRegistry;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.commands.CommandMessages;
import org.xmind.ui.commands.ModifyMetadataCommand;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.resources.FontUtils;
import org.xmind.ui.tabfolder.PageBookPage;

public class WorkbookRevisionsPage extends PageBookPage
        implements ICoreEventListener, Listener, IPropertyListener {

    private static final String K_AUTO_SAVE = IMeta.CONFIG_AUTO_REVISION_GENERATION;

    private static final String V_YES = IMeta.V_YES;

    private static final String V_NO = IMeta.V_NO;

    private Button autoSaveOption = null;

    private IWorkbook workbook;

    private ICoreEventRegistration coreEventReg = null;

    private Composite container;

    /**
     * 
     */
    public WorkbookRevisionsPage(IGraphicalEditor editor) {
        super(editor);
        this.workbook = (IWorkbook) editor.getAdapter(IWorkbook.class);
    }

    public IGraphicalEditor getEditor() {
        return (IGraphicalEditor) super.getSourcePageProvider();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.ui.tabfolder.PageBookPage#createControl(org.eclipse.swt.widgets
     * .Composite)
     */
    @Override
    public void createControl(Composite parent) {
        container = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        gridLayout.verticalSpacing = 0;
        gridLayout.horizontalSpacing = 0;
        container.setLayout(gridLayout);

        Composite contentContainer = new Composite(container, SWT.NONE);
        contentContainer.setLayout(new FillLayout());
        GridData contentLayoutData = new GridData(SWT.FILL, SWT.FILL, true,
                true);
        contentLayoutData.widthHint = SWT.DEFAULT;
        contentLayoutData.heightHint = SWT.DEFAULT;
        contentContainer.setLayoutData(contentLayoutData);

        super.createControl(contentContainer);

        Composite optionContainer = new Composite(container, SWT.NONE);
        GridLayout optionLayout = new GridLayout(1, false);
        optionLayout.marginWidth = 5;
        optionLayout.marginHeight = 5;
        optionLayout.verticalSpacing = 5;
        optionLayout.horizontalSpacing = 5;
        optionContainer.setLayout(optionLayout);
        GridData optionLayoutData = new GridData(SWT.FILL, SWT.FILL, true,
                false);
        optionLayoutData.widthHint = SWT.DEFAULT;
        optionLayoutData.heightHint = SWT.DEFAULT;
        optionContainer.setLayoutData(optionLayoutData);

        fillOptions(optionContainer);

        handleWorkbookChange();
        getEditor().addPropertyListener(this);
        autoSaveOption.addListener(SWT.Selection, this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.ui.tabfolder.PageBookPage#getControl()
     */
    @Override
    public Control getControl() {
        return container;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.ui.tabfolder.PageBookPage#dispose()
     */
    @Override
    public void dispose() {
        getEditor().removePropertyListener(this);
        if (coreEventReg != null) {
            coreEventReg.unregister();
            coreEventReg = null;
        }
        super.dispose();
    }

    private void fillOptions(Composite parent) {
//        titleLabel = new Label(parent, SWT.NONE);
//        titleLabel.setFont(FontUtils.getNewHeight(JFaceResources.DEFAULT_FONT,
//                11));
//        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
//        titleLabel.setLayoutData(layoutData);

        autoSaveOption = new Button(parent, SWT.CHECK | SWT.WRAP);
        autoSaveOption.setFont(
                FontUtils.getNewHeight(JFaceResources.DEFAULT_FONT, -1));
        autoSaveOption.setText(
                MindMapMessages.WorkbookRevisionsPage_AutoSaveRevisionsCheck_text);
        autoSaveOption
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.ui.tabfolder.PageBookPage#createDefaultPage(org.eclipse.swt
     * .widgets.Composite)
     */
    @Override
    protected Control createDefaultPage(Composite parent) {
        return new Composite(parent, SWT.NONE);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.ui.tabfolder.PageBookPage#doCreateNestedPage(java.lang.Object)
     */
    @Override
    protected Page doCreateNestedPage(Object sourcePage) {
        return new RevisionsPage((IGraphicalEditorPage) sourcePage);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.ui.tabfolder.PageBookPage#refreshGlobalActionHandlers()
     */
    @Override
    protected void refreshGlobalActionHandlers() {
        super.refreshGlobalActionHandlers();

        // Set new actions from editor.
        IActionRegistry registry = (IActionRegistry) getEditor()
                .getAdapter(IActionRegistry.class);
        if (registry != null) {
            initGlobalActionHandlers(getSite().getActionBars(), registry);
        }
    }

    protected void initGlobalActionHandlers(IActionBars bars,
            IActionRegistry registry) {
        setGlobalActionHandler(bars, registry, ActionFactory.UNDO.getId());
        setGlobalActionHandler(bars, registry, ActionFactory.REDO.getId());
    }

    protected void setGlobalActionHandler(IActionBars bars,
            IActionRegistry registry, String actionId) {
        IAction action = registry.getAction(actionId);
        if (action != null) {
            bars.setGlobalActionHandler(actionId, action);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.core.event.ICoreEventListener#handleCoreEvent(org.xmind.core
     * .event.CoreEvent)
     */
    public void handleCoreEvent(CoreEvent event) {
        if (getControl() == null || getControl().isDisposed())
            return;
        if (K_AUTO_SAVE.equals(event.getTarget())) {
            getControl().getDisplay().asyncExec(new Runnable() {
                public void run() {
                    updateAutoSaveOption();
                }
            });
        }
    }

    /**
     * 
     */
    private void updateAutoSaveOption() {
        if (workbook != null) {
            autoSaveOption.setEnabled(true);
            autoSaveOption.setSelection(isAutoSave(workbook));
        } else {
            autoSaveOption.setEnabled(false);
        }
    }

    private static final boolean isAutoSave(IWorkbook workbook) {
        String value = workbook.getMeta().getValue(K_AUTO_SAVE);
        return value == null || V_YES.equalsIgnoreCase(value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.
     * Event)
     */
    public void handleEvent(Event event) {
        if (event.type == SWT.Selection) {
            boolean selection = ((Button) event.widget).getSelection();
            Command command = new ModifyMetadataCommand(workbook, K_AUTO_SAVE,
                    selection ? V_YES : V_NO);
            command.setLabel(selection
                    ? CommandMessages.Command_TurnOnAutoRevisionSaving
                    : CommandMessages.Command_TurnOffAutoRevisionSaving);
            ICommandStack commandStack = getEditor().getCommandStack();
            if (commandStack != null) {
                commandStack.execute(command);
            } else {
                command.execute();
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IPropertyListener#propertyChanged(java.lang.Object,
     * int)
     */
    public void propertyChanged(Object source, int propId) {
        if (propId == IEditorPart.PROP_INPUT) {
            handleWorkbookChange();
        }
    }

    private void handleWorkbookChange() {
        if (coreEventReg != null) {
            coreEventReg.unregister();
            coreEventReg = null;
        }
        workbook = (IWorkbook) getEditor().getAdapter(IWorkbook.class);
        if (workbook != null) {
            IMeta meta = workbook.getMeta();
            if (meta instanceof ICoreEventSource) {
                coreEventReg = ((ICoreEventSource) meta)
                        .registerCoreEventListener(Core.Metadata, this);
            }
        }
        updateAutoSaveOption();
    }

}