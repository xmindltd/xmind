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
package org.xmind.ui.internal.mindmap;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.xmind.core.Core;
import org.xmind.core.IFileEntry;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.core.event.ICoreEventSource;
import org.xmind.core.util.FileUtils;
import org.xmind.core.util.HyperlinkUtils;
import org.xmind.gef.command.Command;
import org.xmind.gef.command.CompoundCommand;
import org.xmind.gef.command.ICommandStack;
import org.xmind.gef.draw2d.geometry.Geometry;
import org.xmind.ui.commands.ModifyImageSizeCommand;
import org.xmind.ui.commands.ModifyImageSourceCommand;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.io.DownloadJob;
import org.xmind.ui.io.UIJobChangeListener;
import org.xmind.ui.mindmap.MindMapUI;

/**
 * @author frankshaka
 * @deprecated Use ImageDownloader instead
 */
public class ImageDownloadCenter {

    protected class ImageDownloadProcess extends UIJobChangeListener {

        private ITopic topic;

        private String url;

        private ICommandStack commandStack;

        private File tempFile;

        private DownloadJob job;

        /**
         * 
         */
        public ImageDownloadProcess(ITopic topic, String url,
                ICommandStack commandStack) {
            this.topic = topic;
            this.url = url;
            this.commandStack = commandStack;
            this.tempFile = createTempFile(FileUtils.getExtension(url));
            start();
        }

        /**
         * @return the topic
         */
        public ITopic getTopic() {
            return topic;
        }

        /**
         * @return the url
         */
        public String getUrl() {
            return url;
        }

        /**
         * 
         */
        private void start() {
            job = new DownloadJob(MindMapMessages.ImageDownload_jobName + url,
                    url, tempFile.getAbsolutePath(), MindMapUIPlugin.PLUGIN_ID);
            job.addJobChangeListener(this);
            job.schedule();
        }

        /**
         * @return
         */
        private File createTempFile(String ext) {
            return Core.getWorkspace().createTempFile("imageDownload", "", ext); //$NON-NLS-1$ //$NON-NLS-2$
        }

        public void kill() {
            if (job != null) {
                job.cancel();
                job = null;
            }
        }

        /**
         * @return the dead
         */
        public boolean isDead() {
            return job == null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.xmind.ui.io.UIJobChangeListener#doAboutToRun(org.eclipse.core
         * .runtime.jobs.IJobChangeEvent)
         */
        protected void doAboutToRun(IJobChangeEvent event) {
            super.doAboutToRun(event);
            notifyImageContent();
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.xmind.ui.io.UIJobChangeListener#doDone(org.eclipse.core.runtime
         * .jobs.IJobChangeEvent)
         */
        protected void doDone(IJobChangeEvent event) {
            job = null;
            processes.remove(topic);
            int code = event.getResult().getSeverity();
            if (code == IStatus.OK) {
                onSuccess();
            } else if (code == IStatus.CANCEL) {
                onCancel();
            } else {
                onFailed();
            }
            tempFile.delete();
        }

        private void onSuccess() {
            addImage();
        }

        private void onCancel() {
            notifyImageContent();
        }

        private void onFailed() {
            notifyImageContent();
        }

        /**
         * 
         */
        private void notifyImageContent() {
            if (topic.getImage() instanceof ICoreEventSource) {
                ICoreEventSource source = (ICoreEventSource) topic.getImage();
                source.getCoreEventSupport().dispatchValueChange(source,
                        Core.ImageSource, topic.getImage().getSource(), url);
            }
        }

        private Dimension getSize() {
            try {
                Image image = new Image(Display.getCurrent(),
                        tempFile.getAbsolutePath());
                Rectangle size = image.getBounds();
                image.dispose();
                return Geometry.getScaledConstrainedSize(size.width,
                        size.height, MindMapUI.IMAGE_INIT_WIDTH,
                        MindMapUI.IMAGE_INIT_HEIGHT);
            } catch (Throwable e) {
            }
            return null;
        }

        private void addImage() {
            final Dimension size = getSize();
            if (size == null) {
                if (topic.getImage() instanceof ICoreEventSource) {
                    ICoreEventSource source = (ICoreEventSource) topic
                            .getImage();
                    source.getCoreEventSupport().dispatchValueChange(source,
                            Core.ImageSource, topic.getImage().getSource(),
                            null);
                }
                return;
            }

            final IWorkbook workbook = topic.getOwnedWorkbook();
            SafeRunner.run(new SafeRunnable() {
                public void run() throws Exception {
                    IFileEntry entry = workbook.getManifest()
                            .createAttachmentFromFilePath(
                                    tempFile.getAbsolutePath());
                    if (entry != null) {
                        String source = HyperlinkUtils.toAttachmentURL(entry
                                .getPath());
                        List<Command> cmds = new ArrayList<Command>();
                        cmds.add(new ModifyImageSourceCommand(topic, source));
                        cmds.add(new ModifyImageSizeCommand(topic, size.width,
                                size.height));
                        Command cmd = new CompoundCommand(cmds);
                        cmd.setLabel(MindMapMessages.Command_InsertImage);
                        commandStack.execute(cmd);
                    }
                }
            });
        }

    }

    private static ImageDownloadCenter instance = new ImageDownloadCenter();

    private Map<ITopic, ImageDownloadProcess> processes = new HashMap<ITopic, ImageDownloadProcess>();

    private ImageDownloadCenter() {
    }

    public void startDownload(ITopic topic, String url,
            ICommandStack commandStack) {
        cancel(topic);
        processes
                .put(topic, new ImageDownloadProcess(topic, url, commandStack));
    }

    public void cancel(ITopic topic) {
        ImageDownloadProcess proc = processes.remove(topic);
        if (proc != null) {
            proc.kill();
        }
    }

    public boolean isDownloading(ITopic topic) {
        ImageDownloadProcess proc = processes.get(topic);
        return proc != null && !proc.isDead();
    }

    /**
     * @return the instance
     */
    public static ImageDownloadCenter getInstance() {
        return instance;
    }

}
