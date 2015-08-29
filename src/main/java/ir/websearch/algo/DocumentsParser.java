package ir.websearch.algo;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.regex.Matcher;

import org.apache.commons.io.FileUtils;

public class DocumentsParser {
	
	private static final String DOC_SEPERATOR = new String(new char[] { 127 });
	private static final String DOC_PREFIX = Matcher.quoteReplacement(".I");	
	
	private final String docsFile;
	
	public DocumentsParser(String docsFile) {
		this.docsFile = docsFile;
	}
	
	public Collection<Document> parse() {
		Collection<Document> documents = null;

		File file = new File(docsFile);
		try {
			String docsJoin = FileUtils.readFileToString(file);
			docsJoin = docsJoin.replace(DOC_PREFIX, DOC_SEPERATOR);
			String[] docs = docsJoin.split("(?=" + DOC_SEPERATOR + ")");
			for (String doc : docs) {
				System.out.println(doc);
			}
			System.out.println("asdsd");
		} catch (IOException e) {}
		
		return documents;
	}

}
