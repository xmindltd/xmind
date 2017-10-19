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
package org.xmind.core.event;

import java.util.EventObject;

/**
 * @author MANGOSOFT
 * 
 */
public class CoreEvent extends EventObject {

    /**
     * 
     */
    private static final long serialVersionUID = -5729530103366986314L;

    private String type;

    private Object oldValue;

    private Object newValue;

    private int index;

    private Object target;

    private Object data;

    public CoreEvent(ICoreEventSource source, String type, Object target) {
        this(source, type, target, null, null, -1);
    }

    public CoreEvent(ICoreEventSource source, String type, Object target,
            int index) {
        this(source, type, target, null, null, index);
    }

    public CoreEvent(ICoreEventSource source, String type, Object target,
            Object oldValue, Object newValue) {
        this(source, type, target, oldValue, newValue, -1);
    }

    public CoreEvent(ICoreEventSource source, String type, Object oldValue,
            Object newValue) {
        this(source, type, null, oldValue, newValue, -1);
    }

    public CoreEvent(ICoreEventSource source, String type, Object oldValue,
            Object newValue, int index) {
        this(source, type, null, oldValue, newValue, index);
    }

    public CoreEvent(ICoreEventSource source, String type, Object target,
            Object oldValue, Object newValue, int index) {
        super(source);
        this.type = type;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.target = target;
        this.index = index;
    }

    /**
     * @return
     */
    public ICoreEventSource getEventSource() {
        return (ICoreEventSource) getSource();
    }

    /**
     * 
     * @return
     */
    public String getType() {
        return type;
    }

    /**
     * @return
     */
    public Object getOldValue() {
        return oldValue;
    }

    /**
     * @return
     */
    public Object getNewValue() {
        return newValue;
    }

    public void setNewValue(Object newValue) {
        this.newValue = newValue;
    }

    public void setOldValue(Object oldValue) {
        this.oldValue = oldValue;
    }

    /**
     * 
     * @return
     */
    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * 
     * @return
     */
    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Object getTarget() {
        return target;
    }

    public void setTarget(Object target) {
        this.target = target;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(30);
        sb.append("{type="); //$NON-NLS-1$
        sb.append(type);
        sb.append(",source="); //$NON-NLS-1$
        sb.append(source);
        if (target != null) {
            sb.append(",target="); //$NON-NLS-1$
            sb.append(target);
        }
        if (oldValue != null) {
            sb.append(",oldValue="); //$NON-NLS-1$
            sb.append(oldValue);
        }
        if (newValue != null) {
            sb.append(",newValue="); //$NON-NLS-1$
            sb.append(newValue);
        }
        if (index >= 0) {
            sb.append(",index="); //$NON-NLS-1$
            sb.append(index);
        }
        if (data != null) {
            sb.append(",data="); //$NON-NLS-1$
            sb.append(data);
        }
        sb.append("}"); //$NON-NLS-1$
        return sb.toString();
    }
}