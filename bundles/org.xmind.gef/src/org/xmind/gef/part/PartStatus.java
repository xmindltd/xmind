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
package org.xmind.gef.part;

import static org.xmind.gef.GEF.PART_ACTIVE;
import static org.xmind.gef.GEF.PART_FOCUSED;
import static org.xmind.gef.GEF.PART_PRESELECTED;
import static org.xmind.gef.GEF.PART_SELECTED;

import org.xmind.gef.status.StatusMachine2;

/**
 * @author Brian Sun
 */
public class PartStatus extends StatusMachine2 implements IPartStatus {

    private IPart host = null;

    /**
     * @param host
     */
    public PartStatus(IPart host) {
        super(host);
        this.host = host;
    }

    /**
     * @return the host
     */
    public IPart getHost() {
        return host;
    }

    /**
     * @see org.xmind.gef.part.IPartStatus#activate()
     */
    public void activate() {
        if (!isActive())
            setStatus(PART_ACTIVE, true);
    }

    /**
     * @see org.xmind.gef.part.IPartStatus#dePreSelect()
     */
    public void dePreSelect() {
        if (isActive())
            setStatus(PART_PRESELECTED, false);
    }

    /**
     * @see org.xmind.gef.part.IPartStatus#deactivate()
     */
    public void deactivate() {
        if (isActive()) {
            dePreSelect();
            deSelect();
            lostFocus();
            setStatus(PART_ACTIVE, false);
        }
    }

    /**
     * @see org.xmind.gef.part.IPartStatus#deSelect()
     */
    public void deSelect() {
        if (isActive())
            setStatus(PART_SELECTED, false);
    }

    /**
     * @see org.xmind.gef.part.IPartStatus#isActive()
     */
    public boolean isActive() {
        return isStatus(PART_ACTIVE);
    }

    /**
     * @see org.xmind.gef.part.IPartStatus#isFocused()
     */
    public boolean isFocused() {
        return isStatus(PART_FOCUSED);
    }

    /**
     * @see org.xmind.gef.part.IPartStatus#isPreSelected()
     */
    public boolean isPreSelected() {
        return isStatus(PART_PRESELECTED);
    }

    /**
     * @see org.xmind.gef.part.IPartStatus#isSelected()
     */
    public boolean isSelected() {
        return isStatus(PART_SELECTED);
    }

    /**
     * @see org.xmind.gef.part.IPartStatus#lostFocus()
     */
    public void lostFocus() {
        if (isActive())
            setStatus(PART_FOCUSED, false);
    }

    /**
     * @see org.xmind.gef.part.IPartStatus#preSelect()
     */
    public void preSelect() {
        if (isActive())
            setStatus(PART_PRESELECTED, true);
    }

    /**
     * @see org.xmind.gef.part.IPartStatus#select()
     */
    public void select() {
        if (isActive())
            setStatus(PART_SELECTED, true);
    }

    /**
     * @see org.xmind.gef.part.IPartStatus#setFocus()
     */
    public void setFocus() {
        if (isActive())
            setStatus(PART_FOCUSED, true);
    }

}