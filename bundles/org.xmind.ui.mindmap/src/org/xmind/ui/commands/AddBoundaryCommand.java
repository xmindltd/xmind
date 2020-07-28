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
import org.xmind.core.IBoundary;
import org.xmind.core.ITopic;
import org.xmind.gef.ArraySourceProvider;
import org.xmind.gef.ISourceProvider;
import org.xmind.gef.command.SourceCommand;

public class AddBoundaryCommand extends SourceCommand {

    private ISourceProvider parentProvider;

    public AddBoundaryCommand(IBoundary boundary, ITopic parent) {
        super(boundary);
        Assert.isNotNull(parent);
        this.parentProvider = new ArraySourceProvider(parent);
    }

    public AddBoundaryCommand(ISourceProvider boundaryProvider, ITopic parent) {
        super(boundaryProvider);
        Assert.isNotNull(parent);
        this.parentProvider = new ArraySourceProvider(parent);
    }

    public AddBoundaryCommand(ISourceProvider boundaryProvider,
            ISourceProvider parentProvider) {
        super(boundaryProvider);
        Assert.isNotNull(parentProvider);
        this.parentProvider = parentProvider;
    }

    public void redo() {
        Object p = parentProvider.getSource();
        if (p instanceof ITopic) {
            ITopic parent = (ITopic) p;
            for (Object source : getSources()) {
                if (source instanceof IBoundary) {
                    parent.addBoundary((IBoundary) source);
                }
            }
        }
        super.redo();
    }

    public void undo() {
        Object p = parentProvider.getSource();
        if (p instanceof ITopic) {
            ITopic parent = (ITopic) p;
            List<Object> sources = getSources();
            for (int i = sources.size() - 1; i >= 0; i--) {
                Object source = sources.get(i);
                if (source instanceof IBoundary) {
                    parent.removeBoundary((IBoundary) source);
                }
            }
        }
        super.undo();
    }
}