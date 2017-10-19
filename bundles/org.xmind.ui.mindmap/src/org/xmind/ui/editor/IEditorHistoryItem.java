package org.xmind.ui.editor;

public interface IEditorHistoryItem {

    String getName();

    long getOpenedTime();

    String toJson();

}
