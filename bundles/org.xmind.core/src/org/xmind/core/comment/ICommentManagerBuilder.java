package org.xmind.core.comment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.xmind.core.CoreException;

public interface ICommentManagerBuilder {

    ICommentManager createCommentManager();

    ICommentManager loadFromStream(InputStream stream) throws IOException,
            CoreException;

    ICommentManager loadFromFile(File file) throws IOException, CoreException;

    ICommentManager loadFromPath(String absolutePath) throws IOException,
            CoreException;

    ICommentManager loadFromUrl(URL url) throws IOException, CoreException;

}
