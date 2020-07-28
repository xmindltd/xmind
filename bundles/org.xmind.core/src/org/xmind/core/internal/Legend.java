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
package org.xmind.core.internal;

import java.util.Collections;
import java.util.Set;

import org.xmind.core.ILegend;
import org.xmind.core.util.Point;

public abstract class Legend extends AbstractWorkbookComponent
        implements ILegend {

    protected static final Set<String> NO_MARKER_IDS = Collections.emptySet();

    /**
     * @see org.xmind.core.ITopic#setPosition(org.xmind.core.util.Point)
     */
    public void setPosition(Point position) {
        if (position == null)
            removePosition();
        else
            setPosition(position.x, position.y);
    }

    /**
     * @see org.xmind.core.ITopic#hasPosition()
     */
    public boolean hasPosition() {
        return getPosition() != null;
    }

    protected abstract void removePosition();
}