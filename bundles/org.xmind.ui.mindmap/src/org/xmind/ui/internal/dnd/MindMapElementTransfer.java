/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See https://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.ui.internal.dnd;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.xmind.core.Core;
import org.xmind.core.ICloneData;
import org.xmind.core.ISerializer;
import org.xmind.core.IWorkbook;
import org.xmind.core.internal.dom.MarkerSheetImpl;
import org.xmind.core.io.DirectoryStorage;
import org.xmind.core.marker.IMarkerRef;
import org.xmind.core.util.FileUtils;
import org.xmind.ui.mindmap.MindMapUI;

public class MindMapElementTransfer extends ByteArrayTransfer {

    private static final MindMapElementTransfer instance = new MindMapElementTransfer();

    private static final String TYPE_NAME = "mindmap-element-transfer-format:" //$NON-NLS-1$
            + System.currentTimeMillis() + ":" + instance.hashCode(); //$NON-NLS-1$

    private static final int TYPE_ID = registerType(TYPE_NAME);

    private Object[] elements = null;

    private IWorkbook tempWorkbook = null;

    private Map<Object, Object> cloneMap = new HashMap<Object, Object>();

    protected MindMapElementTransfer() {
    }

    public static MindMapElementTransfer getInstance() {
        return instance;
    }

    protected int[] getTypeIds() {
        return new int[] { TYPE_ID };
    }

    protected String[] getTypeNames() {
        return new String[] { TYPE_NAME };
    }

    public void javaToNative(Object object, TransferData transferData) {
        if (!(object instanceof Object[])) {
            setElements(null);
            return;
        }

        setElements((Object[]) object);
        if (getElements() == null)
            return;

        byte[] check = TYPE_NAME.getBytes();
        super.javaToNative(check, transferData);
    }

    public Object nativeToJava(TransferData transferData) {
        Object object = super.nativeToJava(transferData);
        return object == null ? null : elements;
    }

    boolean checkType(Object object) {
        if (!(object instanceof Object[]))
            return false;
        Object[] eles = (Object[]) object;
        return eles.length > 0;
    }

    @Override
    protected boolean validate(Object object) {
        return checkType(object);
    }

    public void setElements(Object[] elements) {
        this.elements = recreateElements(elements);
    }

    public Object[] getElements() {
        return elements;
    }

    private Object[] recreateElements(Object[] elements) {
        tempWorkbook = recreateTempWorkbook();
        if (elements == null || elements.length == 0)
            return null;

        ICloneData result = tempWorkbook.clone(Arrays.asList(elements));
        Collection<Object> cloneds = result.getCloneds();
        if (!cloneds.isEmpty()) {
            for (Object element : elements) {
                if (element instanceof IMarkerRef) {
                    IMarkerRef ref = (IMarkerRef) element;
                    ((MarkerSheetImpl) tempWorkbook.getMarkerSheet())
                            .getElementRegistry().register(ref.getMarker());
                }
            }
            fillCloneMap(result, elements);
            return cloneds.toArray();
        }
        return null;
    }

    private void fillCloneMap(ICloneData result, Object[] elements) {
        if (!cloneMap.isEmpty())
            cloneMap.clear();
        for (Object obj : elements) {
            Object cloned = result.get(obj);
            cloneMap.put(cloned, obj);
        }
    }

    public Map<Object, Object> getTransferMap() {
        return cloneMap;
    }

    private IWorkbook recreateTempWorkbook() {
        String tempDir = Core.getWorkspace().getTempDir("element_transfer" //$NON-NLS-1$
                + MindMapUI.FILE_EXT_XMIND_TEMP);
        File tempLocation = new File(tempDir);
        FileUtils.delete(tempLocation);
        tempLocation.mkdirs();
        IWorkbook workbook = Core.getWorkbookBuilder()
                .createWorkbook(new DirectoryStorage(tempLocation));
        workbook.getMarkerSheet().setParentSheet(
                MindMapUI.getResourceManager().getSystemMarkerSheet());
        try {
            ISerializer serializer = Core.getWorkbookBuilder().newSerializer();
            serializer.setWorkbook(workbook);
            serializer.setWorkbookStorageAsOutputTarget();
            serializer.serialize(null);
        } catch (Exception e) {
        }
        return workbook;
    }

}