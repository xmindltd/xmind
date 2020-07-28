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
package org.xmind.ui.mindmap;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.xmind.core.IImage;
import org.xmind.core.ITopic;
import org.xmind.gef.part.IGraphicalPart;

public interface IImagePart extends IGraphicalPart {

    IImage getImageModel();

    Image getImage();

    ImageDescriptor getImageDescriptor();

    ITopic getTopic();

    ITopicPart getTopicPart();

}