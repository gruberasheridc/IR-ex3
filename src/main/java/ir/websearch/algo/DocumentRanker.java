package ir.websearch.algo;

import ir.websearch.algo.helper.InputParams;
import ir.websearch.algo.helper.InputParams.Parser;

public class DocumentRanker {

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
		
		
	}

}
