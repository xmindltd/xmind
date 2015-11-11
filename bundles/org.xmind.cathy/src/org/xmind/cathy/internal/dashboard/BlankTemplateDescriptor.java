package org.xmind.cathy.internal.dashboard;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.xmind.core.IWorkbook;
import org.xmind.ui.internal.WorkbookFactory;
import org.xmind.ui.internal.wizards.AbstractTemplateDescriptor;

public class BlankTemplateDescriptor extends AbstractTemplateDescriptor {

    /**
     * <b>NOT PUBLIC API.</b>
     */
    public static String DEFAULT_STRUCTURE_CLASS = "org.xmind.ui.map.unbalanced"; //$NON-NLS-1$

    private String id;

    private String structureClass;

    private String name;

    public BlankTemplateDescriptor(String id, String structureClass,
            String name) {
        this.id = id;
        this.structureClass = structureClass;
        this.name = name;
    }

    public String getSymbolicName() {
        return "blank:" + id; //$NON-NLS-1$
    }

    public String getStructureClass() {
        return structureClass;
    }

    public String getName() {
        return name;
    }

    public InputStream newStream() {
        IWorkbook workbook = WorkbookFactory.createEmptyWorkbook();
        if (structureClass != null) {
            workbook.getPrimarySheet().getRootTopic()
                    .setStructureClass(structureClass);
        }
        byte[] data = WorkbookFactory.getWorkbookContent(workbook);
        return new ByteArrayInputStream(data);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof BlankTemplateDescriptor))
            return false;
        BlankTemplateDescriptor that = (BlankTemplateDescriptor) obj;
        return this.id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return BlankTemplateDescriptor.class.hashCode() ^ id.hashCode();
    }

    @Override
    public String toString() {
        return "TemplateFromStructure(id=" + id + ",structureClass=" //$NON-NLS-1$//$NON-NLS-2$
                + structureClass + ")"; //$NON-NLS-1$
    }

}
