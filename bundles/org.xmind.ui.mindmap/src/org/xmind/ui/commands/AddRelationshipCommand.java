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

import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.xmind.core.IRelationship;
import org.xmind.core.ISheet;
import org.xmind.gef.ISourceProvider;
import org.xmind.gef.command.SourceCommand;

public class AddRelationshipCommand extends SourceCommand {

    private ISheet parent;

    public AddRelationshipCommand(IRelationship relationship, ISheet parent) {
        super(relationship);
        Assert.isNotNull(parent);
        this.parent = parent;
    }

    public AddRelationshipCommand(ISourceProvider relationshipProvider,
            ISheet parent) {
        super(relationshipProvider);
        Assert.isNotNull(parent);
        this.parent = parent;
    }

    public void redo() {
        for (Object source : getSources()) {
            if (source instanceof IRelationship) {
                parent.addRelationship((IRelationship) source);
            }
        }
        super.redo();
    }

    public void undo() {
        List<Object> sources = getSources();
        for (int i = sources.size() - 1; i >= 0; i--) {
            Object source = sources.get(i);
            if (source instanceof IRelationship) {
                parent.removeRelationship((IRelationship) source);
            }
        }
        super.undo();
    }
}