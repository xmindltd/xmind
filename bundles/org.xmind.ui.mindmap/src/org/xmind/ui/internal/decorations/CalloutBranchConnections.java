package org.xmind.ui.internal.decorations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.xmind.gef.draw2d.IAnchor;
import org.xmind.gef.draw2d.decoration.CompoundDecoration;
import org.xmind.gef.draw2d.decoration.IConnectionDecoration;
import org.xmind.gef.draw2d.decoration.ICorneredDecoration;
import org.xmind.gef.draw2d.decoration.IDecoration;
import org.xmind.gef.draw2d.decoration.ILineDecoration;
import org.xmind.gef.draw2d.decoration.IShadowedDecoration;
import org.xmind.ui.decorations.IBranchConnectionDecoration;
import org.xmind.ui.decorations.IBranchConnections2;

public class CalloutBranchConnections extends CompoundDecoration
        implements IBranchConnections2, IShadowedDecoration {

    private List<IAnchor> sourceAnchors = new ArrayList<IAnchor>();

    private List<Color> lineColors = new ArrayList<Color>();

    private List<Integer> lineStyles = new ArrayList<Integer>();

    private List<Integer> lineWidths = new ArrayList<Integer>();

    private List<Integer> sourceOrientations = new ArrayList<Integer>();

    private List<Integer> sourceExpansions = new ArrayList<Integer>();

    private List<Integer> cornerSizes = new ArrayList<Integer>();

    private boolean tapered = false;

    private HashMap<IFigure, IDecoration> figureToDecoration = new HashMap<IFigure, IDecoration>();

    public Color getLineColor(int index) {
        if (lineColors.size() <= index)
            return null;
        return lineColors.get(index);
    }

    public int getLineStyle(int index) {
        if (lineStyles.size() <= index)
            return SWT.LINE_SOLID;
        return lineStyles.get(index);
    }

    public int getLineWidth(int index) {
        if (lineWidths.size() <= index)
            return 1;
        return lineWidths.get(index);
    }

    public void setLineColor(IFigure figure, int index, Color color) {
        if (lineColors.size() > index) {
            Color oldColor = lineColors.get(index);
            if (oldColor == color
                    || (oldColor != null && oldColor.equals(color)))
                return;
            lineColors.set(index, color);
        } else {
            lineColors.add(index, color);
        }
        if (figure != null) {
            repaint(figure);
        }
        update(figure, index);
    }

    public void putFigureToDecoration(IFigure figure, IDecoration decoration) {
        figureToDecoration.put(figure, decoration);
    }

    public IDecoration getDecoration(IFigure figure) {
        return figureToDecoration.get(figure);
    }

    private void update(IFigure figure) {
        for (IDecoration decoration : getDecorations()) {
            if (decoration != null)
                update(figure, decoration);
        }
    }

    @Override
    protected void update(IFigure figure, IDecoration decoration) {
        super.update(figure, decoration);
        updateAnchor(figure, getDecorations().indexOf(decoration));
        updateConnection(figure, decoration);
    }

    private void updateConnection(IFigure figure, IDecoration decoration) {
        int index = getDecorations().indexOf(decoration);
        if (decoration instanceof ICorneredDecoration) {
            ((ICorneredDecoration) decoration).setCornerSize(figure,
                    getCornerSize(index));
        }
        if (decoration instanceof ILineDecoration) {
            ILineDecoration line = (ILineDecoration) decoration;

            line.setLineColor(figure, getLineColor(index));

            line.setLineStyle(figure, getLineStyle(index));
            line.setLineWidth(figure, getLineWidth(index));
        }
        if (decoration instanceof IBranchConnectionDecoration) {
            IBranchConnectionDecoration conn = (IBranchConnectionDecoration) decoration;
            conn.setSourceOrientation(figure, getSourceOrientation(index));
            conn.setSourceExpansion(figure, getSourceExpansion(index));
            conn.setTapered(figure, tapered);
        }

    }

    private void update(IFigure figure, int index) {
        IDecoration decoration = getDecorations().get(index);
        super.update(figure, decoration);
        updateAnchor(figure, index);
        updateConnection(figure, decoration);
    }

    public void setLineWidth(IFigure figure, int index, int width) {
        if (lineWidths.size() > index) {
            Integer lineWidth = lineWidths.get(index);
            if (lineWidth.intValue() == width)
                return;
            lineWidths.set(index, width);
        } else {
            lineWidths.add(index, width);
        }
        if (figure != null) {
            figure.revalidate();
            repaint(figure);
        }
        invalidate();
        update(figure, index);

    }

    public void setLineStyle(IFigure figure, int index, int style) {
        if (lineStyles.size() > index) {
            Integer lineStyle = lineStyles.get(index);
            if (lineStyle.intValue() == style)
                return;
            lineStyles.set(index, style);
        } else {
            lineStyles.add(index, style);
        }
        if (figure != null) {
            repaint(figure);
        }
        update(figure, index);
    }

    public int getCornerSize(int index) {
        if (cornerSizes.size() <= index)
            return 0;
        return cornerSizes.get(index);
    }

    public void setCornerSize(IFigure figure, int index, int cornerSize) {
        if (cornerSizes.size() > index) {
            Integer size = cornerSizes.get(index);
            if (size.intValue() == cornerSize)
                return;
            cornerSizes.set(index, cornerSize);
        } else {
            cornerSizes.add(index, cornerSize);
        }
        if (figure != null) {
            figure.revalidate();
            repaint(figure);
        }
        invalidate();
        update(figure);
    }

    public void paintShadow(IFigure figure, Graphics graphics) {
        if (!isVisible())
            return;
        checkValidation(figure);
        for (IDecoration decoration : getDecorations()) {
            if (decoration instanceof IShadowedDecoration) {
                ((IShadowedDecoration) decoration).paintShadow(figure,
                        graphics);
            }
        }
    }

    public void setSourceOrientation(IFigure figure, int index,
            int orientation) {
        if (sourceOrientations.size() > index) {
            Integer oldOrientation = sourceOrientations.get(index);
            if (oldOrientation.intValue() == orientation)
                return;
            sourceOrientations.set(index, orientation);
        } else {
            sourceOrientations.add(index, orientation);
        }
        if (figure != null) {
            figure.revalidate();
            repaint(figure);
        }
        invalidate();
        update(figure, index);
    }

    public void setSourceExpansion(IFigure figure, int index, int expansion) {
        if (sourceExpansions.size() > index) {
            Integer oldExpansion = sourceExpansions.get(index);
            if (oldExpansion.intValue() == expansion)
                return;
            sourceExpansions.set(index, expansion);
        } else {
            sourceExpansions.add(index, expansion);
        }
        if (figure != null) {
            figure.revalidate();
            repaint(figure);
        }
        invalidate();
        update(figure, index);
    }

    public void setTapered(IFigure figure, boolean tapered) {
        if (tapered == this.tapered)
            return;
        this.tapered = tapered;
        if (figure != null) {
            figure.revalidate();
            repaint(figure);
        }
        invalidate();
        update(figure);
    }

    public int getSourceExpansion(int index) {
        if (sourceExpansions.size() <= index)
            return 0;
        return sourceExpansions.get(index);
    }

    public int getSourceOrientation(int index) {
        if (sourceOrientations.size() <= index)
            return PositionConstants.NONE;
        return sourceOrientations.get(index);
    }

    public boolean isTapered() {
        return tapered;
    }

    public IAnchor getSourceAnchor(int index) {
        if (sourceAnchors.size() <= index)
            return null;
        return sourceAnchors.get(index);
    }

    public void setSourceAnchor(IFigure figure, int index, IAnchor anchor) {
        if (sourceAnchors.size() > index) {
            IAnchor oldAnchor = sourceAnchors.get(index);
            if (oldAnchor == anchor
                    || (oldAnchor != null && oldAnchor.equals(anchor)))
                return;
            sourceAnchors.set(index, anchor);
        } else {
            sourceAnchors.add(index, anchor);
        }
        updateAnchor(figure, index);

    }

    private void updateAnchor(IFigure figure, int index) {
        IDecoration decoration = getDecoration(index);
        if (decoration instanceof IConnectionDecoration) {
            ((IConnectionDecoration) decoration).setSourceAnchor(figure,
                    getSourceAnchor(index));
        }

    }

    public void rerouteAll(IFigure figure) {
        int size = size();
        for (int i = 0; i < size; i++) {
            IDecoration decoration = getDecoration(i);
            if (decoration instanceof IConnectionDecoration) {
                ((IConnectionDecoration) decoration).reroute(figure);
            }
        }
        if (figure != null) {
            repaint(figure);
        }
    }
}
