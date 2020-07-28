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
import org.eclipse.jface.viewers.IInputSelectionProvider;
import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;

/**
 * @author Frank Shaka
 */
public interface IRichTextEditViewer extends IInputSelectionProvider,
        IPostSelectionProvider {

    int DEFAULT_CONTROL_STYLE = SWT.MULTI | SWT.WRAP | SWT.H_SCROLL
            | SWT.V_SCROLL;

    Control getControl();

    Control getFocusControl();

    void setInput(Object input);

    void setDocument(IRichDocument document);

    IRichDocument getDocument();

    IRichTextRenderer getRenderer();

    void refresh();

    TextViewer getTextViewer();

//    public static final int ACTION_FONT = 1;
//
//    public static final int ACTION_SIZE_UP_DOWN = 1 << 1;
//
//    public static final int ACTION_BOLD = 1 << 2;
//
//    public static final int ACTION_ITALIC = 1 << 3;
//
//    public static final int ACTION_UNDERLINE = 1 << 4;
//
//    public static final int ACTION_STRIKETHROUGH = 1 << 5;
//
//    public static final int ACTION_ALIGN_GROUP = 1 << 6;
//
//    public static final int ACTION_INDENT_OUTDENT = 1 << 7;
//
//    public static final int ACTION_COLORS = 1 << 8;
//
//    public static final int ACTION_CLEAR_STYLE = 1 << 10;
//
//    public static final int INFO_TITLE = 1 << 11;
//
//    public static final int SIMPLE_COLOR_CHOOSER = 1 << 12;
//
//    public static final int ALL_VIEWER_STYLE = ACTION_FONT
//            | ACTION_SIZE_UP_DOWN | ACTION_BOLD | ACTION_ITALIC
//            | ACTION_UNDERLINE | ACTION_STRIKETHROUGH | ACTION_ALIGN_GROUP
//            | ACTION_INDENT_OUTDENT | ACTION_COLORS;

//    IRichDocumentProvider getDocumentProvider();
//
//    void setDocumentProvider(IRichDocumentProvider documentProvider);

//    IBaseLabelProvider getTitleLabelProvider();
//
//    void setTitleLabelProvider(IBaseLabelProvider labelProvider);

//    void setTextStyleProvider(ITextStyleProvider textStyleProvider);

//    ITextStyleProvider getTextStyleProvider();

//    void fill(IMenuManager menuManager);

}