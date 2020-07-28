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
package org.xmind.ui.util;

import java.util.Iterator;

public class Chainability {

    private Chainability() {
        throw new AssertionError();
    }

    /**
     * Inserts an element into the chain after the specified one.
     * <p>
     * Example:
     * </p>
     * <p>
     * Inserting B after A <blockquote>... - ''A - 'A - A - A' - A'' -
     * ...</blockquote> results <blockquote>... - ''A - 'A - A - B - A' - A'' -
     * ...</blockquote>
     * </p>
     * 
     * @param chained
     *            an element in the chain
     * @param element
     *            the element to be inserted into the chain
     */
    public static <T extends IChained<T>> void insertAfter(T chained, T element) {
        T next = chained.getNext();
        if (next != null) {
            next.setPrevious(element);
        }
        chained.setNext(element);
        element.setNext(next);
        element.setPrevious(chained);
    }

    /**
     * Joins two sub-chains. The new chain consists of the <code>left</code>
     * element and all its previous elements plus the <code>right</code> element
     * and all its next elements.
     * 
     * <p>
     * Example:
     * </p>
     * <p>
     * Concatenate A <blockquote>... - ''A - 'A - A - A' - A'' -
     * ...</blockquote> and B <blockquote>... - ''B - 'B - B - B' - B'' -
     * ...</blockquote> results <blockquote>... - ''A - 'A - A - B - B' - B'' -
     * ...</blockquote>
     * 
     * @param left
     *            an element at the end of the left sub-chain
     * @param right
     *            an element at the beginning of the right sub-chain
     */
    public static <T extends IChained<T>> void concatenate(T left, T right) {
        T next = left.getNext();
        if (next != null) {
            next.setPrevious(null);
        }
        T previous = right.getPrevious();
        if (previous != null) {
            previous.setNext(null);
        }
        left.setNext(right);
        right.setPrevious(left);
    }

    /**
     * Removes an element from its chain.
     * 
     * @param element
     *            an element in the chain
     */
    public static <T extends IChained<T>> void remove(T element) {
        T previous = element.getPrevious();
        T next = element.getNext();
        if (previous != null) {
            previous.setNext(next);
        }
        if (next != null) {
            next.setPrevious(previous);
        }
        element.setPrevious(null);
        element.setNext(null);
    }

    /**
     * Constructs an iterator that iterates over the chain from the specified
     * starting element to the specified ending element (both included).
     * 
     * @param start
     *            an element in the chain
     * @param end
     *            another element in the chain
     * @return an {@link Iterator} object
     */
    public static <T extends IChained<T>> Iterator<T> iterate(final T start,
            final T end) {
        return new Iterator<T>() {

            T next = start;

            public boolean hasNext() {
                return next != null;
            }

            public T next() {
                T n = next;
                next = (next == null || next == end) ? null : next.getNext();
                return n;
            }

            public void remove() {
                // do nothing
            }
        };
    }

}
