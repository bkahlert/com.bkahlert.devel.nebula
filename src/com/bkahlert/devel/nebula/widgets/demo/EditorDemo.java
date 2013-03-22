package com.bkahlert.devel.nebula.widgets.demo;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.about.AboutAction;

import com.bkahlert.devel.nebula.dialogs.PopupDialog;
import com.bkahlert.devel.nebula.widgets.RoundedComposite;
import com.bkahlert.devel.nebula.widgets.SimpleIllustratedComposite.IllustratedText;
import com.bkahlert.devel.nebula.widgets.browser.IAnker;
import com.bkahlert.devel.nebula.widgets.browser.listener.AnkerAdapter;
import com.bkahlert.devel.nebula.widgets.browser.listener.IAnkerListener;
import com.bkahlert.devel.nebula.widgets.composer.Composer.ToolbarSet;
import com.bkahlert.devel.nebula.widgets.editor.AutosaveEditor;
import com.bkahlert.devel.nebula.widgets.editor.Editor;
import com.bkahlert.nebula.information.EnhanceableInformationControl;
import com.bkahlert.nebula.information.ISubjectInformationProvider;
import com.bkahlert.nebula.information.InformationControl;
import com.bkahlert.nebula.information.InformationControlCreator;
import com.bkahlert.nebula.information.InformationControlManager;
import com.bkahlert.nebula.information.EnhanceableInformationControl.Delegate;
import com.bkahlert.nebula.information.EnhanceableInformationControl.DelegateFactory;

public class EditorDemo extends Composite {

	public EditorDemo(Composite parent, int style) {
		super(parent, style);

		this.setLayout(GridLayoutFactory.fillDefaults().create());

		Composite composerControls = new RoundedComposite(this, SWT.BORDER);
		composerControls.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				false));
		composerControls.setLayout(new RowLayout());

		Button composerSetSource = new Button(composerControls, SWT.PUSH);
		composerSetSource.setText("Set Source 1");
		Button composerSetSource2 = new Button(composerControls, SWT.PUSH);
		composerSetSource2.setText("Set Source 2");

		final Editor<String> editor = new AutosaveEditor<String>(this,
				SWT.NONE, 500, ToolbarSet.DEFAULT) {
			@Override
			public String getHtml(String objectToLoad, IProgressMonitor monitor) {
				return objectToLoad;
			}

			@Override
			public void setHtml(String objectToLoad, String html,
					IProgressMonitor monitor) {
				System.out.println("saved: " + html);
			}
		};
		editor.addAnkerListener(new IAnkerListener() {
			private PopupDialog popup = null;

			@Override
			public void ankerClicked(IAnker anker) {
				System.err.println("clicked on " + anker.getHref());
			}

			@Override
			public void ankerHovered(final IAnker anker, boolean entered) {
				System.err.println((entered ? "entered " : "left") + ": "
						+ anker.getHref());
				if (this.popup != null) {
					this.popup.close();
					this.popup = null;
				}
				if (entered) {
					IllustratedText illustratedText = new IllustratedText(
							SWT.ICON_INFORMATION, anker.getContent());
					this.popup = new PopupDialog(illustratedText, "Test") {
						@Override
						protected Control createControls(Composite parent) {
							Label label = new Label(parent, SWT.NONE);
							label.setText(anker.toHtml());
							return label;
						};
					};
				}
			}
		});

		InformationControlManager<Editor<?>, IAnker> editorInformationControlManager = new InformationControlManager<Editor<?>, IAnker>(
				new InformationControlCreator<IAnker>() {
					@Override
					protected InformationControl<IAnker> doCreateInformationControl(
							Shell parent) {
						return new EnhanceableInformationControl<IAnker, Delegate<IAnker>>(
								parent,
								new DelegateFactory<Delegate<IAnker>>() {
									@Override
									public Delegate<IAnker> create() {
										return new Delegate<IAnker>() {
											private Label label;

											@Override
											public void build(Composite parent) {
												this.label = new Label(parent,
														SWT.BORDER);
											}

											@Override
											public boolean load(
													IAnker anker,
													ToolBarManager toolBarManager) {
												if (anker == null) {
													return false;
												}

												System.out.println(this.label
														.hashCode());

												if (toolBarManager != null) {
													toolBarManager
															.add(new AboutAction(
																	PlatformUI
																			.getWorkbench()
																			.getActiveWorkbenchWindow()));
												}
												String content = toolBarManager != null ? anker
														.toHtml() : anker
														.getContent();
												System.out.println(content);
												this.label.setText(content);
												return true;
											}
										};
									}
								});
					}
				}, new ISubjectInformationProvider<Editor<?>, IAnker>() {
					private IAnker hoveredAnker = null;
					private IAnkerListener ankerListener = new AnkerAdapter() {
						@Override
						public void ankerHovered(IAnker anker, boolean entered) {
							hoveredAnker = entered ? anker : null;
						}
					};

					@Override
					public void register(Editor<?> editor) {
						editor.addAnkerListener(this.ankerListener);
					}

					@Override
					public void unregister(Editor<?> editor) {
						editor.removeAnkerListener(this.ankerListener);
					}

					@Override
					public Point getHoverArea() {
						return new Point(50, 20);
					}

					@Override
					public IAnker getInformation() {
						return this.hoveredAnker;
					}
				});
		editorInformationControlManager.install(editor);

		try {
			editor.load("This is an auto-saving editor");
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		editor.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		editor.load("Hello CKEditor - Editor!");

		composerSetSource.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				editor.load("<p title='test'><b>Hallo</b><i>Welt 1!</i></p>");
			}
		});
		composerSetSource2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				editor.load("<p title='test'><b>Hallo</b><i>Welt 2!</i></p>");
			}
		});
	}
}
