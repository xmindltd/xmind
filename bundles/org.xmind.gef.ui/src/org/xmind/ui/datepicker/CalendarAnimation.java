/**
 * 
 */
package org.xmind.ui.datepicker;

import static java.util.Calendar.MONTH;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.widgets.Display;

public abstract class CalendarAnimation {

	protected final IAnimationAdvisor advisor;

	private Runnable callback;

	protected IFigure newPanel;

	protected Dimension oldSize;

	protected List<DayFigure> oldDays = new ArrayList<DayFigure>(31);

	protected List<DayFigure> newDays = new ArrayList<DayFigure>(31);

	protected boolean hasNewPanel;

	protected Rectangle oldInitConstraint;

	protected Rectangle oldFinalConstraint;

	protected Rectangle newInitConstraint;

	protected Rectangle newFinalConstraint;

	private Rectangle oldConstraint;

	private Rectangle newConstraint;

	private long start;

	public CalendarAnimation(IAnimationAdvisor advisor) {
		this.advisor = advisor;
	}

	public CalendarAnimation callback(Runnable callback) {
		this.callback = callback;
		return this;
	}

	public IFigure getNewPanel() {
		return newPanel;
	}

	public void start() {
		init();
		start = System.currentTimeMillis();
		Runnable step = new Runnable() {
			public void run() {
				int pass = (int) (System.currentTimeMillis() - start);
				if (pass > advisor.getDuration()) {
					end();
				} else {
					playback(pass);
					Display.getCurrent().asyncExec(this);
				}
			}
		};
		Display.getCurrent().asyncExec(step);
	}

	private void init() {
		this.oldSize = advisor.getPanel().getSize();
		initOldDays();
		newPanel = createNewPanel();
		hasNewPanel = newPanel != advisor.getPanel();
		if (hasNewPanel) {
			advisor.getLayer().add(newPanel);
		}
		createNewDays();
		initConstraint();
	}

	private void initOldDays() {
		List dayFigures = advisor.getPanel().getChildren();
		for (int i = 0; i < dayFigures.size(); i++) {
			DayFigure dayFigure = (DayFigure) dayFigures.get(i);
			advisor.initOldDay(dayFigure);
			Calendar date = dayFigure.getDate();
			int month = date.get(MONTH);
			if (month == advisor.getOldMonth()) {
				oldDays.add(dayFigure);
			} else if (month == advisor.getNewMonth()) {
				oldDayInNewMonth(dayFigure, date, month, i);
			}
		}
	}

	protected IFigure createNewPanel() {
		return advisor.getPanel();
	}

	protected abstract void createNewDays();

	protected void oldDayInNewMonth(DayFigure dayFigure, Calendar date,
			int month, int index) {
	}

	protected abstract Rectangle createOldInitConstraint();

	protected abstract Rectangle createOldFinalConstraint();

	protected Rectangle createNewInitConstraint() {
		return null;
	}

	protected Rectangle createNewFinalConstraint() {
		return null;
	}

	private void initConstraint() {
		hasNewPanel = newPanel != advisor.getPanel();
		oldInitConstraint = createOldInitConstraint();
		oldFinalConstraint = createOldFinalConstraint();
		newInitConstraint = hasNewPanel ? createNewInitConstraint() : null;
		newFinalConstraint = hasNewPanel ? createNewFinalConstraint() : null;

		oldConstraint = oldInitConstraint.getCopy();
		newConstraint = hasNewPanel ? newInitConstraint.getCopy() : null;
		advisor.getLayer().setConstraint(advisor.getPanel(), oldConstraint);
		if (hasNewPanel)
			advisor.getLayer().setConstraint(newPanel, newConstraint);
	}

	private void playback(int elapsed) {
		int duration = advisor.getDuration();
		IFigure layer = advisor.getLayer();
		oldConstraint.x = DatePicker.calc(oldInitConstraint.x,
				oldFinalConstraint.x, elapsed, duration);
		oldConstraint.y = DatePicker.calc(oldInitConstraint.y,
				oldFinalConstraint.y, elapsed, duration);
		layer.setConstraint(advisor.getPanel(), oldConstraint);

		if (hasNewPanel) {
			newConstraint.x = DatePicker.calc(newInitConstraint.x,
					newFinalConstraint.x, elapsed, duration);
			newConstraint.y = DatePicker.calc(newInitConstraint.y,
					newFinalConstraint.y, elapsed, duration);
			layer.setConstraint(newPanel, newConstraint);
		}

		for (DayFigure oldDay : oldDays) {
			oldDay.setTextAlpha(DatePicker.calc(DatePicker.NORMAL_ALPHA,
					DatePicker.SIBLING_MONTH_ALPHA, elapsed, duration));
		}
		for (DayFigure newDay : newDays) {
			newDay.setTextAlpha(DatePicker.calc(DatePicker.SIBLING_MONTH_ALPHA,
					DatePicker.NORMAL_ALPHA, elapsed, duration));
		}
	}

	protected void end() {
		removeOldDays();
		updateNewPanel();
		updateJobs();
		if (callback != null)
			callback.run();
	}

	protected void removeOldDays() {
	}

	private void updateNewPanel() {
		List dayFigures = newPanel.getChildren();
		for (int i = 0; i < dayFigures.size(); i++) {
			DayFigure dayFigure = (DayFigure) dayFigures.get(i);
			advisor.updateNewDay(dayFigure);
		}
		newPanel.invalidate();
		advisor.getLayer().setConstraint(newPanel, null);
	}

	protected abstract void updateJobs();

}