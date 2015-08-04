package com.example.fantasyfootballrankings.ClassFiles.LittleStorage;

/**
 * A little custom class to store all of the scoring the user inputs
 * 
 * @author Jeff
 * 
 */
public class Scoring {
	public int passYards;
	public int passTD;
	public int interception;
	public int rushYards;
	public int rushTD;
	public int fumble;
	public int recYards;
	public int recTD;
	public int catches;

	/**
	 * For the sake of ease later, checks, a default.
	 */
	public Scoring() {
		passYards = 0;
		passTD = 0;
		interception = 0;
		rushYards = 0;
		rushTD = 0;
		fumble = 0;
		recYards = 0;
		recTD = 0;
		catches = 0;
	}
}
