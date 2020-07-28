/*
 * *****************************************************************************
 * * Copyright (c) 2006-2012 XMind Ltd. and others. This file is a part of XMind
 * 3. XMind releases 3 and above are dual-licensed under the Eclipse Public
 * License (EPL), which is available at
 * http://www.eclipse.org/legal/epl-v10.html and the GNU Lesser General Public
 * License (LGPL), which is available at http://www.gnu.org/licenses/lgpl.html
 * See https://www.xmind.net/license.html for details. Contributors: XMind Ltd. -
 * initial API and implementation
 *******************************************************************************/
package org.xmind.ui.internal.views;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.IContributedContentsView;
import org.eclipse.ui.part.ViewPart;
import org.xmind.core.Core;
import org.xmind.core.ISheet;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.CoreEventRegister;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventRegister;
import org.xmind.core.event.ICoreEventRegistration;
import org.xmind.core.event.ICoreEventSource;
import org.xmind.core.event.ICoreEventSupport;
import org.xmind.core.style.IStyle;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.MindMapUI;

public class ThemesView extends ViewPart implements IContributedContentsView,
        IPartListener, IPageChangedListener, ICoreEventListener {

    private static final String GROUP_FILE = IWorkbenchActionConstants.GROUP_FILE;

    private static final String GROUP_OPEN = "group.open"; //$NON-NLS-1$

    private static final String GROUP_SHOW_IN = IWorkbenchActionConstants.GROUP_SHOW_IN;

    private static final String GROUP_EDIT = "group.edit"; //$NON-NLS-1$

    private static final String GROUP_REORGANIZE = IWorkbenchActionConstants.GROUP_REORGANIZE;

    private static final String GROUP_GENERATE = "group.generate"; //$NON-NLS-1$

    private static final String GROUP_PROPERTIES = "group.properties"; //$NON-NLS-1$

    private static final String KEY_LINK_TO_EDITOR = "LINK_TO_EDITOR"; //$NON-NLS-1$

    private class ToggleLinkEditorAction extends Action {
        public ToggleLinkEditorAction() {
            super(MindMapMessages.ThemesView_LinkWithEditor_text, AS_CHECK_BOX);
            setToolTipText(MindMapMessages.ThemesView_LinkWithEditor_toolTip);
            setImageDescriptor(
                    MindMapUI.getImages().get(IMindMapImages.SYNCED, true));
            setDisabledImageDescriptor(
                    MindMapUI.getImages().get(IMindMapImages.SYNCED, false));
            setChecked(isLinkingToEditor());
        }

        public void run() {
            setLinkingToEditor(isChecked());
        }
    }

    protected class SetDefaultThemeAction extends Action {

        public SetDefaultThemeAction() {
            super(MindMapMessages.DefaultThemeAction_text,
                    IAction.AS_PUSH_BUTTON);
            setToolTipText(MindMapMessages.DefaultThemeAction_toolTip);
            setImageDescriptor(MindMapUI.getImages()
                    .get(IMindMapImages.DEFAULT_THEME, true));
            setDisabledImageDescriptor(MindMapUI.getImages()
                    .get(IMindMapImages.DEFAULT_THEME, false));
            setEnabled(getSelectionStyle() != null);
        }

        public void run() {
            IStyle style = getSelectionStyle();
            if (style == null)
                return;
            MindMapUI.getResourceManager().setDefaultTheme(style.getId());
            viewer.setInput(ThemeUICore.getInstance());
        }

        private IStyle getSelectionStyle() {
            if (viewer == null)
                return null;
            ISelection selection = viewer.getSelection();
            Object obj = ((IStructuredSelection) selection).getFirstElement();
            if (obj instanceof IStyle)
                return (IStyle) obj;
            return null;
        }
    }

    private IGraphicalEditor activeEditor;

    private ICoreEventRegistration currentSheetEventReg;

    private CategorizedThemeViewer viewer;

    private IDialogSettings dialogSettings;

    private boolean linkingToEditor;

    private ICoreEventRegister register = null;

    private SetDefaultThemeAction setDefaultThemeAction;

    public void init(IViewSite site) throws PartInitException {
        super.init(site);
    }

    public CategorizedThemeViewer getViewer() {
        return viewer;
    }

    public void createPartControl(Composite parent) {
        setDefaultThemeAction = new SetDefaultThemeAction();

        dialogSettings = MindMapUIPlugin.getDefault()
                .getDialogSettings(getClass().getName());
        if (dialogSettings.get(KEY_LINK_TO_EDITOR) == null) {
            dialogSettings.put(KEY_LINK_TO_EDITOR, true);
        }
        linkingToEditor = dialogSettings != null
                && dialogSettings.getBoolean(KEY_LINK_TO_EDITOR);

        parent.setLayout(new FillLayout());

        Composite container = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 0;
        container.setLayout(layout);
        parent.setLayout(new FillLayout());

        MenuManager contextMenu = new MenuManager("#PopupMenu"); //$NON-NLS-1$
        contextMenu.add(setDefaultThemeAction);
        contextMenu.add(new Separator(GROUP_FILE));
        contextMenu.add(new Separator(GROUP_OPEN));
        contextMenu.add(new GroupMarker(GROUP_SHOW_IN));
        contextMenu.add(new Separator(GROUP_EDIT));
        contextMenu.add(new Separator(GROUP_REORGANIZE));
        contextMenu.add(new Separator(GROUP_GENERATE));
        contextMenu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        contextMenu.add(new Separator(GROUP_PROPERTIES));
        container.setMenu(contextMenu.createContextMenu(container));

        viewer = new CategorizedThemeViewer(container);
        viewer.getControl()
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        getSite().registerContextMenu(contextMenu, viewer);

        viewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                ISelection selection = event.getSelection();
                if (setDefaultThemeAction != null) {
                    setDefaultThemeAction.setEnabled(!selection.isEmpty());
                }
            }
        });

        IEditorPart editor = getSite().getPage().getActiveEditor();
        if (editor instanceof IGraphicalEditor) {
            setActiveEditor((IGraphicalEditor) editor);
        }

        ToggleLinkEditorAction toggleLinkingAction = new ToggleLinkEditorAction();

        IMenuManager menu = getViewSite().getActionBars().getMenuManager();
        menu.add(setDefaultThemeAction);
        menu.add(toggleLinkingAction);
        menu.add(new Separator());
        menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        menu.add(new Separator());
        menu.add(new Separator(GROUP_EDIT));

        getSite().setSelectionProvider(viewer);
        getSite().getPage().addPartListener(this);

        ICoreEventSupport ces = (ICoreEventSupport) MindMapUI
                .getResourceManager().getUserThemeSheet()
                .getAdapter(ICoreEventSupport.class);
        register = new CoreEventRegister(this);
        register.setNextSupport(ces);
        register.register(Core.StyleAdd);
        register.register(Core.StyleRemove);
        register.register(Core.Name);
    }

    public void dispose() {
        if (register != null) {
            register.unregisterAll();
            register = null;
        }
        getSite().getPage().removePartListener(this);
        getSite().setSelectionProvider(null);

        setActiveEditor(null);

        super.dispose();
        viewer = null;
        dialogSettings = null;
        setDefaultThemeAction = null;
    }

    public void setFocus() {
        if (viewer != null && !viewer.getControl().isDisposed()) {
            viewer.getControl().setFocus();
        }
    }

    public IWorkbenchPart getContributingPart() {
        return getSite().getPage().getActiveEditor();
    }

    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
        if (adapter == IContributedContentsView.class) {
            return this;
        }
        return super.getAdapter(adapter);
    }

    private boolean isLinkingToEditor() {
        return linkingToEditor;
    }

    private void setLinkingToEditor(boolean linking) {
        if (linking == this.linkingToEditor)
            return;

        this.linkingToEditor = linking;
        if (dialogSettings != null) {
            dialogSettings.put(KEY_LINK_TO_EDITOR, linking);
        }
        if (linking)
            updateSelection();
    }

    private Object getViewerInput() {
        return ThemeUICore.getInstance();
    }

    private void updateSelection() {
        if (!isLinkingToEditor())
            return;

        if (viewer == null || viewer.getControl().isDisposed())
            return;
        String themeId = getCurrentThemeId();
        IStyle theme = MindMapUI.getResourceManager().getBlankTheme();
        if (themeId != null && !theme.getId().equals(themeId)) {
            theme = MindMapUI.getResourceManager().getSystemThemeSheet()
                    .findStyle(themeId);
            if (theme == null)
                theme = MindMapUI.getResourceManager().getUserThemeSheet()
                        .findStyle(themeId);
        }
        viewer.setSelection(theme == null ? StructuredSelection.EMPTY
                : new StructuredSelection(theme));
    }

    private String getCurrentThemeId() {
        if (activeEditor == null)
            return null;
        IGraphicalEditorPage page = activeEditor.getActivePageInstance();
        if (page == null)
            return null;
        ISheet sheet = (ISheet) page.getAdapter(ISheet.class);
        if (sheet == null)
            return null;
        String themeId = sheet.getThemeId();
        return themeId;
    }

    public void partActivated(IWorkbenchPart part) {
        if (!(part instanceof IGraphicalEditor))
            return;

        setActiveEditor((IGraphicalEditor) part);
    }

    private void setActiveEditor(IGraphicalEditor editor) {
        if (editor == this.activeEditor)
            return;

        if (this.activeEditor != null) {
            unhook(this.activeEditor);
        }
        this.activeEditor = editor;
        if (editor != null) {
            hook(editor);
        }
        setCurrentSheet(findCurrentSheet());
        updateSelection();
    }

    private void setCurrentSheet(ISheet sheet) {
        if (currentSheetEventReg != null) {
            currentSheetEventReg.unregister();
            currentSheetEventReg = null;
        }
        if (sheet != null) {
            hookSheet(sheet);
        }
    }

    private void hookSheet(ISheet sheet) {
        ICoreEventSupport ces = (ICoreEventSupport) sheet
                .getAdapter(ICoreEventSupport.class);
        if (ces != null) {
            currentSheetEventReg = ces.registerCoreEventListener(
                    (ICoreEventSource) sheet, Core.ThemeId, this);
        }
    }

    private ISheet findCurrentSheet() {
        if (activeEditor == null)
            return null;
        IGraphicalEditorPage page = activeEditor.getActivePageInstance();
        if (page == null)
            return null;
        ISheet sheet = (ISheet) page.getAdapter(ISheet.class);
        return sheet;
    }

    private void hook(IGraphicalEditor editor) {
        editor.addPageChangedListener(this);
    }

    private void unhook(IGraphicalEditor editor) {
        editor.removePageChangedListener(this);
    }

    public void partBroughtToTop(IWorkbenchPart part) {
    }

    public void partClosed(IWorkbenchPart part) {
        if (part == this.activeEditor) {
            setActiveEditor(null);
        }
    }

    public void partDeactivated(IWorkbenchPart part) {
    }

    public void partOpened(IWorkbenchPart part) {
    }

    public void pageChanged(PageChangedEvent event) {
        setCurrentSheet(findCurrentSheet());
        viewer.refresh(true);
        updateSelection();
    }

    public void handleCoreEvent(final CoreEvent event) {
        if (viewer == null)
            return;

        Control c = viewer.getControl();
        if (c == null || c.isDisposed())
            return;

        c.getDisplay().syncExec(new Runnable() {
            public void run() {
                if (Core.ThemeId.equals(event.getType())) {
                    updateSelection();
                } else if (Core.Name.equals(event.getType())) {
                    viewer.update(new Object[] { event.getSource() });
                } else {
                    viewer.setInput(getViewerInput());
                    viewer.setSelection(
                            new StructuredSelection(event.getSource()), true);
                }
            }
        });
    }

}
