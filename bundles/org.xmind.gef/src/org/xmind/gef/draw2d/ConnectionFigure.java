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
package org.xmind.gef.draw2d;

import org.eclipse.draw2d.Figure;

public abstract class ConnectionFigure extends Figure implements
        IAnchorListener, IConnectionFigure {

    private IAnchor sourceAnchor;

    private IAnchor targetAnchor;

    public ConnectionFigure() {
    }

    public ConnectionFigure(IAnchor sourceAnchor, IAnchor targetAnchor) {
        setSourceAnchor(sourceAnchor);
        setTargetAnchor(targetAnchor);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.gef.draw2d.IConnectionFigure#getSourceAnchor()
     */
    public IAnchor getSourceAnchor() {
        return sourceAnchor;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.gef.draw2d.IConnectionFigure#getTargetAnchor()
     */
    public IAnchor getTargetAnchor() {
        return targetAnchor;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.gef.draw2d.IConnectionFigure#setSourceAnchor(org.xmind.gef.draw2d.IAnchor)
     */
    public void setSourceAnchor(IAnchor anchor) {
        if (anchor == this.sourceAnchor)
            return;

        if (this.sourceAnchor != null) {
            unhookSourceAnchor(this.sourceAnchor);
        }
        this.sourceAnchor = anchor;
        if (anchor != null) {
            hookSourceAnchor(anchor);
        }
        revalidate();
        repaint();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.gef.draw2d.IConnectionFigure#setTargetAnchor(org.xmind.gef.draw2d.IAnchor)
     */
    public void setTargetAnchor(IAnchor anchor) {
        if (anchor == this.targetAnchor)
            return;
        if (this.targetAnchor != null) {
            unhookTargetAnchor(this.targetAnchor);
        }
        this.targetAnchor = anchor;
        if (anchor != null) {
            hookTargetAnchor(anchor);
        }
        revalidate();
        repaint();
    }

    protected void unhookSourceAnchor(IAnchor anchor) {
        anchor.removeAnchorListener(this);
    }

    protected void hookSourceAnchor(IAnchor anchor) {
        anchor.addAnchorListener(this);
    }

    protected void unhookTargetAnchor(IAnchor anchor) {
        anchor.removeAnchorListener(this);
    }

    protected void hookTargetAnchor(IAnchor anchor) {
        anchor.addAnchorListener(this);
    }

    public void anchorMoved(IAnchor anchor) {
        if (anchor.getOwner() != this)
            revalidate();
    }

}