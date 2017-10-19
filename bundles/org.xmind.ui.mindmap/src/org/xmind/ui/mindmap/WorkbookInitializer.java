package org.xmind.ui.mindmap;

import org.eclipse.osgi.util.NLS;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.core.style.IStyle;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.style.StyleUtils;

/**
 * 
 * @author Frank Shaka
 * @since 3.6.50
 */
public final class WorkbookInitializer {

    private static String DEFAULT_STRUCTURE_CLASS = "org.xmind.ui.map.unbalanced"; //$NON-NLS-1$

    private static final WorkbookInitializer defaultInstance = new WorkbookInitializer();

    private String structureClass = null;

    private IStyle theme = null;

    private WorkbookInitializer() {
    }

    public void initWorkbook(IWorkbook workbook) {
        ISheet sheet = workbook.getPrimarySheet();
        sheet.setTitleText(NLS.bind(MindMapMessages.TitleText_Sheet,
                workbook.getSheets().size()));
        ITopic rootTopic = sheet.getRootTopic();
        rootTopic.setTitleText(MindMapMessages.TitleText_CentralTopic);

        String structureClass = getStructureClass();
        if (structureClass == null) {
            structureClass = DEFAULT_STRUCTURE_CLASS;
        }
        rootTopic.setStructureClass(structureClass);

        IStyle theme = getTheme();
        if (theme == null) {
            theme = MindMapUI.getResourceManager().getDefaultTheme();
        }
        StyleUtils.setTheme(sheet, theme);
    }

    public WorkbookInitializer copy() {
        WorkbookInitializer that = new WorkbookInitializer();
        that.structureClass = this.structureClass;
        that.theme = this.theme;
        return that;
    }

    public WorkbookInitializer withStructureClass(String structureClass) {
        WorkbookInitializer that = copy();
        that.structureClass = structureClass;
        return that;
    }

    public WorkbookInitializer withTheme(IStyle theme) {
        WorkbookInitializer that = copy();
        that.theme = theme;
        return that;
    }

    public String getStructureClass() {
        return this.structureClass;
    }

    public IStyle getTheme() {
        return this.theme;
    }

    @Override
    public int hashCode() {
        return 37 ^ (structureClass == null ? 37 : structureClass.hashCode())
                ^ (theme == null ? 37 : theme.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof WorkbookInitializer))
            return false;
        WorkbookInitializer that = (WorkbookInitializer) obj;
        return (this.structureClass == that.structureClass
                || (this.structureClass != null
                        && this.structureClass.equals(that.structureClass)))
                && (this.theme == that.theme || (this.theme != null
                        && this.theme.equals(that.theme)));
    }

    public static WorkbookInitializer getDefault() {
        return defaultInstance;
    }

}
