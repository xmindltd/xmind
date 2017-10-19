package org.xmind.ui.decorations;

import org.eclipse.draw2d.IFigure;
import org.xmind.gef.draw2d.IAnchor;
import org.xmind.gef.draw2d.decoration.ICompoundDecoration;

public interface IBranchConnections2 extends ICompoundDecoration,
        ILineDecoration2 {

    void setSourceOrientation(IFigure figure, int index, int orientation);

    void setSourceExpansion(IFigure figure, int index, int expansion);

    void setTapered(IFigure figure, boolean tapered);

    int getSourceExpansion(int index);

    int getSourceOrientation(int index);

    boolean isTapered();

    IAnchor getSourceAnchor(int index);

    void setSourceAnchor(IFigure figure, int index, IAnchor anchor);

    void setCornerSize(IFigure figure, int index, int cornerSize);

    int getCornerSize(int index);

    void rerouteAll(IFigure figure);
}
