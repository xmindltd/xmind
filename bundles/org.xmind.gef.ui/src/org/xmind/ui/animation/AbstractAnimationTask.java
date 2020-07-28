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
package org.xmind.ui.animation;

/**
 * @author Frank Shaka
 */
public abstract class AbstractAnimationTask implements IAnimationTask {

    private Object source;

    private Object start;

    private Object end;

    public AbstractAnimationTask(Object source, Object start, Object end) {
        this.source = source;
        this.start = start;
        this.end = end;
    }

    public Object getEndValue() {
        return end;
    }

    public Object getSource() {
        return source;
    }

    public Object getStartValue() {
        return start;
    }

    /**
     * @see cn.brainy.ui.animation.IAnimationTask#start()
     */
    public void start() {
        setValue(getStartValue());
    }

    /**
     * @see cn.brainy.ui.animation.IAnimationTask#finish()
     */
    public void finish() {
        setValue(getEndValue());
    }

    /**
     * @see cn.brainy.ui.animation.IAnimationTask#cancel()
     */
    public void cancel() {
    }

}