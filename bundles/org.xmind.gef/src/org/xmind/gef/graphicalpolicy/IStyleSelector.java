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
package org.xmind.gef.graphicalpolicy;

import org.xmind.gef.part.IGraphicalPart;

/**
 * @author Frank Shaka
 */
public interface IStyleSelector {

    /**
     * Selects a proper style value for this part by the given key.
     * <p>
     * An implementor may take into account following ways of acquiring a value:
     * <br>
     * <ul>
     * <li>defined by user (may be directly provided by the part's model);</li>
     * <li>inherited from the parent part;</li>
     * <li>derived from parent styles;</li>
     * <li>provided by a default style sheet;</li>
     * <li>restricted by other specific attributes, e.g. position, layout,
     * index, etc.</li>
     * </ul>
     * </p>
     * <p>
     * The order of the above methods are not specified and none of them is
     * guaranteed to be performed by an implementor, but commonly it's a good
     * practice to try fetching user-defined value before the others unless
     * there's some sort of restrictions encountered. If there's no value
     * acquirable from any of the above sources, <code>null</code> may be
     * returned.
     * </p>
     * <p>
     * 
     * </p>
     * 
     * @param part
     *            The part on which the returned style is applied
     * @param key
     *            The key to find the value
     * @return A value specified by this style selector; may by
     *         <code>null</code>
     */
    String getStyleValue(IGraphicalPart part, String key);

    /**
     * Selects a proper style value for this part by the given key.
     * <p>
     * An implementor may take into account following ways of acquiring a value:
     * <br>
     * <ul>
     * <li>defined by user (may be directly provided by the part's model);</li>
     * <li>inherited from the parent part;</li>
     * <li>derived from parent styles;</li>
     * <li>provided by a default style sheet;</li>
     * <li>restricted by other specific attributes, e.g. position, layout,
     * index, etc.</li>
     * </ul>
     * </p>
     * <p>
     * The order of the above methods are not specified and none of them is
     * guaranteed to be performed by an implementor, but commonly it's a good
     * practice to try fetching user-defined value before the others unless
     * there's some sort of restrictions encountered. If there's no value
     * acquirable from any of the above sources, <code>null</code> may be
     * returned.
     * </p>
     * 
     * @param part
     *            The part on which the returned style is applied
     * @param key
     *            The key to find the value
     * @param defaultValueProvider
     *            The default value provider
     * @return A value specified by this style selector; may by
     *         <code>null</code>
     */
    String getStyleValue(IGraphicalPart part, String key,
            IStyleValueProvider defaultValueProvider);

    /**
     * Gets the user-defined value for this part by the given key.
     * 
     * @param part
     *            The part on which the returned style is applied
     * @param key
     *            The key to find the value
     * @return A user-defined value acquired from the part
     */
    String getUserValue(IGraphicalPart part, String key);

    /**
     * Selects a value that's not defined by user for this part by the given
     * key.
     * 
     * @param part
     *            The part on which the returned style is applied
     * @param key
     *            The key to find the value
     * @return A value that's not defined by user
     */
    String getAutoValue(IGraphicalPart part, String key);

    /**
     * 
     * @param part
     * @param key
     * @param defaultValue
     * @return
     */
    String getAutoValue(IGraphicalPart part, String key,
            IStyleValueProvider defaultValueProvider);

    String getOverridedValue(IGraphicalPart part, String key, String layerName);

}