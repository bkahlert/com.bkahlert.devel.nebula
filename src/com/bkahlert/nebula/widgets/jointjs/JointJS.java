package com.bkahlert.nebula.widgets.jointjs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

import com.bkahlert.nebula.utils.IConverter;
import com.bkahlert.nebula.utils.IReflexiveConverter;
import com.bkahlert.nebula.utils.JSONUtils;
import com.bkahlert.nebula.utils.colors.RGB;
import com.bkahlert.nebula.utils.selection.SelectionUtils;
import com.bkahlert.nebula.widgets.browser.Browser;
import com.bkahlert.nebula.widgets.browser.BrowserUtils;

/**
 * Shows an image in a way that it always fills the {@link Composite}'s
 * available width.
 * 
 * @author bkahlert
 * 
 */
public class JointJS extends Browser implements ISelectionProvider {

	private static final Logger LOGGER = Logger.getLogger(JointJS.class);

	public static interface IJointJSListener {
		public void loaded(String json);

		public void save(String json);

		public void linkTitleChanged(String id, String title);

		public void hovered(String id, boolean hoveredIn);
	}

	public static class JointJSListener implements IJointJSListener {

		@Override
		public void loaded(String json) {
		}

		@Override
		public void save(String json) {
		}

		@Override
		public void linkTitleChanged(String id, String title) {
		}

		@Override
		public void hovered(String id, boolean hoveredIn) {
		}

	}

	private final List<IJointJSListener> jointJSListeners = new ArrayList<IJointJSListener>();

	private final ListenerList selectionChangedListeners = new ListenerList();
	private ISelection selection = new StructuredSelection();
	private IReflexiveConverter<String, Object> selectionConverter;
	private String lastHovered = null;

	private String nodeCreationPrefix;
	private String linkCreationPrefix;

	public JointJS(Composite parent, int style, String nodeCreationPrefix,
			String linkCreationPrefix) {
		this(parent, style, nodeCreationPrefix, linkCreationPrefix, null);
	}

	/**
	 * 
	 * @param parent
	 * @param style
	 * @param nodeCreationPrefix
	 *            prefix used if a node is created but no id was passed. The
	 *            prefix is put in front of the automatically generated id.
	 * @param linkCreationPrefix
	 *            prefix used if a link is created but no id was passed. The
	 *            prefix is put in front of the automatically generated id.
	 * @param selectionConverter
	 *            converts the model id string to a type of your choice before
	 *            firing it as an selection
	 */
	public JointJS(Composite parent, int style, String nodeCreationPrefix,
			String linkCreationPrefix,
			final IReflexiveConverter<String, Object> selectionConverter) {
		super(parent, style);
		this.deactivateNativeMenu();

		this.selectionConverter = selectionConverter;

		Assert.isNotNull(nodeCreationPrefix);
		Assert.isNotNull(linkCreationPrefix);

		this.nodeCreationPrefix = nodeCreationPrefix;
		this.linkCreationPrefix = linkCreationPrefix;

		new BrowserFunction(this.getBrowser(), "loaded") {
			@Override
			public Object function(Object[] arguments) {
				if (arguments.length == 1) {
					for (IJointJSListener jointJSListener : JointJS.this.jointJSListeners) {
						jointJSListener.loaded((String) arguments[0]);
					}
				}
				return null;
			}
		};

		new BrowserFunction(this.getBrowser(), "save") {
			@Override
			public Object function(Object[] arguments) {
				if (arguments.length == 1) {
					for (IJointJSListener jointJSListener : JointJS.this.jointJSListeners) {
						jointJSListener.save((String) arguments[0]);
					}
				}
				return null;
			}
		};

		new BrowserFunction(this.getBrowser(), "__linkTitleChanged") {
			@Override
			public Object function(Object[] arguments) {
				if (arguments.length == 2) {
					for (IJointJSListener jointJSListener : JointJS.this.jointJSListeners) {
						jointJSListener.linkTitleChanged((String) arguments[0],
								(String) arguments[1]);
					}
				}
				return null;
			}
		};

		new BrowserFunction(this.getBrowser(), "__cellHoveredOver") {
			@Override
			public Object function(Object[] arguments) {
				if (arguments.length == 1) {
					String id = (String) arguments[0];
					JointJS.this.fireHoveredIn(id);
				}
				return null;
			}
		};

		new BrowserFunction(this.getBrowser(), "__cellHoveredOut") {
			@Override
			public Object function(Object[] arguments) {
				if (arguments.length == 1) {
					String id = (String) arguments[0];
					JointJS.this.fireHoveredOut(id);
				}
				return null;
			}
		};

		new BrowserFunction(this.getBrowser(), "__selectionChanged") {
			@Override
			public Object function(Object[] arguments) {
				if (arguments.length == 1 && arguments[0] instanceof Object[]) {
					Object[] ids = (Object[]) arguments[0];
					if (selectionConverter != null) {
						List<Object> selection = new ArrayList<Object>();
						for (Object id : ids) {
							selection.add(selectionConverter
									.convert((String) id));
						}
						JointJS.this.setSelection(new StructuredSelection(
								selection));
					} else {
						JointJS.this.setSelection(new StructuredSelection(ids));
					}
				}
				return null;
			}
		};

		// get focus if mouse hovers over this JointJS instance
		this.getBrowser().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				JointJS.this.getBrowser().setFocus();
			}
		});

		this.getBrowser().addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				JointJS.this.addFocusBorder();
			}

			@Override
			public void focusLost(FocusEvent e) {
				JointJS.this.removeFocusBorder();
				JointJS.this.setFocus(null);
			}
		});

		this.open(BrowserUtils.getFileUrl(JointJS.class, "html/index.html",
				"?internal=true"), 60000);
	}

	public Future<String> load(String json) {
		return JointJS.this.run("return com.bkahlert.nebula.jointjs.load("
				+ JSONUtils.enquote(json) + ")", IConverter.CONVERTER_STRING);
	}

	public Future<String> save() {
		return this.run("return com.bkahlert.nebula.jointjs.save();",
				IConverter.CONVERTER_STRING);
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		this.run("return com.bkahlert.nebula.jointjs.setEnabled("
				+ (isEnabled ? "true" : "false") + ");",
				IConverter.CONVERTER_BOOLEAN);
	}

	public Future<String> getTitle() {
		return this.run("return com.bkahlert.nebula.jointjs.getTitle();",
				IConverter.CONVERTER_STRING);
	}

	public Future<Void> setTitle(String title) {
		return this.run("com.bkahlert.nebula.jointjs.setTitle(\"" + title
				+ "\");", IConverter.CONVERTER_VOID);
	}

	public Future<String> createNode(String id, Object json) {
		if (id == null) {
			id = this.nodeCreationPrefix + UUID.randomUUID().toString();
		}
		return this.run("return com.bkahlert.nebula.jointjs.createNode('" + id
				+ "', " + JSONUtils.buildJson(json) + ");",
				IConverter.CONVERTER_STRING);
	}

	@SuppressWarnings("serial")
	public Future<String> createNode(String id, String title, String content,
			final Point position, final Point size) {
		Map<String, Object> json = new HashMap<String, Object>();
		json.put("title", title);
		if (content != null) {
			json.put("content", content);
		}
		if (position != null) {
			json.put("position", new HashMap<String, Integer>() {
				{
					this.put("x", position.x);
					this.put("y", position.y);
				}
			});
		}
		if (size != null) {
			json.put("size", new HashMap<String, Integer>() {
				{
					this.put("width", size.x);
					this.put("height", size.y);
				}
			});
		}
		return this.createNode(id, json);
	}

	public Future<String> createLink(String id, Object source, Object target) {
		if (id == null) {
			id = this.linkCreationPrefix + UUID.randomUUID().toString();
		}
		return this.run(
				"return com.bkahlert.nebula.jointjs.createLink('" + id + "', "
						+ JSONUtils.buildJson(source) + ", "
						+ JSONUtils.buildJson(target) + ");",
				IConverter.CONVERTER_STRING);
	}

	public Future<String> createPermanentLink(String id, Object source,
			Object target) {
		if (id == null) {
			id = this.linkCreationPrefix + UUID.randomUUID().toString();
		}
		return this.run(
				"return com.bkahlert.nebula.jointjs.createPermanentLink('" + id
						+ "', " + JSONUtils.buildJson(source) + ", "
						+ JSONUtils.buildJson(target) + ");",
				IConverter.CONVERTER_STRING);
	}

	@SuppressWarnings("serial")
	public Future<String> createLink(String id, final String sourceId,
			final String targetId) {
		return this.createLink(id, new HashMap<String, String>() {
			{
				this.put("id", sourceId);
			}
		}, new HashMap<String, String>() {
			{
				this.put("id", targetId);
			}
		});
	}

	@SuppressWarnings("serial")
	public Future<String> createPermanentLink(String id, final String sourceId,
			final String targetId) {
		return this.createPermanentLink(id, new HashMap<String, String>() {
			{
				this.put("id", sourceId);
			}
		}, new HashMap<String, String>() {
			{
				this.put("id", targetId);
			}
		});
	}

	public Future<Void> setText(String id, Object index, String text) {
		String indexParam = "null";
		if (index instanceof Integer) {
			indexParam = ((Integer) index).toString();
		} else if (index instanceof String) {
			indexParam = "'" + index + "'";
		}
		return this.run("return com.bkahlert.nebula.jointjs.setText('" + id
				+ "', " + indexParam + ", '" + text + "');",
				IConverter.CONVERTER_VOID);
	}

	public Future<String> getText(String id, Object index) {
		String indexParam = "null";
		if (index instanceof Integer) {
			indexParam = ((Integer) index).toString();
		} else if (index instanceof String) {
			indexParam = "'" + index + "'";
		}
		return this.run("return com.bkahlert.nebula.jointjs.getText('" + id
				+ "', " + indexParam + ");", IConverter.CONVERTER_STRING);
	}

	public Future<Void> setNodeTitle(String id, String title) {
		return this.setText(id, "title", title);
	}

	public Future<String> getNodeTitle(String id) {
		return this.getText(id, "title");
	}

	public Future<Void> setNodeContent(String id, String content) {
		return this.setText(id, "content", content);
	}

	public Future<String> getNodeContent(String id) {
		return this.getText(id, "content");
	}

	public Future<Void> setLinkTitle(String id, String title) {
		return this.setText(id, 0, title);
	}

	public Future<String> getLinkTitle(String id) {
		return this.getText(id, 0);
	}

	public Future<Void> setColor(String id, RGB rgb) {
		String color = rgb != null ? "'" + rgb.toCssString() + "'"
				: "'initial'";
		return this.run("return com.bkahlert.nebula.jointjs.setColor('" + id
				+ "', " + color + ");", IConverter.CONVERTER_VOID);
	}

	public Future<Void> setBackgroundColor(String id, RGB rgb) {
		String color = rgb != null ? "'" + rgb.toCssString() + "'"
				: "'initial'";
		return this.run(
				"return com.bkahlert.nebula.jointjs.setBackgroundColor('" + id
						+ "', " + color + ");", IConverter.CONVERTER_VOID);
	}

	public Future<Void> setBorderColor(String id, RGB rgb) {
		String color = rgb != null ? "'" + rgb.toCssString() + "'"
				: "'initial'";
		return this.run("return com.bkahlert.nebula.jointjs.setBorderColor('"
				+ id + "', " + color + ");", IConverter.CONVERTER_VOID);
	}

	public Future<Void> setPosition(String id, int x, int y) {
		return this.run("return com.bkahlert.nebula.jointjs.setPosition('" + id
				+ "', " + x + ", " + y + ");", IConverter.CONVERTER_VOID);
	}

	public Future<Void> setSize(String id, int width, int height) {
		return this.run("return com.bkahlert.nebula.jointjs.setSize('" + id
				+ "', " + width + ", " + height + ");",
				IConverter.CONVERTER_VOID);
	}

	public Future<Boolean> remove(String id) {
		Future<Boolean> rt = this.run(
				"return com.bkahlert.nebula.jointjs.removeCell('" + id + "');",
				IConverter.CONVERTER_BOOLEAN);
		if (id != null && id.equals(this.lastHovered)) {
			this.fireHoveredOut(id);
		}
		return rt;
	}

	public Future<List<String>> getNodes() {
		return this.run("return com.bkahlert.nebula.jointjs.getNodes();",
				IConverter.CONVERTER_STRINGLIST);
	}

	public Future<List<String>> getLinks() {
		return this.run("return com.bkahlert.nebula.jointjs.getLinks();",
				IConverter.CONVERTER_STRINGLIST);
	}

	public Future<List<String>> getPermanentLinks() {
		return this.run(
				"return com.bkahlert.nebula.jointjs.getPermanentLinks();",
				IConverter.CONVERTER_STRINGLIST);
	}

	public Future<List<String>> getConnectedLinks(String id) {
		return this.run(
				"return com.bkahlert.nebula.jointjs.getConnectedLinks(\"" + id
						+ "\");", IConverter.CONVERTER_STRINGLIST);
	}

	public Future<List<String>> getConnectedPermanentLinks(String id) {
		return this.run(
				"return com.bkahlert.nebula.jointjs.getConnectedPermanentLinks(\""
						+ id + "\");", IConverter.CONVERTER_STRINGLIST);
	}

	public Future<Double> getZoom() {
		return this.run("return com.bkahlert.nebula.jointjs.getZoom();",
				IConverter.CONVERTER_DOUBLE);
	}

	public Future<Void> setZoom(Double zoom) {
		return this.run("return com.bkahlert.nebula.jointjs.setZoom(" + zoom
				+ ");", IConverter.CONVERTER_VOID);
	}

	public Future<Double> zoomIn() {
		return this.run("return com.bkahlert.nebula.jointjs.zoomIn();",
				IConverter.CONVERTER_DOUBLE);
	}

	public Future<Double> zoomOut() {
		return this.run("return com.bkahlert.nebula.jointjs.zoomOut();",
				IConverter.CONVERTER_DOUBLE);
	}

	public Future<Point> getPan() {
		return this.run("return com.bkahlert.nebula.jointjs.getPan();",
				IConverter.CONVERTER_POINT);
	}

	public Future<Void> setPan(int x, int y) {
		return this.run("return com.bkahlert.nebula.jointjs.setPan(" + x + ", "
				+ y + ");", IConverter.CONVERTER_VOID);
	}

	public Future<Void> autoLayout() {
		return this.run("return com.bkahlert.nebula.jointjs.autoLayout();",
				IConverter.CONVERTER_VOID);
	}

	public Future<Void> highlight(List<String> ids) {
		String list = ids != null ? JSONUtils.buildJson(ids) : "null";
		return this.run("return com.bkahlert.nebula.jointjs.highlight(" + list
				+ ");", IConverter.CONVERTER_VOID);
	}

	public Future<Void> setFocus(List<String> ids) {
		String list = ids != null ? JSONUtils.buildJson(ids) : "null";
		return this.run("return com.bkahlert.nebula.jointjs.setFocus(" + list
				+ ");", IConverter.CONVERTER_VOID);
	}

	public void addJointJSListener(IJointJSListener jointJSListener) {
		this.jointJSListeners.add(jointJSListener);
	}

	public void removeJointJSListener(IJointJSListener jointJSListener) {
		this.jointJSListeners.remove(jointJSListener);
	}

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		this.selectionChangedListeners.add(listener);
	}

	@Override
	public ISelection getSelection() {
		return this.selection;
	}

	@Override
	public void removeSelectionChangedListener(
			ISelectionChangedListener listener) {
		this.selectionChangedListeners.remove(listener);
	}

	@Override
	public void setSelection(ISelection selection) {
		List<Object> objects = SelectionUtils.getObjects(selection);

		List<String> ids = new ArrayList<String>(objects.size());
		for (Object object : objects) {
			ids.add(this.selectionConverter != null ? this.selectionConverter
					.convertBack(object) : object.toString());
		}

		String list = ids != null ? JSONUtils.buildJson(ids) : "null";
		try {
			if (this.isLoadingCompleted()) {
				this.run(
						"return com.bkahlert.nebula.jointjs.highlight(" + list
								+ ");", IConverter.CONVERTER_VOID).get();
			}
			this.selection = selection;
			this.fireSelectionChanged(new SelectionChangedEvent(JointJS.this,
					JointJS.this.selection));
		} catch (Exception e) {
			LOGGER.error("Error setting selection to " + selection);
		}
	}

	/**
	 * Notifies any selection changed listeners that the viewer's selection has
	 * changed. Only listeners registered at the time this method is called are
	 * notified.
	 * 
	 * @param event
	 *            a selection changed event
	 * 
	 * @see ISelectionChangedListener#selectionChanged
	 */
	protected void fireSelectionChanged(final SelectionChangedEvent event) {
		Object[] listeners = this.selectionChangedListeners.getListeners();
		for (int i = 0; i < listeners.length; ++i) {
			final ISelectionChangedListener l = (ISelectionChangedListener) listeners[i];
			SafeRunnable.run(new SafeRunnable() {
				@Override
				public void run() {
					l.selectionChanged(event);
				}
			});
		}
	}

	private void fireHoveredIn(String id) {
		this.lastHovered = id;
		for (IJointJSListener jointJSListener : JointJS.this.jointJSListeners) {
			jointJSListener.hovered(id, true);
		}
	}

	private void fireHoveredOut(String id) {
		for (IJointJSListener jointJSListener : JointJS.this.jointJSListeners) {
			jointJSListener.hovered(id, false);
		}
		this.lastHovered = null;
	}

}
