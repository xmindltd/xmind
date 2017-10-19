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
package org.xmind.core.internal;

import java.util.Collections;
import java.util.List;

import org.xmind.core.ISummary;
import org.xmind.core.ITopic;
import org.xmind.core.style.IStyle;

public abstract class Summary extends AbstractWorkbookComponent
        implements ISummary {

    protected static final List<ITopic> NO_ENCLOSING_TOPICS = Collections
            .emptyList();

    public String getStyleType() {
        return IStyle.SUMMARY;
    }

    public List<ITopic> getEnclosingTopics() {
        int startIndex = getStartIndex();
        int endIndex = getEndIndex();
        if (startIndex >= 0 && endIndex >= 0 && endIndex >= startIndex) {
            ITopic parent = getParent();
            if (parent != null) {
                List<ITopic> children = parent.getChildren(ITopic.ATTACHED);
                if (!children.isEmpty()) {
                    return getSubtopics(startIndex, endIndex, children);
                }
            }
        }
        return NO_ENCLOSING_TOPICS;
    }

    private List<ITopic> getSubtopics(int startIndex, int endIndex,
            List<ITopic> children) {
        startIndex = Math.min(startIndex, children.size() - 1);
        endIndex = Math.min(endIndex, children.size() - 1);
        return children.subList(startIndex, endIndex + 1);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.ITopicRange#encloses(org.xmind.core.ITopic)
     */
    public boolean encloses(ITopic subtopic) {
        if (subtopic == null)
            return false;
        ITopic parent = subtopic.getParent();
        if (parent == null || !parent.equals(getParent()))
            return false;
        int startIndex = getStartIndex();
        int endIndex = getEndIndex();
        int subIndex = subtopic.getIndex();
        return subIndex >= startIndex && subIndex <= endIndex;
    }

    public ITopic getStartTopic() {
        return getTopic(getStartIndex());
    }

    public ITopic getEndTopic() {
        return getTopic(getEndIndex());
    }

    protected ITopic getTopic(int index) {
        if (index >= 0) {
            ITopic parent = getParent();
            if (parent != null) {
                List<ITopic> children = parent.getChildren(ITopic.ATTACHED);
                if (index < children.size())
                    return children.get(index);
            }
        }
        return null;
    }

    public String toString() {
        return "SUMMARY (" + getTopicId() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
    }

}