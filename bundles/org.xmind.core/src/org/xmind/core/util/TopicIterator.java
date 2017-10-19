/* ******************************************************************************
 * Copyright (c) 2006-2016 XMind Ltd. and others.
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
/**
 * 
 */
package org.xmind.core.util;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Stack;

import org.xmind.core.ITopic;

/**
 * @author Frank Shaka
 * @since 3.6.50
 */
public class TopicIterator implements Iterator<ITopic> {

    private static class TopicWrapper {

        public final ITopic topic;

        /**
         * 
         */
        public TopicWrapper(ITopic topic) {
            this.topic = topic;
        }
    }

    public static final int NONE = 0;
    public static final int REVERSED = 1 << 0;

    private int options;

    private ITopic next;

    private Stack<Object> stack;

    public TopicIterator(ITopic root) {
        this(root, NONE);
    }

    /**
     * 
     */
    public TopicIterator(ITopic root, int options) {
        this.options = options;
        this.stack = new Stack<Object>();
        this.stack.push(root);
        this.next = findNext();
    }

    private static void pushFIFO(Stack<Object> stack, List<ITopic> topics) {
        ListIterator<ITopic> it = topics.listIterator(topics.size());
        while (it.hasPrevious()) {
            stack.push(it.previous());
        }
    }

    private static void pushLIFOStack(Stack<Object> stack,
            List<ITopic> topics) {
        for (ITopic topic : topics) {
            stack.push(topic);
        }
    }

    private ITopic findNext() {
        if (stack.isEmpty())
            return null;

        Object o = stack.pop();
        if (hasOption(REVERSED)) {
            if (o instanceof TopicWrapper)
                return ((TopicWrapper) o).topic;
            ITopic t = (ITopic) o;
            stack.push(new TopicWrapper(t));
            pushLIFOStack(stack, t.getAllChildren());
            return findNext();
        } else {
            ITopic t = (ITopic) o;
            pushFIFO(stack, t.getAllChildren());
            return t;
        }
    }

    private boolean hasOption(int option) {
        return (options & option) != 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Iterator#hasNext()
     */
    public boolean hasNext() {
        return next != null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Iterator#next()
     */
    public ITopic next() {
        ITopic t = next;
        if (t == null)
            throw new NoSuchElementException();
        next = findNext();
        return t;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Iterator#remove()
     */
    public void remove() {
        /// ignore remove operations
    }

}
