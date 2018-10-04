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
			
			final PhonUIAction csv2PhonAct = new PhonUIAction(Phon2CSVMenuHandler.class, "phon2CsvWizard");
			csv2PhonAct.setData(owner);
			csv2PhonAct.putValue(PhonUIAction.NAME, "Export as CSV...");
			csv2PhonAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Export Phon sessions as CSV");
			builder.addItem("./Tools", csv2PhonAct);
		} else if(owner instanceof SessionEditor) {
			JMenu fileMenu = menuBar.getMenu(0);
			if(fileMenu == null) return;
		
			final PhonUIAction saveAsCsvAct = new PhonUIAction(Phon2CSVMenuHandler.class, "session2CsvWizard");
			saveAsCsvAct.setData(owner);
			saveAsCsvAct.putValue(PhonUIAction.NAME, "Export as CSV...");
			saveAsCsvAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Save session as CSV");
			fileMenu.add(new JSeparator(), 0);
			fileMenu.add(new JMenuItem(saveAsCsvAct), 0);
		}
	}
	
	public static void phon2CsvWizard(PhonActionEvent pae) {
		final ProjectWindow pw = (ProjectWindow)pae.getData();
		final Project project = pw.getProject();
		if(project == null) return;
		
		final CSVExportWizard wizard = new CSVExportWizard(project);
		wizard.pack();
		wizard.centerWindow();
		wizard.showWizard();
	}

	public static void session2CsvWizard(PhonActionEvent pae) {
		final SessionEditor sessionEditor = (SessionEditor)pae.getData();
		
		final SessionCSVExportWizard wizard = 
				new SessionCSVExportWizard(sessionEditor.getProject(), sessionEditor.getSession());
		wizard.pack();
		wizard.centerWindow();
		wizard.setVisible(true);
	}
}
