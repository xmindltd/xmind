package org.xmind.ui.internal.outline.resource;

import org.xmind.core.ISheet;
import org.xmind.ui.internal.outline.OutlineIndexModelPart;
import org.xmind.ui.mindmap.IWorkbookRef;

public class OutlineResources {

    private IOutlineResource markerResourceForWorkbook;
    private IOutlineResource markerResourceForSheet;

    private IOutlineResource labelResourceForWorkbook;
    private IOutlineResource labelResourceForSheet;

    private IOutlineResource assigneeResourceForWorkbook;
    private IOutlineResource assigneeResourceForSheet;

    private ITaskDateResource taskDateResourceForWorkbook;
    private ITaskDateResource taskDateResourceForSheet;

    private IAZResource azResourceForWorkbook;
    private IAZResource azResourceForSheet;

    public Object getResourceForSheet(ISheet sheet, int indexType,
            boolean forceUpdate) {
        Object resource = null;
        if (sheet != null)
            switch (indexType) {
            case OutlineIndexModelPart.OUTLINE_TYPE_NONE:
                resource = sheet;
                break;
            case OutlineIndexModelPart.OUTLINE_TYPE_BY_MARKERS:
                resource = getMarkerResourceForSheet(sheet, forceUpdate);
                break;
            case OutlineIndexModelPart.OUTLINE_TYPE_BY_LABELS:
                resource = getLabelResourceForSheet(sheet, forceUpdate);
                break;
            case OutlineIndexModelPart.OUTLINE_TYPE_BY_ASSIGNEE:
                resource = getAssigneeResourceForSheet(sheet, forceUpdate);
                break;
            case OutlineIndexModelPart.OUTLINE_TYPE_BY_STARTDATE:
                resource = getTaskDateResourceForSheet(sheet, indexType,
                        forceUpdate);
                break;
            case OutlineIndexModelPart.OUTLINE_TYPE_BY_ENDDATE:
                resource = getTaskDateResourceForSheet(sheet, indexType,
                        forceUpdate);
                break;
            case OutlineIndexModelPart.OUTLINE_TYPE_BY_AZ:
                resource = getAZResourceForSheet(sheet, true, forceUpdate);
                break;
            case OutlineIndexModelPart.OUTLINE_TYPE_BY_ZA:
                resource = getAZResourceForSheet(sheet, false, forceUpdate);
                break;
            }
        return resource;
    }

    public Object getResourceForWorkbook(IWorkbookRef workbookRef,
            int indexType, boolean forceUpdate) {
        Object resource = null;
        if (workbookRef != null)
            switch (indexType) {
            case OutlineIndexModelPart.OUTLINE_TYPE_NONE:
                resource = workbookRef;
                break;
            case OutlineIndexModelPart.OUTLINE_TYPE_BY_MARKERS:
                resource = getMarkerResourceForWorkbook(workbookRef,
                        forceUpdate);
                break;
            case OutlineIndexModelPart.OUTLINE_TYPE_BY_LABELS:
                resource = getLabelResourceForWorkbook(workbookRef,
                        forceUpdate);
                break;
            case OutlineIndexModelPart.OUTLINE_TYPE_BY_ASSIGNEE:
                resource = getAssigneeResourceForWorkbook(workbookRef,
                        forceUpdate);
                break;
            case OutlineIndexModelPart.OUTLINE_TYPE_BY_STARTDATE:
                resource = getTaskDateResourceForWorkbook(workbookRef,
                        indexType, forceUpdate);
                break;
            case OutlineIndexModelPart.OUTLINE_TYPE_BY_ENDDATE:
                resource = getTaskDateResourceForWorkbook(workbookRef,
                        indexType, forceUpdate);
                break;
            case OutlineIndexModelPart.OUTLINE_TYPE_BY_AZ:
                resource = getAZResourceForWorkbook(workbookRef, true,
                        forceUpdate);
                break;
            case OutlineIndexModelPart.OUTLINE_TYPE_BY_ZA:
                resource = getAZResourceForWorkbook(workbookRef, false,
                        forceUpdate);
                break;
            }
        return resource;
    }

    private IOutlineResource getMarkerResourceForWorkbook(
            IWorkbookRef workbookRef, boolean forceUpdate) {
        if (markerResourceForWorkbook != null && forceUpdate) {
            markerResourceForWorkbook.reset(workbookRef, true);
        }
        if (markerResourceForWorkbook == null)
            markerResourceForWorkbook = new MarkerResourceForWorkbook(
                    workbookRef);
        return markerResourceForWorkbook;
    }

    private IOutlineResource getMarkerResourceForSheet(ISheet sheet,
            boolean forceUpdate) {
        if (markerResourceForSheet != null && forceUpdate) {
            markerResourceForSheet.reset(sheet, true);
        }
        if (markerResourceForSheet == null)
            markerResourceForSheet = new MarkerResourceForSheet(sheet);
        return markerResourceForSheet;
    }

    private IOutlineResource getLabelResourceForSheet(ISheet sheet,
            boolean forceUpdate) {
        if (labelResourceForSheet != null && forceUpdate) {
            labelResourceForSheet.reset(sheet, true);
        }
        if (labelResourceForSheet == null)
            labelResourceForSheet = new LabelResourceForSheet(sheet);
        return labelResourceForSheet;
    }

    private IOutlineResource getLabelResourceForWorkbook(
            IWorkbookRef workbookRef, boolean forceUpdate) {
        if (labelResourceForWorkbook != null && forceUpdate) {
            labelResourceForWorkbook.reset(workbookRef, true);
        }
        if (labelResourceForWorkbook == null)
            labelResourceForWorkbook = new LabelResourceForWorkbook(
                    workbookRef);
        return labelResourceForWorkbook;
    }

    private IOutlineResource getAssigneeResourceForSheet(ISheet sheet,
            boolean forceUpdate) {
        if (assigneeResourceForSheet != null && forceUpdate) {
            assigneeResourceForSheet.reset(sheet, true);
        }
        if (assigneeResourceForSheet == null)
            assigneeResourceForSheet = new AssigneeResourceForSheet(sheet);
        return assigneeResourceForSheet;
    }

    private IOutlineResource getAssigneeResourceForWorkbook(
            IWorkbookRef workbookRef, boolean forceUpdate) {
        if (assigneeResourceForWorkbook != null && forceUpdate) {
            assigneeResourceForWorkbook.reset(workbookRef, true);
        }
        if (assigneeResourceForWorkbook == null)
            assigneeResourceForWorkbook = new AssigneeResourceForWorkbook(
                    workbookRef);
        return assigneeResourceForWorkbook;
    }

    private IOutlineResource getTaskDateResourceForSheet(ISheet sheet,
            int taskDateType, boolean forceUpdate) {
        if (taskDateResourceForSheet != null && forceUpdate) {
            taskDateResourceForSheet.reset(sheet, forceUpdate);
        }
        if (taskDateResourceForSheet == null)
            taskDateResourceForSheet = new TaskDateResourceForSheet(sheet,
                    taskDateType);
        taskDateResourceForSheet.setTaskDateResourceType(taskDateType);
        return taskDateResourceForSheet;
    }

    private IOutlineResource getTaskDateResourceForWorkbook(
            IWorkbookRef workbookRef, int taskDateType, boolean forceUpdate) {
        if (taskDateResourceForWorkbook != null && forceUpdate) {
            taskDateResourceForWorkbook.reset(workbookRef, forceUpdate);
        }
        if (taskDateResourceForWorkbook == null)
            taskDateResourceForWorkbook = new TaskDateResourceForWorkbook(
                    workbookRef, taskDateType);
        taskDateResourceForWorkbook.setTaskDateResourceType(taskDateType);
        return taskDateResourceForWorkbook;
    }

    private IOutlineResource getAZResourceForSheet(ISheet sheet,
            boolean isPositiveSequence, boolean forceUpdate) {
        if (azResourceForSheet != null && forceUpdate) {
            azResourceForSheet.reset(sheet, true);
        }
        if (azResourceForSheet == null) {
            azResourceForSheet = new AZResourceForSheet(sheet);
        }
        azResourceForSheet.setSequence(isPositiveSequence);
        return azResourceForSheet;
    }

    private IOutlineResource getAZResourceForWorkbook(IWorkbookRef workbookRef,
            boolean isPositiveSequence, boolean forceUpdate) {
        if (azResourceForWorkbook != null && forceUpdate) {
            azResourceForWorkbook.reset(workbookRef, true);
        }
        if (azResourceForWorkbook == null) {
            azResourceForWorkbook = new AZResourceForWorkbook(workbookRef);
        }
        azResourceForWorkbook.setSequence(isPositiveSequence);
        return azResourceForWorkbook;
    }

}
