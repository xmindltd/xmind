package org.xmind.ui.internal.zen;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xmind.core.ISheet;
import org.xmind.core.IWorkbook;

public class WorkbookDeserializer {

    public void deserialize(IWorkbook workbook, JSONArray workbookArray)
            throws IOException {

        //deserialize sheets
        SheetDeserializer sheetDeserializer = new SheetDeserializer(workbook);
        for (int i = 0; i < workbookArray.length(); i++) {
            JSONObject sheetObject = workbookArray.getJSONObject(i);
            ISheet sheet = null;
            if (i == 0) {
                sheet = workbook.getPrimarySheet();
            } else {
                sheet = (ISheet) workbook.createSheet();
                workbook.addSheet(sheet);
            }
            sheetDeserializer.deserialize(sheet, sheetObject);
        }

        //deserialize extensions
//        ExtensionsDeserializer extensionsDeserializer = new ExtensionsDeserializer();
//        IWorkbookExtensionManager extensionManager = (IWorkbookExtensionManager) workbook
//                .getAdapter(IWorkbookExtensionManager.class);
//        extensionsDeserializer.deserialize(extensionManager, workbookArray);
    }

}
