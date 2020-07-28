package org.xmind.ui.internal.svgsupport;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.SWTGraphics;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmind.ui.util.XMLUtils;

/**
 * @author Enki Xiong
 */
public class SVGImageData {
    private boolean isInit;
    private boolean documentContainError = false;
    private String filePath;
    private Dimension size = new Dimension(-1, -1);
    private static final Dimension INVALID_DIMENSION = new Dimension(-1, -1);
    private List<SVGShape> list;

    public SVGImageData(String filePath) {
        this.filePath = filePath;
        this.isInit = false;
        list = new ArrayList<SVGShape>();
    }

    private void init() {

        try {
            Element svg = getRootElement(filePath);

            // FIXME handle problem : width = 12cm 12 or 100% 
            int height = Integer
                    .parseInt(svg.getAttribute(SVGDefinitionConstants.HEIGHT)
                            .split(SVGDefinitionConstants.PX)[0]);
            int width = Integer
                    .parseInt(svg.getAttribute(SVGDefinitionConstants.WIDTH)
                            .split(SVGDefinitionConstants.PX)[0]);
            size = new Dimension(width, height);

            NodeList nodeList = svg.getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element ele = (Element) node;
                    if (SVGDefinitionConstants.TAG_DEFS
                            .equals(ele.getTagName()))
                        parseDefinition(ele);
                }
            }

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element ele = (Element) node;
                    SVGShape shape = SVGShape.parseShape(ele, null);
                    if (shape != null)
                        list.add(shape);
                }
            }
        } catch (Exception e) {
            documentContainError = true;
            e.printStackTrace();
            return;
        }
        isInit = true;
    }

    private Element getRootElement(String path) {
        InputStream stream = null;
        try {
            URL url = new URL(path);
            return getRootElement(url.openStream());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                    stream = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private Element getRootElement(InputStream stream) throws Exception {
        Document document = XMLUtils.getSvgDocumentBuilder().parse(stream);
        Element element = document.getDocumentElement();
        return element;
    }

    private void parseDefinition(Element defs) {

        NodeList list = defs.getChildNodes();

        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element ele = (Element) node;
                String tagName = ele.getTagName();

                if (null != tagName && !"".equals(tagName)) { //$NON-NLS-1$
                    String id = ele.getAttribute(SVGDefinitionConstants.ID);

                    if (tagName.equals(
                            SVGDefinitionConstants.TAG_LINEARGRADIENT)) {
                        SVGDefinition def = LinearGradient
                                .parseLinearGradient(ele);
                        SVGShape.idRefs.put(id, def);
                    } else {
                        SVGShape def = SVGShape.parseShape(ele, null);
                        if (def != null)
                            SVGShape.idRefs.put(id, def);
                    }

                }
            }
        }
    }

    private void paint(Graphics graphics, Display device) {

        if (!isInit)
            init();

        if (documentContainError) {
            return;
        }

        try {
            for (SVGShape shape : list) {
                shape.paintImage(graphics, device);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * create an image
     * 
     * @param imageSize
     *            size of the image you want to create
     * @param background
     *            the background color of the image, default value is null
     * @return
     */

    public ImageData createImage(Dimension imageSize, RGB background) {
        if (imageSize != null && imageSize.equals(INVALID_DIMENSION)) {
            if (size.equals(INVALID_DIMENSION))
                init();
            imageSize = size;
        }

        Image image = new Image(Display.getDefault(), imageSize.width,
                imageSize.height);
        Rectangle rect = new Rectangle(0, 0, imageSize.width, imageSize.height);
        paintImage(image, rect, background);

        ImageData imgData = image.getImageData();
        image.dispose();

        return imgData;
    }

    /**
     * paint svg image to specific area <B> paintArea</B> in <B>image</B>
     * 
     * @param image
     * @param paintArea
     * @param background
     *            add background for svg image,null if not need
     */
    public void paintImage(Image image, Rectangle paintArea, RGB background) {

        GC gc = new GC(image);
        SWTGraphics graphics = new SWTGraphics(gc);
        paintFigure(graphics, paintArea, null, background);

        graphics.dispose();
        gc.dispose();
    }

    /**
     * paint svg image to specific area <b>paintArea</b> with <b>graphics</b>
     * 
     * @param graphics
     * @param paintArea
     * @param manager
     *            if this svg image will be painted frequently,you should
     *            provide ResourceManager instance and manager this instance by
     *            yourself <br>
     *            <i>Note:</i>
     *            <ul>
     *            <li>if this image be removed from parent container, you must
     *            invoke manager.dispose() to release OS resource;</li>
     *            <li>if you provide null, this class will create a
     *            LocalResourceManager for painting and dispose it when painting
     *            ended</li>
     *            </ul>
     */
    public void paintFigure(Graphics graphics, Rectangle paintArea,
            ResourceManager manager) {
        paintFigure(graphics, paintArea, manager, null);
    }

    /**
     * paint svg image to specific area <b>(paintArea)</b> with <b>graphics</b>
     * 
     * @param background
     *            the background color of the image, default value is null
     * @see paintFigure(Graphics graphics, Rectangle paintArea, ResourceManager
     *      manager);
     */
    public void paintFigure(Graphics graphics, Rectangle paintArea,
            ResourceManager manager, RGB background) {
        graphics.setAdvanced(true);
        graphics.setAntialias(SWT.ON);

        graphics.translate(paintArea.x, paintArea.y);
        Dimension svgImageSize = getSize();
        graphics.scale(((float) paintArea.width) / svgImageSize.width,
                ((float) paintArea.height) / svgImageSize.height);

        boolean isNewManager = false;
        if (manager == null) {
            ResourceManager resources = JFaceResources.getResources();
            resources = resources == null
                    ? JFaceResources.getResources(Display.getDefault())
                    : resources;
            manager = new LocalResourceManager(resources);
            isNewManager = true;
        }

        if (background != null) {
            graphics.pushState();
            graphics.setBackgroundColor(manager.createColor(background));
            graphics.fillRectangle(0, 0, svgImageSize.width,
                    svgImageSize.height);
            graphics.popState();
        }

        setResourceManager(manager);
        paint(graphics, Display.getDefault());

        /// reset scale for export
        graphics.scale(1.0f);

        if (isNewManager)
            manager.dispose();

    }

    private void setResourceManager(ResourceManager manager) {
        for (SVGShape shape : list)
            shape.setResourceManager(manager);

    }

    public Dimension getSize() {
        if (!isInit)
            init();

        return size;
    }

}
