package org.xmind.ui.internal.biggerplate.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.xmind.ui.biggerplate.BiggerplateMessages;
import org.xmind.ui.biggerplate.BiggerplatePlugin;
import org.xmind.ui.internal.biggerplate.BiggerplateAPI;
import org.xmind.ui.internal.biggerplate.Info;

public class BiggerplateUploadJob extends CancelableJob {

    private Info info;

    public BiggerplateUploadJob(Info info) {
        super(NLS.bind(BiggerplateMessages.BiggerplateUploadJob_JobName, info.getString(Info.TITLE)));
        this.info = info;
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        monitor.beginTask(null, 100);
        monitor.worked(50);

        BiggerplateAPI.submitMap(info, this);

        if (info.getBoolean(Info.CANCELED)) {
            return Status.CANCEL_STATUS;
        } else if (info.getString(Info.RESULT_URL) == null
                || info.getString(Info.RESULT_URL).equals("")) { //$NON-NLS-1$
            return new Status(IStatus.WARNING, BiggerplatePlugin.PLUGIN_ID,
                    BiggerplateMessages.BiggerplateUploadJob_Failed_text);
        }

        monitor.worked(50);
        return Status.OK_STATUS;
    }

}
