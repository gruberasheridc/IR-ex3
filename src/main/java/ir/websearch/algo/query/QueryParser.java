package ir.websearch.algo.query;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.io.FileUtils;

import ir.websearch.algo.query.Query.Builder;
import ir.websearch.algo.helper.StringUtils;

public class QueryParser {
	
	private static final String DOC_SEPERATOR = new String(new char[] { 127 });
	private static final String DOC_PREFIX = ".I";
	private static final String TEXT_PREFIX = ".W";
	
	private final String docsFile;
	
	public QueryParser(String docsFile) {
		this.docsFile = docsFile;
	}
	
	public Collection<Query> parse() {
		Collection<Query> documents = new ArrayList<Query>();

		File file = new File(docsFile);
		try {
			String docsJoin = FileUtils.readFileToString(file);
			docsJoin = docsJoin.replace(DOC_PREFIX, DOC_SEPERATOR);
			String[] docs = docsJoin.split("(?=" + DOC_SEPERATOR + ")");
			for (String doc : docs) {
				Query.Builder queryBuilder = new Builder();
				String[] idTextSplit =  doc.split(TEXT_PREFIX);
				String docIDPart = idTextSplit[0];
				Integer docID = Integer.parseInt(StringUtils.findRegexFirstMatch("\\d+", docIDPart));
				queryBuilder.id(docID);
				
				String textPart = idTextSplit[1];
				String[] titelAbstructSplit = textPart.split("\\.", 2);
				String title = titelAbstructSplit[0];
				queryBuilder.title(title);
				
				String abst = titelAbstructSplit[1];
				queryBuilder.abst(abst);
				
				documents.add(queryBuilder.build());
			}
		} catch (Exception e) {
			documents = null;
		}
		
		return documents;
	}

}
