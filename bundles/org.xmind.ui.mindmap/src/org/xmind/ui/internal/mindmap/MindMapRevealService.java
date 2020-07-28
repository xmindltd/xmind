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
package org.xmind.ui.internal.mindmap;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.xmind.gef.GraphicalViewer;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.gef.service.ZoomingAndPanningRevealService;
import org.xmind.ui.mindmap.IMindMapViewer;
import org.xmind.ui.mindmap.ISheetPart;
import org.xmind.ui.mindmap.ITopicPart;

public class MindMapRevealService extends ZoomingAndPanningRevealService {

    public MindMapRevealService(IGraphicalViewer viewer) {
        super(viewer);
        setShouldRevealOnIntersection(false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.gef.service.ZoomingAndPanningRevealService#exclude(org.xmind
     * .gef.part.IGraphicalPart)
     */
    @Override
    protected boolean exclude(IGraphicalPart part) {
        return part instanceof ISheetPart;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.gef.service.AbstractViewerService#inputChanged(java.lang.Object
     * , java.lang.Object)
     */
    @Override
    public void inputChanged(Object oldInput, Object newInput) {
        super.inputChanged(oldInput, newInput);
        centerOnCentralTopic();
    }

    public void centerOnCentralTopic() {
        final ITopicPart centralTopic = (ITopicPart) getViewer()
                .getAdapter(ITopicPart.class);
        if (centralTopic == null)
            return;
//        centerOnCentralTopic(centralTopic);
        ((GraphicalViewer) getViewer()).getLightweightSystem()
                .getUpdateManager().runWithUpdate(new Runnable() {
                    public void run() {
                        if (getViewer().getFocused() == null) {
                            centerOnCentralTopic(centralTopic);
                            final Canvas canvas = getViewer().getCanvas();
                            canvas.addListener(SWT.Paint, new Listener() {
                                public void handleEvent(Event event) {
                                    canvas.removeListener(SWT.Paint, this);
                                    centerOnCentralTopic(centralTopic);
                                }
                            });
                        }
                    }
                });
    }

    private void centerOnCentralTopic(ITopicPart centralTopic) {
        boolean selected = getViewer().getProperties().getBoolean(
                IMindMapViewer.VIEWER_SELECT_CENTRALTOPIC, Boolean.TRUE);
        if (selected) {
            getViewer().setSelection(new StructuredSelection(centralTopic),
                    false);
            getViewer().center(centralTopic.getFigure().getBounds());
        }
    }

}