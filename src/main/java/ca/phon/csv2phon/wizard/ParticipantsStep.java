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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.table.AbstractTableModel;

import org.jdesktop.swingx.JXTable;

import ca.phon.csv2phon.CSVParticipantUtil;
import ca.phon.csv2phon.io.ImportDescriptionType;
import ca.phon.csv2phon.io.ObjectFactory;
import ca.phon.csv2phon.io.ParticipantType;
import ca.phon.session.Participant;
import ca.phon.session.SessionFactory;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.participant.ParticipantEditor;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Setup participants for all imported sessions/
 *
 */
public class ParticipantsStep extends CSVImportStep {
	
	private static final long serialVersionUID = -2083303664285587057L;

	private final static Logger LOGGER = Logger
			.getLogger(ParticipantsStep.class.getName());
	
	private DialogHeader header;
	
	/**
	 * Participant Table
	 * 
	 */
	private JXTable participantTable;
	private JButton editParticipantButton;
	private JButton addParticipantButton;
	
	/**
	 * Constructor
	 */
	public ParticipantsStep() {
		super();
		
		init();
	}
	
	private void init() {
		setLayout(new BorderLayout());
		
		header = new DialogHeader("CSV Import", "Set up participants.");
		add(header, BorderLayout.NORTH);
		
		FormLayout participantLayout = new FormLayout(
				"fill:pref:grow, 1dlu, pref",
				"pref, 3dlu, pref, 3dlu, pref, fill:pref:grow");
		CellConstraints cc = new CellConstraints();
		JPanel participantPanel = new JPanel(participantLayout);
		participantPanel.setBorder(BorderFactory.createTitledBorder("Participants"));
		
		JLabel infoLabel = new JLabel(
				"<html><body><p>(Optional) Set up participants which will be added to each imported session.</p></body></html>");
		participantTable = new JXTable();
		
		ImageIcon addIcon = 
			IconManager.getInstance().getIcon("actions/list-add", IconSize.XSMALL);
		addParticipantButton = new JButton(addIcon);
		addParticipantButton.setFocusable(false);
		addParticipantButton.setToolTipText("Add participant");
		addParticipantButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				newParticipant();
			}
			
		});
		
		ImageIcon editIcon = 
			IconManager.getInstance().getIcon("actions/edit", IconSize.XSMALL);
		editParticipantButton = new JButton(editIcon);
		editParticipantButton.setFocusable(false);
		editParticipantButton.setToolTipText("Edit participant");
		editParticipantButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				editParticipant();
			}
			
		});
		
		Action deleteParticipantAction = new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				deleteParticipant();
			}
			
		};
		ActionMap participantActionMap = participantTable.getActionMap();
		participantActionMap.put("DELETE_PARTICIPANT", deleteParticipantAction);
		InputMap participantInputMap = participantTable.getInputMap();
		participantInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "DELETE_PARTICIPANT");
		participantInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "DELETE_PARTICIPANT");
		
		participantTable.setActionMap(participantActionMap);
		participantTable.setInputMap(JComponent.WHEN_FOCUSED, participantInputMap);
		
		participantPanel.add(infoLabel, cc.xy(1,1));
		participantPanel.add(new JScrollPane(participantTable), cc.xywh(1, 3, 1, 4));
		participantPanel.add(addParticipantButton, cc.xy(3, 3));
		participantPanel.add(editParticipantButton, cc.xy(3, 5));
		
		add(participantPanel, BorderLayout.CENTER);
	}
	
	private void newParticipant() {
		final SessionFactory factory = SessionFactory.newFactory();
		Participant part = factory.createParticipant();
		
		boolean canceled = ParticipantEditor.editNewParticipant(CommonModuleFrame.getCurrentFrame(), part, getParticipants());
		
		if(!canceled) {
			getSettings().getParticipant().add(CSVParticipantUtil.copyPhonParticipant(new ObjectFactory(), part));
			((ParticipantsTableModel)participantTable.getModel()).fireTableDataChanged();
		}
	}
	
	private void editParticipant() {
		int selectedRow = participantTable.getSelectedRow();
		if(selectedRow < 0) return;
		selectedRow = participantTable.convertRowIndexToModel(selectedRow);
		if(selectedRow >= 0 && selectedRow < getParticipants().size()) {
			Participant part = getParticipants().get(selectedRow);
			
			ParticipantEditor.editParticipant(CommonModuleFrame.getCurrentFrame(), part, getParticipants());
			
			ParticipantType pt = CSVParticipantUtil.copyPhonParticipant(new ObjectFactory(), part);
			getSettings().getParticipant().set(selectedRow, pt);
		
			((ParticipantsTableModel)participantTable.getModel()).fireTableDataChanged();
		}
	}
	
	private void deleteParticipant() {
		int selectedRow = participantTable.getSelectedRow();
		if(selectedRow < 0) return;
		
		selectedRow = participantTable.convertRowIndexToModel(selectedRow);
		if(selectedRow >= 0 && selectedRow < getParticipants().size()) {
			if(getSettings() != null) {
				getSettings().getParticipant().remove(selectedRow);
				((ParticipantsTableModel)participantTable.getModel()).fireTableDataChanged();
			}
		}
	}
	
	
	public List<Participant> getParticipants() {
		List<Participant> retVal = new ArrayList<Participant>();
		
		if(getSettings() != null) {
			for(ParticipantType pt:getSettings().getParticipant()) {
				retVal.add(CSVParticipantUtil.copyXmlParticipant(SessionFactory.newFactory(), pt, 
						LocalDate.now()));
			}
		}
		
		return retVal;
	}
	
	
	@Override
	public void setSettings(ImportDescriptionType settings) {
		super.setSettings(settings);
		
		participantTable.setModel(new ParticipantsTableModel());
	}

	private class ParticipantsTableModel extends AbstractTableModel {
		
		public ParticipantsTableModel() {
			super();
			
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public int getRowCount() {
			return getParticipants().size();
		}
		
		@Override
		public String getColumnName(int col) {
			String retVal = "";
			
			if(col == 0) retVal = "Name";
			else if(col == 1) retVal = "Birthday";
			
			return retVal;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			Object retVal = new String();
			Participant p = getParticipants().get(rowIndex);
			if(columnIndex == 0) {
				if(p.getName() == null) return p.getId();
				retVal = p.getName();
			} else if(columnIndex == 1) {
				if(p.getBirthDate() != null) {
					final DateTimeFormatter dateFormatter = 
							DateTimeFormatter.ofPattern("yyyy-MM-dd");
					retVal = dateFormatter.format(p.getBirthDate());
				}
			} 
			return retVal;
		}
	}
	
}
