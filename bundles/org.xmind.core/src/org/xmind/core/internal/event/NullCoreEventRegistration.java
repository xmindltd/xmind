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
package org.xmind.core.internal.event;

import org.xmind.core.event.ICoreEventRegistration;

public class NullCoreEventRegistration implements ICoreEventRegistration {

    private static final NullCoreEventRegistration instance = new NullCoreEventRegistration();

    private boolean valid = true;

    private NullCoreEventRegistration() {
    }

    public void unregister() {
        valid = false;
    }

    public boolean isValid() {
        return valid;
    }

    public static NullCoreEventRegistration getInstance() {
        return instance;
    }
}