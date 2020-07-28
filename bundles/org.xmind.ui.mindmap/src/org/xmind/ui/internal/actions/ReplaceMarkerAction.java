/* ******************************************************************************
 * Copyright (c) 2006-2016 XMind Ltd. and others.
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
/**
 * 
 */
package org.xmind.ui.internal.actions;

import java.io.IOException;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.xmind.core.ISheet;
import org.xmind.core.marker.IMarker;
import org.xmind.core.marker.IMarkerRef;
import org.xmind.core.util.CloneHandler;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.command.Command;
import org.xmind.gef.command.CompoundCommand;
import org.xmind.ui.commands.AddMarkerCommand;
import org.xmind.ui.commands.CommandMessages;
import org.xmind.ui.commands.DeleteMarkerCommand;
import org.xmind.ui.util.MarkerImageDescriptor;

/**
 * @author Frank Shaka
 * @since 3.6.50
 */
public class ReplaceMarkerAction extends ViewerAction {

    private final IMarkerRef sourceMarkerRef;

    private final IMarker targetMarker;

    /**
     * 
     */
    public ReplaceMarkerAction(IGraphicalViewer viewer,
            IMarkerRef sourceMarkerRef, IMarker targetMarker) {
        super(viewer);
        Assert.isNotNull(sourceMarkerRef);
        Assert.isNotNull(targetMarker);
        this.sourceMarkerRef = sourceMarkerRef;
        this.targetMarker = targetMarker;

        ISheet sheet = sourceMarkerRef.getOwnedSheet();
        String text = sheet == null || !sheet.getLegend().getMarkerIds()
                .contains(targetMarker.getId()) ? null
                        : sheet.getLegend()
                                .getMarkerDescription(targetMarker.getId());
        text = text == null ? targetMarker.getName() : text;
        setText(text == null ? "" : text); //$NON-NLS-1$
        setImageDescriptor(
                MarkerImageDescriptor.createFromMarker(targetMarker, 16, 16));

        boolean sameMarker = targetMarker.getId()
                .equals(sourceMarkerRef.getMarkerId());
        setChecked(sameMarker);
        setEnabled(!sameMarker);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        if (!isEnabled())
            return;

        SafeRunner.run(new SafeRunnable() {
            @Override
            public void run() throws Exception {
                runSafely();
            }
        });
    }

    /**
     * @throws IOException
     */
    private void runSafely() throws IOException {
        IMarker actualTargetMarker = sourceMarkerRef.getOwnedWorkbook()
                .getMarkerSheet().findMarker(targetMarker.getId());
        if (actualTargetMarker == null) {
            actualTargetMarker = (IMarker) new CloneHandler()
                    .withMarkerSheets(targetMarker.getOwnedSheet(),
                            sourceMarkerRef.getOwnedWorkbook().getMarkerSheet())
                    .cloneObject(targetMarker);
            if (actualTargetMarker == null)
                return;
        }

        Command command = new CompoundCommand( //
                CommandMessages.Command_ReplaceMarker, //

                new DeleteMarkerCommand(sourceMarkerRef), //
                new AddMarkerCommand(sourceMarkerRef.getParent(),
                        actualTargetMarker.getId()) //

        );

        executeCommand(command);
    }

}
