package org.xmind.ui.internal.outline;

import org.xmind.ui.internal.MindMapMessages;

public enum OutlineType {

    None(MindMapMessages.OutlineType_None,
            OutlineIndexModelPart.OUTLINE_TYPE_NONE),  //
    ByAZ("A ~ Z", OutlineIndexModelPart.OUTLINE_TYPE_BY_AZ),  // //$NON-NLS-1$
    ByZA("Z ~ A", OutlineIndexModelPart.OUTLINE_TYPE_BY_ZA),  // //$NON-NLS-1$
    ByMarkers(MindMapMessages.OutlineType_Markers,
            OutlineIndexModelPart.OUTLINE_TYPE_BY_MARKERS),  //
    ByLabels(MindMapMessages.OutlineType_Labels,
            OutlineIndexModelPart.OUTLINE_TYPE_BY_LABELS),  //
    ByStartDate(MindMapMessages.OutlineType_StartDate,
            OutlineIndexModelPart.OUTLINE_TYPE_BY_STARTDATE),  //
    ByEndDate(MindMapMessages.OutlineType_EndDate,
            OutlineIndexModelPart.OUTLINE_TYPE_BY_ENDDATE),  //
    ByAssignee(MindMapMessages.OutlineType_Assignee,
            OutlineIndexModelPart.OUTLINE_TYPE_BY_ASSIGNEE);

    private String name;

    private int type;

    private OutlineType(String name, int type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public int getType() {
        return type;
    }

    public static String[] getNames() {
        OutlineType[] instances = values();
        String[] ids = new String[instances.length];
        for (int i = 0; i < instances.length; i++)
            ids[i] = instances[i].getName();
        return ids;
    }

    public static OutlineType findByName(String name) {
        if (name == null)
            return null;

        for (OutlineType outlineType : values()) {
            if (outlineType.getName().equals(name))
                return outlineType;
        }

        return null;
    }

}
