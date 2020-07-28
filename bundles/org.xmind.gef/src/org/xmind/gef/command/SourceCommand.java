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
package org.xmind.gef.command;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.xmind.gef.ArraySourceProvider;
import org.xmind.gef.ISourceProvider;
import org.xmind.gef.ISourceProvider2;

public class SourceCommand extends Command implements ISourceProvider2 {

    protected static final List<Object> NO_SOURCES = Collections.emptyList();

    private ISourceProvider sourceDelegate;

    private boolean sourceCollectable = true;

    public SourceCommand(Object source) {
        Assert.isNotNull(source);
        this.sourceDelegate = new ArraySourceProvider(source);
    }

    public SourceCommand(Collection<?> sources) {
        Assert.isNotNull(sources);
        for (Object source : sources) {
            Assert.isNotNull(source);
        }
        this.sourceDelegate = new ArraySourceProvider(sources);
    }

    public SourceCommand(ISourceProvider sourceDelegate) {
        this.sourceDelegate = sourceDelegate;
    }

    protected SourceCommand() {
        this.sourceDelegate = null;
    }

    public boolean hasSource() {
        return sourceDelegate != null && sourceDelegate.hasSource();
    }

    public List<Object> getSources() {
        if (sourceDelegate == null) {
            return NO_SOURCES;
        }
        return sourceDelegate.getSources();
    }

    public Object getSource() {
        return sourceDelegate == null ? null : sourceDelegate.getSource();
    }

    public void setSourceDelegate(ISourceProvider sourceProvider) {
        this.sourceDelegate = sourceProvider;
    }

    public ISourceProvider getSourceDelegate() {
        return sourceDelegate;
    }

    protected void setSource(Object newSource) {
        Assert.isNotNull(newSource);
        if (sourceDelegate == null)
            sourceDelegate = new ArraySourceProvider();
        if (sourceDelegate instanceof ArraySourceProvider) {
            ((ArraySourceProvider) sourceDelegate).setSource(newSource);
        }
    }

    protected void setSources(Collection<?> newSources) {
        Assert.isNotNull(newSources);
        for (Object source : newSources) {
            Assert.isNotNull(source);
        }
        if (sourceDelegate == null)
            sourceDelegate = new ArraySourceProvider();
        if (sourceDelegate instanceof ArraySourceProvider) {
            ((ArraySourceProvider) sourceDelegate).setSources(newSources);
        }
    }

    protected void addSource(Object newSource) {
        Assert.isNotNull(newSource);
        if (sourceDelegate == null)
            sourceDelegate = new ArraySourceProvider();
        if (sourceDelegate instanceof ArraySourceProvider) {
            ((ArraySourceProvider) sourceDelegate).addSource(newSource);
        }
    }

    protected void addSources(Collection<?> newSources) {
        Assert.isNotNull(newSources);
        for (Object source : newSources) {
            Assert.isNotNull(source);
        }
        if (sourceDelegate == null)
            sourceDelegate = new ArraySourceProvider();
        if (sourceDelegate instanceof ArraySourceProvider) {
            ((ArraySourceProvider) sourceDelegate).addSources(newSources);
        }
    }

    public boolean isSourceCollectable() {
        return sourceCollectable;
    }

    public void setSourceCollectable(boolean sourceCollectable) {
        this.sourceCollectable = sourceCollectable;
    }

}