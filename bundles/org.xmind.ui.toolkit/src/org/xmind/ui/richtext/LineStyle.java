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
package org.xmind.ui.richtext;

import org.eclipse.swt.SWT;

/**
 * @author Frank Shaka
 */
public class LineStyle implements Cloneable {

    public static final String NONE_STYLE = "none"; //$NON-NLS-1$

    public static final String BULLET = "bullet"; //$NON-NLS-1$

    public static final String NUMBER = "number"; //$NON-NLS-1$

    public static final int DEFAULT_ALIGNMENT = SWT.LEFT;

    public int lineIndex;

    public int alignment = DEFAULT_ALIGNMENT;

    public int indent = 0;

//    public boolean bullet = false;

//    public boolean number = false;

    public String bulletStyle = NONE_STYLE;

    public LineStyle() {
        this(0);
    }

    public LineStyle(int startLine) {
        this.lineIndex = startLine;
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof LineStyle))
            return false;
        LineStyle that = (LineStyle) obj;
        return this.lineIndex == that.lineIndex
                && this.alignment == that.alignment
                && this.indent == that.indent
                && this.bulletStyle == that.bulletStyle;
    }

    public boolean similarTo(LineStyle that) {
        if (that == null)
            return false;
        return this.alignment == that.alignment && this.indent == that.indent
                && this.bulletStyle == that.bulletStyle;
    }

    public boolean isUnstyled() {
        return this.alignment == DEFAULT_ALIGNMENT && this.indent == 0
                && NONE_STYLE.equals(this.bulletStyle);
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            LineStyle clone = new LineStyle(this.lineIndex);
            clone.alignment = this.alignment;
            clone.indent = this.indent;
            clone.bulletStyle = this.bulletStyle;
            return clone;
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(50);
        sb.append("LineStyle{lineIndex="); //$NON-NLS-1$
        sb.append(lineIndex);
        sb.append(",alignment="); //$NON-NLS-1$
        switch (alignment) {
        case SWT.CENTER:
            sb.append("center"); //$NON-NLS-1$
            break;
        case SWT.RIGHT:
            sb.append("right"); //$NON-NLS-1$
            break;
        default:
            sb.append("left"); //$NON-NLS-1$
        }
        sb.append(",indent="); //$NON-NLS-1$
        sb.append(indent);
        sb.append(",bulletStyle="); //$NON-NLS-1$
        sb.append(bulletStyle);
        sb.append("}"); //$NON-NLS-1$
        return sb.toString();
    }

}