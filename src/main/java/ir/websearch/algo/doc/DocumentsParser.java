package ir.websearch.algo.doc;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.io.FileUtils;

import ir.websearch.algo.doc.Document.Builder;
import ir.websearch.algo.helper.StringUtils;

public class DocumentsParser {
	
	private static final String DOC_SEPERATOR = new String(new char[] { 127 });
	private static final String DOC_PREFIX = ".I";
	private static final String TEXT_PREFIX = ".W";
	private static final String REMOVE_CHARS_REGEX = "[^-A-Za-z0-9\\s]";
	
	private final String docsFile;
	
	public DocumentsParser(String docsFile) {
		this.docsFile = docsFile;
	}
	
	public Collection<Document> parse() {
		Collection<Document> documents = new ArrayList<Document>();

		File file = new File(docsFile);
		try {
			String docsJoin = FileUtils.readFileToString(file);
			docsJoin = docsJoin.replace(DOC_PREFIX, DOC_SEPERATOR);
			docsJoin = docsJoin.trim();
			String[] docs = docsJoin.split("(?=" + DOC_SEPERATOR + ")");
			for (String doc : docs) {
				Document.Builder docBuilder = new Builder();
				String[] idTextSplit =  doc.split(TEXT_PREFIX);
				String docIDPart = idTextSplit[0];
				Integer docID = Integer.parseInt(StringUtils.findRegexFirstMatch("\\d+", docIDPart));
				docBuilder.id(docID);
				
				String textPart = idTextSplit[1];
				String[] titelAbstructSplit = textPart.split("\\.", 2);
				String title = titelAbstructSplit[0];
				title = StringUtils.whitespacesToSingleSpace(title);
				title = StringUtils.removeRedundantChars(title, REMOVE_CHARS_REGEX);
				docBuilder.title(title);
				
				String abst = titelAbstructSplit[1];
				abst = StringUtils.whitespacesToSingleSpace(abst);
				abst = StringUtils.removeRedundantChars(abst, REMOVE_CHARS_REGEX);
				docBuilder.abst(abst);
				
				String text = title + " " + abst;
				docBuilder.text(text);
				
				documents.add(docBuilder.build());
			}
		} catch (Exception e) {
			documents = null;
		}
		
		return documents;
	}

}
