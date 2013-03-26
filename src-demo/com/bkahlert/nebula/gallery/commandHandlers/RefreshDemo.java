package com.bkahlert.nebula.gallery.commandHandlers;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import com.bkahlert.nebula.gallery.views.GalleryView;


public class RefreshDemo extends AbstractHandler {

    protected final Logger log = Logger.getLogger(ShowWidgetGalleryView.class);

    public Object execute(ExecutionEvent event) throws ExecutionException {
	IWorkbenchPart part = HandlerUtil.getActivePart(event);
	if (part instanceof GalleryView) {
	    GalleryView view = (GalleryView) part;
	    view.openDemo();
	}
	return null;
    }

}