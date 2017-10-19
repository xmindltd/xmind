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

import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.util.EventListenerSupport;
import org.xmind.gef.util.IEventDispatcher;

/**
 * @author Frank Shaka
 * 
 */
public abstract class BaseRevealService extends GraphicalViewerService
        implements IRevealService {

    private EventListenerSupport listeners = new EventListenerSupport();

    /**
     * @param viewer
     */
    public BaseRevealService(IGraphicalViewer viewer) {
        super(viewer);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.gef.service.IRevealService#addRevealServiceListener(org.xmind
     * .gef.service.IRevealServiceListener)
     */
    public void addRevealServiceListener(IRevealServiceListener listener) {
        listeners.addListener(IRevealServiceListener.class, listener);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.gef.service.IRevealService#removeRevealServiceListener(org.
     * xmind.gef.service.IRevealServiceListener)
     */
    public void removeRevealServiceListener(IRevealServiceListener listener) {
        listeners.removeListener(IRevealServiceListener.class, listener);
    }

    protected void revealingStarted(final RevealEvent event) {
        listeners.fireEvent(IRevealServiceListener.class,
                new IEventDispatcher() {
                    public void dispatch(Object listener) {
                        ((IRevealServiceListener) listener)
                                .revealingStarted(event);
                    }
                });
    }

    protected void revealingCanceled(final RevealEvent event) {
        listeners.fireEvent(IRevealServiceListener.class,
                new IEventDispatcher() {
                    public void dispatch(Object listener) {
                        ((IRevealServiceListener) listener)
                                .revealingCanceled(event);
                    }
                });
    }

    protected void revealingFinished(final RevealEvent event) {
        listeners.fireEvent(IRevealServiceListener.class,
                new IEventDispatcher() {
                    public void dispatch(Object listener) {
                        ((IRevealServiceListener) listener)
                                .revealingFinished(event);
                    }
                });
    }

}
