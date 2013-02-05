package com.bkahlert.devel.nebula.widgets.timeline;

import java.util.Calendar;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWTException;

import com.bkahlert.devel.nebula.utils.CalendarUtils;
import com.bkahlert.devel.nebula.widgets.browser.IBrowserComposite;
import com.bkahlert.devel.nebula.widgets.timeline.impl.Decorator;
import com.bkahlert.devel.nebula.widgets.timeline.impl.Timeline;

/**
 * Instances of this class denote a timeline displayed by means of the SIMILE
 * SelectionTimeline and an integrated browser widget. It also facilitates the
 * interaction with timeline widget by providing a rich API.
 * <p>
 * Please note that this basic implementation only consumes string
 * representations of Java objects. In order to represent a date you need to
 * provide it formatted in ISO 8601.<br/>
 * <p>
 * e.g. Tuesday, 15 May 1984 at 2:30pm in timezone 01:00 summertime would be
 * 1984-05-15T14:30:00+02:00. To make your life easier the static utility method
 * {@link CalendarUtils#toISO8601(Calendar)} is provided.
 * 
 * @author bkahlert
 * 
 */
public interface ITimeline extends IBrowserComposite {

	/**
	 * Runs a Java script in the browser immediately.
	 * <p>
	 * Warning: Calling this method does not guarantee that the DOM has been
	 * loaded, yet. Use {@link #enqueueJs(String)} if you want to make sure.
	 * 
	 * @param js
	 * @return
	 */
	public boolean runJs(String js);

	/**
	 * Runs a Java script in the browser after the DOM has been loaded.
	 * 
	 * @param js
	 */
	public void enqueueJs(String js);

	/**
	 * Includes the given path as a cascading style sheet.
	 * 
	 * @param path
	 */
	public void injectCssFile(String path);

	/**
	 * Display the given JSON string on the {@link ITimeline}. The format is
	 * quite complex.<br>
	 * It is therefore preferable to use
	 * {@link #show(ITimelineInput, IProgressMonitor)}.
	 * 
	 * @param jsonTimeline
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong
	 *                thread</li>
	 *                <li>ERROR_WIDGET_DISPOSED when the widget has been
	 *                disposed</li>
	 *                </ul>
	 */
	public void show(final String jsonTimeline);

	/**
	 * Displays the given {@link ITimelineInput} on the {@link ITimeline}.
	 * 
	 * @param input
	 * @param monitor
	 */
	public void show(ITimelineInput input, IProgressMonitor monitor);

	/**
	 * Sets the date where the visible part of the {@link Timeline} should
	 * start.
	 * 
	 * @param iso8601Date
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong
	 *                thread</li>
	 *                <li>ERROR_WIDGET_DISPOSED when the widget has been
	 *                disposed</li>
	 *                </ul>
	 */
	public void setMinVisibleDate(Calendar calendar);

	/**
	 * Sets the date where the visible part of the {@link Timeline} should
	 * centered.
	 * 
	 * @param iso8601Date
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong
	 *                thread</li>
	 *                <li>ERROR_WIDGET_DISPOSED when the widget has been
	 *                disposed</li>
	 *                </ul>
	 */
	public void setCenterVisibleDate(Calendar calendar);

	/**
	 * Sets the date where the visible part of the {@link Timeline} should end.
	 * 
	 * @param iso8601Date
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong
	 *                thread</li>
	 *                <li>ERROR_WIDGET_DISPOSED when the widget has been
	 *                disposed</li>
	 *                </ul>
	 */
	public void setMaxVisibleDate(Calendar calendar);

	/**
	 * Renders the default {@link Decorator}s plus the given {@link Decorator}
	 * on this {@link Timeline}.
	 * 
	 * @param jsonDecorators
	 *            a list of {@link Decorator} as a json string; example: <code>
	 * [{ "startDate": "2011-09-13T13:08:05+02:00", "endDate": "2011-09-13T13:18:28+02:00" }]</code>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong
	 *                thread</li>
	 *                <li>ERROR_WIDGET_DISPOSED when the widget has been
	 *                disposed</li>
	 *                </ul>
	 */
	public void applyDecorators(String jsonDecorators);
}