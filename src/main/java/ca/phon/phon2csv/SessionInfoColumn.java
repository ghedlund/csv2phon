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

import java.time.format.DateTimeFormatter;

import ca.phon.session.Record;
import ca.phon.session.Session;

public class SessionInfoColumn implements CSVExportColumn {

	private String field = "Name";
	
	public SessionInfoColumn() {
		
	}
	
	public SessionInfoColumn(String field) {
		this.field = field;
	}
	
	@Override
	public String getData(Session t, Record utt) {
		String retVal = "";
		
		if(field.equalsIgnoreCase("Name")) {
			retVal = t.getCorpus() + "." +  t.getName();
		} else if(field.equalsIgnoreCase("Corpus")) {
			retVal = t.getCorpus();
		} else if(field.equalsIgnoreCase("ID")) {
			retVal = t.getName();
		} else if(field.equalsIgnoreCase("Media")) {
			retVal = (t.getMediaLocation() != null ? t.getMediaLocation() : "");
		} else if(field.equalsIgnoreCase("Date")) {
			if(t.getDate() != null) {
				final DateTimeFormatter dateFormatter = 
						DateTimeFormatter.ofPattern("yyyy-MM-dd");
				retVal = dateFormatter.format(t.getDate());
			}
		} else {
			retVal = "";
		}
		return retVal;
	}

	@Override
	public String getTitle() {
		return "Session:" + field;
	}

}
