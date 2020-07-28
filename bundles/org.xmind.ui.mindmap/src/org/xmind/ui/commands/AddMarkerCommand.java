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
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.xmind.core.ITopic;
import org.xmind.gef.ISourceProvider;
import org.xmind.gef.command.SourceCommand;

public class AddMarkerCommand extends SourceCommand {

    private String markerId;

    public AddMarkerCommand(ITopic topic, String newMarkerId) {
        super(topic);
        Assert.isNotNull(newMarkerId);
        this.markerId = newMarkerId;
    }

    public AddMarkerCommand(Collection<? extends ITopic> topics,
            String newMarkerId) {
        super(topics);
        Assert.isNotNull(newMarkerId);
        this.markerId = newMarkerId;
    }

    public AddMarkerCommand(ISourceProvider sourceProvider,
            String newMarkerId) {
        super(sourceProvider);
        Assert.isNotNull(newMarkerId);
        this.markerId = newMarkerId;
    }

    @Override
    public void redo() {
        for (Object source : getSources()) {
            if (source instanceof ITopic) {
                ITopic t = (ITopic) source;
                t.addMarker(markerId);
            }
        }
        super.redo();
    }

    @Override
    public void undo() {
        List<Object> sources = getSources();
        for (int i = sources.size() - 1; i >= 0; i--) {
            Object source = sources.get(i);
            if (source instanceof ITopic) {
                ITopic t = (ITopic) source;
                t.removeMarker(markerId);
            }
        }
        super.undo();
    }

}