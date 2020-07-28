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
package org.xmind.core.command.arguments;

import java.util.Map;

import org.eclipse.core.runtime.Assert;

/**
 * This class provides a mechanism to normalize indexed objects and their
 * properties into key-value pairs.
 * 
 * <p>
 * The keys look like <code>"prefix-index"</code> or
 * <code>"prefix-index-subkey"</code>, where <code>prefix</code> is an
 * identifier to protect all objects under a same group, <code>index</code> is
 * the index of the object, <code>subkey</code> is for storing properties of the
 * object.
 * </p>
 * 
 * <p>
 * The <code>"prefix-index"</code> key is used to map an object alone, while the
 * <code>"prefix-index-subkey"</code> key is used to map a property of an
 * object.
 * </p>
 * 
 * <p>
 * To write objects into a map, iterate over the collection of objects and set
 * each object/property into the <code>ArrayMapper</code>. To read objects from
 * a map, iterate over the <code>ArrayMapper</code> and get each object/property
 * from it.
 * </p>
 * 
 * <p>
 * <b>Note</b> that the method <code>next()</code> should be called at the
 * beginning of each iteration step to make the index cursor point to the next
 * object.
 * </p>
 * 
 * <p>
 * <b>Also note</b> that the method <code>setSize()</code> should be called
 * after writing objects into the map is finished, otherwise the map will be
 * unable to read objects from.
 * </p>
 * 
 * <p>
 * Samples:
 * 
 * <pre>
 * Map writeElementsIntoMap(Collection elements) {
 *     Map data = new HashMap();
 *     ArrayMapper writer = new ArrayMapper(data, &quot;elements&quot;);
 *     Iterator it = elements.iterator();
 *     while (it.hasNext()) {
 *         Element element = (Element) it.next();
 *         writer.next();
 *         writer.set(&quot;content&quot;, element.getContent());
 *         writer.set(&quot;time&quot;, element.getTime());
 *     }
 *     writer.setSize(); //DO NOT FORGET THIS LINE!
 *     return data;
 * }
 * 
 * Collection readElementsFromMap(Map data) {
 *     Collection elements = new ArrayList();
 *     ArrayMapper reader = new ArrayMapper(data, &quot;elements&quot;);
 *     while (reader.hasNext()) {
 *         reader.next();
 *         Element element = new Element();
 *         element.setContent(reader.get(&quot;content&quot;));
 *         element.setTime(reader.get(&quot;time&quot;));
 *         elements.add(element);
 *     }
 *     return elements;
 * }
 * </pre>
 * 
 * </p>
 * 
 * @author Frank Shaka
 * 
 */
public class ArrayMapper {

    @SuppressWarnings("rawtypes")
    private Map map;

    private String prefix;

    private int index = -1;

    /**
     * Constructs an <code>ArrayMapper</code> instance using the map and prefix.
     * 
     * @param map
     *            the map to store/load objects
     * @param prefix
     *            the key prefix of all objects to map, while <code>null</code>
     *            or an empty string indicates no prefix
     */
    @SuppressWarnings("rawtypes")
    public ArrayMapper(Map map, String prefix) {
        Assert.isNotNull(map);
        this.map = map;
        this.prefix = prefix;
    }

    /**
     * Checks if there are more objects to read.
     * 
     * @return <code>true</code> if there are more objects to read, or
     *         <code>false</code> otherwise
     */
    public boolean hasNext() {
        return index + 1 < getSize();
    }

    /**
     * Moves the index cursor forward to next object.
     * <p>
     * <b>NOTE:</b> This method should be called at the beginning of each
     * iteration step.
     * </p>
     */
    public void next() {
        index++;
    }

    /**
     * Manually sets the index cursor to a specific position. This method is not
     * recommended unless you really want to roll back or skip forward. To move
     * the index cursor one step forward, use <code>next()</code> instead.
     * 
     * <p>
     * Note that no <code>IndexOutOfBoundsException</code> will be raised if the
     * index is set to an illegal value, so it's the client's responsibility to
     * set a proper value to prevent reading/writing failure.
     * </p>
     * 
     * @param index
     *            the new position of the index cursor
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * Puts the object at the current index into the map with the
     * <code>"prefix-index"</code> key.
     * 
     * @param value
     *            the object to put
     */
    @SuppressWarnings("unchecked")
    public void set(Object value) {
        map.put(getKey(null), value);
    }

    /**
     * Puts the property of the object at the current index into the map with
     * the <code>"prefix-index-subkey"</code> key.
     * 
     * @param subkey
     *            the sub-key of this property
     * @param value
     *            the property to put
     */
    @SuppressWarnings("unchecked")
    public void set(String subkey, Object value) {
        map.put(getKey(subkey), value);
    }

    /**
     * Retrieves the object at the current index from the map specified by the
     * <code>"prefix-index"</code> key.
     * 
     * @return the object at the current index
     */
    public Object get() {
        return map.get(getKey(null));
    }

    /**
     * Retrieves the property of the object at the current index from map
     * specified by the <code>"prefix-index-subkey"</code> key.
     * 
     * @param subkey
     *            the sub-key of this property
     * @return the disired property of the object at the current index
     */
    public Object get(String subkey) {
        return map.get(getKey(subkey));
    }

    /**
     * Generates a key using the prefix, the current index and the sub-key.
     * 
     * @param subkey
     *            the sub-key to use, or <code>null</code> to indicate no
     *            sub-key
     * @return <code>"prefix-index-subkey"</code> if prefix and subkey are
     *         non-empty, <code>"prefix-index"</code> if prefix is non-empty and
     *         subkey is empty, <code>"index-subkey"</code> if prefix is empty
     *         and subkey is non-empty, or <code>"index"</code> is prefix and
     *         subkey are empty
     */
    private String getKey(String subkey) {
        if (prefix == null || "".equals(prefix)) //$NON-NLS-1$
            return subkey == null || "".equals(subkey) //$NON-NLS-1$
            ? String.valueOf(index) : String.valueOf(index) + "-" + subkey; //$NON-NLS-1$
        return subkey == null || "".equals(subkey) //$NON-NLS-1$
        ? prefix + "-" + index //$NON-NLS-1$
        : prefix + "-" + index + "-" + subkey; //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Generates the key for the <code>size</code> value.
     * 
     * @return <code>"prefix-size"</code> if prefix is non-empty, or
     *         <code>"size"</code> otherwise
     */
    private String getSizeKey() {
        if (prefix == null || "".equals(prefix)) { //$NON-NLS-1$
            return "size"; //$NON-NLS-1$
        } else {
            return prefix + "-size"; //$NON-NLS-1$
        }
    }

    /**
     * Stores the current size into the map.
     * <p>
     * <b>NOTE:</b> This method should be called after all objects are written
     * into the map. Failing to do so will lead to unability to read objects
     * from the map.
     * </p>
     */
    public void setSize() {
        setSize(index + 1);
    }

    /**
     * Manually stores the size to a specific value. This method is not
     * recommended unless you have manually changed the index cursor using
     * <code>setIndex(int)</code>, otherwise use <code>setSize()</code> instead.
     * 
     * @param size
     *            the new size value
     */
    @SuppressWarnings("unchecked")
    public void setSize(int size) {
        map.put(getSizeKey(), String.valueOf(size));
    }

    /**
     * Gets the number of the objects stored in the map.
     * 
     * @return the number of all objects, or <code>0</code> if retrieving is
     *         failed.
     */
    public int getSize() {
        Object size = map.get(getSizeKey());
        if (size == null || !(size instanceof String))
            return 0;
        try {
            return Integer.parseInt((String) size, 10);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

}
