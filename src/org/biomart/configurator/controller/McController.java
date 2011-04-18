package org.biomart.configurator.controller;

import java.util.Observable;
import java.util.Observer;
import org.biomart.configurator.model.McModel;
import org.biomart.configurator.utils.McEventObject;
import org.biomart.configurator.view.McView;

public class McController implements Observer {

	private McModel model;
	private McView view;
	
	public McController(McView view, McModel model) {
		this.model = model;
		this.view = view;
		this.model.addObserver(this);
	}
	
	public void update(Observable model, Object obj) {
		this.processC2Vupdate(model,(McEventObject)obj);
	}
	
	public void processV2Cupdate(McEventObject object) {
		this.model.processUpdate(object);
	}
	
	private void processC2Vupdate(Observable model, McEventObject obj) {
//			view.update(model, obj);		
	}
	
}