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

import org.eclipse.core.runtime.Assert;
import org.xmind.core.ITopic;
import org.xmind.core.marker.IMarkerRef;
import org.xmind.gef.GEF;
import org.xmind.gef.command.SourceCommand;
import org.xmind.ui.internal.MindMapMessages;

public class DeleteMarkerCommand extends SourceCommand {

    private String markerId;

    public DeleteMarkerCommand(ITopic topic, String markerId) {
        super(topic);
        Assert.isNotNull(markerId);
        this.markerId = markerId;
        setLabel(MindMapMessages.DeleteMarker_label);
    }

    public DeleteMarkerCommand(IMarkerRef markerRef) {
        Assert.isNotNull(markerRef);
        ITopic topic = markerRef.getParent();
        Assert.isNotNull(topic);
        String markerId = markerRef.getMarkerId();
        Assert.isNotNull(markerId);
        setSource(topic);
        this.markerId = markerId;
        setLabel(MindMapMessages.DeleteMarker_label);
    }

    public int getType() {
        return GEF.CMD_DELETE;
    }

    @Override
    public void redo() {
        ((ITopic) getSource()).removeMarker(markerId);
        super.redo();
    }

    @Override
    public void undo() {
        ((ITopic) getSource()).addMarker(markerId);
        super.undo();
    }
}