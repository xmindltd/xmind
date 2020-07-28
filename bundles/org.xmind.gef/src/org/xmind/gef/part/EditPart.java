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

import org.xmind.gef.EditDomain;
import org.xmind.gef.Request;
import org.xmind.gef.acc.AccessibleRegistry;
import org.xmind.gef.acc.IAccessible;

/**
 * @author Brian Sun
 */
public class EditPart extends Part {

    private IAccessible accessible = null;

    private IRequestHandler requestHandler = null;

    protected void register() {
        super.register();
        registerAccessible();
        declareEditPolicies(getRequestHandler());
    }

    protected void unregister() {
        super.unregister();
        unregisterAccessible();
    }

    protected void registerAccessible() {
        IAccessible acc = getAccessible();
        if (acc != null) {
            AccessibleRegistry accRegistry = getSite().getViewer()
                    .getAccessibleRegistry();
            if (accRegistry != null) {
                accRegistry.register(acc);
            }
        }
    }

    protected void unregisterAccessible() {
        IAccessible acc = getAccessible();
        if (acc != null) {
            AccessibleRegistry accRegistry = getSite().getViewer()
                    .getAccessibleRegistry();
            if (accRegistry != null) {
                accRegistry.unregister(acc);
            }
        }
    }

    protected IAccessible getAccessible() {
        return accessible;
    }

    /**
     * @param accessible
     */
    protected void setAccessible(IAccessible accessible) {
        if (accessible == null || this.accessible != null)
            return;

        this.accessible = accessible;
    }

    protected void installModelListeners() {
    }

    protected void uninstallModelListeners() {
    }

    public IRequestHandler getRequestHandler() {
        if (requestHandler == null) {
            requestHandler = new RequestHandler(this);
        }
        return requestHandler;
    }

    public void setRequestHandler(IRequestHandler requestProcessor) {
        this.requestHandler = requestProcessor;
    }

    /**
     * @see org.xmind.gef.part.Part#onActivated()
     */
    @Override
    protected void onActivated() {
        installModelListeners();
        super.onActivated();
    }

    /**
     * @see org.xmind.gef.part.Part#onDeactivated()
     */
    @Override
    protected void onDeactivated() {
        super.onDeactivated();
        uninstallModelListeners();
    }

    protected void declareEditPolicies(IRequestHandler reqHandler) {
    }

    protected void sendRequest(String reqType) {
        sendRequest(new Request(reqType).setViewer(getSite().getViewer())
                .setPrimaryTarget(this));
    }

    protected void sendRequest(Request request) {
        EditDomain domain = getSite().getDomain();
        if (domain == null)
            return;

        domain.handleRequest(
                request.setDomain(domain).setViewer(getSite().getViewer()));
    }

    public <T> T getAdapter(Class<T> adapter) {
        if (adapter == IAccessible.class)
            return adapter.cast(getAccessible());
        if (adapter == IRequestHandler.class)
            return adapter.cast(getRequestHandler());
        return super.getAdapter(adapter);
    }

    public boolean hasRole(String role) {
        if (requestHandler != null)
            return requestHandler.hasRole(role);
        return super.hasRole(role);
    }

    public void handleRequest(Request request, String role) {
        if (requestHandler != null) {
            requestHandler.handleRequest(request, role);
        }
    }

}
