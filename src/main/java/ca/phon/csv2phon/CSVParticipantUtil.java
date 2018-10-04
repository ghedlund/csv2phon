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
package ca.phon.csv2phon;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.lang3.StringUtils;

import ca.phon.csv2phon.io.ObjectFactory;
import ca.phon.csv2phon.io.ParticipantType;
import ca.phon.csv2phon.io.SexType;
import ca.phon.session.Participant;
import ca.phon.session.ParticipantRole;
import ca.phon.session.SessionFactory;
import ca.phon.session.Sex;

public class CSVParticipantUtil {
	
	private final static Logger LOGGER = Logger
			.getLogger(CSVParticipantUtil.class.getName());

	public static ParticipantType copyPhonParticipant(ObjectFactory factory, Participant part) {
		final ParticipantType retVal = factory.createParticipantType();
		
		retVal.setId(part.getId());
		retVal.setName(part.getName());
		
		final LocalDate bday = part.getBirthDate();
		if(bday != null) {
			try {
				final DatatypeFactory df = DatatypeFactory.newInstance();
				final XMLGregorianCalendar cal = df.newXMLGregorianCalendar(
						GregorianCalendar.from(bday.atStartOfDay(ZoneId.systemDefault())));
				cal.setTimezone(DatatypeConstants.FIELD_UNDEFINED);
				retVal.setBirthday(cal);
			} catch (DatatypeConfigurationException e) {
				LOGGER.log(Level.WARNING, e.toString(), e);
			}
		}
		
		final Period age = part.getAge(null);
		if(age != null) {
			try {
				final DatatypeFactory df = DatatypeFactory.newInstance();
				final Duration ageDuration = df.newDuration(true, age.getYears(), age.getMonths(), age.getDays(), 0, 0, 0);
				retVal.setAge(ageDuration);
			} catch (DatatypeConfigurationException e) {
				LOGGER.log(Level.WARNING, e.toString(), e);
			}
		}
		
		retVal.setEducation(part.getEducation());
		retVal.setGroup(part.getGroup());
		
		final String lang = part.getLanguage();
		if(lang != null) {
			final String langs[] = lang.split(",");
			for(String l:langs) {
				retVal.getLanguage().add(StringUtils.strip(l));
			}
		}

		retVal.setSex(part.getSex() == Sex.MALE ? SexType.MALE : SexType.FEMALE);
		
		ParticipantRole prole = part.getRole();
		if(prole == null)
			prole = ParticipantRole.TARGET_CHILD;
		retVal.setRole(prole.toString());
		
		retVal.setSES(part.getSES());
			
		return retVal;
	}
	
	public static Participant copyXmlParticipant(SessionFactory factory, ParticipantType pt,
			LocalDate sessionDate) {
		final Participant retVal = factory.createParticipant();
		
		retVal.setId(pt.getId());
		retVal.setName(pt.getName());
		
		final XMLGregorianCalendar bday = pt.getBirthday();
		if(bday != null) {
			final LocalDate bdt = 
					LocalDate.of(bday.getYear(), bday.getMonth(), bday.getDay());
			retVal.setBirthDate(bdt);
			
			// calculate age up to the session date
			final Period period = Period.between(bdt, sessionDate);
			retVal.setAgeTo(period);
		}
		
		final Duration ageDuration = pt.getAge();
		if(ageDuration != null) {
			// convert to period
			final Period age = Period.parse(ageDuration.toString());
			retVal.setAge(age);
		}
		
		retVal.setEducation(pt.getEducation());
		retVal.setGroup(pt.getGroup());
		
		String langs = "";
		for(String lang:pt.getLanguage())
			langs += (langs.length() > 0 ? ", " : "") + lang;
		retVal.setLanguage(langs);

		retVal.setSex(pt.getSex() == SexType.MALE ? Sex.MALE : Sex.FEMALE);
		
		ParticipantRole prole = ParticipantRole.fromString(pt.getRole());
		if(prole == null)
			prole = ParticipantRole.TARGET_CHILD;
		retVal.setRole(prole);
		
		retVal.setSES(pt.getSES());
			
		return retVal;
	}
	
}
