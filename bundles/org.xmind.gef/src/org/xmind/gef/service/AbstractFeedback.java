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
package org.xmind.gef.service;

import org.xmind.gef.ZoomManager;

public abstract class AbstractFeedback implements IFeedback {

    private ZoomManager zoomManager;

    public void setZoomManager(ZoomManager zoomManager) {
        this.zoomManager = zoomManager;
    }

    protected ZoomManager getZoomManager() {
        return zoomManager;
    }

}