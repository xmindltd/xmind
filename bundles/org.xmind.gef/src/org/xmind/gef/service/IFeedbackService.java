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
package org.xmind.gef.service;

import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.jface.viewers.IColorProvider;
import org.xmind.gef.draw2d.SelectionFigure;

public interface IFeedbackService extends IViewerService {

    /**
     * Key for getting the focus color from a {@link IColorProvider}.
     * 
     * @see org.xmind.gef.service.IFeedbackService
     */
    Object FocusColor = "FocusColor"; //$NON-NLS-1$
    /**
     * Key for getting the selection color from a {@link IColorProvider}.
     * 
     * @see org.xmind.gef.service.IFeedbackService
     */
    Object SelectionColor = "SelectionColor"; //$NON-NLS-1$
    /**
     * Key for getting the pre-selection color from a {@link IColorProvider}.
     * 
     * @see org.xmind.gef.service.IFeedbackService
     */
    Object PreselectionColor = "PreselectionColor"; //$NON-NLS-1$

    Object DisabledFocusColor = "DisabledFocusColor"; //$NON-NLS-1$

    Object DisabledSelectionColor = "DisabledSelectionColor"; //$NON-NLS-1$

    Object DisabledPreselectionColor = "DisabledPreselectionColor"; //$NON-NLS-1$

    void setLayer(IFigure layer);

    public void addFeedback(IFeedback feedback);

    public void removeFeedback(IFeedback feedback);

    public List<IFeedback> getFeedbackParts();

    /**
     * @param selectionColorProvider
     *            the selectionColorProvider to set
     */
    void setSelectionColorProvider(IColorProvider selectionColorProvider);

    /**
     * @return the selectionColorProvider
     */
    IColorProvider getSelectionColorProvider();

    SelectionFigure addSelection(IFigure source);

    SelectionFigure removeSelection(IFigure source);

    /**
     * @param source
     * @return
     */
    SelectionFigure getSelectionFigure(IFigure source);

    SelectionFigure setSelected(IFigure source);

    SelectionFigure setPreselected(IFigure source);

    SelectionFigure setFocused(IFigure source);

//  void setSkylight(IPositionProvider pp, Color fogColor, int fogAlpha);
//        void setSkylight(IPositionProvider pp, Color fogColor);
//        void removeSkylight();
//        IPositionProvider getSkylight();
//        void updateSkylight();
//        void updateSkylight(IPositionProvider pp);

}