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
package org.xmind.core.style;

import java.util.Iterator;

import org.xmind.core.IAdaptable;
import org.xmind.core.IIdentifiable;
import org.xmind.core.INamed;
import org.xmind.core.IProperties;
import org.xmind.core.ITopic;
import org.xmind.core.util.Property;

public interface IStyle extends IAdaptable, IIdentifiable, IProperties, INamed {

    /**
     * Style type for theme properties.
     */
    String THEME = "theme"; //$NON-NLS-1$

    /**
     * Style type for paragraph properties.
     */
    String PARAGRAPH = "paragraph"; //$NON-NLS-1$
    /**
     * Style type for bullet paragraph properties.
     */
//    String BULLETPARAGRAPH = "bulletParagraph";
    /**
     * Style type for text properties.
     */
    String TEXT = "text"; //$NON-NLS-1$

    /**
     * Style type for summary properties.
     */
    String SUMMARY = "summary"; //$NON-NLS-1$

    /**
     * Style type for boundary properties.
     */
    String BOUNDARY = "boundary"; //$NON-NLS-1$

    /**
     * Style type for relationship properties.
     */
    String RELATIONSHIP = "relationship"; //$NON-NLS-1$

    /**
     * Style type for topic properties.
     */
    String TOPIC = "topic"; //$NON-NLS-1$

    /**
     * Style type for map properties.
     */
    String MAP = "map"; //$NON-NLS-1$

    /**
     * Gets the style sheet who owns this style.
     * <p>
     * A style can not be added to a style sheet that does not own it.
     * 
     * @return the owned style sheet
     */
    IStyleSheet getOwnedStyleSheet();

    /**
     * Gets the string identifitying the type of this style. A style with a
     * specific type can only be applied to a specific type of object, i.e. a
     * style with {@link #TOPIC} type can only be applied to {@link ITopic}
     * objects. Trying to apply a style to a object of different type may result
     * in no effects.
     * 
     * @see #TOPIC
     * @see #MAP
     * @see #BOUNDARY
     * @see #RELATIONSHIP
     * @see #SUMMARY
     * @see #PARAGRAPH
     * @see #TEXT
     * @see #THEME
     * @return the type of this style
     */
    String getType();

    /**
     * @return
     */
    Iterator<Property> defaultStyles();

    /**
     * @param styleFamily
     * @return
     */
    String getDefaultStyleId(String styleFamily);

    /**
     * @param styleFamily
     * @return
     */
    IStyle getDefaultStyle(String styleFamily);

    /**
     * @param styleId
     * @return
     */
    IStyle getDefaultStyleById(String styleId);

    /**
     * @param styleFamily
     * @param styleId
     */
    void setDefaultStyleId(String styleFamily, String styleId);

}
