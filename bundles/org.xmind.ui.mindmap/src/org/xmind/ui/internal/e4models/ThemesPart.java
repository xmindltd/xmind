package org.xmind.ui.internal.e4models;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.xmind.core.Core;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.CoreEventRegister;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventRegister;
import org.xmind.core.event.ICoreEventSupport;
import org.xmind.core.internal.UserDataConstants;
import org.xmind.core.style.IStyle;
import org.xmind.core.style.IStyleSheet;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.gallery.GalleryViewer;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.editor.MindMapEditor;
import org.xmind.ui.internal.utils.ResourceUtils;
import org.xmind.ui.internal.views.CategorizedThemeViewer;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.resources.ColorUtils;

public class ThemesPart extends ViewModelPart
        implements ICoreEventListener, IPageChangedListener {

    private CategorizedThemeViewer viewer;
    private ICoreEventRegister register = null;

    private IGraphicalEditor sourceEditor;
    private ICoreEventRegister topicRegister = null;
    private ITopic rootTopic;

    @Override
    protected Control doCreateContent(Composite parent) {
        MindMapUIPlugin.getDefault().getUsageDataCollector()
                .trackView(UserDataConstants.VIEW_THEME);

        Composite container = new Composite(parent, SWT.NONE);
        container.setBackground(
                (Color) resources.get(ColorUtils.toDescriptor("#ffffff"))); //$NON-NLS-1$

        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 0;
        container.setLayout(layout);
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        this.registerContextMenu(container,
                IModelConstants.POPUPMENU_ID_RESOURCEMANAGER_THEME);

        viewer = new CategorizedThemeViewer(container) {
            @Override
            protected void postInit() {
                getProperties().set(GalleryViewer.FrameContentSize,
                        new Dimension(240, 120));
                super.postInit();
            };
        };
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.horizontalIndent = Util.isMac() ? 8 : -2;
        viewer.getControl().setLayoutData(gridData);

        this.setSelectionProvider(viewer);

        ICoreEventSupport ces = (ICoreEventSupport) MindMapUI
                .getResourceManager().getUserThemeSheet()
                .getAdapter(ICoreEventSupport.class);
        register = new CoreEventRegister(this);
        register.setNextSupport(ces);
        register.register(Core.StyleAdd);
        register.register(Core.StyleRemove);
        register.register(Core.Name);

        return container;
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
        if (register != null) {
            register.unregisterAll();
            register = null;
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
        viewer = null;
    }

    @Override
    public void init() {
        super.init();
        registerRunnables();
        this.registerViewMenu(IModelConstants.VIEWMENU_ID_THEME);
    }

    private void registerRunnables() {
        IEclipseContext ec = getAdapter(MPart.class).getContext();
        ec.set(IModelConstants.KEY_MODEL_PART_REFRESH_PAGE,
                new IContextRunnable() {
                    @Override
                    public void run() {
                        if (viewer != null && viewer.getControl() != null
                                && !viewer.getControl().isDisposed()) {
                            viewer.refresh();
                        }
                    }
                });
        ec.set(IModelConstants.KEY_MODEL_PART_RENAME, new IContextRunnable() {
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
                Set<IStyle> systemThemeSets = MindMapUI.getResourceManager()
                        .getSystemThemeSheet()
                        .getStyles(IStyleSheet.MASTER_STYLES);
                boolean canExecute = themes.size() == 1;
                for (IStyle theme : themes) {
                    canExecute = canExecute && !systemThemeSets.contains(theme);
                }
                return canExecute;
            }
        });

        ec.set(IModelConstants.KEY_MODEL_PART_DUPLICATE,
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

        ec.set(IModelConstants.KEY_MODEL_PART_DELETE, new IContextRunnable() {

            @Override
            public void run() {
                if (viewer != null && viewer.getControl() != null
                        && !viewer.getControl().isDisposed()) {
                    List<IStyle> themes = getSelectedThemes();
                    if (!themes.isEmpty()
                            && ResourceUtils.confirmToDeleteStyles(
                                    viewer.getControl().getShell(), themes)) {
                        ResourceUtils.deleteStyles(themes);
                    }
                }
            }

            @Override
            public boolean canExecute(IEclipseContext context,
                    String contextKey) {
                List<IStyle> themes = getSelectedThemes();
                Set<IStyle> systemThemeSets = MindMapUI.getResourceManager()
                        .getSystemThemeSheet()
                        .getStyles(IStyleSheet.MASTER_STYLES);
                boolean canExecute = !themes.isEmpty();
                for (IStyle theme : themes) {
                    canExecute = canExecute && !systemThemeSets.contains(theme);
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

    private ITopic findRootTopic(IGraphicalEditorPage page) {
        ISheet sheet = page.getAdapter(ISheet.class);
        if (sheet != null)
            return sheet.getRootTopic();

        return null;
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

    @Override
    protected void handlePartActivated(MPart part) {
        super.handlePartActivated(part);
        final IEditorPart editorPart = part.getContext().get(IEditorPart.class);
        if (editorPart instanceof MindMapEditor) {
            Display.getCurrent().asyncExec(new Runnable() {
                public void run() {
                    setEditor((MindMapEditor) editorPart);
                }
            });
        }

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

}
