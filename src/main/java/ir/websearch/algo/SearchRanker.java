package ir.websearch.algo;

import java.util.Collection;

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
		
	}

}
