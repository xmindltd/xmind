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
package org.xmind.ui.internal.browser;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.xmind.core.net.util.LinkUtils;

public class BrowserUtil {

    private static final String BROWSER_PACKAGE_NAME = "org.eclipse.swt.browser.Browser"; //$NON-NLS-1$

    private static Boolean isInternalBrowserOperational = null;

    private BrowserUtil() {
    }

    public static Object getWindowKey(IWorkbenchWindow window) {
        return new Integer(window.hashCode());
    }

    public static boolean gotoUrl(String url) {
        if (url != null && !BrowserUtil.isWindows()) {
            int index = url.indexOf(" "); //$NON-NLS-1$
            while (index >= 0) {
                url = url.substring(0, index) + "%20" //$NON-NLS-1$
                        + url.substring(index + 1);
                index = url.indexOf(" "); //$NON-NLS-1$
            }
        }

        boolean success = false;
        try {
            success = Program.launch(url);
        } catch (Throwable th) {
            Program program = Program.findProgram("html"); //$NON-NLS-1$
            if (program == null) {
                program = Program.findProgram("htm"); //$NON-NLS-1$
            }
            try {
                if (program != null)
                    success = program.execute(url);
            } catch (Throwable t) {
            }
        }
        return success;
    }

    /**
     * Returns true if we're running on Windows.
     * 
     * @return boolean
     */
    public static boolean isWindows() {
        String os = System.getProperty("os.name"); //$NON-NLS-1$
        if (os != null && os.toLowerCase().indexOf("win") >= 0) //$NON-NLS-1$
            return true;
        return false;
    }

    public static boolean canUseInternalWebBrowser() {
        // if we have already figured this out, don't do it again.
        if (isInternalBrowserOperational != null)
            return isInternalBrowserOperational.booleanValue();

        // check for the class
        try {
            Class.forName(BROWSER_PACKAGE_NAME);
        } catch (ClassNotFoundException e) {
            isInternalBrowserOperational = new Boolean(false);
            return false;
        }

        // try loading it
        Shell shell = null;
        try {
            shell = new Shell(PlatformUI.getWorkbench().getDisplay());
            new Browser(shell, SWT.NONE);
            isInternalBrowserOperational = new Boolean(true);
            return true;
        } catch (Throwable t) {
            BrowserPlugin.getDefault().getLog()
                    .log(new Status(IStatus.WARNING, BrowserPlugin.PLUGIN_ID, 0,
                            "Internal browser is not available: " //$NON-NLS-1$
                                    + t.getMessage(),
                            null));
            isInternalBrowserOperational = new Boolean(false);
            return false;
        } finally {
            if (shell != null)
                shell.dispose();
        }
    }

    public static boolean canUseSystemBrowser() {
        // Disabling system browser on Solaris due to bug 94497
        if (Platform.OS_SOLARIS.equals(Platform.getOS()))
            return false;
        return true; //Program.findProgram("html") != null;
    }

    public static String encodeStyle(String clientId, int style) {
        return clientId + "-" + style; //$NON-NLS-1$
    }

    public static int decodeStyle(String id) {
        return Integer.parseInt(id.substring(id.lastIndexOf('-') + 1));
    }

    public static String decodeClientId(String id) {
        return id.substring(0, id.lastIndexOf('-'));
    }

    public static String normalizeURL(String url) {
        if (url == null || "".equals(url)) //$NON-NLS-1$
            return url;

        if (!url.startsWith("http:") && !url.startsWith("https:")) //$NON-NLS-1$ //$NON-NLS-2$
            return url;

        URI uri;
        try {
            uri = new URI(url);
            url = uri.toString();
        } catch (Exception ignore) {
            uri = null;
        }

        if (uri != null) {
            String host = uri.getHost();
            if (host != null && (host.endsWith(".xmind.net") //$NON-NLS-1$
                    || host.endsWith(".xmind.cn")))  //$NON-NLS-1$
                /// make our server decide where exactly this url should go
                return makeRedirectURL(url);
        }

        return url;
    }

    public static String makeRedirectURL(String url) {
        StringBuffer buffer = new StringBuffer(100);

        boolean isCnLink = url.contains(LinkUtils.HOST_CN)
                && url.indexOf(LinkUtils.HOST_CN) < LinkUtils.HOST_NET.length();
        buffer.append(
                LinkUtils.getLinkByUser(isCnLink, true, true, "/xmind/go?r=")); //$NON-NLS-1$

        buffer.append(encode(url));
        buffer.append("&u="); //$NON-NLS-1$
        String user = System.getProperty("net.xmind.signin.account.user"); //$NON-NLS-1$
        if (user != null) {
            buffer.append(encode(user));
        }
        buffer.append("&t="); //$NON-NLS-1$
        String token = System.getProperty("net.xmind.signin.account.token"); //$NON-NLS-1$
        if (token != null) {
            buffer.append(encode(token));
            buffer.append("&exp="); //$NON-NLS-1$
            buffer.append(encode(System
                    .getProperty("net.xmind.signin.account.expireDate", ""))); //$NON-NLS-1$ //$NON-NLS-2$
        }
        String distributionId = System
                .getProperty("org.xmind.product.distribution.id"); //$NON-NLS-1$
        if (distributionId != null) {
            buffer.append("&distrib="); //$NON-NLS-1$
            buffer.append(encode(distributionId));
        }
        buffer.append("&nl="); //$NON-NLS-1$
        buffer.append(encode(Platform.getNL()));
        buffer.append("&os="); //$NON-NLS-1$
        buffer.append(encode(Platform.getOS()));
        buffer.append("&arch="); //$NON-NLS-1$
        buffer.append(encode(Platform.getOSArch()));
        buffer.append("&app="); //$NON-NLS-1$
        buffer.append(encode(Platform.getProduct().getApplication()));

        return buffer.toString();
    }

    private static String encode(String text) {
        try {
            return URLEncoder.encode(text, "UTF-8"); //$NON-NLS-1$
        } catch (UnsupportedEncodingException e) {
            return text;
        }
    }

}
