package ir.websearch.algo.helper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class InputParams {

	private final String queryFileName;
	private final String docsFileName;
	private final String outputFileName;
	private final String retrievalAlgorithm;

	public String getQueryFileName() {
		return queryFileName;
	}

	public String getDocsFileName() {
		return docsFileName;
	}

	public String getOutputFileName() {
		return outputFileName;
	}

	public String getRetrievalAlgorithm() {
		return retrievalAlgorithm;
	}

	public static class Parser {

		private static final String RETRIEVAL_ALGORITHM_KEY = "retrievalAlgorithm";
		private static final String OUTPUT_FILE_KEY = "outputFile";
		private static final String DOCS_FILE_KEY = "docsFile";
		private static final String QUERY_FILE_KEY = "queryFile";
		
		private String queryFileName = null;
		private String docsFileName = null;
		private String outputFileName = null;
		private String retrievalAlgorithm = null;
		private String fileName;

		public Parser(String fileName) {
			this.fileName = fileName;
		}

		public InputParams parse() {
			InputParams inputParams = null;
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(fileName));
				String line = null;
				while ((line = reader.readLine()) != null) {
					if (queryFileName == null) {
						queryFileName = getParamByKey(line, QUERY_FILE_KEY);
					}
					
					if (docsFileName == null) {
						docsFileName = getParamByKey(line, DOCS_FILE_KEY); 
					}
					
					if (outputFileName == null) {
						outputFileName = getParamByKey(line, OUTPUT_FILE_KEY);
					}
					
					if (retrievalAlgorithm == null) {
						retrievalAlgorithm = getParamByKey(line, RETRIEVAL_ALGORITHM_KEY);
					}
				}
				
				if (isInputValid()) {
					inputParams = new InputParams(this);
				}				
			} catch (IOException e) {
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException e) {
						// Invalidate the input params if we encounter an exception.
						inputParams = null;
					}
				}
			}
								
			return inputParams;
		}
		
		private static String getParamByKey(String line, String paramKey) {
			String param = null;
			String paramKeyPrefix = paramKey + "="; 
			if (line.startsWith(paramKeyPrefix)) {
				param = line.substring(line.indexOf(paramKeyPrefix) + paramKeyPrefix.length(), line.length());
			}
			
			return param;
		}
		
		private boolean isInputValid() {
			boolean retval = false;
			
			if (this.docsFileName != null && this.fileName != null && this.outputFileName != null && this.queryFileName != null && this.retrievalAlgorithm != null) {
				retval = true;
			}
			
			return retval;
		}
	}

	private InputParams(Parser parser) {
		this.queryFileName = parser.queryFileName;
		this.docsFileName = parser.docsFileName;
		this.outputFileName = parser.outputFileName;
		this.retrievalAlgorithm = parser.retrievalAlgorithm;
	}
}
