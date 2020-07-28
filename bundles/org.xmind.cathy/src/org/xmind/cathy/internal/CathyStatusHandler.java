/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See https://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.cathy.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.statushandlers.AbstractStatusHandler;
import org.eclipse.ui.statushandlers.IStatusAdapterConstants;
import org.eclipse.ui.statushandlers.StatusAdapter;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.statushandlers.StatusManager.INotificationTypes;
import org.xmind.core.internal.UserDataConstants;
import org.xmind.ui.internal.ToolkitPlugin;
import org.xmind.ui.internal.statushandlers.RuntimeErrorDialog;

public class CathyStatusHandler extends AbstractStatusHandler {

    private static final String PROPERTY_PREFIX = "org.xmind.cathy.statusHandlers.adapters"; //$NON-NLS-1$

    private static final QualifiedName BLOCK = new QualifiedName(
            PROPERTY_PREFIX, "block"); //$NON-NLS-1$

    private List<StatusAdapter> statusQueue = new ArrayList<StatusAdapter>(4);

    private RuntimeErrorDialog currentDialog = null;

    public CathyStatusHandler() {
    }

    public boolean supportsNotification(int type) {
        if (type == INotificationTypes.HANDLED) {
            return true;
        }
        return super.supportsNotification(type);
    }

    public void handle(final StatusAdapter statusAdapter, final int style) {
        CathyPlugin.getDefault().getUsageDataCollector().trackEvent(
                UserDataConstants.CATEGORY_ERROR,
                UserDataConstants.ENCOUNTER_ERROR);

        if (((style & StatusManager.SHOW) == StatusManager.SHOW)
                || ((style & StatusManager.BLOCK) == StatusManager.BLOCK)) {

            final boolean block = ((style
                    & StatusManager.BLOCK) == StatusManager.BLOCK);

            addProperties(statusAdapter, block);

            if (Display.getCurrent() != null) {
                showStatusAdapter(statusAdapter);
            } else {
                if (block) {
                    Display.getDefault().syncExec(new Runnable() {
                        public void run() {
                            showStatusAdapter(statusAdapter);
                        }
                    });

                } else {
                    Display.getDefault().asyncExec(new Runnable() {
                        public void run() {
                            showStatusAdapter(statusAdapter);
                        }
                    });
                }
            }
        }

        if ((style & StatusManager.LOG) == StatusManager.LOG) {
            StatusManager.getManager()
                    .addLoggedStatus(statusAdapter.getStatus());
            ToolkitPlugin.getDefault().getLog().log(statusAdapter.getStatus());
        }
    }

    private void addProperties(final StatusAdapter statusAdapter,
            boolean block) {
        // Add timestamp:
        if (statusAdapter.getProperty(
                IStatusAdapterConstants.TIMESTAMP_PROPERTY) == null) {
            statusAdapter.setProperty(
                    IStatusAdapterConstants.TIMESTAMP_PROPERTY,
                    Long.valueOf(System.currentTimeMillis()));
        }

        statusAdapter.setProperty(BLOCK, Boolean.valueOf(block));
    }

    /**
     * Shows the status adapter.
     * 
     * @param statusAdapter
     *            the status adapter to show
     * @param block
     *            <code>true</code> to request a modal dialog and suspend the
     *            calling thread till the dialog is closed, <code>false</code>
     *            otherwise.
     */
    private void showStatusAdapter(final StatusAdapter statusAdapter) {
        if (!PlatformUI.isWorkbenchRunning()) {
            // we are shutting down, so just log
            ToolkitPlugin.getDefault().getLog().log(statusAdapter.getStatus());
            return;
        }

        statusQueue.add(statusAdapter);

        if (currentDialog == null) {
            currentDialog = showErrorDialogFor(statusAdapter);
        }

        if (((Boolean) statusAdapter.getProperty(BLOCK)).booleanValue()) {
            Display display = Display.getCurrent();
            if (display != null && !display.isDisposed()) {
                while (statusQueue.contains(statusAdapter)
                        && !display.isDisposed()) {
                    if (!display.readAndDispatch()) {
                        display.sleep();
                    }
                }
            }
        }
    }

    private RuntimeErrorDialog showErrorDialogFor(
            final StatusAdapter statusAdapter) {
        StatusManager.getManager().fireNotification(INotificationTypes.HANDLED,
                new StatusAdapter[] { statusAdapter });

        int style = RuntimeErrorDialog.NONE;
        if (((Boolean) statusAdapter.getProperty(BLOCK)).booleanValue()) {
            style |= RuntimeErrorDialog.BLOCKED;
        }
        RuntimeErrorDialog dialog = new RuntimeErrorDialog(style, statusAdapter,
                null, CathyPlugin.getDefault().getErrorReporter());
        dialog.create();
        dialog.getShell().addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                statusQueue.remove(statusAdapter);
                if (statusQueue.size() > 0) {
                    currentDialog = showErrorDialogFor(statusQueue.get(0));
                } else {
                    currentDialog = null;
                }
            }
        });
        dialog.open();
        CathyPlugin.getDefault().getUsageDataCollector().trackEvent(
                UserDataConstants.CATEGORY_ERROR,
                UserDataConstants.SHOW_ERROR_DIALOG);

        return dialog;
    }

}
