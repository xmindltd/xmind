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
package org.xmind.ui.viewers;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class SWTUtils {

    protected static final int lower2Upper = 'A' - 'a';

    protected static final int upper2lower = 'a' - 'A';

    public static int toLowerLetter(int letterChar) {
        if (letterChar >= 'A' && letterChar <= 'Z')
            return letterChar + upper2lower;
        return letterChar;
    }

    public static boolean equalsIgnoreCase(int char1, int char2) {
        return toLowerLetter(char1) == toLowerLetter(char2);
    }

    public static boolean matchKey(int stateMask, int keyCode,
            int expectedState, int expectedKeyCode) {
        return matchState(stateMask, expectedState)
                && matchKeyCode(keyCode, expectedKeyCode);
    }

    public static boolean matchState(int stateMask, int expectedState) {
        if (expectedState != 0 && (stateMask & expectedState) == 0)
            return false;
        int unexpectedState = ~expectedState;
        return ((stateMask & unexpectedState) == 0);
    }

    public static boolean matchKeyCode(int keyCode, int expectedKeyCode) {
        if (expectedKeyCode == SWT.CR || expectedKeyCode == SWT.KEYPAD_CR)
            return keyCode == SWT.CR || keyCode == SWT.KEYPAD_CR;
        if (expectedKeyCode == '+' || expectedKeyCode == SWT.KEYPAD_ADD)
            return keyCode == '+' || keyCode == SWT.KEYPAD_ADD;
        if (expectedKeyCode == '-' || expectedKeyCode == SWT.KEYPAD_SUBTRACT)
            return keyCode == '-' || keyCode == SWT.KEYPAD_SUBTRACT;
        // ...
        // TODO add other key code matching policy when needed

        return equalsIgnoreCase(keyCode, expectedKeyCode);
    }

    public static boolean isModifierKey(int stateMask, int keyCode) {
        return stateMask == 0 && (keyCode == SWT.MOD1 || keyCode == SWT.MOD2
                || keyCode == SWT.MOD3 || keyCode == SWT.MOD4);
    }

    public static Path addRoundedRectangle(Path path, float x, float y,
            float width, float height, float corner) {
        float r = x + width;
        float b = y + height;
        float x0 = x + width / 2;
        float y0 = y + height / 2;

        float y1 = Math.min(y + corner, y0);
        path.moveTo(x, y1);

        float x1 = Math.min(x + corner, x0);
        float cx1 = x + (x1 - x) / 4;
        float cy1 = y + (y1 - y) / 4;
        path.cubicTo(x, cy1, cx1, y, x1, y);

        float x2 = Math.max(r - corner, x0);
        path.lineTo(x2, y);

        float cx2 = r - (r - x2) / 4;
        path.cubicTo(cx2, y, r, cy1, r, y1);

        float y2 = Math.max(b - corner, y0);
        path.lineTo(r, y2);

        float cy2 = b - (b - y2) / 4;
        path.cubicTo(r, cy2, cx2, b, x2, b);

        path.lineTo(x1, b);

        path.cubicTo(cx1, b, x, cy2, x, y2);

        path.close();
        return path;
    }

    public static void makeNumeralInput(Control inputControl,
            final boolean minusPermitted, final boolean dotPermitted) {
        inputControl.addListener(SWT.KeyDown, new Listener() {
            public void handleEvent(Event event) {
                if (event.character < 0x20 || event.character > 0x7e) {
                    event.doit = true;
                    return;
                }
                if ((event.character >= '0' && event.character <= '9')
                        || (minusPermitted && event.character == '-')) {
                    event.doit = true;
                } else if (event.character == '.' && dotPermitted) {
                    event.doit = true;
                } else {
                    event.doit = false;
                }
            }

        });
    }

    public static boolean isInside(Control outer, Control inner) {
        if (inner == null || outer == null)
            return false;
        while (inner != null && inner != outer) {
            Control parent = inner.getParent();
            if (parent == inner)
                return false;
            inner = parent;
        }
        return inner == outer;
    }

}