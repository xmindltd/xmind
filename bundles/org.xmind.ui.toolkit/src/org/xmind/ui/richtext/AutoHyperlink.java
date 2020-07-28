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
package org.xmind.ui.richtext;

/**
 * 
 * @author Karelun huang
 */
public class AutoHyperlink implements Cloneable {

    public int start;

    public int length;

    public AutoHyperlink(int start, int length) {
        this.start = start;
        this.length = length;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj != this && !(obj instanceof AutoHyperlink))
            return false;
        AutoHyperlink that = (AutoHyperlink) obj;
        return this.start == that.start && this.length == that.length;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("autoHyperlink{start="); //$NON-NLS-1$
        stringBuilder.append(start);
        stringBuilder.append(","); //$NON-NLS-1$
        stringBuilder.append(start + length);
        stringBuilder.append("}"); //$NON-NLS-1$
        return stringBuilder.toString();
    }

    @Override
    protected Object clone() {
        AutoHyperlink clone = new AutoHyperlink(this.start, this.length);
        return clone;
    }
}
