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

import java.awt.Window;
import java.util.logging.Logger;

import javax.swing.*;

import ca.phon.app.project.ProjectWindow;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.phon2csv.sessionwizard.SessionCSVExportWizard;
import ca.phon.phon2csv.wizard.CSVExportWizard;
import ca.phon.plugin.*;
import ca.phon.project.Project;
import ca.phon.ui.action.*;
import ca.phon.ui.menu.MenuBuilder;

@PhonPlugin(name="phon2csv",
	author="Greg J. Hedlund",
	version="1-SNAPSHOT",
	minPhonVersion="1.7.0")
public class Phon2CSVMenuHandler 
	implements IPluginMenuFilter, IPluginExtensionPoint<IPluginMenuFilter> {

	private final static Logger LOGGER = Logger.getLogger(Phon2CSVMenuHandler.class.getName());
	
	@Override
	public Class<?> getExtensionType() {
		return IPluginMenuFilter.class;
	}

	@Override
	public IPluginExtensionFactory<IPluginMenuFilter> getFactory() {
		return new IPluginExtensionFactory<IPluginMenuFilter>() {
			
			@Override
			public IPluginMenuFilter createObject(Object... args) {
				return Phon2CSVMenuHandler.this;
			}
			
		};
	}

	@Override
	public void filterWindowMenu(Window owner, JMenuBar menuBar) {
		if(owner instanceof ProjectWindow) {
			final MenuBuilder builder = new MenuBuilder(menuBar);
			
			final PhonUIAction<ProjectWindow> csv2PhonAct = PhonUIAction.eventConsumer(Phon2CSVMenuHandler::phon2CsvWizard, (ProjectWindow) owner);
			csv2PhonAct.putValue(PhonUIAction.NAME, "Export as CSV...");
			csv2PhonAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Export Phon sessions as CSV");
			builder.addItem("./Tools", csv2PhonAct);
		} else if(owner instanceof SessionEditor) {
			JMenu fileMenu = menuBar.getMenu(0);
			if(fileMenu == null) return;
		
			final PhonUIAction<SessionEditor> saveAsCsvAct = PhonUIAction.eventConsumer(Phon2CSVMenuHandler::session2CsvWizard, (SessionEditor) owner);
			saveAsCsvAct.putValue(PhonUIAction.NAME, "Export session (CSV)...");
			saveAsCsvAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Export session as CSV");
			fileMenu.add(new JSeparator(), 0);
			fileMenu.add(new JMenuItem(saveAsCsvAct), 0);
		}
	}
	
	public static void phon2CsvWizard(PhonActionEvent<ProjectWindow> pae) {
		final ProjectWindow pw = pae.getData();
		final Project project = pw.getProject();
		if(project == null) return;
		
		final CSVExportWizard wizard = new CSVExportWizard(project);
		wizard.pack();
		wizard.centerWindow();
		wizard.showWizard();
	}

	public static void session2CsvWizard(PhonActionEvent<SessionEditor> pae) {
		final SessionEditor sessionEditor = (SessionEditor)pae.getData();
		
		final SessionCSVExportWizard wizard = 
				new SessionCSVExportWizard(sessionEditor.getProject(), sessionEditor.getSession());
		wizard.pack();
		wizard.centerWindow();
		wizard.setVisible(true);
	}
}
