package ir.websearch.algo;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.io.FileUtils;

public class DocumentsParser {
	
	private final String docsFile;
	
	public DocumentsParser(String docsFile) {
		this.docsFile = docsFile;
	}
	
	public Collection<Document> parse() {
		Collection<Document> documents = null;

		File file = new File(docsFile);
		try {
			String docsConcat = FileUtils.readFileToString(file);
			String [] docs = docsConcat.split(".I");
			for (String string : docs) {
				System.out.println("asdsd");
			}
			System.out.println("asdsd");
		} catch (IOException e) {}
		
		return documents;
	}

}
