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
package org.xmind.ui.internal.protocols;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.internal.registry.RegistryReader;
import org.xmind.core.util.HyperlinkUtils;
import org.xmind.ui.browser.BrowserSupport;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.browser.BrowserUtil;
import org.xmind.ui.internal.mindmap.TopicContext;
import org.xmind.ui.mindmap.IHyperlinked;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.IProtocol;
import org.xmind.ui.mindmap.IProtocolDescriptor;
import org.xmind.ui.mindmap.IProtocolManager;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.Logger;

public class ProtocolManager extends RegistryReader
        implements IProtocolManager {

    private static String DEFAULT_BROWSER_ID = "org.xmind.ui.defaultProtocol.browser"; //$NON-NLS-1$

    private static class DefaultOpenURLAction extends Action
            implements IHyperlinked {
        private String url;

        public DefaultOpenURLAction(String url) {
            super(MindMapMessages.OpenHyperlink_text,
                    MindMapUI.getImages().get(IMindMapImages.HYPERLINK, true));
            this.url = url;
            setDisabledImageDescriptor(
                    MindMapUI.getImages().get(IMindMapImages.HYPERLINK, false));
            setToolTipText(url);
        }

        public void run() {
            SafeRunner.run(new SafeRunnable() {
                public void run() throws Exception {
                    String theURL = url;
                    try {
                        URI uri = new URI(theURL);
                        String host = uri.getHost();
                        if (host != null && (host.endsWith(".xmind.net") //$NON-NLS-1$
                                || host.endsWith(".xmind.cn"))) {  //$NON-NLS-1$
                            theURL = BrowserUtil.makeRedirectURL(theURL);
                        }
                    } catch (Exception ignored) {
                    }
                    BrowserSupport.getInstance()
                            .createBrowser(DEFAULT_BROWSER_ID).openURL(theURL);
                }
            });
        }

        @Override
        public String getHyperlink() {
            return url;
        }
    }

    protected static class DefaultProtocol implements IProtocol {

        public String getProtocolName() {
            return null;
        }

        public IAction createOpenHyperlinkAction(Object source,
                final String uri) {
            return new DefaultOpenURLAction(uri);
        }

        public boolean isHyperlinkModifiable(Object source, String uri) {
            return true;
        }
    }

    private IProtocol defaultProtocol = null;

    private List<ProtocolDescriptor> protocolDescriptors = null;

    private Map<String, IProtocol> protocols = null;

    public ProtocolManager() {
    }

    private List<ProtocolDescriptor> getProtocolDescriptors() {
        ensureLoaded();
        return protocolDescriptors;
    }

    private void ensureLoaded() {
        if (protocolDescriptors != null)
            return;

        lazyLoad();
        if (protocolDescriptors == null)
            protocolDescriptors = Collections.emptyList();
    }

    private void lazyLoad() {
        readRegistry(Platform.getExtensionRegistry(), MindMapUI.PLUGIN_ID,
                "protocols"); //$NON-NLS-1$
    }

    protected boolean readElement(IConfigurationElement element) {
        String name = element.getName();
        if ("protocol".equals(name)) { //$NON-NLS-1$
            readProtocolDescriptor(element);
            return true;
        }
        return false;
    }

    private void readProtocolDescriptor(IConfigurationElement element) {
        try {
            registerProtocolDescriptor(new ProtocolDescriptor(element));
        } catch (CoreException e) {
            Logger.log(e);
        }
    }

    private void registerProtocolDescriptor(
            ProtocolDescriptor protocolDescriptor) {
        if (protocolDescriptors == null)
            protocolDescriptors = new ArrayList<ProtocolDescriptor>();
        protocolDescriptors.add(protocolDescriptor);
    }

    private IProtocol getDefaultProtocol() {
        if (defaultProtocol == null)
            defaultProtocol = createDefaultProtocol();
        return defaultProtocol;
    }

    private IProtocol createDefaultProtocol() {
        return new DefaultProtocol();
    }

    public IProtocolDescriptor findProtocolDescriptor(String uri) {
        String name = HyperlinkUtils.getProtocolName(uri);

        if (name != null) {
            for (ProtocolDescriptor p : getProtocolDescriptors()) {
                if (p.hasProtocolName(name)) {
                    return p;
                }
            }
        }
        return null;
    }

    public IProtocol findProtocol(String uri) {
        return getProtocol(findProtocolDescriptor(uri));
    }

    private IProtocol getProtocol(IProtocolDescriptor desc) {
        if (desc != null) {
            IProtocol p = getCreatedProtocol((ProtocolDescriptor) desc);
            if (p != null)
                return p;
        }
        return getDefaultProtocol();
    }

    private IProtocol getCreatedProtocol(IProtocolDescriptor desc) {
        if (protocols == null)
            protocols = new HashMap<String, IProtocol>();
        String id = desc.getId();
        IProtocol protocol = getExistingProtocol(id);
        if (protocol == null) {
            protocol = createProtocol((ProtocolDescriptor) desc);
            registerProtocol(id, protocol);
        }
        return protocol;
    }

    private IProtocol registerProtocol(String id, IProtocol protocol) {
        return protocols.put(id, protocol);
    }

    private IProtocol getExistingProtocol(String id) {
        return protocols.get(id);
    }

    private IProtocol createProtocol(ProtocolDescriptor p) {
        try {
            return p.createProtocol();
        } catch (CoreException e) {
            Logger.log(e, "Failed to create protocol: " + p.getId()); //$NON-NLS-1$
        }
        return getDefaultProtocol();
    }

    public IAction createOpenHyperlinkAction(Object context, String uri) {
        if (context instanceof ITopicPart) {
            context = new TopicContext((ITopicPart) context);
        }
        IProtocolDescriptor desc = findProtocolDescriptor(uri);
        return adaptAction(
                getProtocol(desc).createOpenHyperlinkAction(context, uri), desc,
                uri);
    }

    private IAction adaptAction(IAction action, IProtocolDescriptor desc,
            String uri) {
        if (action == null)
            return null;

        if (action.getText() == null) {
            String text;
            if (desc == null) {
                text = MindMapMessages.OpenHyperlink_text;
            } else {
                text = NLS.bind(
                        MindMapMessages.ProtocolManager_OpenHyperlink_pattern,
                        desc.getName());
            }
            action.setText(text);
        }

        if (action.getImageDescriptor() == null) {
            action.setImageDescriptor(
                    MindMapUI.getImages().get(IMindMapImages.HYPERLINK, true));
        }

        if (action.getToolTipText() == null) {
            action.setToolTipText(uri);
        }
        return action;
    }

    public boolean isHyperlinkModifiable(Object context, String uri) {
        if (context instanceof ITopicPart) {
            context = new TopicContext((ITopicPart) context);
        }
        return findProtocol(uri).isHyperlinkModifiable(context, uri);
    }

}
