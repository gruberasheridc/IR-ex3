package ir.websearch.algo.query;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.io.FileUtils;

import ir.websearch.algo.query.Query.Builder;
import ir.websearch.algo.helper.StringUtils;

public class QueriesParser {
	
	private static final String QUERY_SEPERATOR = new String(new char[] { 127 });
	private static final String QUERY_PREFIX = ".I";
	private static final String TEXT_PREFIX = ".W";
	
	private final String queriesFile;
	
	public QueriesParser(String queriesFile) {
		this.queriesFile = queriesFile;
	}
	
	public Collection<Query> parse() {
		Collection<Query> queries = new ArrayList<Query>();

		File file = new File(queriesFile);
		try {
			String queriesJoin = FileUtils.readFileToString(file);
			queriesJoin = queriesJoin.replace(QUERY_PREFIX, QUERY_SEPERATOR);
			String[] rawQueries = queriesJoin.split("(?=" + QUERY_SEPERATOR + ")");
			for (String doc : rawQueries) {
				Query.Builder queryBuilder = new Builder();
				String[] idTextSplit =  doc.split(TEXT_PREFIX);
				String docIDPart = idTextSplit[0];
				Integer docID = Integer.parseInt(StringUtils.findRegexFirstMatch("\\d+", docIDPart));
				queryBuilder.id(docID);
				
				String textPart = idTextSplit[1];
				queryBuilder.query(textPart);
				
				queries.add(queryBuilder.build());
			}
		} catch (Exception e) {
			queries = null;
		}
		
		return queries;
	}

}
