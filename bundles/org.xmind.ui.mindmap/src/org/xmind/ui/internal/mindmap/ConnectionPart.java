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

import org.xmind.ui.mindmap.IConnectionPart;
import org.xmind.ui.mindmap.INodePart;

public abstract class ConnectionPart extends MindMapPartBase implements
        IConnectionPart {

    private INodePart source;

    private INodePart target;

    public INodePart getSourceNode() {
        return source;
    }

    public INodePart getTargetNode() {
        return target;
    }

    public void setSourceNode(INodePart node) {
        if (node == this.source)
            return;

        if (this.source != null) {
            this.source.removeSourceConnection(this);
        }
        this.source = node;
        if (node != null) {
            node.addSourceConnection(this);
        }
    }

    public void setTargetNode(INodePart node) {
        if (node == this.target)
            return;

        if (this.target != null) {
            this.target.removeTargetConnection(this);
        }
        this.target = node;
        if (node != null) {
            node.addTargetConnection(this);
        }
    }

    protected void refreshNodes() {
        setSourceNode(findSourceNode());
        setTargetNode(findTargetNode());
    }

    protected abstract INodePart findSourceNode();

    protected abstract INodePart findTargetNode();

    public void refresh() {
        refreshNodes();
        super.refresh();
    }

    public void removeNotify() {
        super.removeNotify();
        setSourceNode(null);
        setTargetNode(null);
    }

}