package org.xmind.ui.internal.biggerplate.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.xmind.core.IMeta;
import org.xmind.ui.internal.biggerplate.Info;
import org.xmind.ui.viewers.ImagePreviewViewer;

public class CutPreviewViewer extends ImagePreviewViewer {

    private Info info;

    public CutPreviewViewer() {
        super();
    }

    public void setInfo(Info info) {
        this.info = info;
        Image img = (Image) info.getProperty(Info.FULL_IMAGE);
        if (img != null) {
            if (info.hasProperty(IMeta.ORIGIN_X)
                    && info.hasProperty(IMeta.ORIGIN_Y)) {
                setImage(img, info.getInt(IMeta.ORIGIN_X, 0),
                        info.getInt(IMeta.ORIGIN_Y, 0));
            } else {
                setImage(img);
            }
        }
    }

    @Override
    protected void createRatioControls(Composite parent) {
        setPrefWidth(SWT.DEFAULT);
        super.createRatioControls(parent);
        if ("win32".equals(SWT.getPlatform())) { //$NON-NLS-1$
            setBackgroundColor(parent.getDisplay().getSystemColor(
                    SWT.COLOR_LIST_BACKGROUND));
        }
    }

    public void setX(double x) {
        super.setX(x);
        if (info != null)
            info.setInt(Info.X, (int) x);
    }

    public void setY(double y) {
        super.setY(y);
        if (info != null)
            info.setInt(Info.Y, (int) y);
    }

    public void setRatio(double ratio) {
        super.setRatio(ratio);
        if (info != null)
            info.setDouble(Info.SCALE, ratio);
    }

}
