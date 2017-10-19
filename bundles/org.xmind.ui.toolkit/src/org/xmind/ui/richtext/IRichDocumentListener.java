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

import org.eclipse.swt.custom.StyleRange;

public interface IRichDocumentListener {

    void textStyleChanged(IRichDocument document, StyleRange[] oldTextStyles,
            StyleRange[] newTextStyles);

    void lineStyleChanged(IRichDocument document, LineStyle[] oldLineStyles,
            LineStyle[] newLineStyles);

    void imageChanged(IRichDocument document, ImagePlaceHolder[] oldImages,
            ImagePlaceHolder[] newImages);

    void hyperlinkChanged(IRichDocument document, Hyperlink[] oldHyperlinks,
            Hyperlink[] newHyperlinks);

}