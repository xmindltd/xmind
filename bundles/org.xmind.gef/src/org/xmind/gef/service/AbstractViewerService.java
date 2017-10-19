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
package org.xmind.gef.service;

import org.eclipse.swt.widgets.Control;
import org.xmind.gef.Disposable;
import org.xmind.gef.IViewer;

public abstract class AbstractViewerService extends Disposable implements
        IViewerService {

    private IViewer viewer;

    private boolean active = false;

    private Control control;

    public AbstractViewerService(IViewer viewer) {
        this.viewer = viewer;
    }

    public IViewer getViewer() {
        return viewer;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        if (active == this.active)
            return;

        this.active = active;
        if (active) {
            if (getControl() != null) {
                hookControl(getControl());
            }
            activate();
        } else {
            deactivate();
            if (getControl() != null) {
                unhookControl(getControl());
            }
        }
    }

    protected abstract void activate();

    protected abstract void deactivate();

    public void setControl(Control viewerControl) {
        if (viewerControl == this.control)
            return;

        if (this.control != null && isActive())
            unhookControl(this.control);
        this.control = viewerControl;
        if (this.control != null && isActive())
            hookControl(this.control);
    }

    public Control getControl() {
        return control;
    }

    protected void hookControl(Control control) {
//        // may override
    }

    protected void unhookControl(Control control) {
        // may override
    }

    public void dispose() {
        setControl(null);
        setActive(false);
        super.dispose();
    }

    public void inputChanged(Object oldInput, Object newInput) {
    }

}