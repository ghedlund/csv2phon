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
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import ca.phon.phon2csv.CSVExportColumn;
import ca.phon.phon2csv.CSVExporter;
import ca.phon.project.Project;
import ca.phon.session.Session;
import ca.phon.session.SessionPath;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.PhonLoggerConsole;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.wizard.WizardFrame;
import ca.phon.ui.wizard.WizardStep;
import ca.phon.worker.PhonWorker;

/**
 * Wizard for exporting sessions in CSV format.
 */
public class CSVExportWizard extends WizardFrame {
	
	private static final Logger LOGGER = Logger
			.getLogger(CSVExportWizard.class.getName());
	
	/*
	 * UI
	 */
	private CSVDirectoryStep dirStep;
	private CSVColumnsStep colStep;
	private WizardStep exportStep;
	private PhonLoggerConsole console;
	
	/**
	 * Constructor
	 */
	public CSVExportWizard(Project project) {
		super("CSV Export");
		putExtension(Project.class, project);

		setWindowName("CSV Export");
		init();
	}
	
	private void init() {
		this.btnCancel.setText("Close");
		this.btnFinish.setVisible(false);
		
		dirStep = new CSVDirectoryStep();
		dirStep.setNextStep(1);
		dirStep.setPrevStep(-1);
		addWizardStep(dirStep);
		
		colStep = new CSVColumnsStep();
		colStep.setNextStep(2);
		colStep.setPrevStep(0);
		addWizardStep(colStep);
		
		exportStep = createExportStep();
		exportStep.setNextStep(-1);
		exportStep.setPrevStep(1);
	}

	private WizardStep createExportStep() {
		JPanel exportPanel = new JPanel(new BorderLayout());
		
		DialogHeader exportHeader = new DialogHeader("CSV Export", "Exporting data.");
		exportPanel.add(exportHeader, BorderLayout.NORTH);
		
		JPanel consolePanel = new JPanel(new BorderLayout());
		
		console = new PhonLoggerConsole();
		console.addLogger(Logger.getLogger("ca.phon.phon2csv"));
		consolePanel.add(console, BorderLayout.CENTER);
		
		exportPanel.add(consolePanel, BorderLayout.CENTER);
		
		return super.addWizardStep(exportPanel);
	}
	
	private Project getProject() {
		return getExtension(Project.class);
	}
	
	private void startExport() {
		PhonWorker worker = PhonWorker.createWorker();
		worker.setName("CSV Exporter");
		worker.setFinishWhenQueueEmpty(true);
		
		Runnable r = new Runnable() {
			@Override
			public void run() {
				Runnable turnOffBack = new Runnable() {
					@Override
					public void run() {
						btnBack.setEnabled(false);
						btnCancel.setEnabled(false);
						showBusyLabel(console);
					}
				};
				Runnable turnOnBack = new Runnable() {
					@Override
					public void run() {
						btnBack.setEnabled(true);
						btnCancel.setEnabled(true);
						stopBusyLabel();
					}
				};
				SwingUtilities.invokeLater(turnOffBack);

				List<CSVExportColumn> cols = 
					new ArrayList<CSVExportColumn>();
				for(CSVExportColumn col:colStep.getColumns())
					cols.add(col);
				
				CSVExporter exporter = 
					new CSVExporter(cols);
				
				Project proj = getProject();
				LOGGER.info("Writing files to directory '" + dirStep.getBase() + "'");
				LOGGER.info("Files will be written using encoding 'UTF-8'");
				
				List<SessionPath> sessions = dirStep.getSelectedSessions();
				for(SessionPath loc:sessions) {
					try {
						Session t = proj.openSession(loc.getCorpus(), loc.getSession());
						
						exporter.exportSession(t, dirStep.getBase() + 
								File.separator + t.getCorpus() + "-" + t.getName() + ".csv");
						
					} catch (IOException e) {
						LOGGER.log(Level.SEVERE,
								e.getLocalizedMessage(), e);
					}
				}
				
				LOGGER.info("Export complete.");
				
				SwingUtilities.invokeLater(turnOnBack);
			}
		};
		
		worker.invokeLater(r);
		worker.start();
	}

	@Override
	protected void next() {
		if(super.getCurrentStep() == colStep) {
			// start export thread
			startExport();
		}
		super.next();
	}

}
