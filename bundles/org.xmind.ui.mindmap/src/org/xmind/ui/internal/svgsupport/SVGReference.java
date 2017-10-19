package org.xmind.ui.internal.svgsupport;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SVGReference {

    private static Map<String, SVGImageData> caches = new ConcurrentHashMap<String, SVGImageData>();

    private String svgFilePath;

    public SVGReference(String svgFilePath) {
        this.svgFilePath = svgFilePath;
        if (!caches.containsKey(svgFilePath))
            caches.put(svgFilePath, new SVGImageData(svgFilePath));
    }

    public SVGImageData getSVGData() {
        return caches.get(svgFilePath);
    }

}
