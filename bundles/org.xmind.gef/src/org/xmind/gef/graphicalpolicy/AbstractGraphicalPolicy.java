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
public abstract class AbstractGraphicalPolicy implements IGraphicalPolicy {

    /**
     * The default style selector.
     */
    private IStyleSelector defaultStyleSelector = null;

    /**
     * The default structure algorithm.
     */
    private IStructure defaultStructureAlgorithm = null;

    /**
     * Returns a default style selector for any graphical part. Subclasses may
     * extend this method and return their specific style selector depending on
     * the given graphical part.
     */
    public IStyleSelector getStyleSelector(IGraphicalPart part) {
        if (defaultStyleSelector == null)
            defaultStyleSelector = createDefaultStyleSelector();
        return defaultStyleSelector;
    }

    /**
     * Creates the default style selector. This method will be called only once
     * during the lifecycle of this class.
     * <p>
     * Must NOT return <code>null</code>.
     * </p>
     * 
     * @return
     */
    protected abstract IStyleSelector createDefaultStyleSelector();

    /**
     * Returns a default structure algorithm for any graphical part. Subclasses
     * may extend this method and return their specific structure algorithm
     * depending on the given graphical part.
     */
    public IStructure getStructure(IGraphicalPart part) {
        if (defaultStructureAlgorithm == null)
            defaultStructureAlgorithm = createDefaultStructureAlgorithm();
        return defaultStructureAlgorithm;
    }

    /**
     * Creates the default structure algorithm. This method will be called only
     * once during the lifecycle of this class.
     * <p>
     * Must NOT return <code>null</code>.
     * </p>
     * 
     * @return
     */
    protected abstract IStructure createDefaultStructureAlgorithm();

    /**
     * Implements to do nothing. Subclasses may extend this method to do some
     * job (adding listeners, setting caches, etc...) when activated on the
     * given part.
     */
    public void activate(IGraphicalPart part) {
    }

    /**
     * Implements to do nothing. Subclasses may extend this method to do some
     * job (removing listeners, flushing caches, etc...) when deactivated on the
     * given part.
     */
    public void deactivate(IGraphicalPart part) {
    }

}