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

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;

public class StyledTextContentAdapter implements IControlContentAdapter2 {

    public String getControlContents(Control control) {
        return ((StyledText) control).getText();
    }

    public void setControlContents(Control control, String contents,
            int cursorPosition) {
        ((StyledText) control).setText(contents);
        ((StyledText) control).setCaretOffset(cursorPosition);
    }

    public Rectangle getInsertionBounds(Control control) {
        StyledText text = (StyledText) control;
        Point caretOrigin = text.getCaret().getLocation();
        return new Rectangle(caretOrigin.x, caretOrigin.y, 1, text
                .getLineHeight());
    }

    public void insertControlContents(Control control, String contents,
            int cursorPosition) {
        Point selection = ((StyledText) control).getSelection();
        ((StyledText) control).insert(contents);
        // Insert will leave the cursor at the end of the inserted text. If this
        // is not what we wanted, reset the selection.
        if (cursorPosition < contents.length()) {
            ((StyledText) control).setCaretOffset(selection.x + cursorPosition);
        }
    }

    public void setCursorPosition(Control control, int index) {
        ((StyledText) control).setCaretOffset(index);
    }

    public int getCursorPosition(Control control) {
        return ((StyledText) control).getCaretOffset();
    }

    public int getLineAtOffset(Control control, int offset) {
        return ((StyledText) control).getLineAtOffset(offset);
    }

    public int getLineStartOffset(Control control, int lineIndex) {
        return ((StyledText) control).getOffsetAtLine(lineIndex);
    }

    public int getLineEndOffset(Control control, int lineIndex) {
        return ((StyledText) control).getOffsetAtLine(lineIndex)
                + ((StyledText) control).getLine(lineIndex).length();
    }

    public int getLineHeightAtOffset(Control control, int offset) {
        return ((StyledText) control).getLineHeight(offset);
    }

    public Point getLocationAtOffset(Control control, int offset) {
        return ((StyledText) control).getLocationAtOffset(offset);
    }

    public int getOffsetAtLocation(Control control, Point point) {
        return ((StyledText) control).getOffsetAtLocation(point);
    }

    public String getControlContents(Control control, int start, int length) {
        return ((StyledText) control).getTextRange(start, length);
    }

    public void replaceControlContents(Control control, int start, int length,
            String newText) {
        ((StyledText) control).replaceTextRange(start, length, newText);
    }

    public Rectangle getTextBounds(Control control, int start, int length) {
        return ((StyledText) control).getTextBounds(start, start + length - 1);
    }

}