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

import java.util.EventObject;

/**
 * @author Brian Sun
 */
public class PartEvent extends EventObject {

    private static final long serialVersionUID = 6229254917943705753L;
    
    public IPart child;

    /**
     * @param source
     * @param child
     */
    public PartEvent(Object source, IPart child) {
        super(source);
        this.child = child;
    }

}