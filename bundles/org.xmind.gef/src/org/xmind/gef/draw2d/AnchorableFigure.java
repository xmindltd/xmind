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

import java.util.ArrayList;
import java.util.List;

public class AnchorableFigure extends ReferencedFigure implements
        IAnchorableFigure {

    private IAnchor anchor = null;

    private List<IAnchorableFigureListener> ancListners = null;

    public IAnchor getAnchor() {
        if (anchor == null)
            anchor = createAnchor();
        return anchor;
    }

    protected IAnchor createAnchor() {
        return new ChopBoxAnchor(this);
    }

    public void setAnchor(IAnchor anchor) {
        if (anchor == null)
            anchor = createAnchor();

        IAnchor oldAnchor = this.anchor;
        if (anchor == oldAnchor)
            return;

        this.anchor = anchor;
        fireAnchorChanged(oldAnchor, anchor);
        revalidate();
    }

    public void addAnchorFigureListener(IAnchorableFigureListener listener) {
        if (ancListners == null)
            ancListners = new ArrayList<IAnchorableFigureListener>();
        ancListners.add(listener);
    }

    public void removeAnchorFigureListener(IAnchorableFigureListener listener) {
        if (ancListners == null)
            return;
        ancListners.remove(listener);
    }

    protected void fireAnchorChanged(IAnchor oldAnchor, IAnchor newAnchor) {
        if (ancListners == null)
            return;
        for (Object l : ancListners.toArray()) {
            ((IAnchorableFigureListener) l).anchorChanged(this, oldAnchor,
                    newAnchor);
        }
    }

}