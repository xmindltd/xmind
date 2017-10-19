package org.xmind.ui.internal.editor;

import java.io.File;

/**
 * 
 * @author Frank Shaka
 * @deprecated
 */
@Deprecated
public class FileEditorInput extends MindMapEditorInput {

    /**
     * @param file
     */
    public FileEditorInput(File file) {
        super(file.toURI());
    }

    public File getFile() {
        return new File(getURI());
    }

}
