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

import org.xmind.gef.part.IGraphicalPart;
import org.xmind.ui.mindmap.IConnectionPart;
import org.xmind.ui.mindmap.INodePart;

public abstract class NodePart extends MindMapPartBase implements INodePart {

    private List<IConnectionPart> sourceConnections = null;

    private List<IConnectionPart> targetConnections = null;

    public List<IConnectionPart> getSourceConnections() {
        if (sourceConnections == null) {
            sourceConnections = initSourceConnections();
        }
        return sourceConnections;
    }

    protected List<IConnectionPart> initSourceConnections() {
        ArrayList<IConnectionPart> list = new ArrayList<IConnectionPart>();
        fillSourceConnections(list);
        return list;
    }

    protected abstract void fillSourceConnections(List<IConnectionPart> list);

    public void addSourceConnection(IConnectionPart connection) {
        if (getSourceConnections().contains(connection))
            return;
        getSourceConnections().add(connection);
    }

    public void removeSourceConnection(IConnectionPart connection) {
        if (getSourceConnections().contains(connection)) {
            getSourceConnections().remove(connection);
        }
    }

    public List<IConnectionPart> getTargetConnections() {
        if (targetConnections == null) {
            targetConnections = initTargetConnections();
        }
        return targetConnections;
    }

    private List<IConnectionPart> initTargetConnections() {
        ArrayList<IConnectionPart> list = new ArrayList<IConnectionPart>();
        fillTargetConnections(list);
        return list;
    }

    protected abstract void fillTargetConnections(List<IConnectionPart> list);

    public void addTargetConnection(IConnectionPart connection) {
        if (getTargetConnections().contains(connection))
            return;
        getTargetConnections().add(connection);
    }

    public void removeTargetConnection(IConnectionPart connection) {
        if (getTargetConnections().contains(connection)) {
            getTargetConnections().remove(connection);
        }
    }

    public void addNotify() {
        super.addNotify();
        refreshConnections();
    }

    public void removeNotify() {
        super.removeNotify();
        refreshConnections();
    }

    public void refresh() {
        super.refresh();
        refreshConnections();
    }

    protected void refreshConnections() {
        refreshSourceConnections();
        refreshTargetConnections();
    }

    protected void refreshSourceConnections() {
        for (Object p : getSourceConnections().toArray()) {
            ((IGraphicalPart) p).refresh();
        }
    }

    protected void refreshTargetConnections() {
        for (Object p : getTargetConnections().toArray()) {
            ((IGraphicalPart) p).refresh();
        }
    }

    public void update() {
        super.update();
        for (Object p : getSourceConnections().toArray()) {
            ((IGraphicalPart) p).update();
        }
        for (Object p : getTargetConnections().toArray()) {
            ((IGraphicalPart) p).update();
        }
    }

}