// Copyright 2003, FreeHEP
package org.xmind.org.freehep.graphicsio.raw;

import java.awt.Color;
import java.util.Locale;
import java.util.Properties;

import javax.imageio.ImageWriteParam;

import org.xmind.org.freehep.graphicsio.ImageParamConverter;
import org.xmind.org.freehep.util.UserProperties;

/**
 * @author Jason Wong
 */
public class RawImageWriteParam extends ImageWriteParam implements
        ImageParamConverter {

    private final static String rootKey = RawImageWriteParam.class.getName();

    public final static String BACKGROUND = rootKey + ".Background"; //$NON-NLS-1$

    public final static String CODE = rootKey + ".Code"; //$NON-NLS-1$

    public final static String PAD = rootKey + ".Pad"; //$NON-NLS-1$

    private Color bkg;

    private String code;

    private int pad;

    public RawImageWriteParam(Locale locale) {
        super(locale);
        bkg = null;
        code = "ARGB"; //$NON-NLS-1$
        pad = 1;
    }

    public ImageWriteParam getWriteParam(Properties properties) {
        UserProperties p = new UserProperties(properties);
        setBackground(p.getPropertyColor(BACKGROUND, bkg));
        setCode(p.getProperty(CODE, code));
        setPad(p.getPropertyInt(PAD, pad));
        return this;
    }

    public Color getBackground() {
        return bkg;
    }

    public void setBackground(Color bkg) {
        this.bkg = bkg;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getPad() {
        return pad;
    }

    public void setPad(int pad) {
        this.pad = pad;
    }
}
