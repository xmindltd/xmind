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
package org.xmind.ui.internal.editor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.internal.registry.RegistryReader;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.mindmap.IMindMapEditorConfigurer;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.Logger;

public class MindMapEditorConfigurerManager extends RegistryReader {

    private static final MindMapEditorConfigurerManager instance = new MindMapEditorConfigurerManager();

    private Map<String, IMindMapEditorConfigurer> configurers = null;

    private MindMapEditorConfigurerManager() {
    }

    public void configureEditor(MindMapEditor editor) {
        ensureLoaded();
        for (IMindMapEditorConfigurer configurer : configurers.values()) {
            try {
                configurer.configureEditor(editor);
            } catch (Throwable e) {
                Logger.log(e);
            }
        }
    }

    public void configurePage(IGraphicalEditorPage page) {
        ensureLoaded();
        for (IMindMapEditorConfigurer configurer : configurers.values()) {
            try {
                configurer.configureEditorPage(page);
            } catch (Throwable e) {
                Logger.log(e);
            }
        }
    }

    private void ensureLoaded() {
        if (configurers != null)
            return;

        lazyLoad();
        if (configurers == null)
            configurers = Collections.emptyMap();
    }

    private void lazyLoad() {
        readRegistry(Platform.getExtensionRegistry(), MindMapUI.PLUGIN_ID,
                "mindMapEditorConfigurer"); //$NON-NLS-1$
    }

    protected boolean readElement(IConfigurationElement element) {
        String name = element.getName();
        if ("configurer".equals(name)) { //$NON-NLS-1$
            readConfigurer(element);
            return true;
        }
        return false;
    }

    private void readConfigurer(IConfigurationElement element) {
        String id = element.getAttribute(IWorkbenchRegistryConstants.ATT_ID);
        if (id == null) {
            return;
        }
        try {
            addConfigurer(id, createConfigurer(element));
        } catch (CoreException e) {
            Logger.log(e);
        }
    }

    private void addConfigurer(String id, IMindMapEditorConfigurer configurer) {
        if (configurers == null)
            configurers = new HashMap<String, IMindMapEditorConfigurer>();
        configurers.put(id, configurer);
    }

    private IMindMapEditorConfigurer createConfigurer(
            IConfigurationElement element) throws CoreException {
        String clazz = element
                .getAttribute(IWorkbenchRegistryConstants.ATT_CLASS);
        if (clazz == null)
            throw new CoreException(new Status(IStatus.ERROR, element
                    .getNamespaceIdentifier(),
                    "Invalid extension(missing class attribute)")); //$NON-NLS-1$
        return (IMindMapEditorConfigurer) element
                .createExecutableExtension(IWorkbenchRegistryConstants.ATT_CLASS);
    }

    public static MindMapEditorConfigurerManager getInstance() {
        return instance;
    }

}