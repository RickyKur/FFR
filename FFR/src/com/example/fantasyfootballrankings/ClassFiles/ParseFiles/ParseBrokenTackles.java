package com.example.fantasyfootballrankings.ClassFiles.ParseFiles;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.example.fantasyfootballrankings.ClassFiles.HandleBasicQueries;
/**
 * Parses the broken tackle data to a hash map
 * to be used later
 * @author Jeff
 *
 */
public class ParseBrokenTackles {

	/**
	 * Actually parses the broken tackle data, then returns it
	 * @return
	 * @throws IOException
	 */
	public static HashMap<String, String> parseBrokenTackles() throws IOException
	{
		String data = HandleBasicQueries.handleLists(
				"http://www.footballoutsiders.com/stat-analysis/2013/broken-tackles-2012", "tr");
		String[] rows = data.split("\n");
		HashMap<String, String> bt = new HashMap<String, String>();
		for(int i = 0; i < rows.length; i++)
		{
			if(rows[i].contains("2012"))
			{
				continue;
			}
			String[] individual = rows[i].split(" ");
			bt.put(individual[0] + " " + individual[1], individual[3]);
		}
		return bt;
	}
}
