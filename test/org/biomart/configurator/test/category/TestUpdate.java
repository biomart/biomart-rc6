package org.biomart.configurator.test.category;

import org.biomart.common.exceptions.MartBuilderException;
import org.biomart.configurator.update.UpdateMartModel;

public class TestUpdate extends TestAddingSource {
	
	public void testUpdate() throws MartBuilderException{
		UpdateMartModel updateModel = new UpdateMartModel();
		updateModel.updateMart(this.getMart());
	}

	@Override
	public boolean test() {
		// TODO Auto-generated method stub
		this.testOpenXML(testName);
		try {
			this.testUpdate();
		} catch (MartBuilderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.testSaveXML(testName);
		return this.compareXML(testName);
	}
	
}
