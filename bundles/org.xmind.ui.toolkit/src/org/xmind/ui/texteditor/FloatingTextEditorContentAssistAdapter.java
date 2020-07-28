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

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.xmind.ui.viewers.SWTUtils;

/**
 * @author Frank Shaka
 */
public class FloatingTextEditorContentAssistAdapter extends
        ContentProposalAdapter {

    private FloatingTextEditor editor;

    public FloatingTextEditorContentAssistAdapter(FloatingTextEditor editor,
            IContentProposalProvider proposalProvider, KeyStroke keyStroke,
            char[] autoActivationCharacters) {
        super(editor.getTextViewer().getTextWidget(),
                new StyledTextContentAdapter(), proposalProvider, keyStroke,
                autoActivationCharacters);
        this.editor = editor;
        editor.addVerifyKeyListener(new VerifyKeyListener() {
            public void verifyKey(VerifyEvent event) {
                int stateMask = event.stateMask;
                int keyCode = event.keyCode;
                if (SWTUtils.matchKey(stateMask, keyCode, 0, SWT.CR)) {
                    if (isProposalPopupOpened()) {
                        event.doit = false;
                    }
                } else if (SWTUtils.matchKey(stateMask, keyCode, 0, SWT.ESC)) {
                    if (isProposalPopupOpened()) {
                        event.doit = false;
                        closeProposalPopup();
                    }
                }
            }
        });
    }

    public FloatingTextEditorContentAssistAdapter(FloatingTextEditor editor,
            IContentProposalProvider proposalProvider) {
        this(editor, proposalProvider, null, null);
    }

    public FloatingTextEditor getEditor() {
        return editor;
    }

}