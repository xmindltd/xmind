package org.xmind.cathy.internal.css;

import org.eclipse.e4.ui.internal.css.swt.ICTabRendering;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

@SuppressWarnings("restriction")
public interface ICTabFolderRendering extends ICTabRendering {

    void setTextVisible(boolean visible);

    void setImageVisible(boolean visible);

    void setOuterBorderVisible(boolean visible);

    void setInnerBorderVisible(boolean visible);

    void setHoverTabColor(Color color);

    void setHoverTabColor(Color[] colors, int[] percents);

    void setSelectedTabAreaColor(Color color);

    void setSelectedTabAreaColor(Color[] colors, int[] percents);

    void setUnselectedTabsBackgroundVisible(boolean visible);

    void setMaximizeImage(Image maxImage);

    void setMinimizeImage(Image minImage);

    void setCloseImage(Image closeImage);

    void setClsoeHoverImage(Image closeHoverImage);

    void setNoneRender(boolean nothingToRender);

}
