/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 *
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL),
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 *
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package net.xmind.workbench.internal;

import java.util.regex.Pattern;

import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.xmind.core.net.internal.XMindNetRequest;

import net.xmind.workbench.ui.internal.NewsletterSubscriptionDialog;

public class NewsletterSubscriptionReminder {

    private static final String SETTINGS_SECTION = "newsletterSubscription"; //$NON-NLS-1$

    private static final String KEY_LAUNCH_COUNT = "launchCount"; //$NON-NLS-1$

    private static final String KEY_NEVER_REMIND = "neverRemind"; //$NON-NLS-1$

//    private static final int DISPLAY_DURATION = 1000 * 60 * 5;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^([a-zA-Z0-9_\\-\\.\\+]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$"); //$NON-NLS-1$

    private static IDialogSettings SETTINGS = null;

    private IWorkbench workbench;

    private XMindNetRequest subscribeRequest = null;

    public NewsletterSubscriptionReminder(IWorkbench workbench) {
        this.workbench = workbench;
    }

    public void start() {
        IDialogSettings settings = getSettings();
        int launchCount = getInt(settings, KEY_LAUNCH_COUNT, 0);
        if (launchCount >= getSysInt(
                "net.xmind.workbench.newsletter.reminder.silenceCount", 4) //$NON-NLS-1$
                && !settings.getBoolean(KEY_NEVER_REMIND)) {
            show();
            neverRemind();
        }
        if (launchCount < Integer.MAX_VALUE) {
            launchCount++;
            settings.put(KEY_LAUNCH_COUNT, launchCount);
        }
    }

//    private void scheduleReminder() {
//        Thread thread = new Thread(new Runnable() {
//            public void run() {
//                // Wait for workbench actually ready:
//                try {
//                    while (!"workbenchReady".equals(System.getProperty("org.xmind.cathy.app.status"))) { //$NON-NLS-1$ //$NON-NLS-2$
//                        Thread.sleep(100);
//                    }
//                } catch (InterruptedException e) {
//                    return;
//                }
//
//                // Give user 1 second break:
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                }
//
//                // Shows the reminder notification:
//                final Display display = workbench.getDisplay();
//                if (display != null && !display.isDisposed()) {
//                    display.asyncExec(new Runnable() {
//                        public void run() {
//                            showReminder();
//                        }
//                    });
//                }
//            }
//        }, "ShowNewsletterSubscriptionReminder"); //$NON-NLS-1$
//        thread.setPriority(Thread.MIN_PRIORITY);
//        thread.setDaemon(true);
//        thread.start();
//    }

    public synchronized void show() {
        if (workbench == null)
            return;
        final Display display = workbench.getDisplay();
        if (display != null && !display.isDisposed()) {
            display.syncExec(new Runnable() {
                public void run() {
                    doShow();
                }
            });
        }
    }

    private void doShow() {
        IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
        if (window == null)
            return;

        final Shell shell = window.getShell();
        if (shell == null || shell.isDisposed())
            return;

        NewsletterSubscriptionDialog dialog = new NewsletterSubscriptionDialog(
                shell, new IInputValidator() {
                    public String isValid(String newText) {
                        if (newText == null || "".equals(newText) //$NON-NLS-1$
                                || !EMAIL_PATTERN.matcher(newText).matches())
                            return ""; //$NON-NLS-1$
                        return null;
                    }
                });

        if (dialog.open() != InputDialog.OK)
            return;

        final String email = dialog.getValue();

        Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    subscribeRequest = new XMindNetRequest(true);
                    subscribeRequest.debug();
                    subscribeRequest.path("/_res/newsletter/subscribe"); //$NON-NLS-1$
                    subscribeRequest.addParameter("email", email); //$NON-NLS-1$
                    subscribeRequest.addParameter("source", "xmind_3.4.1"); //$NON-NLS-1$ //$NON-NLS-2$
                    subscribeRequest.post();
                } catch (OperationCanceledException e) {

                } catch (Throwable e) {
                    XMindNetWorkbench.log(e,
                            "Failed to subscribe to XMind newsletter: " //$NON-NLS-1$
                                    + email);
                } finally {
                    subscribeRequest = null;
                }
            }
        }, "SubscribeXMindNewsletter"); //$NON-NLS-1$
        thread.setDaemon(true);
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();

//        IAction action = new Action() {
//            public void run() {
//                openSubscriptionForm(shell);
//            }
//        };
//        action.setText(Messages.NewsletterSubscriptionReminder_message);
//        new NotificationWindow(shell, null, action, null, DISPLAY_DURATION)
//                .open();
//        neverRemind();
    }

//    private void openSubscriptionForm(Shell shell) {
//        XMindNet.gotoURL(XMindNetWorkbench.URL_SUBSCRIBE_NEWSLETTER);
//    }

    private static int getSysInt(String key, int defaultValue) {
        String value = System.getProperty(key);
        if (value != null) {
            try {
                return Integer.parseInt(value, 10);
            } catch (Throwable e) {
            }
        }
        return defaultValue;
    }

    private static int getInt(IDialogSettings settings, String key,
            int defaultValue) {
        try {
            return settings.getInt(key);
        } catch (Throwable e) {
            return defaultValue;
        }
    }

    private static IDialogSettings getSettings() {
        if (SETTINGS == null) {
            IDialogSettings mainSettings = XMindNetWorkbench.getDefault()
                    .getDialogSettings();
            SETTINGS = mainSettings.getSection(SETTINGS_SECTION);
            if (SETTINGS == null) {
                SETTINGS = mainSettings.addNewSection(SETTINGS_SECTION);
            }
        }
        return SETTINGS;
    }

    public void stop() {
        if (subscribeRequest != null) {
            subscribeRequest.abort();
            subscribeRequest = null;
        }
    }

    public static void neverRemind() {
        getSettings().put(KEY_NEVER_REMIND, true);
    }

}
