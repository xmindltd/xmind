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

import org.eclipse.core.runtime.IAdaptable;
import org.xmind.gef.IViewer;
import org.xmind.ui.internal.TopicContextService;
import org.xmind.ui.mindmap.ITopicPart;

/**
 * @author frankshaka
 * 
 */
public class TopicContext implements IAdaptable {

    private ITopicPart topicPart;

    private TopicContextService service;

    /**
     * 
     */
    public TopicContext(ITopicPart topicPart) {
        this.topicPart = topicPart;
        this.service = findService();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    /**
     * @return
     */
    private TopicContextService findService() {
        IViewer viewer = topicPart.getSite().getViewer();
        if (viewer != null) {
            return (TopicContextService) viewer
                    .getService(TopicContextService.class);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
        Object o = topicPart.getAdapter(adapter);
        if (o != null)
            return o;

        if (service != null) {
            o = service.getAdapter(adapter);
            if (o != null)
                return o;
        }
        return null;
    }

}
