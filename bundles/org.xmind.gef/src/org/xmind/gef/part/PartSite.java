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
package org.xmind.gef.part;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.xmind.gef.EditDomain;
import org.xmind.gef.IViewer;
import org.xmind.gef.util.Properties;

/**
 * @author Brian Sun
 */
public class PartSite implements IPartSite {

    private IPart host = null;

    private IRootPart root = null;

    public PartSite(IPart part) {
        host = part;
    }

    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
        if (adapter == IRootPart.class)
            return getRoot();
        if (adapter == EditDomain.class)
            return getDomain();
        if (adapter == IViewer.class)
            return getViewer();
        if (adapter == IPartFactory.class)
            return getPartFactory();
        if (adapter == PartRegistry.class)
            return getPartRegistry();
        if (adapter == Properties.class)
            return getProperties();
        return host.getAdapter(adapter);
    }

    /**
     * @see org.xmind.gef.part.IPartSite#getRoot()
     */
    public IRootPart getRoot() {
        if (root == null)
            root = findRootPart();
        return root;
    }

    protected IRootPart findRootPart() {
        if (host instanceof IRootPart)
            return (IRootPart) host;
        IPart parent = host.getParent();
        if (parent != null) {
            return parent.getSite().getRoot();
        }
        return null;
    }

    /**
     * @see org.xmind.gef.part.IPartSite#getPart()
     */
    public IPart getPart() {
        return host;
    }

    /**
     * @see org.xmind.gef.part.IPartSite#getDomain()
     */
    public EditDomain getDomain() {
        return getViewer().getEditDomain();
    }

    public IViewer getViewer() {
        IRootPart rootPart = getRoot();
        return rootPart == null ? null : rootPart.getViewer();
    }

    public IPartFactory getPartFactory() {
        IViewer viewer = getViewer();
        return viewer == null ? null : viewer.getPartFactory();
    }

    public PartRegistry getPartRegistry() {
        IViewer viewer = getViewer();
        return viewer == null ? null : viewer.getPartRegistry();
    }

//    public IModelContentProvider getContentProvider() {
//        IViewer viewer = getViewer();
//        return viewer == null ? null : viewer.getContentProvider();
//    }

    public Properties getProperties() {
        IViewer viewer = getViewer();
        return viewer == null ? null : viewer.getProperties();
    }

    public Shell getShell() {
        Control control = getViewerControl();
        return control == null || control.isDisposed() ? null
                : control.getShell();
    }

    public Control getViewerControl() {
        IViewer viewer = getViewer();
        return viewer == null ? null : viewer.getControl();
    }
}