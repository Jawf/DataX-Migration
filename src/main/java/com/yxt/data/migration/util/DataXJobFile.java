package com.yxt.data.migration.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.jayway.jsonpath.JsonPath;

@Service
public class DataXJobFile {

	protected static final Log log = LogFactory.getLog(DataXJobFile.class);
	private static String template = null;

	@Autowired
	private AppConfig config;

	public void generateJsonJobFile(String sourceTableName, String targetTableName, List<String> columns, String pk, String whereClause, long migrationRecords) {

		String json = getTemplate();

		CharSequence cols = getColumnsString(columns);
		int channels = getChannelNumber(migrationRecords);
		
		json = json.replace("{job.channel}", String.valueOf(channels));
		json = json.replace("{source.db.username}", config.getSourceDbUsername());
		json = json.replace("{source.db.password}", config.getSourceDbPassword());
		json = json.replace("{source.db.table.columns}", cols);
		json = json.replace("{source.db.table.pk}", pk == null ? "" : pk);
		json = json.replace("{source.db.table.name}", sourceTableName);
		json = json.replace("{source.db.url}", config.getSourceDbUrl());
		
		if (whereClause!=null && !"".equals(whereClause)){
			json = json.replace("{source.db.table.where.clause}",
					"\"where\": \" " + whereClause +"\",");
		} else {
			json = json.replace("{source.db.table.where.clause}\n                        ", "");
		}
		
		json = json.replace("{target.db.username}", config.getTargetDbUsername());
		json = json.replace("{target.db.password}", config.getTargetDbPassword());
		json = json.replace("{target.db.table.columns}", cols);
		json = json.replace("{target.db.table.name}", targetTableName);
		json = json.replace("{target.db.url}", config.getTargetDbUrl());

		//log.info(json);

		try {
			log.info("Write job json for table:"+sourceTableName);
			writeToFile(sourceTableName, json);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}

	private int getChannelNumber(long migrationRecords) {
		int result = 1;
		if ("true".equalsIgnoreCase(config.getDataxUseMultipleChannel()) && migrationRecords > 0) {
			if (migrationRecords > config.getDataxUse2ChannelRecordsOver()) {
				result = 2;
			} 
			if (migrationRecords > config.getDataxUse4ChannelRecordsOver()) {
				result = 4;
			} 
			if (migrationRecords > config.getDataxUseNChannelRecordsOver()) {
				result = config.getDataxUseNChannelNumber();
			}

		}
		return result;
	}

	public String getSourceGlobalTableWhereClause(List<String> columns) {
		String whereCase1 = config.getGlobalWhereClause();
		String whereCase2 = config.getGlobalWhere2Clause();
		String result = null;
		if (columns != null && !columns.isEmpty()) {
			if (hasWhereColumn(whereCase1, columns)) {
				result = whereCase1;
			} else if (hasWhereColumn(whereCase2, columns)) {
				result = whereCase2;
			}
		}
		return result;
	}

	/**
	 * Pre-condition: the job file has need to be generated.
	 * @param tableName String
	 * @return String
	 */
	public String getJobFileWhereClause(String tableName){
		String jsonContent = this.ReadFile(config.getDataxToolFolder() + "/job/" + tableName + ".json");
		String value = null;
		try {
			value = JsonPath.read(jsonContent, "$.job.content[0].reader.parameter.where");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return value;
				
	}

	private boolean hasWhereColumn(String whereCase, List<String> columns) {
		String whereCol = null;
		
		if (!StringUtils.isEmpty(whereCase)) {
			whereCase = whereCase.replace(" ", "");
			whereCase = whereCase.replace("\"", "");
			whereCase = whereCase.replace("in", "=");
			String[] temp = whereCase.split("=");
			if (temp != null && temp.length > 1) {
				whereCol=temp[0];
			}
		}

		boolean result = false;
		if (whereCol!=null && columns != null && columns.size() > 0) {
			for (String column:columns){
				if (column!=null && column.equalsIgnoreCase(whereCol)){
					result = true;
					break;
				}
			}
		}

		return result;
	}


	private void writeToFile(String fileName, String json) throws IOException {
		File file = new File(config.getDataxToolFolder() + "/job/" + fileName + ".json");

		BufferedWriter out = new BufferedWriter(new FileWriter(file));
		out.write(json);
		out.close();
		log.info("Write json to file:"+file.getAbsolutePath());
	}

	private CharSequence getColumnsString(List<String> columns) {
		StringBuffer stb = new StringBuffer();

		for (String s : columns) {
			stb.append("\"");
			stb.append(s);
			stb.append("\",");
		}
		return stb.subSequence(0, stb.length() - 1);
	}
	
	private String getTemplate() {
		if (template == null) {
			StringBuffer stb = new StringBuffer();
			try {
				readToBuffer(stb, "job/jobtemplate.json");
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
			template = stb.toString();
			log.info(template);
		}

		return template;
	}

	private void readToBuffer(StringBuffer buffer, String filePath) throws IOException {
		InputStream is = DataXJobFile.class.getClassLoader().getResourceAsStream(filePath);
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String line; 
		line = reader.readLine(); 
		while (line != null) {
			buffer.append(line);
			buffer.append("\n"); 
			line = reader.readLine();
		}
		reader.close();
	}
	
	private String ReadFile(String path) {  
        File file = new File(path);  
        BufferedReader reader = null;  
        String result = "";  
        try {  
            reader = new BufferedReader(new FileReader(file));  
            String tempString = null;  
            while ((tempString = reader.readLine()) != null) {  
                result = result + tempString;  
            }  
            reader.close();  
        } catch (IOException e) {  
        	log.error(e.getMessage(), e);
        } finally {  
            if (reader != null) {  
                try {  
                    reader.close();  
                } catch (IOException e1) {  
                }  
            }  
        }  
        return result;  
    } 

}
