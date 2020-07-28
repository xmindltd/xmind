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
package org.xmind.ui.internal.findreplace;

import org.eclipse.swt.graphics.Font;
import org.xmind.gef.draw2d.graphics.GraphicsUtils;

/**
 * @author Frank Shaka
 */
public abstract class AbstractFindReplaceOperationProvider implements
        IFindReplaceOperationProvider {

    private int parameter = 0;

    /**
     * @see cn.brainy.ui.mindmap.dialogs.IFindReplaceOperationProvider#find(java.lang.String)
     */
    public boolean find(String toFind) {
        return isAll() ? findAll(toFind) : findNext(toFind);
    }

    /**
     * @see cn.brainy.ui.mindmap.dialogs.IFindReplaceOperationProvider#replace(java.lang.String,
     *      java.lang.String)
     */
    public boolean replace(String toFind, String toReplaceWith) {
        return isAll() ? replaceAll(toFind, toReplaceWith) : replaceNext(
                toFind, toReplaceWith);
    }

    protected abstract boolean findAll(String toFind);

    protected abstract boolean findNext(String toFind);

    protected abstract boolean replaceAll(String toFind, String toReplaceWith);

    protected abstract boolean replaceNext(String toFind, String toReplaceWith);

    /**
     * @param text
     * @param toFind
     * @param offset
     * @return
     */
    protected int indexOf(String text, String toFind, int offset) {
        if (text == null)
            return -1;
        if (!isCaseSensitive()) {
            text = text.toLowerCase();
            toFind = toFind.toLowerCase();
        }
        int index = isForward() ? text.indexOf(toFind, offset) : text
                .lastIndexOf(toFind, offset - toFind.length());
        if (index >= 0 && isWholeWord()
                && !isWholeWord(text, index, toFind.length())) {
            index = -1;
        }
        return index;
    }

    /**
     * @param toFind
     * @param selectionText
     * @return
     */
    protected boolean equals(String toFind, String selectionText) {
        return isCaseSensitive() ? toFind.equals(selectionText) : toFind
                .equalsIgnoreCase(selectionText);
    }

    /**
     * @param text
     * @return
     */
    protected int getNewOffset(String text) {
        return isForward() ? 0 : text.length() + 1;
    }

    protected boolean isIndexPermitted(int newIndex, int startIndex) {
        return isForward() ? newIndex < startIndex : newIndex > startIndex;
    }

    protected boolean isWholeWord(String text, int start, int length) {
        char pre = start == 0 ? ' ' : text.charAt(start - 1);
        char post = start == text.length() - length ? ' ' : text.charAt(start
                + length);
        return pre == ' ' && post == ' ';
    }

    protected boolean isAll() {
        return (getParameter() & PARAM_ALL) != 0;
    }

    protected boolean isForward() {
        return (getParameter() & PARAM_FORWARD) != 0;
    }

    protected boolean isCaseSensitive() {
        return (getParameter() & PARAM_CASE_SENSITIVE) != 0;
    }

    protected boolean isWholeWord() {
        return (getParameter() & PARAM_WHOLE_WORD) != 0;
    }

    protected boolean isWorkbook() {
        return (getParameter() & PARAM_WORKBOOK) != 0;
    }

    /**
     * @see cn.brainy.ui.mindmap.dialogs.IFindReplaceOperationProvider#canFindAll(java.lang.String)
     */
    public boolean canFindAll(String toFind) {
        return canFind(toFind) && !isWorkbook();
    }

    /**
     * @see cn.brainy.ui.mindmap.dialogs.IFindReplaceOperationProvider#canReplaceAll(java.lang.String,
     *      java.lang.String)
     */
    public boolean canReplaceAll(String toFind, String toReplaceWith) {
        return canReplace(toFind, toReplaceWith) && !isWorkbook();
    }

    public int getParameter() {
        return this.parameter;
    }

    public void setParameter(int op, boolean value) {
        if (value)
            this.parameter |= op;
        else
            this.parameter &= ~op;
    }

    public void setParameter(int parameter) {
        this.parameter = parameter;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.ui.internal.findreplace.IFindReplaceOperationProvider#
     * understandsPatameter(int)
     */
    public boolean understandsPatameter(int parameter) {
        return true;
    }

    protected static int computeTextWidth(String text, Font font) {
        return GraphicsUtils.getAdvanced().getTextSize(text, font).width;
    }

    protected static String constrainText(String text, int maxWidth, Font font) {
        if (computeTextWidth(text, font) <= maxWidth)
            return text;

        String head = text.substring(0, Math.max(0, text.length() / 2));
        String trail = text.substring(text.length() / 2 + 1);
        boolean cutHead = true;
        while (head.length() > 0 && trail.length() > 0) {
            text = head + "..." + trail; //$NON-NLS-1$
            if (computeTextWidth(text, font) <= maxWidth)
                return text;

            if (cutHead) {
                head = head.substring(0, head.length() - 1);
            } else {
                trail = trail.substring(1);
            }
            cutHead = !cutHead;
        }
        return text;
    }
}