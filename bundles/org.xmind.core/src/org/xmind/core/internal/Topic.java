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
package org.xmind.core.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.xmind.core.IBoundary;
import org.xmind.core.ISheet;
import org.xmind.core.ISummary;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.core.marker.IMarkerRef;
import org.xmind.core.style.IStyle;
import org.xmind.core.util.Point;

/**
 * @author briansun
 * 
 */
public abstract class Topic extends AbstractWorkbookComponent
        implements ITopic {

    protected static final List<ITopic> NO_CHILDREN = Collections.emptyList();

    protected static final Set<String> NO_TYPES = Collections.emptySet();

    protected static final Set<IMarkerRef> NO_MARKER_REFS = Collections
            .emptySet();

    protected static final Set<IBoundary> NO_BOUNDARIES = Collections
            .emptySet();

    protected static final Set<ISummary> NO_SUMMARIES = Collections.emptySet();

    protected static final Set<String> NO_LABELS = Collections.emptySet();

    public String getStyleType() {
        return IStyle.TOPIC;
    }

    /**
     * Clients may override this method.
     * 
     * @see org.xmind.core.ITitled#getTitleText()
     */
    public String getTitleText() {
        String t = getLocalTitleText();
        return t == null ? "" : t; //$NON-NLS-1$
    }

    /**
     * @see org.xmind.core.ITitled#hasTitle()
     */
    public boolean hasTitle() {
        return getLocalTitleText() != null;
    }

    /**
     * @see org.xmind.core.ITopic#setPosition(org.xmind.core.util.Point)
     */
    public void setPosition(Point position) {
        if (position == null)
            removePosition();
        else
            setPosition(position.x, position.y);
    }

    /**
     * @see org.xmind.core.ITopic#hasPosition()
     */
    public boolean hasPosition() {
        return getPosition() != null;
    }

    /**
     * @see org.xmind.core.ITopic#getAllChildren()
     */
    public List<ITopic> getAllChildren() {
        Set<String> types = getChildrenTypes();
        ArrayList<ITopic> all = new ArrayList<ITopic>(types.size() * 10);
        for (String type : types) {
            all.addAll(getChildren(type));
        }
        return all;
    }

    public boolean isAttached() {
        return ITopic.ATTACHED.equals(getType());
    }

    public int getIndex() {
        ITopic parent = getParent();
        if (parent != null) {
            return parent.getChildren(getType()).indexOf(this);
        }
        return -1;
    }

    /**
     * @see org.xmind.core.ITopic#getOwnedSheet()
     */
    public ISheet getOwnedSheet() {
        return getPath().getSheet();
    }

    /**
     * @see org.xmind.core.ITopic#getOwnedWorkbook()
     */
    public IWorkbook getOwnedWorkbook() {
        return getPath().getWorkbook();
    }

    /**
     * @return
     */
    protected abstract String getLocalTitleText();

    /**
     * 
     */
    protected abstract void removePosition();

    public <T> T getAdapter(Class<T> adapter) {
        if (adapter == ISheet.class)
            return adapter.cast(getOwnedSheet());

        return super.getAdapter(adapter);
    }

    public String toString() {
        return "TOPIC (" + getTitleText() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
    }

}