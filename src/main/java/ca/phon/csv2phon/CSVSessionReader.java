package ca.phon.csv2phon;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import ca.phon.csv2phon.io.ImportDescriptionType;
import ca.phon.csv2phon.io.ObjectFactory;
import ca.phon.session.Session;
import ca.phon.session.io.SessionIO;
import ca.phon.session.io.SessionReader;

@SessionIO(
	group = "ca.phon",
	id = "csv2phon",
	version = "1.0",
	mimetype = "application/csv",
	extension = "csv",
	name = "CSV"
)
public class CSVSessionReader implements SessionReader {
	
	private ImportDescriptionType importSettings;
	
	public CSVSessionReader() {
	}
	
	public CSVSessionReader(ImportDescriptionType importSettings) {
		this.importSettings = importSettings;
	}

	@Override
	public boolean canRead(File file) throws IOException {
		// file must have a .csv extension
		if(file.getName().toLowerCase().endsWith(".csv"))
			return true;
		else
			return false;
	}
	

	/**
	 * Obtain import settings from modal dialog.
	 * 
	 * @return <code>true</code> if the user entered
	 *  import settings, <code>false</code> if the
	 *  action has been user-canceled
	 */
	private boolean askForImportSettings() {
		return false;
	}

	@Override
	public Session readSession(InputStream stream) throws IOException {
		if(importSettings == null) {
			if(!askForImportSettings()) 
				throw new IOException("User canceled session import");
		}
		return null;
	}

}
