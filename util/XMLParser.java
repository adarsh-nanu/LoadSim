package util;

import java.util.*;

public class XMLParser
{
	public static Vector getXMLTagValue(String xml, String section) throws Exception
	{
		String xmlString = new String(xml);
		Vector v = new Vector();
		String beginTagToSearch = "<" + section + ">";
		String endTagToSearch = "</" + section + ">";

		// Look for the first occurrence of begin tag
		int index = xmlString.indexOf(beginTagToSearch);
		while(index != -1)
		{
			// Look for end tag
			// DOES NOT HANDLE
			int lastIndex = xmlString.indexOf(endTagToSearch);
	
			// Make sure there is no error
			if((lastIndex == -1) || (lastIndex < index))
				throw new Exception("Parse Error");
	
			// extract the substring
			String subs = xmlString.substring((index + beginTagToSearch.length()), lastIndex) ;
	
			// Add it to our list of tag values
			v.addElement(subs);
	
			// Try it again. Narrow down to the part of string which is not
			// processed yet.
			try
			{
				xmlString = xmlString.substring(lastIndex + endTagToSearch.length());
			}
			catch(Exception e)
			{
				xmlString = "";
			}
	
			// Start over again by searching the first occurrence of the begin tag
			// to continue the loop.
	
			index = xmlString.indexOf(beginTagToSearch);
		}	
		return v;
	}
}
