package org.xmind.ui.internal.views;

import java.util.List;
import java.util.Set;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.xmind.core.IBoundary;
import org.xmind.core.IRelationship;
import org.xmind.core.ISheet;
import org.xmind.core.ISummary;
import org.xmind.core.ITopic;
import org.xmind.core.internal.UserDataConstants;
import org.xmind.core.style.IStyle;
import org.xmind.core.style.IStyleSheet;
import org.xmind.gef.EditDomain;
import org.xmind.gef.GEF;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.IViewer;
import org.xmind.gef.Request;
import org.xmind.gef.event.MouseEvent;
import org.xmind.gef.part.IPart;
import org.xmind.gef.tool.ITool;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.gef.util.Properties;
import org.xmind.ui.gallery.CategorizedGalleryViewer;
import org.xmind.ui.gallery.FramePart;
import org.xmind.ui.gallery.GalleryEditTool;
import org.xmind.ui.gallery.GalleryLayout;
import org.xmind.ui.gallery.GallerySelectTool;
import org.xmind.ui.gallery.GalleryViewer;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.views.ThemeUICore.ThemeUIGroup;
import org.xmind.ui.mindmap.ISheetPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.prefs.PrefConstants;
import org.xmind.ui.texteditor.FloatingTextEditor;
import org.xmind.ui.util.MindMapUtils;

/**
 * @author Ren Siu
 * @since 3.6.50
 */
public class CategorizedThemeViewer extends CategorizedGalleryViewer {

    private static final int FRAME_WIDTH = 200;
    private static final int FRAME_HEIGHT = 100;

    private class ThemeSelectTool extends GallerySelectTool {

        protected boolean handleMouseDown(MouseEvent me) {
            FramePart targetFrame = findFrame(me.target);
            if (targetFrame != null && targetFrame.getFigure().isSelected()) {
                return super.handleMouseDown(me);
            } else {
                return handleSelectionOnMouseDown(me);
            }
        }

        private FramePart findFrame(IPart part) {
            while (part != null) {
                if (part instanceof FramePart)
                    return (FramePart) part;
                part = part.getParent();
            }
            return null;
        }
    }

    private class CategorizedThemeContentProvider
            implements ITreeContentProvider {

        public void dispose() {
        }

        public void inputChanged(Viewer viewer, Object oldInput,
                Object newInput) {
        }

        public Object[] getElements(Object inputElement) {
            return ((ThemeUICore) inputElement).getThemeGroups().toArray();
        }

        public Object[] getChildren(Object parentElement) {
            if (parentElement instanceof ThemeUIGroup) {
                return ((ThemeUIGroup) parentElement).getItems();
            }
            return null;
        }

        public Object getParent(Object element) {
            if (element instanceof ThemeUIGroup) {
                return ThemeUICore.getInstance();
            } else if (element instanceof IStyle) {
                List<ThemeUIGroup> themeGroups = ThemeUICore.getInstance()
                        .getThemeGroups();
                for (ThemeUIGroup themeGroup : themeGroups) {
                    IStyle[] styles = themeGroup.getItems();
                    for (IStyle style : styles) {
                        if (element.equals(style)) {
                            return themeGroup;
                        }
                    }
                }
            }
            return null;
        }

        public boolean hasChildren(Object element) {
            return element instanceof ThemeUICore
                    || element instanceof ThemeUIGroup;
        }

    }

    private class CategorizedThemeLabelProvider extends ThemeLabelProvider {
        public String getText(Object element) {
            if (element instanceof ThemeUIGroup) {
                return ((ThemeUIGroup) element).getName();
            } else if (element instanceof IStyle
                    && IStyle.THEME.equals(((IStyle) element).getType())) {
                return ((IStyle) element).getName();
            }
            return super.getText(element);
        }

    }

    private class ThemeGalleryViewer extends GalleryViewer {
        protected boolean isTitleEditable(IPart p) {
            IStyleSheet styleSheet = MindMapUI.getResourceManager()
                    .getUserThemeSheet();
            return styleSheet == null ? false
                    : styleSheet.getAllStyles().contains(p.getModel());
        }
    }

    private class ThemeNameEditTool extends GalleryEditTool {

        protected IDocument getTextContents(IPart source) {
            return new org.eclipse.jface.text.Document(
                    ((IStyle) source.getModel()).getName());
        }

        protected void handleTextModified(IPart source, IDocument document) {
            ((IStyle) source.getModel()).setName(document.get());
            MindMapUI.getResourceManager().saveUserThemeSheet();
        }

        protected void hookEditor(FloatingTextEditor editor) {
            super.hookEditor(editor);
            getHelper().setPrefWidth(FRAME_WIDTH);
        }

    }

    private class ChangeThemeListener implements IOpenListener {

        public void open(OpenEvent event) {
            IPreferenceStore pref = MindMapUIPlugin.getDefault()
                    .getPreferenceStore();
            String themeApply = pref.getString(PrefConstants.THEME_APPLY);
            if (isThemeModified() && (PrefConstants.ASK_USER.equals(themeApply)
                    || IPreferenceStore.STRING_DEFAULT_DEFAULT
                            .equals(themeApply))) {
                int code = openCoverDialog();
                if (IDialogConstants.CANCEL_ID == code)
                    return;

                if (IDialogConstants.OK_ID == code)
                    themeApply = PrefConstants.THEME_OVERRIDE;
                else if (IDialogConstants.NO_ID == code)
                    themeApply = PrefConstants.THEME_KEEP;
            }

            Object o = ((IStructuredSelection) event.getSelection())
                    .getFirstElement();
            if (o != null && o instanceof IStyle) {
                changeTheme((IStyle) o, themeApply);
            }
        }

        private int openCoverDialog() {
            Shell shell = getControl().getShell();
            if (shell != null)
                return new ThemeOverrideDialog(shell).open();
            return IDialogConstants.NO_ID;
        }

        private boolean isThemeModified() {
            ISheet sheet = getCurrentSheet();
            if (sheet == null)
                return false;

            if (sheet.getStyleId() != null)
                return true;

            List<ITopic> topics = MindMapUtils.getAllTopics(sheet, true, true);
            for (ITopic topic : topics) {
                if (topic.getStyleId() != null)
                    return true;
                Set<IBoundary> boundaries = topic.getBoundaries();
                for (IBoundary boundary : boundaries) {
                    if (boundary.getStyleId() != null)
                        return true;
                }
                Set<ISummary> summaries = topic.getSummaries();
                for (ISummary summary : summaries) {
                    if (summary.getStyleId() != null)
                        return true;
                }
            }

            Set<IRelationship> relationships = sheet.getRelationships();
            for (IRelationship relationship : relationships) {
                if (relationship.getStyleId() != null)
                    return true;
            }

            return false;
        }

        private ISheet getCurrentSheet() {
            IGraphicalEditorPage page = getCurrentPage();
            if (page == null)
                return null;

            ISheet sheet = page.getAdapter(ISheet.class);
            return sheet;
        }

        private IGraphicalEditorPage getCurrentPage() {
            IWorkbenchWindow window = PlatformUI.getWorkbench()
                    .getActiveWorkbenchWindow();
            if (window == null)
                return null;

            IEditorPart activeEditor = window.getActivePage().getActiveEditor();
            if (!(activeEditor instanceof IGraphicalEditor))
                return null;

            return ((IGraphicalEditor) activeEditor).getActivePageInstance();
        }

        private void changeTheme(IStyle theme, String apply) {
            MindMapUIPlugin.getDefault().getUsageDataCollector().trackEvent(
                    UserDataConstants.CATEGORY_THEME,
                    UserDataConstants.CHANGE_THEME);

            if (theme != null)
                MindMapUIPlugin.getDefault().getUsageDataCollector().trackEvent(
                        UserDataConstants.CATEGORY_THEME,
                        String.format(UserDataConstants.USE_THEME_S,
                                theme.getName().replaceAll(" ", "_"))); //$NON-NLS-1$ //$NON-NLS-2$

            IGraphicalEditorPage page = getCurrentPage();
            if (page == null)
                return;

            IGraphicalViewer viewer = page.getViewer();
            if (viewer == null)
                return;

            ISheetPart sheetPart = viewer.getAdapter(ISheetPart.class);
            if (sheetPart == null)
                return;

            EditDomain domain = page.getEditDomain();
            if (domain == null)
                return;

            domain.handleRequest(new Request(MindMapUI.REQ_MODIFY_THEME)
                    .setViewer(viewer).setPrimaryTarget(sheetPart)
                    .setParameter(MindMapUI.PARAM_RESOURCE, theme)
                    .setParameter(MindMapUI.PARAM_OVERRIDE, apply));

            Control control = viewer.getControl();
            if (control != null && !control.isDisposed()) {
                control.forceFocus();
            }
        }
    }

    public CategorizedThemeViewer(Composite container) {
        super();
        setContentProvider(new CategorizedThemeContentProvider());
        setLabelProvider(new CategorizedThemeLabelProvider());

        EditDomain domain = new EditDomain();
        domain.installTool(GEF.TOOL_SELECT, new GallerySelectTool());
        setEditDomain(domain);

        initProperties();
        postInit();

        createControl(container, SWT.WRAP);
        setInput(ThemeUICore.getInstance());
    }

    protected void postInit() {
        addOpenListener(new ChangeThemeListener());
    }

    private void initProperties() {
        Properties properties = getProperties();
        properties.set(GalleryViewer.Horizontal, Boolean.TRUE);
        properties.set(GalleryViewer.Wrap, Boolean.TRUE);
        properties.set(GalleryViewer.FrameContentSize,
                new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
        properties.set(GalleryViewer.TitlePlacement,
                GalleryViewer.TITLE_BOTTOM);
        properties.set(GalleryViewer.SingleClickToOpen, Boolean.FALSE);
        properties.set(GalleryViewer.SolidFrames, Boolean.TRUE);
        properties.set(GalleryViewer.FlatFrames, Boolean.TRUE);
        properties.set(GalleryViewer.HideTitle, Boolean.FALSE);
        properties.set(GalleryViewer.ImageConstrained, Boolean.TRUE);
        properties.set(GalleryViewer.Layout, new GalleryLayout().align(
                GalleryLayout.ALIGN_TOPLEFT, GalleryLayout.ALIGN_TOPLEFT));
        properties.set(GalleryViewer.CustomContentPaneDecorator, true);
    }

    @Override
    protected void configureNestedViewer(GalleryViewer viewer,
            Object category) {
        super.configureNestedViewer(viewer, category);
        initGalleryViewer(viewer);
    }

    protected void initGalleryViewer(GalleryViewer galleryViewerer) {
        galleryViewerer.setLabelProvider(new ThemeLabelProvider());
        EditDomain editDomain = new EditDomain();
        editDomain.installTool(GEF.TOOL_SELECT, new ThemeSelectTool());
        editDomain.installTool(GEF.TOOL_EDIT, new ThemeNameEditTool());
        galleryViewerer.setEditDomain(editDomain);

        Properties properties = galleryViewerer.getProperties();
        properties.set(GalleryViewer.TitlePlacement,
                GalleryViewer.TITLE_BOTTOM);
        properties.set(GalleryViewer.HideTitle, false);
        properties.set(GalleryViewer.SingleClickToOpen, Boolean.FALSE);
        properties.set(GalleryViewer.SolidFrames, true);
        properties.set(GalleryViewer.FlatFrames, true);
        properties.set(GalleryViewer.ImageConstrained, true);
        properties.set(GalleryViewer.CustomContentPaneDecorator, true);
    }

    protected GalleryViewer createNestedViewer() {
        return new ThemeGalleryViewer();
    }

    public void update(Object[] elements) {
        Object[] themeGroupList = ((CategorizedThemeContentProvider) getContentProvider())
                .getElements(getInput());
        for (Object tg : themeGroupList) {
            ThemeUIGroup themeGroup = (ThemeUIGroup) tg;
            GalleryViewer galleryViewer = getNestedViewer(themeGroup);
            galleryViewer.update(elements);
        }
    }

    public void startEditing(IStyle theme) {
        List<ThemeUIGroup> themeGroups = ThemeUICore.getInstance()
                .getThemeGroups();
        for (ThemeUIGroup themeGroup : themeGroups) {
            IStyle[] styles = themeGroup.getItems();
            for (IStyle style : styles) {
                if (style.equals(theme)) {
                    GalleryViewer galleryViewer = getNestedViewer(themeGroup);
                    EditDomain domain = galleryViewer.getEditDomain();
                    ITool tool = domain.getDefaultTool();

                    ((GallerySelectTool) tool).getStatus()
                            .setStatus(GEF.ST_ACTIVE, true);
                    domain.handleRequest(GEF.REQ_EDIT, (IViewer) galleryViewer);
                    return;
                }
            }
        }

    }

    public void selectDefault() {
        List<Object> categories = getCategories();
        if (categories == null || categories.isEmpty()
                || !(categories.get(0) instanceof ThemeUIGroup)) {
            return;
        }
        Object defaultCategory = null;
        for (Object category : getCategories()) {
            if ("default".equals(((ThemeUIGroup) category).getId())) { //$NON-NLS-1$
                defaultCategory = category;
                setSelectionToCategory(category, new StructuredSelection(
                        MindMapUI.getResourceManager().getDefaultTheme()),
                        true);
            } else {
                setSelectionToCategory(category, StructuredSelection.EMPTY,
                        false);
            }
        }
        reveal(defaultCategory);
    }
}
