package de.tud.plt.r43ples.management;

public class QueryParser {
	
	public static String getStringEnclosedinBraces(final String string, int start_pos){
		int end_pos = start_pos;
		int count_parenthesis = 1;
		while (count_parenthesis>0) {
			end_pos++;
			char ch = string.charAt(end_pos);
			if (ch=='{') count_parenthesis++;
			if (ch=='}') count_parenthesis--;
		}
		String substring = string.substring(start_pos, end_pos);
		return substring;
	}
	
}
