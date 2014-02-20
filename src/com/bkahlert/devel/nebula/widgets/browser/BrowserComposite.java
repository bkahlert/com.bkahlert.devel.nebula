package com.bkahlert.devel.nebula.widgets.browser;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.browser.LocationAdapter;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.ProgressAdapter;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import com.bkahlert.devel.nebula.utils.EventDelegator;
import com.bkahlert.devel.nebula.utils.ExecUtils;
import com.bkahlert.devel.nebula.utils.IConverter;
import com.bkahlert.devel.nebula.widgets.browser.extended.html.Anker;
import com.bkahlert.devel.nebula.widgets.browser.extended.html.Element;
import com.bkahlert.devel.nebula.widgets.browser.extended.html.IAnker;
import com.bkahlert.devel.nebula.widgets.browser.extended.html.IElement;
import com.bkahlert.devel.nebula.widgets.browser.listener.IAnkerListener;
import com.bkahlert.devel.nebula.widgets.browser.listener.IFocusListener;
import com.bkahlert.nebula.browser.BrowserScriptRunner;
import com.bkahlert.nebula.browser.BrowserScriptRunner.BrowserStatus;
import com.bkahlert.nebula.browser.BrowserUtils;
import com.bkahlert.nebula.utils.CompletedFuture;

public class BrowserComposite extends Composite implements IBrowserComposite {

	private static Logger LOGGER = Logger.getLogger(BrowserComposite.class);

	private Browser browser;
	private BrowserScriptRunner browserScriptRunner;

	private boolean settingUri = false;
	private boolean allowLocationChange = false;

	private final List<IAnkerListener> ankerListeners = new ArrayList<IAnkerListener>();
	private final List<IFocusListener> focusListeners = new ArrayList<IFocusListener>();

	public BrowserComposite(Composite parent, int style) {
		super(parent, style);
		this.setLayout(new FillLayout());

		this.browser = new Browser(this, SWT.NONE);
		this.browserScriptRunner = new BrowserScriptRunner(this.browser) {
			@Override
			public void scriptAboutToBeSentToBrowser(String script) {
				BrowserComposite.this.scriptAboutToBeSentToBrowser(script);
			}

			@Override
			public void scriptReturnValueReceived(Object returnValue) {
				BrowserComposite.this.scriptReturnValueReceived(returnValue);
			}
		};

		new BrowserFunction(this.browser, "mouseenter") {
			@Override
			public Object function(Object[] arguments) {
				if (arguments.length == 1 && arguments[0] instanceof String) {
					BrowserComposite.this.fireAnkerHover((String) arguments[0],
							true);
				}
				return null;
			}
		};
		new BrowserFunction(this.browser, "mouseleave") {
			@Override
			public Object function(Object[] arguments) {
				if (arguments.length == 1 && arguments[0] instanceof String) {
					BrowserComposite.this.fireAnkerHover((String) arguments[0],
							false);
				}
				return null;
			}
		};
		new BrowserFunction(this.getBrowser(), "__focusgained") {
			@Override
			public Object function(Object[] arguments) {
				if (arguments.length == 1 && arguments[0] instanceof String) {
					final IElement element = new Element((String) arguments[0]);
					BrowserComposite.this.fireFocusGained(element);
				}
				return null;
			}
		};
		new BrowserFunction(this.getBrowser(), "__focuslost") {
			@Override
			public Object function(Object[] arguments) {
				if (arguments.length == 1 && arguments[0] instanceof String) {
					final IElement element = new Element((String) arguments[0]);
					BrowserComposite.this.fireFocusLost(element);
				}
				return null;
			}
		};

		this.browser.addLocationListener(new LocationAdapter() {
			@Override
			public void changing(LocationEvent event) {
				if (!BrowserComposite.this.settingUri) {
					if (BrowserComposite.this.browserScriptRunner
							.getBrowserStatus() == BrowserStatus.LOADED) {
						IAnker anker = null;
						try {
							anker = BrowserUtils
									.extractAnker(BrowserComposite.this.browserScriptRunner
											.runImmediately(
													"return window[\"hoveredAnker\"]",
													IConverter.CONVERTER_STRING));
						} catch (Exception e) {
							LOGGER.error(
									"Error getting most recently hovered anker",
									e);
						}
						if (anker == null
								|| !BrowserUtils.fuzzyEquals(anker.getHref(),
										event.location)) {
							anker = new Anker(event.location, null, null);
						}
						for (IAnkerListener ankerListener : BrowserComposite.this.ankerListeners) {
							ankerListener.ankerClicked(anker);
						}
					}
					event.doit = BrowserComposite.this.allowLocationChange
							|| BrowserComposite.this.browserScriptRunner
									.getBrowserStatus() == BrowserStatus.LOADING;
				}
			}

			// TODO call injectAnkerCode after a page has loaded a user clicked
			// on (or do all the same steps on first page load on all
			// consecutive loads)
		});

		this.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				BrowserComposite.this.browserScriptRunner.dispose();
			}
		});
	}

	private boolean successfullyInjectedAnkerHoverCallback = false;

	/**
	 * Injects the code needed for {@link #addAnkerListener(IAnkerListener)} and
	 * {@link #removeAnkerListener(IAnkerListener)} to work.
	 * <p>
	 * The JavaScript remembers a successful injection in case to consecutive
	 * calls are made.
	 * <p>
	 * As soon as a successful injection has been registered,
	 * {@link #successfullyInjectedAnkerHoverCallback} is set so no unnecessary
	 * further injection is made.
	 */
	private void injectAnkerHoverCallback() {
		if (this.successfullyInjectedAnkerHoverCallback) {
			return;
		}

		String js = "return (function(){function e(e){var t=document.createElement(\"div\");t.appendChild(e.cloneNode(true));return t.innerHTML}if(window[\"successfullyInjectedAnkerHoverCallback\"])return false;window[\"hoveredAnker\"]=null;var t=null;window.addEventListener(\"mouseover\",function(n){if(n.srcElement.tagName==\"A\"){var r=e(n.srcElement);window[\"hoveredAnker\"]=r;t=r;if(window[\"mouseenter\"]&&typeof window[\"mouseenter\"]){window[\"mouseenter\"](r)}}},true);window.addEventListener(\"mouseout\",function(n){if(n.srcElement.tagName==\"A\"){var r=e(n.srcElement);t=null;if(window[\"mouseleave\"]&&typeof window[\"mouseleave\"]){window[\"mouseleave\"](r)}}},true);var n=function(e){if(t==null)return;var n=t;t=null;if(window[\"mouseleave\"]&&typeof window[\"mouseleave\"]){window[\"mouseleave\"](n)}};window.addEventListener(\"DOMSubtreeModified\",n,true);window.addEventListener(\"beforeunload\",n,true);window.addEventListener(\"unload\",n,true);window[\"successfullyInjectedAnkerHoverCallback\"]=true;return true})()";
		try {
			boolean success = BrowserComposite.this.runImmediately(js,
					IConverter.CONVERTER_BOOLEAN);
			if (success) {
				BrowserComposite.this.successfullyInjectedAnkerHoverCallback = true;
			}
		} catch (Exception e) {
			LOGGER.error("Could not inject anker hover callback code in "
					+ BrowserComposite.this.getClass().getSimpleName(), e);
		}
	}

	private boolean successfullyInjectedFocusCallback = false;

	/**
	 * Injects the code needed for {@link #addAnkerListener(IAnkerListener)} and
	 * {@link #removeAnkerListener(IAnkerListener)} to work.
	 * <p>
	 * The JavaScript remembers a successful injection in case to consecutive
	 * calls are made.
	 * <p>
	 * As soon as a successful injection has been registered,
	 * {@link #successfullyInjectedAnkerHoverCallback} is set so no unnecessary
	 * further injection is made.
	 */
	private void injectFocusCallback() {
		if (this.successfullyInjectedFocusCallback) {
			return;
		}

		String js = "return (function(){function e(e){try{var t=document.createElement(\"div\");t.appendChild(e.cloneNode(true));return t.innerHTML}catch(n){return null}}if(window[\"successfullyInjectedFocusCallback\"])return false;window[\"__focusElement\"]=null;var t=null;window.addEventListener(\"focus\",function(n){var r=e(n.srcElement);window[\"__focusElement\"]=r;t=r;if(window[\"__focusgained\"]&&typeof window[\"__focusgained\"]){window[\"__focusgained\"](r)}},true);window.addEventListener(\"blur\",function(n){var r=e(n.srcElement);t=null;if(window[\"__focuslost\"]&&typeof window[\"__focuslost\"]){window[\"__focuslost\"](r)}},true);var n=function(e){if(t==null)return;var n=t;t=null;if(window[\"__focuslost\"]&&typeof window[\"__focuslost\"]){window[\"__focuslost\"](n)}};window.addEventListener(\"DOMSubtreeModified\",n,true);window.addEventListener(\"beforeunload\",n,true);window.addEventListener(\"unload\",n,true);window[\"successfullyInjectedFocusCallback\"]=true;return true})()";
		try {
			boolean success = BrowserComposite.this.runImmediately(js,
					IConverter.CONVERTER_BOOLEAN);
			if (success) {
				BrowserComposite.this.successfullyInjectedFocusCallback = true;
			}
		} catch (Exception e) {
			LOGGER.error("Could not inject focus callback code in "
					+ BrowserComposite.this.getClass().getSimpleName(), e);
		}
	}

	@Override
	public Future<Boolean> open(String address, Integer timeout) {
		return this.open(address, timeout, null);
	}

	@Override
	public Future<Boolean> open(final String uri, final Integer timeout,
			final String pageLoadCheckScript) {
		BrowserComposite.this.browserScriptRunner
				.setBrowserStatus(BrowserStatus.LOADING);

		this.browser.addProgressListener(new ProgressAdapter() {
			@Override
			public void completed(ProgressEvent event) {
				if (BrowserComposite.this.browser == null
						|| BrowserComposite.this.browser.isDisposed()) {
					return;
				}

				if (BrowserComposite.this.browserScriptRunner
						.getBrowserStatus() != BrowserStatus.LOADING) {
					if (BrowserComposite.this.browserScriptRunner
							.getBrowserStatus() != BrowserStatus.CANCELLED) {
						LOGGER.error("State Error: "
								+ BrowserComposite.this.browserScriptRunner
										.getBrowserStatus());
					}
					return;
				}

				try {
					/*
					 * WORKAROUND: If multiple browsers are instantiated it can
					 * occur that some have not loaded, yet. Therefore we poll
					 * the page until it is really loaded. Optionally a user
					 * provided pageLoadCheckScript is executed.
					 */
					String readyState = BrowserComposite.this.browserScriptRunner
							.runImmediately("return document.readyState;",
									IConverter.CONVERTER_STRING);
					if (readyState.equals("complete")
							&& (pageLoadCheckScript == null || BrowserComposite.this.browserScriptRunner
									.runImmediately(pageLoadCheckScript,
											IConverter.CONVERTER_BOOLEAN))) {

						String uri = BrowserComposite.this.browser.getUrl();
						final Future<Void> finished = BrowserComposite.this
								.beforeCompletion(uri);
						ExecUtils.nonUISyncExec(BrowserComposite.class,
								"Progress Check for " + uri, new Runnable() {
									@Override
									public void run() {
										try {
											if (finished != null) {
												finished.get();
											}
										} catch (Exception e) {
											LOGGER.error(e);
										}

										BrowserComposite.this
												.injectAnkerHoverCallback();
										BrowserComposite.this
												.injectFocusCallback();
										synchronized (BrowserComposite.this.monitor) {
											if (BrowserComposite.this.browserScriptRunner
													.getBrowserStatus() != BrowserStatus.CANCELLED) {
												BrowserComposite.this.browserScriptRunner
														.setBrowserStatus(BrowserStatus.LOADED);
											}
											BrowserComposite.this.monitor
													.notifyAll();
										}
									}
								});
					} else {
						ExecUtils.asyncExec(new Runnable() {
							@Override
							public void run() {
								completed(null);
							}
						}, 50);
					}
				} catch (Exception e) {
					LOGGER.error(
							"An error occurred while checking the page load state",
							e);
					synchronized (BrowserComposite.this.monitor) {
						BrowserComposite.this.monitor.notifyAll();
					}
				}
			}
		});

		return ExecUtils.nonUIAsyncExec(BrowserComposite.class, "Opening "
				+ uri, new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				// stops waiting after timeout
				Future<Void> timeoutMonitor = null;
				if (timeout != null && timeout > 0) {
					timeoutMonitor = ExecUtils.nonUIAsyncExec(
							BrowserComposite.class, "Timeout Watcher for "
									+ uri, new Runnable() {
								@Override
								public void run() {
									synchronized (BrowserComposite.this.monitor) {
										if (BrowserComposite.this.browserScriptRunner
												.getBrowserStatus() != BrowserStatus.LOADED) {
											BrowserComposite.this.browserScriptRunner
													.setBrowserStatus(BrowserStatus.CANCELLED);
										}
										BrowserComposite.this.monitor
												.notifyAll();
									}
								}
							}, timeout);
				} else {
					LOGGER.warn("timeout must be greater or equal 0. Ignoring timeout.");
				}

				BrowserComposite.this.beforeLoad(uri);

				ExecUtils.syncExec(new Runnable() {
					@Override
					public void run() {
						BrowserComposite.this.settingUri = true;
						BrowserComposite.this.browser.setUrl(uri.toString());
						BrowserComposite.this.settingUri = false;
					}
				});

				BrowserComposite.this.afterLoad(uri);

				synchronized (BrowserComposite.this.monitor) {
					if (BrowserComposite.this.browserScriptRunner
							.getBrowserStatus() == BrowserStatus.LOADING) {
						LOGGER.debug("Waiting for "
								+ uri
								+ " to be loaded (Thread: "
								+ Thread.currentThread()
								+ "; status: "
								+ BrowserComposite.this.browserScriptRunner
										.getBrowserStatus() + ")");
						BrowserComposite.this.monitor.wait();
						// notified by progresslistener or by timeout
					}

					if (timeoutMonitor != null) {
						timeoutMonitor.cancel(true);
					}

					switch (BrowserComposite.this.browserScriptRunner
							.getBrowserStatus()) {
					case LOADED:
						LOGGER.debug("Successfully loaded " + uri);
						break;
					case CANCELLED:
						LOGGER.error("Aborted loading " + uri
								+ " due to timeout");
						break;
					default:
						throw new RuntimeException("Implementation error");
					}

					return BrowserComposite.this.browserScriptRunner
							.getBrowserStatus() == BrowserStatus.LOADED;
				}
			}
		});
	}

	@Override
	public Future<Boolean> open(URI uri, Integer timeout) {
		return this.open(uri.toString(), timeout, null);
	}

	@Override
	public Future<Boolean> open(URI uri, Integer timeout,
			String pageLoadCheckScript) {
		return this.open(uri.toString(), timeout, pageLoadCheckScript);
	}

	@Override
	public Future<Boolean> openAboutBlank() {
		try {
			return this.open(new URI("about:blank"), 5000);
		} catch (URISyntaxException e) {
			return new CompletedFuture<Boolean>(false, e);
		}
	}

	@Override
	public void setAllowLocationChange(boolean allow) {
		this.allowLocationChange = allow;
	}

	@Override
	public void beforeLoad(String uri) {
	}

	@Override
	public void afterLoad(String uri) {
	}

	@Override
	public Future<Void> beforeCompletion(String uri) {
		return null;
	}

	@Override
	public void addListener(int eventType, Listener listener) {
		// TODO evtl. erst ausführen, wenn alles wirklich geladen wurde, um
		// evtl. falsche Mauskoordinaten zu verhindern und so ein Fehlverhalten
		// im InformationControl vorzeugen
		if (EventDelegator.mustDelegate(eventType, this)) {
			this.browser.addListener(eventType, listener);
		} else {
			super.addListener(eventType, listener);
		}
	}

	/**
	 * Deactivate browser's native context/popup menu. Doing so allows the
	 * definition of menus in an inheriting composite via setMenu.
	 */
	public void deactivateNativeMenu() {
		this.getBrowser().addListener(SWT.MenuDetect, new Listener() {
			@Override
			public void handleEvent(Event event) {
				event.doit = false;
			}
		});
	}

	@Override
	public Browser getBrowser() {
		return this.browser;
	}

	public boolean isLoadingCompleted() {
		return this.browserScriptRunner.getBrowserStatus() == BrowserStatus.LOADED;
	}

	private final Object monitor = new Object();

	@Override
	public Future<Boolean> inject(URI script) {
		return this.browserScriptRunner.inject(script);
	}

	@Override
	public Future<Boolean> run(final File script) {
		return this.browserScriptRunner.run(script);
	}

	@Override
	public Future<Boolean> run(final URI script) {
		return this.browserScriptRunner.run(script);
	}

	@Override
	public Future<Object> run(final String script) {
		return this.browserScriptRunner.run(script);
	}

	@Override
	public <DEST> Future<DEST> run(final String script,
			final IConverter<Object, DEST> converter) {
		return this.browserScriptRunner.run(script, converter);
	}

	@Override
	public <DEST> DEST runImmediately(String script,
			IConverter<Object, DEST> converter) throws Exception {
		return this.browserScriptRunner.runImmediately(script, converter);
	}

	@Override
	public void runImmediately(File script) throws Exception {
		this.browserScriptRunner.runImmediately(script);
	}

	@Override
	public void scriptAboutToBeSentToBrowser(String script) {
		return;
	}

	@Override
	public void scriptReturnValueReceived(Object returnValue) {
		return;
	}

	@Override
	public Future<Void> injectCssFile(URI uri) {
		return this
				.run("if(document.createStyleSheet){document.createStyleSheet(\""
						+ uri.toString()
						+ "\")}else{$(\"head\").append($(\"<link rel=\\\"stylesheet\\\" href=\\\""
						+ uri.toString() + "\\\" type=\\\"text/css\\\" />\"))}",
						IConverter.CONVERTER_VOID);
	}

	@Override
	public Future<Void> injectCss(String css) {
		String script = "(function(){var style=document.createElement(\"style\");style.appendChild(document.createTextNode(\""
				+ css
				+ "\"));(document.getElementsByTagName(\"head\")[0]||document.documentElement).appendChild(style)})()";
		return this.run(script, IConverter.CONVERTER_VOID);
	}

	@Override
	public void addAnkerListener(IAnkerListener ankerListener) {
		this.ankerListeners.add(ankerListener);
	}

	@Override
	public void removeAnkerListener(IAnkerListener ankerListener) {
		this.ankerListeners.remove(ankerListener);
	}

	/**
	 * 
	 * @param string
	 * @param mouseEnter
	 *            true if mouseenter; false otherwise
	 */
	protected void fireAnkerHover(String html, boolean mouseEnter) {
		IAnker anker = BrowserUtils.extractAnker(html);
		for (IAnkerListener ankerListener : BrowserComposite.this.ankerListeners) {
			ankerListener.ankerHovered(anker, mouseEnter);
		}
	}

	/**
	 * 
	 * @param string
	 */
	protected void fireAnkerClicked(String html) {
		IAnker anker = BrowserUtils.extractAnker(html);
		for (IAnkerListener ankerListener : BrowserComposite.this.ankerListeners) {
			ankerListener.ankerClicked(anker);
		}
	}

	@Override
	public void addFocusListener(IFocusListener focusListener) {
		this.focusListeners.add(focusListener);
	}

	@Override
	public void removeFocusListener(IFocusListener focusListener) {
		this.focusListeners.remove(focusListener);
	}

	synchronized protected void fireFocusGained(IElement element) {
		for (IFocusListener focusListener : this.focusListeners) {
			focusListener.focusGained(element);
		}
	}

	synchronized protected void fireFocusLost(IElement element) {
		for (IFocusListener focusListener : this.focusListeners) {
			focusListener.focusLost(element);
		}
	}

	@Override
	public Future<Boolean> containsElementWithID(String id) {
		return this.run("return document.getElementById('" + id + "') != null",
				IConverter.CONVERTER_BOOLEAN);
	}

	@Override
	public Future<Boolean> containsElementsWithName(String name) {
		return this.run("return document.getElementsByName('" + name
				+ "').length > 0", IConverter.CONVERTER_BOOLEAN);
	}

	@Override
	public Future<Void> setBodyHtml(String html) {
		String escapedHtml = html.replace("\n", "<br>").replace("&#xD;", "")
				.replace("\r", "").replace("\"", "\\\"").replace("'", "\\'");
		return this.run("document.body.innerHTML = ('" + escapedHtml + "');",
				IConverter.CONVERTER_VOID);
	}

	@Override
	public Future<String> getBodyHtml() {
		return this.run("return document.body.innerHTML",
				IConverter.CONVERTER_STRING);
	}

}
