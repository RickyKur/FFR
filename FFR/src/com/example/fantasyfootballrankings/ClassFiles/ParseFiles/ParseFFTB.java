package com.example.fantasyfootballrankings.ClassFiles.ParseFiles;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;

import org.htmlcleaner.XPatherException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.example.fantasyfootballrankings.ClassFiles.HighLevel;
import com.example.fantasyfootballrankings.ClassFiles.ManageInput;
import com.example.fantasyfootballrankings.ClassFiles.ParseRankings;
import com.example.fantasyfootballrankings.ClassFiles.StorageClasses.PlayerObject;
import com.example.fantasyfootballrankings.ClassFiles.StorageClasses.Storage;
import com.example.fantasyfootballrankings.ClassFiles.Utils.Constants;
import com.example.fantasyfootballrankings.ClassFiles.Utils.HandleBasicQueries;
import com.example.fantasyfootballrankings.Pages.Home;

/**
 * Handles parsing from fantasy football toolbox's rankings
 * 
 * @author Jeff
 * 
 */
public class ParseFFTB {
	/**
	 * Parses each of the top four pages of the fftoolbox rankings
	 * 
	 * @param holder
	 * @throws XPatherException
	 * @throws IOException
	 * @throws MalformedURLException
	 */
	public static void parseFFTBRankingsWrapper(Storage holder)
			throws MalformedURLException, IOException, XPatherException {
		parseFFTBPage(
				holder,
				"http://www.fftoolbox.com/football/" + Home.yearKey + "/auction-values.cfm?pos=QB&teams=12&budget=200", Constants.QB);
		parseFFTBPage(
				holder,
				"http://www.fftoolbox.com/football/" + Home.yearKey + "/auction-values.cfm?pos=RB&teams=12&budget=200", Constants.RB);
		parseFFTBPage(
				holder,
				"http://www.fftoolbox.com/football/" + Home.yearKey + "/auction-values.cfm?pos=WR&teams=12&budget=200", Constants.WR);
		parseFFTBPage(
				holder,
				"http://www.fftoolbox.com/football/" + Home.yearKey + "/auction-values.cfm?pos=TE&teams=12&budget=200", Constants.TE);
		parseFFTBPage(
				holder,
				"http://www.fftoolbox.com/football/" + Home.yearKey + "/auction-values.cfm?pos=PK&teams=12&budget=200", Constants.DST);
		parseFFTBPage(
				holder,
				"http://www.fftoolbox.com/football/" + Home.yearKey + "/auction-values.cfm?pos=Def&teams=12&budget=200", Constants.K);
	}

	/**
	 * Does the individual, per page work
	 * 
	 * @param holder
	 *            the storage unit to write to
	 * @param url
	 *            the url to be parsed
	 * @throws XPatherException
	 * @throws IOException
	 * @throws MalformedURLException
	 */
	public static void parseFFTBPage(Storage holder, String url, String pos)
			throws MalformedURLException, IOException, XPatherException {
		List<String> brokenUp = HandleBasicQueries.handleLists(url, "td");
		int min = 0;
		for (int i = 0; i < brokenUp.size(); i++) {
			if (ManageInput.isInteger(brokenUp.get(i))) {
                min = i + 1;
                break;
            }
		}
		for (int i = min; i < brokenUp.size(); i += 8) {
			String name = "";
			String team = "";
			if (brokenUp.get(i + 2).equals("Def")) {
                team = ParseRankings.fixTeams(brokenUp.get(i + 1));
                name = ParseRankings.fixDefenses(team);
			} else {
				team = ParseRankings.fixTeams(brokenUp.get(i + 1));
				name = ParseRankings.fixNames(brokenUp.get(i));
			}
			String age = brokenUp.get(i + 4);
			String val = brokenUp.get(i + 6);
			if (team.split(" ").length <= 3) {
				val = val.substring(1, val.length());
				PlayerObject newPlayer = new PlayerObject("", "", "", 0);
				try {
					newPlayer = new PlayerObject(name, team, pos,
							Integer.parseInt(val));
				} catch (NumberFormatException e) {
					break;
				}
				PlayerObject match = Storage.pqExists(holder, name);
				if (match != null) {
					match.info.age = age;
				}
				newPlayer.info.age = age;
				ParseRankings.handlePlayer(holder, newPlayer, match);
			}
		}
	}

	/**
	 * Parses the bye weeks into a hashmap
	 * 
	 * @return
	 * @throws IOException
	 */
	public static HashMap<String, String> parseByeWeeks() throws IOException {
		HashMap<String, String> byes = new HashMap<String, String>();
		List<String> brokenUp = HandleBasicQueries.handleLists(
				"http://www.fftoolbox.com/football/byeweeks.cfm", "td");
		int floor = 0;
		for (int i = 0; i < brokenUp.size(); i++) {
			if (brokenUp.get(i).contains("Week")) {
				floor = i;
				break;
			}
		}
		for (int i = floor; i < brokenUp.size(); i += 2) {
			String week = brokenUp.get(i);
			String[] teamSet = brokenUp.get(i + 1).split(", ");
			for (String team : teamSet) {
				String newTeam = ParseRankings.fixTeams(team);
				byes.put(newTeam, week);
			}
		}
		return byes;
	}

	/**
	 * Calls the worker to handle all of the various pages
	 */
	public static void parseSOSInSeason(Storage holder) throws IOException {
		HashMap<String, Integer> sos = new HashMap<String, Integer>();
		Document testDoc = Jsoup
				.connect(
				"http://www.fftoolbox.com/football/" + Home.yearKey
						+ "/points-allowed.cfm?pos=QB")
				.get();
		if (testDoc.html().contains("Coming Soon")) {
			HighLevel.getSOS(holder);
			return;
		}
		parseSOSWorker(
"http://www.fftoolbox.com/football/" + Home.yearKey
				+ "/points-allowed.cfm?pos=QB",
				sos, Constants.QB);
		parseSOSWorker(
"http://www.fftoolbox.com/football/" + Home.yearKey
				+ "/points-allowed.cfm?pos=RB",
				sos, Constants.RB);
		parseSOSWorker(
"http://www.fftoolbox.com/football/" + Home.yearKey
				+ "/points-allowed.cfm?pos=WR",
				sos, Constants.WR);
		parseSOSWorker(
"http://www.fftoolbox.com/football/" + Home.yearKey
				+ "/points-allowed.cfm?pos=TE",
				sos, Constants.TE);
		parseSOSWorker(
"http://www.fftoolbox.com/football/" + Home.yearKey
				+ "/points-allowed.cfm?pos=K",
				sos, Constants.K);
		holder.sos = sos;
	}

	/**
	 * Does the per page parsing of the positional SOS data
	 */
	public static void parseSOSWorker(String url, HashMap<String, Integer> sos,
			String pos) throws IOException {
		List<String> trSet = HandleBasicQueries.handleLists(url, "tr");
		for (int i = 0; i < trSet.size(); i++) {
			String[] trWords = trSet.get(i).split(" ");
			if (ManageInput.isInteger(trWords[0])) {
				StringBuilder team = new StringBuilder(100);
				for (int j = 1; j < trWords.length; j++) {
					if (!ManageInput.isInteger(trWords[j])) {
						team.append(trWords[j] + " ");
					} else {
						break;
					}
				}
				String teamStr = team.toString();
				teamStr = ParseRankings.fixTeams(teamStr.substring(0,
						teamStr.length() - 1));
				sos.put(teamStr + "," + pos, Integer.valueOf(trWords[0]));
			}
		}
	}
}
