package org.xmind.ui.internal.resourcemanager;

import java.util.List;
import java.util.Set;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.xmind.core.style.IStyle;
import org.xmind.core.style.IStyleSheet;
import org.xmind.gef.EditDomain;
import org.xmind.gef.GEF;
import org.xmind.gef.IViewer;
import org.xmind.gef.part.IPart;
import org.xmind.gef.tool.ITool;
import org.xmind.gef.util.Properties;
import org.xmind.ui.gallery.GalleryEditTool;
import org.xmind.ui.gallery.GallerySelectTool;
import org.xmind.ui.gallery.GalleryViewer;
import org.xmind.ui.internal.views.ThemeGroupCore;
import org.xmind.ui.internal.views.ThemeGroupCore.CategorizedThemeGroup;
import org.xmind.ui.internal.views.ThemeLabelProvider;
import org.xmind.ui.mindmap.IResourceManager;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.texteditor.FloatingTextEditor;

public class ThemeResourceManagerViewer extends ResourceManagerViewer {

    private static final int FRAME_HEIGHT = 72;
    private static final int FRAME_WIDTH = 132;

    private class ThemeGalleryViewer extends GalleryViewer {
        protected boolean isTitleEditable(IPart p) {
            IStyleSheet styleSheet = MindMapUI.getResourceManager()
                    .getUserThemeSheet();
            return styleSheet == null ? false
                    : styleSheet.getAllStyles().contains(p.getModel());
        }
    }

    private class ThemeCategorizedContentProvider
            implements ITreeContentProvider {

        public void dispose() {
        }

        public void inputChanged(Viewer viewer, Object oldInput,
                Object newInput) {
        }

        public Object[] getElements(Object inputElement) {
            return ((ThemeGroupCore) inputElement).getThemeGroups().toArray();
        }

        public Object[] getChildren(Object parentElement) {
            if (parentElement instanceof CategorizedThemeGroup) {
                return ((CategorizedThemeGroup) parentElement).getItems()
                        .toArray();
            }
            return null;
        }

        public Object getParent(Object element) {
            if (element instanceof CategorizedThemeGroup) {
                return ThemeGroupCore.getInstance();
            } else if (element instanceof IStyle) {
                List<CategorizedThemeGroup> themeGroups = ThemeGroupCore
                        .getInstance().getThemeGroups();
                for (CategorizedThemeGroup themeGroup : themeGroups) {
                    List<IStyle> styles = themeGroup.getItems();
                    if (styles.contains(element)) {
                        return themeGroup;
                    }
                }
            }
            return null;
        }

        public boolean hasChildren(Object element) {
            return element instanceof ThemeGroupCore
                    || element instanceof CategorizedThemeGroup;
        }

    }

    private class ThemeCategorizedLabelProvider extends ThemeLabelProvider
            implements IFontProvider {
        public String getText(Object element) {
            if (element instanceof CategorizedThemeGroup) {
                return ((CategorizedThemeGroup) element).getName();
            } else if (element instanceof IStyle
                    && IStyle.THEME.equals(((IStyle) element).getType())) {
                return ((IStyle) element).getName();
            }
            return super.getText(element);
        }

        @Override
        public Font getFont(Object element) {
            FontData data = getContainer().getFont().getFontData()[0];
            if (Util.isMac()) {
                data.setHeight(12);
            } else {
                data.setHeight(9);
            }
            data.setStyle(SWT.NONE);
            FontDescriptor fontDescriptor = FontDescriptor.createFrom(data);
            return getResourceManager().createFont(fontDescriptor);
        }

    }

    class ThemeNestedGalleryViewer extends GalleryViewer {
        protected boolean isTitleEditable(IPart p) {

            if (p.getModel() instanceof IStyle) {
                IStyle style = (IStyle) p.getModel();
                final IResourceManager rm = MindMapUI.getResourceManager();
                Set<IStyle> systemThemeSets = rm.getUserThemeSheet()
                        .getStyles(IStyleSheet.MASTER_STYLES);
                return systemThemeSets.contains(style);
            }
            return false;
        }
    }

    private class ThemeNestedViewerNameEditTool extends GalleryEditTool {

        protected IDocument getTextContents(IPart source) {
            return new org.eclipse.jface.text.Document(
                    ((IStyle) source.getModel()).getName());
        }

        protected void handleTextModified(IPart source, IDocument document) {
            ((IStyle) source.getModel()).setName(document.get());
            MindMapUI.getResourceManager().saveUserThemeSheet();
            ThemeResourceManagerViewer.this.refresh();
        }

        protected void hookEditor(FloatingTextEditor editor) {
            super.hookEditor(editor);
            getHelper().setPrefWidth(
                    FRAME_WIDTH + DEFAULT_FLOATING_TEXT_EDITOR_WIDTH_EXPAND);
        }

    }

    @Override
    public void createControl(Composite container) {
        super.createControl(container);
        setContentProvider(new ThemeCategorizedContentProvider());
        setLabelProvider(new ThemeCategorizedLabelProvider());
        EditDomain domain = new EditDomain();
        domain.installTool(GEF.TOOL_SELECT,
                new ResourceCategorizedSelectTool());
        setEditDomain(domain);
        initProperties();
        createControl(container, SWT.WRAP);
        getControl()
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        setInput(ThemeGroupCore.getInstance());
    }

    @Override
    protected void initNestedGalleryViewer(GalleryViewer galleryViewerer) {
        super.initNestedGalleryViewer(galleryViewerer);
        galleryViewerer.getEditDomain().installTool(GEF.TOOL_EDIT,
                new ThemeNestedViewerNameEditTool());
    }

    @Override
    protected void initProperties() {
        super.initProperties();
        Properties properties = getProperties();
        properties.set(GalleryViewer.FrameContentSize,
                new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
    }

    protected ITool createNestedViewerEditTool() {
        return new ThemeNestedViewerNameEditTool();
    }

    public void update(Object[] elements) {
        Object[] themeGroupList = ((ThemeCategorizedContentProvider) getContentProvider())
                .getElements(getInput());
        for (Object tg : themeGroupList) {
            CategorizedThemeGroup themeGroup = (CategorizedThemeGroup) tg;
            GalleryViewer galleryViewer = getNestedViewer(themeGroup);
            galleryViewer.update(elements);
        }
    }

    public void startEditing(IStyle theme) {
        List<CategorizedThemeGroup> themeGroups = ThemeGroupCore.getInstance()
                .getThemeGroups();
        for (CategorizedThemeGroup themeGroup : themeGroups) {
            List<IStyle> styles = themeGroup.getItems();
            if (styles.contains(theme)) {
                GalleryViewer galleryViewer = getNestedViewer(themeGroup);
                EditDomain domain = galleryViewer.getEditDomain();
                ITool tool = domain.getDefaultTool();

                ((GallerySelectTool) tool).getStatus().setStatus(GEF.ST_ACTIVE,
                        true);
                domain.handleRequest(GEF.REQ_EDIT, (IViewer) galleryViewer);
                break;
            }
        }
    }

    public void selectDefault() {
        List<Object> categories = getCategories();
        if (categories == null || categories.isEmpty()
                || !(categories.get(0) instanceof CategorizedThemeGroup)) {
            return;
        }
        Object defaultCategory = null;
        for (Object category : getCategories()) {
            if ("default".equals(((CategorizedThemeGroup) category).getId())) { //$NON-NLS-1$
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

    protected GalleryViewer createNestedViewer() {
        return new ThemeGalleryViewer();
    }

}
