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
package org.xmind.ui.internal.figures;

import org.xmind.gef.draw2d.DecoratedConnectionFigure;
import org.xmind.gef.draw2d.IAnchor;
import org.xmind.gef.draw2d.decoration.IConnectionDecorationEx;
import org.xmind.ui.decorations.ISummaryDecoration;

public class SummaryFigure extends DecoratedConnectionFigure {

    private IAnchor conclusionAnchor = null;

    public ISummaryDecoration getDecoration() {
        return (ISummaryDecoration) super.getDecoration();
    }

    public IAnchor getConclusionAnchor() {
        return conclusionAnchor;
    }

    public void setConclusionAnchor(IAnchor anchor) {
        if (anchor == this.conclusionAnchor)
            return;

        if (this.conclusionAnchor != null) {
            unhookConclusionAnchor(this.conclusionAnchor);
        }
        this.conclusionAnchor = anchor;
        if (anchor != null) {
            hookConclusionAnchor(anchor);
        }
        if (getDecoration() != null) {
            getDecoration().setNodeAnchor(this, anchor);
        }
        revalidate();
        repaint();
    }

    protected void hookConclusionAnchor(IAnchor anchor) {
        anchor.addAnchorListener(this);
    }

    protected void unhookConclusionAnchor(IAnchor anchor) {
        anchor.removeAnchorListener(this);
    }

    @Override
    protected void connectionAdded(IConnectionDecorationEx connection) {
        super.connectionAdded(connection);
        if (connection instanceof ISummaryDecoration) {
            ((ISummaryDecoration) connection).setNodeAnchor(this,
                    getConclusionAnchor());
        }
    }

    @Override
    protected void connectionRemoved(IConnectionDecorationEx connection) {
        super.connectionRemoved(connection);
        if (connection instanceof ISummaryDecoration) {
            ((ISummaryDecoration) connection).setNodeAnchor(this, null);
        }
    }

}