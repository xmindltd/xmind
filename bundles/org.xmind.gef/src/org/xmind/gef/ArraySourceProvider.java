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
package org.xmind.gef;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 
 * @author Frank Shaka
 * 
 */
public class ArraySourceProvider implements ISourceProvider {

    private List<Object> sources;

    public ArraySourceProvider() {
        this.sources = new ArrayList<Object>();
    }

    public ArraySourceProvider(Object source) {
        this.sources = new ArrayList<Object>();
        addSource(source);
    }

    public ArraySourceProvider(Collection<?> sources) {
        this.sources = new ArrayList<Object>(sources.size());
        addSources(sources);
    }

    public List<Object> getSources() {
        return sources;
    }

    public Object getSource() {
        return sources.isEmpty() ? null : sources.get(0);
    }

    public boolean hasSource() {
        return !sources.isEmpty();
    }

    public void setSources(Collection<?> newSources) {
        this.sources.clear();
        addSources(newSources);
    }

    public void setSource(Object source) {
        this.sources.clear();
        addSource(source);
    }

    public void addSources(Collection<?> newSources) {
        for (Object source : newSources) {
            addSource(source);
        }
    }

    public void addSource(Object source) {
        if (source != null && !this.sources.contains(source)) {
            this.sources.add(source);
        }
    }

    public void removeSource(Object source) {
        this.sources.remove(source);
    }

    public void removeSources(Collection<Object> sources) {
        this.sources.removeAll(sources);
    }

    public void removeAllSources() {
        this.sources.clear();
    }

}