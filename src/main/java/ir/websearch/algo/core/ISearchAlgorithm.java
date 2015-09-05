package ir.websearch.algo.core;

import java.util.List;

public interface ISearchAlgorithm {
	
	/**
	 * The method performers search for the given document collection and query set. 
	 * @return search results in printable formated lines (QueryID, DocID, Rank).
	 */
	public List<String> search();

}
