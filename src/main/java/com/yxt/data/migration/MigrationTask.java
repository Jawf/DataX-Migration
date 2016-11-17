package com.yxt.data.migration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yxt.data.migration.bean.DataTable;
import com.yxt.data.migration.bean.DataTableStatus;
import com.yxt.data.migration.util.AppConfig;
import com.yxt.data.migration.util.DataXJobFile;

/**
 * 
 * Batch Migration Task to split each table to one DataX job. Then call datax
 * script to start migration and noted the status records to reports.
 * @author Jawf Can Li
 * @since 1.0 base on datax 3.0
 * 
 */
@Service()
public class MigrationTask {

	protected static final Log log = LogFactory.getLog(MigrationTask.class);

	protected static boolean breakFlag = false;
	
	@Autowired
	private AppConfig config;

	@Autowired
	private DataXJobFile jobFile;
	
	@Autowired
	private DbViewer viewer;

	private List<DataTableStatus> allTableStatus;
	private List<DataTable> targetTables;


	/*
	 * Testing
	 * @param reportFlag 
	 * @param args
	 * @throws IOException
	 */
	/*public static void main(String args[]) throws IOException {

		Process process = Runtime.getRuntime().exec(
				"python E:/work/db_migration/datax/datax/bin/datax.py E:/work/db_migration/datax/datax/job/qidatest.json");

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line = null;
			while ((line = reader.readLine()) != null) {
				System.out.println(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/

	public void execute() {
		execute(false);		
	}
	
	public void execute(final boolean skipCommandFlag) {
		try {

			initAllTableStatus();
			log.info("init All Table Status done with all table size="+allTableStatus!=null?allTableStatus.size():0);

			
			//Get the split index points.
			List<Integer> splitIndexs = getJobThreadSplitIndexes();
			
			//Split All table jobs to several job threads According to the splitIndexs
			int startNum = 0;
			int endNum = 0;
			if (splitIndexs!=null && !splitIndexs.isEmpty()){
				for (int i = 0; i < splitIndexs.size(); i++) {
					if (i==0){
						endNum = splitIndexs.get(i);
					} else if (i==splitIndexs.size()-1){
						startNum = endNum;
						endNum = allTableStatus.size()-1;
					} else {
						startNum = endNum;
						endNum = splitIndexs.get(i);
					} 
					if (endNum==0){
						//when  allTableStatus.size() ==1
						endNum = 1;
					}
					final int jobStartNum = startNum;
					final int jobEndNum = endNum;
					log.info("Split Jobs: Job Thread "+(i+1)+": Start Index>="+jobStartNum+", End Index<"+jobEndNum);
					new Thread(new Runnable() {
						public void run() {
							StringBuffer logInfo = new StringBuffer();
							for (int t1=jobStartNum;t1<jobEndNum;t1++){
								long threadId = Thread.currentThread().getId();
								String threadString = "Thread "+ threadId + " - ";
								if (breakFlag){
									logInfo.append(threadString+"Breaked as got exception in one jobs thread").append("\n");
									log.info(logInfo.toString());
									break;
								}
								DataTableStatus geTableBean = allTableStatus.get(t1);
								String sourceTableName = geTableBean.getName();
								if (isIgnoreTables(sourceTableName)){
									logInfo.append(threadString+sourceTableName + " ingored as it configed as ingored table").append("\n");
									log.info(logInfo.toString());
									continue;
								}
								if (isIgnoreBigTables(geTableBean.getSize())){
									logInfo.append(threadString+sourceTableName + " ingored as its size too bigger than the configured size").append("\n");
									log.info(logInfo.toString());
									continue;
								}
								logInfo.append(threadString+sourceTableName+" job start to transfer...\n");
								String targetTableName = getTargetTableName(sourceTableName);
								String whereClause = jobFile.getJobFileWhereClause(sourceTableName);
								
								boolean hasException = false;
								try {
									long pendingCount = viewer.getSourceTransfterTableMigrationCount(sourceTableName, whereClause);
									geTableBean.setPendingRecords(pendingCount);
									
									if (pendingCount == 0) {
										logInfo.append(threadString + sourceTableName + " ingored as its count=0\n");
										log.info(logInfo.toString());
										continue;
									}
									
									if (!skipCommandFlag){
										//Execute Command
										String cmd = getCommand(sourceTableName);
										hasException = executeCommand(cmd, geTableBean, logInfo);
										if (!"true".equalsIgnoreCase(config.getErrorContinue())) {
											if (hasException){
												breakFlag = true;
												logInfo.append(threadString+sourceTableName+" breaked as got exception in this jobs thread").append("\n");
												log.info(logInfo.toString());
												break;
											}
										}
									}								
								} catch (Exception e) {
									hasException = true;
									log.error(e.getMessage(), e);
									if (!"true".equalsIgnoreCase(config.getErrorContinue())) {
										breakFlag = true;
										logInfo.append(threadString+sourceTableName+" breaked as got exception in this jobs thread").append("\n");
										log.info(logInfo.toString());
										break;
									}
								} finally {
									try {
										long finishedCount = viewer.getTargetTransfterTableMigrationFinishedCount(targetTableName, whereClause);
										geTableBean.setFinishedRecords(finishedCount);
									} catch (SQLException e) {
										hasException = true;
										log.error(e.getMessage(), e);
										if (!"true".equalsIgnoreCase(config.getErrorContinue())) {
											breakFlag = true;
											logInfo.append(threadString+sourceTableName+" breaked as got exception in this jobs thread").append("\n");
											log.info(logInfo.toString());
											break;
										}
									}
								}
								geTableBean.setHasException(hasException);
								
								geTableBean.setFinished(true);
								logInfo.append(threadString+sourceTableName+" job finished for transfer. Table index="+t1).append("\n");	
								
								try {
									outputMigrationStatusToFile();
								} catch (IOException e) {
									log.error(threadString+e.getMessage(), e);
								}
								log.info(logInfo.toString());
							}
						}
						
						
						private boolean executeCommand(String command, DataTableStatus tableStatus, StringBuffer logInfo) throws IOException {
							long threadId = Thread.currentThread().getId();
							String threadString = "Thread "+ threadId + " - ";
							boolean hasException = false;
							
							//Execute
							Process process = Runtime.getRuntime().exec(command);
							
							if (tableStatus.getPendingRecords()>1000000){
								StreamCommandOutputThread outputThread = new StreamCommandOutputThread(process.getInputStream(), threadString, tableStatus, logInfo);
								outputThread.start();
								
								try {
									outputThread.join();								
									//process.waitFor();
								} catch (InterruptedException e) {
									hasException = true;
									log.error(threadString+e.getMessage(), e);
								}
							} else {
								BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
								String line = null;
								
								while ((line = reader.readLine()) != null) {
									logInfo.append(threadString+line).append("\n");
									if (!hasException && line!=null && (line.contains("DataXException") || line.contains("SQLException"))){
										hasException = true;
										tableStatus.setHasException(hasException);
									}
									readExecueOutputLineStatus(line, tableStatus);
								}
								logInfo.append(threadString+"execute finished!");
							}
							return hasException;
						}
	
	
						
					}).start();
		
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage(), e);
		} finally {
			try {
				outputMigrationStatusToFile();
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	private static void readExecueOutputLineStatus(String line, DataTableStatus tableStatus) {
		if (line!=null && line.contains("  :  ")){
			String[] resultTemp = line.split("  :  ");
			String value = resultTemp[1].trim();
			if (value.matches("\\d+s")){
				tableStatus.setCostTime(value);
			} else if (value.matches("\\d+[A-Z]*B\\/s")){
				tableStatus.setReadWriteRateSpeed(value);
			} else if (value.matches("\\d+rec\\/s")){
				tableStatus.setReadWriteRecordSpeed(value);
			}
		}else if (line!=null && line.contains("used")){
			int count = Integer.parseInt(line.substring(line.indexOf("used")+5, line.indexOf("ms")-1).trim());
			tableStatus.setCostTime(count+"ms");
		} else if (line!=null && line.contains("StandAloneJobContainerCommunicator")){
			int count = Integer.parseInt(line.substring(line.indexOf("Total")+5, line.indexOf("records")).trim());
			tableStatus.setReadWriteRecords(count);
			
			int countFail = Integer.parseInt(line.substring(line.indexOf("Error")+5, line.indexOf("records", line.indexOf("Error"))).trim());
			tableStatus.setReadWriteFailRecords(countFail);
			
			String speed = line.substring(line.indexOf("Speed")+5, line.indexOf("B/s")+3).trim();
			tableStatus.setReadWriteRateSpeed(speed);
			
			String speedRec = line.substring(line.indexOf(",", line.indexOf("B/s"))+1, line.indexOf("records/s")+9).trim();
			tableStatus.setReadWriteRecordSpeed(speedRec);								
		}
	}


	private String getCommand(String fileName) {
		String dataxPath = config.getDataxToolFolder();
		String command = "python " + dataxPath + "/bin/datax.py " + dataxPath + "/job/" + fileName + ".json";
		return command;
	}

	protected boolean executeCommand(String command) throws IOException {
		Process process = Runtime.getRuntime().exec(command);

		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line = null;
		boolean hasExeption = false;
		while ((line = reader.readLine()) != null) {
			log.info(line);
			if (!hasExeption && line!=null && line.contains("Exception")){
				hasExeption = true;
			}
		}
		return hasExeption;
	}

	private void initAllTableStatus() throws SQLException {
		allTableStatus = viewer.getSourceTransfterTablesStatus();
		targetTables = viewer.getTargetTransfterTables();
	}
	
	private List<Integer> getJobThreadSplitIndexes() {
		List<Integer> splitIndexs = new ArrayList<Integer>();
		String type = config.getJobthreadSplitType();
		float[] splitConfigSize = getSplitSizeFromConfig();//Size Unit: MB
		int[] splitConfigIndexes = getSplitIndexFromConfig();//Size Unit: MB
		int splitMaxCount = config.getJobthreadSplitMaxcount();
		if (allTableStatus != null && allTableStatus.size() > 1 ){
			 if ("index".equalsIgnoreCase(type) && splitConfigIndexes!=null) {
				int splitCount = 0;				
				int i = 0;
				if (splitConfigIndexes[i]==0){
					i++;
				}
				for (int j = 1; j < allTableStatus.size(); j++) {
					if (splitCount >= splitMaxCount) {
						splitIndexs.add(j);
						splitCount = 0;
					} else if (j >= splitConfigIndexes[i] && j <= splitConfigIndexes[splitConfigIndexes.length - 1]) {
						splitIndexs.add(j);
						splitCount = 0;
						if (i < splitConfigIndexes.length - 1) {
							i++;
						}
					} else {
						splitCount++;
					}
				}
			} else if (splitConfigSize != null) {
				int splitCount = 0;
				for (int i = 0; i < splitConfigSize.length; i++) {
					int n = 0;
					if (i > 0 && splitIndexs.size() > 0) {
						n = splitIndexs.get(splitIndexs.size() - 1);
					}
					for (int j = n + 1; j < allTableStatus.size(); j++) {
						DataTable ta = allTableStatus.get(j);

						float size = ta.getSize();
						if (splitCount >= splitMaxCount) {
							splitIndexs.add(j);
							splitCount = 0;
						} else if (size < splitConfigSize[i]) {
							splitIndexs.add(j);
							splitCount = 0;
							break;
						} else {
							splitCount++;
						}
					}
				}
			} else {
				splitIndexs.add(allTableStatus.size() - 1);
			}
		} else if (allTableStatus != null && allTableStatus.size() > 0) {
			splitIndexs.add(allTableStatus.size() - 1);
		}
		return splitIndexs;
	}

	private String getTargetTableName(String sourceTableName) {
		String result = null; 
		if (sourceTableName!=null && targetTables!=null){
			for (DataTable t:targetTables){
				if (sourceTableName.equalsIgnoreCase(t.getName())){
					result = t.getName();
					break;
				}
			}
		}
		if (result == null){
			String errorMsg = "Target Table for "+sourceTableName+" is empty or not existed!";
			log.error(errorMsg);
		}
		return result;
	}
	
	private boolean isIgnoreTables(String tableName) {
		boolean result = false;
		String[] ingoreTables = getIgnoreTables();
		if (ingoreTables != null) {
			for (int i = 0; i < ingoreTables.length; i++) {
				if (ingoreTables[i] != null && ingoreTables[i].equalsIgnoreCase(tableName)) {
					result = true;
					break;
				}
			}
		}
		return result;
	}
	
	private boolean isIgnoreBigTables(float size) {
		float bigsize = config.getIngoreBigTablesSizeThanMb();
		boolean result = false;
		if (bigsize != 0) {
			result = size > bigsize;
		}
		return result;
	}
	
	private String[] getIgnoreTables(){
		String tables = config.getIngoreTables();
		String[] result = null;
		if (tables!=null && !"empty".equalsIgnoreCase(tables) && !"null".equalsIgnoreCase(tables) && !"false".equalsIgnoreCase(tables) && !"no".equalsIgnoreCase(tables)){
			tables = tables.replace(" ", "");
			tables = tables.replace("\"", "");
			tables = tables.replace(";", ",");
			tables = tables.replace(":", ",");
			result = tables.split(",");
		}
		return result;
	}

	private float[] getSplitSizeFromConfig() {
		float[] splitSize = null;
		String splitTemp = config.getJobthreadSplitTableSizeMb();
		if (splitTemp != null && !splitTemp.trim().equals("")) {
			splitTemp = splitTemp.replace(" ", "");
			splitTemp = splitTemp.replace("\"", "");
			splitTemp = splitTemp.replace(";", ",");
			splitTemp = splitTemp.replace(":", ",");
			String[] splits = splitTemp.split(",");

			if (splits != null && splits.length > 0) {
				splitSize = new float[splits.length];
				for (int i = 0; i < splits.length; i++) {
					try {
						splitSize[i] = Float.parseFloat(splits[i]);
					} catch (Exception e) {
						log.error(e.getMessage(), e);
					}
				}
			}
		}

		return splitSize;
	}
	
	private int[] getSplitIndexFromConfig() {
		int[] splitIndexes = null;
		String splitTemp = config.getJobthreadSplitTableIndexes();
		if (splitTemp != null && !splitTemp.trim().equals("")) {
			splitTemp = splitTemp.replace(" ", "");
			splitTemp = splitTemp.replace("\"", "");
			splitTemp = splitTemp.replace(";", ",");
			splitTemp = splitTemp.replace(":", ",");
			String[] splits = splitTemp.split(",");
			
			if (splits != null && splits.length > 0) {
				splitIndexes = new int[splits.length];
				for (int i = 0; i < splits.length; i++) {
					try {
						splitIndexes[i] = Integer.parseInt(splits[i]);
					} catch (Exception e) {
						log.error(e.getMessage(), e);
					}
				}
			}
		}
		
		return splitIndexes;
	}

	private synchronized void outputMigrationStatusToFile() throws IOException {
		String datestr = new SimpleDateFormat("yyyyMMdd-mm").format(new Date());
		File file = new File(config.getDataxToolFolder() + "/reports");
		if (!file.exists()){
			file.mkdirs();
		}
		
		file = new File(config.getDataxToolFolder() + "/reports/Migration-status_"+datestr+".csv");
		
		BufferedWriter out = new BufferedWriter(new FileWriter(file));
		if (allTableStatus != null && !allTableStatus.isEmpty()) {
			out.write("TableName,Size_MB,RecordsCount,MigrationRecords,Finished,HasException,CostTime,RateSpeed,RecordSpeed,ReadWriteRecords,ExceptionRecords,FinishedRecords\n");
			for (DataTableStatus ta : allTableStatus) {
				String finished = ta.isFinished()?"Yes":"No";
				String isHasException = ta.isHasException()?"Yes":"No";
				out.write(ta.getName() + "," + ta.getSize() + "," + ta.getCount() + "," + ta.getPendingRecords() +  "," + finished + "," + isHasException + "," + ta.getCostTime() + "," + ta.getReadWriteRateSpeed() + "," + ta.getReadWriteRecordSpeed() + "," + ta.getReadWriteRecords() + "," + ta.getReadWriteFailRecords() +"," + ta.getFinishedRecords() + "\n");
			}
		}
		out.close();
	}
	
	/*public static void main(String[] str){
		String line="2016-11-08 15:57:52.270 [job-0] INFO  StandAloneJobContainerCommunicator - Total 54 records, 835 bytes | Speed 69B/s, 4 records/s | Error 2 records, 0 bytes |  All Task WaitWriterTime 0.010s |  All Task WaitReaderTime 1.101s | Percentage 100.00%";
		int countFail = Integer.parseInt(line.substring(line.indexOf("Error")+5, line.indexOf("records", line.indexOf("Error"))).trim());
		String speed = line.substring(line.indexOf("Speed")+5, line.indexOf("B/s")+3).trim();
		String speedRec = line.substring(line.indexOf(",", line.indexOf("B/s"))+1, line.indexOf("records/s")+9).trim();
		System.out.println((line.substring(line.indexOf("Total")+5, line.indexOf("records")).trim()));
		System.out.println(countFail);
		System.out.println(speed);
		System.out.println(speedRec);
		
		line = " 2016-11-08 15:57:44.936 [taskGroup-0] INFO  TaskGroupContainer - taskGroup[0] taskId[0] is successed, used[2131]ms";
		int count = Integer.parseInt(line.substring(line.indexOf("used")+5, line.indexOf("ms")-1).trim());
		System.out.println(count+"ms");
		
		line = "任务总计耗时                    :                 23s";
		System.out.println(line.split("  :  ")[1].trim().matches("\\d+s"));
		line = "任务平均流量                    :               76B/s";
		System.out.println(line.split("  :  ")[1].trim().matches("\\d+[A-Za-z]+\\/s"));
		line = "记录写入速度                    :              0rec/s";
		System.out.println(line.split("  :  ")[1].trim().matches("\\d+rec\\/s"));
	}*/

	class StreamCommandOutputThread extends Thread {
		private InputStream is;
		private String threadString;
		private StringBuffer stringOutput;
		private DataTableStatus tableStatus;

		public StreamCommandOutputThread(InputStream is, String threadString, DataTableStatus tableStatus, StringBuffer stringOutput) {
			this.is = is;
			this.stringOutput = stringOutput;
			this.tableStatus = tableStatus;
			this.threadString = threadString;
		}

		public void run() {
			String line = null;
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			boolean hasException = false;
			try {
				while ((line = br.readLine()) != null) {
					stringOutput.append(threadString+line).append("\n");
					if (!hasException && line!=null && (line.contains("DataXException") || line.contains("SQLException"))){
						hasException = true;
						tableStatus.setHasException(true);
					}
					readExecueOutputLineStatus(line, tableStatus);
				}
				stringOutput.append(threadString+"execute finished in streamCommandthread!");
			} catch (IOException ioe) {
				log.error(ioe.getMessage(), ioe);
				hasException = true;
				tableStatus.setHasException(true);
			} finally {
				try {
					br.close();
					isr.close();
				} catch (IOException e) {
					log.error(e.getMessage(), e);
					hasException = true;
					tableStatus.setHasException(true);
				}
			}
		}
	}
}