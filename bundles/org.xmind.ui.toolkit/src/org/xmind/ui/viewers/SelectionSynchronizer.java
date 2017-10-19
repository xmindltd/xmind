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

package org.xmind.ui.viewers;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;

/**
 * @author Frank Shaka
 * 
 */
public class SelectionSynchronizer {

    private class SelectionMonitor implements ISelectionChangedListener {

        private ISelectionProvider source;

        /**
         * 
         */
        public SelectionMonitor(ISelectionProvider source) {
            this.source = source;
            this.source.addSelectionChangedListener(this);
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged
         * (org.eclipse.jface.viewers.SelectionChangedEvent)
         */
        public void selectionChanged(SelectionChangedEvent event) {
            syncSelection(source, event);
        }

        /**
         * @return the source
         */
        public ISelectionProvider getSource() {
            return source;
        }

        public void dispose() {
            this.source.removeSelectionChangedListener(this);
        }

    }

    private Map<ISelectionProvider, SelectionMonitor> monitors = new HashMap<ISelectionProvider, SelectionMonitor>();

    private boolean syncing = false;

    public void addPrimary(ISelectionProvider source) {
        add(source, true);
    }

    public void add(ISelectionProvider source) {
        add(source, false);
    }

    private void add(ISelectionProvider source, boolean primary) {
        if (monitors.containsKey(source))
            return;
        monitors.put(source, new SelectionMonitor(source));
        if (!monitors.isEmpty()) {
            if (primary) {
                ISelection selection = source.getSelection();
                for (ISelectionProvider target : monitors.keySet()) {
                    target.setSelection(selection);
                }
            } else {
                source.setSelection(monitors.keySet().iterator().next()
                        .getSelection());
            }
        }
    }

    public void remove(ISelectionProvider source) {
        SelectionMonitor monitor = monitors.remove(source);
        if (monitor != null) {
            monitor.dispose();
        }
    }

    private void syncSelection(ISelectionProvider source,
            SelectionChangedEvent event) {
        if (syncing)
            return;

        syncing = true;
        ISelection selection = event.getSelection();
        for (Object monitor : monitors.values().toArray()) {
            syncSelection(source, selection, (SelectionMonitor) monitor);
        }
        syncing = false;
    }

    /**
     * @param source
     * @param event
     * @param selectionMonitor
     */
    private void syncSelection(ISelectionProvider source, ISelection selection,
            SelectionMonitor monitor) {
        if (monitor.getSource() == source)
            return;
        monitor.getSource().setSelection(selection);
    }

    /**
     * 
     */
    public void clear() {
        for (Object source : monitors.keySet().toArray()) {
            remove((ISelectionProvider) source);
        }
    }
}
