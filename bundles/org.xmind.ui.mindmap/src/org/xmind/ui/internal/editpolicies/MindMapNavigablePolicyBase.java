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
package org.xmind.ui.internal.editpolicies;

import java.util.List;

import org.eclipse.draw2d.FigureListener;
import org.eclipse.draw2d.IFigure;
import org.xmind.gef.GEF;
import org.xmind.gef.Request;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.gef.part.IPart;
import org.xmind.gef.policy.NavigablePolicy;
import org.xmind.ui.util.MindMapUtils;

public abstract class MindMapNavigablePolicyBase extends NavigablePolicy {

    public static final String CACHE_NAV_OUTGOING = "org.xmind.ui.cache.navigation.outgoing."; //$NON-NLS-1$

    public static final String CACHE_NAV_INCOMING = "org.xmind.ui.cache.navigation.incoming."; //$NON-NLS-1$

    protected void findNavParts(Request request, String navType,
            List<IPart> sources, List<IPart> result) {
        IPart navPart = findCachedNavigation(sources, invertNavType(navType));
        if (navPart != null) {
            result.add(navPart);
            setNavCaches(sources, navPart, navType);
            return;
        }
        navPart = findNewNavParts(request, navType, sources);
        setNavCaches(sources, navPart, navType);
        if (navPart != null) {
            result.add(navPart);
            return;
        }
        super.findNavParts(request, navType, sources, result);
    }

    protected abstract IPart findNewNavParts(Request request, String navType,
            List<IPart> sources);

    protected String invertNavType(String navType) {
        if (GEF.REQ_NAV_UP.equals(navType))
            return GEF.REQ_NAV_DOWN;
        if (GEF.REQ_NAV_DOWN.equals(navType))
            return GEF.REQ_NAV_UP;
        if (GEF.REQ_NAV_LEFT.equals(navType))
            return GEF.REQ_NAV_RIGHT;
        if (GEF.REQ_NAV_RIGHT.equals(navType))
            return GEF.REQ_NAV_LEFT;
        return navType;
    }

    private String getCacheKey(String navType, boolean inOrOut) {
        String prefix = inOrOut ? CACHE_NAV_INCOMING : CACHE_NAV_OUTGOING;
        return prefix + navType.replaceAll("\\s", "_"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    protected IPart findCachedNavigation(List<IPart> sources, String navType) {
        for (IPart source : sources) {
            IPart navPart = getCachedNavigation(source, navType);
            if (navPart != null)
                return navPart;
        }
        return null;
    }

    protected IPart getCachedNavigation(IPart source, String navType) {
        String inKey = getCacheKey(navType, true);
        IPart target = (IPart) MindMapUtils.getCache(source, inKey);
        if (target != null) {
            if (target.getStatus().isActive()
                    && target.hasRole(GEF.ROLE_SELECTABLE)
                    && source == MindMapUtils.getCache(target, getCacheKey(
                            navType, false))) {
                return target;
            }
            MindMapUtils.flushCache(source, inKey);
        }
        return null;
    }

    protected void setNavCaches(List<IPart> sources, IPart target,
            String navType) {
        if (!sources.isEmpty()) {
            setNavCache(sources.get(0), target, navType);
        }
        for (int i = 1; i < sources.size(); i++) {
            setNavCache(sources.get(i), null, navType);
        }
    }

    protected void setNavCache(final IPart source, final IPart target,
            String navType) {
        final String outKey = getCacheKey(navType, false);
        if (target == null || target == source) {
            IPart oldTarget = (IPart) MindMapUtils.getCache(source, outKey);
            if (oldTarget != null) {
                MindMapUtils.flushCache(oldTarget, getCacheKey(navType, true));
            }
            MindMapUtils.flushCache(source, outKey);
        } else {
            MindMapUtils.setCache(source, outKey, target);
            if (source instanceof IGraphicalPart) {
                ((IGraphicalPart) source).getFigure().addFigureListener(
                        new FigureListener() {
                            public void figureMoved(IFigure fig) {
                                fig.removeFigureListener(this);
                                MindMapUtils.flushCache(source, outKey);
                            }
                        });
            }
        }

        if (target != null) {
            final String inKey = getCacheKey(navType, true);
            MindMapUtils.setCache(target, inKey, source);
            if (target instanceof IGraphicalPart) {
                ((IGraphicalPart) target).getFigure().addFigureListener(
                        new FigureListener() {
                            public void figureMoved(IFigure fig) {
                                fig.removeFigureListener(this);
                                MindMapUtils.flushCache(target, inKey);
                            }
                        });
            }
        }
    }

}