package org.xmind.ui.internal.editor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.xmind.core.Core;
import org.xmind.core.util.FileUtils;

public class ResourceFileOutputStream extends FilterOutputStream {

    private IFile file;

    private String tempFile;

    private IProgressMonitor monitor;

    public ResourceFileOutputStream(IFile file, IProgressMonitor monitor)
            throws IOException {
        super(null);
        this.tempFile = createTempFile();
        this.file = file;
        this.out = new FileOutputStream(tempFile);
        this.monitor = monitor;
    }

    @Override
    public void write(byte[] b) throws IOException {
        out.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
    }

    @Override
    public void write(int b) throws IOException {
        out.write(b);
    }

    @Override
    public void close() throws IOException {
        try {
            super.close();
            try {
                transferContents();
            } catch (CoreException e) {
                IOException ex = new IOException(e.getMessage());
                ex.initCause(e);
                throw ex;
            }
        } finally {
            deleteTempFile();
        }

    }

    private void transferContents() throws IOException, CoreException {
        file.setContents(new FileInputStream(tempFile), IResource.KEEP_HISTORY,
                monitor);
    }

    private void deleteTempFile() {
        FileUtils.delete(new File(tempFile));
    }

    private static String createTempFile() throws IOException {
        String path = Core.getWorkspace().getTempFile(
                Core.getIdFactory().createId());
        return path;
    }

}
