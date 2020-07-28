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
package org.xmind.gef.event;

import org.eclipse.swt.events.TraverseEvent;

/**
 * @author Brian Sun
 * @version 2005
 */
public class KeyEvent {

    public int keyCode;

    public char character;

    public int traverse;

    public boolean isImeOpened;

    private int state;

    private boolean consumed = false;

    /**
     * @param keyCode
     * @param character
     */
    public KeyEvent(int state, int keyCode, char character) {
        super();
        this.state = state;
        this.keyCode = keyCode;
        this.character = character;
    }

    /**
     * @param keyCode
     * @param character
     * @param traverse
     */
    public KeyEvent(int state, int keyCode, char character, int traverse) {
        this.state = state;
        this.keyCode = keyCode;
        this.character = character;
        this.traverse = traverse;
    }

    /**
     * @return the state
     */
    public int getState() {
        return state;
    }

    public boolean isConsumed() {
        return consumed;
    }

    public void consume() {
        consumed = true;
    }

//    public static KeyEvent createEvent( org.eclipse.draw2d.KeyEvent ke ) {
//        return new KeyEvent(ke.getState(), ke.keycode, ke.character);
//    }

    public static KeyEvent createEvent(org.eclipse.swt.events.KeyEvent ke,
            boolean isImeOpened) {
        KeyEvent ret = new KeyEvent(ke.stateMask, ke.keyCode, ke.character);
        if (ke instanceof TraverseEvent)
            ret.traverse = ((TraverseEvent) ke).detail;
        ret.isImeOpened = isImeOpened;
        return ret;
    }

}