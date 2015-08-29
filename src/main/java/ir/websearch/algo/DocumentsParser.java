package ir.websearch.algo;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import ir.websearch.algo.helper.StringUtils;

public class DocumentsParser {
	
	private static final String DOC_SEPERATOR = new String(new char[] { 127 });
	private static final String DOC_PREFIX = ".I";
	private static final String TEXT_PREFIX = ".W";
	private static final String OPEN_PARAM_TOKEN = "OPEN_TOKEN";
	private static final String CLOSE_PARAM_TOKEN = "CLOSE_PARAM_TOKEN";
	private static final String PARAM_REGEX_PATTERN = ".*" + OPEN_PARAM_TOKEN + "(.*?)" + CLOSE_PARAM_TOKEN;
	
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
				String docId =  doc.split(TEXT_PREFIX)[0];
				Integer docID = Integer.parseInt(StringUtils.findRegexFirstMatch("\\d+", docId));
				System.out.println(docId);
			}
		} catch (IOException e) {}
		
		return documents;
	}

}
