package org.xmind.core;

import java.util.List;

public interface ISpanList {

    List<ISpan> getSpans();

    void addSpan(ISpan span);

    void removeSpan(ISpan span);

}
