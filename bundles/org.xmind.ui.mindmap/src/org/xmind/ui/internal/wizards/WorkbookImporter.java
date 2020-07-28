package org.xmind.ui.internal.wizards;

import java.lang.reflect.InvocationTargetException;

import org.xmind.core.Core;
import org.xmind.core.ISheet;
import org.xmind.core.IWorkbook;
import org.xmind.core.util.CloneHandler;
import org.xmind.ui.mindmap.LazyPasswordBasedEncryptor;
import org.xmind.ui.wizards.MindMapImporter;

/**
 * @deprecated
 * @author Frank Shaka
 */
public class WorkbookImporter extends MindMapImporter {

    public WorkbookImporter(String sourcePath, IWorkbook targetWorkbook) {
        super(sourcePath, targetWorkbook);
    }

    public void build() throws InvocationTargetException, InterruptedException {
        IWorkbook targetWorkbook = getTargetWorkbook();
        try {
            Core.getWorkbookBuilder()
                    .setEntryStreamNormalizer(new LazyPasswordBasedEncryptor());
            IWorkbook sourceWorkbook = Core.getWorkbookBuilder()
                    .loadFromPath(getSourcePath());
            CloneHandler cloneHandler = new CloneHandler()
                    .withWorkbooks(sourceWorkbook, targetWorkbook);
            for (ISheet sourceSheet : sourceWorkbook.getSheets()) {
                ISheet targetSheet = (ISheet) cloneHandler
                        .cloneObject(sourceSheet);
                if (targetSheet != null) {
                    targetWorkbook.addSheet(targetSheet);
                }
            }
        } catch (Exception e) {
            throw new InvocationTargetException(e);
        }
        postBuilded();
    }

}
