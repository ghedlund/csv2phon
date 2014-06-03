package ca.phon.csv2phon;

import java.awt.Window;

import javax.swing.JMenu;
import javax.swing.JMenuBar;

import ca.phon.app.project.ProjectFrame;
import ca.phon.app.project.ProjectWindow;
import ca.phon.csv2phon.wizard.CSVImportWizard;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.IPluginMenuFilter;
import ca.phon.plugin.PhonPlugin;
import ca.phon.project.Project;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;

@PhonPlugin(name="csv2phon",
	author="Greg J. Hedlund",
	version="1-SNAPSHOT",
	minPhonVersion="1.7.0")
public class CSV2PhonMenuHandler 
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
				return CSV2PhonMenuHandler.this;
			}
			
		};
	}

	@Override
	public void filterWindowMenu(Window owner, JMenuBar menuBar) {
		if(!(owner instanceof ProjectWindow)) return;
		final CommonModuleFrame cmf = (CommonModuleFrame)owner;
		
		JMenu pluginsMenu = null;
		for(int i = 0; i < menuBar.getMenuCount(); i++) {
			final JMenu menu = menuBar.getMenu(i);
			if(menu.getText().equals("Plugins")) {
				pluginsMenu = menu;
				break;
			}
		}
		
		if(pluginsMenu != null) {
			final PhonUIAction csv2PhonAct = new PhonUIAction(this, "cvs2PhonWizard", cmf);
			csv2PhonAct.putValue(PhonUIAction.NAME, "Import from CSV...");
			csv2PhonAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Import CSV files as new Sessions in the current project.");
			pluginsMenu.add(csv2PhonAct);
		}
	}
	
	public void cvs2PhonWizard(PhonActionEvent pae) {
		final ProjectWindow pw = (ProjectWindow)pae.getData();
		final Project project = pw.getProject();
		if(project == null) return;
		
		final CSVImportWizard wizard = new CSVImportWizard(project);
		wizard.pack();
		wizard.centerWindow();
		wizard.showWizard();
	}

}
