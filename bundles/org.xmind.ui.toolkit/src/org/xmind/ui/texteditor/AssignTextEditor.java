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
package org.xmind.ui.texteditor;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

/**
 * 
 * @author Karelun Huang
 */

public class AssignTextEditor extends FloatingTextEditor {

    private TextViewer textViewer;

    public AssignTextEditor(Composite parent) {
        super(parent);
        open(true);
    }

    @Override
    protected ITextViewer createTextViewer(Composite parent, int style) {
        int s = style & SWT.BORDER;
        if (s == 0)
            style |= SWT.BORDER;
        textViewer = new TextViewer(parent, style) {
            protected int getEmptySelectionChangedEventDelay() {
                return 300;
            }
        };
        textViewer.getControl().setLayoutData(
                new GridData(GridData.FILL, GridData.FILL, true, false));
        return textViewer;
    }

    public void setTextContents(String content) {
        Document document = new Document(content);
        setInput(document);
    }

    public void setForeground(Color color) {
        StyledText styledText = textViewer.getTextWidget();
        styledText.setForeground(color);
    }

    public void setEnabled(boolean enabled) {
        StyledText styledText = textViewer.getTextWidget();
        styledText.setEnabled(enabled);
    }

    @Override
    public boolean close(boolean finish) {
        return false;
    }

}
