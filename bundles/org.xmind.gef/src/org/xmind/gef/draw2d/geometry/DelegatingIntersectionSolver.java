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
package org.xmind.gef.draw2d.geometry;

public abstract class DelegatingIntersectionSolver extends
        DelegatingPositionSolver implements IIntersectionSolver {

    public int getSpacing() {
        if (getDelegate() instanceof IIntersectionSolver)
            return ((IIntersectionSolver) getDelegate()).getSpacing();
        return 0;
    }

    public void setSpacing(int spacing) {
        if (getDelegate() instanceof IIntersectionSolver)
            ((IIntersectionSolver) getDelegate()).setSpacing(spacing);
    }

}