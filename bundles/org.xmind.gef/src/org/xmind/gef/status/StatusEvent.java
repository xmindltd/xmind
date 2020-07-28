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
package org.xmind.gef.status;

import java.util.EventObject;


/**
 * @author Brian Sun
 */
public class StatusEvent extends EventObject {
    
    public int key;
    public boolean newValue;

    /**
     * @param source
     * @param key
     * @param oldValue
     * @param newValue
     */
    public StatusEvent( Object source, int key, boolean newValue ) {
        super( source );
        this.key = key;
        this.newValue = newValue;
    }

    private static final long serialVersionUID = -5453115942237599642L;
    
}