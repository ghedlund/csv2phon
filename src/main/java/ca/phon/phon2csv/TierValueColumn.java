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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.phon.ipa.AlternativeTranscript;
import ca.phon.ipa.IPATranscript;
import ca.phon.session.Record;
import ca.phon.session.Session;
import ca.phon.session.SystemTierType;
import ca.phon.session.Tier;

public class TierValueColumn implements CSVExportColumn {

	private String tierName;
	
	public TierValueColumn(String tier) {
		this.tierName = tier;
	}
	
	/* (non-Javadoc)
	 * @see ca.phon.phon2csv.CSVExportColumn#getData(ca.phon.application.transcript.ITranscript, ca.phon.application.transcript.IUtterance)
	 */
	@Override
	public String getData(Session t, Record utt) {
		String retVal = "";
		// if we have an IPA tier followed by a username
		// in parenthesis, we want a blind-transcription
		String blindIPAPattern = 
			"(IPA (?:Target|Actual)) \\((\\w+)\\)";
		Pattern p = Pattern.compile(blindIPAPattern);
		Matcher m = p.matcher(tierName);
		
		if(m.matches()) {
			String tierName = m.group(1);
			String username = m.group(2);
			
			final Tier<IPATranscript> ipaTier = 
					(SystemTierType.IPATarget.getName().equals(tierName) ? utt.getIPATarget() : utt.getIPAActual());
			retVal = "";
			for(IPATranscript grp:ipaTier) {
				retVal += (retVal.length() > 0 ? " " : "") + "[";
				final AlternativeTranscript alts = grp.getExtension(AlternativeTranscript.class);
				if(alts != null && alts.containsKey(username)) {
					retVal += alts.get(username).toString();
				}
				retVal += "]";
			}
		} else {
			final Tier<String> tier = utt.getTier(tierName, String.class);
			retVal = (tier != null ? tier.toString() : "[]");
		}
		
		return retVal;
	}

	@Override
	public String getTitle() {
		return "Tier:" + tierName;
	}

}
