/* ******************************************************************************
 * Copyright (c) 2006-2016 XMind Ltd. and others.
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
/**
 * 
 */
package org.xmind.core.net;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Assert;

/**
 * @author Frank Shaka
 * @since 3.6.50
 */
public class FieldSet implements Iterable<Field> {

    private final List<Field> fields;

    /**
     * 
     */
    public FieldSet() {
        this.fields = new ArrayList<Field>();
    }

    /**
     * 
     */
    public FieldSet(Collection<Field> fields) {
        this.fields = new ArrayList<Field>(fields);
    }

    public FieldSet(FieldSet source) {
        this.fields = (source == null) ? new ArrayList<Field>()
                : new ArrayList<Field>(source.fields);
    }

    public boolean has(String name) {
        if (name == null)
            return false;
        for (Field field : fields) {
            if (name.equalsIgnoreCase(field.name))
                return true;
        }
        return false;
    }

    public FieldSet put(String name, Object value) {
        Assert.isLegal(name != null);
        remove(name);
        add(name, value);
        return this;
    }

    public FieldSet add(String name, Object value) {
        Assert.isLegal(name != null);
        if (value != null)
            fields.add(new Field(name, value));
        return this;
    }

    public FieldSet putAll(FieldSet source) {
        if (source != null) {
            for (Field field : source.fields) {
                remove(field.name);
                fields.add(field);
            }
        }
        return this;
    }

    public FieldSet addAll(FieldSet source) {
        if (source != null) {
            fields.addAll(source.fields);
        }
        return this;
    }

    public FieldSet remove(String name) {
        Assert.isLegal(name != null);
        Iterator<Field> it = fields.iterator();
        while (it.hasNext()) {
            Field field = it.next();
            if (name.equalsIgnoreCase(field.name)) {
                it.remove();
            }
        }
        return this;
    }

    public Object get(String name) {
        if (name != null) {
            for (Field field : fields) {
                if (name.equalsIgnoreCase(field.name))
                    return field.value;
            }
        }
        return null;
    }

    public Field get(int index) {
        if (index < 0 || index >= fields.size())
            return null;
        return fields.get(index);
    }

    public String getString(String name) {
        Object value = get(name);
        return value != null && value instanceof String ? (String) value : null;
    }

    public int getInt(String name, int defaultValue) {
        Object value = get(name);
        return value != null && value instanceof Integer
                ? ((Integer) value).intValue() : defaultValue;
    }

    /**
     * @return the headers
     */
    public Collection<Field> toList() {
        return Collections.unmodifiableCollection(fields);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Iterable#iterator()
     */
    public Iterator<Field> iterator() {
        final Iterator<Field> it = fields.iterator();
        return new Iterator<Field>() {

            public void remove() {
                throw new UnsupportedOperationException();
            }

            public Field next() {
                return it.next();
            }

            public boolean hasNext() {
                return it.hasNext();
            }
        };
    }

    public boolean isEmpty() {
        return fields.isEmpty();
    }

    public int size() {
        return fields.size();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        for (Field field : fields) {
            buffer.append(field.name);
            buffer.append(':');
            buffer.append(' ');
            buffer.append(field.value);
            buffer.append('\r');
            buffer.append('\n');
        }
        return buffer.toString();
    }

    public String toSemicolonSeparatedString(boolean quoteValues) {
        StringBuilder buffer = new StringBuilder();
        for (Field field : fields) {
            if (buffer.length() > 0) {
                buffer.append(';');
                buffer.append(' ');
            }
            buffer.append(field.name);
            buffer.append('=');
            if (quoteValues) {
                buffer.append('"');
            }
            buffer.append(field.value);
            if (quoteValues) {
                buffer.append('"');
            }
        }
        return buffer.toString();
    }

    public static FieldSet fromSemicolonSeparatedString(String str) {
        FieldSet set = new FieldSet();
        String name = null;
        String value = null;
        StringBuilder buffer = null;
        boolean inQuote = false;

        int size = str.length();
        for (int i = 0; i < size; i++) {
            char c = str.charAt(i);
            if (c == ' ') {
                if (inQuote) {
                    // add it to buffer
                } else {
                    if (buffer == null) {
                        // skip white spaces
                    } else if (name == null) {
                        // name ends, value starts
                        name = buffer.toString();
                        buffer = null;
                        value = null;
                    } else {
                        // value ends
                        value = (buffer == null) ? "" : buffer.toString(); //$NON-NLS-1$
                        set.add(name, value);
                        name = null;
                        value = null;
                        buffer = null;
                    }
                    continue;
                }
            } else if (c == '"') {
                inQuote = !inQuote;
                continue;
            } else if (c == '=') {
                if (inQuote) {
                    // add it to buffer
                } else {
                    // name ends, value starts
                    name = (buffer == null) ? "" : buffer.toString(); //$NON-NLS-1$
                    buffer = null;
                    value = null;
                    continue;
                }
            } else if (c == ';') {
                if (inQuote) {
                    // add it to buffer
                } else {
                    // value ends, name starts
                    if (buffer != null) {
                        if (name == null) {
                            name = buffer.toString();
                            value = ""; //$NON-NLS-1$
                        } else {
                            value = buffer.toString();
                        }
                    }
                    if (name != null) {
                        set.add(name, value == null ? "" : value); //$NON-NLS-1$
                    }
                    name = null;
                    value = null;
                    buffer = null;
                    continue;
                }
            }

            if (buffer == null) {
                buffer = new StringBuilder();
            }
            buffer.append(c);
        }

        if (buffer != null) {
            if (name == null) {
                name = buffer.toString();
                set.add(name, ""); //$NON-NLS-1$
            } else {
                value = buffer.toString();
                set.add(name, value);
            }
        } else if (name != null) {
            set.add(name, ""); //$NON-NLS-1$
        }

        return set;
    }

}
