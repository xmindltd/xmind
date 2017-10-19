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

package org.xmind.gef.event;

import org.eclipse.draw2d.SWTEventDispatcher;
import org.eclipse.swt.graphics.Cursor;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.dnd.IDndSupport;

/**
 * @author Frank Shaka
 * 
 */
public abstract class ViewerEventDispatcher extends SWTEventDispatcher {

    private final IGraphicalViewer viewer;

    private IDndSupport dndSupport = null;

    private Cursor overridingCursor = null;

    private boolean active = false;

    /**
     * 
     */
    public ViewerEventDispatcher(IGraphicalViewer viewer) {
        this.viewer = viewer;
    }

    /**
     * @return the viewer
     */
    public IGraphicalViewer getViewer() {
        return viewer;
    }

    /**
     * Activates this event dispatcher. If already activated, does nothing.
     */
    public final void activate() {
        if (isActive())
            return;

        this.active = true;
        onActivated();
    }

    /**
     * Deactivate this event dispatcher. If already deactivated, does nothing.
     */
    public final void deactivate() {
        if (!isActive())
            return;

        this.active = false;
        onDeactivated();
    }

    /**
     * For subclasses to extend.
     */
    protected void onActivated() {
    }

    /**
     * For subclasses to extend.
     */
    protected void onDeactivated() {
    }

    /**
     * Determines if this event dispatcher has been activated.
     * 
     * @return <code>true</code> if this event dispatcher is activated
     */
    public final boolean isActive() {
        return this.active;
    }

    public void updateToolTip() {
    }

    public void setOverridingCursor(Cursor cursor) {
        if (cursor == this.overridingCursor)
            return;
        this.overridingCursor = cursor;
        if (cursor == null) {
            updateCursor();
        } else {
            setCursor(cursor);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.draw2d.SWTEventDispatcher#setCursor(org.eclipse.swt.graphics
     * .Cursor)
     */
    @Override
    protected void setCursor(Cursor c) {
        if (overridingCursor != null) {
            c = overridingCursor;
        }
        super.setCursor(c);
    }

    /**
     * @param dndSupport
     *            the dndSupport to set
     */
    public void setDndSupport(IDndSupport dndSupport) {
        if (dndSupport == this.dndSupport)
            return;
        IDndSupport oldDndSupport = getDndSupport();
        this.dndSupport = dndSupport;
        dndSupportChanged(oldDndSupport, getDndSupport());
    }

    protected IDndSupport getDndSupport() {
        return this.dndSupport;
    }

    protected void dndSupportChanged(IDndSupport oldDndSupport,
            IDndSupport newDndSupport) {
    }

}
