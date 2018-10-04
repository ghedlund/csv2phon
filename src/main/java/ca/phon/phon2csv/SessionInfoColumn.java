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
