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

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Unmarshaller;

import ca.phon.app.log.BufferPanel;
import ca.phon.app.log.LogBuffer;
import ca.phon.csv2phon.CSVImporter;
import ca.phon.csv2phon.io.FileType;
import ca.phon.csv2phon.io.ImportDescriptionType;
import ca.phon.csv2phon.io.ObjectFactory;
import ca.phon.project.Project;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.toast.ToastFactory;
import ca.phon.ui.wizard.WizardFrame;
import ca.phon.ui.wizard.WizardStep;
import ca.phon.worker.PhonTask;
import ca.phon.worker.PhonWorker;

/**
 * CSV import wizard.
 *
 */
public class CSVImportWizard extends WizardFrame {
	
	private final static Logger LOGGER = Logger
			.getLogger(CSVImportWizard.class.getName());

	/** CSV Import settings */
	private ImportDescriptionType importDescription;
	
	/** Steps */
	private DirectoryStep dirStep;
	private SessionInfoStep sessionInfoStep;
	private ParticipantsStep partsStep;
	private ColumnMapStep columnMapStep;
	private WizardStep importStep;
	
	private BufferPanel bufferPanel;
	
	private final String settingsFileName = "importsettings.xml";
	
	private final Project project;
	
	private ImportTask importTask;
	
	public CSVImportWizard(Project project) {
		super("CSV Import");
		
		this.project = project;
		
		setWindowName("CSV Import");
		super.btnFinish.setVisible(false);
		super.btnCancel.setText("Close");
		
		init();
		
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				if(importTask != null) {
					importTask.shutdown();
				}
			}
			
		});
	}
	
	public void setSingleFile(boolean singleFile) {
		if(singleFile) {
			removeWizardStep(dirStep);
			removeWizardStep(sessionInfoStep);
			removeWizardStep(importStep);
			
			partsStep.setPrevStep(-1);
			partsStep.setNextStep(1);
			
			columnMapStep.setPrevStep(0);
			columnMapStep.setNextStep(-1);
		} else {
			super.removeAllSteps();
			init();
		}
	}
	
	private void init() {
		ObjectFactory factory = new ObjectFactory();
		importDescription = factory.createImportDescriptionType();
		
		dirStep = new DirectoryStep();
		dirStep.setPrevStep(-1);
		dirStep.setNextStep(1);
		addWizardStep(dirStep);
		
		sessionInfoStep = new SessionInfoStep();
		sessionInfoStep.setPrevStep(0);
		sessionInfoStep.setNextStep(2);
		addWizardStep(sessionInfoStep);
		
		partsStep = new ParticipantsStep();
		partsStep.setPrevStep(1);
		partsStep.setNextStep(3);
		addWizardStep(partsStep);
		
		columnMapStep = new ColumnMapStep();
		columnMapStep.setPrevStep(2);
		columnMapStep.setNextStep(4);
		addWizardStep(columnMapStep);
		
		importStep = createImportStep();
		importStep.setPrevStep(3);
		importStep.setNextStep(-1);
	}
	
	private WizardStep createImportStep() {
		JPanel importPanel = new JPanel(new BorderLayout());
		
		DialogHeader importHeader = new DialogHeader("CSV Import", "Importing data.");
		importPanel.add(importHeader, BorderLayout.NORTH);
		
		JPanel consolePanel = new JPanel(new BorderLayout());
		
		bufferPanel = new BufferPanel("CVS Import");
		consolePanel.add(bufferPanel, BorderLayout.CENTER);
		
		importPanel.add(consolePanel, BorderLayout.CENTER);
		
		return super.addWizardStep(importPanel);
	}
	
	private void startImport() throws IOException {
		PhonWorker worker = PhonWorker.createWorker();
		worker.setName("CSV Importer");
		worker.setFinishWhenQueueEmpty(true);
		
		final BufferedWriter out = new BufferedWriter(new OutputStreamWriter(bufferPanel.getLogBuffer().getStdOutStream(), "UTF-8"));
	    importTask = new ImportTask(out);
		
		worker.invokeLater(importTask);
		worker.start();
	}
	
	private void saveSettings() {
		// write settings to file
		try {
			File settingsFile = new File(dirStep.getBase(), "importsettings.xml");
			LOGGER.info("Saving settings to file '.../" + settingsFile.getName() + "'");
			
			JAXBContext ctx = JAXBContext.newInstance(ObjectFactory.class);
			Marshaller marshaller = ctx.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			
			JAXBElement<ImportDescriptionType> jaxbEle = 
				(new ObjectFactory()).createCsvimport(importDescription);
			marshaller.marshal(jaxbEle, settingsFile);
			
		} catch (PropertyException e) {
			e.printStackTrace();
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}
	
	public Project getProject() {
		return this.project;
	}
	
	@Override
	protected void next() {
		if(super.getCurrentStep() == dirStep) {
			sessionInfoStep.setBase(dirStep.getBase().getAbsolutePath());
			
			
			ObjectFactory factory = new ObjectFactory();
			
			// check for previous settings
			File settingsFile = new File(dirStep.getBase(), settingsFileName);
			if(settingsFile.exists()) {
				// read settings from file
				try {
					JAXBContext ctx = JAXBContext.newInstance(factory.getClass());
					Unmarshaller unmarshaller = ctx.createUnmarshaller();
					
					JAXBElement<ImportDescriptionType> jaxbEle = 
						(JAXBElement<ImportDescriptionType>)unmarshaller.unmarshal(settingsFile);
					importDescription = jaxbEle.getValue();
				} catch (JAXBException e) {
					e.printStackTrace();
					importDescription = factory.createImportDescriptionType();
				}
			} else {
				// create new empty settings

				importDescription = factory.createImportDescriptionType();
			}
			
			sessionInfoStep.setSettings(importDescription);
		} else if(super.getCurrentStep() == sessionInfoStep) {
			columnMapStep.setBase(dirStep.getBase().getAbsolutePath());
			columnMapStep.setSettings(importDescription);
			partsStep.setSettings(importDescription);
		} else if(super.getCurrentStep() == columnMapStep) {
			// start import
			try {
				startImport();
			} catch (IOException e) {
				ToastFactory.makeToast(e.getMessage()).start(columnMapStep);
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		}
		super.next();
	}
	
	private final class ImportTask extends PhonTask {
		
		private BufferedWriter out;
		
		
		public ImportTask(BufferedWriter out) {
			this.out = out;
		}

		@Override
		public void performTask() {
			Runnable turnOffBack = new Runnable() {
				@Override
				public void run() {
					btnBack.setEnabled(false);
					btnCancel.setEnabled(false);
					
					try {
						out.flush();
						out.write(LogBuffer.ESCAPE_CODE_PREFIX + BufferPanel.SHOW_BUSY);
						out.flush();
					} catch (IOException e) {}
				}
			};
			Runnable turnOnBack = new Runnable() {
				@Override
				public void run() {
					btnBack.setEnabled(true);
					btnCancel.setEnabled(true);
					
					try {
						out.flush();
						out.write(LogBuffer.ESCAPE_CODE_PREFIX + BufferPanel.STOP_BUSY);
						out.flush();
					} catch (IOException e) {}
				}
			};
			SwingUtilities.invokeLater(turnOffBack);
			
			saveSettings();
			CSVImporter importer =
				new CSVImporter(dirStep.getBase().getAbsolutePath(), importDescription, getProject());
			
			importer.setFileEncoding(dirStep.getFileEncoding());
			importer.setTextDelimChar(dirStep.getTextDelimiter().orElse('\0'));
			importer.setFieldDelimChar(dirStep.getFieldDelimiter().orElse('\0'));

			int numFilesCompleted = 0;
			for(FileType ft:importDescription.getFile()) {
				if(ft.isImport() && !isShutdown()) {
					try {
						String msg = String.format("Importing file '.../%s'", ft.getLocation());
						LOGGER.info(msg);
						out.write(msg);
						out.write("\n");
						out.flush();
						importer.importFile(ft);
						++numFilesCompleted;
					} catch (IOException e) {
						LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
						try {
							out.write("Unable to import file: " + e.getLocalizedMessage());
							out.write("\n");
							out.flush();
						} catch (IOException ex) {}
					}
				}
			}

			try {
				out.write("=============================\n");
				out.write(String.format("%d/%d files imported\n", numFilesCompleted, importDescription.getFile().size()));
				out.flush();
			} catch (IOException e) {}
			
			SwingUtilities.invokeLater(turnOnBack);
		}
		
	}
	
}
