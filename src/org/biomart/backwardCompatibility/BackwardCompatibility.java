package org.biomart.backwardCompatibility;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;

import org.biomart.common.resources.Log;
import org.biomart.common.resources.Resources;
import org.biomart.common.utils.PartitionUtils;
import org.biomart.common.utils.XMLElements;
import org.biomart.configurator.model.object.DataLinkInfo;
import org.biomart.configurator.utils.type.Cardinality;
import org.biomart.configurator.utils.type.DataLinkType;
import org.biomart.configurator.utils.type.DatasetTableType;
import org.biomart.configurator.utils.type.PartitionType;
import org.biomart.configurator.utils.type.PortableType;
import org.biomart.objects.enums.FilterType;
import org.biomart.objects.objects.Column;
import org.biomart.objects.objects.Config;
import org.biomart.objects.objects.Container;
import org.biomart.objects.objects.DatasetColumn;
import org.biomart.objects.objects.DatasetTable;
import org.biomart.objects.objects.ElementList;
import org.biomart.objects.objects.Filter;
import org.biomart.objects.objects.ForeignKey;
import org.biomart.objects.objects.Mart;
import org.biomart.objects.objects.MartRegistry;
import org.biomart.objects.objects.PartitionTable;
import org.biomart.objects.objects.PrimaryKey;
import org.biomart.objects.objects.Relation;
import org.biomart.objects.objects.RelationTarget;
import org.biomart.queryEngine.OperatorType;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.filter.ElementFilter;
import org.jdom.input.SAXBuilder;
import org.python.modules.newmodule;
import org.xml.sax.InputSource;


public class BackwardCompatibility {
	private Element allRoot = new Element("options");


	public Element getOptions() {
		return allRoot;
	}
	
	private HashMap<String, Set<String>> pointedDatasets = new HashMap<String, Set<String>>();
	
	public HashMap<String, Set<String>> getPointedDatasets(){
		return pointedDatasets;
	}
	
	@SuppressWarnings("unchecked")
	public List<Mart> parseOldTemplates(){
		List<Mart> martList = new ArrayList<Mart>();
		HashSet<String> tableQuery = new HashSet<String>();
		//TableNameSet tableList = new TableNameSet();
		List<String> simpleTableList = new ArrayList<String>();
		HashMap<String, byte[]> xmlList = new HashMap<String, byte[]>();
		HashMap<String, URL> xmlList2 = new HashMap<String, URL>();
		HashMap<String, HashSet<Integer>> templateMap = new HashMap<String, HashSet<Integer>>();

		if (!isWebService) {
			// Get the list of all tables in the database and store it for later
			try {
				ResultSet result = this.databaseConnection.getMetaData().getTables(null, this.schema, null, null);
				while (result.next()){
					//System.out.println(result.getString(1));
					tableQuery.add(result.getString(3));
				}
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			//tableQuery = queryDB("SHOW TABLES");
			for (String table : tableQuery) {
				if (table.endsWith("__main") || table.endsWith("__dm")) {
					//tableList.add(new TableName(table, isDMPartitioned));
					if(!table.startsWith("meta"))
						simpleTableList.add(table);
				}
			}
			// Get the list of template names and their corresponding set of ID numbers
			try {
				Statement stmt = null;
				stmt = databaseConnection.createStatement();
				ResultSet result = stmt
				.executeQuery("SELECT * FROM meta_template__template__main");

				while (result.next()) {
					//System.out.println(result.getString(1));
					String templateName = result.getString("template");
					HashSet<Integer> datasetIDs = templateMap.get(templateName);
					if (datasetIDs == null) {
						datasetIDs = new HashSet<Integer>();
					}
					datasetIDs.add(result.getInt("dataset_id_key"));
					templateMap.put(templateName, datasetIDs);
				}
				// For all the templates present in the template__main table, get the GZipped XML
				StringBuilder templateInListQuery = new StringBuilder(
				"SELECT * FROM meta_template__xml__dm WHERE template IN (");
				for (String templateName : templateMap.keySet()) {
					templateInListQuery.append("'" + templateName + "',");
				}
				templateInListQuery.delete(templateInListQuery.length() - 1,
						templateInListQuery.length());
				templateInListQuery.append(")");
				//System.out.println(templateInListQuery);
				xmlList = queryDBbytes(templateInListQuery.toString());
				//System.err.println("Data retrieved.");
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {

			for(DatasetFromUrl dataset : this.datasetList){
				xmlList.put(dataset.getName(), null);
				try {
					xmlList2.put(dataset.getName(), new URL( dataset.getUrl()));
					//Log.error("Trying to connect to: " + dataset.getUrl() + " for dataset " + dataset.getDisplayName());
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
		}

		// For each template with retrieved XML
		for(String xmlGZkey : xmlList.keySet()){
			LinkedHashMap<String, HashMap<String, String>> oldPartitionAliases = new LinkedHashMap<String, HashMap<String,String>>();
			HashSet<String> internalNames = new HashSet<String>();
			// Parse the XML
			try {
				InputStream rstream = null;
				SAXBuilder builder = new SAXBuilder();
				Document document = null;
				byte[] xmlGZ = xmlList.get(xmlGZkey);
				if(!isWebService){
					rstream = new GZIPInputStream(new ByteArrayInputStream(xmlGZ));
					InputSource is = new InputSource(rstream);
					document = builder.build(is);
				} else {
					rstream = xmlList2.get(xmlGZkey).openStream();
					Log.error("Trying to connect to: " + xmlGZkey + " " + xmlList2.get(xmlGZkey).toString());
					//System.err.println(xmlList2.get(xmlGZkey).getContent().toString());
					document = builder.build(xmlList2.get(xmlGZkey));
				}
				Element root = document.getRootElement();



				//System.out.println(root.getAttributeValue("template"));
				//String partitionName = root.getAttributeValue("template");
				//System.out.println("Partition table \"" + partitionName + "\":");
				List dynamicDatasets = root.getChildren("DynamicDataset");
				Iterator dynamicDatasetsIterator = dynamicDatasets.iterator();
				// Get the aliases from each dataset in the old template
				while (dynamicDatasetsIterator.hasNext()) {
					Element child = (Element) dynamicDatasetsIterator.next();
					//System.out.print(child.getAttributeValue("internalName"));
					//System.out.print(": ");
					//System.out.println(child.getAttributeValue("aliases"));
					String internalName = child.getAttributeValue("internalName");
					internalNames.add(internalName);
					String aliases = child.getAttributeValue("aliases");
					if(aliases!=null){
						String[] aliasPairs = aliases.split(",", -1);
						for(String aliasPair:aliasPairs){
							String[] aliasPairSplit = aliasPair.split("=");
							String aliasValue;
							if(aliasPairSplit.length > 1)
							{
								aliasValue = aliasPairSplit[1];
							} else { 
								aliasValue = null;
							}
							HashMap<String,String> aliasValuesByDataset = oldPartitionAliases.get(aliasPairSplit[0]);
							if(aliasValuesByDataset==null){
								aliasValuesByDataset = new HashMap<String, String>();
							}
							aliasValuesByDataset.put(internalName,aliasValue);
							oldPartitionAliases.put(aliasPairSplit[0], aliasValuesByDataset);

						}
					}
				}
				// Create a map of the old partition value names to the new column values
				HashMap<String,Integer> oldPartitionToColumn = new HashMap<String, Integer>();
				int firstRow=14; // Start at 14 because of the connection data that's present for all rows, and reserved cols
				for(String aliasName: oldPartitionAliases.keySet()){
					oldPartitionToColumn.put(aliasName,firstRow);
					firstRow++;
				}

				ArrayList<String> rowList = new ArrayList<String>();
				String templateName = xmlGZkey;
				String webTemplateName = templateName;
				boolean hasTemplate = false;

				// Create the new main partition table
				if(!isWebService){
					for(String rowName: internalNames){
						// For every row in the partition table, only include those those that have corresponding
						//  tables in the dataset
						String suffix = rowName.substring(rowName.indexOf('_')+1);
						templateName = suffix;
						if(rowName.indexOf('_') >= 0)
							hasTemplate = true;
						StringBuilder rowData = new StringBuilder();
						boolean hasPartitionedTable = false;
						for(String tableName : simpleTableList){
							if(tableName.startsWith(rowName)){
								hasPartitionedTable = true;
								break;
							}
						}
						if(hasPartitionedTable/*tableList.hasPartitionedTable(rowName)*/){
							/*System.out.print('\t');
						System.out.print(rowName + ": ");
						System.out.print(jdbcLinkObject.getJdbcUrl() + separator + jdbcLinkObject.getDatabaseName() +
								separator + jdbcLinkObject.getSchemaName() + separator + jdbcLinkObject.getUserName() +
								separator + jdbcLinkObject.getPassword() + separator);
							 */
							//TODO DB partition table
							if(this.dataLinkInfo.getDataLinkType().equals(DataLinkType.SOURCE) ||
									this.dataLinkInfo.getDataLinkType().equals(DataLinkType.TARGET))
								rowData.append(this.dataLinkInfo.getJdbcLinkObject().getConnectionBase() + separator + 
										this.dataLinkInfo.getJdbcLinkObject().getDatabaseName() +separator + 
										this.dataLinkInfo.getJdbcLinkObject().getSchemaName() + separator + 
										this.dataLinkInfo.getJdbcLinkObject().getUserName() +separator + 
										this.dataLinkInfo.getJdbcLinkObject().getPassword() + separator);
							else if(this.dataLinkInfo.getDataLinkType().equals(DataLinkType.URL))
								rowData.append(this.dataLinkInfo.getUrlLinkObject().getFullHost()+ separator + 
										this.dataLinkInfo.getUrlLinkObject().getPort() +separator + 
										this.dataLinkInfo.getUrlLinkObject().getPath() + separator + 
										"" + separator + "" + separator);
							if(rowName.indexOf('_') >= 0){
								//rowData.append(rowName + separator);//.substring(0,rowName.indexOf('_')) + separator);
								rowData.append(rowName + separator);
							} else {
								//rowData.append(templateName + separator);
								rowData.append(templateName + separator);
							}
							rowData.append("false" + separator);
							String displayName = root.getAttributeValue("displayName");
							for(String aliasName : oldPartitionAliases.keySet()){
								String value = oldPartitionAliases.get(aliasName).get(rowName);
								if(value==null){
									value="";
								}

								displayName = displayName.replace("*" + aliasName + "*", value);
							}
							rowData.append(displayName + separator);
							rowData.append("0.7" + separator);
							rowData.append(separator + separator + separator + separator + separator); // reserved for future use!

							for(String aliasName : oldPartitionAliases.keySet()){
								String value = oldPartitionAliases.get(aliasName).get(rowName);
								if(value==null){
									value="";
								}
								//System.out.print(value + separator);
								rowData.append(value + separator);
							}

							//System.out.println();
							rowList.add(rowData.toString());
						}
					}
				}

				Mart mart = new Mart(this.martRegistry,templateName,null);
				
				this.pointedDatasets.put(mart.getName(),new HashSet<String>());


				if(datasetMap!=null && datasetMap.get(xmlGZkey)!=null){
					mart.setDisplayName(this.datasetMap.get(xmlGZkey).getDisplayName());
					mart.setHideValue(!(this.datasetMap.get(xmlGZkey).isVisible()));
				}
				martList.add(mart);
				HashMap<String,DatasetTable> mainTableByKey = new HashMap<String, DatasetTable>();

				// If this is a webservice, populate the table lists
				if(isWebService){
					webTemplateName = root.getAttributeValue("dataset",templateName);
					//TODO Webservice
					//Iterator mainTables = root.getDescendants((new ElementFilter("MainTable")).or(new ElementFilter("Key")));
					Iterator mainTables = root.getDescendants(new ElementFilter("MainTable"));
					Iterator mainTableKeys = root.getDescendants(new ElementFilter("Key"));

					ArrayList<Element> mainTableList = new ArrayList<Element>();
					ArrayList<Element> mainTableKeyList = new ArrayList<Element>();
					while(mainTables.hasNext()){
						mainTableList.add((Element) mainTables.next());
					}
					while(mainTableKeys.hasNext()){
						mainTableKeyList.add((Element) mainTableKeys.next());
					}
					for(int i = 0; i < mainTableList.size(); ++i){
						DatasetTableType type = DatasetTableType.MAIN_SUBCLASS;
						if(i==0){
							type = DatasetTableType.MAIN;
						}
						DatasetTable table = mainTableByKey.get(mainTableKeyList.get(i).getValue());
						if(table==null){
							String partitionedName = null;
							if(mainTableList.get(i).getValue().split("__").length >= 3)
								partitionedName = "(p0c5)__" + mainTableList.get(i).getValue().split("__",2)[1];
							else
								partitionedName = mainTableList.get(i).getValue();
							table = new DatasetTable(mart, partitionedName,type);
							DatasetColumn column = new DatasetColumn(table, mainTableKeyList.get(i).getValue());
							column.addInPartitions(webTemplateName);
							table.addColumn(column);
							PrimaryKey pKey = new PrimaryKey(column);
							table.setPrimaryKey(pKey);
							if(i>0){	
								DatasetColumn keyColumn = table.getColumnByName(mainTableKeyList.get(i-1).getValue());
								if(keyColumn==null){
									keyColumn = new DatasetColumn(table, mainTableKeyList.get(i-1).getValue());
									table.addColumn(keyColumn);
									keyColumn.addInPartitions(webTemplateName);
								}
								ForeignKey fKey = table.getFirstForeignKey();
								if(fKey == null) {
									fKey = new ForeignKey(keyColumn);
								}
								table.addForeignKey(fKey);

								/*DatasetTable lastTable = mainTableByKey.get(mainTableKeyList.get(i-1).getValue());
								//DatasetColumn lastColumn = new DatasetColumn(lastTable, mainTableKeyList.get(i-1).getValue());
								PrimaryKey lastKey = lastTable.getPrimaryKey();
								if( (table.getType()==DatasetTableType.MAIN || table.getType()==DatasetTableType.MAIN_SUBCLASS) && (lastTable.getType()==DatasetTableType.MAIN || lastTable.getType()==DatasetTableType.MAIN_SUBCLASS) ){
									Relation relation = new Relation(lastKey, fKey, Cardinality.MANY_A);
								} else {
									Relation relation = new Relation(lastKey, pKey, Cardinality.MANY_A);
								}*/
								//lastKey.addRelation(relation);
								//pKey.addRelation(relation);
							}
							mainTableByKey.put(mainTableKeyList.get(i).getValue(), table);
							mart.addTable(table);
						}
					}
					Iterator allAttributes = root.getDescendants(new ElementFilter("AttributeDescription").or(new ElementFilter("FilterDescription").or(new ElementFilter("Option"))));
					while(allAttributes.hasNext()){
						Element attribute = (Element) allAttributes.next();
						String tableName = attribute.getAttributeValue("tableConstraint");
						String columnName = attribute.getAttributeValue("field");
						String keyName = attribute.getAttributeValue("key");
						if(columnName!=null && columnName.equals("orthologs_plant_genome_1_togo_bool")){
							Log.error("Foudn one");
						}
						if(tableName!=null && columnName!=null && keyName!=null){
							if(tableName.endsWith("__dm")){
								if(tableName.split("__").length < 3)
									tableName = webTemplateName + "__" + tableName;
								DatasetTable table = mart.getTableByName(tableName);
								if(table==null){
									table = new DatasetTable(mart, tableName, DatasetTableType.DIMENSION);
									mart.addTable(table);
								}
								table.addInPartitions(webTemplateName);
								DatasetColumn column = table.getColumnByName(columnName);
								if(column==null){
									column = new DatasetColumn(table, columnName);
									table.addColumn(column);
								}
								column.addInPartitions(webTemplateName);
								DatasetColumn keyColumn = table.getColumnByName(keyName);
								if(keyColumn == null){
									keyColumn = new DatasetColumn(table, keyName);
									table.addColumn(keyColumn);
								}
								keyColumn.addInPartitions(webTemplateName);
								ForeignKey fKey = table.getFirstForeignKey();
								if(fKey == null) {
									fKey = new ForeignKey(keyColumn);
								}
								table.addForeignKey(fKey);
								/*if(mainTableByKey.get(keyName)!=null) {
									PrimaryKey pk = mainTableByKey.get(keyName).getPrimaryKey();
									if(pk!=null && fKey!=null && !Relation.isRelationExist(pk, fKey)) {
										Relation relation = new Relation(pk, fKey, Cardinality.MANY_A);
										//fKey.addRelation(relation);
										//pk.addRelation(relation);
									}
								}*/
							} else if(tableName.equals("main") || tableName.endsWith("__main")){
								DatasetTable table = mainTableByKey.get(attribute.getAttributeValue("key"));
								if(table!=null){
									table.addInPartitions(webTemplateName);
									DatasetColumn column = table.getColumnByName(columnName);
									if(column==null){
										column = new DatasetColumn(table, columnName);
										table.addColumn(column);
									}
									column.addInPartitions(webTemplateName);
									mainTableByKey.put(attribute.getAttributeValue("key"), table);
								} else {
									//System.err.println("Problem finding main table with key: " + attribute.getAttributeValue("key"));
								}
							}
						}
					}
					for(int i = 1; i < mainTableList.size(); ++i){
						DatasetTable table = mainTableByKey.get(mainTableKeyList.get(i).getValue());
						if(table!=null){			
							DatasetTable lastTable = mainTableByKey.get(mainTableKeyList.get(i-1).getValue());
							for(Column lastColumn : lastTable.getColumnList()){
								table.addColumn(lastColumn);
							}
							mainTableByKey.put(mainTableKeyList.get(i).getValue(), table);
							mart.addTable(table);
						}
					}
				}
				// End webservice
				else {
					//TODO Database
					Iterator mainTables = root.getDescendants(new ElementFilter("MainTable"));
					Iterator mainTableKeys = root.getDescendants(new ElementFilter("Key"));

					ArrayList<Element> mainTableList = new ArrayList<Element>();
					ArrayList<Element> mainTableKeyList = new ArrayList<Element>();
					while(mainTables.hasNext()){
						mainTableList.add((Element) mainTables.next());
					}
					while(mainTableKeys.hasNext()){
						mainTableKeyList.add((Element) mainTableKeys.next());
					}
					for(int i = 0; i < mainTableList.size(); ++i){
						DatasetTableType type = DatasetTableType.MAIN_SUBCLASS;
						if(i==0){
							type = DatasetTableType.MAIN;
						}
						DatasetTable table = mainTableByKey.get(mainTableKeyList.get(i).getValue());
						if(table==null){
							String partitionedName = null;
							if(mainTableList.get(i).getValue().split("__").length >= 3)
								partitionedName = "(p0c5)__" + mainTableList.get(i).getValue().split("__",2)[1];
							else
								partitionedName = mainTableList.get(i).getValue();
							table = new DatasetTable(mart, partitionedName,type);
							DatasetColumn column = new DatasetColumn(table, mainTableKeyList.get(i).getValue());
							table.addColumn(column);
							PrimaryKey pKey = new PrimaryKey(column);
							table.setPrimaryKey(pKey);
							if(i>0){	
								DatasetColumn keyColumn = table.getColumnByName(mainTableKeyList.get(i-1).getValue());
								if(keyColumn==null){
									keyColumn = new DatasetColumn(table, mainTableKeyList.get(i-1).getValue());
									table.addColumn(keyColumn);
								}
								ForeignKey fKey = table.getFirstForeignKey();
								if(fKey == null) {
									fKey = new ForeignKey(keyColumn);
								}
								table.addForeignKey(fKey);

								/*DatasetTable lastTable = mainTableByKey.get(mainTableKeyList.get(i-1).getValue());
								//DatasetColumn lastColumn = new DatasetColumn(lastTable, mainTableKeyList.get(i-1).getValue());
								PrimaryKey lastKey = lastTable.getPrimaryKey();
								if( (table.getType()==DatasetTableType.MAIN || table.getType()==DatasetTableType.MAIN_SUBCLASS) && (lastTable.getType()==DatasetTableType.MAIN || lastTable.getType()==DatasetTableType.MAIN_SUBCLASS) ){
									Relation relation = new Relation(lastKey, fKey, Cardinality.MANY_A);
								} else {
									Relation relation = new Relation(lastKey, pKey, Cardinality.MANY_A);
								}*/
								//lastKey.addRelation(relation);
								//pKey.addRelation(relation);
							}
							mainTableByKey.put(mainTableKeyList.get(i).getValue(), table);
							mart.addTable(table);
							String[] splitName = mainTableList.get(i).getValue().split("__");
							table.addInPartitions(splitName[0]);
						}
					}
					for(String tableName : simpleTableList){
						if(tableName.endsWith("__dm")){
							String[] splitName = tableName.split("__");
							if(splitName.length == 3){
								String partitionedName = "(p0c5)__" + splitName[1] + "__dm";
								DatasetTable table = mart.getTableByName(partitionedName);
								if(table==null){
									table = new DatasetTable(mart, partitionedName, DatasetTableType.DIMENSION);
									mart.addTable(table);
								}
								table.addInPartitions(splitName[0]);
								//HashSet<String> currentColumns = queryDB("DESCRIBE "+ tableName);
								//BEGIN NEW
								HashSet<String> currentColumns = getColumns(tableName);
								//END NEW
								DatasetColumn keyColumn = null;
								for(String columnName : currentColumns){
									DatasetColumn column = table.getColumnByName(columnName);
									if(column==null){
										column = new DatasetColumn(table, columnName);
										table.addColumn(column);
									}
									column.addInPartitions(splitName[0]);
									if(columnName.endsWith("_key"))
										keyColumn = column;
								}
								ForeignKey fKey = table.getFirstForeignKey();
								if(fKey == null) {
									if(keyColumn==null)
										Log.error("No key: " + tableName);
									else
										fKey = new ForeignKey(keyColumn);
								}
								table.addForeignKey(fKey);
								/*if(mainTableByKey.get(keyColumn.getName())!=null) {
									PrimaryKey pk = mainTableByKey.get(keyColumn.getName()).getPrimaryKey();
									if(pk!=null && fKey!=null && !Relation.isRelationExist(pk, fKey)) {
										Relation relation = new Relation(pk, fKey, Cardinality.MANY_A);
										//fKey.addRelation(relation);
										//pk.addRelation(relation);
									}
								}*/
							} else {
								Log.error("Weird naming convention: " + tableName);
							}
						} else if(tableName.endsWith("__main")){
//							String[] splitName = tableName.split("__");
//							if(splitName.length == 3){
//								String partitionedName = "(p0c5)__" + splitName[1] + "__main";
//								DatasetTable table = mart.getTableByName(partitionedName);
//								mart.getM
//								if(table!=null){
//									table.addInPartitions(splitName[0]);
//									HashSet<String> currentColumns = queryDB("DESCRIBE "+ tableName);
//									DatasetColumn keyColumn = null;
//									for(String columnName : currentColumns){
//										DatasetColumn column = table.getColumnByName(columnName);
//										if(column==null){
//											column = new DatasetColumn(table, columnName);
//											table.addColumn(column);
//										}
//										column.addInPartitions(splitName[0]);
//										if(columnName.endsWith("_key"))
//											keyColumn = column;
//									}
//								}
//							}
						}
					}
					for(int i = 1; i < mainTableList.size(); ++i){
						DatasetTable table = mainTableByKey.get(mainTableKeyList.get(i).getValue());
						if(table!=null){			
							DatasetTable lastTable = mainTableByKey.get(mainTableKeyList.get(i-1).getValue());
							for(Column lastColumn : lastTable.getColumnList()){
								table.addColumn(lastColumn);
							}
							mainTableByKey.put(mainTableKeyList.get(i).getValue(), table);
							mart.addTable(table);
						}
					}
				}

				// Populate main partition table object
				if(rowList.size()>0){
					PartitionTable partitionTable = new PartitionTable(mart, PartitionType.SCHEMA);
					for(String row:rowList){
						partitionTable.addNewRow(row);
					}
					mart.addPartitionTable(partitionTable);
				} else {
					//System.err.println("No partition table! " + templateName);
					//TODO webservice partition table
					PartitionTable partitionTable = new PartitionTable(mart, PartitionType.SCHEMA);
					if(this.dataLinkInfo.getDataLinkType().equals(DataLinkType.SOURCE) ||
							this.dataLinkInfo.getDataLinkType().equals(DataLinkType.TARGET))					
						partitionTable.addNewRow(this.dataLinkInfo.getJdbcLinkObject().getConnectionBase() + separator + 
								this.dataLinkInfo.getJdbcLinkObject().getDatabaseName() +separator + 
								this.dataLinkInfo.getJdbcLinkObject().getSchemaName() + separator + 
								this.dataLinkInfo.getJdbcLinkObject().getUserName() +separator + 
								this.dataLinkInfo.getJdbcLinkObject().getPassword() + separator + 
								templateName + separator + 
								"false" + separator +
								root.getAttributeValue("displayName") + separator + "0.7" + separator);
					else if(this.dataLinkInfo.getDataLinkType().equals(DataLinkType.URL))
						partitionTable.addNewRow(this.dataLinkInfo.getUrlLinkObject().getFullHost() + separator + 
								this.dataLinkInfo.getUrlLinkObject().getPort() +separator + 
								this.dataLinkInfo.getUrlLinkObject().getPath() + separator + 
								"" +separator + ""+ separator + 
								templateName + separator+ 
								"false" + separator +
								root.getAttributeValue("displayName") + separator + "0.7" + separator);

					mart.addPartitionTable(partitionTable);
				}
				// Now: figure out which tables exist in the dataset, and get their information
				// This is to create the dm partition tables
				// HashMap<String,HashSet<String>> dmPartitionColumns = new HashMap<String, HashSet<String>>();
				// a DM partition table is uniquely defined by the dmPartitionTable and the key name : hence Map of a Map of a Set
				//HashMap<String,HashMap<String,HashSet<String>>> dmPartitionColumns = new HashMap<String, HashMap<String,HashSet<String>>>();
				//HashMap<String,HashSet<TableName>> tablesInDMPartiton = new HashMap<String,HashSet<TableName>>();

				// For main tables, we have a list of columns for each, plus an ordering based on number of keys
				

				//Integer numberOfColsP0 = 1;
				String mainTableName = "";
				if(mart.getPartitionTableByName("p0")!=null && mart.getPartitionTableByName("p0").getTotalRows() > 0){
					//Integer numberOfColsP0 = (mart.getPartitionTableByName("p0").getTotalColumns()-1);
					//mainTableName = "(p0c" + numberOfColsP0.toString() + ")";
					mainTableName = "(p0c5)";
				}
				//HashMap<String,PartitionTable> partitionByTable = new HashMap<String,PartitionTable>();
				/*for(String tableName : dmPartitionColumns.keySet()){
					for(String keyName : dmPartitionColumns.get(tableName).keySet()){
						PartitionTable dmPartition = new PartitionTable(mart, PartitionType.DIMENSION);
						for(TableName table : tablesInDMPartiton.get(tableName + keyName)){
							if(table.getDmPartitionName()!=null)
								dmPartition.addNewRow(table.getFullMainName() + separator + table.getDmPartitionName());
						}
						if(dmPartition.getRowNamesList().size() > 0 && isDMPartitioned){
							mart.addPartitionTable(dmPartition);
							//System.err.println(tableName +":");
							String name;
							if(mainTableName.equals("")){
								name = templateName + "__" 
								+ tableName + "_(" + dmPartition.getName() + "c1)" + "__dm";
							} else {
								name = "(" + dmPartition.getName() + "c0)" + "__" 
								+ tableName + "_(" + dmPartition.getName() + "c1)" + "__dm";
							}
							DatasetTable table = new DatasetTable(mart, name , DatasetTableType.DIMENSION);
							DatasetColumn keyColumn= new DatasetColumn(table, keyName);
							ForeignKey fKey = new ForeignKey(keyColumn);
							table.addForeignKey(fKey);
							//System.err.print('\t');
							for(String columnName : dmPartitionColumns.get(tableName).get(keyName)){
								//System.err.print(columnName + ", ");
								DatasetColumn column = new DatasetColumn(table, columnName);
								table.addColumn(column);
							}
							//System.err.println("");
							mart.addTable(table);
							//System.err.println(tableName + " " + keyName + ": " + dmPartition.getName());
							partitionByTable.put(tableName + keyName,dmPartition);
						} else { // This table isn't part of a dmPartition
							String name = mainTableName + "__" 
							+ tableName + "__dm";

							DatasetTable table = new DatasetTable(mart, name , DatasetTableType.DIMENSION);
							DatasetColumn keyColumn= new DatasetColumn(table, keyName);
							ForeignKey fKey = new ForeignKey(keyColumn);
							table.addForeignKey(fKey);
							//System.err.print('\t');
							for(String columnName : dmPartitionColumns.get(tableName).get(keyName)){
								//System.err.print(columnName + ", ");
								DatasetColumn column = new DatasetColumn(table, columnName);
								table.addColumn(column);
							}
							//System.err.println("");
							mart.addTable(table);
							//System.err.println(tableName + " " + keyName + ": " + dmPartition.getName());
							partitionByTable.put(tableName + keyName,dmPartition);
						}
					}
				}*/

				HashMap<String, HashMap<String, HashSet<String>>> mainPartitionColumns = new HashMap<String, HashMap<String, HashSet<String>>>();
				TreeMap<Integer,String> mainTableOrder = new TreeMap<Integer, String>();
				for(String table : simpleTableList){
					if(hasTemplate && !table.split("__")[0].endsWith("_"+templateName))
						continue;
					if(table.endsWith("__main")){
						HashSet<String> columns  = getColumns(table);
						// Find keys
						HashSet<String> keySet = new HashSet<String>();
						for(String columnName : columns){
							if(columnName.endsWith("_key")){
								keySet.add(columnName);
							}
						}
						HashMap<String,HashSet<String>> existingTables = mainPartitionColumns.get(table.split("__", -1)[1]);
						
						if(existingTables == null){
							existingTables = new HashMap<String, HashSet<String>>();
						}
						existingTables.put(table, columns);
						mainPartitionColumns.put(table.split("__", -1)[1], existingTables);
						
						mainTableOrder.put(keySet.size(), table.split("__", -1)[1]);
					}
				}
				
				HashSet<String> usedKeys = new HashSet<String>();
				String lastKey = null;
				PrimaryKey lastPrimaryKey = null;
				for(Integer order : mainTableOrder.keySet()){
					String tableName = mainTableOrder.get(order);
					HashMap<String, HashSet<String>> columnsMap = mainPartitionColumns.get(tableName);
					HashSet<String> keySet = new HashSet<String>();

					// If this is not the first main table (i.e. the main table with only one key), it is a submain
					DatasetTableType type = DatasetTableType.MAIN_SUBCLASS;
					if(order==1){
						type = DatasetTableType.MAIN;
					}

					// Find all the keys
					for(String partitionedTable: columnsMap.keySet()){
						HashSet<String> columns = columnsMap.get(partitionedTable);
						for(String columnName : columns){
							if(columnName.endsWith("_key")){
								keySet.add(columnName);
							}
						}
					}

					// Remove the primary keys from main tables earlier in the hierarchy
					keySet.removeAll(usedKeys);
					// Add this table's primary key to the set of used keys, for the next iteration of the loop
					usedKeys.addAll(keySet);
					// Get the primary key for this table; keySet now only has one member, but an iterator is the only way I know to retrieve it
					String primaryKey = null;
					for(String key:keySet){
						primaryKey = key;
					}
					// Remove both the primary key and the last table's primary key (lastKey) from the set of columns
					//columns.remove(primaryKey);
					//columns.remove(lastKey);

					// Set up the table object
					DatasetTable table = new DatasetTable(mart, mainTableName + "__" + tableName + "__main", type);
					DatasetColumn primaryKeyColumn = new DatasetColumn(table, primaryKey);
					PrimaryKey pKey = new PrimaryKey(primaryKeyColumn);
					table.setPrimaryKey(pKey);
					// If this isn't the first table, it's a submain, and therefore has a foreign key pointing to the previous main
					if(order > 1){
						DatasetColumn foreignKeyColumn = new DatasetColumn(table, lastKey);
						ForeignKey fKey = table.getFirstForeignKey();
						if(fKey == null) {
							fKey = new ForeignKey(foreignKeyColumn);
						}
						table.addForeignKey(fKey);
						if(!Relation.isRelationExist(lastPrimaryKey, fKey))
							new RelationTarget(lastPrimaryKey, fKey, Cardinality.MANY_A);
						//lastPrimaryKey.addRelation(relation);
						//fKey.addRelation(relation);
					}
					mainTableByKey.put(primaryKey,table);
					// Set the last key to this columns key, for the next iteration of the loop
					lastKey = primaryKey;
					lastPrimaryKey = pKey;

					// Add all the columns to the table object
					for(String partitionedTable: columnsMap.keySet()){
						HashSet<String> columns = columnsMap.get(partitionedTable);
						for(String columnName : columns){
							DatasetColumn column = table.getColumnByName(columnName);
							if (column==null)
								column = new DatasetColumn(table, columnName);
							column.addInPartitions(partitionedTable.split("__")[0]);
							table.addColumn(column);
						}
					}

					// Figure out all the relations pointing to this main table
					for(DatasetTable dmTable : mart.getDatasetTables()){
						if(dmTable.getForeignKeys().size() >0){
							ForeignKey fKey = dmTable.getFirstForeignKey();
							if(fKey ==null)
								fKey = dmTable.getForeignKeys().iterator().next();
							if(fKey.equalsByName(pKey)){
								if(!Relation.isRelationExist(pKey, fKey))
									new RelationTarget(pKey, fKey, Cardinality.MANY_A);
								//pKey.addRelation(relation);
								//fKey.addRelation(relation);
							}
						}
					}

					// Add the table to the mart object
					mart.addTable(table);
					for(String pName : mart.getSchemaPartitionTable().getCol(PartitionUtils.DATASETNAME)){
						table.addInPartitions(pName);
					}
				}
				Config config = new Config(templateName);
				mart.addConfig(config);
				String configHide = root.getAttributeValue("visible","1");
				if (configHide.equals("0"))
					config.setHideValue(true);
				config.setProperty(XMLElements.METAINFO, "");
				config.setProperty(XMLElements.DATASETDISPLAYNAME,"(p0c7)");
				//config.setName("(p0c5)");
				config.setProperty(XMLElements.DATASETHIDEVALUE,"(p0c6)");
				Container rootContainer = config.getRootContainer();
				config.addRootContainer(rootContainer);

				Element dataRoot = new Element("config");
				dataRoot.setAttribute(new Attribute("name", templateName));
				Element martOptions = new Element("mart");
				martOptions.setAttribute(new Attribute("name", templateName));
				martOptions.addContent(dataRoot);

				//Document xmlOptions = new Document(dataRoot);

				//				//TODO Process all attributes to create DM partition tables
				//				Iterator allAttributes = root.getDescendants(new ElementFilter("AttributeDescription").or(new ElementFilter("Option")));
				//				//HashMap<String,HashMap<String,HashSet<String>>> partitionAttributeList = new HashMap<String, HashMap<String,HashSet<String>>>();
				//				//Partition Table name -> FieldName -> column number
				//				//HashMap<String,HashMap<String,HashMap<String,Integer>>> locateAttributeInPartitionTable = new HashMap<String, HashMap<String,HashMap<String,Integer>>>();
				//				HashMap<String, HashMap<String,Integer>> attributeInPartitionTable = new HashMap<String, HashMap<String,Integer>>();
				//				if(!isWebService && isDMPartitioned){
				//					while(allAttributes.hasNext()){
				//						Element oldAttribute = (Element) allAttributes.next();
				//						if(oldAttribute.getName().equals("AttributeDescription") || ((Element) oldAttribute.getParent()).getName().equals("FilterDescription")){
				//							String tableName = oldAttribute.getAttributeValue("tableConstraint");
				//							String hidden = oldAttribute.getAttributeValue("hidden","false");
				//							String field = oldAttribute.getAttributeValue("field");
				//
				//							if(oldAttribute.getAttributeValue("pointerAttribute")==null && hidden.equals("false")){
				//								if(tableName!=null && !(tableName.equalsIgnoreCase("main"))){
				//									if(tableName.split("__").length > 3){
				//										//System.err.println("Illegal tableConstraint! Too many double underscores! " + tableName);
				//									}
				//									int whichSection = 0;
				//									if(tableName.split("__").length == 3)
				//										whichSection = 1;
				//									String[] splitName = tableName.split("__")[whichSection].split("_",2);
				//
				//									if(splitName.length > 1 && !isWebService && isDMPartitioned){
				//										//This table that this attribute refers to is part of a dm partition
				//										PartitionTable currentPartition = partitionByTable.get(splitName[0] + oldAttribute.getAttributeValue("key"));
				//										//System.err.println(tableName + ": ");//+ splitName[0] +", " + attribute.getAttributeValue("key") );
				//										//System.err.println("\t" + currentPartition.getName());
				//										if(currentPartition!=null){
				//											HashMap<String,Integer> columnsByFieldName = attributeInPartitionTable.get(currentPartition.getName() + tableName + field);
				//											if(columnsByFieldName==null){
				//												columnsByFieldName = new HashMap<String, Integer>();
				//											}
				//											for(Object tempObject : oldAttribute.getAttributes()){
				//												Attribute xmlAttribute = (Attribute) tempObject;
				//												String propertyName = xmlAttribute.getName();
				//												if(columnsByFieldName.get(propertyName)==null)
				//													columnsByFieldName.put(propertyName, currentPartition.addColumn(""));
				//
				//												currentPartition.setColumnByColumn(1, splitName[1], columnsByFieldName.get(propertyName), xmlAttribute.getValue());
				//											}
				//											if(oldAttribute.getChildren("SpecificAttributeContent").size()>0){
				//												if(columnsByFieldName.get("hidden")==null)
				//													columnsByFieldName.put("hidden", currentPartition.addColumn("Xtrue"));
				//												currentPartition.setColumnValue(columnsByFieldName.get("hidden"), "Xtrue");
				//											}
				//											attributeInPartitionTable.put(currentPartition.getName() +tableName + field,columnsByFieldName);
				//
				//											// Now, for each partition table, we have a map of which xmlAttribute names have more than one value,
				//											//  which we can use later to determine whether it needs to be added to the partition table or not.
				//										} else {
				//											//System.err.println("Partition for " + tableName + " doesn't appear to exist!");
				//										}
				//									}
				//								}
				//							}
				//						}
				//					}
				//				}


				List attributePages = root.getChildren("AttributePage");
				Container tabContainer = null;
				if(attributePages.size()>1){
					tabContainer = new Container("ATTRIBUTES");
					rootContainer.addContainer(tabContainer);
					tabContainer.setProperty(XMLElements.MAXCONTAINERS, "1");
				}
				Iterator attributePageIterator = attributePages.iterator();

				HashMap<String, org.biomart.objects.objects.Attribute> fieldTableKeyToAttribute = new HashMap<String, org.biomart.objects.objects.Attribute>();
				while (attributePageIterator.hasNext()) {
					Element attributePage = (Element) attributePageIterator.next();
					//String internalName = attributePage.getAttributeValue("internalName").toLowerCase();
					if(attributePage.getAttributeValue("hidden","false").equals("false")){
						Container attributePageContainer = populateContainer(attributePage);
						if(tabContainer!=null)
							tabContainer.addContainer(attributePageContainer);
						else
							rootContainer.addContainer(attributePageContainer);
						
						List attributeGroups = attributePage.getChildren("AttributeGroup");
						Iterator attributeGroupIterator = attributeGroups.iterator();
						while (attributeGroupIterator.hasNext()){
							Element attributeGroup = (Element) attributeGroupIterator.next();
							if(attributeGroup.getAttributeValue("hidden","false").equals("false")){

								Container attributeGroupContainer = populateContainer(attributeGroup);
								attributePageContainer.addContainer(attributeGroupContainer);
								List attributeCollections = attributeGroup.getChildren("AttributeCollection");
								Iterator attributeCollectionIterator = attributeCollections.iterator();
								while (attributeCollectionIterator.hasNext()){
									Element attributeCollection = (Element) attributeCollectionIterator.next();
									if(attributeCollection.getAttributeValue("hidden","false").equals("false")){

										Container attributeCollectionContainer = populateContainer(attributeCollection);
										attributeGroupContainer.addContainer(attributeCollectionContainer);
										List attributeDescriptions = attributeCollection.getChildren("AttributeDescription");
										Iterator attributeDescriptionIterator = attributeDescriptions.iterator();
										while(attributeDescriptionIterator.hasNext()){
											Element attributeDescription = (Element) attributeDescriptionIterator.next();
											if(attributeDescription.getAttributeValue("hidden","false").equals("false")){

												if(attributeDescription.getAttributeValue("pointerAttribute")==null){
													String tableName = attributeDescription.getAttributeValue("tableConstraint");
													String keyName = attributeDescription.getAttributeValue("key");
													String fieldName = attributeDescription.getAttributeValue("field");

													if(tableName!=null && !(tableName.equalsIgnoreCase("main") || tableName.endsWith("__main"))){
														// Attribute is not a member of a DM partition, because it's "_" split only has one section
														//String currentTableName = mainTableName + templateName + "__" + splitName[0] + "__dm";
														String currentTableName = tableName;
														DatasetTable currentTable = mart.getTableByName(currentTableName);
														if(currentTable==null){
															Log.error("Can't create attribute, because table " + currentTableName + " not found; trying " + mart.getName() + "__" + currentTableName);
															currentTable = mart.getTableByName("(p0c5)__" + currentTableName);
														}
														if(currentTable==null && currentTableName.split("__",2).length>1){
															currentTable = mart.getTableByName("(p0c5)__" + currentTableName.split("__",2)[1]);
														}
														if(currentTable==null){
															Log.error("Nope, still missing");
														} else {
															DatasetColumn currentColumn = currentTable.getColumnByName(fieldName);
															if(currentColumn!=null){
																org.biomart.objects.objects.Attribute attribute = new org.biomart.objects.objects.Attribute(currentColumn, attributeDescription.getAttributeValue("internalName").toLowerCase());
																attributeCollectionContainer.addAttribute(attribute);

																attribute.setHideValue(attributeDescription.getAttributeValue("hideDisplay","false").equals("true"));
																if(attributeDescription.getAttributeValue("displayName")!=null){
																	attribute.setDisplayName(attributeDescription.getAttributeValue("displayName"));
																}
																attribute.setDescription(attributeDescription.getAttributeValue("description",""));
																attribute.setLinkOutUrl(processLinkoutURL(attributeDescription, config));

																attribute.setName(attribute.getInternalName());

																fieldTableKeyToAttribute.put(fieldName + tableName + keyName, attribute);
															}
														}

													} else if (tableName!=null && (tableName.equalsIgnoreCase("main") || tableName.endsWith("__main"))){
														DatasetTable currentTable= mainTableByKey.get(attributeDescription.getAttributeValue("key"));
														if (currentTable != null) {
															DatasetColumn currentColumn = currentTable.getColumnByName(fieldName);
															if (currentColumn == null) {
																//System.err.println("Can't create attribute, because column " + attributeDescription.getAttributeValue("field") + " wasn't found in table " + currentTable.getName());
															} else {
																org.biomart.objects.objects.Attribute attribute = new org.biomart.objects.objects.Attribute(currentColumn,attributeDescription.getAttributeValue("internalName","").toLowerCase());
																attributeCollectionContainer.addAttribute(attribute);

																attribute.setHideValue(attributeDescription.getAttributeValue("hideDisplay","false").equals("true"));
																if(attributeDescription.getAttributeValue("displayName")!=null){
																	attribute.setDisplayName(attributeDescription.getAttributeValue("displayName"));
																}
																attribute.setDescription(attributeDescription.getAttributeValue("description",""));
																attribute.setLinkOutUrl(processLinkoutURL(attributeDescription, config));

																attribute.setName(attribute.getInternalName());

																fieldTableKeyToAttribute.put(fieldName + tableName + keyName, attribute);
															}
														} else {
															//System.err.println(mart.getName() + ": ENTERED MAIN ATTRIBUTE " + attributeDescription.getAttributeValue("internalName").toLowerCase() + " " + attributeDescription.getAttributeValue("key"));
															//System.err.println("Main table reference with non-existing key.");
														}
													}
												} else { // It's a pointer attribute
													//TODO Pointer Attribute code
												
													//if(!(attributeDescription.getAttributeValue("pointerDataset","").endsWith(templateName))){ // It's a non-local pointer
													if(isRemotePointer(mart.getPartitionTableByName("p0"), internalNames, attributeDescription.getAttributeValue("pointerDataset",""), oldPartitionToColumn)){
														String[] pointerDataset = attributeDescription.getAttributeValue("pointerDataset").split("\\*",-1);
														if(pointerDataset.length <= 3){
															org.biomart.objects.objects.Attribute pointerAttribute = new org.biomart.objects.objects.Attribute(attributeDescription.getAttributeValue("internalName").toLowerCase(),attributeDescription.getAttributeValue("pointerAttribute"),replaceAliases(attributeDescription.getAttributeValue("pointerDataset"),oldPartitionToColumn));
															attributeCollectionContainer.addAttribute(pointerAttribute);

															this.pointedDatasets.get(mart.getName()).add(pointerAttribute.getPointedDatasetName());
															//pointerAttribute.setInternalName(attributeDescription.getAttributeValue("pointerAttribute"));
															//pointerAttribute.setConfigName("POINTER");
															//pointerAttribute.setName(pointerAttribute.getInternalName());
															pointerAttribute.setLinkOutUrl(processLinkoutURL(attributeDescription, config));

														} else if (pointerDataset.length > 3){
															Log.error("Too many aliases in pointerDataset property!");
														} else {
															PartitionTable mainPartition = mart.getPartitionTableByName("p0");
															HashMap<String, String> internalNameToValue = oldPartitionAliases.get(pointerDataset[1]);
															for(String internalName : internalNameToValue.keySet()){
																org.biomart.objects.objects.Attribute pointerAttribute = new org.biomart.objects.objects.Attribute(attributeDescription.getAttributeValue("internalName").toLowerCase(),attributeDescription.getAttributeValue("pointerAttribute"), pointerDataset[0] + internalNameToValue.get(internalName) + pointerDataset[2]);
																attributeCollectionContainer.addAttribute(pointerAttribute);
																this.pointedDatasets.get(mart.getName()).add(pointerAttribute.getPointedDatasetName());
																//pointerAttribute.setInternalName(attributeDescription.getAttributeValue("pointerAttribute"));
																//pointerAttribute.setConfigName("POINTER");
																//pointerAttribute.setName(pointerAttribute.getInternalName());
																pointerAttribute.setLinkOutUrl(processLinkoutURL(attributeDescription, config));

																int hideColumn = mainPartition.addColumn("true");
																int row = mainPartition.getRowNumberByDatasetName(internalName);
																if(row >= 0)
																	mainPartition.updateValue(row, hideColumn, "false");

																pointerAttribute.setHideValueInString("(p0c" + Integer.toString(hideColumn) + ")");

															}
														}
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
				List filterPages = root.getChildren("FilterPage");
				Iterator filterPageIterator = filterPages.iterator();
				while (filterPageIterator.hasNext()) {
					Element filterPage = (Element) filterPageIterator.next();
					//String internalName = filterPage.getfilterValue("internalName");
					if(filterPage.getAttributeValue("hidden","false").equals("false")){
						Container filterPageContainer = populateContainer(filterPage);
						rootContainer.addContainer(filterPageContainer);
						List filterGroups = filterPage.getChildren("FilterGroup");
						Iterator filterGroupIterator = filterGroups.iterator();
						while (filterGroupIterator.hasNext()){
							Element filterGroup = (Element) filterGroupIterator.next();
							if(filterGroup.getAttributeValue("hidden","false").equals("false")){
								Container filterGroupContainer = populateContainer(filterGroup);
								filterPageContainer.addContainer(filterGroupContainer);
								List filterCollections = filterGroup.getChildren("FilterCollection");
								Iterator filterCollectionIterator = filterCollections.iterator();
								while (filterCollectionIterator.hasNext()){
									Element filterCollection = (Element) filterCollectionIterator.next();
									if(filterCollection.getAttributeValue("hidden","false").equals("false")){
										
										//Container filterCollectionContainer = populateContainer(filterCollection);
										List filterDescriptions = filterCollection.getChildren("FilterDescription");
										
										String filterCollectionName = filterCollection.getAttributeValue("displayName", "ERROR");
										String filterCollectionPrefix = filterCollectionName + " - ";
										
										Iterator filterDescriptionIterator = filterDescriptions.iterator();
										while(filterDescriptionIterator.hasNext()){
											Element filterDescription = (Element) filterDescriptionIterator.next();
											if(filterDescription.getAttributeValue("internalName","").equals("plant_genome_1_togo_bool"))
												Log.error("Found");
											if(filterDescription.getAttributeValue("hidden","false").equals("false") && filterDescription.getAttributeValue("pointerFilter")==null){
												String displayType = filterDescription.getAttributeValue("displayType");
												if(displayType==null){
													Log.error("Filter "+ filterDescription.getAttributeValue("internalName") + " is missing a displayType");
												
												} else {
													if(displayType.equals("container")){
														FilterType containerType;
														if(filterDescription.getAttributeValue("type","").startsWith("boolean")){
															containerType = FilterType.SINGLESELECTBOOLEAN;
														} else {
															containerType = FilterType.SINGLESELECTUPLOAD;
														}
														Filter filterList = new Filter(containerType, filterDescription.getAttributeValue("internalName").toLowerCase());
														if(filterDescription.getAttributeValue("displayName")!=null){
															filterList.setDisplayName((filterDescriptions.size() == 1) ? filterCollectionName : filterCollectionPrefix +filterDescription.getAttributeValue("displayName"));
														} else if (filterCollection.getChildren().size() == 1 && filterCollection.getAttributeValue("displayName")!=null ){
															filterList.setDisplayName((filterDescriptions.size() == 1) ? filterCollectionName : filterCollectionPrefix +filterCollection.getAttributeValue("displayName"));
														}
														filterGroupContainer.addFilter(filterList);

														//filterList.setName(filterDescription.getAttributeValue("internalName").toLowerCase());
														Container filterListContainer = new Container(filterDescription.getAttributeValue("internalName").toLowerCase() + "_listContainer");
														filterPageContainer.addContainer(filterListContainer);
														filterListContainer.setHideValue(true);
														List filterListFilters = filterDescription.getChildren("Option");
														Iterator filterListFiltersIterator = filterListFilters.iterator();
														while(filterListFiltersIterator.hasNext()){
															Element filterListFilter = (Element) filterListFiltersIterator.next();
														//	if(filterListFilter.getAttributeValue("internalName","").equals("uniprot_swissprot"))
														//		Log.error("Got one");
															if(filterListFilter.getAttributeValue("hidden","false").equals("false")){
																String subDisplayType = filterListFilter.getAttributeValue("displayType","text");

																if(subDisplayType==null){
																	//System.err.println("FilterList: No display type in old XML!");
																}

																String fieldName = filterListFilter.getAttributeValue("field");
																String tableName = filterListFilter.getAttributeValue("tableConstraint");
																String keyName = filterListFilter.getAttributeValue("key");
																String multipleValues = filterListFilter.getAttributeValue("multipleValues");
																String style = filterListFilter.getAttributeValue("style");
																FilterType newType = null;
																if(fieldName!=null){
																	if (subDisplayType.equals("text")){
																		if(multipleValues!=null && multipleValues.equals("1")){
																			newType = FilterType.UPLOAD;
																		} else {
																			newType = FilterType.TEXT;
																		}
																	} else if (subDisplayType.equals("list")){
																		if(style.equals("radio")){
																			newType = FilterType.BOOLEAN;
																		} else if (style.equals("menu")){
																			if(multipleValues!=null && multipleValues.equals("1")){
																				newType = FilterType.MULTISELECT;
																			} else {
																				newType = FilterType.SINGLESELECT;
																			}
																		}
																	}
																	if (newType == null){
																		Log.error("FilterList: Invalid type in old XML!");
																	}

																	org.biomart.objects.objects.Attribute attribute = fieldTableKeyToAttribute.get(fieldName+tableName+keyName);
														
																	if (attribute == null)
																		attribute = fieldTableKeyToAttribute.get(fieldName+templateName+"__"+tableName+keyName);
																	if (attribute == null)
																		attribute = config.getAttributeByName(filterListFilter.getAttributeValue("internalName").toLowerCase(), null);
																	
																	if(attribute==null) { // The attribute for this filter doesn't exist, so we need to create it and hide it
																		//System.err.println("FilterListOption: no attribute for " + fieldName + " " + tableName + " " + keyName);
																		if(tableName!=null && !(tableName.equalsIgnoreCase("main") || tableName.endsWith("__main"))){
																			// Attribute is not a member of a DM partition, because it's "_" split only has one section
																			//String currentTableName = mainTableName + templateName + "__" + splitName[0] + "__dm";
																			String currentTableName = tableName;
																			DatasetTable currentTable = mart.getTableByName(currentTableName);
																			// If the table doesn't exist, try adding the prefix for the webservice (UGLY)
																			if(currentTable==null){
																				currentTable = mart.getTableByName(webTemplateName + "__" + currentTableName);
																			}
																			if(currentTable==null){
																				Log.error("Can't create attribute, because table " + currentTableName + " not found; trying " + mart.getName() + "__" + currentTableName);
																				currentTable = mart.getTableByName("(p0c5)__" + currentTableName);
																			}
																			if(currentTable==null && currentTableName.split("__",2).length>1){
																				currentTable = mart.getTableByName("(p0c5)__" + currentTableName.split("__",2)[1]);
																			}
																			if(currentTable==null){
																				Log.error("Nope, still missing");
																			}else {
																				DatasetColumn currentColumn = currentTable.getColumnByName(fieldName);
																				if(currentColumn==null){
																					//System.err.println("*" + mart.getName() + " " + filterListFilter.getAttributeValue("internalName").toLowerCase() + " FILTERLIST Can't create attribute, because column " + fieldName + " in table " + currentTableName + " not found");
																				} else {
																					org.biomart.objects.objects.Attribute hiddenAttribute = new org.biomart.objects.objects.Attribute(currentColumn, filterListFilter.getAttributeValue("internalName").toLowerCase());
																					hiddenAttribute.setHideValue(true);
																					filterListContainer.addAttribute(hiddenAttribute);
																					fieldTableKeyToAttribute.put(fieldName + tableName + keyName, hiddenAttribute);
																				
																					if(filterListFilter.getAttributeValue("displayName")!=null){
																						hiddenAttribute.setDisplayName(filterListFilter.getAttributeValue("displayName"));
																					}
																					hiddenAttribute.setDescription(filterListFilter.getAttributeValue("description",""));

																					hiddenAttribute.setName(hiddenAttribute.getInternalName());

																					attribute = hiddenAttribute;
																				}
																			}

																		} else if (tableName!=null && (tableName.equalsIgnoreCase("main") || tableName.endsWith("__main"))){
																			DatasetTable currentTable= mainTableByKey.get(filterListFilter.getAttributeValue("key"));

																			if (currentTable != null) {
																				DatasetColumn currentColumn = currentTable.getColumnByName(fieldName);
																				if (currentColumn == null) {
																					//System.err.println("MAIN FILTERLIST Can't create attribute, because column " + filterListFilter.getAttributeValue("field") + " wasn't found in table " + currentTable.getName());
																				} else {
																					org.biomart.objects.objects.Attribute hiddenAttribute = new org.biomart.objects.objects.Attribute(currentColumn,filterListFilter.getAttributeValue("internalName").toLowerCase());

																					filterListContainer.addAttribute(hiddenAttribute);
																					fieldTableKeyToAttribute.put(fieldName + tableName + keyName, attribute);	hiddenAttribute.setHideValue(true);
																					if(filterListFilter.getAttributeValue("displayName")!=null){
																						hiddenAttribute.setDisplayName(filterListFilter.getAttributeValue("displayName"));
																					}
																					hiddenAttribute.setName(hiddenAttribute.getInternalName());

																					hiddenAttribute.setDescription(filterListFilter.getAttributeValue("description",""));


																					attribute = hiddenAttribute;
																				}
																			} else {
																				//System.err.println("MAIN FILTERLIST Main table reference with non-existing key.");
																			}
																		}

																	}

																	if(attribute!=null){									
																		Filter filter = new Filter(attribute,filterListFilter.getAttributeValue("internalName").toLowerCase());
																		if (attribute.getName().split("__").length > 1){
																			//filter = new Filter(attribute,attribute.getName().split("__")[0] + "__" + filterListFilter.getAttributeValue("internalName").toLowerCase());
																			filter = new Filter(attribute, filterListFilter.getAttributeValue("internalName").toLowerCase());

																		}
																		filterList.addFilter(filter);


																		if(filterListFilter.getAttributeValue("displayName")!=null){
																			filter.setDisplayName(filterListFilter.getAttributeValue("displayName"));
																		}
																		// Need to set filter's hidden status to be the same as the attached attribute
																		//filter.setHideValueInString(attribute.getHideString());
																		filter.setFilterType(newType);
																		if(newType==FilterType.BOOLEAN){
																			filter.setOnlyValue("Only");
																			filter.setExcludedValue("Excluded");
																		}
																		filterListContainer.addFilter(filter);

																		String legalQualifiers = filterListFilter.getAttributeValue("legal_qualifiers");
																		if(legalQualifiers!=null){
																			if(legalQualifiers.contains(">=")){
																				filter.setQualifier(OperatorType.GTE);
																			} else if (legalQualifiers.contains("<=")){
																				filter.setQualifier(OperatorType.LTE);
																			} else if (legalQualifiers.contains(">")){
																				filter.setQualifier(OperatorType.GT);
																			} else if (legalQualifiers.contains("<")){
																				filter.setQualifier(OperatorType.LT);
																			} else if (legalQualifiers.contains("in") || legalQualifiers.contains("=")){
																				filter.setQualifier(OperatorType.E);
																			} else if (legalQualifiers.contains("like")){
																				filter.setQualifier(OperatorType.LIKE);
																			} else {
																				filter.setQualifier(OperatorType.E);
																			}
																		}
																		filter.setDescription(filterListFilter.getAttributeValue("description",""));


																		parseSpecificFilterContent(dataRoot, filterListFilter, filter, "SpecificOptionContent");
																		if(newType!=FilterType.BOOLEAN){
																			parseFilterOptions(dataRoot,filterListFilter,filter);
																		}

																	}
																}
															}
														}
														//filterCollectionContainer.addContainer(filterListContainer);
														//filterCollectionContainer.addFilter(filterList);
														//filterGroupContainer.addContainer(filterListContainer);
													} else { // Not a container, therefore a normal filter
														String fieldName = filterDescription.getAttributeValue("field");
														String tableName = filterDescription.getAttributeValue("tableConstraint");
														String keyName = filterDescription.getAttributeValue("key");
														String multipleValues = filterDescription.getAttributeValue("multipleValues");
														String style = filterDescription.getAttributeValue("style","");
														FilterType newType = null;
														if (displayType.equals("text")){
															if(multipleValues!=null && multipleValues.equals("1")){
																newType = FilterType.UPLOAD;
															} else {
																newType = FilterType.TEXT;
															}
														} else if (displayType.equals("list")){
															if(style.equals("radio")){
																newType = FilterType.BOOLEAN;
															} else /*if (style.equals("menu"))*/{
																if(multipleValues!=null && multipleValues.equals("1")){
																	newType = FilterType.MULTISELECT;
																} else {
																	newType = FilterType.SINGLESELECT;
																}
															}
														}
														if (newType == null){
															Log.error("Invalid filter type in old XML! Setting to text");
															newType = FilterType.TEXT;
														}
														org.biomart.objects.objects.Attribute attribute = fieldTableKeyToAttribute.get(fieldName+tableName+keyName);
														if (attribute == null)
															attribute = fieldTableKeyToAttribute.get(fieldName+templateName+"__"+tableName+keyName);
														if (attribute == null)
															attribute = config.getAttributeByName(filterDescription.getAttributeValue("internalName").toLowerCase(), null);

														if(attribute==null) { // The attribute for this filter doesn't exist, so we need to create it and hide it
															if(tableName!=null && !(tableName.equalsIgnoreCase("main") || tableName.endsWith("__main"))){													
																{ // Attribute is not a member of a DM partition, because it's "_" split only has one section
																	//String currentTableName = mainTableName + templateName + "__" + splitName[0] + "__dm";
																	String currentTableName = tableName;
																	DatasetTable currentTable = mart.getTableByName(currentTableName);
																	if(currentTable==null){
																		Log.error("Can't create filter, because table " + currentTableName + " not found; trying " + mart.getName() + "__" + currentTableName);
																		currentTable = mart.getTableByName("(p0c5)__" + currentTableName);
																	}
																	if(currentTable==null && currentTableName.split("__",2).length>1){
																		currentTable = mart.getTableByName("(p0c5)__" + currentTableName.split("__",2)[1]);
																	}
																	if(currentTable==null){
																		Log.error("Nope, still missing");
																	} else {
																		DatasetColumn currentColumn = currentTable.getColumnByName(fieldName);
																		if(currentColumn!=null){
																			org.biomart.objects.objects.Attribute hiddenAttribute = new org.biomart.objects.objects.Attribute(currentColumn, filterDescription.getAttributeValue("internalName").toLowerCase());
																			filterGroupContainer.addAttribute(hiddenAttribute);
																			fieldTableKeyToAttribute.put(fieldName + tableName + keyName, hiddenAttribute);
																			hiddenAttribute.setHideValue(true);
																			if(filterDescription.getAttributeValue("displayName")!=null){
																				hiddenAttribute.setDisplayName(filterDescription.getAttributeValue("displayName"));
																			}
																			hiddenAttribute.setName(hiddenAttribute.getInternalName());

																			hiddenAttribute.setDescription(filterDescription.getAttributeValue("description",""));


																			
																			attribute = hiddenAttribute;
																		}
																	}
																	if(attribute==null){
																		Log.error("Something's wrong");
																	}
																}
															} else if (tableName!=null && (tableName.equalsIgnoreCase("main") || tableName.endsWith("__main") )){
																DatasetTable currentTable= mainTableByKey.get(filterDescription.getAttributeValue("key"));

																if (currentTable != null) {
																	DatasetColumn currentColumn = currentTable.getColumnByName(fieldName);
																	if (currentColumn == null) {
																		//System.err.println("FILTER Can't create attribute, because column " + filterDescription.getAttributeValue("field") + " wasn't found in table " + currentTable.getName());
																	} else {
																		org.biomart.objects.objects.Attribute hiddenAttribute = new org.biomart.objects.objects.Attribute(currentColumn,filterDescription.getAttributeValue("internalName").toLowerCase());
																		filterGroupContainer.addAttribute(hiddenAttribute);
																		hiddenAttribute.setHideValue(true);
																		if(filterDescription.getAttributeValue("displayName")!=null){
																			hiddenAttribute.setDisplayName(filterDescription.getAttributeValue("displayName"));
																		}
																		hiddenAttribute.setName(hiddenAttribute.getInternalName());

																		fieldTableKeyToAttribute.put(fieldName + tableName + keyName, attribute);
																		attribute = hiddenAttribute;
																	}
																} else {
																	//System.err.println("FILTER Main table reference with non-existing key.");
																}
															}

														} else {
															//System.err.println("======> " + tableName+" " +fieldName+" "+keyName+" "+attribute.getName());
														}
														//TODO filter creation
														if(attribute!=null){ // If it's possible to find or make an attribute filter at this point, we've done it
															// so we can move on and create the filter, and populate it's data tree

															//Check if the attribute is part of a partition table and if it is get the current partition table

															Filter filter = new Filter(attribute,filterDescription.getAttributeValue("internalName").toLowerCase());

															if (attribute.getName().split("__").length > 1){
																//filter = new Filter(attribute,attribute.getName().split("__")[0] + "__" + filterDescription.getAttributeValue("internalName").toLowerCase());
																filter = new Filter(attribute,filterDescription.getAttributeValue("internalName").toLowerCase());
															}
															filterGroupContainer.addFilter(filter);


															if(filterDescription.getAttributeValue("displayName")!=null){

																filter.setDisplayName((filterDescriptions.size() == 1) ? filterCollectionName : filterCollectionPrefix + filterDescription.getAttributeValue("displayName"));

															}
															filter.setDescription(filterDescription.getAttributeValue("description", ""));

															String legalQualifiers = filterDescription.getAttributeValue("legal_qualifiers");
															if(legalQualifiers!=null){
																if(legalQualifiers.contains(">=")){
																	filter.setQualifier(OperatorType.GTE);
																} else if (legalQualifiers.contains("<=")){
																	filter.setQualifier(OperatorType.LTE);
																} else if (legalQualifiers.contains(">")){
																	filter.setQualifier(OperatorType.GT);
																} else if (legalQualifiers.contains("<")){
																	filter.setQualifier(OperatorType.LT);
																} else if (legalQualifiers.contains("in") || legalQualifiers.contains("=")){
																	filter.setQualifier(OperatorType.E);
																} else if (legalQualifiers.contains("like")){
																	filter.setQualifier(OperatorType.LIKE);
																} else {
																	filter.setQualifier(OperatorType.E);
																}
															}
															// Need to set filter's hidden status to be the same as the attached attribute
															//filter.setHideValueInString(attribute.getHideString());
															filter.setFilterType(newType);
															if(newType==FilterType.BOOLEAN){
																filter.setOnlyValue("Only");
																filter.setExcludedValue("Excluded");
															}
															//filterCollectionContainer.addFilter(filter);

															parseSpecificFilterContent(dataRoot, filterDescription, filter, "SpecificFilterContent");
															if(newType!=FilterType.BOOLEAN){
																parseFilterOptions(dataRoot,filterDescription,filter);
															}
														} else {
															//System.err.println("Filter " + filterDescription.getAttributeValue("internalName").toLowerCase() + " couldn't be created, because no matching column was found");
														}
													}
												}
											} else if(filterDescription.getAttributeValue("hidden","false").equals("false")){ //TODO add in Filter Pointers
												if(filterDescription.getAttributeValue("pointerDataset") == null){
													Log.error("Invalid pointer filter: " + config.getName() + " " + filterDescription.getAttributeValue("internalName"));
												} else if(isRemotePointer(mart.getPartitionTableByName("p0"), internalNames, filterDescription.getAttributeValue("pointerDataset",""), oldPartitionToColumn)){ // It's a non-local pointer
													String[] pointerDataset = filterDescription.getAttributeValue("pointerDataset").split("\\*",-1);
													if(pointerDataset.length <= 3){
														Filter pointerFilter = new Filter(filterDescription.getAttributeValue("internalName").toLowerCase(), filterDescription.getAttributeValue("pointerFilter"), replaceAliases(filterDescription.getAttributeValue("pointerDataset"),oldPartitionToColumn));
														filterGroupContainer.addFilter(pointerFilter);
														this.pointedDatasets.get(mart.getName()).add(pointerFilter.getPointedDatasetName());
														//pointerFilter.setInternalName(filterDescription.getAttributeValue("pointerFilter"));
														//pointerAttribute.setConfigName("POINTER");

														//pointerFilter.setName(pointerFilter.getInternalName());


													} else if (pointerDataset.length > 3){
														Log.error("Too many aliases in pointerDataset property!");
													} else {
														PartitionTable mainPartition = mart.getPartitionTableByName("p0");
														HashMap<String, String> internalNameToValue = oldPartitionAliases.get(pointerDataset[1]);
														for(String internalName : internalNameToValue.keySet()){
															Filter pointerFilter = new Filter(filterDescription.getAttributeValue("internalName").toLowerCase(), filterDescription.getAttributeValue("pointerFilter"),  pointerDataset[0] + internalNameToValue.get(internalName) + pointerDataset[2]);
															filterGroupContainer.addFilter(pointerFilter);
															//pointerFilter.setInternalName(filterDescription.getAttributeValue("pointerFilter"));
															this.pointedDatasets.get(mart.getName()).add(pointerFilter.getPointedDatasetName());

															//pointerAttribute.setConfigName("POINTER");
															//pointerFilter.setName(pointerFilter.getInternalName());
															int hideColumn = mainPartition.addColumn("true");
															int row = mainPartition.getRowNumberByDatasetName(internalName);
															if(row >= 0)
																mainPartition.updateValue(row, hideColumn, "false");

															pointerFilter.setHideValueInString("(p0c" + Integer.toString(hideColumn) + ")");


														}
													}

												}

											}

											//filterGroupContainer.addContainer(filterCollectionContainer);
										}
									}
								}
							}
						}
					}
				}
				List importables = root.getChildren("Importable");
				Iterator importablesIterator = importables.iterator();
				while (importablesIterator.hasNext()) {
					boolean include = true;
					Element oldImportable = (Element) importablesIterator.next();
					String[] filters = oldImportable.getAttributeValue("filters").split(",");
					ElementList newImportable = new ElementList(config, replaceAliases(oldImportable.getAttributeValue("linkName", "!NONAME!"),oldPartitionToColumn), PortableType.IMPORTABLE);
					newImportable.setProperty(XMLElements.ORDERBY, oldImportable.getAttributeValue("orderBy",""));
					newImportable.setLinkVersion(replaceAliases(oldImportable.getAttributeValue("linkVersion", ""), oldPartitionToColumn));
					newImportable.setProperty(XMLElements.TYPE,oldImportable.getAttributeValue("type","link"));

					if(newImportable.getName().equals("anatomical_system_term"))
						Log.error("Checking importable");

					for(String filterName : filters){
						if(filterName.equals("link_anatomical_system"))
							Log.error("checking filter");
						Filter filterObject = rootContainer.getFilterRecursively2(filterName);
						if(filterObject == null){
							include = false;
							break;
						} else {
							newImportable.addFilter(filterObject);
						}
					}
					if(include && newImportable.getFilterList().size()>0){
						config.addElementList(newImportable);
					} else {
						Log.error("Importable warning: " + newImportable.getName() + " not added");
						Log.error("\t" + oldImportable.getAttributeValue("filters"));
					}

				}
				List exportables = root.getChildren("Exportable");
				Iterator exportablesIterator = exportables.iterator();
				while (exportablesIterator.hasNext()) {
					boolean include = true;
					Element oldExportable = (Element) exportablesIterator.next();
					String[] attributes = oldExportable.getAttributeValue("attributes").split(",");
					ElementList newExportable = new ElementList(config, replaceAliases(oldExportable.getAttributeValue("linkName", "!NONAME!"),oldPartitionToColumn), PortableType.EXPORTABLE);
					newExportable.setProperty(XMLElements.ORDERBY, oldExportable.getAttributeValue("orderBy",""));
					newExportable.setLinkVersion(replaceAliases(oldExportable.getAttributeValue("linkVersion", ""), oldPartitionToColumn));
					newExportable.setProperty(XMLElements.TYPE,oldExportable.getAttributeValue("type","link"));
					if(newExportable.getName().endsWith("uniprot_id"))
						Log.error("Checking exportable");
					String defaultState = oldExportable.getAttributeValue("default","false");
					if(defaultState.equals("true") || defaultState.equals("1"))
						defaultState = "true";
					else
						defaultState = "false";
					newExportable.setDefaultState(new Boolean(defaultState));

					for(String attributeName : attributes){
						org.biomart.objects.objects.Attribute attributeObject = rootContainer.getAttributeRecursively2(attributeName);
						if(attributeObject == null){
							include = false;
							Log.error("Attribute not found: " + attributeName);
							break;
						} else {
							attributeObject.setName(attributeObject.getInternalName());
							newExportable.addAttribute(attributeObject);
						}
					}
					if(include && newExportable.getAttributeList().size()>0){
						config.addElementList(newExportable);
					} else {
						Log.error("Exportable warning: " + newExportable.getName() + " not added");
						Log.error("\t" + oldExportable.getAttributeValue("attributes"));
					}
				}
				if(dataRoot.getChildren().size()>0){
					allRoot.addContent(martOptions);
					/*try {
						XMLOutputter outputter = new XMLOutputter();
						FileWriter writer = new FileWriter(Settings.getStorageDirectory().getCanonicalPath() + "/option/" + templateName + ".xml");
						outputter.output(xmlOptions, writer);
						writer.close();
					} catch (java.io.IOException e) {
						e.printStackTrace();
					}*/
				}
			} catch (Exception e){
				e.printStackTrace();
			}
		}
		Log.error("Done");
		return martList;
	}

	private String processLinkoutURL(Element attributeDescription, Config config) {
		String linkOut = attributeDescription.getAttributeValue("linkoutURL","").replaceAll("%s", "%s%");
		String attributeName = attributeDescription.getAttributeValue("internalName");
		
		if(linkOut.startsWith("exturl|http://") || linkOut.startsWith("exturl|https://") || linkOut.startsWith("exturl|ftp://"))
			linkOut = linkOut.replaceFirst("exturl\\|","");
		linkOut = linkOut.replaceFirst("exturl\\|","%exturl%");
		if(linkOut.startsWith("%exturl%") && (config.getAttributeByName("exturl", null) == null)){
			
			org.biomart.objects.objects.Attribute exturl = new org.biomart.objects.objects.Attribute("exturl", "External URL prefix");
			exturl.setHideValue(true);
			exturl.setDescription("This pseudoattribute is created by Backwards compatibility to allow linkOutURLs to function. Please manually check that the value is correct.");
			if(isWebService){
				exturl.setValue(this.dataLinkInfo.getUrlLinkObject().getFullHost());
			} else {
				exturl.setValue("");
			}
			config.getRootContainer().addAttribute(exturl);
			
		}
				
		String[] splitLink = linkOut.split("\\|+",0);
		if(splitLink.length > 1){
			String[] splitURL = splitLink[0].split("%s%",-1);
			StringBuilder tempURL = new StringBuilder(splitURL[0]);
			for(int i = 1; i < splitLink.length; ++i){
				if(splitLink[i].equals(attributeName))
					splitLink[i] = "s";
				tempURL.append("%" + splitLink[i] + "%");
				tempURL.append(splitURL[i]);
			}
			linkOut = tempURL.toString();
		}
			
		return linkOut;
	}

	private HashSet<String> getColumns(String tableName) {
		HashSet<String> currentColumns = new HashSet<String>();
		try {
			ResultSet result = this.databaseConnection.getMetaData().getColumns(null, this.schema, tableName, null);
			while (result.next()){
				//System.out.println(result.getString(1));
				currentColumns.add(result.getString(4));
			}
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return currentColumns;
	}

	private void parseFilterOptions(Element dataRoot,
			Element parentNode, Filter filter) {
		List optionList = parentNode.getChildren("Option");
		if(optionList.size() > 0){
			Element filterXML = new Element("filter");
			filterXML.setAttribute(new Attribute("name",filter.getName()));
			parseFilterOptionsRecursive(parentNode,filterXML);
			dataRoot.addContent(filterXML);
		}
	}

	private void parseSpecificFilterContent(Element dataRoot,
			Element parentNode, Filter filter, String name) {
		List optionList = parentNode.getChildren(name);
		Iterator optionListIterator = optionList.iterator();
		if(optionList.size() > 0){
			Element filterXML = new Element("filter");
			filterXML.setAttribute(new Attribute("name",filter.getName()));
			while(optionListIterator.hasNext()){
				Element currentOption = (Element) optionListIterator.next();
				Element newPartition = new Element("dataset").setAttribute(new Attribute("name",currentOption.getAttributeValue("internalName","OOPS!")));
				parseFilterOptionsRecursive(currentOption, newPartition);
				filterXML.addContent(newPartition);
			}
			dataRoot.addContent(filterXML);
		}
	}
	/*if (optionList.size() > 0){
			Element filterXML = new Element("Filter");
			filterXML.setAttribute(new Attribute("name",filter.getName()));
			while(optionListIterator.hasNext()){
				Element filterXML = new Element("Filter");
				Element currentOption = (Element) optionListIterator.next();
				filterXML.setAttribute(new Attribute("name",filter.getName() + "___" + currentOption.getAttributeValue("internalName","OOPS!")));
				parseFilterOptionsRecursive(currentOption, filterXML);
			}
			dataRoot.addContent(filterXML);

		}
	}*/

	private void parseFilterOptionsRecursive(
			Element parentNode, Element rowNode) {
		List pushList = parentNode.getChildren("PushAction");
		Iterator pushListIterator = pushList.iterator();
		while(pushListIterator.hasNext()){
			Element currentPush = (Element) pushListIterator.next();
			String optionData = currentPush.getAttributeValue("ref","NULL"); // + separator + currentOption.getAttributeValue("displayName","NULL") + separator + currentOption.getAttributeValue("default","NULL");
			Element newPush = new Element("filter").setAttribute(new Attribute("name",optionData));
			parseFilterOptionsRecursive(currentPush, newPush);
			rowNode.addContent(newPush);
		}
		List optionList = parentNode.getChildren("Option");
		Iterator optionListIterator = optionList.iterator();
		while(optionListIterator.hasNext()){
			Element currentOption = (Element) optionListIterator.next();
			String optionData = currentOption.getAttributeValue("value","NULL").replace("|", "\\|") + separator + currentOption.getAttributeValue("displayName","NULL").replace("|", "\\|") + separator + currentOption.getAttributeValue("default","false");
			Element newRow = new Element("row").setAttribute(new Attribute("data",optionData));
			parseFilterOptionsRecursive(currentOption, newRow);
			rowNode.addContent(newRow);
		}
	}

	private Container populateContainer(Element element){
		Container container = new Container(element.getAttributeValue("internalName").toLowerCase());
		container.setInternalName(element.getAttributeValue("internalName").toLowerCase());
		if(element.getAttributeValue("hidden","false").equals("true")){
			container.setHideValue(true);
		}
		//String displayName = child.getAttributeValue("displayName");
		if(element.getAttributeValue("displayName")!=null){
			container.setDisplayName(element.getAttributeValue("displayName"));
		}
		container.setMaxAttributes(Integer.parseInt(element.getAttributeValue("maxSelect","0")));
		container.setDescription(element.getAttributeValue("description","NULL"));
		container.setHideValue(element.getAttributeValue("hideDisplay","false").equals("true"));
		return container;
	}
	/**
	 * Disconnects from the database at the end of execution.
	 */
	private void disconnectDB(){
		try {
			databaseConnection.close();
			System.err.println("Disconnected.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Connects to the database at the beginning of the query.
	 * Currently the database parameters are hard-coded.
	 */
	private void connectDB(){
		// Login information for the sequence database
		// URL format: jdbc:mysql://server[:port]/[databasename]

		String URL = "jdbc:mysql://martdb.ensembl.org:5316/ensembl_mart_56";
		String username = "anonymous";
		String password = "";

		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (Exception e) {
			System.err.println("Failed to load JDBC/ODBC driver.");
		}

		try {
			databaseConnection = DriverManager.getConnection (URL,username,password);
			System.err.println("Connected.");
		} catch (Exception e) {
			System.err.println("problems connecting to "+URL);
		}
	}
	/**
	 *  The database connection for this invocation of the processor.
	 */
	private Connection databaseConnection = null;
	private String schema = null;
	private HashSet<String> queryDB(String query){
		HashSet<String> resultList = new HashSet<String>();
		try {
			Statement stmt = null;
			stmt = databaseConnection.createStatement();
			ResultSet result = stmt.executeQuery(query);

			while (result.next()){
				//System.out.println(result.getString(1));
				resultList.add(result.getString(1));
			}
		} catch (Exception e){
			e.printStackTrace();
		}
		return resultList;
	}
	private HashMap<String, byte[]> queryDBbytes(String query){
		HashMap<String, byte[]> resultList = new HashMap<String, byte[]>();
		try {
			Statement stmt = null;
			stmt = databaseConnection.createStatement();
			ResultSet result = stmt.executeQuery(query);

			while (result.next()){
				//System.out.println(result.getString(1));
				resultList.put(result.getString(1), result.getBytes(2));
			}
		} catch (Exception e){
			e.printStackTrace();
		}
		return resultList;
	}
	private static String replaceAliases(String oldText, HashMap<String, Integer> map){
		String newText = oldText;
		for(String alias: map.keySet()){
			newText = newText.replaceAll("\\*" + alias + "\\*", "(p0c" + map.get(alias).toString() + ")");
		}
		return newText;
	}

	private boolean isRemotePointer(PartitionTable partitionTable, HashSet<String> internalNames, String templateName, HashMap<String, Integer> map){
		if(templateName.contains("*")){
			String[] splitName = templateName.split("\\*");
			for(String prefix : partitionTable.getCol(map.get(splitName[1]))){
			if(internalNames.contains((splitName[0] + prefix + splitName[2])))
				return false;
			}
			return true;
		} else {
			if(internalNames.contains(templateName))
				return false;
			return true;
		}
	}
	private String separator = Resources.get("COLUMNSEPARATOR");

	private boolean isWebService = false;

	private boolean isDMPartitioned = false;

	private List<DatasetFromUrl> datasetList;
	private LinkedHashMap<String, DatasetFromUrl> datasetMap;
	//added by Yong
	private MartRegistry martRegistry;
	private DataLinkInfo dataLinkInfo;


	public void setDatasetsForUrl(List<DatasetFromUrl> dsList) {
		this.datasetList = dsList;
		datasetMap = new LinkedHashMap<String,DatasetFromUrl>();
		for(DatasetFromUrl ds: dsList) {
			datasetMap.put(ds.getName(), ds);
		}
	}

	public void setMartRegistry(MartRegistry martRegistry) {
		this.martRegistry = martRegistry;
	}


	public void setConnectionObject(Connection conObj) {
		this.databaseConnection = conObj;
	}
	
	
	
	public void setSchema(String schema) {
		this.schema = schema;
	}

	public void setDataLinkInfoObject(DataLinkInfo dlink) {
		this.dataLinkInfo = dlink;
		if(this.dataLinkInfo.getDataLinkType().equals(DataLinkType.URL)) {
			this.isWebService = true;
		}
	}
}
