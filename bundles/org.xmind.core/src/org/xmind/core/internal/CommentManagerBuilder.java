package org.xmind.core.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.xmind.core.CoreException;
import org.xmind.core.comment.ICommentManager;
import org.xmind.core.comment.ICommentManagerBuilder;

public abstract class CommentManagerBuilder implements ICommentManagerBuilder {

    public ICommentManager loadFromFile(File file) throws IOException,
            CoreException {
        if (!file.exists() || file.isDirectory())
            throw new FileNotFoundException();
        FileInputStream stream = new FileInputStream(file);
        try {
            return loadFromStream(stream);
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
            }
        }
    }

    public ICommentManager loadFromPath(String absolutePath)
            throws IOException, CoreException {
        return loadFromFile(new File(absolutePath));
    }

    public ICommentManager loadFromUrl(URL url) throws IOException,
            CoreException {
        InputStream stream = url.openStream();
        try {
            return loadFromStream(stream);
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
            }
        }
    }

}
