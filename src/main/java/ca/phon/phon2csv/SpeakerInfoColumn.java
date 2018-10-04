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

import ca.phon.session.AgeFormatter;
import ca.phon.session.Participant;
import ca.phon.session.Record;
import ca.phon.session.Session;

public class SpeakerInfoColumn implements CSVExportColumn {

	private String field = "name";
	
	public SpeakerInfoColumn() {
		
	}
	
	public SpeakerInfoColumn(String field) {
		this.field = field;
	}

	
	@Override
	public String getData(Session t, Record utt) {
		Participant speaker = utt.getSpeaker();
		if(speaker != null) {
			String retVal = "";
			if(field.equalsIgnoreCase("name")) {
				retVal = speaker.toString();
			} else if(field.equalsIgnoreCase("age")) {
				if(t.getDate() != null && speaker.getBirthDate() != null) {
					final AgeFormatter ageFormatter = new AgeFormatter();
					retVal = ageFormatter.format(speaker.getAge(t.getDate()));
				}
			} else if(field.equalsIgnoreCase("education")) {
				retVal = speaker.getEducation();
			} else if(field.equalsIgnoreCase("language")) {
				retVal = speaker.getLanguage();
			} else if(field.equalsIgnoreCase("group")) {
				retVal = speaker.getGroup();
			} else if(field.equalsIgnoreCase("sex")) {
				if(speaker.getSex() != null) {
					retVal = speaker.getSex().toString();
				}
			} else if(field.equalsIgnoreCase("role")) {
				retVal = speaker.getRole().getTitle();
			} else if(field.equalsIgnoreCase("birthday")) {
				if(speaker.getBirthDate() != null) {
					final DateTimeFormatter dateFormatter = 
							DateTimeFormatter.ofPattern("yyyy-MM-dd");
					retVal = dateFormatter.format(speaker.getBirthDate());
				}
			} else {
				retVal = "";
			}
			return retVal;
		} else {
			return "";
		}
	}

	@Override
	public String getTitle() {
		return "Speaker:" + field;
	}

}
