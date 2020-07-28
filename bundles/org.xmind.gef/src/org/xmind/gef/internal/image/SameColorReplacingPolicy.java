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
package org.xmind.gef.internal.image;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.graphics.RGB;

/**
 * @author Frank Shaka
 */
class SameColorReplacingPolicy implements IColorReplacingPolicy {
    /**
     * @see cn.brainy.gef.image.IColorReplacingPolicy#getReplacedColor(org.eclipse.swt.graphics.RGB)
     */
    public RGB getReplacedColor( RGB source ) {
        return source;
    }
    /**
     * @see cn.brainy.gef.image.IColorReplacingPolicy#getReplacedColors(java.util.Map)
     */
    public RGB[] getReplacingColors( int numMaxColors, Map<RGB, Integer> colorOccurrences ) {
        List<RGB> colors = new ArrayList<RGB>( colorOccurrences.keySet() );
        return colors.toArray( new RGB[ colors.size() ] );
    }
}