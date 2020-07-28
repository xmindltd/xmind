/*
 * *****************************************************************************
 * * Copyright (c) 2006-2012 XMind Ltd. and others. This file is a part of XMind
 * 3. XMind releases 3 and above are dual-licensed under the Eclipse Public
 * License (EPL), which is available at
 * http://www.eclipse.org/legal/epl-v10.html and the GNU Lesser General Public
 * License (LGPL), which is available at http://www.gnu.org/licenses/lgpl.html
 * See https://www.xmind.net/license.html for details. Contributors: XMind Ltd. -
 * initial API and implementation
 *******************************************************************************/
package org.xmind.ui.internal.dialogs;

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.progress.DetailedProgressViewer;
import org.eclipse.ui.internal.progress.FinishedJobs;
import org.eclipse.ui.internal.progress.JobInfo;
import org.eclipse.ui.internal.progress.JobTreeElement;
import org.eclipse.ui.internal.progress.ProgressManager;
import org.eclipse.ui.internal.progress.ProgressMessages;
import org.eclipse.ui.internal.progress.ProgressViewerContentProvider;
import org.eclipse.ui.internal.util.PrefUtil;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.e4models.ModelPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.resources.ColorUtils;
import org.xmind.ui.resources.FontUtils;

public class ProgressDialogPart extends ModelPart {

    @SuppressWarnings("unchecked")
    private static class ProgressViewerComparator extends ViewerComparator {

        @Override
        @SuppressWarnings("rawtypes")
        public int compare(Viewer testViewer, Object e1, Object e2) {
            return ((Comparable) e1).compareTo(e2);
        }

        @Override
        public void sort(final Viewer viewer, Object[] elements) {
            /*
             * https://bugs.eclipse.org/371354 This ordering is inherently
             * unstable, since it relies on modifiable properties of the
             * elements: E.g. the default implementation in JobTreeElement
             * compares getDisplayString(), many of whose implementations use
             * getPercentDone(). JavaSE 7+'s TimSort introduced a breaking
             * change: It now throws a new IllegalArgumentException for bad
             * comparators. Workaround is to retry a few times.
             */
            for (int retries = 3; retries > 0; retries--) {
                try {
                    Arrays.sort(elements, new Comparator<Object>() {

                        @Override
                        public int compare(Object a, Object b) {
                            return ProgressViewerComparator.this.compare(viewer,
                                    a, b);
                        }
                    });
                    return; // success
                } catch (IllegalArgumentException e) {
                    // retry
                }
            }

            // One last try that will log and throw TimSort's IAE if it happens:
            super.sort(viewer, elements);
        }
    }

    private class ProgressViewer extends DetailedProgressViewer {

        private Control nullContentArea;

        public ProgressViewer(Composite parent, int style) {
            super(parent, style);
        }

        @Override
        public void add(Object[] elements) {
            super.add(elements);
            updateForShowingContent();
        }

        @Override
        public void remove(Object[] elements) {
            super.remove(elements);
            updateForShowingContent();
        }

        @Override
        protected void inputChanged(Object input, Object oldInput) {
            super.inputChanged(input, oldInput);
            updateForShowingContent();
        }

        @Override
        protected void internalRefresh(Object element) {
            super.internalRefresh(element);
            updateForShowingContent();
        }

        private void updateForShowingContent() {
            Control control = getControl();
            if (control == null || control.isDisposed()) {
                return;
            }

            if (getContentProvider() == null) {
                return;
            }

            Object[] elements = ((IStructuredContentProvider) getContentProvider())
                    .getElements(getInput());
            if (elements.length != 0) {
                //show content viewer
                if (nullContentArea != null) {
                    nullContentArea.setVisible(false);
                    ((GridData) nullContentArea.getLayoutData()).exclude = true;
                }

                control.setVisible(true);
                ((GridData) control.getLayoutData()).exclude = false;

                if (removeAllHyperlink != null
                        && !removeAllHyperlink.isDisposed()) {
                    removeAllHyperlink.setEnabled(true);
                }

            } else {
                //show null content area
                control.setVisible(false);
                ((GridData) control.getLayoutData()).exclude = true;

                if (nullContentArea == null) {
                    nullContentArea = createNullContentArea(
                            control.getParent());
                }
                nullContentArea.setVisible(true);
                ((GridData) nullContentArea.getLayoutData()).exclude = false;

                if (removeAllHyperlink != null
                        && !removeAllHyperlink.isDisposed()) {
                    removeAllHyperlink.setEnabled(false);
                }
            }

            getControl().getParent().layout();
        }

        private Composite createNullContentArea(Composite parent) {
            Composite composite = new Composite(parent, SWT.NONE);
            composite.setBackground(composite.getParent().getBackground());
            final GridData layoutData = new GridData(GridData.FILL_BOTH);
            composite.setLayoutData(layoutData);
            GridLayout layout = new GridLayout(1, false);
            layout.marginWidth = 0;
            layout.marginHeight = 0;
            layout.verticalSpacing = 0;
            composite.setLayout(layout);

            createNullContent(composite);

            return composite;
        }

        private void createNullContent(Composite parent) {
            Composite composite = new Composite(parent, SWT.NONE);
            composite.setBackground(composite.getParent().getBackground());
            composite.setLayoutData(
                    new GridData(SWT.CENTER, SWT.CENTER, true, true));
            GridLayout layout = new GridLayout(1, false);
            layout.marginWidth = 0;
            layout.marginHeight = 0;
            layout.verticalSpacing = 20;
            composite.setLayout(layout);

            Label label = new Label(composite, SWT.NONE);
            label.setBackground(label.getParent().getBackground());
            label.setLayoutData(
                    new GridData(SWT.CENTER, SWT.CENTER, false, false));
            label.setImage((Image) resources.get(MindMapUI.getImages()
                    .get("progress_no_operations.png", true))); //$NON-NLS-1$

            Composite composite2 = new Composite(composite, SWT.NONE);
            composite2.setBackground(composite2.getParent().getBackground());
            composite2.setLayoutData(
                    new GridData(SWT.CENTER, SWT.CENTER, false, false));
            GridLayout layout2 = new GridLayout(1, false);
            layout2.marginWidth = 0;
            layout2.marginHeight = 0;
            layout2.verticalSpacing = 0;
            composite2.setLayout(layout2);

            Label label2 = new Label(composite2, SWT.NONE);
            label2.setBackground(label2.getParent().getBackground());
            label2.setForeground(
                    resources.createColor(ColorUtils.toDescriptor("#aaaaaa"))); //$NON-NLS-1$
            label2.setLayoutData(
                    new GridData(SWT.CENTER, SWT.CENTER, false, false));
            label2.setText(MindMapMessages.ProgressDialog_NullContet_message);
            label2.setFont(resources.createFont(
                    FontDescriptor.createFrom(FontUtils.relativeHeight(
                            label2.getFont().getFontData(), 1))));
        }
    }

    public static final String CONTEXT_MENU_ID = "org.xmind.ui.ProgressDialog"; //$NON-NLS-1$

    private ResourceManager resources;

    private DetailedProgressViewer viewer;

    private Control control;

    private Hyperlink removeAllHyperlink;

    private Action clearAllAction;

    private Action cancelAction;

    @Override
    protected void createContent(Composite parent) {
        resources = new LocalResourceManager(JFaceResources.getResources(),
                parent);

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        composite.setBackground(
                (Color) resources.get(ColorUtils.toDescriptor("#ffffff"))); //$NON-NLS-1$

        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 0;
        composite.setLayout(layout);

        createContentsSection(composite);
        createBottomSection(composite);

        this.control = composite;
    }

    private void createContentsSection(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        composite.setBackground(
                (Color) resources.get(ColorUtils.toDescriptor("#d8d8d8"))); //$NON-NLS-1$

        GridLayout layout = new GridLayout();
        layout.marginHeight = 1;
        layout.marginWidth = 1;
        layout.verticalSpacing = 0;
        composite.setLayout(layout);

        Composite composite2 = new Composite(composite, SWT.NONE);
        composite2.setLayoutData(new GridData(GridData.FILL_BOTH));
        composite2.setBackground(parent.getBackground());

        GridLayout layout2 = new GridLayout();
        layout2.marginHeight = 0;
        layout2.marginWidth = 0;
        layout2.marginBottom = 0;
        layout2.verticalSpacing = 0;
        layout2.horizontalSpacing = 0;
        composite2.setLayout(layout2);

        createProgressViewerSection(composite2);
        createShowOperationsCheck(composite2);
    }

    protected void createProgressViewerSection(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        composite.setBackground(parent.getBackground());

        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.marginBottom = 0;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 0;
        composite.setLayout(layout);

        createProgressViewer(composite);
    }

    private void createProgressViewer(Composite parent) {
        viewer = new ProgressViewer(parent, SWT.MULTI | SWT.H_SCROLL);
        viewer.setComparator(new ProgressViewerComparator());
        viewer.getControl()
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
                IWorkbenchHelpContextIds.RESPONSIVE_UI);

        initContentProvider();
        createClearAllAction();
        createCancelAction();
        initContextMenu();
        setSelectionProvider(viewer);
    }

    private void createShowOperationsCheck(Composite parent) {
        Composite border = new Composite(parent, SWT.NONE);
        border.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        border.setBackground(
                (Color) resources.get(ColorUtils.toDescriptor("#d8d8d8"))); //$NON-NLS-1$

        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.marginTop = 1;
        layout.marginBottom = 0;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 0;
        border.setLayout(layout);

        Composite composite = new Composite(border, SWT.NONE);
        composite
                .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        composite.setBackground(
                (Color) resources.get(ColorUtils.toDescriptor("#ffffff"))); //$NON-NLS-1$

        GridLayout layout2 = new GridLayout();
        layout2.marginHeight = 8;
        layout2.marginWidth = 0;
        layout2.marginTop = 0;
        layout2.marginBottom = 0;
        layout2.verticalSpacing = 0;
        layout2.horizontalSpacing = 0;
        composite.setLayout(layout2);

        Composite composite2 = new Composite(composite, SWT.NONE);
        composite2
                .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        composite2.setBackground(composite.getBackground());

        GridLayout layout3 = new GridLayout(2, false);
        layout3.marginHeight = 0;
        layout3.marginWidth = 15;
        layout3.verticalSpacing = 0;
        layout3.horizontalSpacing = 0;
        composite2.setLayout(layout3);

        final Button showSystemCheck = new Button(composite2, SWT.CHECK);
        showSystemCheck.setBackground(composite2.getBackground());
        showSystemCheck
                .setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
        showSystemCheck
                .setText(MindMapMessages.ProgressDialog_ShowSystem_check);
        showSystemCheck.setForeground(
                (Color) resources.get(ColorUtils.toDescriptor("#000000"))); //$NON-NLS-1$

        //set initial selection
        boolean showSystemJobs = PrefUtil.getAPIPreferenceStore()
                .getBoolean(IWorkbenchPreferenceConstants.SHOW_SYSTEM_JOBS);
        showSystemCheck.setSelection(showSystemJobs);

        showSystemCheck.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean showSystem = showSystemCheck.getSelection();
                PrefUtil.getAPIPreferenceStore().setValue(
                        IWorkbenchPreferenceConstants.SHOW_SYSTEM_JOBS,
                        showSystem);
                viewer.refresh();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
    }

    private void createBottomSection(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite
                .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        composite.setBackground(
                (Color) resources.get(ColorUtils.toDescriptor("#f3f3f3"))); //$NON-NLS-1$

        GridLayout layout = new GridLayout(2, false);
        layout.marginHeight = 14;
        layout.marginWidth = 0;
        layout.marginRight = 13;
        layout.marginBottom = 1;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 0;
        composite.setLayout(layout);

        createRemoveAllHyperlink(composite);
        createButtonBar(composite);
    }

    private void createRemoveAllHyperlink(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite
                .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        composite.setBackground(parent.getBackground());

        GridLayout layout = new GridLayout(1, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.marginLeft = 35;
        composite.setLayout(layout);

        removeAllHyperlink = new Hyperlink(composite, SWT.NONE);
        removeAllHyperlink.setBackground(composite.getBackground());
        removeAllHyperlink
                .setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
        removeAllHyperlink
                .setText(MindMapMessages.ProgressDialog_RemoveAll_hyperlink);
        removeAllHyperlink.setUnderlined(false);
        removeAllHyperlink.setForeground(
                (Color) resources.get(ColorUtils.toDescriptor("#6484fc"))); //$NON-NLS-1$
        removeAllHyperlink.setFont((Font) resources
                .get(FontDescriptor.createFrom(FontUtils.relativeHeight(
                        removeAllHyperlink.getFont().getFontData(), -1))));

        removeAllHyperlink.addHyperlinkListener(new IHyperlinkListener() {

            public void linkExited(HyperlinkEvent e) {
            }

            public void linkEntered(HyperlinkEvent e) {
            }

            public void linkActivated(HyperlinkEvent e) {
                clearAllAction.run();
            }
        });

        //set hyperlink enabled
        Object[] elements = ((IStructuredContentProvider) viewer
                .getContentProvider()).getElements(viewer.getInput());
        removeAllHyperlink.setEnabled(elements.length != 0);
    }

    private void createButtonBar(Composite parent) {
        Composite buttonBar = new Composite(parent, SWT.NONE);
        buttonBar.setLayoutData(
                new GridData(SWT.RIGHT, SWT.CENTER, false, false));
        buttonBar.setBackground(parent.getBackground());

        GridLayout layout = new GridLayout();
        layout.numColumns = 0;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 6;
        buttonBar.setLayout(layout);

        createButton(buttonBar, IDialogConstants.CANCEL_ID,
                IDialogConstants.CLOSE_LABEL, false);
    }

    private Button createButton(Composite parent, int id, String label,
            boolean defaultButton) {
        // increment the number of columns in the button bar
        ((GridLayout) parent.getLayout()).numColumns++;
        Button button = new Button(parent, SWT.PUSH);
        button.setBackground(parent.getBackground());
        button.setText(label);
        button.setFont(JFaceResources.getDialogFont());
        button.setData(Integer.valueOf(id));
        button.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent event) {
                buttonPressed(((Integer) event.widget.getData()).intValue());
            }
        });
        if (defaultButton) {
            Shell shell = parent.getShell();
            if (shell != null) {
                shell.setDefaultButton(button);
            }
        }
        setButtonLayoutData(button);
        return button;
    }

    private void setButtonLayoutData(Button button) {
        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        data.widthHint = 92;
        button.setLayoutData(data);
    }

    private void buttonPressed(int buttonId) {
        if (IDialogConstants.CANCEL_ID == buttonId) {
            cancelPressed();
        }
    }

    private void cancelPressed() {
        close();
    }

    private void close() {
        if (control != null && !control.isDisposed()) {
            control.getShell().close();
        }
    }

    @Override
    protected void setFocus() {
        super.setFocus();
        if (viewer != null) {
            viewer.setFocus();
        }
    }

    /**
     * Sets the content provider for the viewer.
     */
    private void initContentProvider() {
        ProgressViewerContentProvider provider = new ProgressViewerContentProvider(
                viewer, true, true);
        viewer.setContentProvider(provider);
        viewer.setInput(ProgressManager.getInstance());
    }

    /**
     * Initialize the context menu for the receiver.
     */
    private void initContextMenu() {
        MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
        Menu menu = menuMgr.createContextMenu(viewer.getControl());
        menuMgr.add(cancelAction);
        menuMgr.addMenuListener(new IMenuListener() {

            @Override
            public void menuAboutToShow(IMenuManager manager) {
                JobInfo info = getSelectedInfo();
                if (info == null) {
                    return;
                }
            }
        });
        menuMgr.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        viewer.getControl().setMenu(menu);

        //todo: this may have bug.
        registerContextMenu(viewer.getControl(), CONTEXT_MENU_ID);
    }

    /**
     * Get the currently selected job info. Only return it if it is the only
     * item selected and it is a JobInfo.
     *
     * @return JobInfo
     */
    private JobInfo getSelectedInfo() {
        IStructuredSelection selection = getSelection();
        if (selection != null && selection.size() == 1) {
            JobTreeElement element = (JobTreeElement) selection
                    .getFirstElement();
            if (element instanceof JobInfo) {
                return (JobInfo) element;
            }
        }
        return null;
    }

    /**
     * Return the selected objects. If any of the selections are not JobInfos or
     * there is no selection then return null.
     *
     * @return JobInfo[] or <code>null</code>.
     */
    private IStructuredSelection getSelection() {
        ESelectionService selectionService = getAdapter(
                ESelectionService.class);
        if (selectionService != null) {
            Object selection = selectionService.getSelection();
            return selection instanceof IStructuredSelection
                    ? (IStructuredSelection) selection : null;
        }

        return null;
    }

    /**
     * Create the cancel action for the receiver.
     */
    private void createCancelAction() {
        cancelAction = new Action(ProgressMessages.ProgressView_CancelAction) {

            @Override
            public void run() {
                viewer.cancelSelection();
            }
        };
    }

    /**
     * Create the clear all action for the receiver.
     */
    private void createClearAllAction() {
        clearAllAction = new Action(
                ProgressMessages.ProgressView_ClearAllAction) {

            @Override
            public void run() {
                FinishedJobs.getInstance().clearAll();
            }
        };
        clearAllAction.setToolTipText(
                ProgressMessages.NewProgressView_RemoveAllJobsToolTip);
        ImageDescriptor id = WorkbenchImages
                .getWorkbenchImageDescriptor("/elcl16/progress_remall.png"); //$NON-NLS-1$
        if (id != null) {
            clearAllAction.setImageDescriptor(id);
        }
        id = WorkbenchImages
                .getWorkbenchImageDescriptor("/dlcl16/progress_remall.png"); //$NON-NLS-1$
        if (id != null) {
            clearAllAction.setDisabledImageDescriptor(id);
        }
    }

    @Override
    public <T> T getAdapter(Class<T> adapter) {
        if (adapter.isAssignableFrom(DetailedProgressViewer.class)) {
            return adapter.cast(viewer);
        }
        return super.getAdapter(adapter);
    }

}
