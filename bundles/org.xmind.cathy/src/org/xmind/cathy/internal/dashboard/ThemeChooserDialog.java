package org.xmind.cathy.internal.dashboard;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.xmind.cathy.internal.WorkbenchMessages;
import org.xmind.core.style.IStyle;
import org.xmind.core.style.IStyleSheet;
import org.xmind.gef.EditDomain;
import org.xmind.gef.GEF;
import org.xmind.gef.util.Properties;
import org.xmind.ui.gallery.GalleryLayout;
import org.xmind.ui.gallery.GalleryNavigablePolicy;
import org.xmind.ui.gallery.GallerySelectTool;
import org.xmind.ui.gallery.GalleryViewer;
import org.xmind.ui.internal.views.ThemeLabelProvider;
import org.xmind.ui.mindmap.IResourceManager;
import org.xmind.ui.mindmap.MindMapUI;

public class ThemeChooserDialog extends Dialog {

    private static final int FRAME_WIDTH = 200;
    private static final int FRAME_HEIGHT = 100;

    private GalleryViewer viewer;

    private IStyle selectedTheme = null;

    protected ThemeChooserDialog(Shell parentShell) {
        super(parentShell);
        setShellStyle(getShellStyle() | SWT.SHEET);
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

        GridData parentData = (GridData) composite.getLayoutData();
        parentData.widthHint = 940;
        parentData.heightHint = 500;

        Label label = new Label(composite, SWT.NONE);
        label.setFont(JFaceResources.getHeaderFont());
        label.setText(WorkbenchMessages.DashboardThemeChoose_message);

        Control viewerControl = createViewer(composite);
        GridData viewerLayoutData = new GridData(SWT.FILL, SWT.FILL, true,
                true);
        viewerControl.setLayoutData(viewerLayoutData);

        return composite;
    }

    private Control createViewer(Composite parent) {
        viewer = new GalleryViewer();

        EditDomain editDomain = new EditDomain();
        editDomain.installTool(GEF.TOOL_SELECT, new GallerySelectTool());
        editDomain.installEditPolicy(GalleryViewer.POLICY_NAVIGABLE,
                new GalleryNavigablePolicy());
        viewer.setEditDomain(editDomain);

        Properties properties = viewer.getProperties();
        properties.set(GalleryViewer.Horizontal, Boolean.TRUE);
        properties.set(GalleryViewer.Wrap, Boolean.TRUE);
        properties.set(GalleryViewer.TitlePlacement,
                GalleryViewer.TITLE_BOTTOM);
        properties.set(GalleryViewer.SingleClickToOpen, Boolean.FALSE);
        properties.set(GalleryViewer.SolidFrames, true);
        properties.set(GalleryViewer.FlatFrames, true);
        properties.set(GalleryViewer.ImageConstrained, true);

        properties.set(GalleryViewer.FrameContentSize,
                new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
        properties.set(GalleryViewer.Layout,
                new GalleryLayout(GalleryLayout.ALIGN_CENTER,
                        GalleryLayout.ALIGN_TOPLEFT, 10, 10, new Insets(10)));
        properties.set(GalleryViewer.EmptySelectionIgnored, true);

        Control control = viewer.createControl(parent,
                SWT.DOUBLE_BUFFERED | SWT.BORDER);

        viewer.setLabelProvider(new ThemeLabelProvider());

        IResourceManager rm = MindMapUI.getResourceManager();

        Set<IStyle> systemThemes = rm.getSystemThemeSheet()
                .getStyles(IStyleSheet.MASTER_STYLES);
        Set<IStyle> userThemes = rm.getUserThemeSheet()
                .getStyles(IStyleSheet.MASTER_STYLES);

        IStyle defaultTheme = rm.getDefaultTheme();

        List<IStyle> themes = new ArrayList<IStyle>(
                systemThemes.size() + userThemes.size());
        themes.addAll(userThemes);
        themes.addAll(systemThemes);

        themes.remove(defaultTheme);
        themes.add(0, defaultTheme);

        viewer.setInput(themes);

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

        viewer.setSelection(new StructuredSelection(defaultTheme));

        return control;
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
