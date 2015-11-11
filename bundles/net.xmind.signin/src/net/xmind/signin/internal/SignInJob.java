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
package net.xmind.signin.internal;

import java.util.Date;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.xmind.core.net.IDataStore;

import net.xmind.signin.ISignInDialogExtension;

/**
 * 
 * @author Frank Shaka
 */
public class SignInJob extends Job {

    private String message;

    private ISignInDialogExtension extension;

    private IDataStore data;

    private SignInDialog3 dialog;

    public static final String REMEMBER = "remember"; //$NON-NLS-1$

    /**
     * @param name
     */
    public SignInJob(String message, ISignInDialogExtension extension) {
        super("Sign In to XMind.net"); //$NON-NLS-1$
        this.message = message;
        this.extension = extension;
        setSystem(true);
    }

    /**
     * @return the data
     */
    public IDataStore getData() {
        return data;
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.
     * IProgressMonitor)
     */
    @Override
    protected IStatus run(final IProgressMonitor monitor) {
        if (!PlatformUI.isWorkbenchRunning())
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    "No workbench is running."); //$NON-NLS-1$

        final IWorkbench workbench = PlatformUI.getWorkbench();
        if (workbench == null)
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    "No workbench is available."); //$NON-NLS-1$

        final Display display = workbench.getDisplay();
        if (display == null || display.isDisposed())
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    "No display is available."); //$NON-NLS-1$

        data = null;
        dialog = null;
        display.asyncExec(new Runnable() {
            public void run() {
                IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
                final Shell shell = window == null ? display.getActiveShell()
                        : window.getShell();
                if (shell != null)
                    shell.setActive();

                dialog = new SignInDialog3(shell, message, extension);
                int code = dialog.open();

                if (monitor.isCanceled())
                    return;

                if (code == SignInDialog3.OK) {
                    data = new PropertyStore(dialog.getData());
//                    if (dialog.shouldRemember()) {
                    setCookies(dialog.getUserID(), dialog.getToken());
//                    }
                } else {
                    data = IDataStore.EMPTY;
                }
            }
        });

        // block job thread
        while (data == null && !monitor.isCanceled()) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                break;
            }
        }

        if (monitor.isCanceled()) {
            if (dialog != null) {
                display.asyncExec(new Runnable() {
                    public void run() {
                        dialog.close();
                    }
                });
            }
            return Status.CANCEL_STATUS;
        }

        return Status.OK_STATUS;
    }

    private static void setCookies(String userId, String token) {
        setCookie("U", userId); //$NON-NLS-1$
        setCookie("T", token); //$NON-NLS-1$
    }

    @SuppressWarnings("deprecation")
    private static void setCookie(String name, String value) {
        StringBuffer buffer = new StringBuffer(100);
        buffer.append(name);
        buffer.append('=');
        buffer.append(value);
        buffer.append("; expires="); //$NON-NLS-1$
        if ("".equals(value)) { //$NON-NLS-1$
            buffer.append("Thu, 01-Jan-1970 00:00:01 GMT"); //$NON-NLS-1$
        } else {
            buffer.append(new Date(System.currentTimeMillis()
                    + XMindNetAuthenticator.TOKEN_LIFE_TIME).toGMTString());
        }
        buffer.append("; domain=.xmind.net; path=/;"); //$NON-NLS-1$
        //Browser.setCookie(buffer.toString(), "www.xmind.net"); //$NON-NLS-1$
    }
}
