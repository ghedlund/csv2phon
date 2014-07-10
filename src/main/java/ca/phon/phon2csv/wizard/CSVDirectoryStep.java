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
