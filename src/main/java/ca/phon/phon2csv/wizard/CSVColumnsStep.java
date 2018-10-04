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
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.html.HTMLEditorKit;

import org.jdesktop.swingx.JXList;

import ca.phon.phon2csv.CSVExportColumn;
import ca.phon.phon2csv.RecordNumberColumn;
import ca.phon.phon2csv.SessionInfoColumn;
import ca.phon.phon2csv.SpeakerInfoColumn;
import ca.phon.phon2csv.TierValueColumn;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.wizard.WizardStep;
import ca.phon.util.PrefHelper;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class CSVColumnsStep extends WizardStep {
	
	private static final Logger LOGGER = Logger
			.getLogger(CSVColumnsStep.class.getName());
	
	private static final String columnHistory = "ca.phon.phon2csv.columnHistory";
	
	/* Documentation string */
	private static final String columnDocString = 
		"<html><body>"
			+	"<p>To add a column to the export type its name in the provided field and press enter."
			+	"The following column names are valid:</p>"
				+	"<ul>"
					+	"<li>Record #</li>"
					+	"<li>Session:Date</li>"
					+	"<li>Session:Media</li>"
					+	"<li>Session:Name</li>"
					+	"<li>Speaker:Age</li>"
					+	"<li>Speaker:Birthday</li>"
					+	"<li>Speaker:Education</li>"
					+	"<li>Speaker:Group</li>"
					+	"<li>Speaker:Language</li>"
					+	"<li>Speaker:Name</li>"
					+	"<li>Speaker:Sex</li>"
					+	"<li>Tier:&lt;tier name&gt;</li>"
				+	"</ul>"
		+	"</body></html>";
	
	/* UI */
	private DialogHeader header;
	
	private JTextField columnEntryField;
	private JTextPane docPane;
	private JXList columnList;
	
	private JButton addColumnBtn;
	private JButton upColumnBtn;
	private JButton downColumnBtn;
	private JButton removeColumnBtn;
	
	private JButton resetDefaultBtn;
	
	private DefaultListModel reportColumnModel;
	
	/* Actions */
	private ActionListener addColumnListener = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			String colText = columnEntryField.getText();
//			columnEntryField.setText("");
			addColumn(colText);
		}
		
	};
	
	private ActionListener downColumnListener = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			int selectedIdx = columnList.getSelectedIndex();
			if(selectedIdx >= 0 && selectedIdx < reportColumnModel.getSize()-1) {
				moveColumnDown(selectedIdx);
			}
		}
		
	};
	
	private ActionListener upColumnListener = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			int selectedIdx = columnList.getSelectedIndex();
			if(selectedIdx > 0 && selectedIdx < reportColumnModel.getSize()) {
				moveColumnUp(selectedIdx);
			}			
		}
		
	};
	
	private ActionListener deleteColumnListener = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			int selectedIdx = columnList.getSelectedIndex();
			if(selectedIdx >= 0 && selectedIdx < reportColumnModel.getSize()) {
				removeColumn(selectedIdx);
			}
		}
		
	};
	
	private final String defaultColumns = 
		"Session:Name;Record #;Speaker:Name;Tier:Orthography;Tier:IPA Target;Tier:IPA Actual;Tier:Segment;Tier:Notes";
	
	private ActionListener resetColumnsListener = new ActionListener() {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			reportColumnModel.clear();
			for(CSVExportColumn col:parseColumnList(defaultColumns)) {
				reportColumnModel.addElement(col);
			}
		}
		
	};
	
	public CSVColumnsStep() {
		super();
		
		init();
	}
	
	private void init() {
		setLayout(new BorderLayout());
		
		JPanel centerPanel = new JPanel();
		FormLayout centerLayout = new FormLayout(
				"250px, fill:pref:grow, pref",
				"pref, pref, pref, pref, fill:pref:grow, pref");
		CellConstraints cc = new CellConstraints();
		centerPanel.setLayout(centerLayout);
		
		docPane = new JTextPane();
		docPane.setEditorKit(new HTMLEditorKit());
		docPane.setText(columnDocString);
		centerPanel.add(new JScrollPane(docPane), cc.xywh(1, 1, 1, 6));
		
		reportColumnModel = new DefaultListModel();
		for(CSVExportColumn rc:getInitialColumnList())
			reportColumnModel.addElement(rc);
		columnList = new JXList(reportColumnModel);
		columnList.setCellRenderer(new ReportColumnCellRenderer());
		centerPanel.add(new JScrollPane(columnList), cc.xywh(2,2,1,5));
		
		columnEntryField = new JTextField();
		columnEntryField.addActionListener(addColumnListener);
		centerPanel.add(columnEntryField, cc.xy(2, 1));
		
		ImageIcon addIcon = 
			IconManager.getInstance().getIcon("actions/list-add", IconSize.SMALL);
		ImageIcon removeIcon = 
			IconManager.getInstance().getIcon("actions/list-remove", IconSize.SMALL);
		ImageIcon upIcon = 
			IconManager.getInstance().getIcon("actions/go-up", IconSize.SMALL);
		ImageIcon downIcon = 
			IconManager.getInstance().getIcon("actions/go-down", IconSize.SMALL);
		ImageIcon resetIcon =
			IconManager.getInstance().getIcon("actions/reload", IconSize.SMALL);
	
		addColumnBtn = new JButton(addIcon);
		addColumnBtn.setToolTipText("Add column");
		addColumnBtn.addActionListener(addColumnListener);
		centerPanel.add(addColumnBtn, cc.xy(3, 1));
		
		upColumnBtn = new JButton(upIcon);
		upColumnBtn.setToolTipText("Move column up");
		upColumnBtn.addActionListener(upColumnListener);
		centerPanel.add(upColumnBtn, cc.xy(3, 3));
		
		downColumnBtn = new JButton(downIcon);
		downColumnBtn.setToolTipText("Move column down");
		downColumnBtn.addActionListener(downColumnListener);
		centerPanel.add(downColumnBtn, cc.xy(3, 4));
		
		removeColumnBtn = new JButton(removeIcon);
		removeColumnBtn.setToolTipText("Remove column");
		removeColumnBtn.addActionListener(deleteColumnListener);
		centerPanel.add(removeColumnBtn, cc.xy(3, 2));
		
		resetDefaultBtn = new JButton(resetIcon);
		resetDefaultBtn.setToolTipText("Reset to default");
		resetDefaultBtn.addActionListener(resetColumnsListener);
		centerPanel.add(resetDefaultBtn, cc.xy(3,6));
		
		header = new DialogHeader("Set up Columns", "Set up columns for csv export");
		
		add(header, BorderLayout.NORTH);
		add(centerPanel, BorderLayout.CENTER);
	}

	/**
	 * Turn a string into a ReportColumn
	 * 
	 * @param column name
	 * @return the ReportColumn or null if invalid String
	 */
	private CSVExportColumn getReportColumn(String colName) {
		CSVExportColumn retVal = null;
		
		String columnName = (colName != null ? colName.trim() : "");
		
		if(columnName.indexOf(':') >= 0) {
			String[] colParts = columnName.split(":");
			
			String part1 = colParts[0].trim();
			String part2 = (colParts.length == 2 ? colParts[1].trim() : "");
			
			if(part1.equalsIgnoreCase("Session")) {
				if(part2.equalsIgnoreCase("Name") || part2.length() == 0) {
					retVal = new SessionInfoColumn();
				} else if(part2.equalsIgnoreCase("Date")) {
					retVal = new SessionInfoColumn("Date");
				} else if(part2.equalsIgnoreCase("Media")) {
					retVal = new SessionInfoColumn("Media");
				}
			} else if(part1.equalsIgnoreCase("Speaker")) {
				if(part2.equalsIgnoreCase("Name") || part2.length() == 0) {
					retVal = new SpeakerInfoColumn("Name");
				} else if(part2.equalsIgnoreCase("Age")) {
					retVal = new SpeakerInfoColumn("Age");
				} else if(part2.equalsIgnoreCase("Birthday")) {
					retVal = new SpeakerInfoColumn("Birthday");
				} else if(part2.equalsIgnoreCase("Group")) {
					retVal = new SpeakerInfoColumn("Group");
				} else if(part2.equalsIgnoreCase("Language")) {
					retVal = new SpeakerInfoColumn("Language");
				} else if(part2.equalsIgnoreCase("Education")) {
					retVal = new SpeakerInfoColumn("Education");
				} else if(part2.equalsIgnoreCase("Sex")) {
					retVal = new SpeakerInfoColumn("Sex");
				}
			} else if(part1.equalsIgnoreCase("Tier")) {
				retVal = new TierValueColumn(part2);
			}
		} else if(columnName.equalsIgnoreCase("Record #")) {
			retVal = new RecordNumberColumn();
		}
		
		return retVal;
	}
	
	/**
	 * Add a column to the list.
	 */
	private void addColumn(String col) {
		CSVExportColumn c = getReportColumn(col);
		if(c == null) {
			LOGGER.warning("Invalid report column '" + col + "'");
			return;
		}
		if(!reportColumnModel.contains(c))
			reportColumnModel.addElement(c);
		columnEntryField.setText("");
	}
	
	/**
	 * Move given column up in the list
	 * 
	 */
	private void moveColumnUp(int col) {
		CSVExportColumn c = (CSVExportColumn)reportColumnModel.remove(col);
		reportColumnModel.add(col-1, c);
		columnList.setSelectedIndex(col-1);
	}
	
	/**
	 * Move given column down in list
	 */
	private void moveColumnDown(int col) {
		CSVExportColumn c = (CSVExportColumn)reportColumnModel.remove(col);
		reportColumnModel.add(col+1, c);
		columnList.setSelectedIndex(col+1);
	}
	
	/**
	 * Remove column
	 */
	private void removeColumn(int col) {
		reportColumnModel.remove(col);
	}
	
	/**
	 * Get the list initial list of columns.
	 * Will return the last set of columns used
	 * for a report or a default list.
	 */
	private CSVExportColumn[] getInitialColumnList() {
		final String storedList = 
				PrefHelper.get(columnHistory, defaultColumns);
		return parseColumnList(storedList);
	}
	
	/**
	 * Create a column list from the given string.
	 */
	private CSVExportColumn[] parseColumnList(String colList) {
		String[] fields = colList.split(";");
		List<CSVExportColumn> cols = new ArrayList<CSVExportColumn>();
		
		for(String f:fields) {
			CSVExportColumn c = getReportColumn(f);
			if(c != null)
				cols.add(c);
			else
				LOGGER.warning("Invalid report column '" + f + "'");
		}
		
		return cols.toArray(new CSVExportColumn[0]);
	}
	
	/**
	 * Get the configured column list
	 */
	public CSVExportColumn[] getColumns() {
		CSVExportColumn[] retVal = new CSVExportColumn[reportColumnModel.getSize()];
		reportColumnModel.copyInto(retVal);
		return retVal;
	}
	
	/**
	 * Report Column renderer
	 */
	private class ReportColumnCellRenderer extends DefaultListCellRenderer {

		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			JLabel retVal =  (JLabel)super.getListCellRendererComponent(list, value, index, isSelected,
					cellHasFocus);
			
			if(value != null) {
				CSVExportColumn c = (CSVExportColumn)value;
				retVal.setText(c.getTitle());
			}
			
			return retVal;
		}
		
	}

	@Override
	public boolean validateStep() {
		// save list of columns
		CSVExportColumn[] list = getColumns();
		String propVal = "";
		for(CSVExportColumn rc:list)
			propVal += (propVal.length() > 0 ? ";" : "") + rc.getTitle();
		PrefHelper.getUserPreferences().put(columnHistory, propVal);
		
		return super.validateStep();
	}
	
}
