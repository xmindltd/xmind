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
package org.xmind.ui.commands;

import java.util.Collection;

import org.xmind.core.IImage;
import org.xmind.core.ITopic;
import org.xmind.core.util.Point;
import org.xmind.gef.ISourceProvider;
import org.xmind.gef.command.ModifyCommand;

public class ModifyImageSizeCommand extends ModifyCommand {

    public ModifyImageSizeCommand(IImage image, int width, int height) {
        super(image, new Point(width, height));
    }

    public ModifyImageSizeCommand(ITopic topic, int width, int height) {
        super(topic, new Point(width, height));
    }

    public ModifyImageSizeCommand(Collection<? extends IImage> images,
            int width, int height) {
        super(images, new Point(width, height));
    }

    public ModifyImageSizeCommand(ISourceProvider topicOrImageProvider,
            int width, int height) {
        super(topicOrImageProvider, new Point(width, height));
    }

    protected Object getValue(Object source) {
        IImage image = getImage(source);
        if (image != null) {
            int width = image.getWidth();
            int height = image.getHeight();
            return new Point(width, height);
        }
        return null;
    }

    protected void setValue(Object source, Object value) {
        IImage image = getImage(source);
        if (image != null) {
            if (value == null) {
                image.setSize(IImage.UNSPECIFIED, IImage.UNSPECIFIED);
            } else if (value instanceof Point) {
                Point size = (Point) value;
                image.setSize(size.x, size.y);
            }
        }
    }

    private IImage getImage(Object source) {
        if (source instanceof IImage)
            return (IImage) source;
        if (source instanceof ITopic)
            return ((ITopic) source).getImage();
        return null;
    }
}