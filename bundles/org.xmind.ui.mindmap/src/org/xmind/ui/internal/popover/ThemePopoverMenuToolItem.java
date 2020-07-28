package org.xmind.ui.internal.popover;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.handlers.IHandlerService;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xmind.core.IBoundary;
import org.xmind.core.IRelationship;
import org.xmind.core.ISheet;
import org.xmind.core.ISummary;
import org.xmind.core.ITopic;
import org.xmind.core.style.IStyle;
import org.xmind.core.style.IStyleSheet;
import org.xmind.gef.EditDomain;
import org.xmind.gef.GEF;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.Request;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.gef.util.Properties;
import org.xmind.ui.gallery.GalleryLayout;
import org.xmind.ui.gallery.GalleryNavigablePolicy;
import org.xmind.ui.gallery.GallerySelectTool;
import org.xmind.ui.gallery.GalleryViewer;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.e4models.IModelConstants;
import org.xmind.ui.internal.utils.E4Utils;
import org.xmind.ui.internal.views.Messages;
import org.xmind.ui.internal.views.ThemeLabelProvider;
import org.xmind.ui.mindmap.IResourceManager;
import org.xmind.ui.mindmap.ISheetPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.prefs.PrefConstants;
import org.xmind.ui.resources.ColorUtils;
import org.xmind.ui.util.MindMapUtils;
import org.xmind.ui.util.XMLUtils;
import org.xml.sax.SAXException;

public class ThemePopoverMenuToolItem extends PopoverMenuToolItem {

    private static final String THEME_POPOVER_XML = "styles/themePopover.xml"; //$NON-NLS-1$

    private static final String TAG_THEME = "theme"; //$NON-NLS-1$

    private static final String ATTR_ID = "id"; //$NON-NLS-1$

    private static final String Extract_Theme_Command_ID = "org.xmind.ui.command.extractTheme"; //$NON-NLS-1$

    private class ChangeThemeListener implements IOpenListener {

        private class ThemeOverrideDialog extends Dialog {
            private Button rememberCheck;

            protected ThemeOverrideDialog(Shell parentShell) {
                super(parentShell);
            }

            @Override
            protected void configureShell(Shell newShell) {
                super.configureShell(newShell);
                newShell.setText(Messages.ThemesView_Dialog_title);
            }

            @Override
            protected Control createDialogArea(Composite parent) {
                Composite composite = (Composite) super.createDialogArea(
                        parent);

                Label label = new Label(composite, SWT.NONE);
                label.setText(Messages.ThemesView_Dialog_message);

                createRememberCheck(composite);

                return composite;
            }

            private void createRememberCheck(Composite parent) {
                Composite composite = new Composite(parent, SWT.NONE);
                GridLayout gridLayout = new GridLayout(1, false);
                gridLayout.marginTop = 25;
                composite.setLayout(gridLayout);
                composite.setLayoutData(new GridData(GridData.FILL_BOTH));

                rememberCheck = new Button(composite, SWT.CHECK);
                rememberCheck.setText(Messages.ThemesView_Dialog_Check);
                rememberCheck.setLayoutData(
                        new GridData(SWT.FILL, SWT.BOTTOM, true, true));
            }

            protected Control createButtonBar(Composite parent) {
                Composite composite = new Composite(parent, SWT.NONE);
                composite.setLayoutData(
                        new GridData(SWT.FILL, SWT.FILL, true, false));
                GridLayout gridLayout = new GridLayout(2, false);
                gridLayout.marginWidth = convertHorizontalDLUsToPixels(
                        IDialogConstants.HORIZONTAL_MARGIN);
                gridLayout.marginHeight = convertVerticalDLUsToPixels(
                        IDialogConstants.VERTICAL_MARGIN);
                gridLayout.marginBottom = convertVerticalDLUsToPixels(
                        IDialogConstants.VERTICAL_MARGIN);
                gridLayout.verticalSpacing = convertVerticalDLUsToPixels(
                        IDialogConstants.VERTICAL_SPACING);
                gridLayout.horizontalSpacing = convertHorizontalDLUsToPixels(
                        IDialogConstants.HORIZONTAL_SPACING);
                composite.setLayout(gridLayout);

                createPrefLink(composite);

                Composite buttonBar = new Composite(composite, SWT.NONE);
                GridLayout layout = new GridLayout();
                layout.numColumns = 0; // this is incremented by createButton
                layout.makeColumnsEqualWidth = false;
                layout.marginWidth = 0;
                layout.marginHeight = 0;
                layout.horizontalSpacing = convertHorizontalDLUsToPixels(
                        IDialogConstants.HORIZONTAL_SPACING);
                layout.verticalSpacing = convertVerticalDLUsToPixels(
                        IDialogConstants.VERTICAL_SPACING);
                buttonBar.setLayout(layout);
                buttonBar.setLayoutData(
                        new GridData(SWT.END, SWT.CENTER, true, true));
                buttonBar.setFont(parent.getFont());

                createButtonsForButtonBar(buttonBar);

                return buttonBar;
            }

            private void createPrefLink(Composite parent) {
                Hyperlink prefLink = new Hyperlink(parent, SWT.SINGLE);
                prefLink.setText(Messages.ThemesView_Dialog_PrefLink);
                prefLink.setUnderlined(true);
                prefLink.setForeground(
                        parent.getDisplay().getSystemColor(SWT.COLOR_BLUE));

                prefLink.addHyperlinkListener(new HyperlinkAdapter() {
                    @Override
                    public void linkActivated(HyperlinkEvent e) {
                        PreferencesUtil.createPreferenceDialogOn(null,
                                "org.xmind.ui.ThemePrefPage", null, null) //$NON-NLS-1$
                                .open();
                    }
                });
            }

            @Override
            protected void createButtonsForButtonBar(Composite parent) {
                createButton(parent, IDialogConstants.OK_ID,
                        Messages.ThemesView_OverrideButton, true);
                createButton(parent, IDialogConstants.NO_ID,
                        Messages.ThemesView_KeepButton, false);
            }

            @Override
            protected void buttonPressed(int buttonId) {
                super.buttonPressed(buttonId);
                if (IDialogConstants.NO_ID == buttonId)
                    noPressed();
            }

            @Override
            protected void okPressed() {
                if (rememberCheck.getSelection())
                    pref.setValue(PrefConstants.THEME_APPLY,
                            PrefConstants.THEME_OVERRIDE);
                else
                    pref.setValue(PrefConstants.THEME_APPLY,
                            PrefConstants.ASK_USER);
                super.okPressed();
            }

            private void noPressed() {
                if (rememberCheck.getSelection())
                    pref.setValue(PrefConstants.THEME_APPLY,
                            PrefConstants.THEME_KEEP);
                else
                    pref.setValue(PrefConstants.THEME_APPLY,
                            PrefConstants.ASK_USER);
                setReturnCode(IDialogConstants.NO_ID);
                close();
            }
        }

        private IPreferenceStore pref = MindMapUIPlugin.getDefault()
                .getPreferenceStore();

        public void open(OpenEvent event) {
            if (updatingSelection)
                return;

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
            Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                    .getShell();
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
            IEditorPart activeEditor = PlatformUI.getWorkbench()
                    .getActiveWorkbenchWindow().getActivePage()
                    .getActiveEditor();

            if (activeEditor instanceof IGraphicalEditor) {
                IGraphicalEditor editor = (IGraphicalEditor) activeEditor;
                if (editor.getActivePageInstance() != null) {
                    ISheet sheet = (ISheet) editor.getActivePageInstance()
                            .getAdapter(ISheet.class);
                    return sheet;
                }
            }
            return null;
        }
    }

    private GalleryViewer viewer;

    private IGraphicalEditor activeEditor;

    private boolean updatingSelection = false;

    @Override
    protected Control createContents(Composite parent) {
        Composite composite = (Composite) super.createContents(parent);

        Composite composite2 = new Composite(composite, SWT.NONE);
        composite2.setBackground(composite2.getParent().getBackground());
        GridData gridData = new GridData(GridData.FILL_BOTH);
        composite2.setLayoutData(gridData);

        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.verticalSpacing = 2;
        composite2.setLayout(layout);

        createThemesContainer(composite2);
        createSeperator(composite2);
        createHyperlinks(composite2);

        return composite;
    }

    private void createThemesContainer(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(composite.getParent().getBackground());

        GridData gridData = new GridData(SWT.FILL, SWT.TOP, true, false);
        composite.setLayoutData(gridData);

        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 5;
        layout.marginTop = 5;
        composite.setLayout(layout);

        createThemesViewer(composite);

        IEditorPart editor = PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow().getActivePage().getActiveEditor();
        if (editor instanceof IGraphicalEditor) {
            setActiveEditor((IGraphicalEditor) editor);
        }
    }

    private void createThemesViewer(Composite parent) {
        GalleryViewer viewer = new GalleryViewer();

        EditDomain editDomain = new EditDomain();
        editDomain.installTool(GEF.TOOL_SELECT, new GallerySelectTool());
        editDomain.installEditPolicy(GalleryViewer.POLICY_NAVIGABLE,
                new GalleryNavigablePolicy());
        viewer.setEditDomain(editDomain);

        Properties properties = viewer.getProperties();
        properties.set(GalleryViewer.Horizontal, Boolean.TRUE);
        properties.set(GalleryViewer.Wrap, Boolean.TRUE);
        properties.set(GalleryViewer.FlatFrames, true);
        properties.set(GalleryViewer.SolidFrames, true);

        properties.set(GalleryViewer.HideTitle, false);
        properties.set(GalleryViewer.TitlePlacement,
                GalleryViewer.TITLE_BOTTOM);
        properties.set(GalleryViewer.ImageConstrained, true);

        properties.set(GalleryViewer.SingleClickToOpen, Boolean.FALSE);
        properties.set(GalleryViewer.CustomContentPaneDecorator, true);

        properties.set(GalleryViewer.FrameContentSize, new Dimension(94, 48));
        properties.set(GalleryViewer.Layout,
                new GalleryLayout(GalleryLayout.ALIGN_CENTER,
                        GalleryLayout.ALIGN_CENTER, 2, 2,
                        new Insets(0, 0, 0, 0)));

        Control control = viewer.createControl(parent);
        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.widthHint = 340;
        control.setLayoutData(gridData);

        viewer.setLabelProvider(new ThemeLabelProvider());
        viewer.addOpenListener(new ChangeThemeListener());
        viewer.setInput(getViewerInput());
    }

    private void createSeperator(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(composite.getParent().getBackground());
        GridData gridData = new GridData(GridData.FILL, GridData.CENTER, true,
                false);
        composite.setLayoutData(gridData);

        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 15;
        layout.marginHeight = 0;
        composite.setLayout(layout);

        Composite seperator = new Composite(composite, SWT.NONE);
        seperator.setBackground(
                new LocalResourceManager(JFaceResources.getResources(),
                        composite).createColor(ColorUtils.toRGB("#cbcbcb"))); //$NON-NLS-1$
        GridData gridData2 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gridData2.heightHint = 1;
        seperator.setLayoutData(gridData2);
        seperator.setLayout(new GridLayout());
    }

    private void createHyperlinks(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(composite.getParent().getBackground());
        GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        composite.setLayoutData(gridData);

        GridLayout layout = new GridLayout();
        layout.marginWidth = 15;
        layout.marginHeight = 5;
        layout.marginBottom = 5;
        layout.verticalSpacing = 4;
        composite.setLayout(layout);

        createMoreThemesHyperlink(composite);
        createManageThemesHyperlink(composite);
        createExtractThemeHyperlink(composite);
    }

    private void createMoreThemesHyperlink(Composite parent) {
        Hyperlink moreThemesHyperlink = createHyperlink(parent,
                Messages.ThemesPopover_MoreTheme_label);

        moreThemesHyperlink.addHyperlinkListener(new HyperlinkAdapter() {

            @Override
            public void linkActivated(HyperlinkEvent e) {
                E4Utils.showPart(IModelConstants.COMMAND_SHOW_MODEL_PART,
                        PlatformUI.getWorkbench().getActiveWorkbenchWindow(),
                        IModelConstants.PART_ID_THEMES, null,
                        IModelConstants.PART_STACK_ID_RIGHT);
            }
        });
    }

    private void createManageThemesHyperlink(Composite parent) {
        Hyperlink manageThemesHyperlink = createHyperlink(parent,
                Messages.ThemesPopover_ManagerTheme_label);

        manageThemesHyperlink.addHyperlinkListener(new HyperlinkAdapter() {

            @Override
            public void linkActivated(HyperlinkEvent e) {
                E4Utils.showPart(IModelConstants.COMMAND_SHOW_DIALOG_PART,
                        PlatformUI.getWorkbench().getActiveWorkbenchWindow(),
                        IModelConstants.PART_ID_RESOURCE_MANAGER,
                        IModelConstants.PAGE_ID_RESOURCE_MANAGER_THEME, null);

            }
        });
    }

    private void createExtractThemeHyperlink(Composite parent) {
        Hyperlink extractThemeHyperlink = createHyperlink(parent,
                Messages.ThemesPopover_Extract_Current_Theme_label);

        extractThemeHyperlink.addHyperlinkListener(new HyperlinkAdapter() {

            @Override
            public void linkActivated(HyperlinkEvent e) {
                IWorkbenchPage page = PlatformUI.getWorkbench()
                        .getActiveWorkbenchWindow().getActivePage();
                if (page == null) {
                    return;
                }

                IEditorPart editor = page.getActiveEditor();
                if (editor == null) {
                    return;
                }

                final IHandlerService handlerService = (IHandlerService) editor
                        .getSite().getService(IHandlerService.class);
                if (handlerService == null) {
                    return;
                }

                SafeRunner.run(new SafeRunnable() {

                    public void run() throws Exception {
                        handlerService.executeCommand(Extract_Theme_Command_ID,
                                null);
                    }
                });
            }
        });
    }

    private Hyperlink createHyperlink(Composite parent, String message) {
        Hyperlink hyperlink = new Hyperlink(parent, SWT.SINGLE);
        hyperlink.setBackground(hyperlink.getParent().getBackground());
        GridData gridData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        hyperlink.setLayoutData(gridData);

        hyperlink.setUnderlined(false);
        hyperlink.setText(message);

        return hyperlink;
    }

    private List<IStyle> getViewerInput() {
        List<IStyle> list = new ArrayList<IStyle>();

        DocumentBuilder documentBuilder = null;
        try {
            documentBuilder = XMLUtils.getDefaultDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        try {
            Bundle bundle = Platform.getBundle(MindMapUI.PLUGIN_ID);
            URL url = FileLocator.find(bundle, new Path(THEME_POPOVER_XML),
                    null);
            Document doc = documentBuilder.parse(url.openStream());

            IResourceManager rm = MindMapUI.getResourceManager();
            Set<IStyle> systemThemeSets = rm.getSystemThemeSheet()
                    .getStyles(IStyleSheet.MASTER_STYLES);

            NodeList nodeList = doc.getElementsByTagName(TAG_THEME);
            for (int i = 0; i < nodeList.getLength(); i++) {
                Element ele = (Element) nodeList.item(i);
                String themeId = ele.getAttribute(ATTR_ID);

                Iterator<IStyle> iterSystemTheme = systemThemeSets.iterator();
                while (iterSystemTheme.hasNext()) {
                    IStyle themeStyle = iterSystemTheme.next();
                    if (themeId.equals(themeStyle.getId())) {
                        list.add(themeStyle);
                        break;
                    }
                }
            }
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }

    private void setActiveEditor(IGraphicalEditor editor) {
        if (editor != activeEditor) {
            activeEditor = editor;
            updateSelection();
        }
    }

    private void updateSelection() {
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

        updatingSelection = true;
        viewer.setSelection(theme == null ? StructuredSelection.EMPTY
                : new StructuredSelection(theme));
        updatingSelection = false;
    }

    private void changeTheme(IStyle theme, String apply) {
        if (activeEditor == null)
            return;

        IGraphicalEditorPage page = activeEditor.getActivePageInstance();
        if (page == null)
            return;

        IGraphicalViewer viewer = page.getViewer();
        if (viewer == null)
            return;

        ISheetPart sheetPart = (ISheetPart) viewer.getAdapter(ISheetPart.class);
        if (sheetPart == null)
            return;

        EditDomain domain = page.getEditDomain();
        if (domain == null)
            return;

        domain.handleRequest(new Request(MindMapUI.REQ_MODIFY_THEME)
                .setViewer(viewer).setPrimaryTarget(sheetPart)
                .setParameter(MindMapUI.PARAM_RESOURCE, theme)
                .setParameter(MindMapUI.PARAM_OVERRIDE, apply));
        updateSelection();
        Control control = viewer.getControl();
        if (control != null && !control.isDisposed()) {
            control.forceFocus();
        }
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

        return sheet.getThemeId();
    }

}
