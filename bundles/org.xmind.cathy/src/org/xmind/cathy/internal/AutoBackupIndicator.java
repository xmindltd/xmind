package org.xmind.cathy.internal;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.util.Util;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;
import org.xmind.ui.resources.FontUtils;
import org.xmind.ui.util.PrefUtils;

public class AutoBackupIndicator extends WorkbenchWindowControlContribution
        implements IPropertyChangeListener, Listener {

    private static final int DISABLED = 1;

    private static final int ENABLED = 2;

    private class ChangeAutoSavePrefAction extends Action {
        private IPreferenceStore ps;
        private int value;

        /**
         * 
         */
        public ChangeAutoSavePrefAction(IPreferenceStore ps, String text,
                int value) {
            super(text);
            this.ps = ps;
            this.value = value;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.action.Action#run()
         */
        @Override
        public void run() {
            if (ps == null)
                return;

            changeStatus(value);
        }

        /**
         * @return the value
         */
        public int getValue() {
            return value;
        }
    }

    private static class OpenPreferencePageAction extends Action {

        /**
         * 
         */
        public OpenPreferencePageAction() {
            super(
                    WorkbenchMessages.AutoBackupIndicator_OpenPreferenceAction_text);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.action.Action#run()
         */
        @Override
        public void run() {
            PrefUtils.openPrefDialog(null, PrefUtils.GENERAL_PREF_PAGE_ID);
        }
    }

    private Control control;

    private Label label;

    private MenuManager menu;

    private IPreferenceStore ps;

    public AutoBackupIndicator() {
        super("org.xmind.ui.status.AutoSaveIndicator"); //$NON-NLS-1$
    }

    protected Control createControl(Composite parent) {
        ps = CathyPlugin.getDefault().getPreferenceStore();

        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 4;
        composite.setLayout(layout);

//        Label sep = new Label(composite, SWT.SEPARATOR | SWT.VERTICAL);
//        sep.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, false, true));

        label = new Label(composite, SWT.CENTER);
        label.setText(WorkbenchMessages.AutoBackupIndicator_AutoSaveDisabled_label);
        label.setFont(FontUtils.getRelativeHeight(JFaceResources.DEFAULT_FONT,
                Util.isMac() ? -2 : -1));
        label.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
        label.setCursor(label.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
        label.addListener(SWT.MouseDown, this);

        menu = new MenuManager();
        menu.add(new ChangeAutoSavePrefAction(
                ps,
                WorkbenchMessages.AutoBackupIndicator_DisableAutoSaveAction_text,
                DISABLED));
        menu.add(new ChangeAutoSavePrefAction(
                ps,
                WorkbenchMessages.AutoBackupIndicator_EnableAutoSaveAction_text,
                ENABLED));
        menu.add(new Separator());
        menu.add(new OpenPreferencePageAction());
        menu.createContextMenu(label);
        label.setMenu(menu.getMenu());

        updateEnablement();

//        composite.setLayoutData(new StatusLineLayoutData());

        ps.removePropertyChangeListener(this);
        ps.addPropertyChangeListener(this);

        this.control = composite;
        return composite;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.ContributionItem#update()
     */
    @Override
    public void update() {
        update(null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.ContributionItem#update(java.lang.String)
     */
    @Override
    public void update(String id) {
        super.update(id);

        if (label == null || label.isDisposed() || ps == null)
            return;

        if (id == null || CathyPlugin.AUTO_SAVE_ENABLED.equals(id)) {
            updateEnablement();
            updateSize();
        }
    }

    private void updateSize() {
        if (control == null || control.isDisposed())
            return;

        Point oldSize = control.getSize();
        control.pack(true);
        Point newSize = control.getSize();
        if (oldSize.equals(newSize))
            return;

        control.getParent().layout(true, true);
    }

    private void updateEnablement() {
        int value = getValue();
        if (value == ENABLED) {
            label.setText(WorkbenchMessages.AutoBackupIndicator_AutoSaveEnabled_label);
            int intervals = ps.getInt(CathyPlugin.AUTO_SAVE_INTERVALS);
            label.setToolTipText(NLS.bind(WorkbenchMessages.AutoSave_label2,
                    intervals));
        } else {
            label.setText(WorkbenchMessages.AutoBackupIndicator_AutoSaveDisabled_label);
            label.setToolTipText(WorkbenchMessages.AutoBackupIndicator_AutoSaveDisabled_description);
        }
        if (menu != null) {
            for (IContributionItem item : menu.getItems()) {
                if (item instanceof ActionContributionItem) {
                    IAction action = ((ActionContributionItem) item)
                            .getAction();
                    if (action instanceof ChangeAutoSavePrefAction) {
                        action.setChecked(((ChangeAutoSavePrefAction) action)
                                .getValue() == value);
                    }
                }
            }
        }
    }

    private int getValue() {
        return ps.getBoolean(CathyPlugin.AUTO_SAVE_ENABLED) ? ENABLED
                : DISABLED;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.ContributionItem#dispose()
     */
    @Override
    public void dispose() {
        if (label != null) {
            label.dispose();
            label = null;
        }
        control = null;
        if (menu != null) {
            menu.dispose();
            menu = null;
        }
        if (ps != null) {
            ps.removePropertyChangeListener(this);
            ps = null;
        }
        super.dispose();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse
     * .jface.util.PropertyChangeEvent)
     */
    public void propertyChange(final PropertyChangeEvent event) {
        if (control == null || control.isDisposed())
            return;
        control.getDisplay().asyncExec(new Runnable() {
            public void run() {
                update(event.getProperty());
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.
     * Event)
     */
    public void handleEvent(final Event event) {
        if (event.widget == label) {
            if (event.button == 1) {
                event.display.timerExec(10, new Runnable() {
                    public void run() {
                        Point loc = label.getParent().toDisplay(event.x,
                                event.y);
                        updateEnablement();
                        menu.getMenu().setLocation(loc);
                        menu.getMenu().setVisible(true);
                    }
                });
            }
        }
    }

    private void changeStatus(int value) {
        ps.setValue(CathyPlugin.AUTO_SAVE_ENABLED, value == ENABLED);
    }
}
