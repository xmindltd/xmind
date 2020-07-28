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

/**
 * @author Frank Shaka
 */
public class NullGraphicalPolicy extends AbstractGraphicalPolicy {

    private static final IGraphicalPolicy instance = new NullGraphicalPolicy();

    /**
     * 
     */
    private NullGraphicalPolicy() {
    }

    protected IStyleSelector createDefaultStyleSelector() {
        return NullStyleSelector.getInstance();
    }

    @Override
    protected IStructure createDefaultStructureAlgorithm() {
        return NullStructureAlgorithm.getInstance();
    }

    public static IGraphicalPolicy getInstance() {
        return instance;
    }

}