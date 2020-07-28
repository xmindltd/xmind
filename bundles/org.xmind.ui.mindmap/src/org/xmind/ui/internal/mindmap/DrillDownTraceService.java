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
package org.xmind.ui.internal.mindmap;

import java.util.ArrayList;
import java.util.List;

import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.service.GraphicalViewerService;
import org.xmind.ui.mindmap.IDrillDownTraceListener;
import org.xmind.ui.mindmap.IDrillDownTraceService;
import org.xmind.ui.mindmap.IMindMap;

public class DrillDownTraceService extends GraphicalViewerService
        implements IDrillDownTraceService {

    private List<ITopic> centralTopics = new ArrayList<ITopic>();

    private List<IDrillDownTraceListener> listeners = null;

    public DrillDownTraceService(IGraphicalViewer viewer) {
        super(viewer);
    }

    protected void activate() {
    }

    protected void deactivate() {
    }

    @Override
    public void init(List<ITopic> centralTopics) {
        if (centralTopics != null && centralTopics.size() != 0) {
            this.centralTopics.addAll(centralTopics);
        }
    }

    public List<ITopic> getCentralTopics() {
        checkEmpty();
        return centralTopics;
    }

    public ITopic getCurrentCentralTopic() {
        checkEmpty();
        if (centralTopics.isEmpty())
            return (ITopic) getViewer().getAdapter(ITopic.class);
        return centralTopics.get(centralTopics.size() - 1);
    }

    public boolean canDrillUp() {
        checkEmpty();
        return centralTopics.size() >= 2;
    }

    public ITopic getPreviousCentralTopic() {
        checkEmpty();
        int size = centralTopics.size();
        if (size >= 2)
            return centralTopics.get(size - 2);
        return null;
    }

    public void setCentralTopic(ITopic topic) {
        if (topic == null) {
            centralTopics.clear();
            checkEmpty();
            fireHistoryChanged();
        } else {
            boolean changed = checkEmpty();
            for (int i = 0; i < centralTopics.size(); i++) {
                ITopic t = centralTopics.get(i);
                if (t.equals(topic)) {
                    while (centralTopics.size() > i + 1) {
                        ITopic removed = centralTopics
                                .remove(centralTopics.size() - 1);
                        changed |= removed != null;
                    }
                    if (changed)
                        fireHistoryChanged();
                    return;
                }
            }
            centralTopics.add(topic);
            fireHistoryChanged();
        }
    }

    private boolean checkEmpty() {
        if (centralTopics.isEmpty()) {
            ITopic rootTopic = (ITopic) getViewer().getAdapter(ITopic.class);
            if (rootTopic != null) {
                centralTopics.add(rootTopic);
                return true;
            }
        }
        return false;
    }

    public void addTraceListener(IDrillDownTraceListener listener) {
        if (listener == null)
            return;
        if (listeners == null)
            listeners = new ArrayList<IDrillDownTraceListener>();
        listeners.add(listener);
    }

    public void removeTraceListener(IDrillDownTraceListener listener) {
        if (listener == null || listeners == null)
            return;
        listeners.remove(listener);
        if (listeners.isEmpty())
            listeners = null;
    }

    protected void fireHistoryChanged() {
        if (listeners == null || listeners.isEmpty())
            return;
        for (Object o : listeners.toArray()) {
            ((IDrillDownTraceListener) o).traceChanged(this);
        }
    }

    public void inputChanged(Object oldInput, Object newInput) {
        if (newInput instanceof IMindMap) {
            setCentralTopic(((IMindMap) newInput).getCentralTopic());
        } else if (newInput instanceof ISheet) {
            setCentralTopic(((ISheet) newInput).getRootTopic());
        } else if (newInput instanceof ITopic) {
            setCentralTopic((ITopic) newInput);
        } else {
            setCentralTopic(null);
        }
    }
}
