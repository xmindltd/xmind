package org.xmind.ui.internal.decorations;

import java.util.List;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.xmind.gef.draw2d.graphics.Path;
import org.xmind.ui.decorations.AbstractBranchConnection;
import org.xmind.ui.decorations.ITopicDecoration;
import org.xmind.ui.internal.figures.BranchFigure;
import org.xmind.ui.internal.figures.TopicFigure;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.ITopicPart;

@SuppressWarnings("restriction")
public class TimelineHorizontalConnection extends AbstractBranchConnection {

    private IBranchPart branch;

    public TimelineHorizontalConnection(IBranchPart branch, String id) {
        super(id);
        this.branch = branch;
    }

    @Override
    protected void route(IFigure figure, Path shape) {
        Point sp = getPosition(branch.getTopicPart());
        List<IBranchPart> subBranches = branch.getSubBranches();
        Point tp = sp;
        if (subBranches != null && !subBranches.isEmpty()) {
            IBranchPart lastBranch = subBranches.get(subBranches.size() - 1);
            tp = getPosition(lastBranch.getTopicPart());
        }

        if (figure instanceof BranchFigure) {
            shape.moveTo(sp);
            shape.lineTo(tp.x, sp.y);
        } else {
            shape.moveTo(getSourcePosition(figure));
            shape.lineTo(getTargetPosition(figure));
        }
    }

    private Point getPosition(ITopicPart topic) {
        Rectangle bounds = topic.getFigure().getBounds();
        return bounds.getLocation().getTranslated(bounds.width / 2,
                bounds.height / 2);
    }

    @Override
    protected void paintPath(IFigure figure, Graphics graphics, Path path,
            boolean fill) {
        if (branch != null) {
            Rectangle bounds = figure.getBounds();
            Path shape = new Path(Display.getCurrent());
            shape.addRectangle(bounds);
            shape.addPath(getDecoration(branch)
                    .createClippingPath(branch.getTopicPart().getFigure()));
            for (IBranchPart sub : branch.getSubBranches()) {
                shape.addPath(getDecoration(sub)
                        .createClippingPath(sub.getTopicPart().getFigure()));
            }
            graphics.pushState();
            try {
                graphics.setClip(shape);
//                if (fill) {
//                graphics.fillPath(path);
//                } else {
                if (getLineWidth() > 0)
                    graphics.drawPath(path);
//                }
                graphics.restoreState();
            } finally {
                graphics.popState();
                shape.close();
                shape.dispose();
            }
            return;

        }

        super.paintPath(figure, graphics, path, fill);
    }

    private ITopicDecoration getDecoration(IBranchPart branch) {
        IFigure figure = branch.getTopicPart().getFigure();
        return ((TopicFigure) figure).getDecoration();
    }

}
