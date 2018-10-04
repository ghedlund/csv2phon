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
package ca.phon.phon2csv.wizard;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import ca.phon.app.session.SessionSelector;
import ca.phon.project.Project;
import ca.phon.session.SessionPath;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.text.FileSelectionField;
import ca.phon.ui.text.FileSelectionField.SelectionMode;
import ca.phon.ui.wizard.WizardStep;
import ca.phon.util.PhonConstants;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Select output directory and session for csv export.
 */
public class CSVDirectoryStep extends WizardStep {

	/** UI */
	private DialogHeader header;
	private JLabel infoLbl;
	private FileSelectionField csvDirField;
	
	private SessionSelector sessionSelector;
	
	/** Output directory */
	private String base = System.getProperty("user.home") + File.separator + "Desktop";
	
	public CSVDirectoryStep() {
		
		init();
	}

	private void init() {
		setLayout(new BorderLayout());
		
		header = new DialogHeader("CSV Export", "Select destination folder.");
		add(header, BorderLayout.NORTH);
		
		JPanel centerPanel = new JPanel(new BorderLayout());
		
		JPanel dirPanel = new JPanel();
		FormLayout layout = new FormLayout(
				"right:pref, 3dlu, fill:pref:grow, pref",
				"pref, 3dlu, pref");
		CellConstraints cc = new CellConstraints();
		dirPanel.setLayout(layout);
		dirPanel.setBorder(BorderFactory.createTitledBorder("Folder"));
		
		String lblTxt = "<html><body><p>" +
				"Select folder for exported csv files." +
				"</p></body></html>";
		infoLbl = new JLabel(lblTxt);
		dirPanel.add(infoLbl, cc.xyw(1,1,4));
		
		dirPanel.add(new JLabel("Destination folder:"), cc.xy(1,3));
		
		csvDirField = new FileSelectionField();
		csvDirField.setMode(SelectionMode.FOLDERS);
		csvDirField.getTextField().setEditable(false);
		
		dirPanel.add(csvDirField, cc.xyw(3,3,2));
		
		centerPanel.add(dirPanel, BorderLayout.NORTH);
		
		// session selection
		JPanel sessionPanel = new JPanel(new BorderLayout());
		sessionPanel.setBorder(BorderFactory.createTitledBorder("Sessions"));
		sessionSelector = new SessionSelector(CommonModuleFrame.getCurrentFrame().getExtension(Project.class));
		sessionPanel.add(new JScrollPane(sessionSelector),BorderLayout.CENTER);
		
		centerPanel.add(sessionPanel, BorderLayout.CENTER);
		
		add(centerPanel, BorderLayout.CENTER);
	}
	
	public List<SessionPath> getSelectedSessions() {
		return sessionSelector.getSelectedSessions();
	}
	
	public String getBase() {
		return (csvDirField.getSelectedFile() != null ?
				csvDirField.getSelectedFile().getAbsolutePath() : null);
	}
	
}
