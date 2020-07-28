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
package org.xmind.ui.gallery;

import org.eclipse.draw2d.IFigure;
import org.xmind.gef.event.MouseEvent;
import org.xmind.ui.texteditor.FloatingTextEditTool;
import org.xmind.ui.texteditor.FloatingTextEditor;
import org.xmind.ui.texteditor.FloatingTextEditorHelper;

public abstract class GalleryEditTool extends FloatingTextEditTool {

    private FloatingTextEditorHelper helper = null;

    public GalleryEditTool() {
    }

    public GalleryEditTool(boolean listensToSelectionChange) {
        super(listensToSelectionChange);
    }

    protected void hookEditor(FloatingTextEditor editor) {
        super.hookEditor(editor);
        if (helper == null) {
            helper = new FloatingTextEditorHelper(true);
        }
        helper.setEditor(editor);
        helper.setViewer(getTargetViewer());
        helper.setFigure(getTitleFigure());
        helper.activate();
    }

    protected FloatingTextEditorHelper getHelper() {
        return helper;
    }

    protected IFigure getTitleFigure() {
        if (getSource() instanceof FramePart) {
            return ((FramePart) getSource()).getFigure().getTitle();
        }
        return getSource().getFigure();
    }

    protected void unhookEditor(FloatingTextEditor editor) {
        if (helper != null) {
            helper.deactivate();
        }
        super.unhookEditor(editor);
    }

    protected String getRedoLabel() {
        return null;
    }

    protected String getUndoLabel() {
        return null;
    }

    protected boolean shouldFinishOnMouseDown(MouseEvent me) {
        if (me.target == getSource() && me.target instanceof FramePart) {
            if (!((FramePart) me.target).getFigure().getTitle().containsPoint(
                    me.cursorLocation))
                return true;
        }
        return super.shouldFinishOnMouseDown(me);
    }

}