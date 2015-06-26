package org.xmind.ui.internal.biggerplate.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.xmind.ui.biggerplate.BiggerplateMessages;
import org.xmind.ui.biggerplate.BiggerplatePlugin;
import org.xmind.ui.internal.biggerplate.BiggerplateOauth;
import org.xmind.ui.internal.biggerplate.Info;

public class AuthoriseJob extends CancelableJob {

    private Info info;

    public AuthoriseJob(Info info) {
        super(BiggerplateMessages.AuthoriseJob_JobName);
        this.info = info;
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        String accessToken = BiggerplateOauth.getAccessToken(info, this);
        if (info.getBoolean(Info.CANCELED)) {
            return Status.CANCEL_STATUS;
        } else if (accessToken == null || accessToken.equals("")) { //$NON-NLS-1$
            return new Status(IStatus.WARNING, BiggerplatePlugin.PLUGIN_ID,
                    BiggerplateMessages.AuthoriseJob_Failed_text);
        } else {
            return Status.OK_STATUS;
        }
    }

}
