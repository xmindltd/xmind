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
package org.xmind.gef.policy;

import java.util.ArrayList;
import java.util.List;

import org.xmind.gef.GEF;
import org.xmind.gef.Request;
import org.xmind.gef.part.IPart;

public abstract class TraversablePolicy extends AbstractEditPolicy {

    public boolean understands(String requestType) {
        return super.understands(requestType)
                || GEF.REQ_GET_TRAVERSABLES.equals(requestType);
    }

    public void handle(Request request) {
        String requestType = request.getType();
        if (GEF.REQ_GET_TRAVERSABLES.equals(requestType)) {
            getTraversables(request);
        }
    }

    protected void getTraversables(Request request) {
        IPart source = getTraverseSource(request);
        if (source == null)
            return;

        List<IPart> result = new ArrayList<IPart>();
        findTraversables(request, source, result);
        setTraverseResult(request, result);
    }

    protected IPart getTraverseSource(Request request) {
        IPart source = request.getPrimaryTarget();
        if (source != null && source.hasRole(GEF.ROLE_TRAVERSABLE))
            return source;
        return null;
    }

    protected void setTraverseResult(Request request, List<IPart> result) {
        if (!result.isEmpty()) {
            request.setResult(GEF.RESULT_TRAVERSE, result
                    .toArray(new IPart[result.size()]));
        }
    }

    protected abstract void findTraversables(Request request, IPart source,
            List<IPart> result);

    protected void addTraversableResults(List<? extends IPart> parts,
            List<IPart> result) {
        for (IPart part : parts) {
            addTraversableResult(part, result);
        }
    }

    protected void addTraversableResult(IPart part, List<IPart> result) {
        if (part != null && part.getStatus().isActive()
                && part.hasRole(GEF.ROLE_TRAVERSABLE))
            result.add(part);
    }
}