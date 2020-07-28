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
package org.xmind.ui.internal.tools;

import java.util.Arrays;

import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.xmind.core.ITitled;
import org.xmind.gef.GEF;
import org.xmind.gef.Request;
import org.xmind.gef.part.IGraphicalEditPart;
import org.xmind.gef.part.IPart;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.texteditor.FloatingTextEditor;
import org.xmind.ui.tools.MindMapEditToolBase;
import org.xmind.ui.util.MindMapUtils;

/**
 * @author Frank Shaka
 */
public class SheetTitleEditTool extends MindMapEditToolBase {

    private Composite textEditorParent;

    private Rectangle textEditorBounds;

    /**
     *
     */
    public SheetTitleEditTool() {
        super();
        this.textEditorParent = null;
        this.textEditorBounds = null;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.xmind.ui.tools.MindMapEditToolBase#getInitialText(org.xmind.gef.part.
     * IPart)
     */
    @Override
    protected String getInitialText(IPart source) {
        if (source != null) {
            ITitled titled = MindMapUIPlugin.getAdapter(
                    MindMapUtils.getRealModel(source), ITitled.class);
            if (titled != null) {
                return titled.getTitleText();
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.xmind.ui.tools.MindMapEditToolBase#createTextRequest(org.xmind.gef.
     * part.IPart, org.eclipse.jface.text.IDocument)
     */
    @Override
    protected Request createTextRequest(IPart source, IDocument document) {
        IGraphicalEditPart sourcePart = getSource();
        if (sourcePart == null)
            return null;

        String newValue = document.get();
        if (newValue == null || "".equals(newValue)) //$NON-NLS-1$
            return null;

        return new Request(GEF.REQ_MODIFY).setViewer(getTargetViewer())
                .setParameter(GEF.PARAM_TEXT, newValue)
                .setTargets(Arrays.asList(sourcePart));
    }

    public void setTextEditorParameters(Composite parent, Rectangle bounds) {
        this.textEditorParent = parent;
        this.textEditorBounds = bounds;
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.ui.texteditor.FloatingTextEditTool#createEditor()
     */
    @Override
    protected FloatingTextEditor createEditor() {
        int style = SWT.BORDER | SWT.V_SCROLL
                | (isMultilineAllowed() ? SWT.MULTI : SWT.SINGLE);
        if (isWrapAllowed()) {
            style |= SWT.WRAP;
        } else {
            style |= SWT.H_SCROLL;
        }

        Composite parent = textEditorParent == null
                ? getTargetViewer().getCanvas() : textEditorParent;
        FloatingTextEditor editor = new FloatingTextEditor(parent, style);

        if (textEditorBounds != null) {
            editor.setInitialLocation(
                    new Point(textEditorBounds.x, textEditorBounds.y));
            editor.setInitialSize(
                    new Point(textEditorBounds.width, textEditorBounds.height));
        }

        return editor;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.xmind.gef.tool.EditTool#shouldUpdateSelectionOnEdit(org.xmind.gef.
     * part.IGraphicalEditPart, org.xmind.gef.Request)
     */
    @Override
    protected boolean shouldUpdateSelectionOnEdit(IGraphicalEditPart newSource,
            Request request) {
        return false;
    }

}
