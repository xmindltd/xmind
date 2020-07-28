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

import org.xmind.core.IRelationship;
import org.xmind.core.IRelationshipEnd;
import org.xmind.gef.ISourceProvider;
import org.xmind.gef.command.ModifyCommand;

public class ModifyRelationshipEndCommand extends ModifyCommand {

    private boolean sourceOrTarget;

    public ModifyRelationshipEndCommand(IRelationship source, String newEndId,
            boolean sourceOrTarget) {
        super(source, newEndId);
        this.sourceOrTarget = sourceOrTarget;
    }

    public ModifyRelationshipEndCommand(Collection<IRelationship> sources,
            String newEndId, boolean sourceOrTarget) {
        super(sources, newEndId);
        this.sourceOrTarget = sourceOrTarget;
    }

    public ModifyRelationshipEndCommand(ISourceProvider sourceProvider,
            String newEndId, boolean sourceOrTarget) {
        super(sourceProvider, newEndId);
        this.sourceOrTarget = sourceOrTarget;
    }

    public ModifyRelationshipEndCommand(IRelationship source,
            ISourceProvider newEndProvider, boolean sourceOrTarget) {
        super(source, newEndProvider);
        this.sourceOrTarget = sourceOrTarget;
    }

    public ModifyRelationshipEndCommand(ISourceProvider sourceProvider,
            ISourceProvider newEndProvider, boolean sourceOrTarget) {
        super(sourceProvider, newEndProvider);
        this.sourceOrTarget = sourceOrTarget;
    }

    protected Object getValue(Object source) {
        if (source instanceof IRelationship) {
            if (sourceOrTarget)
                return ((IRelationship) source).getEnd1Id();
            return ((IRelationship) source).getEnd2Id();
        }
        return null;
    }

    protected void setValue(Object source, Object value) {
        if (source instanceof IRelationship) {
            if (value instanceof ISourceProvider) {
                Object newEnd = ((ISourceProvider) value).getSource();
                if (newEnd instanceof String) {
                    value = (String) newEnd;
                } else if (newEnd instanceof IRelationshipEnd) {
                    value = ((IRelationshipEnd) newEnd).getId();
                }
            }
            if (value == null || value instanceof String) {
                if (sourceOrTarget) {
                    ((IRelationship) source).setEnd1Id((String) value);
                } else {
                    ((IRelationship) source).setEnd2Id((String) value);
                }
            }
        }
    }

}