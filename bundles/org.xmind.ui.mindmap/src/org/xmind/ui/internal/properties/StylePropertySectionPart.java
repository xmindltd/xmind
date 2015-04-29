package org.xmind.ui.internal.properties;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.xmind.core.style.IStyle;
import org.xmind.core.style.IStyleSheet;
import org.xmind.core.style.IStyled;
import org.xmind.gef.EditDomain;
import org.xmind.gef.GEF;
import org.xmind.gef.part.GraphicalEditPart;
import org.xmind.gef.part.IPart;
import org.xmind.gef.part.IPartFactory;
import org.xmind.gef.util.Properties;
import org.xmind.ui.gallery.FrameFigure;
import org.xmind.ui.gallery.FramePart;
import org.xmind.ui.gallery.GalleryLayout;
import org.xmind.ui.gallery.GallerySelectTool;
import org.xmind.ui.gallery.GalleryViewer;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.editor.MindMapEditorPage;
import org.xmind.ui.internal.handlers.IMindMapCommandConstants;
import org.xmind.ui.internal.views.StyleFigure;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.properties.StyledPropertySectionPart;
import org.xmind.ui.viewers.MButton;

public class StylePropertySectionPart extends StyledPropertySectionPart {

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

        public StylePart(Object model) {
            setModel(model);
        }

        public IStyle getStyle() {
            return (IStyle) super.getModel();
        }

        protected IFigure createFigure() {
            return new StyleFigure();
        }

        protected void updateView() {
            super.updateView();
            ((StyleFigure) getFigure()).setStyle(getStyle());

            Properties properties = ((GalleryViewer) getSite().getViewer())
                    .getProperties();
            Dimension size = (Dimension) properties
                    .get(GalleryViewer.FrameContentSize);
            if (size != null) {
                getFigure().setPreferredSize(size);
            }
        }
    }

    private static class StylePartFactory implements IPartFactory {
        private IPartFactory factory;

        public StylePartFactory(IPartFactory factory) {
            this.factory = factory;
        }

        public IPart createPart(IPart context, Object model) {
            if (context instanceof FramePart && model instanceof IStyle) {
                FrameFigure figure = ((FramePart) context).getFigure();
                if (figure != null)
                    figure.setToolTip(new Label(((IStyle) model).getName()));
                return new StylePart(model);
            }
            return factory.createPart(context, model);
        }
    }

    private class SelectStyleDialog extends PopupDialog implements
            IOpenListener {

        private Control handle;

        private GalleryViewer viewer;

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
            properties.set(GalleryViewer.Layout, new GalleryLayout(
                    GalleryLayout.ALIGN_TOPLEFT, GalleryLayout.ALIGN_FILL, 1,
                    1, new Insets(5)));
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

            viewer.setPartFactory(new StylePartFactory(viewer.getPartFactory()));
            viewer.setLabelProvider(new StyleLabelProvider());

            viewer.createControl(composite);
            GridData galleryData = new GridData(GridData.FILL, GridData.FILL,
                    true, true);
            viewer.getControl().setLayoutData(galleryData);

            final Display display = parent.getDisplay();
            viewer.getControl().setBackground(
                    display.getSystemColor(SWT.COLOR_LIST_BACKGROUND));

            viewer.setInput(getInput());

            Composite bottom = new Composite(composite, SWT.NONE);
            GridLayout bottomLayout = new GridLayout();
            bottomLayout.marginWidth = 0;
            bottomLayout.marginHeight = 0;
            bottom.setLayout(bottomLayout);
            bottom.setLayoutData(new GridData(GridData.FILL, GridData.FILL,
                    true, false));

            Button showStyleEditorDialog = new Button(bottom, SWT.PUSH);
            showStyleEditorDialog
                    .setText(MindMapMessages.StylePropertySectionPart_ShowStyleEditorDialogButton_text);
            showStyleEditorDialog.setLayoutData(new GridData(GridData.CENTER,
                    GridData.CENTER, true, false));
            showStyleEditorDialog.addListener(SWT.Selection, new Listener() {
                public void handleEvent(Event event) {
                    close();
                    display.asyncExec(new Runnable() {
                        public void run() {
                            showStyleEditorDialog(MindMapUI
                                    .getResourceManager().getSystemStyleSheet()
                                    .createStyle(getInputType()));
                        }
                    });
                }
            });

            return composite;
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
            return new Point(250, 300);
        }

        protected Point getInitialLocation(Point initialSize) {
            if (handle != null && !handle.isDisposed()) {
                Point loc = handle.toDisplay(handle.getLocation());
                return new Point(loc.x, loc.y + handle.getBounds().height);
            }
            return super.getInitialLocation(initialSize);
        }

        protected IDialogSettings getDialogSettings() {
            return MindMapUIPlugin.getDefault().getDialogSettings(
                    MindMapUI.POPUP_DIALOG_SETTINGS_ID);
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

    private PopupDialog selectStyleDialog;

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
                NLS.bind(MindMapMessages.StylePropertySectionPart_tooltip,
                        getInputType()));
        selectStyleWidget.addOpenListener(new IOpenListener() {
            public void open(OpenEvent event) {
                openSelectStyleDialog();
            }
        });
    }

    public void openSelectStyleDialog() {
        if (selectStyleWidget != null && selectStyleWidget.getControl() != null
                && !selectStyleWidget.getControl().isDisposed()) {
            if (selectStyleDialog == null) {
                Control handle = selectStyleWidget.getControl();
                selectStyleDialog = new SelectStyleDialog(handle.getShell(),
                        handle);
            }
            selectStyleDialog.open();
            Shell shell = selectStyleDialog.getShell();
            if (shell != null && !shell.isDisposed()) {
                selectStyleWidget.setForceFocus(true);
                shell.addListener(SWT.Dispose, new Listener() {
                    public void handleEvent(Event event) {
                        if (selectStyleWidget != null
                                && !selectStyleWidget.getControl().isDisposed()) {
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
        selectStyleDialog = null;
        selectStyleWidget = null;
    }

    private String getInputType() {
        ISelection styleType = ((MindMapEditorPage) getActivePage())
                .getSelectionProvider().getSelection();
        Object obj = null;
        if (styleType instanceof StructuredSelection) {
            obj = ((StructuredSelection) styleType).getFirstElement();
        }
        if (obj instanceof IStyled) {
            return ((IStyled) obj).getStyleType();
        }
        return null;
    }

    private List<IStyle> getInput() {
        List<IStyle> list = new ArrayList<IStyle>();
        Set<IStyle> autoStyles = MindMapUI.getResourceManager()
                .getSystemStyleSheet().getStyles(IStyleSheet.AUTOMATIC_STYLES);
        Set<IStyle> userStyles = MindMapUI.getResourceManager()
                .getUserStyleSheet().getAllStyles();
        for (IStyle style : autoStyles) {
            String type = style.getType();
            if (getInputType().equalsIgnoreCase(type)) {
                list.add(style);
            }
        }
        for (IStyle style : userStyles) {
            String type = style.getType();
            if (getInputType().equalsIgnoreCase(type))
                list.add(0, style);
        }
        return list;
    }

    private void showStyleEditorDialog(IStyle style) {
        invokeCommand(style, IMindMapCommandConstants.STYLE_EDIT);
    }

    private void applyStyle(IStyle style) {
        invokeCommand(style, IMindMapCommandConstants.STYLE_APPLY);
    }

    private void invokeCommand(IStyle style, String type) {
        if (style == null)
            return;
        final ICommandService cs = (ICommandService) PlatformUI.getWorkbench()
                .getService(ICommandService.class);
        final IHandlerService hs = (IHandlerService) PlatformUI.getWorkbench()
                .getService(IHandlerService.class);
        if (cs == null || hs == null)
            return;

        final Command command = cs.getCommand(type);
        if (command == null || !command.isDefined() || !command.isEnabled()
                || !command.isHandled())
            return;

        final String resourceURI = MindMapUI.getResourceManager()
                .toResourceURI(style);
        if (resourceURI == null)
            return;
        final Event event = new Event();
        event.data = this;

        SafeRunner.run(new SafeRunnable() {
            public void run() throws Exception {
                IParameter resourceURIParam = command
                        .getParameter(IMindMapCommandConstants.RESOURCE_URI);
                if (resourceURIParam == null)
                    return;
                hs.executeCommand(new ParameterizedCommand(command,
                        new Parameterization[] { new Parameterization(
                                resourceURIParam, resourceURI) }), event);
            }
        });
    }

}
