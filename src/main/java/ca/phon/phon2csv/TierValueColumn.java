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
		Pattern blindPattern = Pattern.compile(blindIPAPattern);
		Matcher blindMatcher = blindPattern.matcher(tierName);
		
		String syllabifiedIPAPattern =
			"(IPA (?:Target|Actual)) Syllabified";
		Pattern syllabifiedPattern = Pattern.compile(syllabifiedIPAPattern);
		Matcher syllabifiedMatcher = syllabifiedPattern.matcher(tierName);
		
		if(blindMatcher.matches()) {
			String tierName = blindMatcher.group(1);
			String username = blindMatcher.group(2);
			
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
		} else if(syllabifiedMatcher.matches()) {
			String tierName = syllabifiedMatcher.group(1);
			
			final Tier<IPATranscript> ipaTier = 
					(SystemTierType.IPATarget.getName().equals(tierName) ? utt.getIPATarget() : utt.getIPAActual());
			StringBuffer sb = new StringBuffer();
			for(IPATranscript grpVal:ipaTier) {
				if(sb.length() > 0) sb.append(' ');
				sb.append('[').append(grpVal.toString(true)).append(']');
			}
			retVal = sb.toString();
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
