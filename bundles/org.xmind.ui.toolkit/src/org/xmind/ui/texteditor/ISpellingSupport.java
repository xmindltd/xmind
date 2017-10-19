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
package org.xmind.ui.texteditor;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.swt.widgets.Control;

public interface ISpellingSupport {

    ISpellingSupport NULL = new ISpellingSupport() {

        public void install(ITextViewer viewer) {
        }

        public void install(Control control, IControlContentAdapter2 adapter) {
        }

        public ISpellingActivation activateSpelling(ITextViewer viewer) {
            return null;
        }

        public ISpellingActivation activateSpelling(Control control,
                IControlContentAdapter2 adapter) {
            return null;
        }

        public void deactivateSpelling(ISpellingActivation activation) {
            // do nothing
        }

    };

    /**
     * 
     * @param textViewer
     */
    void install(ITextViewer textViewer);

    /**
     * 
     * @param control
     * @param adapter
     */
    void install(Control control, IControlContentAdapter2 adapter);

    /**
     * 
     * @param textViewer
     * @return
     */
    ISpellingActivation activateSpelling(ITextViewer textViewer);

    /**
     * 
     * @param control
     * @param adapter
     * @return
     */
    ISpellingActivation activateSpelling(Control control,
            IControlContentAdapter2 adapter);

    /**
     * 
     * @param activation
     */
    void deactivateSpelling(ISpellingActivation activation);

}