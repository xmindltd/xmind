package org.xmind.ui.mindmap;

public interface IWorkbookRefListener {

    void fileChanged(String title, String message, String[] buttons);

    void fileRemoved(String title, String message, String[] buttons,
            boolean forceQuit);

}
