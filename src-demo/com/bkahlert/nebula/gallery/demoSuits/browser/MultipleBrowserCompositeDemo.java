package com.bkahlert.nebula.gallery.demoSuits.browser;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Future;

import org.apache.commons.io.FileUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.bkahlert.devel.nebula.utils.ExecutorUtil;
import com.bkahlert.devel.nebula.widgets.browser.BrowserComposite;
import com.bkahlert.devel.nebula.widgets.decoration.EmptyText;
import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;

@Demo
public class MultipleBrowserCompositeDemo extends AbstractDemo {

	private BrowserComposite[] browserComposites;
	private String alertString = "Hello World!";
	private static String timeoutString = "10000";
	private static String[] URLS = new String[] { "http://www.google.de",
			"http://www.bkahlert.com", "http://google.com" };

	@Override
	public void createControls(Composite composite) {
		Button alert = new Button(composite, SWT.PUSH);
		alert.setText("alert");
		alert.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						log("alerting");
						for (BrowserComposite browserComposite : browserComposites) {
							try {
								browserComposite
										.run("alert(\""
												+ MultipleBrowserCompositeDemo.this.alertString
												+ "\");").get();
							} catch (Exception e) {
								log(e.toString());
							}
						}
						log("alerted");
					}
				}).start();
			}
		});

		Button fileAlert = new Button(composite, SWT.PUSH);
		fileAlert.setText("alert using external file");
		fileAlert.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						log("alerting using external file");
						try {
							File jsFile = File.createTempFile(
									MultipleBrowserCompositeDemo.class
											.getSimpleName(), ".js");
							FileUtils
									.write(jsFile,
											"alert(\""
													+ MultipleBrowserCompositeDemo.this.alertString
													+ "\");");
							for (BrowserComposite browserComposite : browserComposites) {
								browserComposite.run(jsFile);
							}
						} catch (Exception e) {
							log(e.toString());
						}
						log("alerted using external file");
					}
				}).start();
			}
		});

		Text text = new Text(composite, SWT.BORDER);
		text.setText(this.alertString);
		text.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				MultipleBrowserCompositeDemo.this.alertString = ((Text) e
						.getSource()).getText();
			}
		});

		Text timeout = new Text(composite, SWT.BORDER);
		timeout.setText(MultipleBrowserCompositeDemo.timeoutString);
		timeout.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				MultipleBrowserCompositeDemo.timeoutString = ((Text) e
						.getSource()).getText();
			}
		});

		new EmptyText(timeout, "Timeout for page load");
	}

	@Override
	public void createDemo(Composite parent) {
		parent.setLayout(new FillLayout());

		this.browserComposites = new BrowserComposite[URLS.length];
		for (int i = 0; i < this.browserComposites.length; i++) {
			this.browserComposites[i] = new BrowserComposite(parent, SWT.BORDER);
			this.browserComposites[i].setAllowLocationChange(true);
			try {
				final Future<Boolean> success = this.browserComposites[i]
						.open(new URI(URLS[i]),
								Integer.parseInt(MultipleBrowserCompositeDemo.timeoutString));
				ExecutorUtil.nonUISyncExec(new Runnable() {
					@Override
					public void run() {
						try {
							if (success.get()) {
								log("Page loaded successfully");
							} else {
								log("Page load timed out");
							}
						} catch (Exception e) {
							log("An error occured while loading: "
									+ e.getMessage());
						}
					}
				});
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			log(this.browserComposites[i].getBrowser().getUrl());
		}

		parent.layout();
	}
}
