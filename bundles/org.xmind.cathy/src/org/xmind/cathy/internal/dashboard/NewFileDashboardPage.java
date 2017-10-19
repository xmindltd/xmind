package org.xmind.cathy.internal.dashboard;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ColorDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.xmind.cathy.internal.CathyPlugin;
import org.xmind.cathy.internal.WorkbenchMessages;
import org.xmind.ui.internal.dashboard.pages.DashboardPage;
import org.xmind.ui.internal.dashboard.pages.IDashboardContext;
import org.xmind.ui.internal.dashboard.pages.IDashboardPage;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.resources.ColorUtils;
import org.xmind.ui.tabfolder.MTabBar;
import org.xmind.ui.tabfolder.MTabBarItem;
import org.xmind.ui.util.StyleProvider;

public class NewFileDashboardPage extends DashboardPage implements IAdaptable {

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

    private Composite pageContainer = null;

    private List<IDashboardPage> pages = new ArrayList<IDashboardPage>();

    @PostConstruct
    private void init(IDashboardContext dashboardContext) {
        NewFromStructuresDashboardPage structurePage = new NewFromStructuresDashboardPage();
        structurePage.setTitle(WorkbenchMessages.DashboardBlankPage_name);
        structurePage
                .setDescription(WorkbenchMessages.DashboardBlankPage_message);
        structurePage.setContext(dashboardContext);
        pages.add(structurePage);

        NewFromTemplatesDashboardPage templatePage = new NewFromTemplatesDashboardPage();
        templatePage.setTitle(WorkbenchMessages.DashboardTemplatesPage_name);
        templatePage.setDescription(
                WorkbenchMessages.DashboardTemplatesPage_message);
        templatePage.setContext(dashboardContext);
        templatePage.registerAvailableCommands();
        pages.add(templatePage);

    }

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

        for (IDashboardPage page : pages) {
            MTabBarItem item = new MTabBarItem(tabBar, SWT.RADIO);
            item.setText(page.getTitle());
            item.setData(page);
        }

        setTitleBarComponentLayoutData();

        showPage(tabBar.getItem(0));
    }

    private void setTitleBarComponentLayoutData() {
        FormData tabData = new FormData();
        // A side can't be attached to parent, so we have to get the size first.
        // CAUTION: This depends on the fact that the size won't change.
        Point tabSize = tabBar.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        tabData.left = new FormAttachment(50, -tabSize.x / 2);
        tabData.top = new FormAttachment(50, -tabSize.y / 2);
        tabData.bottom = new FormAttachment(50, tabSize.y / 2);
        tabBar.setLayoutData(tabData);

        FormData rightData = new FormData();
        rightData.top = new FormAttachment(0, 0);
        rightData.right = new FormAttachment(100, 0);
        rightData.bottom = new FormAttachment(100, 0);
        rightBar.setLayoutData(rightData);
    }

    private Control createTitleBar(Composite parent) {
        titleBar = new Composite(parent, SWT.NONE);
        FormLayout titleBarLayout = new FormLayout();
        titleBarLayout.marginWidth = 10;
        titleBarLayout.marginHeight = 0;
        titleBarLayout.marginRight = 15;
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
        GridLayoutFactory.fillDefaults().numColumns(1).applyTo(rightBar);

        createImportButton(rightBar);

        return rightBar;
    }

    private void createImportButton(Composite parent) {
        final Label importBtton = new Label(parent, SWT.NONE);
        importBtton.setBackground(parent.getBackground());
        importBtton.setToolTipText(
                WorkbenchMessages.NewFileDashboardPage_Import_button);
        importBtton.setImage((Image) resourceManager.get(
                CathyPlugin.imageDescriptorFromPlugin(CathyPlugin.PLUGIN_ID,
                        "dashboard/new/button_import.png"))); //$NON-NLS-1$
        GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER)
                .grab(true, true).applyTo(importBtton);

        final IAction addTemplateAction = getAddTemplateAction();

        importBtton.addListener(SWT.MouseDown, new Listener() {

            @Override
            public void handleEvent(Event event) {
                addTemplateAction.run();
            }
        });

        importBtton.addMouseTrackListener(new MouseTrackAdapter() {

            @Override
            public void mouseExit(MouseEvent e) {
                importBtton.setImage((Image) resourceManager.get(CathyPlugin
                        .imageDescriptorFromPlugin(CathyPlugin.PLUGIN_ID,
                                "dashboard/new/button_import.png"))); //$NON-NLS-1$
            }

            @Override
            public void mouseEnter(MouseEvent e) {
                importBtton.setImage((Image) resourceManager.get(CathyPlugin
                        .imageDescriptorFromPlugin(CathyPlugin.PLUGIN_ID,
                                "dashboard/new/button_import_hover.png"))); //$NON-NLS-1$
            }
        });
    }

    private IAction getAddTemplateAction() {
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

        return addTemplateAction;
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
            }
            layout.topControl = page.getControl();
            pageContainer.layout(true);

            updateTitleBar();
        }

        getContext().setSelectionProvider(getAdapter(ISelectionProvider.class));
    }

    @Override
    public void dispose() {
        if (pages != null) {
            for (IDashboardPage page : pages) {
                page.dispose();
            }
            pages.clear();
            pages = null;
        }
        super.dispose();
    }

    private void updateTitleBar() {
        MTabBarItem item = tabBar.getSelection();
        IDashboardPage page = (IDashboardPage) item.getData();
        if (page instanceof NewFromTemplatesDashboardPage) {
            rightBar.setVisible(true);
        } else if (page instanceof NewFromStructuresDashboardPage) {
            rightBar.setVisible(false);
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

    public <T> T getAdapter(Class<T> adapter) {
        MTabBarItem item = tabBar.getSelection();
        IDashboardPage page = (IDashboardPage) item.getData();
        if (page instanceof IAdaptable) {
            return ((IAdaptable) page).getAdapter(adapter);
        }

        return null;
    }

}
