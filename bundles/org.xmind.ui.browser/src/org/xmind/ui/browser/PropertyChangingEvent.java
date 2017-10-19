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

package org.xmind.ui.browser;

import java.beans.PropertyChangeEvent;

/**
 * @author Frank Shaka
 * 
 */
public class PropertyChangingEvent extends PropertyChangeEvent {

    /**
     * 
     */
    private static final long serialVersionUID = 6238615584441475137L;

    public boolean doit;

    /**
     * @param source
     * @param propertyName
     * @param oldValue
     * @param newValue
     */
    public PropertyChangingEvent(Object source, String propertyName,
            Object oldValue, Object newValue, boolean doit) {
        super(source, propertyName, oldValue, newValue);
        this.doit = doit;
    }

}
