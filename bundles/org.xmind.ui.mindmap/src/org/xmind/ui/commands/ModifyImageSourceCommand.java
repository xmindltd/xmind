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

public class ModifyImageSourceCommand extends ModifyCommand {

    public ModifyImageSourceCommand(ITopic topic, String newValue) {
        super(topic, newValue);
    }

    public ModifyImageSourceCommand(IImage image, String newValue) {
        super(image, newValue);
    }

    public ModifyImageSourceCommand(ISourceProvider topicOrImageProvider,
            String newValue) {
        super(topicOrImageProvider, newValue);
    }

    public ModifyImageSourceCommand(Collection<? extends IImage> images,
            String newValue) {
        super(images, newValue);
    }

    protected Object getValue(Object source) {
        IImage image = getImage(source);
        if (image != null) {
            return image.getSource();
        }
        return null;
    }

    protected void setValue(Object source, Object value) {
        IImage image = getImage(source);
        if (image != null) {
            if (value == null) {
                image.setSource(null);
            } else if (value instanceof String) {
                image.setSource((String) value);
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