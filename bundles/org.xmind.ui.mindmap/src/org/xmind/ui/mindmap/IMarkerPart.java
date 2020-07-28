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

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.swt.graphics.Image;
import org.xmind.core.ITopic;
import org.xmind.core.marker.IMarker;
import org.xmind.core.marker.IMarkerRef;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.ui.internal.svgsupport.SVGImageData;

public interface IMarkerPart extends IGraphicalPart {

    IMarkerRef getMarkerRef();

    IMarker getMarker();

    ITopic getTopic();

    ITopicPart getTopicPart();

    Image getImage();

    SVGImageData getSVGData();

    Dimension getPreferredSize();

    ResourceManager getResourceManager();

}