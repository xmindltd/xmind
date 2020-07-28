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

import org.eclipse.jface.text.TextViewer;

/**
 * @author Frank Shaka
 */
public abstract class LineStyleModifier implements ILineStyleModifier {

    private Object value;

    public void setValue(Object value) {
        this.value = value;
    }

    public boolean modify(LineStyle style) {
        return modify(style, value);
    }

    public void updateViewer(TextViewer viewer, int startLine, int lineCount) {
        updateViewer(viewer, startLine, lineCount, value);
    }

    protected abstract boolean modify(LineStyle style, Object value);

    protected abstract void updateViewer(TextViewer viewer, int startLine,
            int lineCount, Object value);
}