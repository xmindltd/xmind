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
package org.xmind.ui.animation;

import org.eclipse.draw2d.IFigure;
import org.xmind.gef.draw2d.IUseTransparency;
import org.xmind.gef.draw2d.decoration.IDecoration;

public class AnimationTaskFactory {

    private AnimationTaskFactory() {
    }

    public static IAnimationTask createMainAlphaTask(IUseTransparency source,
            int start, int end) {
        return new AlphaAnimationTask(source, start, end) {

            public void setValue(Object value) {
                ((IUseTransparency) getSource()).setMainAlpha((Integer) value);
            }

            public void start() {
                super.start();
                if (getSource() instanceof IFigure) {
                    ((IFigure) getSource()).setVisible(true);
                }
            }
        };
    }

    public static IAnimationTask createSubAlphaTask(IUseTransparency source,
            int start, int end) {
        return new AlphaAnimationTask(source, start, end) {
            public void setValue(Object value) {
                ((IUseTransparency) getSource()).setSubAlpha((Integer) value);
            }

            public void start() {
                super.start();
                if (getSource() instanceof IFigure) {
                    ((IFigure) getSource()).setVisible(true);
                }
            }
        };
    }

    public static IAnimationTask createDecorationAlphaTask(IDecoration source,
            final IFigure hostingFigure, int start, int end) {
        return new AlphaAnimationTask(source, start, end) {
            public void setValue(Object value) {
                ((IDecoration) getSource()).setAlpha(hostingFigure,
                        (Integer) value);
            }

            public void start() {
                super.start();
                ((IDecoration) getSource()).setVisible(hostingFigure, true);
            }
        };
    }

}