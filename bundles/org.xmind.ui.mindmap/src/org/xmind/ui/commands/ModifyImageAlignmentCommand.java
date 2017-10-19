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
import org.xmind.gef.ISourceProvider;
import org.xmind.gef.command.ModifyCommand;

public class ModifyImageAlignmentCommand extends ModifyCommand {

    public ModifyImageAlignmentCommand(IImage image, String newAlignment) {
        super(image, newAlignment);
    }

    public ModifyImageAlignmentCommand(ITopic topic, String newAlignment) {
        super(topic, newAlignment);
    }

    public ModifyImageAlignmentCommand(Collection<? extends IImage> images,
            String newAlignment) {
        super(images, newAlignment);
    }

    public ModifyImageAlignmentCommand(ISourceProvider topicOrImageProvider,
            String newAlignment) {
        super(topicOrImageProvider, newAlignment);
    }

    protected Object getValue(Object source) {
        IImage image = getImage(source);
        if (image != null)
            return image.getAlignment();
        return null;
    }

    protected void setValue(Object source, Object value) {
        IImage image = getImage(source);
        if (image != null) {
            if (value instanceof String) {
                image.setAlignment((String) value);
            } else {
                image.setAlignment(null);
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