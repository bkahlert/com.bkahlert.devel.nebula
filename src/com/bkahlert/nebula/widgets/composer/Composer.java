package com.bkahlert.nebula.widgets.composer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.bkahlert.nebula.utils.ExecUtils;
import com.bkahlert.nebula.utils.IConverter;
import com.bkahlert.nebula.utils.JSONUtils;
import com.bkahlert.nebula.utils.colors.RGB;
import com.bkahlert.nebula.widgets.browser.Browser;
import com.bkahlert.nebula.widgets.browser.BrowserUtils;
import com.bkahlert.nebula.widgets.browser.extended.html.Anker;
import com.bkahlert.nebula.widgets.browser.extended.html.IAnker;

/**
 * This is a wrapped CKEditor (ckeditor.com).
 * <p>
 * <b>Important developer warning</b>: Do not try to wrap a WYSIWYG editor based
 * on iframes like TinyMCE. Some internal browsers (verified with Webkit) do not
 * handle cut, copy and paste actions when the iframe is in focus. CKEditor can
 * operate in both modes - iframes and divs (and p tags).
 * 
 * @author bkahlert
 * 
 */
public class Composer extends Browser {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(Composer.class);
	private static final Pattern URL_PATTERN = Pattern
			.compile("(.*?)(\\w+://[!#$&-;=?-\\[\\]_a-zA-Z~%]+)(.*?)");

	public static enum ToolbarSet {
		DEFAULT, TERMINAL, NONE;
	}

	private final ToolbarSet toolbarSet;

	private final List<IAnkerLabelProvider> ankerLabelProviders = new ArrayList<IAnkerLabelProvider>();
	private final List<ModifyListener> modifyListeners = new ArrayList<ModifyListener>();
	private String oldHtml = "";
	private final Timer delayChangeTimer = new Timer(this.getClass()
			.getSimpleName() + " :: Delay Change Timer", false);
	private TimerTask delayChangeTimerTask = null;

	public Composer(Composite parent, int style) {
		this(parent, style, 0, ToolbarSet.DEFAULT);
	}

	/**
	 * 
	 * @param parent
	 * @param style
	 * @param delayChangeEventUpTo
	 *            is the delay that must have been passed in order to fire a
	 *            change event. If 0 no delay will be applied. The minimal delay
	 *            is defined by the CKEditor's config.js.
	 * @throws IOException
	 */
	public Composer(Composite parent, int style,
			final long delayChangeEventUpTo, ToolbarSet toolbarSet) {
		super(parent, style);
		this.deactivateNativeMenu();

		this.toolbarSet = toolbarSet;

		this.fixShortcuts(delayChangeEventUpTo);
		this.listenForModifications(delayChangeEventUpTo);

		this.open(BrowserUtils.getFileUrl(Composer.class, "html/index.html",
				"?internal=true&toolbarSet="
						+ toolbarSet.toString().toLowerCase()), 30000,
				"return typeof jQuery != \"undefined\" && jQuery(\"html\").hasClass(\"ready\")");

		this.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				if (Composer.this.delayChangeTimer != null) {
					Composer.this.delayChangeTimer.cancel();
				}
			}
		});
	}

	public void fixShortcuts(final long delayChangeEventUpTo) {
		this.getBrowser().addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if ((e.stateMask & SWT.CTRL) != 0
						|| (e.stateMask & SWT.COMMAND) != 0) {
					if (e.keyCode == 97) {
						// a - select all
						Composer.this.selectAll();
					}

					/*
					 * The CKEditor plugin "onchange" does not notify on cut and
					 * paste operations. Therefore we need to handle them here.
					 */
					if (e.keyCode == 120) {
						// x - cut
						// wait for the ui thread to apply the operation
						ExecUtils.asyncExec(new Runnable() {
							@Override
							public void run() {
								Composer.this.modifiedCallback(
										Composer.this.getSource(),
										delayChangeEventUpTo);
							}
						});
					}
					if (e.keyCode == 99) {
						// c - copy
						// do nothing
					}
					if (e.keyCode == 118) {
						// v - paste
						// wait for the ui thread to apply the operation
						ExecUtils.asyncExec(new Runnable() {
							@Override
							public void run() {
								Composer.this.modifiedCallback(
										Composer.this.getSource(),
										delayChangeEventUpTo);
							}
						});
					}
				}

			}
		});
	}

	public void listenForModifications(final long delayChangeEventUpTo) {
		new BrowserFunction(this.getBrowser(), "modified") {
			@Override
			public Object function(Object[] arguments) {
				if (arguments.length >= 1) {
					if (arguments[0] instanceof String) {
						String newHtml = (String) arguments[0];
						Composer.this.modifiedCallback(newHtml,
								delayChangeEventUpTo);
					}
				}
				return null;
			}
		};

		this.getBrowser().addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				Composer.this.modifiedCallback(Composer.this.getSource(), 0);
			}
		});
	}

	protected void modifiedCallback(String html, long delayChangeEventTo) {
		String newHtml = (html == null || html.replace("&nbsp;", " ").trim()
				.isEmpty()) ? "" : html.trim();

		if (StringUtils.equals(this.oldHtml, newHtml)) {
			return;
		}

		this.oldHtml = newHtml;

		// When space entered but widget is not disposing, create links
		if (delayChangeEventTo > 0) {
			String prevCaretCharacter = this.getPrevCaretCharacter();
			if (prevCaretCharacter != null
					&& this.getPrevCaretCharacter().matches("[\\s| ]")) { // space
																			// is
																			// non
																			// breaking
																			// space
				String autoLinkedHtml = this.createLinks(newHtml);
				if (!autoLinkedHtml.equals(newHtml)) {
					this.setSource(autoLinkedHtml, true);
					newHtml = autoLinkedHtml;
				}
			}
		}

		final String saveHtml = newHtml;
		final Runnable fireRunnable = new Runnable() {
			@Override
			public void run() {
				Event event = new Event();
				event.display = Display.getCurrent();
				event.widget = Composer.this;
				event.text = saveHtml;
				event.data = saveHtml;
				ModifyEvent modifyEvent = new ModifyEvent(event);
				for (ModifyListener modifyListener : Composer.this.modifyListeners) {
					modifyListener.modifyText(modifyEvent);
				}
			}
		};

		synchronized (this) {
			if (this.delayChangeTimerTask != null) {
				this.delayChangeTimerTask.cancel();
			}
			if (delayChangeEventTo > 0) {
				this.delayChangeTimerTask = new TimerTask() {
					@Override
					public void run() {
						fireRunnable.run();
					}
				};
				this.delayChangeTimer.schedule(this.delayChangeTimerTask,
						delayChangeEventTo);
			} else {
				this.delayChangeTimerTask = null;
				fireRunnable.run();
			}
		}
	}

	private String createLinks(String html) {
		boolean htmlChanged = false;
		Document doc = Jsoup.parseBodyFragment(html);
		for (Element e : doc.getAllElements()) {
			if (e.tagName().equals("a")) {
				IAnker anker = new Anker(e);
				IAnker newAnker = this.createAnkerFromLabelProviders(anker);
				if (newAnker == null
						&& !anker.getHref().equals(anker.getContent())) {
					newAnker = new Anker(anker.getContent(),
							anker.getClasses(), anker.getContent());
				}

				if (newAnker != null) {
					e.html(newAnker.toHtml());
					htmlChanged = true;
				}
			} else {
				String ownText = e.ownText();
				Matcher matcher = URL_PATTERN.matcher(ownText);
				if (matcher.matches()) {
					String uri = matcher.group(2);

					IAnker anker = new Anker(uri, new String[0], uri);
					IAnker newAnker = this.createAnkerFromLabelProviders(anker);
					if (newAnker == null) {
						newAnker = anker;
					}

					String newHtml = e.html().replace(uri, newAnker.toHtml());
					e.html(newHtml);
					htmlChanged = true;
				}
			}
		}
		String newHtml = htmlChanged ? doc.body().children().toString() : html;
		return newHtml;
	}

	public IAnker createAnkerFromLabelProviders(IAnker oldAnker) {
		IAnker newAnker = null;
		for (IAnkerLabelProvider ankerLabelProvider : this.ankerLabelProviders) {
			if (ankerLabelProvider.isResponsible(oldAnker)) {
				newAnker = new Anker(ankerLabelProvider.getHref(oldAnker),
						ankerLabelProvider.getClasses(oldAnker),
						ankerLabelProvider.getContent(oldAnker));
				break;
			}
		}
		return newAnker;
	}

	public ToolbarSet getToolbarSet() {
		return this.toolbarSet;
	}

	/**
	 * Checks whether the current editor contents present changes when compared
	 * to the contents loaded into the editor at startup.
	 * 
	 * @return
	 */
	public Boolean isDirty() {
		Boolean isDirty = (Boolean) this.getBrowser().evaluate(
				"return com.bkahlert.nebula.editor.isDirty();");
		if (isDirty != null) {
			return isDirty;
		} else {
			return null;
		}
	}

	public void selectAll() {
		this.run("com.bkahlert.nebula.editor.selectAll();");
	}

	public Future<Boolean> setSource(String html) {
		return this.setSource(html, false);
	}

	public Future<Boolean> setSource(String html, boolean restoreSelection) {
		/*
		 * do not wait for the delay to pass but invoke the task immediately
		 */
		synchronized (this) {
			if (this.delayChangeTimerTask != null) {
				this.delayChangeTimerTask.cancel();
				this.delayChangeTimerTask.run();
				this.delayChangeTimerTask = null;
			}
		}
		this.oldHtml = html;
		return this.run("return com.bkahlert.nebula.editor.setSource("
				+ JSONUtils.enquote(html) + ", "
				+ (restoreSelection ? "true" : "false") + ");",
				IConverter.CONVERTER_BOOLEAN);
	}

	/**
	 * TODO use this.run
	 * 
	 * @return
	 */
	public String getSource() {
		if (!this.isLoadingCompleted()) {
			return null;
		}
		String html = (String) this.getBrowser().evaluate(
				"return com.bkahlert.nebula.editor.getSource();");
		return html;
	}

	public void setMode(String mode) {
		this.run("com.bkahlert.nebula.editor.setMode(\"" + mode + "\");");
	}

	public void showSource() {
		this.setMode("source");
	}

	public void hideSource() {
		this.setMode("wysiwyg");
	}

	public String getPrevCaretCharacter() {
		if (!this.isLoadingCompleted()) {
			return null;
		}
		String html = (String) this
				.getBrowser()
				.evaluate(
						"return com.bkahlert.nebula.editor.getPrevCaretCharacter();");
		return html;
	}

	public void saveSelection() {
		this.run("com.bkahlert.nebula.editor.saveSelection();");
	}

	public void restoreSelection() {
		this.run("com.bkahlert.nebula.editor.restoreSelection();");
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		this.run("return com.bkahlert.nebula.editor.setEnabled("
				+ (isEnabled ? "true" : "false") + ");",
				IConverter.CONVERTER_BOOLEAN);
	}

	@Override
	public void setBackground(Color color) {
		this.setBackground(color != null ? new RGB(color.getRGB()) : null);
	}

	public void setBackground(RGB rgb) {
		String hex = rgb != null ? rgb.toHexString() : "transparent";
		this.injectCss("html .cke_reset { background-color: " + hex + "; }");
	}

	public void addAnkerLabelProvider(IAnkerLabelProvider ankerLabelProvider) {
		this.ankerLabelProviders.add(ankerLabelProvider);
	}

	public void removeAnkerLabelProvider(IAnkerLabelProvider ankerLabelProvider) {
		this.ankerLabelProviders.remove(ankerLabelProvider);
	}

	/**
	 * Adds a {@link ModifyListener} to this {@link Image}.
	 * <p>
	 * Please note that {@link ModifyListener#modifyText(ModifyEvent)} is also
	 * fired when the {@link Image} is being disposed. This is the last
	 * opportunity to read the {@link Image}'s current demoAreaContent.
	 * 
	 * @param modifyListener
	 */
	public void addModifyListener(ModifyListener modifyListener) {
		this.modifyListeners.add(modifyListener);
	}

	/**
	 * Removes a {@link ModifyListener} from this {@link Image}.
	 * 
	 * @param modifyListener
	 */
	public void removeModifyListener(ModifyListener modifyListener) {
		this.modifyListeners.remove(modifyListener);
	}

}