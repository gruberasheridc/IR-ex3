package ir.websearch.algo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.StopwordAnalyzerBase;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.misc.HighFreqTerms;
import org.apache.lucene.misc.HighFreqTerms.DocFreqComparator;
import org.apache.lucene.misc.HighFreqTerms.TotalTermFreqComparator;
import org.apache.lucene.misc.TermStats;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.CollectionUtil;

import ir.websearch.algo.doc.Document;
import ir.websearch.algo.doc.DocumentsParser;
import ir.websearch.algo.helper.CollectionUtils;
import ir.websearch.algo.helper.InputParams;
import ir.websearch.algo.helper.InputParams.Parser;
import ir.websearch.algo.query.QueriesParser;
import ir.websearch.algo.query.Query;

public class SearchRanker {

	public static void main(String[] args) {
		if (args.length != 1) {
			// must accept only one parameter which is the name of a parameter file.
			System.out.println("Must include the parameter file name");
			return;
		}

		String fileName = args[0];
		Parser inputParser = new Parser(fileName);
		InputParams inputParams = inputParser.parse();
		if (inputParams == null) {
			System.out.println("Faild to load parameter file name: " + fileName + ".");
			return;
		}

		DocumentsParser docsParser = new DocumentsParser(inputParams.getDocsFileName());
		Collection<Document> docs = docsParser.parse();
		if (docs == null) {
			System.out.println("Faild to load document file name: " + inputParams.getDocsFileName() + ".");
			return;
		}

		QueriesParser queriesParser = new QueriesParser(inputParams.getQueryFileName());
		Collection<Query> queries = queriesParser.parse();
		if (queries == null) {
			System.out.println("Faild to load queries file name: " + inputParams.getQueryFileName() + ".");
			return;
		}

		// Index documents to lucene.
		Analyzer indexAnalyzer = new StandardAnalyzer();
		Directory index = new RAMDirectory();
		IndexWriterConfig config = new IndexWriterConfig(indexAnalyzer);

		try (IndexWriter idxWriter = new IndexWriter(index, config)) {
			for (Document doc : docs) {
				// Index document.
				addDoc(idxWriter, doc);
			}

		} catch (IOException e) {
			// TODO handle exception block.
			e.printStackTrace();
		}
				
		Set<String> freqStopWords = calcTopStopWords(index, 20);
		List<String> outputOfAllQueries = generateQuerySearchResults(queries, indexAnalyzer, index, freqStopWords);		
		Path outputPath = Paths.get(inputParams.getOutputFileName());
		try {
			Files.write(outputPath, outputOfAllQueries);
		} catch (IOException e) {
			// TODO handle catch block
			e.printStackTrace();
		}
	}

	/**
	 * The method generate lucene queries and execute search. 
	 * @param queries the search query.
	 * @param idxAnalyzer the index used for the collection indexing.
	 * @param index the collection index to search.
	 * @param freqStopWords a set of top stop words from the collection (most frequent).
	 * @return query search results in printable format.
	 */
	private static List<String> generateQuerySearchResults(Collection<Query> queries, Analyzer indexAnalyzer, 
			Directory index, Set<String> freqStopWords) {
		int hitsPerPage = 10;
		List<String> outputOfAllQueries = new ArrayList<String>();
		final CharArraySet queryStopWords = calcStopWordsForQueryAnalyzer(indexAnalyzer, freqStopWords);
		Analyzer queyrAnalyzer = new StandardAnalyzer(queryStopWords);
		try (IndexReader idxReader = DirectoryReader.open(index)) {
			for (Query query : queries) {
				// Submit the query: for each query term, fetch the inverted list from the index.
				QueryParser parser = new QueryParser(Document.TITLE_FIELD, queyrAnalyzer);
				org.apache.lucene.search.Query q = parser.parse(query.getQuery());
				IndexSearcher searcher = new IndexSearcher(idxReader);
				TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage);
				searcher.search(q, collector);
				ScoreDoc[] scoreDocs = collector.topDocs().scoreDocs;
				System.out.println("Found " + scoreDocs.length + " hits.");

				List<String> queryOutput = new ArrayList<String>();
				List<ScoreDoc> hits = Arrays.asList(scoreDocs);
				List<ImmutablePair<Integer, Float>> sortedHits = hits.stream().
					map(scoreDoc -> 
							{
								int docId = scoreDoc.doc;								
								org.apache.lucene.document.Document document = searcher.doc(docId);
								Integer extlDocID = Integer.parseInt(document.get(Document.ID_FIELD));
								Float score = scoreDoc.score;
								return new ImmutablePair<Integer, Float>(extlDocID, score);
							})
					.sorted(Comparator.comparing(keyExtractor, keyComparator)))
								.thenComparing(Comparator.comparingInt(scoreDoc -> scorDoc.getLeft())))
					.collect(Collectors.toList());
				
				for (ScoreDoc scoreDoc : hits) {
					int docId = scoreDoc.doc;					
					float score = scoreDoc.score;
					org.apache.lucene.document.Document document = searcher.doc(docId);
					System.out.println("DocID: " + docId + "\t" + "Doc Score: " + score + "\t" + 
							"DocID: " + document.get("id") + "\t" + "Doc Title: " + document.get("title") + "\t" + 
							"Doc Abstruct: " + document.get("abstruct"));
					String outputLine = "q" + query.getId() + "," + "doc" + document.get("id") + "," + score;
					queryOutput.add(outputLine);
				}
				
				if (CollectionUtils.isEmpty(queryOutput)) {
					// No documents are retrieved for a query. Create dummy output.
					String dummayOutputLine = "q" + query.getId() + "," + "dummy" + "," + 1;
					queryOutput.add(dummayOutputLine);
				}
				
				outputOfAllQueries.addAll(queryOutput);
			}
		} catch (ParseException | IOException e) {
			// TODO handle exception block.
			e.printStackTrace();
		}
		return outputOfAllQueries;
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

	private static Set<String> calcTopStopWordsSelf(Directory index, int top) {
		final Map<String, Long> frequencyMap = new HashMap<>();
	    
	    try (IndexReader idxReader = DirectoryReader.open(index)) {
			Fields fields = MultiFields.getFields(idxReader);
	        Terms terms = fields.terms(Document.ABSTRACT_FIELD);
	        TermsEnum iterator = terms.iterator();
	        BytesRef byteRef = null;
	        while((byteRef = iterator.next()) != null) {
	        	String term = byteRef.utf8ToString();
	            long df = iterator.totalTermFreq();
	            frequencyMap.put(term, df);
	        }	        
		} catch (IOException e) {
			// TODO handle catch block
			e.printStackTrace();
		}
	    
	    // Get the top stop words by document frequency.
	    Set<String> stopWords = frequencyMap.entrySet().stream()
	    			.sorted(Map.Entry.<String, Long>comparingByValue().reversed())
	    			//.filter(entry -> entry.getValue() > 1) // In case we do not have top words with frequency above 1 return a smaller list.
	    			.map(entry -> entry.getKey())
	    			.limit(top)
	    			.collect(Collectors.toSet());
	    
	    return stopWords;
	}
	
	/**
	 * The method calculates stop words from the indexed document collection.
	 * @param index the index from whom to derive stop words.
	 * @param top the amount of desired stop words.
	 * @return a set of top stop words.
	 */
	private static Set<String> calcTopStopWords(Directory index, int top) {
		Set<String> stopWords = new HashSet<>(); 
	    try (IndexReader idxReader = DirectoryReader.open(index)) {
	    	TotalTermFreqComparator cmp = new HighFreqTerms.TotalTermFreqComparator();
		    TermStats[] highFreqTerms = HighFreqTerms.getHighFreqTerms(idxReader, top, Document.ABSTRACT_FIELD, cmp);
		    for (TermStats ts : highFreqTerms) {
		    	String term = ts.termtext.utf8ToString();
				stopWords.add(term);
		    }
		} catch (Exception e) {
			// TODO handle catch block
			e.printStackTrace();
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
		writer.addDocument(document);
	}

}
