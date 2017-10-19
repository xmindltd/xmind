package org.xmind.ui.gallery;

import org.eclipse.draw2d.IFigure;

public interface ILabelDecorator {

    IFigure decorateFigure(IFigure figure, Object element,
            IDecorationContext context);

}
