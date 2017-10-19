package org.xmind.ui.datepicker;

import org.eclipse.draw2d.IFigure;

public interface IAnimationAdvisor {
    
    boolean isDone();

    int getMonthsToRoll();

    int getYearsToRoll();

    void addMonthsToRoll(int count);

    void addYearsToRoll(int count);

    void initOldDay(DayFigure figure);
    
    void initNewDay(DayFigure figure);
    
    void updateNewDay(DayFigure figure);

    void setStartMonth(int oldYear, int oldMonth);
    
    void setEndMonth(int newYear, int newMonth);

    int getOldYear();
    
    int getOldMonth();
    
    int getNewYear();
    
    int getNewMonth();
    
    IFigure getLayer();
    
    IFigure getPanel();
    
    int getDuration();
    
}
