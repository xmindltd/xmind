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
package org.xmind.gef;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.xmind.gef.command.ICommandStack;
import org.xmind.gef.part.PartRoles;
import org.xmind.gef.policy.IEditPolicy;
import org.xmind.gef.policy.NullEditPolicy;
import org.xmind.gef.tool.ITool;

/**
 * @author Brian Sun
 */
public class EditDomain extends Disposable {

    private IViewer targetViewer = null;

    private ITool activeTool = null;

    private String defaultTool = GEF.TOOL_DEFAULT;

    private Map<String, ITool> tools = null;

    private PartRoles partRoles = null;

    private Map<String, IEditPolicy> policies = null;

    private ICommandStack commandStack = null;

    private List<IEditDomainListener> listeners = null;

    private Queue<Request> requestQueue = new LinkedList<Request>();

    private boolean handlingRequests = false;

    @Override
    public void dispose() {
        if (activeTool != null) {
            activeTool.deactivate(null);
            activeTool = null;
        }
        super.dispose();
    }

    public void installEditPolicy(String policyId, IEditPolicy policy) {
        if (policies == null)
            policies = new HashMap<String, IEditPolicy>();
        policies.put(policyId, policy);
    }

    public void uninstallEditPolicy(String policyId) {
        if (policies == null)
            return;
        policies.remove(policyId);
    }

    public IEditPolicy getEditPolicy(String role, String policyId) {
        if (policies != null) {
            IEditPolicy policy = policies.get(policyId);
            if (policy != null)
                return policy;
        }
        return NullEditPolicy.getInstance();
    }

    /**
     * @return the partRoles
     */
    public PartRoles getPartRoles() {
        if (partRoles == null) {
            partRoles = new PartRoles();
        }
        return partRoles;
    }

    /**
     * @return
     */
    public ICommandStack getCommandStack() {
        return commandStack;
    }

    /**
     * @param commandStack
     *            the commandStack to set
     */
    public void setCommandStack(ICommandStack commandStack) {
        this.commandStack = commandStack;
    }

    /**
     * 
     * @return the viewer
     * @deprecated use {@link #getTargetViewer()} instead
     */
    public IViewer getViewer() {
        return this.targetViewer;
    }

    /**
     * 
     * @param viewer
     *            the viewer to set
     * @deprecated use {@link IViewer#setEditDomain(EditDomain)} instead
     */
    public void setViewer(IViewer viewer) {
        viewer.setEditDomain(this);
//        if (this.targetViewer != null) {
//            this.targetViewer.setEditDomain(null);
//        }
//        this.targetViewer = viewer;
//        if (viewer != null) {
//            viewer.setEditDomain(this);
//        }
    }

    public IViewer getTargetViewer() {
        return this.targetViewer;
    }

    /**
     * @param targetViewer
     *            the targetViewer to set
     */
    public void setTargetViewer(IViewer targetViewer) {
        this.targetViewer = targetViewer;
    }

//    public List<IViewer> getViewers() {
//        if (viewers == null)
//            viewers = new ArrayList<IViewer>();
//        return viewers;
//    }
//
//    /**
//     * @param viewer
//     *            the viewer to set
//     */
//    public void addViewer(IViewer viewer) {
//        viewer.setDomain(this);
//        if (viewers == null)
//            viewers = new ArrayList<IViewer>();
//        if (!viewers.contains(viewer))
//            viewers.add(viewer);
//    }
//
//    public void removeViewer(IViewer viewer) {
//        if (viewers == null)
//            return;
//        if (viewers.remove(viewer)) {
//            viewer.setDomain(null);
//        }
//    }

    public ITool getDefaultTool() {
        return tools == null ? null : tools.get(defaultTool);
    }

    public String getDefaultToolType() {
        return defaultTool;
    }

    public void setDefaultTool(String id) {
        this.defaultTool = id;
    }

    /**
     * @return the activeTool
     */
    public ITool getActiveTool() {
        if (activeTool == null) {
            setActiveTool(GEF.TOOL_DEFAULT);
        }
        return activeTool;
    }

    public ITool setActiveTool(String id) {
        ITool ret = getTool(id);
        if (ret != null) {
            setActiveTool(ret);
        }
        return ret;
    }

    /**
     * @param activeTool
     *            the activeTool to set
     */
    private void setActiveTool(ITool activeTool) {
        if (activeTool == this.activeTool)
            return;
        ITool oldTool = this.activeTool;
        if (oldTool != null)
            oldTool.deactivate(activeTool);
        this.activeTool = activeTool;
        if (activeTool != null)
            activeTool.activate(oldTool);
        fireActiveToolChanged(oldTool);
    }

    public void installTool(String id, ITool tool) {
        if (id == null || tool == null)
            return;
        if (tools == null)
            tools = new HashMap<String, ITool>();
        tools.put(id, tool);
        tool.setDomain(this);
        if (defaultTool == null || GEF.TOOL_DEFAULT.equals(defaultTool))
            defaultTool = id;
    }

    public ITool getTool(String id) {
        if (GEF.TOOL_DEFAULT.equals(id))
            return getDefaultTool();
        return tools == null ? null : tools.get(id);
    }

    public boolean hasTool(String id) {
        if (GEF.TOOL_DEFAULT.equals(id))
            return hadDefaultTool();
        return tools != null && id != null && tools.containsKey(id);
    }

    private boolean hadDefaultTool() {
        return tools != null && defaultTool != null
                && tools.containsKey(defaultTool);
    }

    public void addEditDomainListener(IEditDomainListener listener) {
        if (listeners == null)
            listeners = new ArrayList<IEditDomainListener>();
        listeners.add(listener);
    }

    public void removeEditDomainListener(IEditDomainListener listener) {
        if (listeners == null)
            return;
        listeners.remove(listener);
        if (listeners.isEmpty())
            listeners = null;
    }

    protected void fireActiveToolChanged(ITool oldTool) {
        if (listeners == null)
            return;
        ITool newTool = getActiveTool();
        for (Object listener : listeners.toArray()) {
            ((IEditDomainListener) listener)
                    .activeToolChanged(oldTool, newTool);
        }
    }

    public void handleRequest(String requestType, IViewer targetViewer) {
        handleRequest(new Request(requestType).setViewer(targetViewer));
    }

    public void handleRequest(Request request) {
        pushRequestInQueue(request);
        ensureRequestHandling();
    }

    /**
     * @param request
     */
    private void pushRequestInQueue(Request request) {
        requestQueue.add(request);
    }

    /**
     * 
     */
    private void ensureRequestHandling() {
        if (handlingRequests)
            return;

        handlingRequests = true;
        while (!requestQueue.isEmpty()) {
            SafeRunner.run(new SafeRunnable() {
                public void run() throws Exception {
                    internalHandleRequest(requestQueue.poll());
                }
            });
        }
        handlingRequests = false;
    }

    /**
     * @param poll
     */
    protected void internalHandleRequest(Request request) {
        ITool tool = getActiveTool();
        if (tool != null) {
            tool.handleRequest(request);
        }
    }

}