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
package org.xmind.ui.viewers;

import org.eclipse.jface.viewers.IContentProvider;

public interface ISliderContentProvider extends IContentProvider {

    /**
     * Returns a selectable value at the specified position on the slider.
     * 
     * @param input
     *            The input of the viewer
     * @param ratio
     *            The ratio of the position compared to the whole slider slot
     *            length, ranged from <code>0</code> (inclusive) to
     *            <code>1</code> (inclusive), where <code>0</code> means the
     *            position is at the 'minimum value' end and <code>1</code> at
     *            the 'maximum value' end
     * @return A selectable value at the specified position on the slider
     */
    Object getValue(Object input, double ratio);

    /**
     * Returns the ratio of the position to represent the specified value
     * compared to the whole slider slot length.
     * 
     * <p>
     * <b>NOTE: The ratio should NOT be less then <code>0</code> or greater than
     * <code>1</code>.</b>
     * </p>
     * 
     * @param input
     *            The input of the viewer
     * @param value
     *            The value to represent
     * @return The ratio of the position to represent the specified value
     *         compared to the whole slider slot length, ranged from
     *         <code>0</code> (inclusive) to <code>1</code> (inclusive)
     */
    double getRatio(Object input, Object value);

}