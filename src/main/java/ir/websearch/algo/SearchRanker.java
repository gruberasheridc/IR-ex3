package ir.websearch.algo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
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
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;

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
		Analyzer analyzer = new StandardAnalyzer();
		Directory index = new RAMDirectory();
		IndexWriterConfig config = new IndexWriterConfig(analyzer);

		try (IndexWriter idxWriter = new IndexWriter(index, config)) {
			for (Document doc : docs) {
				// Index document.
				addDoc(idxWriter, doc);
			}

		} catch (IOException e) {
			// TODO handle exception block.
			e.printStackTrace();
		}
		
		// Calculate stop words from the indexed document collection.
		Set<String> stopWords = calcTopStopWords(index, 20);

		// Generate lucene queries and execute search.
		int hitsPerPage = 10;
		List<String> outputOfAllQueries = new ArrayList<String>();
		for (Query query : queries) {
			List<String> queryOutput = new ArrayList<String>();			
			try {
				QueryParser parser = new QueryParser("title", analyzer);
				org.apache.lucene.search.Query q = parser.parse(query.getQuery());
				IndexReader idxReader = DirectoryReader.open(index);
				IndexSearcher searcher = new IndexSearcher(idxReader);
				TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage);
				searcher.search(q, collector);
				ScoreDoc[] hits = collector.topDocs().scoreDocs;

				System.out.println("Found " + hits.length + " hits.");								
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
			} catch (ParseException | IOException e) {
				// TODO handle exception block.
				e.printStackTrace();
			}
			
			if (CollectionUtils.isEmpty(queryOutput)) {
				// No documents are retrieved for a query. Create dummy output.
				String dummayOutputLine = "q" + query.getId() + "," + "dummy" + "," + 1;
				queryOutput.add(dummayOutputLine);
			}
			
			outputOfAllQueries.addAll(queryOutput);
		}
		
		Path outputPath = Paths.get(inputParams.getOutputFileName());
		try {
			Files.write(outputPath, outputOfAllQueries);
		} catch (IOException e) {
			// TODO handle catch block
			e.printStackTrace();
		}
	}

	private static Set<String> calcTopStopWords(Directory index, int top) {
		final Map<String, Integer> frequencyMap = new HashMap<String, Integer>();
	    
	    try (IndexReader idxReader = DirectoryReader.open(index)) {
			Fields fields = MultiFields.getFields(idxReader);
	        Terms terms = fields.terms("abstruct");
	        TermsEnum iterator = terms.iterator();
	        BytesRef byteRef = null;
	        while((byteRef = iterator.next()) != null) {
	        	String term = byteRef.utf8ToString();
	            int df = iterator.docFreq();
	            frequencyMap.put(term, df);
	        }	        
		} catch (IOException e) {
			// TODO handle catch block
			e.printStackTrace();
		}
	    
	    // Get the top stop words by document frequency.
	    Set<String> stopWords = frequencyMap.entrySet().stream()
	    			.sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
	    			.filter(entry -> entry.getValue() > 1) // In case we do not have top words with frequency above 1 return a smaller list.
	    			.map(entry -> entry.getKey())
	    			.limit(top)
	    			.collect(Collectors.toSet());
	    
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
		document.add(new IntField("id", doc.getId(), Field.Store.YES));
		document.add(new TextField("title", doc.getTitle(), Field.Store.YES));
		document.add(new TextField("abstruct", doc.getAbst(), Field.Store.YES));
		writer.addDocument(document);
	}

}
