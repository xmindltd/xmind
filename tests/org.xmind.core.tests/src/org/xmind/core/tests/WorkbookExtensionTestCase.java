package org.xmind.core.tests;

import static org.junit.Assert.*;

import org.junit.*;
import org.xmind.core.*;
import org.xmind.core.internal.dom.*;
import org.xmind.core.internal.tests.*;
import org.xmind.core.io.*;

public class WorkbookExtensionTestCase extends WorkbookTestCaseBase {

    @Test
    public void testExtensions() {
        IWorkbook workbook = createWorkbook(new ByteArrayStorage());
        IWorkbookExtensionManager extManager = workbook.getAdapter(IWorkbookExtensionManager.class);
        assertNotNull(extManager);
        assertEquals(0, extManager.getExtensions().size());

        IWorkbookExtension ext = extManager.createExtension("org.xmind.test.foobar");
        assertNotNull(ext);
        assertEquals("org.xmind.test.foobar", ext.getProviderName());
        assertEquals(1, extManager.getExtensions().size());

        extManager.createExtension("org.xmind.test.foobar");
        assertEquals(1, extManager.getExtensions().size());

        IWorkbookExtension ext2 = extManager.getExtension("org.xmind.test.foobar");
        assertSame(ext, ext2);
    }

    @Test
    public void testResourceRef() {
        IWorkbook workbook = createWorkbook(new ByteArrayStorage());
        IWorkbookExtensionManager extManager = workbook.getAdapter(IWorkbookExtensionManager.class);
        IWorkbookExtension ext = extManager.createExtension("org.xmind.test.foobar");

        assertEquals(0, ext.getResourceRefs().size());

        IResourceRef ref = workbook.createResourceRef(IResourceRef.FILE_ENTRY, "attachemnt/demo"); //$NON-NLS-1$
        ext.addResourceRef(ref);
        assertEquals(1, ext.getResourceRefs().size());
        assertEquals(IResourceRef.FILE_ENTRY, ref.getType());
        assertEquals("attachemnt/demo", ref.getResourceId()); //$NON-NLS-1$

        ext.removeResourceRef(ref);
        assertEquals(0, ext.getResourceRefs().size());
    }

    @Test
    public void testName() {
        IWorkbook workbook = createWorkbook(new ByteArrayStorage());
        IWorkbookExtensionManager extManager = workbook.getAdapter(IWorkbookExtensionManager.class);
        IWorkbookExtension extension = extManager.createExtension("org.xmind.test.foobar");

        IWorkbookExtensionElement content = extension.getContent();
        assertEquals(DOMConstants.TAG_CONTENT, content.getName());

        IWorkbookExtensionElement ele = content.createChild("Name"); //$NON-NLS-1$
        assertEquals("Name", ele.getName()); //$NON-NLS-1$
    }

    @Test
    public void testParent() {
        IWorkbook workbook = createWorkbook(new ByteArrayStorage());
        IWorkbookExtensionManager extManager = workbook.getAdapter(IWorkbookExtensionManager.class);
        IWorkbookExtension extension = extManager.createExtension("org.xmind.test.foobar");

        IWorkbookExtensionElement content = extension.getContent();
        assertNull(content.getParent());

        IWorkbookExtensionElement ele = content.createChild("Name"); //$NON-NLS-1$
        assertSame(content, ele.getParent());
    }

    @Test
    public void testTextContent() {
        IWorkbook workbook = createWorkbook(new ByteArrayStorage());
        IWorkbookExtensionManager extManager = workbook.getAdapter(IWorkbookExtensionManager.class);
        IWorkbookExtension extension = extManager.createExtension("org.xmind.test.foobar");

        IWorkbookExtensionElement content = extension.getContent();
        assertNull(content.getTextContent());

        content.setTextContent("text"); //$NON-NLS-1$
        assertEquals("text", content.getTextContent()); //$NON-NLS-1$

        content.setTextContent(null);
        assertNull(content.getTextContent());
    }

    @Test
    public void testGetChildren() {
        IWorkbook workbook = createWorkbook(new ByteArrayStorage());
        IWorkbookExtensionManager extManager = workbook.getAdapter(IWorkbookExtensionManager.class);
        IWorkbookExtension extension = extManager.createExtension("org.xmind.test.foobar");

        IWorkbookExtensionElement content = extension.getContent();
        assertEquals(0, content.getChildren().size());
        assertEquals(0, content.getChildren("Name1").size()); //$NON-NLS-1$

        content.createChild("Name1"); //$NON-NLS-1$
        content.createChild("Name2"); //$NON-NLS-1$

        assertEquals(2, content.getChildren().size());
        assertEquals(1, content.getChildren("Name1").size()); //$NON-NLS-1$
    }

    @Test
    public void testCreateChild() {
        IWorkbook workbook = createWorkbook(new ByteArrayStorage());
        IWorkbookExtensionManager extManager = workbook.getAdapter(IWorkbookExtensionManager.class);
        IWorkbookExtension extension = extManager.createExtension("org.xmind.test.foobar");

        IWorkbookExtensionElement content = extension.getContent();
        IWorkbookExtensionElement ele = content.createChild("Name"); //$NON-NLS-1$
        assertNotNull(ele);
        assertEquals("Name", ele.getName()); //$NON-NLS-1$
    }

    @Test
    public void testGetCreatedChild() {
        IWorkbook workbook = createWorkbook(new ByteArrayStorage());
        IWorkbookExtensionManager extManager = workbook.getAdapter(IWorkbookExtensionManager.class);
        IWorkbookExtension extension = extManager.createExtension("org.xmind.test.foobar");

        IWorkbookExtensionElement content = extension.getContent();
        assertNotNull(content.getCreatedChild("Name1")); //$NON-NLS-1$

        IWorkbookExtensionElement ele = content.createChild("Name2"); //$NON-NLS-1$
        assertSame(ele, content.getCreatedChild("Name2")); //$NON-NLS-1$
    }

    @Test
    public void testGetFirstChild() {
        IWorkbook workbook = createWorkbook(new ByteArrayStorage());
        IWorkbookExtensionManager extManager = workbook.getAdapter(IWorkbookExtensionManager.class);
        IWorkbookExtension extension = extManager.createExtension("org.xmind.test.foobar");

        IWorkbookExtensionElement content = extension.getContent();
        assertNull(content.getFirstChild("Name")); //$NON-NLS-1$

        IWorkbookExtensionElement ele1 = content.createChild("Name"); //$NON-NLS-1$
        assertSame(ele1, content.getFirstChild("Name")); //$NON-NLS-1$

        IWorkbookExtensionElement ele2 = content.createChild("Name"); //$NON-NLS-1$
        assertSame(ele1, content.getFirstChild("Name")); //$NON-NLS-1$

        content.addChild(ele2, 0);
        assertSame(ele2, content.getFirstChild("Name")); //$NON-NLS-1$
    }

    @Test
    public void testDelete() {
        IWorkbook workbook = createWorkbook(new ByteArrayStorage());
        IWorkbookExtensionManager extManager = workbook.getAdapter(IWorkbookExtensionManager.class);
        IWorkbookExtension extension = extManager.createExtension("org.xmind.test.foobar");

        IWorkbookExtensionElement content = extension.getContent();

        IWorkbookExtensionElement ele1 = content.createChild("Name"); //$NON-NLS-1$
        assertEquals(1, content.getChildren().size());
        content.deleteChild(ele1);
        assertEquals(0, content.getChildren().size());

        content.createChild("Name"); //$NON-NLS-1$
        assertEquals(1, content.getChildren("Name").size()); //$NON-NLS-1$
        content.deleteChildren("Name"); //$NON-NLS-1$
        assertEquals(0, content.getChildren("Name").size()); //$NON-NLS-1$

        content.createChild("Name1"); //$NON-NLS-1$
        content.createChild("Name2"); //$NON-NLS-1$
        assertEquals(2, content.getChildren().size());
        content.deleteChildren();
        assertEquals(0, content.getChildren().size());
    }

    @Test
    public void testAttributes() {
        IWorkbook workbook = createWorkbook(new ByteArrayStorage());
        IWorkbookExtensionManager extManager = workbook.getAdapter(IWorkbookExtensionManager.class);
        IWorkbookExtension extension = extManager.createExtension("org.xmind.test.foobar");

        IWorkbookExtensionElement content = extension.getContent();
        assertEquals(0, content.getAttributeKeys().size());

        content.setAttribute("Name1", "Value1"); //$NON-NLS-1$//$NON-NLS-2$
        content.setAttribute("Name2", "Value2"); //$NON-NLS-1$//$NON-NLS-2$
        assertEquals(2, content.getAttributeKeys().size());
        assertEquals("Value1", content.getAttribute("Name1")); //$NON-NLS-1$//$NON-NLS-2$

        content.setAttribute("Name1", null); //$NON-NLS-1$
        assertEquals(1, content.getAttributeKeys().size());
    }

}
