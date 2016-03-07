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
