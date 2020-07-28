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
package org.xmind.ui.internal;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.internal.registry.RegistryReader;
import org.xmind.ui.mindmap.INumberFormat;
import org.xmind.ui.mindmap.INumberFormatDescriptor;
import org.xmind.ui.mindmap.INumberFormatManager;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.Logger;

public class NumberFormatExtensionManager extends RegistryReader
        implements INumberFormatManager {

    private static class NumberFormatProxy
            implements INumberFormat, INumberFormatDescriptor {

        private IConfigurationElement element;

        private String id;

        private String name;

        private String description;

        private INumberFormat implementation;

        private boolean failedInitImplementation = false;

        public NumberFormatProxy(IConfigurationElement element)
                throws CoreException {
            this.element = element;
            this.id = element.getAttribute(RegistryConstants.ATT_ID);
            this.name = element.getAttribute(RegistryConstants.ATT_NAME);
            this.description = element
                    .getAttribute(RegistryConstants.ATT_DESCRIPTION);
            if (getClassValue(element, RegistryConstants.ATT_CLASS) == null)
                throw new CoreException(new Status(IStatus.ERROR,
                        element.getNamespaceIdentifier(), 0,
                        "Invalid extension (missing class name): " + id, //$NON-NLS-1$
                        null));
        }

        private INumberFormat getImplementation() {
            if (implementation == null && !failedInitImplementation) {
                try {
                    implementation = (INumberFormat) element
                            .createExecutableExtension(
                                    RegistryConstants.ATT_CLASS);
                } catch (CoreException e) {
                    Logger.log(e,
                            "Failed to create number format from class: " //$NON-NLS-1$
                                    + getClassValue(element,
                                            RegistryConstants.ATT_CLASS));
                    failedInitImplementation = true;
                }
            }
            return implementation;
        }

        public String getText(int index) {
            INumberFormat impl = getImplementation();
            if (impl != null)
                return impl.getText(index);
            return null;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

    }

    private static final String LANGUAGE_OSGI_NL_KEY = "osgi.nl"; //$NON-NLS-1$

    private static final String SIMPLECHINESEFORMAT = "org.xmind.numbering.simplechinese"; //$NON-NLS-1$

    private static final String TRADITIONALCHINESEFORMAT = "org.xmind.numbering.traditionalchinese"; //$NON-NLS-1$

    private Map<String, NumberFormatProxy> formats = null;

    private List<INumberFormatDescriptor> list = null;

    private Properties configIniProperties;

    /* package */ NumberFormatExtensionManager() {
    }

    protected boolean readElement(IConfigurationElement element) {
        String name = element.getName();
        if (RegistryConstants.TAG_FORMAT.equals(name)) {
            readFormat(element);
            return true;
        }
        return false;
    }

    private void readFormat(IConfigurationElement element) {
        NumberFormatProxy proxy;
        try {
            proxy = new NumberFormatProxy(element);
        } catch (CoreException e) {
            Logger.log(e, "Failed to load numbering format: " + element); //$NON-NLS-1$
            return;
        }
        String id = proxy.getId();
        if (configIniProperties == null)
            configIniProperties = loadProperties(getConfigFile());
        /// "zh_CN", "zh_TW"
        if (SIMPLECHINESEFORMAT.equals(id) && !"zh_CN" //$NON-NLS-1$
                .equals(configIniProperties.getProperty(LANGUAGE_OSGI_NL_KEY)))
            return;

        if (TRADITIONALCHINESEFORMAT.equals(id) && !"zh_TW" //$NON-NLS-1$
                .equals(configIniProperties.getProperty(LANGUAGE_OSGI_NL_KEY)))
            return;

        if (formats == null)
            formats = new HashMap<String, NumberFormatProxy>();
        formats.put(proxy.getId(), proxy);
        if (list == null)
            list = new ArrayList<INumberFormatDescriptor>();
        list.add(proxy);
    }

    private void ensureLoaded() {
        if (formats != null && list != null)
            return;
        lazyLoad();
        if (formats == null)
            formats = Collections.emptyMap();
        if (list == null)
            list = Collections.emptyList();
    }

    private void lazyLoad() {
        readRegistry(Platform.getExtensionRegistry(), MindMapUI.PLUGIN_ID,
                RegistryConstants.EXT_NUMBER_FORMATS);
    }

    public INumberFormatDescriptor getDescriptor(String formatId) {
        ensureLoaded();
        return formats.get(formatId);
    }

    public List<INumberFormatDescriptor> getDescriptors() {
        ensureLoaded();
        return list;
    }

    public INumberFormat getFormat(String formatId) {
        ensureLoaded();
        return formats.get(formatId);
    }

    public String getNumberText(String formatId, int index) {
        INumberFormat format = getFormat(formatId);
        if (format != null)
            return format.getText(index);
        return null;
    }

    private Properties loadProperties(File file) {
        if (file != null && file.exists() && file.canRead()) {
            try {
                InputStream stream = new BufferedInputStream(
                        new FileInputStream(file), 1024);
                try {
                    Properties properties = new Properties();
                    properties.load(stream);
                    return properties;
                } finally {
                    try {
                        stream.close();
                    } catch (IOException e) {
                    }
                }
            } catch (IOException e) {
            }
        }
        return null;
    }

    private File getConfigFile() {
        URL configDir = Platform.getConfigurationLocation().getURL();
        try {
            URL configIni = new URL(configDir, "config.ini"); //$NON-NLS-1$
            File file = new File(configIni.getFile());
            return file;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

}