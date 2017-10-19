package org.xmind.ui.editor;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.protocols.FilePathParser;

public class EditorHistoryItem implements IEditorHistoryItem {

    public static final String KEY_NAME = "name"; //$NON-NLS-1$
    public static final String KEY_OPENED_TIME = "openedTime"; //$NON-NLS-1$

    private static final String defaultName = MindMapMessages.EditorHistoryItem_defaultName;

    private String name;

    private long openedTime;

    public EditorHistoryItem(String name, long openedTime) {
        if (name == null || name.trim().equals("")) //$NON-NLS-1$
            this.name = defaultName;
        else
            this.name = name;
        this.openedTime = openedTime;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getOpenedTime() {
        return openedTime;
    }

    @Override
    public String toJson() {
        JSONObject json = new JSONObject();
        json.put(KEY_NAME, name);
        json.put(KEY_OPENED_TIME, openedTime);
        return json.toString();
    }

    public static IEditorHistoryItem readEditorHistoryItem(String uriString,
            String json) {
        if (null == json || json.trim().equals("")) {//$NON-NLS-1$
            if (null == uriString || uriString.trim().equals("")) //$NON-NLS-1$
                return new EditorHistoryItem(defaultName,
                        System.currentTimeMillis());
            try {
                URI uri = new URI(uriString);
                Map<URI, String> labels = new HashMap<URI, String>();
                FilePathParser.calculateFileURILabels(new URI[] { uri },
                        labels);
                return new EditorHistoryItem(labels.get(uri),
                        System.currentTimeMillis());
            } catch (URISyntaxException e) {
                MindMapUIPlugin.log(e,
                        "EditorHistoryItem parase uri to file name occur Some error."); //$NON-NLS-1$
            }
            return null;
        }
        try {
            /*
             * The version of JSONObject is too old , it does not support
             * JSONObject.toBean() and JSONObject.fromString().
             */
            JSONObject itemJson = new JSONObject(new JSONTokener(json));
            String jName = itemJson.getString(EditorHistoryItem.KEY_NAME);
            long jTime = itemJson.getLong(EditorHistoryItem.KEY_OPENED_TIME);

            IEditorHistoryItem item = new EditorHistoryItem(jName, jTime);
            return item;
        } catch (JSONException e) {
            MindMapUIPlugin.log(e,
                    "Read Json of EditorHistoryItem occur Some error."); //$NON-NLS-1$
            return new EditorHistoryItem(defaultName,
                    System.currentTimeMillis());
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof EditorHistoryItem))
            return false;

        EditorHistoryItem that = (EditorHistoryItem) obj;
        return this.name == that.name && this.openedTime == that.openedTime;
    }

    @Override
    public String toString() {
        return "EditorHistoryItem : (" + (name = name == null ? " " //$NON-NLS-1$ //$NON-NLS-2$
                : name) + "," + Calendar.getInstance().getTime() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
    }

}
