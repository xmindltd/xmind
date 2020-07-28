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
package org.xmind.ui.internal.editpolicies;

import org.xmind.gef.policy.TraversablePolicy;

public abstract class MindMapTraversablePolicyBase extends TraversablePolicy {

//    private static final String CACHE_TRAVERSE_INCOMING = "org.xmind.ui.cache.traverse.incoming"; //$NON-NLS-1$
//
//    private static final String CACHE_TRAVERSE_OUTGOING = "org.xmind.ui.cache.traverse.outgoing"; //$NON-NLS-1$
//
//    protected void setTraverseResult(Request request, List<IPart> result) {
//        IPart source = getTraverseSource(request);
//        if (source != null && !result.isEmpty()) {
//            resortResults(source, result);
//        }
//        super.setTraverseResult(request, result);
//    }
//
//    protected void resortResults(IPart source, List<IPart> result) {
//        IPart cache = findCachedTraversable(source, result);
//        if (cache != null) {
//            while (result.get(0) != cache) {
//                result.add(result.remove(0));
//            }
//        } else {
//
//        }
//        //TODO cache traverse result
//    }
//
//    private IPart findCachedTraversable(IPart source, List<IPart> result) {
//        Object in = MindMapUtils.getCache(source, CACHE_TRAVERSE_INCOMING);
//        MindMapUtils.flushCache(source, CACHE_TRAVERSE_INCOMING);
//        if (in instanceof IPart) {
//            IPart inPart = (IPart) in;
//            if (inPart.getStatus().isActive()
//                    && inPart.hasRole(GEF.ROLE_TRAVERSABLE)
//                    && result.contains(inPart)) {
//                Object out = MindMapUtils.getCache(inPart,
//                        CACHE_TRAVERSE_OUTGOING);
//                if (out == source) {
//                    return inPart;
//                }
//            }
//        }
//        return null;
//    }

}