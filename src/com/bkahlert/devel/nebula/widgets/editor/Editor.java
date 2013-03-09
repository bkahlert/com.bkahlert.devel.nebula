package com.bkahlert.devel.nebula.widgets.editor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import com.bkahlert.devel.nebula.utils.ExecutorUtil;
import com.bkahlert.devel.nebula.widgets.browser.listener.IAnkerListener;
import com.bkahlert.devel.nebula.widgets.composer.Composer;
import com.bkahlert.devel.nebula.widgets.composer.Composer.ToolbarSet;
import com.bkahlert.devel.nebula.widgets.composer.IAnkerLabelProvider;

/**
 * Instances of this class wrap a {@link Composer} and at load and save
 * functionality.
 * 
 * @author bkahlert
 * 
 */
public abstract class Editor<T> extends Composite {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(Editor.class);

	private T loadedObject = null;
	private Job loadJob = null;
	private Map<T, Job> saveJobs = new HashMap<T, Job>();

	protected Composer composer;

	/**
	 * 
	 * @param parent
	 * @param style
	 * @param delayChangeEventUpTo
	 *            is the delay that must have been passed in order save the
	 *            currently loaded object. If 0 no delay will be applied. The
	 *            minimal delay however defined by the wrapped {@link Composer}.
	 * @param toolbarSet
	 */
	public Editor(Composite parent, int style, final long delayChangeEventUpTo,
			ToolbarSet toolbarSet) {
		super(parent, style & ~SWT.BORDER);

		this.setLayout(new FillLayout());
		this.composer = new Composer(this, style & SWT.BORDER,
				delayChangeEventUpTo, toolbarSet);
		this.composer.setEnabled(false);
	}

	/**
	 * @param ankerListener
	 * @see com.bkahlert.devel.nebula.widgets.browser.BrowserComposite#addAnkerListener(com.bkahlert.devel.nebula.widgets.browser.listener.IAnkerListener)
	 */
	public void addAnkerListener(IAnkerListener ankerListener) {
		this.composer.addAnkerListener(ankerListener);
	}

	/**
	 * @param ankerLabelProvider
	 * @see com.bkahlert.devel.nebula.widgets.composer.Composer#addAnkerLabelProvider(com.bkahlert.devel.nebula.widgets.composer.IAnkerLabelProvider)
	 */
	public void addAnkerLabelProvider(IAnkerLabelProvider ankerLabelProvider) {
		this.composer.addAnkerLabelProvider(ankerLabelProvider);
	}

	/**
	 * 
	 * @see com.bkahlert.devel.nebula.widgets.composer.Composer#showSource()
	 */
	public void showSource() {
		this.composer.showSource();
	}

	/**
	 * 
	 * @see com.bkahlert.devel.nebula.widgets.composer.Composer#hideSource()
	 */
	public void hideSource() {
		this.composer.hideSource();
	}

	@Override
	public boolean setFocus() {
		return this.composer.setFocus();
	}

	public T getLoadedObject() {
		return this.loadedObject;
	}

	/**
	 * Loads the given object and immediately returns.
	 * <p>
	 * May safely be called multiple times. If this {@link Editor} is still
	 * loading the current load job is cancelled.
	 * 
	 * @ArbitraryThread may be called from whatever thread you like.
	 * 
	 * @param objectToLoad
	 * @return the {@link Job} used to load the object; null if no object to be
	 *         loaded.
	 * @throws Exception
	 */
	public final Job load(final T objectToLoad) {
		if (this.loadedObject == objectToLoad) {
			return null;
		}

		if (this.loadJob != null) {
			this.loadJob.cancel();
		}

		if (objectToLoad == null) {
			// refreshHeader();
			this.loadedObject = null;
			ExecutorUtil.syncExec(new Runnable() {
				@Override
				public void run() {
					Editor.this.composer.setSource("");
					Editor.this.composer.setEnabled(false);
				}
			});
			return null;
		} else {
			this.loadJob = new Job("Loading " + objectToLoad) {
				@Override
				protected IStatus run(IProgressMonitor progressMonitor) {
					if (progressMonitor.isCanceled()) {
						Editor.this.loadedObject = null;
						return Status.CANCEL_STATUS;
					}
					SubMonitor monitor = SubMonitor.convert(progressMonitor, 3);
					final String html = Editor.this.getHtml(objectToLoad,
							monitor.newChild(1));
					// refreshHeader();
					monitor.worked(1);
					ExecutorUtil.syncExec(new Runnable() {
						@Override
						public void run() {
							Editor.this.composer.setSource(html);
							Editor.this.composer.setEnabled(true);
						}
					});
					monitor.worked(1);
					monitor.done();
					Editor.this.loadedObject = objectToLoad;
					return Status.OK_STATUS;
				}
			};
			this.loadJob.schedule();
			return this.loadJob;
		}
	}

	/**
	 * Saves the current content of the {@link Editor} to the loaded object.
	 * 
	 * @ArbitraryThread may be called from whatever thread you like.
	 * 
	 * @return the {@link Job} used to save the object.
	 * @throws Exception
	 */
	public final Job save() throws Exception {
		String html = ExecutorUtil.syncExec(new Callable<String>() {
			@Override
			public String call() {
				return Editor.this.composer.getSource();
			}
		});
		return this.save(html);
	}

	/**
	 * Saves the given html to the loaded object.
	 * <p>
	 * In contrast to {@link #save()} this method does not use the
	 * {@link Editor}'s content and thus also works if this widget is being
	 * disposed.
	 * 
	 * @ArbitraryThread may be called from whatever thread you like.
	 * 
	 * @param html
	 * @return the {@link Job} used to save the object.
	 */
	synchronized Job save(final String html) {
		final T savedLoadedObject = this.loadedObject;

		if (this.saveJobs.get(savedLoadedObject) != null) {
			this.saveJobs.get(savedLoadedObject).cancel();
		}

		// make the loaded object still accessible even if another one has been
		// loaded already so the save job can finish

		Job saveJob = new Job("Saving " + savedLoadedObject) {
			@Override
			protected IStatus run(IProgressMonitor progressMonitor) {
				if (progressMonitor.isCanceled()) {
					return Status.CANCEL_STATUS;
				}
				SubMonitor monitor = SubMonitor.convert(progressMonitor, 2);
				monitor.worked(1);
				Editor.this.setHtml(savedLoadedObject, html,
						monitor.newChild(1));
				monitor.done();
				return Status.OK_STATUS;
			}
		};
		saveJob.schedule();
		this.saveJobs.put(savedLoadedObject, saveJob);
		return saveJob;
	}

	/**
	 * Returns the html for the given object.
	 * 
	 * @param objectToLoad
	 * @param monitor
	 * @return
	 */
	public abstract String getHtml(T objectToLoad, IProgressMonitor monitor);

	/**
	 * Sets the given html to the loaded object.
	 * 
	 * @param loadedObject
	 * @param html
	 * @param monitor
	 */
	public abstract void setHtml(T loadedObject, String html,
			IProgressMonitor monitor);

}
