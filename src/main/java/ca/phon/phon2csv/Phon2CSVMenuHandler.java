package ca.phon.phon2csv;

import java.awt.Window;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import ca.phon.app.project.ProjectFrame;
import ca.phon.app.project.ProjectWindow;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.actions.SaveSessionAction;
import ca.phon.phon2csv.sessionwizard.SessionCSVExportWizard;
import ca.phon.phon2csv.wizard.CSVExportWizard;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.IPluginMenuFilter;
import ca.phon.project.Project;
import ca.phon.session.Session;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;

public class Phon2CSVMenuHandler 
	implements IPluginMenuFilter, IPluginExtensionPoint<IPluginMenuFilter> {

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
			JMenu pluginsMenu = null;
			for(int i = 0; i < menuBar.getMenuCount(); i++) {
				final JMenu menu = menuBar.getMenu(i);
				if(menu.getText().equals("Plugins")) {
					pluginsMenu = menu;
					break;
				}
			}
			
			if(pluginsMenu != null) {
				final PhonUIAction csv2PhonAct = new PhonUIAction(this, "phon2CsvWizard", owner);
				csv2PhonAct.putValue(PhonUIAction.NAME, "Export to CSV...");
				csv2PhonAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Export Phon sessions as CSV");
				pluginsMenu.add(csv2PhonAct);
			}
		} else if(owner instanceof SessionEditor) {
			JMenu fileMenu = menuBar.getMenu(0);
			
			final PhonUIAction saveAsCsvAct = new PhonUIAction(this, "session2CsvWizard", owner);
			saveAsCsvAct.putValue(PhonUIAction.NAME, "Save as CSV...");
			saveAsCsvAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Save session as CSV");
			fileMenu.add(new JMenuItem(saveAsCsvAct), 0);
		}
	}
	
	public void phon2CsvWizard(PhonActionEvent pae) {
		final ProjectWindow pw = (ProjectWindow)pae.getData();
		final Project project = pw.getProject();
		if(project == null) return;
		
		final CSVExportWizard wizard = new CSVExportWizard(project);
		wizard.pack();
		wizard.centerWindow();
		wizard.showWizard();
	}

	public void session2CsvWizard(PhonActionEvent pae) {
		final SessionEditor sessionEditor = (SessionEditor)pae.getData();
		
		final SessionCSVExportWizard wizard = 
				new SessionCSVExportWizard(sessionEditor.getProject(), sessionEditor.getSession());
		wizard.pack();
		wizard.centerWindow();
		wizard.setVisible(true);
	}
}
