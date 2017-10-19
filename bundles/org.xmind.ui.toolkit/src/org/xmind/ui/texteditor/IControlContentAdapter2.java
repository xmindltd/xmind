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

import org.eclipse.jface.fieldassist.IControlContentAdapter;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;

public interface IControlContentAdapter2 extends IControlContentAdapter {

    Point getLocationAtOffset(Control control, int offset);

    int getOffsetAtLocation(Control control, Point point);

    int getLineAtOffset(Control control, int offset);

    int getLineHeightAtOffset(Control control, int offset);

    int getLineStartOffset(Control control, int lineIndex);

    int getLineEndOffset(Control control, int lineIndex);

    Rectangle getTextBounds(Control control, int start, int length);

    String getControlContents(Control control, int start, int length);

    void replaceControlContents(Control control, int start, int length,
            String newText);

}