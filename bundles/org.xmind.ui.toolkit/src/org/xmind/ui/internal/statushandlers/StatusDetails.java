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
package org.xmind.ui.internal.statushandlers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLEncoder;
import java.util.Random;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.statushandlers.IStatusAdapterConstants;
import org.eclipse.ui.statushandlers.StatusAdapter;

public class StatusDetails {

    private final StatusAdapter statusAdapter;

    private String detailsText = null;

    public StatusDetails(StatusAdapter statusAdapter) {
        this.statusAdapter = statusAdapter;
    }

    public StatusAdapter getStatusAdapter() {
        return statusAdapter;
    }

    public Image getImage() {
        int severity = statusAdapter.getStatus().getSeverity();
        int iconId;
        if ((severity & IStatus.ERROR) != 0) {
            iconId = SWT.ICON_ERROR;
        } else if ((severity & IStatus.WARNING) != 0) {
            iconId = SWT.ICON_WARNING;
        } else {
            iconId = SWT.ICON_INFORMATION;
        }
        return Display.getCurrent().getSystemImage(iconId);
    }

    public String getMessage() {
        IStatus status = statusAdapter.getStatus();
        String message = status.getMessage();
        if (message != null && !"".equals(message)) //$NON-NLS-1$
            return message;
        Throwable exception = getRootCause(status.getException());
        if (exception != null)
            return NLS
                    .bind(StatusHandlerMessages.StatusDetails_ErrorMessage_with_RootCauseClassName_and_RootCauseMessage,
                            exception.getClass().getName(),
                            exception.getLocalizedMessage());
        return StatusHandlerMessages.StatusDetails_SimpleErrorMessage;
    }

    public String getFullText() {
        if (detailsText == null) {
            detailsText = buildDetailsText(statusAdapter);
        }
        return detailsText;
    }

    public String buildMailingURL() {
        String caseId = generateCaseId();
        String subject = String.format(
                "XMind Runtime Error Report (Case ID: %s)", //$NON-NLS-1$
                caseId);
        return String.format(
                "mailto:support@xmind.net?subject=%s&body=%s", //$NON-NLS-1$
                urlEncode(subject),
                urlEncode(System.getProperty("line.separator") //$NON-NLS-1$
                        + getFullText()));
    }

    private static String buildDetailsText(StatusAdapter statusAdapter) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(out);

        IStatus status = statusAdapter.getStatus();
        String message = status.getMessage();
        Throwable exception = status.getException();
        Long timestamp = (Long) statusAdapter
                .getProperty(IStatusAdapterConstants.TIMESTAMP_PROPERTY);

        if (message != null) {
            ps.println(message);
            ps.println();
        }

        if (exception != null) {
            exception.printStackTrace(ps);
        }

        ps.println();

        ps.printf("Severity: %s", toSeverityString(status.getSeverity())); //$NON-NLS-1$
        ps.println();
        ps.printf("Plug-in ID: %s", status.getPlugin()); //$NON-NLS-1$
        ps.println();
        ps.printf("Code: %s", status.getCode()); //$NON-NLS-1$
        ps.println();

        ps.println();
        ps.println("== Environment =="); //$NON-NLS-1$
        ps.printf("Time: %1$tF %1$tT", //$NON-NLS-1$ 
                timestamp);
        ps.println();

        String application = System.getProperty("eclipse.application"); //$NON-NLS-1$
        if (application == null || "".equals(application)) { //$NON-NLS-1$
            IProduct product = Platform.getProduct();
            if (product != null) {
                application = product.getApplication();
            }
        }
        if ("org.xmind.cathy.application".equals(application)) { //$NON-NLS-1$
            printp(ps, "XMind Distribution Pack: %s", //$NON-NLS-1$
                    "org.xmind.product.distribution.id"); //$NON-NLS-1$
        } else {
            ps.println("XMind Installed as Plugins"); //$NON-NLS-1$
        }
        printp(ps, "XMind Build ID: %s", "org.xmind.product.buildid"); //$NON-NLS-1$ //$NON-NLS-2$
        printp(ps, "Operating System: %s %s (%s)", //$NON-NLS-1$ 
                "os.name", "os.version", "os.arch"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        printp(ps, "Platform: %s.%s.%s", //$NON-NLS-1$
                "osgi.os", "osgi.ws", "osgi.arch"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        printp(ps, "Language: %s", "user.language"); //$NON-NLS-1$ //$NON-NLS-2$
        printp(ps, "Country: %s", "user.country"); //$NON-NLS-1$ //$NON-NLS-2$
        ps.println();

        ps.println("== Product =="); //$NON-NLS-1$
        printp(ps, "Product ID: %s", "eclipse.product"); //$NON-NLS-1$ //$NON-NLS-2$
        printp(ps, "Application ID: %s", "eclipse.application"); //$NON-NLS-1$ //$NON-NLS-2$
        printp(ps, "Launcher: %s", "eclipse.launcher.name"); //$NON-NLS-1$ //$NON-NLS-2$
        ps.println();
        printp(ps, "Command Line Arguments: %s", "eclipse.commands"); //$NON-NLS-1$ //$NON-NLS-2$
        printp(ps, "VM Arguments: %s", "eclipse.vmargs"); //$NON-NLS-1$ //$NON-NLS-2$
        ps.println();

        ps.println("== Java Properties =="); //$NON-NLS-1$
        printp(ps, "Java Version: %s", "java.version"); //$NON-NLS-1$ //$NON-NLS-2$
        printp(ps, "Java Vendor: %s", "java.vendor"); //$NON-NLS-1$ //$NON-NLS-2$
        printp(ps, "Java Runtime: %s", "java.runtime.name"); //$NON-NLS-1$ //$NON-NLS-2$
        printp(ps, "    Version: %s", "java.runtime.version"); //$NON-NLS-1$ //$NON-NLS-2$
        printp(ps, "Java VM: %s", "java.vm.name"); //$NON-NLS-1$ //$NON-NLS-2$
        printp(ps, "    Version: %s", "java.vm.version"); //$NON-NLS-1$ //$NON-NLS-2$
        printp(ps, "    Vendor: %s", "java.vm.vendor"); //$NON-NLS-1$ //$NON-NLS-2$
        printp(ps, "    Info: %s", "java.vm.info"); //$NON-NLS-1$ //$NON-NLS-2$

        ps.close();
        try {
            out.close();
        } catch (IOException e) {
        }

        // Replace user name to protect privacy:
        String text = out.toString();
        String userName = System.getProperty("user.name"); //$NON-NLS-1$
        if (userName != null && !"".equals(userName.trim())) { //$NON-NLS-1$
            text = text.replace(userName, "USERNAME"); //$NON-NLS-1$
        }
        return text;
    }

    private static void printp(PrintStream ps, String format, String... keys) {
        Object[] values = new Object[keys.length];
        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];
            String value = getSysProp(key);
            if (value.indexOf("\n") >= 0 || value.indexOf("\r") >= 0) { //$NON-NLS-1$ //$NON-NLS-2$
                value = value.replaceAll("\r\n|\r|\n", //$NON-NLS-1$
                        System.getProperty("line.separator") + " "); //$NON-NLS-1$ //$NON-NLS-2$
            }
            values[i] = value;
        }
        ps.printf(format, values);
        ps.println();
    }

    private static String getSysProp(String key) {
        String value = System.getProperty(key);
        return value == null ? "" : value; //$NON-NLS-1$
    }

    private static String urlEncode(String string) {
        try {
            return URLEncoder.encode(string, "UTF-8") //$NON-NLS-1$
                    .replace("+", "%20"); //$NON-NLS-1$ //$NON-NLS-2$
        } catch (UnsupportedEncodingException e) {
            return string;
        }
    }

    private static String generateCaseId() {
        return Integer.toString(new Random().nextInt(Integer.MAX_VALUE), 36)
                .toUpperCase();
    }

    private static String toSeverityString(int s) {
        if ((s & IStatus.ERROR) != 0)
            return "error"; //$NON-NLS-1$
        if ((s & IStatus.WARNING) != 0)
            return "warning"; //$NON-NLS-1$
        if ((s & IStatus.INFO) != 0)
            return "info"; //$NON-NLS-1$
        if ((s & IStatus.CANCEL) != 0)
            return "cancel"; //$NON-NLS-1$
        return "ok"; //$NON-NLS-1$
    }

    public static Throwable getRootCause(Throwable exception) {
        if (exception == null)
            return null;
        if (exception instanceof InvocationTargetException)
            return getRootCause(((InvocationTargetException) exception)
                    .getCause());
        if (exception instanceof CoreException) {
            Throwable cause = ((CoreException) exception).getStatus()
                    .getException();
            if (cause != null)
                return getRootCause(cause);
        }
        return exception;
    }

}
