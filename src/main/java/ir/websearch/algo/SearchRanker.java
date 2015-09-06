package ir.websearch.algo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

import ir.websearch.algo.core.BasicAlgorithm;
import ir.websearch.algo.core.ISearchAlgorithm;
import ir.websearch.algo.core.ImprovedAlgorithm;
import ir.websearch.algo.doc.Document;
import ir.websearch.algo.doc.DocumentsParser;
import ir.websearch.algo.helper.InputParams;
import ir.websearch.algo.helper.InputParams.Parser;
import ir.websearch.algo.query.QueriesParser;
import ir.websearch.algo.query.Query;

public class SearchRanker {
	
	private static final String BASIC_ALGORITHM = "basic";
	private static final String IMPROVED_ALGORITHM = "improved";

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

		// Generate the a retrieval algorithm of choice and perform search.
		ISearchAlgorithm algorithm = null; 
		switch (inputParams.getRetrievalAlgorithm()) {
		case BASIC_ALGORITHM:
			algorithm = new BasicAlgorithm(docs, queries);
			break;
		case IMPROVED_ALGORITHM:
			algorithm = new ImprovedAlgorithm(docs, queries);
			break;
		}
		
		List<String> outputOfAllQueries = algorithm.search();
		if (outputOfAllQueries == null) {
			System.out.println("Faild to search the collection.");
			return;
		}
		
		// Output retrieval experiment results.
		Path outputPath = Paths.get(inputParams.getOutputFileName());
		try {
			Files.write(outputPath, outputOfAllQueries);
		} catch (IOException e) {
			System.out.println("Faild to write output file name: " + inputParams.getOutputFileName() + ".");
		}
	}

	

}
