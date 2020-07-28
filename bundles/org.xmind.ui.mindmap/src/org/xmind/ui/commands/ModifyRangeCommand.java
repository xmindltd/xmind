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
package org.xmind.ui.commands;

import java.util.Collection;

import org.xmind.core.IRange;
import org.xmind.gef.ISourceProvider;
import org.xmind.gef.command.ModifyCommand;

public class ModifyRangeCommand extends ModifyCommand {

    private boolean startOrEnd;

    public ModifyRangeCommand(IRange range, int newIndex, boolean startOrEnd) {
        super(range, newIndex);
        this.startOrEnd = startOrEnd;
    }

    public ModifyRangeCommand(Collection<? extends IRange> ranges,
            int newIndex, boolean startOrEnd) {
        super(ranges, newIndex);
        this.startOrEnd = startOrEnd;
    }

    public ModifyRangeCommand(ISourceProvider rangeProvider, int newIndex,
            boolean startOrEnd) {
        super(rangeProvider, newIndex);
        this.startOrEnd = startOrEnd;
    }

    protected Object getValue(Object source) {
        if (source instanceof IRange) {
            if (startOrEnd)
                return ((IRange) source).getStartIndex();
            return ((IRange) source).getEndIndex();
        }
        return null;
    }

    protected void setValue(Object source, Object value) {
        if (source instanceof IRange && value instanceof Integer) {
            if (startOrEnd) {
                ((IRange) source).setStartIndex((Integer) value);
            } else {
                ((IRange) source).setEndIndex((Integer) value);
            }
        }
    }

}