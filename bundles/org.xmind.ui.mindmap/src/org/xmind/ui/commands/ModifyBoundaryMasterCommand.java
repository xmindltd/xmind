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

import org.xmind.core.IBoundary;
import org.xmind.gef.ISourceProvider;
import org.xmind.gef.command.ModifyCommand;

public class ModifyBoundaryMasterCommand extends ModifyCommand {

    public ModifyBoundaryMasterCommand(IBoundary boundary, boolean overall) {
        super(boundary, overall);
    }

    public ModifyBoundaryMasterCommand(
            Collection<? extends IBoundary> boundaries, boolean overall) {
        super(boundaries, overall);
    }

    public ModifyBoundaryMasterCommand(ISourceProvider boundaryProvider,
            boolean overall) {
        super(boundaryProvider, overall);
    }

    protected Object getValue(Object source) {
        if (source instanceof IBoundary) {
            IBoundary b = (IBoundary) source;
            return b.isMasterBoundary();
        }
        return null;
    }

    protected void setValue(Object source, Object value) {
        if (source instanceof IBoundary) {
            IBoundary b = (IBoundary) source;
            if (value instanceof Boolean) {
                b.setMasterBoundary(((Boolean) value).booleanValue());
            }
        }
    }

}