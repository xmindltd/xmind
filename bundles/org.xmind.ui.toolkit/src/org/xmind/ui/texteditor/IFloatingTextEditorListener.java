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
package org.xmind.ui.texteditor;

public interface IFloatingTextEditorListener {

    public static class Stub implements IFloatingTextEditorListener {

        public void editingAboutToCancel(TextEvent e) {
        }

        public void editingAboutToFinish(TextEvent e) {
        }

        public void editingAboutToStart(TextEvent e) {
        }

        public void editingCanceled(TextEvent e) {
        }

        public void editingFinished(TextEvent e) {
        }

        public void editingStarted(TextEvent e) {
        }

        public void textAboutToChange(TextEvent e) {
        }

        public void textChanged(TextEvent e) {
        }

    }

    void editingAboutToStart(TextEvent e);

    void editingStarted(TextEvent e);

    void editingAboutToCancel(TextEvent e);

    void editingCanceled(TextEvent e);

    void editingAboutToFinish(TextEvent e);

    void editingFinished(TextEvent e);

    void textAboutToChange(TextEvent e);

    void textChanged(TextEvent e);

}