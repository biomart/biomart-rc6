package org.biomart.configurator.test;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import junit.extensions.abbot.ScriptFixture;

import org.biomart.common.resources.Settings;
import org.biomart.configurator.test.category.McTestCategory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = McParameterized.class)
public class McTestDevelopment {
	
	private Element testcaseElement;
	private static String configxml = "./conf/xml/TestConfigDevelopment.xml";

    @BeforeClass
    public static void runBeforeClass() {
    	//load the config xml and init for MC
    	Settings.setApplication(Settings.MARTCONFIGURATOR);
    	MartConfigurator.initForWeb();
		Settings.loadGUIConfigProperties();
		Settings.load();
		SettingsForTest.loadConfigXML(configxml);
    }
    
	@Parameters
	public static Collection<Object[]> data() {
		List<Object[]> query = new ArrayList<Object[]>();

		SAXBuilder saxBuilder = new SAXBuilder("org.apache.xerces.parsers.SAXParser", false);
		try {
			Document document = saxBuilder.build(configxml);
			@SuppressWarnings("unchecked")
			List<org.jdom.Element> xmlElementList = document.getRootElement().getChildren("testcase");
			for(org.jdom.Element element: xmlElementList) {
				//get the first xml for now
				if("true".equals(element.getAttributeValue("ignore"))) {
					continue;
				} else {
					Element[] eArray = new Element[]{element};
					query.add(eArray);
				}
			}
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return query;
	}
	
	@Test
	public void test() {
		String testCaseName = this.testcaseElement.getAttributeValue("name");
		McTestCategory test = McTestCategory.getCategory(testCaseName);
		if(SettingsForTest.isGenerateReferenceXML()){			
			String testCaseRefXMLName = SettingsForTest.getTestCaseXMLPath(testCaseName);
			String os = System.getProperty("os.name").toLowerCase();
			//before regenerate reference xml, set the current open directory to testdata
			String curDir = Settings.getProperty("currentSaveDir");
			Settings.setProperty("currentSaveDir", SettingsForTest.getTestPath());
			Settings.save();
			if(os.indexOf("win")>=0){
				//set testCaseRefXMLName to be windows script
				testCaseRefXMLName += "_win.xml";
			}else if(os.indexOf("mac")>=0){
				testCaseRefXMLName += "_mac.xml";
			}else if(os.indexOf("nix")>=0 || os.indexOf("nux")>=0){
				testCaseRefXMLName += "_linux.xml";
			}else{
				System.err.println(os+" is not supported OS!");
				return;
			}
			if(!testCaseRefXMLName.isEmpty()){
				//regenerate reference xml
				ScriptFixture sf = new ScriptFixture(testCaseRefXMLName);
				sf.run();
			}
			//after finish set back current open dir
			try{
				Settings.setProperty("currentSaveDir", curDir);
			}catch(NullPointerException npe){
				
			}
			Settings.save();
		}{
			assertTrue(test.test());
		}		
	}
	
	public McTestDevelopment(Element testcaseElement) {
		this.testcaseElement = testcaseElement;
	}
}