package org.xmind.ui.internal.resourcemanager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.dialogs.IDialogConstants;
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
import org.xmind.core.Core;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.CoreEventRegister;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventSupport;
import org.xmind.core.style.IStyle;
import org.xmind.core.style.IStyleSheet;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.e4models.IContextRunnable;
import org.xmind.ui.internal.e4models.IModelConstants;
import org.xmind.ui.internal.utils.ResourceUtils;
import org.xmind.ui.mindmap.MindMapUI;

public class StyleResourceManagerPage extends ResourceManagerDialogPage
        implements ICoreEventListener {

    private final static int editStyleButtonID = IDialogConstants.CLIENT_ID + 1;
    private CoreEventRegister coreEventRegister;
    private Button editStyleButton;
    private StyleResourceManagerViewer viewer;

    @Override
    protected ResourceManagerViewer createViewer() {
        viewer = new StyleResourceManagerViewer();
        viewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                ISelection selection = event.getSelection();
                if (selection instanceof IStructuredSelection) {
                    IStructuredSelection sel = (IStructuredSelection) selection;
                    Object obj = sel.getFirstElement();
                    if (obj != null && obj instanceof IStyle) {
                        editStyleButton.setEnabled(true);
                        return;
                    }
                }
                editStyleButton.setEnabled(false);
            }
        });
        registerCoreEvent();
        return viewer;
    }

    @Override
    protected void createButtonsForButtonBar(Composite buttonBar) {
        editStyleButton = createButton(buttonBar, editStyleButtonID,
                MindMapMessages.StyleResourceManager_Editor_button, false);
        editStyleButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                StyleResourceManagerViewer styleViewer = (StyleResourceManagerViewer) viewer;
                styleViewer.editStyle();
            }
        });
        editStyleButton.setEnabled(false);
    }

    private void registerCoreEvent() {
        ICoreEventSupport ces = (ICoreEventSupport) MindMapUI
                .getResourceManager().getUserStyleSheet()
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

    @Override
    public void handleCoreEvent(final CoreEvent event) {
        if (viewer == null)
            return;

        Control c = viewer.getControl();
        if (c == null || c.isDisposed())
            return;

        c.getDisplay().syncExec(new Runnable() {
            public void run() {
                viewer.refresh();
                viewer.setSelection(new StructuredSelection(event.getSource()),
                        true);
            }
        });
    }

    @Override
    public void dispose() {
        if (coreEventRegister != null)
            coreEventRegister.unregisterAll();
        super.dispose();
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
                            List<IStyle> styles = getSelectedStyles();
                            if (styles.size() == 1)
                                viewer.startEditing(styles.get(0));
                        }
                    }

                    @Override
                    public boolean canExecute(IEclipseContext context,
                            String contextKey) {
                        return isOneUserStyleSelected();
                    }
                });

        eclipseContext.set(
                getId() + "/" + IModelConstants.KEY_MODEL_PART_DUPLICATE, //$NON-NLS-1$
                new IContextRunnable() {
                    @Override
                    public void run() {
                        if (viewer != null && viewer.getControl() != null
                                && !viewer.getControl().isDisposed()) {
                            List<IStyle> styles = getSelectedStyles();
                            if (!styles.isEmpty()) {
                                List<IStyle> newStyles = ResourceUtils
                                        .duplicateStyles(styles);
                                viewer.setSelection(
                                        new StructuredSelection(newStyles));
                            }
                        }
                    }

                    @Override
                    public boolean canExecute(IEclipseContext context,
                            String contextKey) {
                        List<IStyle> styles = getSelectedStyles();
                        boolean canExecute = !styles.isEmpty();
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
                            List<IStyle> styles = getSelectedStyles();
                            if (!styles.isEmpty()
                                    && ResourceUtils.confirmToDeleteStyles(
                                            viewer.getControl().getShell(),
                                            styles)) {
                                ResourceUtils.deleteStyles(styles);
                            }
                        }
                    }

                    @Override
                    public boolean canExecute(IEclipseContext context,
                            String contextKey) {
                        return isAllUserStyles();
                    }
                });

        eclipseContext.set(getId() + "/" + IModelConstants.KEY_MODEL_PART_EDIT, //$NON-NLS-1$
                new IContextRunnable() {

                    @Override
                    public void run() {
                        StyleResourceManagerViewer styleViewer = (StyleResourceManagerViewer) viewer;
                        styleViewer.editStyle();
                    }

                    @Override
                    public boolean canExecute(IEclipseContext context,
                            String contextKey) {
                        List<IStyle> selected = getSelectedStyles();
                        return selected != null && !selected.isEmpty();
                    }
                });

    }

    private List<IStyle> getSelectedStyles() {
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
    protected String getContextMenuId() {
        return IModelConstants.POPUPMENU_ID_RESOURCEMANAGER_STYLE;
    }

    @Override
    public String getModelPageId() {
        return IModelConstants.PAGE_ID_RESOURCE_MANAGER_STYLE;
    }

    @Override
    public String getModelPageTitle() {
        return null;
    }

    private boolean isAllUserStyles() {
        List<IStyle> styles = getSelectedStyles();
        Set<IStyle> userStyles = MindMapUI.getResourceManager()
                .getUserStyleSheet().getAllStyles();
        boolean canExecute = !styles.isEmpty();
        for (IStyle style : styles) {
            canExecute = canExecute && userStyles.contains(style);
        }
        return canExecute;
    }

    private boolean isOneUserStyleSelected() {
        List<IStyle> styles = getSelectedStyles();
        IStyleSheet userStyleSheet = MindMapUI.getResourceManager()
                .getUserStyleSheet();
        Set<IStyle> userStyles = userStyleSheet.getAllStyles();
        boolean canExecute = styles.size() == 1;
        for (IStyle style : styles) {
            canExecute = canExecute && userStyles.contains(style);
        }
        return canExecute;
    }

}
