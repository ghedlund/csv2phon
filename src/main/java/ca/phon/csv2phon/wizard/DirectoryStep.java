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
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.SortedMap;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.text.FileSelectionField;
import ca.phon.ui.text.FileSelectionField.SelectionMode;
import ca.phon.util.PhonConstants;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Select directory containing csv files.
 *
 */
public class DirectoryStep extends CSVImportStep {
	
	/** UI */
	private DialogHeader header;
	private JLabel infoLbl;
	
	private FileSelectionField csvDirField;
	
	private JComboBox charsetBox;
	
	private String charsetName = "UTF-8";
	
	private char fieldDelim = ',';
	
	private JTextField fieldDelimField;
	
	private char textDelim = '"';
	
	private JTextField textDelimField;
	
	public DirectoryStep() {
		init();
	}
	
	private void init() {
		setLayout(new BorderLayout());
		header = new DialogHeader("CSV Import", "Select folder containing csv files.");
		add(header, BorderLayout.NORTH);
		
		JPanel centerPanel = new JPanel();
		centerPanel.setBorder(BorderFactory.createTitledBorder("Folder"));
		FormLayout layout = new FormLayout(
				"right:pref, 3dlu, fill:pref:grow, pref",
				"pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref");
		CellConstraints cc = new CellConstraints();
		centerPanel.setLayout(layout);
		
		String lblTxt = "<html><body><p>Please select the folder containing the csv files for import." +
			"  <font color='red'>All csv files should have the same column structure and encoding</font>.</p></body></html>";
		infoLbl = new JLabel(lblTxt);
		centerPanel.add(infoLbl, cc.xyw(1,1,3));
		
		csvDirField = new FileSelectionField();
		csvDirField.setMode(SelectionMode.FOLDERS);
		csvDirField.getTextField().setEditable(false);
		
		// setup charset chooser
		SortedMap<String, Charset> availableCharset = 
			Charset.availableCharsets();
		charsetBox = new JComboBox(availableCharset.keySet().toArray(new String[0]));
		charsetBox.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					charsetName = charsetBox.getSelectedItem().toString();
				}
			}
			
		});
		charsetBox.setSelectedItem("UTF-8");
		
		textDelimField = new JTextField();
		textDelimField.setDocument(new SingleCharDocument());
		textDelimField.setText(textDelim+"");
		
		fieldDelimField = new JTextField();
		fieldDelimField.setDocument(new SingleCharDocument());
		fieldDelimField.setText(fieldDelim+"");
		
		centerPanel.add(new JLabel("Folder:"), cc.xy(1,3));
		centerPanel.add(csvDirField, cc.xyw(3,3,2));
		
		centerPanel.add(new JLabel("File encoding:"), cc.xy(1, 5));
		centerPanel.add(charsetBox, cc.xy(3, 5));
		
		centerPanel.add(new JLabel("Field delimiter:"), cc.xy(1,7));
		centerPanel.add(fieldDelimField, cc.xy(3, 7));
		
		centerPanel.add(new JLabel("Text delimiter:"), cc.xy(1, 9));
		centerPanel.add(textDelimField, cc.xy(3, 9));
		
		add(centerPanel, BorderLayout.CENTER);
	}
	
	@Override
	public boolean validateStep() {
		boolean retVal = false;
		
		// make sure a directory is selected and exists
		if(getBase() != null) {
			File f = getBase();
			retVal = f.exists() && f.isDirectory();
		}
		
		return retVal;
	}
	
	public File getBase() {
		return csvDirField.getSelectedFile();
	}

	public String getFileEncoding()  {
		return this.charsetName;
	}
	
	public Optional<Character> getTextDelimiter() {
		return Optional.ofNullable((textDelimField.getText().length() > 0 ?
				textDelimField.getText().charAt(0) : null));
	}
	
	public Optional<Character> getFieldDelimiter() {
		return Optional.ofNullable((fieldDelimField.getText().length() > 0 ?
				fieldDelimField.getText().charAt(0) : null));
	}
	
	private final class SingleCharDocument extends PlainDocument {

		@Override
		public void insertString(int offs, String str, AttributeSet a)
				throws BadLocationException {
			if(getLength() > 0) {
				Toolkit.getDefaultToolkit().beep();
			} else {
				super.insertString(0, (str.length() > 0 ? str.substring(0, 1) : str), a);
			}
		}

	}
	
}
