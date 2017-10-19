package org.xmind.cathy.internal.dashboard;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.xmind.cathy.internal.WorkbenchMessages;
import org.xmind.core.style.IStyle;
import org.xmind.gef.EditDomain;
import org.xmind.gef.GEF;
import org.xmind.gef.util.Properties;
import org.xmind.ui.gallery.GallerySelectTool;
import org.xmind.ui.gallery.GalleryViewer;
import org.xmind.ui.internal.views.CategorizedThemeViewer;
import org.xmind.ui.internal.views.ThemeLabelProvider;
import org.xmind.ui.mindmap.IResourceManager;
import org.xmind.ui.mindmap.MindMapUI;

public class ThemeChooserDialog extends Dialog {

    private IStyle selectedTheme = null;

    private String structureClass;

    protected ThemeChooserDialog(Shell parentShell) {
        super(parentShell);
        setShellStyle(getShellStyle() | SWT.SHEET);
    }

    protected ThemeChooserDialog(Shell parentShell, String structureClass) {
        this(parentShell);
        this.structureClass = structureClass;
    }

    @Override
    protected boolean isResizable() {
        return true;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(WorkbenchMessages.DashboardThemeChoose_message);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);

        composite.setLayout(new FillLayout());
        GridData parentData = (GridData) composite.getLayoutData();
        parentData.widthHint = 940;
        parentData.heightHint = 500;
        doCreateViewer(composite);
        return composite;
    }

    private void doCreateViewer(Composite parent) {

        CategorizedThemeViewer viewer = doCreatePartControlViewer(parent);

        viewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                selectedTheme = (IStyle) ((IStructuredSelection) event
                        .getSelection()).getFirstElement();
                setButtonEnabled(IDialogConstants.OK_ID,
                        !event.getSelection().isEmpty());
            }
        });

        viewer.addOpenListener(new IOpenListener() {
            public void open(OpenEvent event) {
                ISelection selection = event.getSelection();
                selectedTheme = selection.isEmpty() ? null
                        : (IStyle) ((IStructuredSelection) selection)
                                .getFirstElement();
                setReturnCode(OK);
                close();
            }
        });
        IResourceManager rm = MindMapUI.getResourceManager();
        IStyle defaultTheme = rm.getDefaultTheme();
        viewer.setSelection(new StructuredSelection(defaultTheme));

    }

    private CategorizedThemeViewer doCreatePartControlViewer(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 0;
        container.setLayout(layout);

        CategorizedThemeViewer viewer = new CategorizedThemeViewer(container) {

            @Override
            protected void postInit() {
                // cancel change theme listener
            }

            @Override
            protected void initGalleryViewer(GalleryViewer galleryViewerer) {
                galleryViewerer.setLabelProvider(
                        new ThemeLabelProvider(structureClass));
                EditDomain editDomain = new EditDomain();
                editDomain.installTool(GEF.TOOL_SELECT,
                        new GallerySelectTool());
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
        };
        viewer.getControl()
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        return viewer;
    }

    @Override
    protected Button createButton(Composite parent, int id, String label,
            boolean defaultButton) {
        if (id == IDialogConstants.OK_ID)
            label = WorkbenchMessages.DashboardThemeCreate_label;
        return super.createButton(parent, id, label, defaultButton);
    }

    @Override
    protected void cancelPressed() {
        super.cancelPressed();
        selectedTheme = null;
    }

    public IStyle getSelectedTheme() {
        return selectedTheme;
    }

    private void setButtonEnabled(int id, boolean enabled) {
        Button button = getButton(id);
        if (button == null || button.isDisposed())
            return;
        button.setEnabled(enabled);
    }

}
