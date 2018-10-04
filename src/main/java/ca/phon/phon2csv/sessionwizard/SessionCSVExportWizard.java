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
