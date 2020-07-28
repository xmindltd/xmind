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
package org.xmind.ui.internal.spreadsheet.structures;

import java.util.Collection;
import java.util.Collections;
import java.util.TreeSet;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Font;
import org.xmind.gef.draw2d.graphics.GraphicsUtils;
import org.xmind.ui.util.MindMapUtils;

public class ColumnHead implements Comparable<ColumnHead> {

    private static final Collection<String> EMPTY_LABELS = Collections
            .emptyList();

    public static final ColumnHead EMPTY = new ColumnHead(EMPTY_LABELS);

    private Collection<String> labels;

    private String text;

    private Font font;

    private Dimension prefSize;

    private int index;

    public ColumnHead(Collection<String> labels) {
        this.labels = new TreeSet<String>(labels);
    }

    public ColumnHead(String text) {
        this.labels = new TreeSet<String>(MindMapUtils.getLabels(text));
    }

    public Collection<String> getLabels() {
        return labels;
    }

    public void setLabels(Collection<String> labels) {
        this.labels = labels;
    }

    public boolean isEmpty() {
        return labels.isEmpty();
    }

    public Font getFont() {
        if (font == null)
            font = calcFont();
        return font;
    }

    private Font calcFont() {
        return JFaceResources.getDefaultFont();
    }

    public Dimension getPrefSize() {
        if (prefSize == null)
            prefSize = calcPrefSize();
        return prefSize;
    }

    private Dimension calcPrefSize() {
        String s = toString();
        if ("".equals(s)) //$NON-NLS-1$
            s = "X"; //$NON-NLS-1$
        return GraphicsUtils.getAdvanced().getTextSize(s, getFont());
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof ColumnHead))
            return false;
        ColumnHead that = (ColumnHead) obj;
        return this.labels.size() == that.labels.size()
                && this.labels.containsAll(that.labels);
    }

    public int hashCode() {
        return labels.hashCode();
    }

    public String toString() {
        if (text == null) {
            text = createText();
        }
        return text;
    }

    private String createText() {
        return MindMapUtils.getLabelText(labels);
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int compareTo(ColumnHead o) {
        return getIndex() - o.getIndex();
    }

}