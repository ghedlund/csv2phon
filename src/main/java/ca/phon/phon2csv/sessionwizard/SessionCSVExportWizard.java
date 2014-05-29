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
package ca.phon.phon2csv.sessionwizard;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import ca.phon.phon2csv.CSVExportColumn;
import ca.phon.phon2csv.CSVExporter;
import ca.phon.phon2csv.wizard.CSVColumnsStep;
import ca.phon.project.Project;
import ca.phon.session.Session;
import ca.phon.ui.PhonLoggerConsole;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.wizard.WizardFrame;
import ca.phon.ui.wizard.WizardStep;
import ca.phon.worker.PhonWorker;

/**
 * Wizard for exporting a session as a csv file.
 * This CSV export allows for exporting specific 
 * records.
 * 
 */
public class SessionCSVExportWizard extends WizardFrame {
	
	private static final Logger LOGGER = Logger
			.getLogger(SessionCSVExportWizard.class.getName());
	
	/*
	 * Steps
	 */
	private SelectRecordsStep recordsStep;
	private CSVColumnsStep csvColumnsStep;  // re-use columns step from project csv export
	
	private WizardStep exportStep;
	private PhonLoggerConsole console;
	
	/* Session we are exporting */
	private Session transcript;
	
	/**
	 * Constructor
	 */
	public SessionCSVExportWizard(Project project, Session t) {
		super("CSV Export");
		this.transcript = t;
		
		putExtension(Project.class, project);
		
		init();
	}
	
	private void init() {
		this.btnCancel.setText("Close");
		this.btnFinish.setVisible(false);
		
		recordsStep = new SelectRecordsStep(getExtension(Project.class), transcript);
		recordsStep.setPrevStep(-1);
		recordsStep.setNextStep(1);
		addWizardStep(recordsStep);
		
		csvColumnsStep = new CSVColumnsStep();
		csvColumnsStep.setPrevStep(0);
		csvColumnsStep.setNextStep(2);
		addWizardStep(csvColumnsStep);
		
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
				for(CSVExportColumn col:csvColumnsStep.getColumns())
					cols.add(col);
				
				CSVExporter exporter = 
					new CSVExporter(cols);
				exporter.setRecordFilter(recordsStep.getRecordFilter());
				
				LOGGER.info("Saving file as '" + recordsStep.getSaveLocation() + "'");
				
				exporter.exportSession(transcript, recordsStep.getSaveLocation());
				
				LOGGER.info("Export complete.");
				
				SwingUtilities.invokeLater(turnOnBack);
			}
		};
		
		worker.invokeLater(r);
		worker.start();
	}

	@Override
	protected void next() {
		if(super.getCurrentStep() == csvColumnsStep) {
			// start export thread
			startExport();
		}
		super.next();
	}
}
