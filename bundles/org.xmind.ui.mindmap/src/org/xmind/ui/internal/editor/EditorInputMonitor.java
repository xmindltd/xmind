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
package org.xmind.ui.internal.editor;

/**
 * @author Frank Shaka
 * 
 */
public class EditorInputMonitor {
//implements ShellListener, IPropertyListener {
//
//    private IEditorPart editor;
//
//    private Shell shell;
//
//    private Boolean oldValue = null;
//
//    /**
//     * 
//     */
//    public EditorInputMonitor(IEditorPart editor) {
//        this.editor = editor;
//        this.editor.addPropertyListener(this);
//        this.shell = editor.getSite().getShell();
//        if (shell != null && !shell.isDisposed()) {
//            shell.addShellListener(this);
//        }
//    }
//
//    public void dispose() {
//        editor.removePropertyListener(this);
//        if (shell != null) {
//            if (!shell.isDisposed()) {
//                shell.removeShellListener(this);
//            }
//            shell = null;
//        }
//    }
//
//    /**
//     * 
//     */
//    private void checkTarget() {
//        if (oldValue == null) {
//            recordOldValue();
//        } else {
//            boolean newValue = willOverwriteTarget();
//            if (oldValue.booleanValue() != newValue) {
//                addDirtyMarker();
//                oldValue = Boolean.valueOf(newValue);
//            }
//        }
//    }
//
//    private void addDirtyMarker() {
//        IWorkbook workbook = (IWorkbook) editor.getAdapter(IWorkbook.class);
//        if (workbook instanceof ICoreEventSource2) {
//            ((ICoreEventSource2) workbook).registerOnceCoreEventListener(
//                    Core.WorkbookPreSaveOnce, ICoreEventListener.NULL);
//        }
//    }
//
//    /**
//     * 
//     */
//    private void recordOldValue() {
//        oldValue = Boolean.valueOf(willOverwriteTarget());
//    }
//
//    private boolean willOverwriteTarget() {
//        WorkbookRef ref = (WorkbookRef) editor.getAdapter(IWorkbookRef.class);
//        return ref != null && ref.willOverwriteTarget();
//    }
//
//    /*
//     * (non-Javadoc)
//     * 
//     * @see
//     * org.eclipse.swt.events.ShellListener#shellActivated(org.eclipse.swt.events
//     * .ShellEvent)
//     */
//    public void shellActivated(ShellEvent e) {
//        checkTarget();
//    }
//
//    /*
//     * (non-Javadoc)
//     * 
//     * @see
//     * org.eclipse.swt.events.ShellListener#shellClosed(org.eclipse.swt.events
//     * .ShellEvent)
//     */
//    public void shellClosed(ShellEvent e) {
//    }
//
//    /*
//     * (non-Javadoc)
//     * 
//     * @see
//     * org.eclipse.swt.events.ShellListener#shellDeactivated(org.eclipse.swt
//     * .events.ShellEvent)
//     */
//    public void shellDeactivated(ShellEvent e) {
//        recordOldValue();
//    }
//
//    /*
//     * (non-Javadoc)
//     * 
//     * @see
//     * org.eclipse.swt.events.ShellListener#shellDeiconified(org.eclipse.swt
//     * .events.ShellEvent)
//     */
//    public void shellDeiconified(ShellEvent e) {
//    }
//
//    /*
//     * (non-Javadoc)
//     * 
//     * @see
//     * org.eclipse.swt.events.ShellListener#shellIconified(org.eclipse.swt.events
//     * .ShellEvent)
//     */
//    public void shellIconified(ShellEvent e) {
//    }
//
//    public void propertyChanged(Object source, int propId) {
//        if (propId == IEditorPart.PROP_INPUT) {
//            recordOldValue();
//        }
//    }
//
}
