package ir.websearch.algo;

import java.io.IOException;
import java.util.Collection;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

import ir.websearch.algo.doc.Document;
import ir.websearch.algo.doc.DocumentsParser;
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
