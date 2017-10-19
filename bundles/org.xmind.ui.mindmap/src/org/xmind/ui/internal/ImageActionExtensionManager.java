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
package org.xmind.ui.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.internal.registry.RegistryReader;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.Logger;

public class ImageActionExtensionManager extends RegistryReader implements
        IWorkbenchRegistryConstants {

    public static class ActionBuilder implements IActionBuilder {

        private IConfigurationElement element;

        private String id;

        private IEditorActionDelegate delegate;

        private boolean delegateFailed = false;

        private ImageDescriptor icon;

        private boolean iconFailed = false;

        private ImageDescriptor disabledIcon;

        private boolean disabledIconFailed = false;

        public ActionBuilder(IConfigurationElement element)
                throws CoreException {
            this.element = element;
            if (getClassValue(element, ATT_CLASS) == null)
                throw new CoreException(new Status(IStatus.ERROR, element
                        .getNamespaceIdentifier(),
                        "Invalid extension (missing class)")); //$NON-NLS-1$
            this.id = element.getAttribute(ATT_ID);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xmind.ui.internal.IActionBuilder#getId()
         */
        public String getId() {
            return id;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xmind.ui.internal.IActionBuilder#getLabel()
         */
        public String getLabel() {
            return element.getAttribute(ATT_LABEL);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xmind.ui.internal.IActionBuilder#getDelegate()
         */
        public IEditorActionDelegate getDelegate() {
            if (delegate == null && !delegateFailed) {
                delegate = createDelegate();
                if (delegate == null)
                    delegateFailed = true;
            }
            return delegate;
        }

        private IEditorActionDelegate createDelegate() {
            try {
                return (IEditorActionDelegate) element
                        .createExecutableExtension(ATT_CLASS);
            } catch (CoreException e) {
                Logger.log(e);
                return null;
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xmind.ui.internal.IActionBuilder#getDefinitionId()
         */
        public String getDefinitionId() {
            return element.getAttribute(ATT_DEFINITION_ID);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xmind.ui.internal.IActionBuilder#getTooltip()
         */
        public String getTooltip() {
            return element.getAttribute(ATT_TOOLTIP);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xmind.ui.internal.IActionBuilder#getIcon()
         */
        public ImageDescriptor getIcon() {
            if (icon == null && !iconFailed) {
                icon = createIcon();
                if (icon == null)
                    iconFailed = true;
            }
            return icon;
        }

        private ImageDescriptor createIcon() {
            String iconPath = element.getAttribute(ATT_ICON);
            if (iconPath != null) {
                return AbstractUIPlugin.imageDescriptorFromPlugin(element
                        .getNamespaceIdentifier(), iconPath);
            }
            return null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xmind.ui.internal.IActionBuilder#getDisabledIcon()
         */
        public ImageDescriptor getDisabledIcon() {
            if (disabledIcon == null && !disabledIconFailed) {
                disabledIcon = createDisabledIcon();
                if (disabledIcon == null)
                    disabledIconFailed = true;
            }
            return disabledIcon;
        }

        private ImageDescriptor createDisabledIcon() {
            String iconPath = element.getAttribute(ATT_DISABLEDICON);
            if (iconPath != null) {
                return AbstractUIPlugin.imageDescriptorFromPlugin(element
                        .getNamespaceIdentifier(), iconPath);
            }
            return null;
        }

        public IWorkbenchAction createAction(IWorkbenchPage page) {
            return new DelegatedAction(this, page);
        }

    }

    private static class DelegatedAction extends Action implements
            IWorkbenchAction, IPartListener, ISelectionListener {

        private IWorkbenchPage page;

        private IActionBuilder builder;

        private IEditorPart targetEditor;

        public DelegatedAction(IActionBuilder builder, IWorkbenchPage page) {
            this.builder = builder;
            this.page = page;
            setId(builder.getId());
            setText(builder.getLabel());
            setToolTipText(builder.getTooltip());
            setImageDescriptor(builder.getIcon());
            setDisabledImageDescriptor(builder.getDisabledIcon());
            setActionDefinitionId(builder.getDefinitionId());
            page.addPartListener(this);
            page.addSelectionListener(this);
        }

        public void run() {
            IEditorActionDelegate delegate = builder.getDelegate();
            if (delegate != null)
                delegate.run(this);
        }

        public void dispose() {
            page.removeSelectionListener(this);
            page.removePartListener(this);
        }

        public void partActivated(IWorkbenchPart part) {
            if (part instanceof IEditorPart) {
                this.targetEditor = (IEditorPart) part;
                IEditorActionDelegate delegate = builder.getDelegate();
                if (delegate != null)
                    delegate.setActiveEditor(this, targetEditor);
            }
        }

        public void partBroughtToTop(IWorkbenchPart part) {
        }

        public void partClosed(IWorkbenchPart part) {
            if (part == this.targetEditor) {
                IEditorActionDelegate delegate = builder.getDelegate();
                if (delegate != null)
                    delegate.setActiveEditor(this, null);
            }
        }

        public void partDeactivated(IWorkbenchPart part) {
        }

        public void partOpened(IWorkbenchPart part) {
        }

        public void selectionChanged(IWorkbenchPart part, ISelection selection) {
            if (part == this.targetEditor) {
                IEditorActionDelegate delegate = builder.getDelegate();
                if (delegate != null)
                    delegate.selectionChanged(this, selection);
            }
        }

    }

    private static final ImageActionExtensionManager instance = new ImageActionExtensionManager();

    private List<IActionBuilder> actionBuilders;

    private ImageActionExtensionManager() {
    }

    public List<IActionBuilder> getActionBuilders() {
        ensureLoaded();
        return actionBuilders;
    }

    private void ensureLoaded() {
        if (actionBuilders != null)
            return;
        lazyLoad();
        if (actionBuilders == null)
            actionBuilders = Collections.emptyList();
    }

    private void lazyLoad() {
        readRegistry(Platform.getExtensionRegistry(), MindMapUI.PLUGIN_ID,
                "imageActions"); //$NON-NLS-1$
    }

    protected boolean readElement(IConfigurationElement element) {
        String name = element.getName();
        if (TAG_ACTION.equals(name)) {
            readChooser(element);
            readElementChildren(element);
            return true;
        }
        return false;
    }

    private void readChooser(IConfigurationElement element) {
        try {
            IActionBuilder actionBuilder = new ActionBuilder(element);
            register(actionBuilder);
        } catch (CoreException e) {
            Logger.log(e);
        }
    }

    private void register(IActionBuilder actionBuilder) {
        if (actionBuilders == null)
            actionBuilders = new ArrayList<IActionBuilder>();
        actionBuilders.add(actionBuilder);
    }

    public static ImageActionExtensionManager getInstance() {
        return instance;
    }

}