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
package org.xmind.gef.ui.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;

public abstract class PageContainerPresentationBase implements
        IPageContainerPresentation {

    private List<IPageChangedListener> pageChangedListeners = null;

    public void addPageChangedListener(IPageChangedListener listener) {
        if (listener == null)
            return;
        if (pageChangedListeners == null)
            pageChangedListeners = new ArrayList<IPageChangedListener>();
        pageChangedListeners.add(listener);
    }

    protected boolean hasListener() {
        return pageChangedListeners != null && !pageChangedListeners.isEmpty();
    }

    public void removePageChangedListener(IPageChangedListener listener) {
        if (pageChangedListeners == null)
            return;
        pageChangedListeners.remove(listener);
    }

    protected void firePageChangedEvent() {
        if (pageChangedListeners == null)
            return;
        PageChangedEvent event = new PageChangedEvent(this, getSelectedPage());
        for (Object listener : pageChangedListeners.toArray()) {
            ((IPageChangedListener) listener).pageChanged(event);
        }
    }

}