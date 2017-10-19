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
package org.xmind.ui.internal.tools;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.IDocument;
import org.xmind.gef.Request;
import org.xmind.gef.part.IGraphicalEditPart;
import org.xmind.gef.part.IPart;
import org.xmind.ui.mindmap.ILegendItemPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.texteditor.FloatingTextEditorHelper;
import org.xmind.ui.tools.TitleEditTool;

public class LegendItemEditTool extends TitleEditTool {

    public LegendItemEditTool() {
        addEditRequestType(MindMapUI.REQ_EDIT_LEGEND_ITEM);
    }

    public void setSource(IGraphicalEditPart source) {
        Assert.isTrue(source instanceof ILegendItemPart);
        super.setSource(source);
    }

    protected String getInitialText(IPart source) {
        return ((ILegendItemPart) source).getDescription();
    }

    protected boolean isMultilineAllowed() {
        return false;
    }

    protected boolean isWrapAllowed() {
        return false;
    }

    protected FloatingTextEditorHelper createHelper() {
        FloatingTextEditorHelper helper = super.createHelper();
        helper.setExtendsBidirectionalHorizontal(false);
        helper.setExpansion(20);
        return helper;
    }

    protected Request createTextRequest(IPart source, IDocument document) {
        return super.createTextRequest(source, document).setPrimaryTarget(
                getSource());
    }
}