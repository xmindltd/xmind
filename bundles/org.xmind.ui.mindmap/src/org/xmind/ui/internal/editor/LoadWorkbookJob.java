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

public class LoadWorkbookJob {
//    extends Job implements IEncryptionHandler {
//
//    private String inputName;
//
//    private WorkbookRef workbookRef;
//
//    private IDialogPaneContainer dialogContainer;
//
//    private Display display;
//
//    private String password = null;
//
//    private boolean firstTry = true;
//
//    private IProgressMonitor progress;
//
//    public LoadWorkbookJob(String inputName, WorkbookRef workbookRef,
//            IDialogPaneContainer dialogContainer, Display display) {
//        super(NLS.bind(MindMapMessages.LoadWorkbookJob_text, inputName));
//        this.inputName = inputName;
//        this.workbookRef = workbookRef;
//        this.dialogContainer = dialogContainer;
//        this.display = display;
//    }
//
//    @Override
//    protected IStatus run(IProgressMonitor monitor) {
//        this.progress = monitor;
//        monitor.beginTask(null, 100);
//
//        if (workbookRef == null)
//            return Status.CANCEL_STATUS;
//
//        IStorage storage = WorkbookRef.createStorage();
//        boolean wrongPassword;
//        Throwable error = null;
//        do {
//            wrongPassword = false;
//            password = null;
//            storage.clear();
//
//            if (workbookRef == null)
//                return Status.CANCEL_STATUS;
//
//            try {
//                workbookRef.loadWorkbook(storage, this, monitor);
//            } catch (Throwable e) {
//                CoreException coreEx = getCoreException(e);
//                if (coreEx != null) {
//                    int errType = coreEx.getType();
//                    if (errType == Core.ERROR_CANCELLATION) {
//                        return Status.CANCEL_STATUS;
//                    }
//                    if (errType == Core.ERROR_WRONG_PASSWORD) {
//                        wrongPassword = true;
//                    }
//                }
//                if (!wrongPassword) {
//                    error = e;
//                    Logger.log(e);
//                }
//            }
//            firstTry = false;
//        } while (wrongPassword);
//
//        if (workbookRef.getWorkbook() == null) {
//            if (error == null) {
//                try {
//                    throw new FileNotFoundException(inputName);
//                } catch (Throwable e) {
//                    error = e;
//                }
//            }
//            return new Status(IStatus.WARNING, MindMapUI.PLUGIN_ID,
//                    IStatus.ERROR,
//                    MindMapMessages.LoadWorkbookJob_errorDialog_title, error);
//        }
//
//        return Status.OK_STATUS;
//    }
//
//    private CoreException getCoreException(Throwable e) {
//        if (e == null)
//            return null;
//        if (e instanceof CoreException)
//            return (CoreException) e;
//        return getCoreException(e.getCause());
//    }
//
//    public String retrievePassword() throws CoreException {
//        if (password == null) {
//            boolean canceled = !doRetrievePassword();
//            if (canceled) {
//                throw new CoreException(Core.ERROR_CANCELLATION);
//            }
//        }
//        return password;
//    }
//
//    @Override
//    protected void canceling() {
//        super.canceling();
//        display.asyncExec(new Runnable() {
//            public void run() {
//                dialogContainer.close(IDialogPane.CANCEL);
//            }
//        });
//    }
//
//    private boolean doRetrievePassword() {
//        final boolean[] ret = new boolean[1];
//        display.syncExec(new Runnable() {
//            public void run() {
//                if (progress != null) {
//                    progress.worked(10);
//                    progress.subTask(MindMapMessages.LoadWorkbookJob_retrive_password_message);
//                }
//
//                String message;
//                if (firstTry) {
//                    message = MindMapMessages.LoadWorkbookJob_firstTry_message;
//                } else {
//                    message = MindMapMessages.LoadWorkbookJob_moreTry_message;
//                }
//                DecryptionDialogPane dialog = new DecryptionDialogPane();
//                dialog.setContent(message, !firstTry);
//                int code = dialogContainer.open(dialog);
//                if (code == DecryptionDialogPane.OK) {
//                    password = dialog.getPassword();
//                    ret[0] = true;
//                } else {
//                    cancel();
//                    ret[0] = false;
//                }
//            }
//        });
//        return ret[0];
//    }

}