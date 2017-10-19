package org.xmind.ui.internal.editor;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.prefs.PrefConstants;

public class OverviewCheckContribution extends ContributionItem
        implements IPropertyChangeListener, Listener {

    private static final String SHOW_OVERVIEW = "show_overview.png"; //$NON-NLS-1$

    private static final String HIDE_OVERVIEW = "hide_overview.png"; //$NON-NLS-1$

    private IPreferenceStore ps;

    private Control control;

    private ToolItem check;

    private ResourceManager resources;

    public OverviewCheckContribution() {
        ps = MindMapUIPlugin.getDefault().getPreferenceStore();
    }

    public void fill(ToolBar parent, int index) {
        Composite composite = new Composite(parent, SWT.NONE);

        resources = new LocalResourceManager(JFaceResources.getResources(),
                composite);

        if (index < 0)
            check = new ToolItem(parent, SWT.PUSH);
        else
            check = new ToolItem(parent, SWT.PUSH, index++);

        check.setImage(
                (Image) resources.get(getImageDescriptor(HIDE_OVERVIEW)));
        check.setToolTipText(MindMapMessages.OverviewCheck_Overview_ON);

        check.addListener(SWT.Selection, this);

        updateCheck();

        ps.removePropertyChangeListener(this);
        ps.addPropertyChangeListener(this);

        this.control = composite;
    }

    private void updateCheck() {
        boolean value = getValue();

        check.setImage((Image) resources.get(
                getImageDescriptor(value ? HIDE_OVERVIEW : SHOW_OVERVIEW)));
        check.setToolTipText(value ? MindMapMessages.OverviewCheck_Overview_OFF
                : MindMapMessages.OverviewCheck_Overview_ON);
    }

    private boolean getValue() {
        return ps.getBoolean(PrefConstants.SHOW_OVERVIEW);
    }

    @Override
    public void handleEvent(final Event event) {
        if (event.widget == check) {
            event.display.timerExec(10, new Runnable() {
                public void run() {
                    changeStatus(!getValue());
                }
            });
        }
    }

    @Override
    public void propertyChange(final PropertyChangeEvent event) {
        if (control == null || control.isDisposed())
            return;

        control.getDisplay().asyncExec(new Runnable() {

            @Override
            public void run() {
                update(event.getProperty());
            }
        });
    }

    public void update(String id) {
        if (check == null || check.isDisposed() || ps == null)
            return;

        if (id == null || PrefConstants.SHOW_OVERVIEW.equals(id)) {
            updateCheck();
        }
    }

    private void changeStatus(boolean value) {
        ps.setValue(PrefConstants.SHOW_OVERVIEW, value);
    }

    public void dispose() {
        if (check != null) {
            check.dispose();
            check = null;
        }
        control = null;
        if (ps != null) {
            ps.removePropertyChangeListener(this);
            ps = null;
        }
    }

    private ImageDescriptor getImageDescriptor(String path) {
        URL url;
        try {
            url = new URL(
                    "platform:/plugin/org.xmind.ui.mindmap/$nl$/icons/" + path); //$NON-NLS-1$
        } catch (MalformedURLException e) {
            return null;
        }
        URL locatedURL = FileLocator.find(url);
        if (locatedURL != null)
            url = locatedURL;
        return ImageDescriptor.createFromURL(url);
    }

}
