/*
 * Copyright (C) 2012-2018 Gregory Hedlund
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.phon.phon2csv;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import au.com.bytecode.opencsv.CSVWriter;
import ca.phon.session.Record;
import ca.phon.session.RecordFilter;
import ca.phon.session.Records;
import ca.phon.session.Session;
import ca.phon.util.OSInfo;

/**
 * Export a session to CSV with a given column
 * configuration.
 *
 */
public class CSVExporter {
	
	private final static Logger LOGGER =
			Logger.getLogger(CSVExporter.class.getName());
	
	/** Report columns */
	private List<CSVExportColumn> exportColumns = 
		new ArrayList<CSVExportColumn>();
	
	private RecordFilter recordFilter = null;

	/**
	 * Constructor
	 * 
	 */
	public CSVExporter() {
		
	}
	
	public CSVExporter(List<CSVExportColumn> cols) {
		this.exportColumns.addAll(cols);
	}
	
	public RecordFilter getRecordFilter() {
		return recordFilter;
	}

	public void setRecordFilter(RecordFilter recordFilter) {
		this.recordFilter = recordFilter;
	}

	public void setExportColumns(List<CSVExportColumn> cols) {
		this.exportColumns.clear();
		this.exportColumns.addAll(cols);
	}
	
	/**
	 * Export session to the specified output directory.
	 * 
	 * @param t
	 * @param outputDirectory
	 */
	public void exportSession(Session t, String outFile) {
		File exportFile = new File(outFile);
		if(exportFile.exists()) {
			LOGGER.warning("Overwriting file '" + exportFile.getAbsolutePath() + "'");
		}
		
		try {
			LOGGER.info("Exporting session '" + t.getCorpus() + "." + t.getName() +
					"' to '.../" + exportFile.getName() + "'");
			
			OutputStreamWriter fWriter = 
				new OutputStreamWriter(new FileOutputStream(exportFile), "UTF-8");
			
			CSVWriter writer = new CSVWriter(fWriter, ',', '\"', 
					(OSInfo.isWindows() ? "\r\n" : "\n"));
			
			// write header line
			List<String> line = new ArrayList<String>();
			for(CSVExportColumn col:exportColumns) {
				String title = col.getTitle();
				if(title.startsWith("Tier:")) {
					title = title.substring(5);
				}
				line.add(title);
			}
			writer.writeNext(line.toArray(new String[0]));
			
			Records utts = t.getRecords();
			
			// write record info
			for(Record utt:utts) {
				
				if(recordFilter != null && !recordFilter.checkRecord(utt)) continue;
				
				line = new ArrayList<String>();
				
				for(CSVExportColumn col:exportColumns) {
					line.add(col.getData(t, utt));
				}
				
				writer.writeNext(line.toArray(new String[0]));
			}
			writer.flush();
			writer.close();
		} catch (UnsupportedEncodingException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} catch (FileNotFoundException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}
	
}
