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
package org.xmind.gef.graphicalpolicy;

import org.xmind.gef.part.IGraphicalPart;
import org.xmind.gef.service.StyleOverrideService;

/**
 * @author Frank Shaka
 */
public abstract class AbstractStyleSelector implements IStyleSelector {

    public String getStyleValue(IGraphicalPart part, String key) {
        StyleOverrideService styleOverrideService = (StyleOverrideService) part
                .getSite().getViewer().getService(StyleOverrideService.class);
        if (styleOverrideService != null && styleOverrideService.isActive()) {
            String value = styleOverrideService.getValue(part, key);
            if (isValidValue(part, key, value))
                return value;
        }
        return getStyleValue(part, key, null);
    }

    public String getStyleValue(IGraphicalPart part, String key,
            IStyleValueProvider defaultValueProvider) {
        StyleOverrideService styleOverrideService = (StyleOverrideService) part
                .getSite().getViewer().getService(StyleOverrideService.class);
        if (styleOverrideService != null && styleOverrideService.isActive()) {
            String value = styleOverrideService.getValue(part, key);
            if (isValidValue(part, key, value))
                return value;
        }
        String value = getUserValue(part, key);

        if (!isValidValue(part, key, value) && !ignoresAutoValue(part, key))
            value = getAutoValue(part, key, defaultValueProvider);
        return value;
    }

    protected boolean isValidValue(IGraphicalPart part, String key, String value) {
        return value != null;
    }

    protected boolean ignoresAutoValue(IGraphicalPart part, String key) {
        return false;
    }

    public abstract String getUserValue(IGraphicalPart part, String key);

    public String getAutoValue(IGraphicalPart part, String key) {
        return getAutoValue(part, key, null);
    }

    public abstract String getAutoValue(IGraphicalPart part, String key,
            IStyleValueProvider defaultValueProvider);

    public String getOverridedValue(IGraphicalPart part, String key,
            String layerName) {
        return null;
    }
}