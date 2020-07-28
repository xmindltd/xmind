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

import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.gef.service.IRectangleProvider;
import org.xmind.gef.service.RectangleFeedback;
import org.xmind.ui.resources.ColorUtils;

public class SimpleSelectionFeedback extends RectangleFeedback {

    private IGraphicalPart host;

    public SimpleSelectionFeedback(final IGraphicalPart host) {
        this.host = host;
        setBoundsProvider(new IRectangleProvider() {
            public Rectangle getRectangle() {
                return getHost().getFigure().getBounds();
            }
        });
    }

    public IGraphicalPart getHost() {
        return host;
    }

    public void update() {
        if (getHost().getStatus().isSelected()) {
            setBorderColor(ColorUtils.getColor("#0000f0")); //$NON-NLS-1$
            setFillColor(null);
            setBorderAlpha(0xff);
            setFillAlpha(0);
        } else if (getHost().getStatus().isPreSelected()) {
            setBorderColor(ColorUtils.getColor("#0000ff")); //$NON-NLS-1$
            setFillColor(ColorUtils.getColor("#0000ff")); //$NON-NLS-1$
            setBorderAlpha(0xff);
            setFillAlpha(0x20);
        }
        super.update();
    }

}