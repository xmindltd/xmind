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
package org.xmind.ui.commands;

import java.util.Collection;

import org.xmind.core.IPositioned;
import org.xmind.core.util.Point;
import org.xmind.gef.ISourceProvider;
import org.xmind.gef.command.ModifyCommand;

public class ModifyPositionCommand extends ModifyCommand {

    public ModifyPositionCommand(IPositioned source, Point newValue) {
        super(source, newValue);
    }

    public ModifyPositionCommand(Collection<? extends IPositioned> sources,
            Point newValue) {
        super(sources, newValue);
    }

    public ModifyPositionCommand(ISourceProvider sourceProvider, Point newValue) {
        super(sourceProvider, newValue);
    }

    protected Object getValue(Object source) {
        if (source instanceof IPositioned) {
            IPositioned t = (IPositioned) source;
            return t.getPosition();
        }
        return null;
    }

    protected void setValue(Object source, Object value) {
        if (source instanceof IPositioned
                && (value == null || value instanceof Point)) {
            ((IPositioned) source).setPosition((Point) value);
        }
    }

}