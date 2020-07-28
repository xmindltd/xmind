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
package org.xmind.ui.internal.tools;

import static org.xmind.ui.mindmap.MindMapUI.COLOR_WARNING;
import static org.xmind.ui.mindmap.MindMapUI.LINE_WIDTH_DUMMY;
import static org.xmind.ui.style.StyleUtils.createBranchConnection;
import static org.xmind.ui.style.StyleUtils.isSameDecoration;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Layer;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.UpdateManager;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.xmind.gef.EditDomain;
import org.xmind.gef.GEF;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.IViewer;
import org.xmind.gef.draw2d.DecoratedShapeFigure;
import org.xmind.gef.draw2d.IAnchor;
import org.xmind.gef.draw2d.IAnchorListener;
import org.xmind.gef.draw2d.IUseTransparency;
import org.xmind.gef.draw2d.SelectionFigure;
import org.xmind.gef.draw2d.decoration.ICorneredDecoration;
import org.xmind.gef.draw2d.decoration.IDecoration;
import org.xmind.gef.draw2d.decoration.IShapeDecoration;
import org.xmind.gef.draw2d.geometry.PrecisionPoint;
import org.xmind.gef.draw2d.graphics.Path;
import org.xmind.gef.graphicalpolicy.IStructure;
import org.xmind.gef.service.IAnimationService;
import org.xmind.gef.service.IFeedbackService;
import org.xmind.ui.branch.IBranchStructureExtension;
import org.xmind.ui.branch.IInsertableBranchStructureExtension;
import org.xmind.ui.branch.IInsertion;
import org.xmind.ui.decorations.IBranchConnectionDecoration;
import org.xmind.ui.decorations.IBranchConnections;
import org.xmind.ui.internal.mindmap.DecoratedAnchor;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.resources.ColorUtils;
import org.xmind.ui.tools.ITopicMoveToolHelper;
import org.xmind.ui.tools.ParentSearchKey;
import org.xmind.ui.tools.ToolHelperBase;

public class TopicMoveToolHelper extends ToolHelperBase implements
        ITopicMoveToolHelper {

    private class DummyConnectionFigure extends Figure {
        /**
         * @see org.eclipse.draw2d.Figure#paintFigure(org.eclipse.draw2d.Graphics)
         */
        @Override
        public void paintFigure(Graphics graphics) {
            graphics.setAntialias(SWT.ON);
            if (parent != null) {
                if (connection != null)
                    connection.paint(this, graphics);
                if (!cursorOverParent && !specialConnection) {
                    paintExpandedLine(graphics, parent);
                }
            }
            super.paintFigure(graphics);
        }

        private void paintExpandedLine(Graphics graphics, IBranchPart parent) {
            IBranchConnections connections = parent.getConnections();
            if (connections == null)
                return;

            int orientation = connections.getSourceOrientation();
            if (orientation == PositionConstants.NONE)
                return;

            IAnchor anc = connections.getSourceAnchor();
            if (anc == null)
                return;

            PrecisionPoint p1 = anc.getLocation(orientation, 0);
            PrecisionPoint p2 = anc.getLocation(orientation,
                    connections.getSourceExpansion());
            graphics.setLineStyle(SWT.LINE_SOLID);
            graphics.setLineWidth(Math.max(LINE_WIDTH_DUMMY,
                    connections.getLineWidth()));
            graphics.setForegroundColor(ColorUtils.getColor(COLOR_WARNING));
            graphics.setAlpha(connections.getAlpha());
            Path p = new Path(Display.getCurrent());
            p.moveTo(p1);
            p.lineTo(p2);
            graphics.drawPath(p);
            p.dispose();
        }
    }

    private class DummyAnchor extends DecoratedAnchor {

        public DummyAnchor() {
            super(null);
        }

        public void setOwner(IFigure figure) {
            super.setOwner(figure);
        }

    }

    private IBranchPart parent = null;

    private boolean showConnection = true;

    private IBranchConnectionDecoration connection = null;

    private IFigure connectionFigure = null;

    private boolean cursorOverParent = false;

    private IFeedbackService feedbackService = null;

    private IAnimationService animationService = null;

    private IInsertion insertion = null;

    private IAnchor sourceAnchor = null;

    private IAnchorListener sourceAnchorListener = null;

    private DummyAnchor targetAnchor = null;

    private IAnchorListener targetAnchorListener = null;

    private boolean specialConnection = false;

    public void activate(EditDomain domain, IViewer viewer) {
        super.activate(domain, viewer);
        feedbackService = (IFeedbackService) viewer
                .getService(IFeedbackService.class);
        Layer layer = ((IGraphicalViewer) viewer)
                .getLayer(GEF.LAYER_PRESENTATION);
        if (layer != null) {
            createConnectionFigure(layer);
        }
        animationService = (IAnimationService) viewer
                .getService(IAnimationService.class);
        if (animationService != null) {
            animationService.stop();
        }
    }

    public void deactivate(EditDomain domain, IViewer viewer) {
        if (connectionFigure != null) {
            if (connectionFigure.getParent() != null)
                connectionFigure.getParent().remove(connectionFigure);
            connectionFigure = null;
        }
        if (feedbackService != null) {
            if (parent != null && parent.getStatus().isActive()) {
                feedbackService.removeSelection(parent.getTopicPart()
                        .getFigure());
            }
            feedbackService = null;
        }
        if (animationService != null) {
            animationService.stop();
            animationService = null;
        }
        if (insertion != null) {
            insertion.pullOut();
            insertion = null;
        }
        setSourceAnchor(null);
        if (targetAnchor != null) {
            if (targetAnchorListener != null) {
                targetAnchor.removeAnchorListener(targetAnchorListener);
            }
            targetAnchor.setOwner(null);
            targetAnchor = null;
        }
        connection = null;
        parent = null;
        super.deactivate(domain, viewer);
    }

//    public Request getAdaptedRequest(IBranchPart targetParent,
//            ParentSearchKey key, Request sourceRequest) {
//        return sourceRequest;
//    }

    public void update(IBranchPart targetParent, ParentSearchKey key) {
        update(targetParent, true, key, -1);
    }

    private void updateCalloutConnection(IBranchPart parent, int index,
            IBranchPart feedback) {
        IDecoration originConnection = parent.getCalloutConnections()
                .getDecoration(index);
        if (!(originConnection instanceof IBranchConnectionDecoration))
            return;
        IBranchConnectionDecoration branchConnectionDecoration = (IBranchConnectionDecoration) originConnection;
        String connectionId = originConnection.getId();

        if (!isSameDecoration(connection, connectionId)) {
            connection = createBranchConnection(parent, connectionId);
        }
        if (connection != null) {
            connection.setId(connectionId);
            connection.setSourceAnchor(connectionFigure, sourceAnchor);

            if (targetAnchor == null) {
                targetAnchor = new DummyAnchor();
                if (targetAnchorListener == null) {
                    targetAnchorListener = new IAnchorListener() {
                        public void anchorMoved(IAnchor anchor) {
                            if (connection != null && connectionFigure != null) {
                                connection.reroute(connectionFigure);
                            }
                        }
                    };
                }
                targetAnchor.addAnchorListener(targetAnchorListener);
            }
            targetAnchor.setOwner(feedback.getTopicPart().getFigure());
            connection.setTargetAnchor(connectionFigure, targetAnchor);

            connection.setLineStyle(connectionFigure,
                    branchConnectionDecoration.getLineStyle());
            connection.setLineWidth(connectionFigure,
                    branchConnectionDecoration.getLineWidth());
            connection.setLineColor(connectionFigure,
                    ColorUtils.getColor(COLOR_WARNING));

            connection.setSourceOrientation(connectionFigure,
                    PositionConstants.CENTER);
            connection.setSourceExpansion(connectionFigure,
                    branchConnectionDecoration.getSourceExpansion());

            connection.setTargetOrientation(connectionFigure,
                    PositionConstants.NONE);
            connection.setTargetExpansion(connectionFigure, 0);
            if (connection instanceof IShapeDecoration) {
                ((IShapeDecoration) connection).setFillColor(connectionFigure,
                        ColorUtils.getColor(COLOR_WARNING));
            }
            if (connectionFigure.getParent() instanceof IUseTransparency) {
                connection.setAlpha(connectionFigure,
                        ((IUseTransparency) connectionFigure.getParent())
                                .getMainAlpha());
            }
            if (connection instanceof ICorneredDecoration
                    && (branchConnectionDecoration instanceof ICorneredDecoration)) {
                ((ICorneredDecoration) connection).setCornerSize(
                        connectionFigure,
                        ((ICorneredDecoration) branchConnectionDecoration)
                                .getCornerSize());
            }

            connection.setVisible(connectionFigure,
                    !cursorOverParent && connectionFigure.isVisible()
                            && connection.getSourceAnchor() != null
                            && connection.getTargetAnchor() != null);
            connection.reroute(connectionFigure);
        }
    }

    private void setSourceAnchor(IAnchor anchor) {
        if (anchor == sourceAnchor)
            return;
        if (sourceAnchor != null && sourceAnchorListener != null) {
            sourceAnchor.removeAnchorListener(sourceAnchorListener);
        }
        sourceAnchor = anchor;
        if (anchor != null) {
            if (sourceAnchorListener == null) {
                sourceAnchorListener = new IAnchorListener() {
                    public void anchorMoved(IAnchor anchor) {
                        if (connection != null && connectionFigure != null) {
                            connection.reroute(connectionFigure);
                        }
                    }
                };
            }
            anchor.addAnchorListener(sourceAnchorListener);
        }
    }

    private void updateConnection(IBranchPart parent, ParentSearchKey key) {
        IBranchPart feedback = key.getFeedback();
        IBranchConnections connections = parent.getConnections();
        String connectionId = connections.getId();
        if (!isSameDecoration(connection, connectionId)) {
            connection = createBranchConnection(parent, connectionId);
        }
        if (connection != null) {
            connection.setId(connectionId);
            connection.setSourceAnchor(connectionFigure, sourceAnchor);

            if (targetAnchor == null) {
                targetAnchor = new DummyAnchor();
                if (targetAnchorListener == null) {
                    targetAnchorListener = new IAnchorListener() {
                        public void anchorMoved(IAnchor anchor) {
                            if (connection != null && connectionFigure != null) {
                                connection.reroute(connectionFigure);
                            }
                        }
                    };
                }
                targetAnchor.addAnchorListener(targetAnchorListener);
            }

            IFigure fig = key.getInvent();
            if (fig == null)
                fig = key.getFigure();
            IShapeDecoration shape = ((DecoratedShapeFigure) fig)
                    .getDecoration();
            shape.setFillColor(fig, ColorUtils.getColor(COLOR_WARNING));
            targetAnchor.setOwner(fig);
            connection.setTargetAnchor(connectionFigure, targetAnchor);

            connection.setLineStyle(connectionFigure,
                    connections.getLineStyle());
            connection.setLineWidth(connectionFigure,
                    Math.max(LINE_WIDTH_DUMMY, connections.getLineWidth()));
            connection.setSourceOrientation(connectionFigure,
                    connections.getSourceOrientation());
            connection.setSourceExpansion(connectionFigure,
                    connections.getSourceExpansion());

            int targetOrientation = PositionConstants.NONE;
            IStructure structure = parent.getBranchPolicy()
                    .getStructure(parent);
            if (structure instanceof IBranchStructureExtension) {
                targetOrientation = ((IBranchStructureExtension) structure)
                        .getChildTargetOrientation(parent, feedback);
            }
            connection
                    .setTargetOrientation(connectionFigure, targetOrientation);
            connection.setTargetExpansion(connectionFigure, 0);
            connection.setLineColor(connectionFigure,
                    ColorUtils.getColor(COLOR_WARNING));
            if (connectionFigure.getParent() instanceof IUseTransparency) {
                connection.setAlpha(connectionFigure,
                        ((IUseTransparency) connectionFigure.getParent())
                                .getMainAlpha());
            }
            if (connection instanceof ICorneredDecoration) {
                ((ICorneredDecoration) connection).setCornerSize(
                        connectionFigure, connections.getCornerSize());
            }

            connection.setVisible(connectionFigure,
                    !cursorOverParent && connectionFigure.isVisible()
                            && connection.getSourceAnchor() != null
                            && connection.getTargetAnchor() != null);
            connection.reroute(connectionFigure);
        }
    }

    public boolean isShowConnection() {
        return showConnection;
    }

    public void setShowConnection(boolean showConnection) {
        this.showConnection = showConnection;
    }

    protected void createConnectionFigure(IFigure layer) {
        if (layer != null && showConnection) {
            connectionFigure = new DummyConnectionFigure();
            layer.add(connectionFigure);
        }
    }

    private IInsertion calcInsertion(IBranchPart parent, ParentSearchKey key) {
        UpdateManager um = key.getFigure().getUpdateManager();
        if (um != null)
            um.performValidation();

        if (parent != null) {
            IStructure structure = parent.getBranchPolicy()
                    .getStructure(parent);
            if (structure instanceof IInsertableBranchStructureExtension) {
                return ((IInsertableBranchStructureExtension) structure)
                        .calcInsertion(parent, key);
            }
        }
        return null;
    }

    private void animInsertion(final IInsertion oldInsertion,
            final IInsertion newInsertion) {
        Runnable job = new Runnable() {
            public void run() {
                if (oldInsertion != null) {
                    oldInsertion.pullOut();
                }
                if (newInsertion != null) {
                    newInsertion.pushIn();
                }
            }
        };
        if (MindMapUI.isAnimationEnabled() && animationService != null
                && animationService.isActive()) {
            animationService.start(job, null, null);
        } else {
            job.run();
        }
    }

    public void update(IBranchPart targetParent, boolean moved,
            ParentSearchKey key, int specialIndex) {
        IBranchPart oldParent = this.parent;

        this.parent = targetParent;

        if (feedbackService != null && oldParent != null
                && oldParent.getStatus().isActive()) {
            feedbackService.removeSelection(oldParent.getTopicPart()
                    .getFigure());
        }

        IFigure parentTopicFigure = targetParent == null ? null : targetParent
                .getTopicPart().getFigure();

        cursorOverParent = parentTopicFigure != null
                && parentTopicFigure.containsPoint(key.getCursorPos());

        specialConnection = specialIndex > -1;

        if (specialIndex > -1) {
            setSourceAnchor(targetParent == null ? null
                    : ((IBranchConnectionDecoration) targetParent
                            .getCalloutConnections()
                            .getDecoration(specialIndex)).getSourceAnchor());
        } else {
            setSourceAnchor(targetParent == null ? null : targetParent
                    .getConnections().getSourceAnchor());
        }

        if (connectionFigure != null) {
            if (parentTopicFigure != null) {
                Rectangle bounds;
                if (specialIndex <= -1 && key.getInvent() != null)
                    bounds = parentTopicFigure.getBounds()
                            .getUnion(key.getInvent().getBounds())
                            .expand(10, 10);
                else
                    bounds = parentTopicFigure.getBounds()
                            .getUnion(key.getFigure().getBounds())
                            .expand(10, 10);
                connectionFigure.setBounds(bounds);
                connectionFigure.setVisible(targetParent != null);
            } else {
                connectionFigure.setVisible(false);
            }
            if (targetParent != null) {
                if (specialIndex > -1) {
                    updateCalloutConnection(targetParent, specialIndex,
                            key.getFeedback());
                } else {
                    updateConnection(targetParent, key);
                }
            }
        }

        if (feedbackService != null && parentTopicFigure != null) {
            SelectionFigure selectionFigure = feedbackService
                    .addSelection(parentTopicFigure);
            selectionFigure.setPreselectionColor(ColorUtils
                    .getColor(COLOR_WARNING));
            selectionFigure.setPreselectionFillColor(ColorUtils
                    .getColor(COLOR_WARNING));
            selectionFigure.setPreselectionFillAlpha(0x10);
            selectionFigure.setPreselected(true);
        }

        if (moved) {
            IInsertion oldInsertion = this.insertion;
            IInsertion newInsertion = calcInsertion(parent, key);
            if (oldInsertion != newInsertion
                    && (newInsertion == null || !newInsertion
                            .equals(oldInsertion))) {
                this.insertion = newInsertion;
                animInsertion(oldInsertion, newInsertion);
            }
        } else {
            IInsertion oldiIInsertion = this.insertion;
            this.insertion = null;
            animInsertion(oldiIInsertion, null);
        }
    }
}