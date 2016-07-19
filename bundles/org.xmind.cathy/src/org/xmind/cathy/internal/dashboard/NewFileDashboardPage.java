package org.xmind.cathy.internal.dashboard;

import java.io.File;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ColorDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ToolBar;
import org.xmind.cathy.internal.WorkbenchMessages;
import org.xmind.ui.internal.dashboard.pages.DashboardPage;
import org.xmind.ui.internal.dashboard.pages.IDashboardPage;
import org.xmind.ui.mindmap.ITemplate;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.resources.ColorUtils;
import org.xmind.ui.tabfolder.MTabBar;
import org.xmind.ui.tabfolder.MTabBarItem;
import org.xmind.ui.util.StyleProvider;

public class NewFileDashboardPage extends DashboardPage
        implements ISelectionChangedListener {

    private class SegmentBarStyleProvider extends StyleProvider {
        @Override
        public Font getFont(Object widget, String key) {
            if (widget instanceof MTabBarItem) {
                if (TEXT.equals(key))
                    return (Font) resourceManager
                            .get(JFaceResources.getDefaultFontDescriptor()
                                    .setHeight(Util.isMac() ? 10 : 12));
            }
            return super.getFont(widget, key);
        }

        @Override
        public int getWidth(Object widget, String key, int defaultValue) {
            if (widget instanceof MTabBarItem) {
                if (key == null)
                    return 100;
            } else if (widget instanceof MTabBar) {
                if (BORDER.equals(key))
                    return 1;
                if (SEPARATOR.equals(key))
                    return 1;
                if (CORNER.equals(key))
                    return 6;
            }
            return super.getWidth(widget, key, defaultValue);
        }

        @Override
        public int getHeight(Object widget, String key, int defaultValue) {
            if (widget instanceof MTabBarItem) {
                //FIXME 
                if (key == null)
                    return 26;
            } else if (widget instanceof MTabBar) {
                if (BORDER.equals(key))
                    return 1;
                if (SEPARATOR.equals(key))
                    return 1;
                if (CORNER.equals(key))
                    return 6;
            }
            return super.getHeight(widget, key, defaultValue);
        }

        @Override
        public int getPosition(Object widget, String key, int defaultValue) {
            if (widget instanceof MTabBarItem) {
                if (TEXT.equals(key))
                    return SWT.BOTTOM;
            }
            return super.getPosition(widget, key, defaultValue);
        }

        @Override
        public Color getColor(Object widget, String key) {
            if (widget instanceof MTabBarItem) {
                MTabBarItem item = (MTabBarItem) widget;
                if (FILL.equals(key)) {
                    if (item.isSelected())
                        return (Color) resourceManager.get(ColorDescriptor
                                .createFrom(ColorUtils.toRGB("#6B6A6B"))); //$NON-NLS-1$
                } else if (TEXT.equals(key)) {
                    if (item.isSelected())
                        return (Color) resourceManager.get(ColorDescriptor
                                .createFrom(ColorUtils.toRGB("#FFFFFF"))); //$NON-NLS-1$
                    return (Color) resourceManager.get(ColorDescriptor
                            .createFrom(ColorUtils.toRGB("#2B2A2B"))); //$NON-NLS-1$
                }
            } else if (widget instanceof MTabBar) {
                if (BORDER.equals(key) || SEPARATOR.equals(key))
                    return (Color) resourceManager.get(ColorDescriptor
                            .createFrom(ColorUtils.toRGB("#A6A6A6"))); //$NON-NLS-1$
            }
            return super.getColor(widget, key);
        }

        @Override
        public int getAlpha(Object widget, String key, int defaultValue) {
            if (widget instanceof MTabBar) {
                if (BORDER.equals(key))
                    return 0xC0;
            }
            return super.getAlpha(widget, key, defaultValue);
        }
    }

    private Control control = null;

    private ResourceManager resourceManager = null;

    private Composite titleBar = null;
    private Composite rightBar = null;
    private MTabBar tabBar = null;
    private ToolBar normalModeEditContainer = null;
    private Control editModeContainer = null;

    private boolean normalOrEditMode = true;

    private Composite pageContainer = null;

    private ITemplate selectedTemplate = null;
    private Action deleteTemplatesAction = null;

    public void createControl(Composite parent) {
        final Composite composite = new Composite(parent, SWT.NONE);

        this.resourceManager = new LocalResourceManager(
                JFaceResources.getResources(), composite);

        composite.setBackground(parent.getBackground());
        composite.setForeground(parent.getForeground());

        GridLayoutFactory.fillDefaults().spacing(0, 0).numColumns(1)
                .applyTo(composite);

        Control titleBar = createTitleBar(composite);
        GridDataFactory.fillDefaults().hint(SWT.DEFAULT, 44)
                .align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(titleBar);

        Label separator = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL)
                .grab(true, false).applyTo(separator);

        Control pageContainer = createPageContainer(composite);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL)
                .grab(true, true).applyTo(pageContainer);

        this.control = composite;

        addPage(new NewFromStructuresDashboardPage(),
                WorkbenchMessages.DashboardBlankPage_name,
                WorkbenchMessages.DashboardBlankPage_message);
        addPage(new NewFromTemplatesDashboardPage(),
                WorkbenchMessages.DashboardTemplatesPage_name,
                WorkbenchMessages.DashboardTemplatesPage_message);

        setTitleBarComponentLayoutData();

        showPage(tabBar.getItem(0));
    }

    private void setTitleBarComponentLayoutData() {
        FormData tabData = new FormData();
        // A side can't be attached to parent, so we have to get the size first.
        // CAUTION: This depends on the fact that the size won't change.
        Point tabSize = tabBar.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        tabData.left = new FormAttachment(50, -tabSize.x / 2);
        tabData.top = new FormAttachment(0, 0);
        tabData.bottom = new FormAttachment(100, 0);
        tabBar.setLayoutData(tabData);

        FormData rightData = new FormData();
        rightData.top = new FormAttachment(0, 0);
        rightData.right = new FormAttachment(100, 0);
        rightData.bottom = new FormAttachment(100, 0);
        rightBar.setLayoutData(rightData);
    }

    private void addPage(IDashboardPage page, String title,
            String description) {
        page.setTitle(title);
        page.setDescription(description);
        page.setContext(getContext());

        MTabBarItem item = new MTabBarItem(tabBar, SWT.RADIO);
        item.setText(title);
        item.setData(page);
    }

    private Control createTitleBar(Composite parent) {
        titleBar = new Composite(parent, SWT.NONE);
        FormLayout titleBarLayout = new FormLayout();
        titleBarLayout.marginLeft = 10;
        titleBarLayout.marginRight = 10;
        titleBarLayout.marginTop = 9;
        titleBarLayout.marginBottom = 9;
        titleBar.setLayout(titleBarLayout);
        titleBar.setForeground(parent.getForeground());
//        titleBar.setBackground(parent.getBackground());
        titleBar.setBackground((Color) JFaceResources.getResources()
                .get(ColorUtils.toDescriptor("#ececec"))); //$NON-NLS-1$

        Control titleLabel = createLeftTitleBarControl(titleBar);
        FormData leftData = new FormData();
        leftData.top = new FormAttachment(0, 0);
        leftData.left = new FormAttachment(0, 0);
        leftData.bottom = new FormAttachment(100, 0);
        titleLabel.setLayoutData(leftData);

        createCentralContainer(titleBar);
//        FormData tabData = new FormData();
//        // A side can't be attached to parent, so we have to get the size first.
//        // CAUTION: This depends on the fact that the size won't change.
//        Point tabSize = tabBar.computeSize(SWT.DEFAULT, SWT.DEFAULT);
//        tabData.left = new FormAttachment(50, -tabSize.x / 2);
//        tabData.top = new FormAttachment(0, 0);
//        tabData.bottom = new FormAttachment(100, 0);
//        tabBar.setLayoutData(tabData);

        createRightBar(titleBar);
//        FormData rightData = new FormData();
//        rightData.top = new FormAttachment(0, 0);
//        rightData.right = new FormAttachment(100, 0);
//        rightData.bottom = new FormAttachment(100, 0);
//        rightBar.setLayoutData(rightData);

        return titleBar;
    }

    private Control createLeftTitleBarControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(parent.getBackground());
        composite.setForeground(parent.getForeground());
        GridLayoutFactory.fillDefaults().numColumns(2).spacing(3, 0)
                .applyTo(composite);

        Label titleNameLabel = new Label(composite, SWT.WRAP);
        titleNameLabel.setBackground(composite.getBackground());
        titleNameLabel.setForeground(composite.getForeground());
        titleNameLabel.setFont((Font) JFaceResources.getResources().get(
                JFaceResources.getHeaderFontDescriptor().increaseHeight(-1)));
        titleNameLabel.setText(
                WorkbenchMessages.NewFileDashboardPage_leftTitleBar_text);
        GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.CENTER)
                .grab(true, true).applyTo(titleNameLabel);

        return composite;
    }

    private Control createCentralContainer(Composite parent) {
        tabBar = new MTabBar(parent, SWT.NONE);
        tabBar.setBackground(
                parent.getDisplay().getSystemColor(SWT.COLOR_TRANSPARENT));
        tabBar.setStyleProvider(new SegmentBarStyleProvider());

        tabBar.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                showPage((MTabBarItem) event.item);
            }
        });
        return tabBar;
    }

    private Control createRightBar(Composite composite) {
        rightBar = new Composite(composite, SWT.NONE);
        GridLayoutFactory.fillDefaults().numColumns(2).applyTo(rightBar);

        ToolBarManager normalModeToolBarManager = new ToolBarManager();
        Action editModeAction = new Action(
                WorkbenchMessages.NewFileDashboardPage_GoToEditTemplateMode_label) {
            @Override
            public void run() {
                normalOrEditMode = false;
                updateTitleBar();
            }
        };
        editModeAction.setToolTipText(
                WorkbenchMessages.NewFileDashboardPage_GoIntoTemplatesManagerMode_tooltip);
        normalModeToolBarManager.add(editModeAction);
        normalModeEditContainer = normalModeToolBarManager
                .createControl(rightBar);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
                .applyTo(normalModeEditContainer);

        editModeContainer = createEditModeContainer(rightBar);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL)
                .applyTo(editModeContainer);

        return rightBar;
    }

    private Control createEditModeContainer(final Composite rightBar) {
        ToolBarManager editModeToolBarManager = new ToolBarManager();
        Action addTemplateAction = new Action(
                WorkbenchMessages.NewFileDashboardPage_AddTemplates_label) {
            @Override
            public void run() {
                FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
                String ext = "*" + MindMapUI.FILE_EXT_TEMPLATE; //$NON-NLS-1$
                dialog.setFilterExtensions(new String[] { ext });
                dialog.setFilterNames(new String[] { NLS.bind("{0} ({1})", //$NON-NLS-1$
                        WorkbenchMessages.NewFileDashboardPage_TemplateFilterName_label,
                        ext) });
                String path = dialog.open();
                if (path == null)
                    return;

                final File templateFile = new File(path);
                if (templateFile != null && templateFile.exists()) {
                    SafeRunner.run(new SafeRunnable() {
                        public void run() throws Exception {
                            MindMapUI.getResourceManager()
                                    .addUserTemplateFromWorkbookURI(
                                            templateFile.toURI());
                        }
                    });
                }

            }
        };
        addTemplateAction.setToolTipText(
                WorkbenchMessages.NewFileDashboardPage_AddTemplates_tooltip);

        deleteTemplatesAction = new Action(
                WorkbenchMessages.NewFileDashboardPage_DeleteTemplate_label) {
            @Override
            public void run() {
                deleteSelectedTemplate();
            }
        };
        deleteTemplatesAction.setToolTipText(
                WorkbenchMessages.NewFileDashboardPage_DeleteTemplate_tooltip);
        deleteTemplatesAction.setEnabled(false);
        Action exitEditModeAction = new Action(
                WorkbenchMessages.NewFileDashboardPage_ExitEditTemplatesMode_label) {
            @Override
            public void run() {
                normalOrEditMode = true;
                updateTitleBar();
            }
        };
        exitEditModeAction.setToolTipText(
                WorkbenchMessages.NewFileDashboardPage_ExitEditMode_tooltip);
        editModeToolBarManager.add(addTemplateAction);
        editModeToolBarManager.add(deleteTemplatesAction);
        editModeToolBarManager.add(exitEditModeAction);
        return editModeToolBarManager.createControl(rightBar);
    }

    private void deleteSelectedTemplate() {
        ITemplate template = selectedTemplate;
        if (template != null) {
            if (!MessageDialog.openConfirm(rightBar.getShell(),
                    WorkbenchMessages.ConfirmDeleteTemplateDialog_title,
                    NLS.bind(
                            WorkbenchMessages.ConfirmDeleteTemplateDialog_message_withTemplateName,
                            template.getName())))
                return;
            MindMapUI.getResourceManager().removeUserTemplate(template);
        }
    }

    private Control createPageContainer(Composite parent) {
        pageContainer = new Composite(parent, SWT.NONE);
        pageContainer.setBackground(parent.getBackground());

        pageContainer.setLayout(new StackLayout());

        return pageContainer;
    }

    private void showPage(MTabBarItem item) {
        if (pageContainer == null || pageContainer.isDisposed())
            return;

        StackLayout layout = (StackLayout) pageContainer.getLayout();

        if (item == null) {
            layout.topControl = null;
            pageContainer.layout(true);
            return;
        }

        IDashboardPage page = (IDashboardPage) item.getData();
        if (page != null) {
            if (page.getControl() == null) {
                page.createControl(pageContainer);
                if (page instanceof NewFromTemplatesDashboardPage) {
                    ((NewFromTemplatesDashboardPage) page)
                            .addSelectionChangedListener(this);
                }
            }
            layout.topControl = page.getControl();
            pageContainer.layout(true);

            updateTitleBar();
        }

    }

    private void updateTitleBar() {
        MTabBarItem item = tabBar.getSelection();
        IDashboardPage page = (IDashboardPage) item.getData();
        if (page instanceof NewFromTemplatesDashboardPage) {
            ((GridData) normalModeEditContainer
                    .getLayoutData()).exclude = !normalOrEditMode;
            normalModeEditContainer.setVisible(normalOrEditMode);
            ((GridData) editModeContainer
                    .getLayoutData()).exclude = normalOrEditMode;
            editModeContainer.setVisible(!normalOrEditMode);
            ((NewFromTemplatesDashboardPage) page).setState(normalOrEditMode);
        } else if (page instanceof NewFromStructuresDashboardPage) {
            normalOrEditMode = true;
            ((GridData) normalModeEditContainer.getLayoutData()).exclude = true;
            normalModeEditContainer.setVisible(false);
            ((GridData) editModeContainer.getLayoutData()).exclude = true;
            editModeContainer.setVisible(false);
        }
        titleBar.layout(true);
    }

    public Control getControl() {
        return this.control;
    }

    public void setFocus() {
        MTabBarItem item = tabBar.getSelection();
        if (item != null) {
            IDashboardPage page = (IDashboardPage) item.getData();
            if (page != null) {
                page.setFocus();
            }
        }
    }

    public void selectionChanged(SelectionChangedEvent event) {
        ITemplate template = findTemplate(event.getSelection());
        setSelectedTemplate(template);
    }

    private ITemplate findTemplate(ISelection selection) {
        if (selection instanceof IStructuredSelection) {
            Object element = ((IStructuredSelection) selection)
                    .getFirstElement();
            if (element instanceof ITemplate) {
                return (ITemplate) element;
            }
        }
        return null;
    }

    private void setSelectedTemplate(ITemplate template) {
        this.selectedTemplate = template;
        if (deleteTemplatesAction != null) {
            boolean enabled = selectedTemplate != null && MindMapUI
                    .getResourceManager().isUserTemplate(selectedTemplate);
            deleteTemplatesAction.setEnabled(enabled);
        }
    }

}
