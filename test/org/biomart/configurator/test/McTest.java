package org.biomart.configurator.test;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.biomart.common.resources.Settings;
import org.biomart.configurator.test.category.McTestCategory;
import org.biomart.configurator.utils.McUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = McParameterized.class)
public class McTest {
	
	private Element testcaseElement;
	private static String configxml = "./conf/xml/TestConfig.xml";

    @BeforeClass
    public static void runBeforeClass() {
    	System.setProperty("api","2");
    	//load the config xml and init for MC
    	MartConfigurator.initForWeb();
		Settings.loadGUIConfigProperties();
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
		McTestCategory test = McTestCategory.getCategory(this.testcaseElement.getAttributeValue("name"));
		assertTrue(test.test());
	}
	
	public McTest(Element testcaseElement) {
		this.testcaseElement = testcaseElement;
	}
}