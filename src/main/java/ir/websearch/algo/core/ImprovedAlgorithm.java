package ir.websearch.algo.core;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.store.Directory;

import ir.websearch.algo.doc.Document;
import ir.websearch.algo.query.Query;

public class ImprovedAlgorithm extends BasicAlgorithm {

	public ImprovedAlgorithm(Collection<Document> docs, Collection<Query> queries) {
		super(docs, queries);
	}
	
	@Override
	protected Set<String> calcTopStopWords(Directory index, int top) {
		Set<String> stopWords = new HashSet<>();	    
	    return stopWords;
	}
	
	@Override
	protected org.apache.lucene.search.Query generateQuery(Analyzer queyrAnalyzer, Query query) throws ParseException {
		QueryParser titleParser = new QueryParser(Document.TITLE_FIELD, queyrAnalyzer);
		org.apache.lucene.search.Query qTitel = titleParser.parse(query.getQuery());
		
		QueryParser abstParser = new QueryParser(Document.ABSTRACT_FIELD, queyrAnalyzer);
		org.apache.lucene.search.Query qAbstruct = abstParser.parse(query.getQuery());
		qAbstruct.setBoost(12f);
		
		Builder queryBuilder = new BooleanQuery.Builder();
		queryBuilder.add(qTitel, BooleanClause.Occur.SHOULD);
		queryBuilder.add(qAbstruct, BooleanClause.Occur.SHOULD);
		BooleanQuery boolQuery = queryBuilder.build();
		
		return boolQuery;
	}

}
