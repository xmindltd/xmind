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
package org.xmind.ui.internal.layers;

import org.eclipse.draw2d.Graphics;
import org.eclipse.swt.SWT;

public class FeedbackLayer extends BaseLayer {

    /**
     * @see cn.brainy.ui.mindmap.layers.MapCommonLayer#paint(org.eclipse.draw2d.Graphics)
     */
    @Override
    public void paint(Graphics graphics) {
        graphics.setAntialias(SWT.ON);
        super.paint(graphics);
    }

}