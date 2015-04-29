package org.xmind.ui.internal.evernote.export;

import static org.xmind.ui.evernote.EvernotePlugin.PLUGIN_ID;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.xmind.ui.evernote.EvernotePlugin;
import org.xmind.ui.evernote.EvernoteStore;
import org.xmind.ui.evernote.signin.IEvernoteAccount;
import org.xmind.ui.internal.evernote.EvernoteMessages;
import org.xmind.ui.mindmap.IMindMapViewer;

import com.evernote.clients.NoteStoreClient;
import com.evernote.clients.UserStoreClient;
import com.evernote.edam.error.EDAMErrorCode;
import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.limits.Constants;
import com.evernote.edam.type.BusinessUserInfo;
import com.evernote.edam.type.Notebook;
import com.evernote.edam.type.PremiumInfo;
import com.evernote.edam.type.User;
import com.evernote.thrift.TException;
import com.evernote.thrift.transport.TTransportException;

public class EvernoteExportJob extends Job {

    private IMindMapViewer viewer;

    private IEvernoteAccount accountInfo;

    private User user;

    public EvernoteExportJob(IMindMapViewer viewer, IEvernoteAccount accountInfo) {
        super(
                NLS.bind(
                        EvernoteMessages.EvernoteExportJob_jobName_withCentralTopicTitle,
                        viewer.getCentralTopic().getTitleText()));
        this.viewer = viewer;
        this.accountInfo = accountInfo;
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        if (!PlatformUI.isWorkbenchRunning())
            return new Status(IStatus.ERROR, EvernotePlugin.PLUGIN_ID,
                    "No workbench is running."); //$NON-NLS-1$

        final IWorkbench workbench = PlatformUI.getWorkbench();
        if (workbench == null)
            return new Status(IStatus.ERROR, EvernotePlugin.PLUGIN_ID,
                    "No workbench is available."); //$NON-NLS-1$

        final Display display = workbench.getDisplay();
        if (display == null || display.isDisposed())
            return new Status(IStatus.ERROR, EvernotePlugin.PLUGIN_ID,
                    "No display is available."); //$NON-NLS-1$

        final EvernoteStore evernoteStore = new EvernoteStore(accountInfo);
        final NoteStoreClient noteStore;
        final UserStoreClient userStore;
        final List<Notebook> notebooks;
        try {
            noteStore = evernoteStore.getNoteStore();
            userStore = evernoteStore.getUserStore();

            notebooks = noteStore.listNotebooks();
            user = userStore.getUser();
        } catch (EDAMUserException e) {
            return error(e);
        } catch (EDAMSystemException e) {
            return error(e);
        } catch (TTransportException e) {
            return error(e);
        } catch (TException e) {
            return new Status(IStatus.WARNING, PLUGIN_ID, e.getMessage(), e);
        }

        final EvernoteExportDialog dialog[] = new EvernoteExportDialog[1];
        final int[] result = new int[1];

        display.syncExec(new Runnable() {
            public void run() {
                IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
                final Shell shell = window == null ? display.getActiveShell()
                        : window.getShell();
                if (shell != null)
                    shell.setActive();

                dialog[0] = new EvernoteExportDialog(shell, notebooks,
                        evernoteStore.getEvernoteService());
                result[0] = dialog[0].open();
            }
        });

        if (result[0] == EvernoteExportDialog.OK) {
            try {
                new EvernoteExporter(viewer, noteStore).export();
            } catch (EDAMSystemException e) {
                return error(e);
            } catch (EDAMUserException e) {
                return error(e);
            } catch (TTransportException e) {
                return error(e);
            } catch (TException e) {
                e.printStackTrace();
            } catch (EDAMNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            return Status.CANCEL_STATUS;
        }

        return Status.OK_STATUS;
    }

    private String getErrorMessage(EDAMErrorCode errorCode, String message) {
        if (errorCode == null || errorCode.equals(EDAMErrorCode.UNKNOWN))
            return EvernoteMessages.EvernoteExportJob_UnknownError_message;
        if (errorCode.equals(EDAMErrorCode.AUTH_EXPIRED))
            return EvernoteMessages.EvernoteExportJob_AuthExpired_message;
        if (errorCode.equals(EDAMErrorCode.INVALID_AUTH))
            return EvernoteMessages.EvernoteExportJob_InvalidAuth_message;
        if (errorCode.equals(EDAMErrorCode.QUOTA_REACHED))
            return NLS
                    .bind(EvernoteMessages.EvernoteExportJob_QuotaReached_message_withMonthlyQuota,
                            getMonthlyQuota());
        if (errorCode.equals(EDAMErrorCode.LIMIT_REACHED))
            return NLS
                    .bind(EvernoteMessages.EvernoteExportJob_LimitReached_message_withMaximumNoteSize,
                            getMaximumNoteSize());
        if (message != null)
            return NLS
                    .bind(EvernoteMessages.EvernoteExportJob_OtherException_message_withErrorMessage,
                            message);
        else
            return NLS
                    .bind(EvernoteMessages.EvernoteExportJob_systemErrorText_withErrorMessage,
                            errorCode.toString());
    }

    private IStatus error(EDAMUserException e) {
        EDAMErrorCode errorCode = e.getErrorCode();
        return new Status(IStatus.WARNING, PLUGIN_ID, errorCode.getValue(),
                getErrorMessage(errorCode, e.getParameter()), e);
    }

    private IStatus error(EDAMSystemException e) {
        EDAMErrorCode errorCode = e.getErrorCode();
        return new Status(IStatus.WARNING, PLUGIN_ID, errorCode.getValue(),
                getErrorMessage(errorCode, null), e);
    }

    private IStatus error(TTransportException e) {
        return new Status(
                IStatus.WARNING,
                PLUGIN_ID,
                NLS.bind(
                        EvernoteMessages.EvernoteExportJob_networkErrorText_withErrorMessage,
                        e.getMessage()), e);
    }

    private long getMonthlyQuota() {
        long monthlyQuota = user.getAccounting().getUploadLimit();
        return byteToMB(monthlyQuota);
    }

    private long getMaximumNoteSize() {
        PremiumInfo info = user.getPremiumInfo();
        BusinessUserInfo businessInfo = user.getBusinessUserInfo();

        if (info != null && info.isPremium()) {
            return byteToMB(Constants.EDAM_NOTE_SIZE_MAX_PREMIUM);
        } else if (businessInfo != null) {
            return byteToMB(Constants.EDAM_NOTE_SIZE_MAX_PREMIUM);
        }
        return byteToMB(Constants.EDAM_NOTE_SIZE_MAX_FREE);
    }

    private int byteToMB(long bytes) {
        return (int) bytes / 1024 / 1024;
    }
}
