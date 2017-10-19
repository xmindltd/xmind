package org.xmind.ui.internal.actions;

import java.util.ArrayList;
import java.util.List;

import org.xmind.core.Core;
import org.xmind.core.ISheet;
import org.xmind.core.IWorkbook;

public class CopiedSheetStorageSupport {
    private static CopiedSheetStorageSupport instance = null;

    private ISheet copiedSheet = null;

    private CopiedSheetStorageSupport() {
    }

    public static CopiedSheetStorageSupport getInstance() {
        if (instance == null) {
            instance = new CopiedSheetStorageSupport();
        }
        return instance;
    }

    public void setCopiedSheet(ISheet sheet) {
        copiedSheet = cloneSheet(sheet);
    }

    public ISheet getCopiedSheet(IWorkbook workbook) {
        return cloneSheet(workbook, copiedSheet);
    }

    public boolean isCopiedSheetExist() {
        return copiedSheet != null;
    }

    private ISheet cloneSheet(ISheet sheet) {
        IWorkbook workbook = Core.getWorkbookBuilder().createWorkbook();
        return cloneSheet(workbook, sheet);
    }

    public ISheet cloneSheet(IWorkbook workbook, ISheet sheet) {
        if (sheet == null) {
            return null;
        }
        List<ISheet> sheetList = new ArrayList<ISheet>();
        sheetList.add(sheet);
        ISheet clonedSheet = (ISheet) workbook.clone(sheetList).getCloneds()
                .iterator().next();

        return clonedSheet;
    }

}
