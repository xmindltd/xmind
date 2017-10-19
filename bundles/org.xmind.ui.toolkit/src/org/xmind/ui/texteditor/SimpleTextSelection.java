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

package org.xmind.ui.texteditor;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.ITextSelection;

/**
 * @author Frank Shaka
 * 
 */
public class SimpleTextSelection implements ITextSelection {

    public static final SimpleTextSelection EMPTY = new SimpleTextSelection(""); //$NON-NLS-1$

    private String text;

    /**
     * 
     */
    public SimpleTextSelection(String text) {
        Assert.isNotNull(text);
        this.text = text;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ISelection#isEmpty()
     */
    public boolean isEmpty() {
        return text.isEmpty();
    }

    public String getText() {
        return text;
    }

    public int getOffset() {
        return 0;
    }

    public int getLength() {
        return text.length();
    }

    public int getStartLine() {
        return 0;
    }

    public int getEndLine() {
        return 0;
    }

}
