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
package org.xmind.ui.tools;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.xmind.gef.GEF;
import org.xmind.gef.Request;
import org.xmind.gef.event.KeyEvent;
import org.xmind.gef.event.MouseEvent;
import org.xmind.gef.part.IPart;
import org.xmind.ui.commands.CommandMessages;
import org.xmind.ui.internal.spellsupport.SpellingSupport;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.texteditor.FloatingTextEditTool;
import org.xmind.ui.texteditor.FloatingTextEditor;
import org.xmind.ui.util.MindMapUtils;

public abstract class MindMapEditToolBase extends FloatingTextEditTool {

    protected boolean finishedOnMouseDown = false;

    protected String oldText = null;

    public MindMapEditToolBase() {
        setContextId(MindMapUI.CONTEXT_MINDMAP_TEXTEDIT);
    }

    protected IDocument getTextContents(IPart source) {
        oldText = getInitialText(source);
        return oldText == null ? null : new Document(oldText);
    }

    protected void hookEditorControl(FloatingTextEditor editor,
            ITextViewer textViewer) {
        super.hookEditorControl(editor, textViewer);
        SpellingSupport.getInstance().install(textViewer);
    }

    protected abstract String getInitialText(IPart source);

    protected void handleTextModified(IPart source, IDocument document) {
        if (finishedOnMouseDown) {
            finishedOnMouseDown = false;
            if (shouldIgnoreTextChange(source, document, oldText))
                return;
        }
        Request request = createTextRequest(source, document);
        if (request != null)
            source.handleRequest(request, GEF.ROLE_MODIFIABLE);
    }

    protected boolean shouldIgnoreTextChange(IPart source, IDocument document,
            String oldText) {
        return document.get().equals(oldText);
    }

    protected abstract Request createTextRequest(IPart source,
            IDocument document);

    protected String getRedoLabel() {
        return CommandMessages.Command_Typing;
    }

    protected String getUndoLabel() {
        return CommandMessages.Command_Typing;
    }

    protected boolean handleKeyDown(KeyEvent ke) {
        if (ke != null && getEditor() != null) {
            if (MindMapUtils.isTopicTextChar(ke.character)
                    || (ke.keyCode == 229 && ke.isImeOpened)) {
                /*
                 * When using an IME, the client may input a long phrase once.
                 * Then the phrase will be splitted into several keyDown events,
                 * coming in one by one, each of which contains a single
                 * character from the phrase. If the text editor is not opened
                 * at first, these events are all sent to the viewer's control
                 * instead of the text editor, even if the first event might
                 * have triggered the opening of the text editor.
                 */
                getEditor().replaceText(Character.toString(ke.character));
                return true;
            }
        }
        return super.handleKeyDown(ke);
    }

    protected boolean shouldFinishOnMouseDown(MouseEvent me) {
        boolean toFinish = super.shouldFinishOnMouseDown(me);
        if (toFinish) {
            finishedOnMouseDown = true;
        }
        return toFinish;
    }

}