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

package org.xmind.ui.gallery;

import org.eclipse.draw2d.IFigure;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.xmind.gef.GEF;
import org.xmind.gef.part.GraphicalEditPart;
import org.xmind.gef.part.IRequestHandler;
import org.xmind.gef.policy.NullEditPolicy;

/**
 * @author Frank Shaka
 * 
 */
public class NavigationItemPart extends GraphicalEditPart {

    /**
     * 
     */
    public NavigationItemPart(Object model) {
        setModel(model);
        getFigure();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.gef.part.GraphicalEditPart#createFigure()
     */
    @Override
    protected IFigure createFigure() {
        return new NavigationItemFigure();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.gef.part.GraphicalEditPart#updateView()
     */
    @Override
    protected void updateView() {
        super.updateView();
        NavigationItemFigure fig = (NavigationItemFigure) getFigure();
        IBaseLabelProvider labelProvider = (IBaseLabelProvider) getSite()
                .getViewer().getAdapter(IBaseLabelProvider.class);
        if (labelProvider instanceof ILabelProvider) {
            fig.setText(((ILabelProvider) labelProvider).getText(getModel()));
            fig.setImage(((ILabelProvider) labelProvider).getImage(getModel()));
        }
        if (labelProvider instanceof IColorProvider) {
            fig.setForegroundColor(((IColorProvider) labelProvider)
                    .getForeground(getModel()));
            fig.setBackgroundColor(((IColorProvider) labelProvider)
                    .getBackground(getModel()));
        }
        if (labelProvider instanceof IFontProvider) {
            fig.setFont(((IFontProvider) labelProvider).getFont(getModel()));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.gef.part.EditPart#declareEditPolicies(org.xmind.gef.part.
     * IRequestHandler)
     */
    @Override
    protected void declareEditPolicies(IRequestHandler reqHandler) {
        super.declareEditPolicies(reqHandler);
        reqHandler.installEditPolicy(GEF.ROLE_NAVIGABLE,
                NavigationItemNavigablePolicy.DEFAULT);
        reqHandler.installEditPolicy(GEF.ROLE_SELECTABLE,
                NullEditPolicy.getInstance());
    }

//    /*
//     * (non-Javadoc)
//     * 
//     * @see
//     * org.xmind.gef.part.Part#handleStatusChanged(org.xmind.gef.status.StatusEvent
//     * )
//     */
//    @Override
//    protected void handleStatusChanged(StatusEvent event) {
//        if (event.key == GEF.PART_SELECTED) {
//            if (event.newValue) {
//                onSelection();
//            } else {
//                onDeselection();
//            }
//        } else {
//            super.handleStatusChanged(event);
//        }
//    }
//
//    /**
//     * 
//     */
//    private void onDeselection() {
//        ((NavigationItemFigure) getFigure()).setState(0);
//    }
//
//    /**
//     * 
//     */
//    private void onSelection() {
//        ((NavigationItemFigure) getFigure()).setState(1);
//        IPart parent = getParent();
//        if (parent != null && parent instanceof NavigationContentPart) {
//            ((NavigationContentPart) parent).onItemSelection(this);
//        }
//    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.gef.part.EditPart#register()
     */
    @Override
    protected void register() {
        registerModel(getModel());
        super.register();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.gef.part.EditPart#unregister()
     */
    @Override
    protected void unregister() {
        super.unregister();
        unregisterModel(getModel());
    }

}
