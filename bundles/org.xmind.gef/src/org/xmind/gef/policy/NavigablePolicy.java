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
package org.xmind.gef.policy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.xmind.gef.GEF;
import org.xmind.gef.Request;
import org.xmind.gef.part.IPart;

public class NavigablePolicy extends AbstractEditPolicy {

    public boolean understands(String requestType) {
        return super.understands(requestType)
                || GEF.REQ_NAV_UP.equals(requestType)
                || GEF.REQ_NAV_DOWN.equals(requestType)
                || GEF.REQ_NAV_LEFT.equals(requestType)
                || GEF.REQ_NAV_RIGHT.equals(requestType)
                || GEF.REQ_NAV_BEGINNING.equals(requestType)
                || GEF.REQ_NAV_END.equals(requestType)
                || GEF.REQ_NAV_NEXT.equals(requestType)
                || GEF.REQ_NAV_PREV.equals(requestType);
    }

    public void handle(Request request) {
        String navType = request.getType();
        if (GEF.REQ_NAV_UP.equals(navType) || GEF.REQ_NAV_DOWN.equals(navType)
                || GEF.REQ_NAV_LEFT.equals(navType)
                || GEF.REQ_NAV_RIGHT.equals(navType)
                || GEF.REQ_NAV_BEGINNING.equals(navType)
                || GEF.REQ_NAV_END.equals(navType)) {
            List<IPart> sources = request.getTargets();
            List<IPart> result = new ArrayList<IPart>();
            IPart seqStart = getSequenceStart(request);
            if (seqStart != null) {
                findSequentialNavParts(request, navType, seqStart, sources,
                        result);
            } else {
                findNavParts(request, navType, sources, result);
            }
            setNavigationResult(request, result);
        } else if (GEF.REQ_NAV_NEXT.equals(navType)
                || GEF.REQ_NAV_PREV.equals(navType)) {
            IPart source = request.getPrimaryTarget();
            IPart target = findNextOrPrev(source, GEF.REQ_NAV_NEXT
                    .equals(navType));
            if (target != null) {
                setNavigationResult(request, Arrays.asList(target));
            }
        }
    }

    protected IPart findNextOrPrev(IPart source, boolean nextOrPrev) {
        return null;
    }

    protected void setNavigationResult(Request request,
            List<? extends IPart> result) {
        if (!result.isEmpty()) {
            request.setResult(GEF.RESULT_NAVIGATION, result
                    .toArray(new IPart[result.size()]));
        }
    }

    protected void findNavParts(Request request, String navType,
            List<IPart> sources, List<IPart> result) {
        result.addAll(sources);
    }

    protected void findSequentialNavParts(Request request, String navType,
            IPart sequenceStart, List<IPart> sources, List<IPart> result) {
        result.addAll(sources);
    }

    protected IPart getSequenceStart(Request request) {
        if (isSequential(request)) {
            return (IPart) request.getParameter(GEF.PARAM_NAV_SEQUENCE_START);
        }
        return null;
    }

    protected boolean isSequential(Request request) {
        return request.isParameter(GEF.PARAM_NAV_SEQUENTIAL);
    }
}