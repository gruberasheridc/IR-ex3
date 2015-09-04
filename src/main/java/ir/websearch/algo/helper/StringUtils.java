package ir.websearch.algo.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
	
	/*
	 * Find regex by grouping, It supports many or one group.
	 * Example :
	 * Text : Hello!hi=WhatsUp; - and we want to take just the WhatsUp : .*=(.*); will do the work OR if we want to take the Hi as well :
	 * .*!(.*)=(.*); - will take both hi and WhatsUp by order (List.get(0) = hi and List.get(1) = WhatsUp)
	 */
	public static List<String> findRegex(String regexToSearch, String source) {
		List<String> foundRegex = new ArrayList<String>();
		Pattern pattern = Pattern.compile(regexToSearch);
		if ((source != null && !source.isEmpty()) && (regexToSearch != null && !regexToSearch.isEmpty())) {
			Matcher matcher = pattern.matcher(source);
			while (matcher.find()) {
				if (matcher.groupCount() > 0) {
					foundRegex.add(matcher.group(1));
				}
				else {
					foundRegex.add(matcher.group());
				}
			}
		}
		return foundRegex;
	}
	
	/**
	 * Find Regex by grouping, It supports many or one group. But returns the first matched group.
	 * @param regexToSearch
	 * @param source
	 * @return the first group match; null if no group was found.
	 */
	public static String findRegexFirstMatch(String regexToSearch, String source) {
		String firtsMatch = null;
		
		List<String> foundRegex = findRegex(regexToSearch, source);				
		if (CollectionUtils.isNotEmpty(foundRegex)) {
			firtsMatch = foundRegex.get(0); 
		}
		
		return firtsMatch;
	}
	
	/**
	 * The method removes redundant characters from the given text according to the given regex.
	 * @param text the text for whom to remove the redundant characters. 
	 * @param regex the regex representing characters to remove.
	 * @return the given text without the redundant characters.
	 */
	public static String removeRedundantChars(String text, String regex) { 		
		return text.replaceAll(regex, "");
	}
	
	/**
	 * The method replaces 2 or more spaces with single space in string and delete leading spaces.
	 * @param text the text for whom to modify the whitespace.
	 * @return the modified text.
	 */
	public static String whitespacesToSingleSpace(String text) {
		return text.trim().replaceAll("\\s+", " ");
	}

}
