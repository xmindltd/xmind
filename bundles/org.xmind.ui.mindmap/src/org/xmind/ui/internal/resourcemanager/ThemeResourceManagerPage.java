package org.xmind.ui.internal.resourcemanager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.osgi.service.event.Event;
import org.xmind.core.Core;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.CoreEventRegister;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventRegister;
import org.xmind.core.event.ICoreEventSupport;
import org.xmind.core.style.IStyle;
import org.xmind.core.style.IStyleSheet;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.e4models.IContextRunnable;
import org.xmind.ui.internal.e4models.IModelConstants;
import org.xmind.ui.internal.editor.MindMapEditor;
import org.xmind.ui.internal.handlers.IMindMapCommandConstants;
import org.xmind.ui.internal.utils.ResourceUtils;
import org.xmind.ui.mindmap.MindMapUI;

public class ThemeResourceManagerPage extends ResourceManagerDialogPage
        implements ICoreEventListener, IPageChangedListener {

    private static final String EDIT_THEME_COMMAND_ID = "org.xmind.ui.command.theme.edit"; //$NON-NLS-1$
    private static final String NEW_THEME_COMMAND_ID = "org.xmind.ui.command.newTheme"; //$NON-NLS-1$
    private final static int NEW_THEME_BUTTON_ID = IDialogConstants.CLIENT_ID
            + 1;
    private final static int EDIT_THEME_BUTTON_ID = IDialogConstants.CLIENT_ID
            + 2;

    private CoreEventRegister coreEventRegister;
    private Button newThemeButton;
    private Button editThemeButton;
    private ThemeResourceManagerViewer viewer;

    private IGraphicalEditor sourceEditor;
    private ICoreEventRegister topicRegister = null;
    private ITopic rootTopic;

    @Override
    protected ResourceManagerViewer createViewer() {
        viewer = new ThemeResourceManagerViewer();
        viewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                ISelection selection = event.getSelection();
                if (selection instanceof IStructuredSelection) {
                    IStructuredSelection sel = (IStructuredSelection) selection;
                    Object obj = sel.getFirstElement();
                    if (obj != null && obj instanceof IStyle
                            && IStyle.THEME.equals(((IStyle) obj).getType())) {
                        editThemeButton.setEnabled(true);
                        return;
                    }
                }
                editThemeButton.setEnabled(false);
            }
        });
        registerCoreEvent();
        return viewer;
    }

    @Override
    protected void registerRunnable(IEclipseContext eclipseContext) {
        super.registerRunnable(eclipseContext);

        eclipseContext.set(
                getId() + "/" + IModelConstants.KEY_MODEL_PART_RENAME, //$NON-NLS-1$
                new IContextRunnable() {
                    @Override
                    public void run() {
                        if (viewer != null && viewer.getControl() != null
                                && !viewer.getControl().isDisposed()) {
                            List<IStyle> themes = getSelectedThemes();
                            if (themes.size() == 1)
                                viewer.startEditing(themes.get(0));
                        }
                    }

                    @Override
                    public boolean canExecute(IEclipseContext context,
                            String contextKey) {
                        List<IStyle> themes = getSelectedThemes();
                        Set<IStyle> systemThemeSets = MindMapUI
                                .getResourceManager().getSystemThemeSheet()
                                .getStyles(IStyleSheet.MASTER_STYLES);
                        boolean canExecute = themes.size() == 1;
                        for (IStyle theme : themes) {
                            canExecute = canExecute
                                    && !systemThemeSets.contains(theme);
                        }
                        return canExecute;
                    }
                });

        eclipseContext.set(
                getId() + "/" + IModelConstants.KEY_MODEL_PART_DUPLICATE, //$NON-NLS-1$
                new IContextRunnable() {
                    @Override
                    public void run() {
                        if (viewer != null && viewer.getControl() != null
                                && !viewer.getControl().isDisposed()) {
                            List<IStyle> themes = getSelectedThemes();
                            if (!themes.isEmpty()) {
                                ResourceUtils.duplicateThemes(themes);
                            }
                        }
                    }

                    @Override
                    public boolean canExecute(IEclipseContext context,
                            String contextKey) {
                        List<IStyle> themes = getSelectedThemes();
                        boolean canExecute = !themes.isEmpty();
                        return canExecute;
                    }

                });

        eclipseContext.set(
                getId() + "/" + IModelConstants.KEY_MODEL_PART_DELETE, //$NON-NLS-1$
                new IContextRunnable() {

                    @Override
                    public void run() {
                        if (viewer != null && viewer.getControl() != null
                                && !viewer.getControl().isDisposed()) {
                            List<IStyle> themes = getSelectedThemes();
                            if (!themes.isEmpty()
                                    && ResourceUtils.confirmToDeleteStyles(
                                            viewer.getControl().getShell(),
                                            themes)) {
                                ResourceUtils.deleteStyles(themes);
                            }
                        }
                    }

                    @Override
                    public boolean canExecute(IEclipseContext context,
                            String contextKey) {
                        List<IStyle> themes = getSelectedThemes();
                        IStyleSheet userThemeSheet = MindMapUI
                                .getResourceManager().getUserThemeSheet();
                        Set<IStyle> styles = userThemeSheet
                                .getStyles(IStyleSheet.MASTER_STYLES);
                        boolean canExecute = !themes.isEmpty();
                        for (IStyle theme : themes) {
                            canExecute = canExecute && styles.contains(theme);
                        }
                        return canExecute;
                    }
                });

    }

    private List<IStyle> getSelectedThemes() {
        List<IStyle> styles = new ArrayList<IStyle>();
        if (viewer != null && viewer.getControl() != null
                && !viewer.getControl().isDisposed()) {
            ISelection selection = viewer.getStructuredSelection();
            if (selection instanceof IStructuredSelection) {
                for (Object element : ((IStructuredSelection) selection)
                        .toList()) {
                    styles.add((IStyle) element);
                }
            }
        }
        return styles;
    }

    @Override
    protected void createButtonsForButtonBar(final Composite composite) {
        final ThemeResourceManagerViewer themeViewer = (ThemeResourceManagerViewer) viewer;
        newThemeButton = createButton(composite, NEW_THEME_BUTTON_ID,
                MindMapMessages.ThemeResourceManagerPage_New_button, false);
        newThemeButton.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                composite.getShell().getDisplay().syncExec(new Runnable() {
                    public void run() {
                        newTheme();
                    }
                });
            }
        });
        editThemeButton = createButton(composite, EDIT_THEME_BUTTON_ID,
                MindMapMessages.ThemeResourceManagerPage_Edit_button, false);
        editThemeButton.setEnabled(false);
        editThemeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                IStructuredSelection selection = (IStructuredSelection) themeViewer
                        .getSelection();
                Object[] objs = selection.toArray();

                for (Object obj : objs)
                    if (obj instanceof IStyle) {
                        IStyle theme = (IStyle) obj;
                        if (IStyle.THEME.equals(theme.getType()))
                            editTheme(theme);
                    }
            }
        });
    }

    private void newTheme() {
        IWorkbenchWindow window = PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow();
        final IHandlerService handlerService = (IHandlerService) window
                .getService(IHandlerService.class);
        try {
            handlerService.executeCommand(NEW_THEME_COMMAND_ID, null);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (NotDefinedException e) {
            e.printStackTrace();
        } catch (NotEnabledException e) {
            e.printStackTrace();
        } catch (NotHandledException e) {
            e.printStackTrace();
        }

    }

    private void editTheme(final IStyle theme) {
        IWorkbenchWindow window = PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow();
        final IHandlerService handlers = (IHandlerService) window
                .getService(IHandlerService.class);
        final ICommandService commands = (ICommandService) window
                .getService(ICommandService.class);
        if (handlers == null || commands == null)
            return;

        final Command command = commands.getCommand(EDIT_THEME_COMMAND_ID);
        if (command == null || !command.isDefined())
            return;

        SafeRunner.run(new SafeRunnable() {
            public void run() throws Exception {
                IParameter param = command
                        .getParameter(IMindMapCommandConstants.RESOURCE_URI);
                if (param == null)
                    return;

                ParameterizedCommand pc = new ParameterizedCommand(command,
                        new Parameterization[] { new Parameterization(param,
                                MindMapUI.getResourceManager()
                                        .toResourceURI(theme)) });
                handlers.executeCommand(pc, null);
            }
        });
    }

    private void registerCoreEvent() {
        ICoreEventSupport ces = (ICoreEventSupport) MindMapUI
                .getResourceManager().getUserThemeSheet()
                .getAdapter(ICoreEventSupport.class);
        if (ces != null) {
            coreEventRegister = new CoreEventRegister(this);
            coreEventRegister.setNextSupport(ces);
            coreEventRegister.register(Core.StyleAdd);
            coreEventRegister.register(Core.StyleRemove);
            coreEventRegister.register(Core.TitleText);
            coreEventRegister.register(Core.Name);
        }
    }

    @Inject
    @Optional
    public void activePartChanged(
            @UIEventTopic(UIEvents.UILifeCycle.ACTIVATE) Event event) {
        Object element = event.getProperty(UIEvents.EventTags.ELEMENT);
        if (!(element instanceof MPart))
            return;

        MPart part = (MPart) element;
        final IEditorPart editorPart = part.getContext().get(IEditorPart.class);
        if (editorPart instanceof MindMapEditor) {
            Display.getCurrent().asyncExec(new Runnable() {
                public void run() {
                    setEditor((MindMapEditor) editorPart);
                }
            });
        }
    }

    private void setRootTopic(ITopic topic) {
        if (topic == rootTopic)
            return;

        if (topicRegister == null)
            topicRegister = new CoreEventRegister(this);

        if (rootTopic != null)
            topicRegister.unregisterAll();

        if (topic != null) {
            if (viewer != null)
                viewer.refresh(true);
            topicRegister.setNextSourceFrom(topic);
            topicRegister.register(Core.StructureClass);
        }
    }

    private void setEditor(IGraphicalEditor editor) {
        if (editor == this.sourceEditor)
            return;

        if (this.sourceEditor != null) {
            this.sourceEditor.removePageChangedListener(this);
        }

        this.sourceEditor = editor;

        if (this.sourceEditor != null) {
            this.sourceEditor.addPageChangedListener(this);

            IGraphicalEditorPage page = sourceEditor.getActivePageInstance();
            if (page != null)
                setRootTopic(findRootTopic(page));
        }
    }

    private ITopic findRootTopic(IGraphicalEditorPage page) {
        ISheet sheet = page.getAdapter(ISheet.class);
        if (sheet != null)
            return sheet.getRootTopic();

        return null;
    }

    public void pageChanged(PageChangedEvent event) {
        final IGraphicalEditorPage page = (IGraphicalEditorPage) event
                .getSelectedPage();
        Display.getCurrent().asyncExec(new Runnable() {

            public void run() {
                if (page.isDisposed() || page.getControl() == null
                        || page.getControl().isDisposed())
                    return;
                setRootTopic(findRootTopic(page));
            }
        });
    }

    @Override
    public void handleCoreEvent(final CoreEvent event) {
        if (viewer == null)
            return;

        Control c = viewer.getControl();
        if (c == null || c.isDisposed())
            return;

        c.getDisplay().syncExec(new Runnable() {
            public void run() {
                if (Core.ThemeId.equals(event.getType())) {
                } else if (Core.Name.equals(event.getType())) {
                    viewer.update(new Object[] { event.getSource() });
                } else if (Core.StyleAdd.equals(event.getType())) {
                    viewer.refresh();
                    Object target = event.getTarget();
                    viewer.setSelection(
                            target == null ? StructuredSelection.EMPTY
                                    : new StructuredSelection(target),
                            true);
                } else if (Core.StyleRemove.equals(event.getType())) {
                    viewer.setInput(viewer.getInput());
                } else if (Core.StructureClass.endsWith(event.getType())) {
                    viewer.refresh(true);
                }

            }
        });
    }

    @Override
    public void dispose() {
        if (coreEventRegister != null) {
            coreEventRegister.unregisterAll();
            coreEventRegister = null;
        }
        if (topicRegister != null) {
            topicRegister.unregisterAll();
            topicRegister = null;
        }
        if (sourceEditor != null) {
            sourceEditor.removePageChangedListener(this);
            sourceEditor = null;
        }
        super.dispose();
    }

    @Override
    protected String getContextMenuId() {
        return IModelConstants.POPUPMENU_ID_RESOURCEMANAGER_THEME;
    }

    @Override
    public String getModelPageId() {
        return IModelConstants.PAGE_ID_RESOURCE_MANAGER_THEME;
    }

    @Override
    public String getModelPageTitle() {
        return null;
    }

}
