package com.example.fantasyfootballrankings.ClassFiles;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.ffr.fantasyfootballrankings.R;
import com.example.fantasyfootballrankings.ClassFiles.LittleStorage.Draft;
import com.example.fantasyfootballrankings.ClassFiles.LittleStorage.Flex;
import com.example.fantasyfootballrankings.ClassFiles.LittleStorage.Roster;
import com.example.fantasyfootballrankings.ClassFiles.LittleStorage.Scoring;
import com.example.fantasyfootballrankings.ClassFiles.StorageClasses.PlayerObject;
import com.example.fantasyfootballrankings.ClassFiles.StorageClasses.Storage;
import com.example.fantasyfootballrankings.ClassFiles.Utils.Constants;
import com.example.fantasyfootballrankings.Pages.Home;
import com.example.fantasyfootballrankings.Pages.Rankings;

import AsyncTasks.ParsingAsyncTask;
import AsyncTasks.StorageAsyncTask;
import AsyncTasks.ParsingAsyncTask.ParseProjections;
import AsyncTasks.StorageAsyncTask.WriteNewPAA;
import FileIO.ReadFromFile;
import FileIO.WriteToFile;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

/**
 * A little class that should help with making user input into searches...etc a
 * little cooler, doing nicer things.
 * 
 * @author Jeff
 * 
 */
public class ManageInput {
	static Scoring dummyScoring = new Scoring();
	static Roster dummyRoster = new Roster();
	static boolean doSyncData = false;

	/**
	 * This sets up the auto complete search with the given arraylist so that it
	 * autocompletes suggestions based on players who have already been parsed.
	 */

	public static List<Map<String, String>> sortData(
			List<Map<String, String>> data) {
		Collections.sort(data, new Comparator<Map<String, String>>() {
			public int compare(Map<String, String> a, Map<String, String> b) {
				String aNorm = a.get("main").toLowerCase();
				String bNorm = b.get("main").toLowerCase();
				int judgment = aNorm.compareTo(bNorm);
				if (judgment < 0) {
					return -1;
				}
				if (judgment > 0) {
					return 1;
				}
				return 0;
			}
		});
		return data;
	}

	public static List<String> sortSingleList(List<String> data) {
		Collections.sort(data);
		return data;
	}

	public static void setupAutoCompleteSearch(Storage holder,
			List<PlayerObject> players, AutoCompleteTextView input, Context cont) {

		final List<Map<String, String>> data = new ArrayList<Map<String, String>>();
		for (PlayerObject player : players) {
			Map<String, String> datum = new HashMap<String, String>(2);
			datum.put("main", player.info.name);
			if (!player.info.name.contains(Constants.DST)
					&& player.info.position.length() >= 1
					&& player.info.team.length() > 2) {
				datum.put("sub", player.info.position + " - "
						+ player.info.team);
			} else {
				datum.put("sub", "");
			}
			data.add(datum);
		}
		List<Map<String, String>> dataFinal = sortData(data);
		final SimpleAdapter mAdapter = new SimpleAdapter(cont, dataFinal,
				android.R.layout.simple_list_item_2, new String[] { "main",
						"sub" }, new int[] { android.R.id.text1,
						android.R.id.text2 });
		input.setAdapter(mAdapter);
	}

	/**
	 * Handles the filter quantity dialog
	 * 
	 * @param cont
	 */
	public static void filterQuantity(final Context cont, final String flag,
			final int listSize) {
		final Dialog dialog = new Dialog(cont, R.style.RoundCornersFull);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.filter_quantity);
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
		lp.copyFrom(dialog.getWindow().getAttributes());
		lp.width = WindowManager.LayoutParams.MATCH_PARENT;
		dialog.getWindow().setAttributes(lp);
		dialog.show();
		int filterSize = ReadFromFile.readFilterQuantitySize(cont, flag);
		final SeekBar selector = (SeekBar) dialog
				.findViewById(R.id.seekBar_quantity);
		final TextView display = (TextView) dialog
				.findViewById(R.id.quantity_display);
		if (filterSize == 0) {
			display.setText("None of the players");
		} else if (filterSize == 100) {
			display.setText("All of the players");
		} else {
			float percentage = (float) filterSize / 100;
			int total = (int) (listSize * percentage);
			String newSize = total + " players maximum";
			display.setText(newSize);
		}
		selector.setProgress(filterSize);
		selector.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
				int prog = selector.getProgress();
				String size = "";
				if (prog == 0) {
					size = "None of the players";
				} else if (prog == 100) {
					size = "All of the players";
				} else {
					float percentage = (float) prog / 100;
					int total = (int) (listSize * percentage);
					size = total + " players maximum";
				}
				display.setText(size);

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}
		});
		Button cancel = (Button) dialog.findViewById(R.id.filter_size_cancel);
		cancel.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		Button submit = (Button) dialog.findViewById(R.id.filter_size_submit);
		submit.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				WriteToFile.writeFilterSize(cont, selector.getProgress(), flag);
				dialog.dismiss();
				((Rankings) cont).intermediateHandleRankings((Activity) cont);
			}
		});
	}

	/**
	 * Sets up pass settings
	 * 
	 * @param cont
	 * @param scoring
	 */
	public static void passSettings(final Context cont, final Scoring scoring,
			final boolean doSync, final Storage holder) {
		doSyncData = doSync;
		final Dialog dialog = new Dialog(cont, R.style.RoundCornersFull);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.scoring_pass);
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
		lp.copyFrom(dialog.getWindow().getAttributes());
		lp.width = WindowManager.LayoutParams.MATCH_PARENT;
		dialog.getWindow().setAttributes(lp);
		dialog.show();
		final EditText yards = (EditText) dialog
				.findViewById(R.id.scoring_pass_yards);
		final EditText tds = (EditText) dialog
				.findViewById(R.id.scoring_pass_td);
		final EditText ints = (EditText) dialog
				.findViewById(R.id.scoring_pass_int);
		Button toRun = (Button) dialog.findViewById(R.id.scoring_pass_continue);
		toRun.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				String yardStr = yards.getText().toString();
				String tdStr = tds.getText().toString();
				String intStr = ints.getText().toString();
				if (isInteger(yardStr) && isInteger(tdStr) && isInteger(intStr)) {
					dummyScoring.passYards = Integer.parseInt(yardStr);
					dummyScoring.passTD = Integer.parseInt(tdStr);
					dummyScoring.interception = Integer.parseInt(intStr) * -1;
					dialog.dismiss();
					runSettings(cont, scoring, doSync, holder);
				} else {
					Toast.makeText(cont,
							"Please enter integer values greater than 0",
							Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	/**
	 * Sets up run settings
	 * 
	 * @param cont
	 * @param scoring
	 */
	public static void runSettings(final Context cont, final Scoring scoring,
			final boolean doSync, final Storage holder) {
		final Dialog dialog = new Dialog(cont, R.style.RoundCornersFull);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.scoring_run);
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
		lp.copyFrom(dialog.getWindow().getAttributes());
		lp.width = WindowManager.LayoutParams.MATCH_PARENT;
		dialog.getWindow().setAttributes(lp);
		dialog.show();
		final EditText yards = (EditText) dialog
				.findViewById(R.id.scoring_run_yards);
		final EditText tds = (EditText) dialog
				.findViewById(R.id.scoring_run_td);
		final EditText ints = (EditText) dialog
				.findViewById(R.id.scoring_run_int);
		Button back = (Button) dialog.findViewById(R.id.scoring_run_back);
		back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				passSettings(cont, scoring, doSync, holder);
			}
		});
		Button toRun = (Button) dialog.findViewById(R.id.scoring_run_continue);
		toRun.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				String yardStr = yards.getText().toString();
				String tdStr = tds.getText().toString();
				String intStr = ints.getText().toString();
				if (isInteger(yardStr) && isInteger(tdStr) && isInteger(intStr)) {
					dummyScoring.rushYards = Integer.parseInt(yardStr);
					dummyScoring.rushTD = Integer.parseInt(tdStr);
					dummyScoring.fumble = Integer.parseInt(intStr) * -1;
					dialog.dismiss();
					recSettings(cont, scoring, doSync, holder);
				} else {
					Toast.makeText(cont,
							"Please enter integer values greater than 0",
							Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	/**
	 * Sets up receiving numbers
	 * 
	 * @param cont
	 * @param scoring
	 */
	public static void recSettings(final Context cont, final Scoring scoring,
			final boolean doSync, final Storage holder) {
		final Dialog dialog = new Dialog(cont, R.style.RoundCornersFull);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.scoring_rec);
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
		lp.copyFrom(dialog.getWindow().getAttributes());
		lp.width = WindowManager.LayoutParams.MATCH_PARENT;
		dialog.getWindow().setAttributes(lp);
		dialog.show();
		final EditText yards = (EditText) dialog
				.findViewById(R.id.scoring_rec_yards);
		final EditText tds = (EditText) dialog
				.findViewById(R.id.scoring_rec_td);
		final EditText ints = (EditText) dialog
				.findViewById(R.id.scoring_rec_catch);
		Button back = (Button) dialog.findViewById(R.id.scoring_rec_back);
		back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				runSettings(cont, scoring, doSync, holder);
			}
		});
		Button toRun = (Button) dialog.findViewById(R.id.scoring_rec_continue);
		toRun.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				String yardStr = yards.getText().toString();
				String tdStr = tds.getText().toString();
				String intStr = ints.getText().toString();
				if (isInteger(yardStr) && isInteger(tdStr) && isInteger(intStr)) {
					dummyScoring.recYards = Integer.parseInt(yardStr);
					dummyScoring.recTD = Integer.parseInt(tdStr);
					dummyScoring.catches = Integer.parseInt(intStr);
					dialog.dismiss();
					WriteToFile.writeScoring("", cont, dummyScoring);
					if (doSyncData) {
						if (ManageInput.confirmInternet(cont)) {
							ParsingAsyncTask stupid = new ParsingAsyncTask();
							ParseProjections task = stupid.new ParseProjections(
									(Activity) cont, ((Home) cont).holder);
							task.execute(holder, cont);
							SharedPreferences.Editor editor = cont
									.getSharedPreferences(Constants.SP_KEY, 0).edit();
							editor.putBoolean("Home Update", true).apply();
							editor.putBoolean("Home Update Draft", true)
									.apply();
							editor.putBoolean("Home Update Trending", true)
									.apply();
							editor.putBoolean("Home Update Import", true)
									.apply();
						} else {
							Toast.makeText(
									cont,
									"No Internet Connection,  Can't Update Projections. Connect and Try Again, or Connect and Update Rankings",
									Toast.LENGTH_LONG).show();
						}
					}
				} else {
					Toast.makeText(cont,
							"Please enter integer values greater than 0",
							Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	/**
	 * Gets the roster/teams input from the user
	 */
	public static void getRoster(final Context cont, final boolean doSync,
			final Storage holder) {
		final Dialog dialog = new Dialog(cont, R.style.RoundCornersFull);
		doSyncData = doSync;
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.roster_selections);
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
		lp.copyFrom(dialog.getWindow().getAttributes());
		lp.width = WindowManager.LayoutParams.MATCH_PARENT;
		dialog.getWindow().setAttributes(lp);
		dialog.show();
		List<String> quantitiesQBTE = new ArrayList<String>();
		quantitiesQBTE.add("0");
		quantitiesQBTE.add("1");
		quantitiesQBTE.add("2");
		List<String> quantitiesRBWR = new ArrayList<String>();
		quantitiesRBWR.add("0");
		quantitiesRBWR.add("1");
		quantitiesRBWR.add("2");
		quantitiesRBWR.add("3");
		List<String> quantitiesTeam = new ArrayList<String>();
		quantitiesTeam.add("8");
		quantitiesTeam.add("10");
		quantitiesTeam.add("12");
		quantitiesTeam.add("14");
		quantitiesTeam.add("16");
		List<String> flexFlag = new ArrayList<String>();
		flexFlag.add("Yes");
		flexFlag.add("No");
		List<String> quantitiesK = new ArrayList<String>();
		quantitiesK.add("0");
		quantitiesK.add("1");
		final Spinner qb = (Spinner) dialog.findViewById(R.id.qb_quantity);
		final Spinner te = (Spinner) dialog.findViewById(R.id.te_quantity);
		ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
				cont, android.R.layout.simple_spinner_dropdown_item,
				quantitiesQBTE);
		qb.setAdapter(spinnerArrayAdapter);
		te.setAdapter(spinnerArrayAdapter);
		final Spinner rb = (Spinner) dialog.findViewById(R.id.rb_quantity);
		final Spinner wr = (Spinner) dialog.findViewById(R.id.wr_quantity);
		spinnerArrayAdapter = new ArrayAdapter<String>(cont,
				android.R.layout.simple_spinner_dropdown_item, quantitiesRBWR);
		rb.setAdapter(spinnerArrayAdapter);
		wr.setAdapter(spinnerArrayAdapter);
		final Spinner team = (Spinner) dialog.findViewById(R.id.team_quantity);
		spinnerArrayAdapter = new ArrayAdapter<String>(cont,
				android.R.layout.simple_spinner_dropdown_item, quantitiesTeam);
		team.setAdapter(spinnerArrayAdapter);
		final Spinner flex = (Spinner) dialog.findViewById(R.id.flex_quantity);
		final Spinner def = (Spinner) dialog
				.findViewById(R.id.defense_quantity);
		final Spinner k = (Spinner) dialog.findViewById(R.id.kicker_quantity);
		spinnerArrayAdapter = new ArrayAdapter<String>(cont,
				android.R.layout.simple_spinner_dropdown_item, flexFlag);
		flex.setAdapter(spinnerArrayAdapter);
		spinnerArrayAdapter = new ArrayAdapter<String>(cont,
				android.R.layout.simple_spinner_dropdown_item, quantitiesK);
		def.setAdapter(spinnerArrayAdapter);
		k.setAdapter(spinnerArrayAdapter);
		flex.setSelection(1);
		team.setSelection(1);
		wr.setSelection(2);
		rb.setSelection(2);
		qb.setSelection(1);
		te.setSelection(1);
		k.setSelection(1);
		def.setSelection(1);
		dummyRoster.flex = new Flex();
		flex.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				String selection = ((TextView) arg1).getText().toString();
				if (selection.equals("Yes")) {
					dummyRoster.flex = new Flex();
					handleFlexPopUp(cont, dummyRoster.flex);
				} else {
					dummyRoster.flex = null;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		Button submit = (Button) dialog.findViewById(R.id.roster_submit);
		submit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dummyRoster.qbs = Integer.parseInt((String) qb
						.getSelectedItem());
				dummyRoster.rbs = Integer.parseInt((String) rb
						.getSelectedItem());
				dummyRoster.wrs = Integer.parseInt((String) wr
						.getSelectedItem());
				dummyRoster.tes = Integer.parseInt((String) te
						.getSelectedItem());
				dummyRoster.teams = Integer.parseInt((String) team
						.getSelectedItem());
				dummyRoster.def = Integer.parseInt((String) def
						.getSelectedItem());
				dummyRoster.k = Integer.parseInt((String) k.getSelectedItem());
				WriteToFile.writeRoster("", cont, dummyRoster);
				dialog.dismiss();
				if (doSyncData) {

					StorageAsyncTask obj = new StorageAsyncTask();
					WriteNewPAA task2 = obj.new WriteNewPAA(cont, true, false);
					task2.execute(holder, cont);
					SharedPreferences.Editor editor = cont
							.getSharedPreferences(Constants.SP_KEY, 0).edit();
					editor.putBoolean("Home Update", true).apply();
					editor.putBoolean("Home Update Draft", true).apply();
					editor.putBoolean("Home Update Trending", true).apply();
					editor.putBoolean("Home Update Import", true).apply();

				}
			}
		});
	}

	/**
	 * Handles the flex pop up for all three major flexes
	 */
	public static void handleFlexPopUp(Context cont, final Flex newFlex) {
		final Dialog dialog = new Dialog(cont, R.style.RoundCornersFull);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.flex_layout_roster);
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
		lp.copyFrom(dialog.getWindow().getAttributes());
		lp.width = WindowManager.LayoutParams.MATCH_PARENT;
		dialog.getWindow().setAttributes(lp);
		dialog.show();
		final Spinner rbwr = (Spinner) dialog.findViewById(R.id.rb_wr_quantity);
		final Spinner rbwrte = (Spinner) dialog
				.findViewById(R.id.rb_wr_te_quantity);
		final Spinner op = (Spinner) dialog.findViewById(R.id.op_quantity);
		List<String> quantitiesQBTE = new ArrayList<String>();
		quantitiesQBTE.add("0");
		quantitiesQBTE.add("1");
		ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
				cont, android.R.layout.simple_spinner_dropdown_item,
				quantitiesQBTE);
		rbwr.setAdapter(spinnerArrayAdapter);
		rbwrte.setAdapter(spinnerArrayAdapter);
		op.setAdapter(spinnerArrayAdapter);
		Button submit = (Button) dialog.findViewById(R.id.roster_submit);
		submit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				newFlex.rbwr = Integer.valueOf(rbwr.getSelectedItem()
						.toString());
				newFlex.rbwrte = Integer.valueOf(rbwrte.getSelectedItem()
						.toString());
				newFlex.op = Integer.valueOf(op.getSelectedItem().toString());
				dialog.dismiss();
			}
		});
	}

	/**
	 * Decides if it's an auction or snake
	 * 
	 * @param cont
	 */
	public static void isAuctionOrSnake(final Context cont, final Storage holder) {
		final Dialog dialog = new Dialog(cont, R.style.RoundCornersFull);
		boolean isAuction = ReadFromFile.readIsAuction(cont);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.is_auction);
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
		lp.copyFrom(dialog.getWindow().getAttributes());
		lp.width = WindowManager.LayoutParams.MATCH_PARENT;
		dialog.getWindow().setAttributes(lp);
		dialog.show();
		Button close = (Button) dialog.findViewById(R.id.auction_close);
		close.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		RadioButton auction = (RadioButton) dialog
				.findViewById(R.id.is_auction);
		RadioButton snake = (RadioButton) dialog.findViewById(R.id.is_snake);
		if (isAuction && !auction.isChecked()) {
			auction.toggle();
		} else if (!isAuction && !snake.isChecked()) {
			snake.toggle();
		}
		final EditText salary = (EditText) dialog
				.findViewById(R.id.auction_salary_input);
		final TextView prompt = (TextView) dialog
				.findViewById(R.id.auction_salary_prompt);
		if (snake.isChecked()) {
			salary.setVisibility(View.GONE);
			prompt.setVisibility(View.GONE);
		}
		snake.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				if (arg1) {
					salary.setVisibility(View.GONE);
					prompt.setVisibility(View.GONE);
				} else {
					salary.setVisibility(View.VISIBLE);
					prompt.setVisibility(View.VISIBLE);
				}
			}
		});
		final RadioButton a = auction;
		final RadioButton s = snake;
		Button submit = (Button) dialog.findViewById(R.id.is_auction_submit);
		submit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				boolean isAuction = ReadFromFile.readIsAuction(cont);
				boolean needReset = false;
				String input = salary.getText().toString();
				if (s.isChecked()
						|| (ManageInput.isInteger(input) && a.isChecked() && Integer
								.parseInt(input) > 0)) {
					SharedPreferences.Editor editor = cont
							.getSharedPreferences(Constants.SP_KEY, 0).edit();
					editor.putBoolean("Home Update", true).apply();
					if ((!isAuction && a.isChecked())
							|| (isAuction && s.isChecked())) {
						needReset = true;
					}
					if (a.isChecked()) {
						double aucFactor = 200.0 / Integer.parseInt(input);
						WriteToFile.writeIsAuction(true, cont, aucFactor,
								holder);
					} else {
						WriteToFile.writeIsAuction(false, cont, 1.0, holder);
					}
					if (needReset) {
						Draft.resetDraftRemote(holder.draft, cont);
					}
					dialog.dismiss();
				} else {
					Toast.makeText(
							cont,
							"Please enter a number greater than 0 for the salary",
							Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	/**
	 * Makes sure a string is valid
	 * 
	 * @param s
	 * @return
	 */
	public static boolean isInteger(String s) {
		try {
			Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	/**
	 * Makes sure a string is a double
	 * 
	 * @param s
	 * @return
	 */
	public static boolean isDouble(String s) {
		try {
			Double.parseDouble(s);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	/**
	 * Sees if there is an internet connection, true if yes, false if no
	 * 
	 * @param cont
	 * @return
	 */
	public static boolean confirmInternet(Context cont) {
		ConnectivityManager connectivityManager = (ConnectivityManager) cont
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	/**
	 * Just to eek out tiny bits of performance improvements
	 * 
	 * @param s
	 * @param key
	 * @param keyLen
	 * @return
	 */
	public static String[] tokenize(String s, char key, int keyLen) {
		char[] c = s.toCharArray();
		LinkedList<String> ll = new LinkedList<String>();
		int index = 0;
		for (int i = 0; i < c.length; i++) {
			if (c[i] == key && ((keyLen > 1 && c[i + 1] == key) || keyLen == 1)
					|| i + 1 == c.length) {
				if (i + 1 == c.length) {
					ll.add(s.substring(index, i + 1));
				} else {
					ll.add(s.substring(index, i));
				}
				index = i + keyLen;
				i += keyLen - 1;
			}
		}
		String[] allData = new String[ll.size()];
		Iterator<String> iter = ll.iterator();
		for (index = 0; iter.hasNext(); index++) {
			allData[index] = iter.next();
		}
		return allData;
	}

	/**
	 * Takes a string and fixes it's case to be normal
	 * 
	 * @param line
	 * @return
	 */
	public static String capitalize(String line) {
		return Character.toUpperCase(line.charAt(0))
				+ line.substring(1).toLowerCase();
	}

	public static void generalHelp(Context cont) {
		final Dialog dialog = new Dialog(cont, R.style.RoundCornersFull);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.one_line_text);
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
		lp.copyFrom(dialog.getWindow().getAttributes());
		lp.width = WindowManager.LayoutParams.MATCH_PARENT;
		dialog.getWindow().setAttributes(lp);
		dialog.show();
		Button close = (Button) dialog.findViewById(R.id.player_stats_close);
		close.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		TextView help = (TextView) dialog.findViewById(R.id.textView1);
		StringBuilder helpStr = new StringBuilder(1000);
		helpStr.append("There are two aspects to this app - the preseason mode and the regular season mode. The regular season mode automatically kicks in as week 1 approaches, and is persistent through the year.\n\n");
		helpStr.append("In the preseason mode, you can draft players (through Rankings you swipe players to the side or through the player info pop up)"
				+ ", and once you're done the rankings reset. To ideally do this, you should set your scoring settings and roster settings through the home screen, "
				+ "as these are used in calculations.\n"
				+ "There are many features in Rankings to help you with this, such as a Who should I draft?, advanced sorting, draft info to help you keep up, "
				+ "a widget to do the same, a search (with the same output as clicking on a player), and a dynamic watch list.\n\n"
				+ "Now, in regular season mode, you can no longer draft players. In addition to that, the player information listed differs - it shifts from projections...etc. to aggregate stats that "
				+ "automatically update with every sync you make through the season and weekly projections/rankings.\n\n"
				+ "My Leagues lets you import leagues you have from ESPN or public Yahoo leagues and do some advanced analysis on those leagues. Each league gets mapped to whatever roster and scoring settings are input at import, but these can be changed.\n"
				+ "Other than that, it's your handy tool to everything you need to manage your team optimally.\nThe only differences between preseason and regular season mode "
				+ "is in the list of players, and that's similar to Rankings -- small differences in what output there is in terms of stats");
		help.setText(helpStr.toString());

	}

	public static float pixelsToSp(Context context, Float px) {
		float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
		return px / scaledDensity;
	}
}
