package org.xmind.ui.internal.properties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.xmind.core.internal.command.Logger;
import org.xmind.core.style.IStyle;
import org.xmind.core.style.IStyleSheet;
import org.xmind.core.style.IStyled;
import org.xmind.core.util.Property;
import org.xmind.gef.EditDomain;
import org.xmind.gef.GEF;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.Request;
import org.xmind.gef.graphicalpolicy.IStyleSelector;
import org.xmind.gef.part.GraphicalEditPart;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.gef.part.IPart;
import org.xmind.gef.part.IPartFactory;
import org.xmind.gef.ui.properties.IPropertySectionPart;
import org.xmind.gef.util.Properties;
import org.xmind.ui.gallery.FrameFigure;
import org.xmind.ui.gallery.FramePart;
import org.xmind.ui.gallery.GalleryLayout;
import org.xmind.ui.gallery.GallerySelectTool;
import org.xmind.ui.gallery.GalleryViewer;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.views.StyleFigure;
import org.xmind.ui.mindmap.IMindMap;
import org.xmind.ui.mindmap.IMindMapViewer;
import org.xmind.ui.mindmap.IResourceManager;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.properties.StyledPropertySectionPart;
import org.xmind.ui.style.StyleUtils;
import org.xmind.ui.util.MindMapUtils;
import org.xmind.ui.viewers.MButton;

public class StylesPropertySectionPart extends StyledPropertySectionPart
        implements IStyleEditingDelegate {

    private static Class<? extends IStyleEditingSectionPart> styleEditingSectionClass = null;

    private static class StyleLabelProvider extends LabelProvider {

        public String getText(Object element) {
            if (element instanceof IStyle) {
                IStyle style = (IStyle) element;
                return style.getName();
            }
            return super.getText(element);
        }

    }

    private static class StylePart extends GraphicalEditPart {
        private HashMap<String, String> existedStyle;

        public StylePart(Object model, HashMap<String, String> existedStyle) {
            setModel(model);
            this.existedStyle = existedStyle;
        }

        public IStyle getStyle() {
            return (IStyle) super.getModel();
        }

        protected IFigure createFigure() {
            return new StyleFigure();
        }

        protected void updateView() {
            super.updateView();
            StyleFigure styleFigure = (StyleFigure) getFigure();
            styleFigure.setStyle(getStyle());
            styleFigure.setExistedStyle(existedStyle);

            Properties properties = ((GalleryViewer) getSite().getViewer())
                    .getProperties();
            Dimension size = (Dimension) properties
                    .get(GalleryViewer.FrameContentSize);
            if (size != null) {
                getFigure().setPreferredSize(size);
            }
        }
    }

    private class StylePartFactory implements IPartFactory {
        private IPartFactory factory;
        private HashMap<String, String> existedStyle;

        public StylePartFactory(IPartFactory factory,
                HashMap<String, String> existedStyle) {
            this.factory = factory;
            this.existedStyle = existedStyle;
        }

        public IPart createPart(IPart context, Object model) {
            if (context instanceof FramePart && model instanceof IStyle) {
                IStyle style = (IStyle) model;
                FrameFigure figure = ((FramePart) context).getFigure();
                if (figure != null) {
                    figure.setToolTip(new Label(style.getName()));
                }
                return new StylePart(style, existedStyle);
            }
            return factory.createPart(context, model);
        }

    }

    private class SelectStyleDialog extends PopupDialog
            implements IOpenListener {

        private Control handle;

        private GalleryViewer viewer;

        private IPropertySectionPart styleEditingSection;

        public SelectStyleDialog(Shell parent, Control handle) {
            super(parent, SWT.RESIZE, true, true, true, false, false, null,
                    null);
            this.handle = handle;
        }

        protected Control createDialogArea(Composite parent) {
            Composite composite = (Composite) super.createDialogArea(parent);

            viewer = new GalleryViewer();
            Properties properties = viewer.getProperties();
            properties.set(GalleryViewer.Horizontal, Boolean.TRUE);
            properties.set(GalleryViewer.Wrap, Boolean.TRUE);
            properties.set(GalleryViewer.FlatFrames, Boolean.TRUE);
            properties.set(GalleryViewer.ImageConstrained, Boolean.TRUE);
            properties.set(GalleryViewer.Layout,
                    new GalleryLayout(GalleryLayout.ALIGN_TOPLEFT,
                            GalleryLayout.ALIGN_FILL, 1, 1, new Insets(5)));
            properties.set(GalleryViewer.FrameContentSize,
                    new Dimension(48, 48));
            properties.set(GalleryViewer.TitlePlacement,
                    GalleryViewer.TITLE_TOP);
            properties.set(GalleryViewer.SingleClickToOpen, Boolean.TRUE);
            properties.set(GalleryViewer.HideTitle, Boolean.TRUE);
            properties.set(GalleryViewer.SolidFrames, Boolean.FALSE);

            viewer.addOpenListener(this);

            EditDomain editDomain = new EditDomain();
            editDomain.installTool(GEF.TOOL_SELECT, new GallerySelectTool());
            viewer.setEditDomain(editDomain);

            IGraphicalPart part = getGraphicalPart(getSelectedElements()[0]);
            HashMap<String, String> existedStyle = generateExistedStyle(part);
            viewer.setPartFactory(new StylePartFactory(viewer.getPartFactory(),
                    existedStyle));
            viewer.setLabelProvider(new StyleLabelProvider());

            viewer.createControl(composite);
            GridData galleryData = new GridData(GridData.FILL, GridData.FILL,
                    true, true);
            viewer.getControl().setLayoutData(galleryData);

            final Display display = parent.getDisplay();
            viewer.getControl().setBackground(
                    display.getSystemColor(SWT.COLOR_LIST_BACKGROUND));

            viewer.setInput(getViewerInput());

            if (styleEditingSection == null) {
                styleEditingSection = createStyleEditingSectionPart();
            }
            if (styleEditingSection != null) {
                Composite editItemParent = new Composite(composite, SWT.NONE);
                editItemParent.setLayoutData(new GridData(GridData.FILL,
                        GridData.FILL, true, false));
                styleEditingSection.createControl(editItemParent);
            }

            return composite;
        }

        @Override
        protected void constrainShellSize() {
            super.constrainShellSize();
            if (styleEditingSection != null) {
                styleEditingSection.setSelection(getCurrentSelection());
            }
            if (viewer != null) {
                viewer.setInput(getViewerInput());
            }
        }

        private HashMap<String, String> generateExistedStyle(
                IGraphicalPart selectedPart) {
            HashMap<String, String> stylePropertiesMap = new HashMap<String, String>();

            IResourceManager resourceManager = MindMapUI.getResourceManager();
            IStyleSheet defaultStyleSheet = resourceManager
                    .getDefaultStyleSheet();
            IStyleSheet userStyleSheet = resourceManager.getUserStyleSheet();

            Object model = MindMapUtils.getRealModel(selectedPart);
            if (model instanceof IStyled) {
                IStyled styled = (IStyled) model;

                Set<String> propertyKeys = new HashSet<String>();

                IGraphicalViewer viewer = getActiveViewer();
                if (!(viewer instanceof IMindMapViewer))
                    return stylePropertiesMap;
                IMindMap mindMap = ((IMindMapViewer) viewer).getMindMap();
                String styleFamily = MindMapUtils.getFamily(styled, mindMap);

                String userStyleId = styled.getStyleId();
                IStyle userStyle = userStyleSheet.findStyle(userStyleId);
                collectPropertyKey(userStyle, propertyKeys);

                IStyle theme = mindMap.getSheet().getTheme();
                if (theme != null) {
                    IStyle themeStyle = theme.getDefaultStyle(styleFamily);
                    collectPropertyKey(themeStyle, propertyKeys);
                }

                IStyle defaultStyle = defaultStyleSheet.findStyle(styleFamily);
                collectPropertyKey(defaultStyle, propertyKeys);

                IStyleSelector styleSelector = StyleUtils
                        .getStyleSelector(selectedPart);
                for (String key : propertyKeys) {
                    stylePropertiesMap.put(key,
                            styleSelector.getStyleValue(selectedPart, key));
                }
            }

            return stylePropertiesMap;
        }

        private void collectPropertyKey(IStyle style,
                Set<String> propertyKeys) {
            if (style == null)
                return;

            Iterator<Property> properties = style.properties();
            while (properties.hasNext()) {
                propertyKeys.add(properties.next().key);
            }
        }

        protected void configureShell(Shell shell) {
            super.configureShell(shell);
            shell.addListener(SWT.Deactivate, new Listener() {
                public void handleEvent(Event event) {
                    event.display.asyncExec(new Runnable() {
                        public void run() {
                            close();
                        }
                    });
                }
            });
        }

        @SuppressWarnings("unchecked")
        protected List getBackgroundColorExclusions() {
            List list = super.getBackgroundColorExclusions();
            if (viewer != null) {
                list.add(viewer.getControl());
            }
            return list;
        }

        @Override
        protected Point getDefaultSize() {
            return new Point(260, 300);
        }

        protected Point getInitialLocation(Point initialSize) {
            if (handle != null && !handle.isDisposed()) {
                Point loc = handle.toDisplay(handle.getLocation());
                return new Point(loc.x, loc.y + handle.getBounds().height);
            }
            return super.getInitialLocation(initialSize);
        }

        protected IDialogSettings getDialogSettings() {
            return MindMapUIPlugin.getDefault()
                    .getDialogSettings(MindMapUI.POPUP_DIALOG_SETTINGS_ID);
        }

        public void open(OpenEvent event) {
            Object o = ((IStructuredSelection) event.getSelection())
                    .getFirstElement();
            if (o instanceof IStyle) {
                close();
                final IStyle selectedStyle = (IStyle) o;
                Display.getCurrent().asyncExec(new Runnable() {
                    public void run() {
                        applyStyle(selectedStyle);
                    }
                });
                selectStyleWidget.setText((selectedStyle).getName());
            }
        }
    }

    private MButton selectStyleWidget;

    @Override
    protected void doRefresh() {
        selectStyleWidget
                .setText(MindMapMessages.StylePropertySectionPart_text);
    }

    @Override
    protected void createContent(Composite parent) {
        selectStyleWidget = new MButton(parent, MButton.NORMAL);
        selectStyleWidget.getControl().setLayoutData(
                new GridData(GridData.FILL, GridData.FILL, true, false));
        selectStyleWidget.getControl().setToolTipText(
                MindMapMessages.StylePropertySectionPart_tooltip);
        selectStyleWidget.addOpenListener(new IOpenListener() {
            public void open(OpenEvent event) {
                openSelectStyleDialog();
            }
        });
    }

    private void openSelectStyleDialog() {
        if (selectStyleWidget != null && selectStyleWidget.getControl() != null
                && !selectStyleWidget.getControl().isDisposed()) {
            Control handle = selectStyleWidget.getControl();
            SelectStyleDialog selectStyleDialog = new SelectStyleDialog(
                    handle.getShell(), handle);
            selectStyleDialog.open();
            Shell shell = selectStyleDialog.getShell();
            if (shell != null && !shell.isDisposed()) {
                selectStyleWidget.setForceFocus(true);
                shell.addListener(SWT.Dispose, new Listener() {
                    public void handleEvent(Event event) {
                        if (selectStyleWidget != null && !selectStyleWidget
                                .getControl().isDisposed()) {
                            selectStyleWidget.setForceFocus(false);
                        }
                    }
                });
            }
        }
    }

    @Override
    public void setFocus() {
        if (selectStyleWidget != null
                && !selectStyleWidget.getControl().isDisposed()) {
            selectStyleWidget.getControl().setFocus();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        selectStyleWidget = null;
    }

    private String getCurrentStyleType() {
        ISelection selection = getCurrentSelection();
        String type = null;
        if (selection instanceof IStructuredSelection) {
            for (Object obj : ((IStructuredSelection) selection).toList()) {
                String t = null;
                if (obj instanceof IStyled) {
                    t = ((IStyled) obj).getStyleType();
                }
                if (t == null)
                    return null;
                if (type != null && !t.equals(type))
                    return null;

                if (type == null) {
                    type = t;
                }
            }
        }
        return type;
    }

    private List<IStyle> getViewerInput() {
        String type = getCurrentStyleType();
        if (type == null)
            return Collections.emptyList();

        List<IStyle> list = new ArrayList<IStyle>();
        Set<IStyle> systemStyles = MindMapUI.getResourceManager()
                .getSystemStyleSheet().getStyles(IStyleSheet.AUTOMATIC_STYLES);
        Set<IStyle> userStyles = MindMapUI.getResourceManager()
                .getUserStyleSheet().getAllStyles();
        for (IStyle style : systemStyles) {
            if (type.equals(style.getType())) {
                list.add(style);
            }
        }
        for (IStyle style : userStyles) {
            if (type.equals(style.getType()))
                list.add(0, style);
        }
        return list;
    }

    private void applyStyle(IStyle style) {
        Request request = fillTargets(new Request(MindMapUI.REQ_MODIFY_STYLE));
        request.setParameter(MindMapUI.PARAM_RESOURCE, style);
        sendRequest(request);
    }

    public void styleEditingFinished() {
        openSelectStyleDialog();
    }

    private IStyleEditingSectionPart createStyleEditingSectionPart() {
        if (styleEditingSectionClass == null)
            return null;
        try {
            IStyleEditingSectionPart section = styleEditingSectionClass
                    .newInstance();
            section.init(getContainer(), getContributedEditor());
            section.setDelegate(this);
            return section;
        } catch (Exception e) {
            Logger.log("Failed to create EditStyleContributionItem", e); //$NON-NLS-1$
            return null;
        }
    }

    public static void setStyleEditingSectionClass(
            Class<? extends IStyleEditingSectionPart> cls) {
        styleEditingSectionClass = cls;
    }

}
