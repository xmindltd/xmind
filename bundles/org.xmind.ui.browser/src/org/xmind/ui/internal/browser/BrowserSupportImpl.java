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

import java.util.HashMap;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.xmind.ui.browser.IBrowser;
import org.xmind.ui.browser.IBrowserSupport;

public class BrowserSupportImpl implements IBrowserSupport {

    private static class InternalMap extends HashMap<Object, IBrowser> {

        /**
         * 
         */
        private static final long serialVersionUID = -6399662915430504209L;

        public InternalMap() {
            super(4);
        }

    }

    private static final String DEFAULT_CLIENT_ID = "org.xmind.ui.defaultBrowser"; //$NON-NLS-1$

    private HashMap<String, Object> browsers = new HashMap<String, Object>();

    public IBrowser createBrowser() {
        return createBrowser(AS_DEFAULT, DEFAULT_CLIENT_ID, null, null);
    }

    public IBrowser createBrowser(int style) {
        return createBrowser(style, DEFAULT_CLIENT_ID, null, null);
    }

    public IBrowser createBrowser(String browserClientId) {
        return createBrowser(AS_DEFAULT, browserClientId, null, null);
    }

    public IBrowser createBrowser(int style, String browserClientId) {
        return createBrowser(style, browserClientId, null, null);
    }

    public IBrowser createBrowser(int style, String browserClientId,
            String name, String tooltip) {
//        //If OS is linux,force to use external browser as a temporary solution for linux browser bug.
//        if (Platform.OS_LINUX.equals(Platform.getOS())) {
//            style = AS_EXTERNAL;
//        }

        //Force to use external browser
        style = AS_EXTERNAL;

        String browserId = BrowserUtil.encodeStyle(
                browserClientId == null ? DEFAULT_CLIENT_ID : browserClientId,
                style);
        IBrowser existingBrowser = getExistingBrowser(style, browserId);
        if (existingBrowser != null && matchesStyle(existingBrowser, style)) {
            if (existingBrowser instanceof InternalBrowser) {
                ((InternalBrowser) existingBrowser).setName(name);
                ((InternalBrowser) existingBrowser).setTooltip(tooltip);
            }
            return existingBrowser;
        }

        IBrowser newBrowser = doCreateBrowser(style, browserId, name, tooltip);
        registerBrowser(newBrowser);
        return newBrowser;
    }

    private boolean matchesStyle(IBrowser browser, int style) {
        if (isInternal(style))
            return browser instanceof InternalBrowser;
        if (isExternal(style))
            return browser instanceof ExternalBrowser;
        return browser instanceof DefaultBrowser;
    }

    private IBrowser getExistingBrowser(int style, String browserClientId) {
        Object object = browsers.get(browserClientId);
        if (object != null) {
            if (object instanceof IBrowser && isExternal(style))
                return (IBrowser) object;
            if (object instanceof InternalMap && isInternal(style)) {
                InternalMap map = (InternalMap) object;
                IWorkbenchWindow window = PlatformUI.getWorkbench()
                        .getActiveWorkbenchWindow();
                if (window != null) {
                    return map.get(BrowserUtil.getWindowKey(window));
                }
            }
        }
        return null;
    }

    private boolean isInternal(int style) {
        return (style & AS_INTERNAL) != 0 || ((style & IMPL_TYPES) == 0
                && BrowserPref.getBrowserChoice() == BrowserPref.INTERNAL);
    }

    private boolean isExternal(int style) {
        return style == AS_EXTERNAL || ((style & IMPL_TYPES) == 0
                && BrowserPref.getBrowserChoice() == BrowserPref.EXTERNAL);
    }

    private IBrowser doCreateBrowser(int style, String browserClientId,
            String name, String tooltip) {
        /// no longer support internal browsers
        return new ExternalBrowser(browserClientId);
    }

    private void registerBrowser(IBrowser browser) {
        String clientId = browser.getClientId();
        Object object = browsers.get(clientId);
        if (browser instanceof InternalBrowser) {
            Object key = ((InternalBrowser) browser).getWindowKey();
            if (object instanceof InternalMap) {
                ((InternalMap) object).put(key, browser);
            } else {
                InternalMap map = new InternalMap();
                map.put(key, browser);
                object = map;
            }
        } else {
            object = browser;
        }
        browsers.put(clientId, object);
    }

    void removeInternalBrowser(InternalBrowser browser) {
        String id = browser.getClientId();
        Object key = browser.getWindowKey();
        Object object = browsers.get(id);
        if (object instanceof InternalMap) {
            InternalMap map = (InternalMap) object;
            if (map != null) {
                map.remove(key);
                if (map.isEmpty())
                    browsers.remove(id);
            }
        } else {
            browsers.remove(id);
        }
    }

}