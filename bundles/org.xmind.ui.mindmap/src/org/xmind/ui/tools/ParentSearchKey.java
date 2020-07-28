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

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.xmind.gef.draw2d.IReferencedFigure;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.ui.mindmap.IBranchPart;

public class ParentSearchKey {

    private IGraphicalPart host;

    private IBranchPart feedback;

    private IReferencedFigure figure;

    private Point cursorPos;

    private IFigure invent;

    public ParentSearchKey(IGraphicalPart host, IReferencedFigure figure) {
        this.host = host;
        this.figure = figure;
        this.cursorPos = figure.getReference();
    }

    public ParentSearchKey(IGraphicalPart host, IReferencedFigure figure,
            Point cursorPos) {
        this.host = host;
        this.figure = figure;
        this.cursorPos = cursorPos;
    }

    public Point getCursorPos() {
        return cursorPos;
    }

    public IReferencedFigure getFigure() {
        return figure;
    }

    public IGraphicalPart getHost() {
        return host;
    }

    public IBranchPart getFeedback() {
        return feedback;
    }

    public void setFeedback(IBranchPart feedback) {
        this.feedback = feedback;
    }

    public IFigure getInvent() {
        return invent;
    }

    public void setInvent(IFigure invent) {
        this.invent = invent;
    }

}