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
package ca.phon.csv2phon.wizard;

import ca.phon.csv2phon.io.ImportDescriptionType;
import ca.phon.csv2phon.io.ObjectFactory;
import ca.phon.ui.wizard.WizardStep;

public class CSVImportStep extends WizardStep {

	private ImportDescriptionType settings;
	
	public ImportDescriptionType getSettings() {
		return settings;
	}

	public void setSettings(ImportDescriptionType settings) {
		this.settings = settings;
	}

	public CSVImportStep() {
		// avoid null pointers
		ObjectFactory factory = new ObjectFactory();
		settings = factory.createImportDescriptionType();
	}
	
}
