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
package org.xmind.ui.style;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.xmind.gef.part.IGraphicalPart;

@Deprecated
public abstract class PredefinedStyleSelector extends LayeredStyleSelector {

    private Map<String, Map<String, Properties>> predefinedProperties = null;

    protected String getLayeredProperty(IGraphicalPart part, String layerName,
            String familyName, String key) {
        if (predefinedProperties == null)
            return null;
        Map<String, Properties> familyToProp = predefinedProperties
                .get(layerName);
        if (familyToProp == null)
            return null;
        Properties prop = familyToProp.get(familyName);
        if (prop == null)
            return null;
        return prop.getProperty(key);
    }

    protected void initPredefinedProperty(String layerName, String familyName,
            String key, String value) {
        if (layerName == null || familyName == null || key == null)
            return;
        if (value == null) {
            removePredefinedProperty(layerName, familyName, key);
        } else {
            putPredefinedProperty(layerName, familyName, key, value);
        }
    }

    private void putPredefinedProperty(String layerName, String familyName,
            String key, String value) {
        if (predefinedProperties == null)
            predefinedProperties = new HashMap<String, Map<String, Properties>>();
        Map<String, Properties> familyToProp = predefinedProperties
                .get(layerName);
        if (familyToProp == null) {
            familyToProp = new HashMap<String, Properties>();
            predefinedProperties.put(layerName, familyToProp);
        }
        Properties prop = familyToProp.get(familyName);
        if (prop == null) {
            prop = new Properties();
            familyToProp.put(familyName, prop);
        }
        prop.setProperty(key, value);
    }

    private void removePredefinedProperty(String layerName, String familyName,
            String key) {
        if (predefinedProperties == null)
            return;
        Map<String, Properties> familyToProp = predefinedProperties
                .get(layerName);
        if (familyToProp == null)
            return;
        Properties prop = familyToProp.get(familyName);
        if (prop == null)
            return;
        prop.remove(key);
        if (prop.isEmpty()) {
            familyToProp.remove(familyName);
            if (familyToProp.isEmpty()) {
                predefinedProperties.remove(layerName);
                if (predefinedProperties.isEmpty()) {
                    predefinedProperties = null;
                }
            }
        }
    }

}