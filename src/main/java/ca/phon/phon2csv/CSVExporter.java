/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
