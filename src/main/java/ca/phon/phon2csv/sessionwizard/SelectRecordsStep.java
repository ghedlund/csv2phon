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
package ca.phon.phon2csv.sessionwizard;

import java.awt.BorderLayout;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ca.phon.app.session.RecordFilterPanel;
import ca.phon.project.Project;
import ca.phon.session.RecordFilter;
import ca.phon.session.Session;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.nativedialogs.FileFilter;
import ca.phon.ui.text.FileSelectionField;
import ca.phon.ui.text.FileSelectionField.SelectionMode;
import ca.phon.ui.wizard.WizardStep;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Select save location and records for export.
 */
public class SelectRecordsStep extends WizardStep {

	private static final long serialVersionUID = 4500150320809208730L;

	/*
	 * UI 
	 */
	private DialogHeader header;
	private FileSelectionField saveLocationField;
	
	private RecordFilterPanel uttFilterPanel;

	private Project project;
	
	/*
	 * Transcript
	 */
	private Session transcript;
	
	public SelectRecordsStep(Project project, Session t) {
		this.transcript = t;
		this.project = project;
		
		init();
	}
	
	private String getDefaultSaveLocation() {
		String retVal = 
			System.getProperty("user.home") + File.separator + "Desktop" + File.separator + 
				transcript.getCorpus() + "-" + transcript.getName() + ".csv";
		return retVal;
	}
	
	private void init() {
		setLayout(new BorderLayout());
		
		header = new DialogHeader("CSV Export", "Select records and location for export.");
		add(header, BorderLayout.NORTH);
		
		JPanel centerPanel = new JPanel(new BorderLayout());
		
		JPanel dirPanel = new JPanel();
		FormLayout layout = new FormLayout(
				"right:pref, 3dlu, fill:pref:grow, pref",
				"pref, 3dlu, pref");
		CellConstraints cc = new CellConstraints();
		dirPanel.setLayout(layout);
		dirPanel.setBorder(BorderFactory.createTitledBorder("Location"));
		
		dirPanel.add(new JLabel("Save as:"), cc.xy(1,1));
		saveLocationField = new FileSelectionField();
		saveLocationField.setMode(SelectionMode.FILES);
		saveLocationField.getTextField().setEditable(false);
		saveLocationField.setFileFilter(FileFilter.csvFilter);
		saveLocationField.setFile(new File(getDefaultSaveLocation()));
		
		dirPanel.add(saveLocationField, cc.xyw(3,1,2));
		
		centerPanel.add(dirPanel, BorderLayout.NORTH);
		
		// filter panel
		uttFilterPanel = new RecordFilterPanel(project, transcript);
		uttFilterPanel.setBorder(BorderFactory.createTitledBorder("Records"));
		
		centerPanel.add(uttFilterPanel, BorderLayout.CENTER);
		
		add(centerPanel, BorderLayout.CENTER);
	}
	
	public String getSaveLocation() {
		return 
				(saveLocationField.getSelectedFile() == null ? getDefaultSaveLocation() :
					saveLocationField.getSelectedFile().getAbsolutePath());
	}
	
	public RecordFilter getRecordFilter() {
		return uttFilterPanel.getRecordFilter();
	}
	
	@Override
	public boolean validateStep() {
		boolean retVal = false;
		
		retVal = 
			uttFilterPanel.validatePanel() && (saveLocationField.getSelectedFile() != null);
		
		return retVal;
	}
	
}
