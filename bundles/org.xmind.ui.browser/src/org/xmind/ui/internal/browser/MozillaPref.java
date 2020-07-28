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
import java.util.Map;

import org.eclipse.core.net.proxy.IProxyChangeEvent;
import org.eclipse.core.net.proxy.IProxyChangeListener;
import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.ui.PlatformUI;

/**
 * @author briansun
 */
public class MozillaPref {

    private Browser browser;

    private IProxyService service;

    private Map<String, Object> preference = null;

    private IProxyChangeListener proxyChangeListener = new IProxyChangeListener() {
        public void proxyInfoChanged(IProxyChangeEvent event) {
            addProxyPref();
            setPref();
        }
    };

    /**
     * @param viewer
     */
    public MozillaPref(BrowserViewer viewer) {
        super();
        this.browser = viewer.getBrowser();
        if (viewer.isMozilla())
            initPref();
    }

    /**
     * 
     */
    private void initPref() {
        if (!PlatformUI.isWorkbenchRunning())
            return;

        service = (IProxyService) PlatformUI.getWorkbench().getService(
                IProxyService.class);//Activator.getProxyService();
        if (service == null)
            return;

        preference = new HashMap<String, Object>();
        preference.put("security.warn_entering_secure", false); //$NON-NLS-1$
        preference.put("security.warn_leaving_secure", false); //$NON-NLS-1$
        addProxyPref();
        setPref();

        service.addProxyChangeListener(proxyChangeListener);
        browser.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                if (service != null) {
                    service.removeProxyChangeListener(proxyChangeListener);
                    service = null;
                }
            }
        });
    }

    /**
     * 
     */
    private void addProxyPref() {
        IProxyData httpData = service.getProxyData(IProxyData.HTTP_PROXY_TYPE);
        preference.put("network.proxy.http", getHost(httpData)); //$NON-NLS-1$
        preference.put("network.proxy.http_port", getPort(httpData)); //$NON-NLS-1$

        IProxyData socksData = service
                .getProxyData(IProxyData.SOCKS_PROXY_TYPE);
        preference.put("network.proxy.socks", getHost(socksData)); //$NON-NLS-1$
        preference.put("network.proxy.socks_port", getPort(socksData)); //$NON-NLS-1$

        IProxyData sslData = service.getProxyData(IProxyData.HTTPS_PROXY_TYPE);
        preference.put("network.proxy.ssl", getHost(sslData)); //$NON-NLS-1$
        preference.put("network.proxy.ssl_port", getPort(sslData)); //$NON-NLS-1$

        String nonProxy = null;
        StringBuilder sb = new StringBuilder();
        for (String host : service.getNonProxiedHosts()) {
            sb.append(host);
            sb.append(", "); //$NON-NLS-1$
        }
        nonProxy = sb.length() > 2 ? sb.substring(0, sb.length() - 2) : sb
                .toString();
        preference.put("network.proxy.no_proxies_on", nonProxy); //$NON-NLS-1$

        int enabled = service.isProxiesEnabled() ? 1 : 0;
        preference.put("network.proxy.type", enabled); //$NON-NLS-1$
    }

    /**
     * 
     */
    private void setPref() {
        StringBuilder sb = new StringBuilder();
        String s = "javascript:var prefService = Components.classes['@mozilla.org/preferences-service;1'].getService(Components.interfaces.nsIPrefService);"; //$NON-NLS-1$
        sb.append(s);
        for (Map.Entry<String, Object> e : preference.entrySet()) {
            if (e.getValue() instanceof String) {
                String s2 = "prefService.setCharPref('%s', '%s');"; //$NON-NLS-1$
                sb.append(String.format(s2, e.getKey(), e.getValue()));
            } else if (e.getValue() instanceof Integer) {
                String s3 = "prefService.setIntPref('%s', %s);"; //$NON-NLS-1$
                sb.append(String.format(s3, e.getKey(), e.getValue()));
            } else if (e.getValue() instanceof Boolean) {
                String s4 = "prefService.setBoolPref('%s', %s);"; //$NON-NLS-1$
                sb.append(String.format(s4, e.getKey(), e.getValue()));
            }
        }
        if (browser != null && !browser.isDisposed())
            browser.setUrl(sb.toString());
    }

    private String getHost(IProxyData pd) {
        return pd.getHost() == null ? "" : pd.getHost(); //$NON-NLS-1$
    }

    private int getPort(IProxyData pd) {
        return pd.getPort() == -1 ? 0 : pd.getPort();
    }

}