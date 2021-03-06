package ir.websearch.algo.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.StopwordAnalyzerBase;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.misc.HighFreqTerms;
import org.apache.lucene.misc.HighFreqTerms.TotalTermFreqComparator;
import org.apache.lucene.misc.TermStats;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

import ir.websearch.algo.doc.Document;
import ir.websearch.algo.helper.CollectionUtils;
import ir.websearch.algo.query.Query;

public class BasicAlgorithm implements ISearchAlgorithm {
	
	private final Collection<Document> docs;
	private final Collection<Query> queries;
	
	public BasicAlgorithm(Collection<Document> docs, Collection<Query> queries) {
		this.docs = docs;
		this.queries = queries;
	}
	
	@Override
	public List<String> search() {
		List<String> outputOfAllQueries = null;
		
		try {
			// Index documents to lucene.
			Analyzer indexAnalyzer = new StandardAnalyzer();
			Directory index = new RAMDirectory();
			indexDocuments(docs, indexAnalyzer, index);
			
			// Run retrieval experiment.
			Set<String> freqStopWords = calcTopStopWords(index, 20);
			outputOfAllQueries = generateQuerySearchResults(queries, indexAnalyzer, index, freqStopWords);
		} catch (Exception e) {
			System.out.println("Faild to search the collection.");
			outputOfAllQueries = null;
		}
		
		return outputOfAllQueries;
	}
	
	/**
	 * The method indexes the collection documents.
	 * @param docs the collection of documents to index. 
	 * @param indexAnalyzer the {@link Analyzer} used for indexing.
	 * @param index the index implementation of {@link Directory}.
	 * @throws IOException 
	 */
	private static void indexDocuments(Collection<Document> docs, Analyzer indexAnalyzer, Directory index) throws IOException {
		IndexWriterConfig config = new IndexWriterConfig(indexAnalyzer);
		try (IndexWriter idxWriter = new IndexWriter(index, config)) {
			for (Document doc : docs) {
				// Index document.
				addDoc(idxWriter, doc);
			}

		} catch (IOException e) {
			throw e;
		}
	}

	/**
	 * The method generate lucene queries and execute search. 
	 * @param queries the search query.
	 * @param idxAnalyzer the index used for the collection indexing.
	 * @param index the collection index to search.
	 * @param freqStopWords a set of top stop words from the collection (most frequent).
	 * @return query search results in printable format.
	 * @throws Exception 
	 */
	private List<String> generateQuerySearchResults(Collection<Query> queries, Analyzer indexAnalyzer, 
			Directory index, Set<String> freqStopWords) throws Exception {
		int hitsPerPage = 10;
		List<String> outputOfAllQueries = new ArrayList<String>();
		final CharArraySet queryStopWords = calcStopWordsForQueryAnalyzer(indexAnalyzer, freqStopWords);
		Analyzer queyrAnalyzer = new StandardAnalyzer(queryStopWords);
		try (IndexReader idxReader = DirectoryReader.open(index)) {
			IndexSearcher searcher = new IndexSearcher(idxReader);			
			for (Query query : queries) {
				TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage);
				List<String> queryOutput = generateQueryOutput(queyrAnalyzer, searcher, collector, query);				
				outputOfAllQueries.addAll(queryOutput);
			}
		} catch (ParseException | IOException e) {
			throw e;
		}
		
		return outputOfAllQueries;
	}

	/**
	 * The method fetches the inverted list from the index for the given query.
	 * Afterwards the matching documents are sorted by their tf-idf scores and document ID and given a rank.
	 * Finally, the method generates a line for each Query, Doc, Rank triplet.   
	 * @param queyrAnalyzer the query search analyzer.
	 * @param searcher the query index searcher.
	 * @param collector the query collector.
	 * @param query the query to search.
	 * @return list of output lines. A line for each Query, Doc, Rank triplet.
	 * @throws ParseException
	 * @throws IOException
	 */
	private List<String> generateQueryOutput(Analyzer queyrAnalyzer, IndexSearcher searcher,
			TopScoreDocCollector collector, Query query) throws ParseException, IOException {
		org.apache.lucene.search.Query q = generateQuery(queyrAnalyzer, query);
		searcher.search(q, collector);
		ScoreDoc[] scoreDocs = collector.topDocs().scoreDocs;
		System.out.println("Found " + scoreDocs.length + " hits.");

		List<String> queryOutput = new ArrayList<String>();
		List<ImmutablePair<Integer, Float>> sortedHits = sortSearchHits(searcher, scoreDocs);
		int rank = 1;
		for (ImmutablePair<Integer, Float> scoreDoc : sortedHits) {
			String outputLine = "q" + query.getId() + "," + "doc" + scoreDoc.getKey() + "," + rank;
			queryOutput.add(outputLine);
			rank++;
		}
		
		if (CollectionUtils.isEmpty(queryOutput)) {
			// No documents are retrieved for a query. Create dummy output.
			String dummayOutputLine = "q" + query.getId() + "," + "dummy" + "," + 1;
			queryOutput.add(dummayOutputLine);
		}
		
		return queryOutput;
	}

	protected org.apache.lucene.search.Query generateQuery(Analyzer queyrAnalyzer, Query query) throws ParseException {
		QueryParser parser = new QueryParser(Document.TEXT_FIELD, queyrAnalyzer);
		org.apache.lucene.search.Query q = parser.parse(query.getQuery());
		return q;
	}

	/**
	 * The method sorts the search hits.
	 * Sorts the matching documents by their tf-idf scores, in descending order. 
	 * The external document ID is a secondary sort key (i.e., for breaking ties), in ascending order.
	 * @param searcher the index searcher.
	 * @param scoreDocs the query hits.
	 * @return a list of sorted Document ID, Document Score pairs (List<ImmutablePair<DocID, Score>>). 
	 */
	private static List<ImmutablePair<Integer, Float>> sortSearchHits(IndexSearcher searcher, ScoreDoc[] scoreDocs) {
		List<ScoreDoc> hits = Arrays.asList(scoreDocs);
		Comparator<ImmutablePair<Integer, Float>> docScoreCmp = createDocScoreComparator();
		List<ImmutablePair<Integer, Float>> sortedHits = hits.stream().
			map(scoreDoc -> 
					{
						int docId = scoreDoc.doc;								
						org.apache.lucene.document.Document document = null;
						try {
							document = searcher.doc(docId);
						} catch (Exception e) {
							System.out.println("Faild to extract document from Doc ID: " + docId);
						}
						
						Integer extlDocID = Integer.parseInt(document.get(Document.ID_FIELD));
						Float score = scoreDoc.score;
						return new ImmutablePair<Integer, Float>(extlDocID, score);
					})
			.sorted(docScoreCmp)
			.collect(Collectors.toList());
		
		return sortedHits;
	}

	private static Comparator<ImmutablePair<Integer, Float>> createDocScoreComparator() {
		Comparator<ImmutablePair<Integer, Float>> docScoreCmp = (ds1, ds2) -> ds2.getRight().compareTo(ds1.getRight()); // Sort the matching documents by their tf-idf scores (descending order).
		docScoreCmp.thenComparing((ds1, ds2) -> ds1.getLeft().compareTo(ds2.getLeft())); // External document ID should be a secondary sort key (ascending order).
		return docScoreCmp;
	}

	/**
	 * The method generates a stop words set for the query search analyzer.
	 * We will want the stop words list to include the stop words of the analyzer which was used for the indexing,
	 * but we will want to add to them the stop words we calculated from the collection. 
	 * @param idxAnalyzer the index used for the collection indexing.
	 * @param freqStopWords most frequent words in the collection.
	 * @return a join stop words set (index analyzer + frequent collection words).
	 */
	private static CharArraySet calcStopWordsForQueryAnalyzer(Analyzer idxAnalyzer, Set<String> freqStopWords) {
		final CharArraySet calcStopWords = new CharArraySet(freqStopWords, false);
		final CharArraySet indexAnalyzeStopWords = ((StopwordAnalyzerBase) idxAnalyzer).getStopwordSet();				
		org.apache.commons.collections4.CollectionUtils.addAll(calcStopWords, indexAnalyzeStopWords);
		return calcStopWords;
	}
	
	/**
	 * The method calculates stop words from the indexed document collection.
	 * @param index the index from whom to derive stop words.
	 * @param top the amount of desired stop words.
	 * @return a set of top stop words.
	 * @throws Exception
	 */
	protected Set<String> calcTopStopWords(Directory index, int top) throws Exception {
		Set<String> stopWords = new HashSet<>(); 
	    try (IndexReader idxReader = DirectoryReader.open(index)) {
	    	TotalTermFreqComparator cmp = new HighFreqTerms.TotalTermFreqComparator();
		    TermStats[] highFreqTerms = HighFreqTerms.getHighFreqTerms(idxReader, top, Document.TEXT_FIELD, cmp);
		    for (TermStats ts : highFreqTerms) {
		    	String term = ts.termtext.utf8ToString();
				stopWords.add(term);
		    }
		} catch (Exception e) {
			throw e;
		}
	    
	    return stopWords;
	}

	/**
	 * The method ads a document to the index.
	 * @param writer the lucene {@link IndexWriter}
	 * @param doc the document to index.
	 * @throws IOException
	 */
	private static void addDoc(IndexWriter writer, Document doc) throws IOException {
		org.apache.lucene.document.Document document = new org.apache.lucene.document.Document();
		document.add(new IntField(Document.ID_FIELD, doc.getId(), Field.Store.YES));
		document.add(new TextField(Document.TITLE_FIELD, doc.getTitle(), Field.Store.YES));
		document.add(new TextField(Document.ABSTRACT_FIELD, doc.getAbst(), Field.Store.YES));
		document.add(new TextField(Document.TEXT_FIELD, doc.getText(), Field.Store.YES));
		writer.addDocument(document);
	}

}
