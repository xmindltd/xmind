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
package org.xmind.ui.gallery;

import org.xmind.gef.part.IPart;
import org.xmind.gef.part.IPartFactory;

public class GalleryPartFactory implements IPartFactory {

    private static final GalleryPartFactory instance = new GalleryPartFactory();

    public IPart createPart(IPart parent, Object model) {
        if (parent instanceof ContentPanePart) {
            return createFramePart(parent, model);
        }

        if (parent instanceof FramePart) {
            return createFrameContentPart(parent, model);
        }

        return createContentPanePart(parent, model);
    }

    protected IPart createFrameContentPart(IPart parent, Object model) {
        return new SimpleImagePart(model);
    }

    protected IPart createFramePart(IPart parent, Object model) {
        return new FramePart(model);
    }

    protected IPart createContentPanePart(IPart parent, Object model) {
        return new ContentPanePart(model);
    }

    public static GalleryPartFactory getDefault() {
        return instance;
    }
}