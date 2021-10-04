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
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import ca.phon.app.log.BufferPanel;
import ca.phon.app.log.LogBuffer;
import ca.phon.phon2csv.CSVExportColumn;
import ca.phon.phon2csv.CSVExporter;
import ca.phon.project.Project;
import ca.phon.session.Session;
import ca.phon.session.SessionPath;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.PhonLoggerConsole;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.toast.ToastFactory;
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
	private BufferPanel bufferPanel;
	
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
		
		bufferPanel = new BufferPanel("CSV Export");
		
		exportPanel.add(bufferPanel, BorderLayout.CENTER);
		
		return super.addWizardStep(exportPanel);
	}
	
	private Project getProject() {
		return getExtension(Project.class);
	}
	
	private void startExport() throws IOException {
		PhonWorker worker = PhonWorker.createWorker();
		worker.setName("CSV Exporter");
		worker.setFinishWhenQueueEmpty(true);
		
		final PrintWriter out = new PrintWriter(new OutputStreamWriter(bufferPanel.getLogBuffer().getStdOutStream(), "UTF-8"));
		
		Runnable r = new Runnable() {
			@Override
			public void run() {
				Runnable turnOffBack = new Runnable() {
					@Override
					public void run() {
						btnBack.setEnabled(false);
						btnCancel.setEnabled(false);

							out.flush();
							out.write(LogBuffer.ESCAPE_CODE_PREFIX + BufferPanel.SHOW_BUSY);
							out.flush();
					}
				};
				Runnable turnOnBack = new Runnable() {
					@Override
					public void run() {
						btnBack.setEnabled(true);
						btnCancel.setEnabled(true);

							out.flush();
							out.write(LogBuffer.ESCAPE_CODE_PREFIX + BufferPanel.STOP_BUSY);
							out.flush();
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
				bufferPanel.getLogBuffer().append("Writing files to directory '" + dirStep.getBase() + "'\n");
				bufferPanel.getLogBuffer().append("Files will be written using encoding 'UTF-8'\n");

				int numFilesExported = 0;
				List<SessionPath> sessions = dirStep.getSelectedSessions();
				for(SessionPath loc:sessions) {
					try {
						Session t = proj.openSession(loc.getCorpus(), loc.getSession());
						out.println("Exporting session " + loc.toString());
						out.flush();
						exporter.exportSession(t, dirStep.getBase() + 
								File.separator + t.getCorpus() + "-" + t.getName() + ".csv");

						++numFilesExported;
					} catch (IOException e) {
						LOGGER.log(Level.SEVERE,
								e.getLocalizedMessage(), e);
						out.println("Unable to export session: " + e.getLocalizedMessage());
						out.flush();
					}
				}

				out.println("=============================");
				out.println(String.format("%d/%d sessions exported", numFilesExported, sessions.size()));
				out.flush();
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
			try {
				startExport();
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
				ToastFactory.makeToast(e.getLocalizedMessage()).start(colStep);
			}
		}
		super.next();
	}

}
