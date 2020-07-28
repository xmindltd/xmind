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

import org.eclipse.draw2d.IFigure;
import org.xmind.gef.part.IGraphicalPart;

public interface IAnimationService extends IViewerService {

    void registerFigure(IFigure figure, IGraphicalPart part);

    void unregisterFigure(IFigure figure);

    IGraphicalPart getRegisteredPart(IFigure figure);

    boolean isIdle();

    boolean isAnimating();

    IPlaybackProvider getPlaybackProvider();

    void setPlaybackProvider(IPlaybackProvider playbackProvider);

    void start(Runnable keyframeMaker, Runnable beforeEffect,
            Runnable afterEffect);

    void stop();

    boolean isAnimating(IFigure figure);

}