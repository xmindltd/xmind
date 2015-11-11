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
package org.xmind.ui.internal.actions;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Menu;
import org.xmind.core.ITopic;
import org.xmind.core.marker.IMarker;
import org.xmind.core.marker.IMarkerGroup;
import org.xmind.core.marker.IMarkerRef;
import org.xmind.gef.ISourceProvider;
import org.xmind.gef.command.Command;
import org.xmind.gef.command.CompoundCommand;
import org.xmind.gef.command.ICommandStack;
import org.xmind.ui.commands.AddMarkerCommand;
import org.xmind.ui.commands.CommandMessages;
import org.xmind.ui.commands.DeleteMarkerCommand;
import org.xmind.ui.mindmap.IWorkbookRef;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.MarkerImageDescriptor;

public class GroupMarkers extends ContributionItem {

    private class ReplaceMarkerAction extends Action {

        private ITopic topic;

        private String sourceMarkerId;

        private String targetMarkerId;

        public ReplaceMarkerAction(ITopic topic, String sourceMarkerId,
                String targetMarkerId) {
            this.topic = topic;
            this.sourceMarkerId = sourceMarkerId;
            this.targetMarkerId = targetMarkerId;
            setText(topic.getOwnedSheet().getLegend()
                    .getMarkerDescription(targetMarkerId));
            setImageDescriptor(MarkerImageDescriptor.createFromMarker(
                    topic.getOwnedWorkbook().getMarkerSheet()
                            .findMarker(targetMarkerId), 16, 16));
            boolean sameMarker = sourceMarkerId.equals(targetMarkerId);
            setEnabled(!sameMarker);
            setChecked(sameMarker);
        }

        public void run() {
            if (!isEnabled())
                return;

            IWorkbookRef wr = MindMapUI.getWorkbookRefManager().findRef(
                    topic.getOwnedWorkbook());
            if (wr != null) {
                ICommandStack cs = wr.getCommandStack();
                if (cs != null) {
                    Command cmd = createReplaceMarkerCommand();
                    cs.execute(cmd);
                    if (cmd instanceof ISourceProvider) {
                        select(((ISourceProvider) cmd).getSources());
                    }
                }
            }
        }

        private void select(List<Object> sources) {
            if (selectionProvider != null) {
                selectionProvider
                        .setSelection(new StructuredSelection(sources));
            }
        }

        private Command createReplaceMarkerCommand() {
            return new CompoundCommand(CommandMessages.Command_ReplaceMarker,
                    new DeleteMarkerCommand(topic, sourceMarkerId),
                    new AddMarkerCommand(topic, targetMarkerId));
        }
    }

    private class DeleteMarkerAction extends Action {

        private ITopic topic;

        private String sourceMarkerId;

        public DeleteMarkerAction(ITopic topic, String sourceMarkerId) {
            this.topic = topic;
            this.sourceMarkerId = sourceMarkerId;
            setText(CommandMessages.Command_Delete);
        }

        @Override
        public void run() {
            IWorkbookRef wr = MindMapUI.getWorkbookRefManager().findRef(
                    topic.getOwnedWorkbook());
            if (wr != null) {
                ICommandStack cs = wr.getCommandStack();
                if (cs != null) {
                    Command cmd = createRemoveMarkerCommand();
                    cs.execute(cmd);
                }
            }
        }

        private Command createRemoveMarkerCommand() {
            return new CompoundCommand(CommandMessages.Command_DeleteMarker,
                    new DeleteMarkerCommand(topic, sourceMarkerId));
        }
    }

    private ISelectionProvider selectionProvider;

    private IMarkerRef sourceMarkerRef;

    public GroupMarkers() {
        setId(ActionConstants.MARKER_GROUP_MENU);
    }

    public void setSelectionProvider(ISelectionProvider selectionProvider) {
        this.selectionProvider = selectionProvider;
    }

    public boolean isVisible() {
        return sourceMarkerRef != null;
    }

    public void setSourceMarkerRef(IMarkerRef sourceMarkerRef) {
        this.sourceMarkerRef = sourceMarkerRef;
    }

    public boolean isDynamic() {
        return true;
    }

    public void fill(Menu menu, int index) {
        if (sourceMarkerRef == null)
            return;

        ITopic topic = sourceMarkerRef.getParent();
        String sourceMarkerId = sourceMarkerRef.getMarkerId();
        IMarker sourceMarker = topic.getOwnedWorkbook().getMarkerSheet()
                .findMarker(sourceMarkerId);
        if (sourceMarker != null) {
            IMarkerGroup group = sourceMarker.getParent();
            if (group != null && !group.isHidden()) {
                for (IMarker marker : group.getMarkers()) {
                    if (!marker.isHidden()) {
                        String targetMarkerId = marker.getId();
                        new ActionContributionItem(new ReplaceMarkerAction(
                                topic, sourceMarkerId, targetMarkerId)).fill(
                                menu, index++);
                    }
                }
                new Separator().fill(menu, index++);
                new ActionContributionItem(new DeleteMarkerAction(topic,
                        sourceMarkerId)).fill(menu, index++);
            }
        }
    }
}