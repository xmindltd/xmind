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
package org.xmind.gef.command;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.xmind.gef.GEF;
import org.xmind.gef.ISourceProvider;

public abstract class ModifyCommand extends SourceCommand {

    private Object newValue;

    private Map<Object, Object> oldValues = null;

    protected ModifyCommand(Object source, Object newValue) {
        super(source);
        this.newValue = newValue;
    }

    protected ModifyCommand(Collection<?> sources, Object newValue) {
        super(sources);
        this.newValue = newValue;
    }

    protected ModifyCommand(ISourceProvider sourceProvider, Object newValue) {
        super(sourceProvider);
        this.newValue = newValue;
    }

    public int getType() {
        return GEF.CMD_MODIFY;
    }

    public Object getNewValue() {
        return newValue;
    }

    public Map<Object, Object> getOldValues() {
        if (oldValues == null) {
            oldValues = new HashMap<Object, Object>();
            for (Object source : getSources()) {
                oldValues.put(source, getValue(source));
            }
        }
        return oldValues;
    }

    public Object getOldValue(Object source) {
        return getOldValues().get(source);
    }

    protected abstract Object getValue(Object source);

    public boolean canExecute() {
        return !isSameValue();
    }

    protected boolean isSameValue() {
        for (Object source : getSources()) {
            Object oldValue = getOldValue(source);
            if (!isSameValue(oldValue, newValue))
                return false;
        }
        return true;
    }

    public void execute() {
        getOldValues();
        super.execute();
    }

    public void redo() {
        setNewValues();
        super.redo();
    }

    public void undo() {
        setOldValues();
        super.undo();
    }

    protected void setNewValues() {
        for (Object source : getSources()) {
            setValue(source, newValue);
        }
    }

    protected void setOldValues() {
        for (Object source : getSources()) {
            setValue(source, getOldValue(source));
        }
    }

    protected abstract void setValue(Object source, Object value);

    public void dispose() {
        newValue = null;
        oldValues = null;
        super.dispose();
    }

    protected boolean isSameValue(Object oldValue, Object newValue) {
        return (oldValue == newValue)
                || (oldValue != null && oldValue.equals(newValue));
    }

    /**
     * An alternate way to set new value.
     * 
     * @param newValue
     */
    protected void setNewValue(Object newValue) {
        if (this.newValue != null)
            return;

        this.newValue = newValue;
    }

}