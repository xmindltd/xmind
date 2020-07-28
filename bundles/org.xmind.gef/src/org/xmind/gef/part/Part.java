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

import static org.xmind.gef.GEF.PART_ACTIVE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.xmind.gef.IViewer;
import org.xmind.gef.Request;
import org.xmind.gef.status.IStatusListener;
import org.xmind.gef.status.StatusEvent;

/**
 * @author Administrator
 */
public class Part implements IPart {

    private static final List<IPart> EMPTY_CHILDREN = Collections.emptyList();

    private Object model = null;

    private IPart parent = null;

    private List<IPart> children = null;

    private IPartSite site = null;

    private IPartStatus status = null;

    private List<IPartListener> partListeners = null;

    public Part() {
    }

    /**
     * @see org.xmind.gef.part.IPart#getModel()
     */
    public Object getModel() {
        return model;
    }

    public IPart getParent() {
        return parent;
    }

    /**
     * @param model
     *            the model to set
     */
    public void setModel(Object model) {
        if (model == this.model)
            return;
        this.model = model;
    }

    public void setParent(IPart parent) {
        if (parent == this)
            throw new IllegalArgumentException(
                    "A part should NOT be set as its own parent."); //$NON-NLS-1$
        this.parent = parent;
    }

    public List<IPart> getChildren() {
        if (children == null)
            return EMPTY_CHILDREN;
        return children;
    }

    public void addNotify() {
        register();
        for (Object o : getChildren().toArray()) {
            ((IPart) o).addNotify();
        }
        refresh();
    }

    public void removeNotify() {
        getStatus().dePreSelect();
        getStatus().deSelect();
        getStatus().lostFocus();
        for (Object o : getChildren().toArray()) {
            ((IPart) o).removeNotify();
        }
        unregister();
    }

    protected void addChild(IPart child, int index) {
        if (child == null)
            return;

        if (child == this)
            throw new IllegalArgumentException(
                    "A part should NOT be added as its own child."); //$NON-NLS-1$

        fireAddingChild(child, index);
        if (index == -1)
            index = getChildren().size();
        if (children == null) {
            children = new ArrayList<IPart>();
        }

        children.add(index, child);
        child.setParent(this);
        addChildView(child, index);
        child.addNotify();

        if (getStatus().isActive())
            child.getStatus().activate();
        fireChildAdded(child, index);
    }

    protected void removeChild(IPart child) {
        if (child == null || child == this)
            return;

        int index = getChildren().indexOf(child);
        if (index < 0)
            return;

        fireRemovingChild(child, index);
        if (getStatus().isActive())
            child.getStatus().deactivate();

        child.removeNotify();
        removeChildView(child);
        child.setParent(null);
        if (children != null && !children.isEmpty())
            children.remove(child);
        fireChildRemoved(child, index);
    }

    protected void addChildView(IPart child, int index) {
    }

    protected void removeChildView(IPart child) {
    }

    protected void register() {
    }

    protected void unregister() {
    }

    protected void unregisterModel(Object model) {
        PartRegistry partRegistry = getPartRegistry();
        if (partRegistry != null) {
            partRegistry.unregister(model, this);
        }
    }

    protected void registerModel(Object model) {
        PartRegistry partRegistry = getPartRegistry();
        if (partRegistry != null) {
            partRegistry.register(model, this);
        }
    }

    protected PartRegistry getPartRegistry() {
        return getSite().getPartRegistry();
    }

    public void refresh() {
        updateView();
        refreshChildren();
        updateChildren();
    }

    public void update() {
        updateView();
        updateChildren();
    }

    protected void updateView() {
    }

    protected void updateChildren() {
    }

    protected void refreshChildren() {
        Map<Object, IPart> modelToPart = new HashMap<Object, IPart>();
        List<IPart> currentChildren = getChildren();
        for (IPart p : currentChildren) {
            modelToPart.put(p.getModel(), p);
        }

        Object[] newModels = getModelChildren(getModel());
        if (newModels.length > 0) {
            IViewer viewer = getSite().getViewer();
            if (viewer != null) {
                newModels = getFilteredModelChildren(viewer, getModel(),
                        newModels);
                newModels = getSortedModelChildren(viewer, getModel(),
                        newModels);
            }
        }

        int i;
        IPartFactory factory = getSite().getPartFactory();
        for (i = 0; i < newModels.length; i++) {
            Object model = newModels[i];
            if (i < currentChildren.size()) {
                Object m = currentChildren.get(i).getModel();
                if (model == m || model.equals(m))
                    continue;
            }

            IPart p = modelToPart.get(model);
            if (p != null) {
                reorderChild(p, i);
            } else {
                p = createChild(model, factory);
                addChild(p, i);
            }
        }

        Object[] toTrim = currentChildren.toArray();
        for (; i < toTrim.length; i++) {
            removeChild((IPart) toTrim[i]);
        }
    }

    protected void reorderChild(IPart child, int index) {
        if (children == null)
            return;

        removeChildView(child);
        children.remove(child);
        children.add(index, child);
        addChildView(child, index);
    }

    protected IPart createChild(Object modelChild, IPartFactory factory) {
        return factory.createPart(this, modelChild);
    }

    protected Object[] getSortedModelChildren(IViewer viewer, Object model,
            Object[] modelChildren) {
        ViewerComparator sorter = viewer.getComparator();
        if (sorter != null) {
            sorter.sort((Viewer) viewer, modelChildren);
        }
        return modelChildren;
    }

    protected Object[] getFilteredModelChildren(IViewer viewer, Object model,
            Object[] modelChildren) {
        ViewerFilter[] filters = viewer.getFilters();
        if (filters != null && filters.length > 0) {
            for (ViewerFilter f : filters) {
                modelChildren = f.filter((Viewer) viewer, model, modelChildren);
            }
        }
        return modelChildren;
    }

    protected Object[] getModelChildren(Object model) {
        return new Object[0];
    }

    /**
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    public <T> T getAdapter(Class<T> adapter) {
        if (adapter == IPartSite.class)
            return adapter.cast(getSite());
        if (adapter == IPartStatus.class)
            return adapter.cast(getStatus());
        if (getModel() instanceof IAdaptable)
            return ((IAdaptable) getModel()).getAdapter(adapter);
        return null;
    }

    public void addPartListener(IPartListener listener) {
        if (partListeners == null) {
            partListeners = new ArrayList<IPartListener>();
        }
        partListeners.add(listener);
    }

    public void removePartListener(IPartListener listener) {
        if (partListeners != null) {
            partListeners.remove(listener);
        }
    }

    protected void fireChildAdded(IPart child, int index) {
        if (partListeners != null) {
            PartEvent event = new PartEvent(this, child);
            for (Object listener : partListeners.toArray()) {
                ((IPartListener) listener).childAdded(event);
            }
        }
    }

    protected void fireRemovingChild(IPart child, int index) {
        if (partListeners != null) {
            PartEvent event = new PartEvent(this, child);
            for (Object listener : partListeners.toArray()) {
                ((IPartListener) listener).childRemoving(event);
            }
        }
    }

    protected void fireAddingChild(IPart child, int index) {
        if (partListeners != null) {
            PartEvent event = new PartEvent(this, child);
            for (Object listener : partListeners.toArray()) {
                if (listener instanceof IPartListener2) {
                    ((IPartListener2) listener).childAdding(event);
                }
            }
        }
    }

    protected void fireChildRemoved(IPart child, int index) {
        if (partListeners != null) {
            PartEvent event = new PartEvent(this, child);
            for (Object listener : partListeners.toArray()) {
                if (listener instanceof IPartListener2) {
                    ((IPartListener2) listener).childRemoved(event);
                }
            }
        }
    }

    public IPartSite getSite() {
        if (site == null) {
            site = createSite();
        }
        return site;
    }

    protected IPartSite createSite() {
        return new PartSite(this);
    }

    /**
     * @param site
     *            the site to set
     */
    protected void setSite(IPartSite site) {
        this.site = site;
    }

    /**
     * @see org.xmind.gef.part.IPart#getStatus()
     */
    public IPartStatus getStatus() {
        if (status == null) {
            status = new PartStatus(this);
            status.addStatusListener(new IStatusListener() {
                public void statusChanged(StatusEvent event) {
                    if (event.key == PART_ACTIVE) {
                        if (event.newValue) {
                            onActivated();
                        } else {
                            onDeactivated();
                        }
                    } else {
                        handleStatusChanged(event);
                    }
                }
            });
        }
        return status;
    }

    protected void handleStatusChanged(StatusEvent event) {
    }

    protected void onActivated() {
        for (Object o : getChildren().toArray()) {
            ((IPart) o).getStatus().activate();
        }
    }

    protected void onDeactivated() {
        for (Object p : getChildren().toArray()) {
            ((IPart) p).getStatus().deactivate();
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" + getModel() + "}"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Default implementation of the abstract method indicating that this part
     * doesn't have any role. Subclasses may override to determine whether or
     * not this part has the specific role.
     */
    public boolean hasRole(String role) {
        return false;
    }

    /**
     * Default implementation of the abstract method to do nothing. Subclasses
     * may override to provide this part with abilities to handle requests.
     */
    public void handleRequest(Request request, String role) {
        // do nothing
    }

}
