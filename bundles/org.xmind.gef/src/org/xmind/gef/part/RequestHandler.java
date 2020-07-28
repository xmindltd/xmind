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

import java.util.HashMap;
import java.util.Map;

import org.xmind.gef.EditDomain;
import org.xmind.gef.Request;
import org.xmind.gef.policy.IEditPolicy;

/**
 * @author Brian Sun
 */
public class RequestHandler implements IRequestHandler {

    private Map<String, IEditPolicy> editPolicies = null;

    private Map<String, String> editPolicyIds = null;

    private IPart host;

    /**
     * @param host
     */
    public RequestHandler(IPart host) {
        this.host = host;
    }

    /**
     * @see org.xmind.gef.part.IRequestHandler#getPolicy(int)
     */
    public IEditPolicy getPolicy(String role) {
        IEditPolicy editPolicy = editPolicies == null ? null : editPolicies
                .get(role);
        if (editPolicy == null) {
            String editPolicyId = editPolicyIds == null ? null : editPolicyIds
                    .get(role);
            if (editPolicyId != null) {
                EditDomain domain = host.getSite().getDomain();
                if (domain != null) {
                    editPolicy = domain.getEditPolicy(role, editPolicyId);
                    if (editPolicy != null)
                        installEditPolicy(role, editPolicy);
                }
                if (editPolicy != null) {
                    editPolicyIds.remove(role);
                }
            }
        }
        return editPolicy;
    }

    /**
     * @see org.xmind.gef.part.IRequestHandler#hasRole(int)
     */
    public boolean hasRole(String role) {
        return (editPolicies != null && editPolicies.containsKey(role))
                || (editPolicyIds != null && editPolicyIds.containsKey(role));
    }

    /**
     * @see org.xmind.gef.part.IRequestHandler#installEditPolicy(int,
     *      org.xmind.gef.policy.IEditPolicy)
     */
    public void installEditPolicy(String role, IEditPolicy editPolicy) {
        if (editPolicies == null)
            editPolicies = new HashMap<String, IEditPolicy>();
        editPolicies.put(role, editPolicy);
    }

    public void installEditPolicy(String role, String editPolicyId) {
        if (editPolicyIds == null)
            editPolicyIds = new HashMap<String, String>();
        editPolicyIds.put(role, editPolicyId);
    }

    /**
     * @see org.xmind.gef.part.IRequestHandler#uninstallEditPolicy(int)
     */
    public void uninstallEditPolicy(String role) {
        if (editPolicies != null)
            editPolicies.remove(role);
        if (editPolicyIds != null)
            editPolicyIds.remove(role);
    }

    /**
     * @see org.xmind.gef.part.IRequestHandler#handleRequest(org.xmind.gef.Request,
     *      String)
     */
    public void handleRequest(Request request, String role) {
        IEditPolicy editPolicy = getPolicy(role);
        if (editPolicy != null && editPolicy.understands(request.getType())) {
            if (request.getPrimaryTarget() == null) {
                request.setPrimaryTarget(host);
            }
            editPolicy.handle(request);
        } else {
            IPart parent = host.getParent();
            if (parent != null) {
                parent.handleRequest(request, role);
            }
        }
    }

}