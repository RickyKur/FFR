package com.example.fantasyfootballrankings.Pages;

import java.io.IOException;



import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ExecutionException;

import org.htmlcleaner.XPatherException;

import com.example.fantasyfootballrankings.R;
import com.example.fantasyfootballrankings.R.id;
import com.example.fantasyfootballrankings.R.layout;
import com.example.fantasyfootballrankings.R.menu;
import com.example.fantasyfootballrankings.ClassFiles.ComparatorHandling;
import com.example.fantasyfootballrankings.ClassFiles.HandleWatchList;
import com.example.fantasyfootballrankings.ClassFiles.HighLevel;
import com.example.fantasyfootballrankings.ClassFiles.ManageInput;
import com.example.fantasyfootballrankings.ClassFiles.ParseRankings;
import com.example.fantasyfootballrankings.ClassFiles.PlayerInfo;
import com.example.fantasyfootballrankings.ClassFiles.PlayerObject;
import com.example.fantasyfootballrankings.ClassFiles.SortHandler;
import com.example.fantasyfootballrankings.ClassFiles.Storage;
import com.example.fantasyfootballrankings.ClassFiles.TradeHandling;
import com.example.fantasyfootballrankings.ClassFiles.LittleStorage.BasicInfo;
import com.example.fantasyfootballrankings.ClassFiles.LittleStorage.Draft;
import com.example.fantasyfootballrankings.ClassFiles.LittleStorage.PostedPlayer;
import com.example.fantasyfootballrankings.ClassFiles.LittleStorage.Scoring;
import com.example.fantasyfootballrankings.ClassFiles.ParseFiles.ParseTrending;
import com.example.fantasyfootballrankings.InterfaceAugmentations.*;

import AsyncTasks.StorageAsyncTask;
import AsyncTasks.StorageAsyncTask.ReadDraft;
import FileIO.ReadFromFile;
import FileIO.WriteToFile;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.speech.RecognizerIntent;
import android.view.GestureDetector;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TwoLineListItem;
import android.widget.AdapterView.OnItemClickListener;
/**
 * Handles the rankings part of the java file
 * 
 * @author Jeff
 *
 */
public class Rankings extends Activity {
	final Context cont = this;
	public static Context newCont;
	public static Context context;
	public static Storage holder = new Storage();
	public static Button voice;
	public static AutoCompleteTextView textView;
	private static final int REQUEST_CODE = 1234;
	static Dialog dialog;
	public static List<String> matchedPlayers;
	static Button search;
	static Button info;
	static Button compare;
	static Button calc;
	static Button sort;
	public static ListView listview;
	static boolean refreshed = false;
	static int sizeOutput = -1;
	static String teamFilter = "";
	static String posFilter = "";
	static List<String> teamList = new ArrayList<String>();
	static List<String> posList = new ArrayList<String>();
	public static List<String> watchList = new ArrayList<String>();
	public static List<Map<String, String>> data;
	public static SimpleAdapter adapter;
	static SwipeDismissListViewTouchListener touchListener;
	static Scoring scoring = new Scoring();
	public static boolean isAuction;
	/**
	 * Sets up the view
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		watchList.clear();
		setContentView(R.layout.activity_rankings);
		search = (Button)findViewById(R.id.search);
		info = (Button)findViewById(R.id.draft_info);
		compare = (Button)findViewById(R.id.player_comparator);
		calc = (Button)findViewById(R.id.trade_calc);
		sort = (Button)findViewById(R.id.sort_players);
    	listview = (ListView)findViewById(R.id.listview_rankings);
    	context = this;
    	isAuction = ReadFromFile.readIsAuction(cont);
    	setLists();
		handleRefresh();
		handleOnClickButtons();
	}
	
	/**
	 * Sets up the menu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.rankings, menu);
		return true;
	}
	
	/**
	 * Runs the on selection part of the menu
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		dialog = new Dialog(cont, R.style.RoundCornersFull);
		switch (item.getItemId()) 
		{
			case R.id.watch_list:
				watchList = ReadFromFile.readWatchList(context);
				if(watchList.size() > 0 && holder.parsedPlayers.contains(watchList.get(0)))
				{
					HandleWatchList.handleWatchInit(holder, cont, watchList);
				}
				else
				{
					Toast.makeText(context, "Watch list is empty", Toast.LENGTH_SHORT).show();
				}
				return true;
			case R.id.refresh:
				refreshRanks(dialog);
		    	return true;
			case R.id.filter_topics_rankings:
				if(holder.players.size() > 10)
				{
					filterTopics(dialog);
				}
				else
				{
					Toast.makeText(context, "Can't filter the rankings until they're fetched", Toast.LENGTH_SHORT).show();
				}
				return true;
			case R.id.filter_quantity_menu:
				if(holder.players.size() > 10)
				{
					filterQuantity();
				}
				else
				{
					Toast.makeText(context, "Can't filter the quantity of rankings until they're fetched", Toast.LENGTH_SHORT).show();
				}
				return true;
			case R.id.help:
				helpDialog();
				return true;
			//New page opens up entirely for going home
			case R.id.go_home:
				Intent home_intent = new Intent(cont, Home.class);
				cont.startActivity(home_intent);		
		        return true;
			case R.id.news:
		        Intent intent_news = new Intent(cont, News.class);
		        cont.startActivity(intent_news);		
 		        return true;
 		    //New page opens up entirely for viewing trending players
			case R.id.view_trending:
		        Intent team_intent = new Intent(cont, Trending.class);
		        cont.startActivity(team_intent);		
				return true;
			
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	/**
	 * Handles the help dialog
	 */
	public void helpDialog() {
		final Dialog dialog = new Dialog(cont, R.style.RoundCornersFull);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.help_rankings);
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
	    lp.copyFrom(dialog.getWindow().getAttributes());
	    lp.width = WindowManager.LayoutParams.FILL_PARENT;
	    dialog.getWindow().setAttributes(lp);
	    dialog.show();
	    Button close = (Button)dialog.findViewById(R.id.help_rankings_close);
	    close.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
	    });
	}
    
	/**
	 * Populate team/pos lists
	 */
	public static void setLists()
	{
		if(posList.size() != 7)
		{
			posList.add("All Positions");
			posList.add("QB");
			posList.add("RB");
			posList.add("WR");
			posList.add("TE");
			posList.add("D/ST");
			posList.add("K");
		}
		if(teamList.size() != 33)
		{
			teamList.add("All Teams");
			teamList.add("Arizona Cardinals");
			teamList.add("Atlanta Falcons");
			teamList.add("Baltimore Ravens");
			teamList.add("Buffalo Bills");
			teamList.add("Carolina Panthers");
			teamList.add("Chicago Bears");
			teamList.add("Cincinnati Bengals");
			teamList.add("Cleveland Browns");
			teamList.add("Dallas Cowboys");
			teamList.add("Denver Broncos");
			teamList.add("Detroit Lions");
			teamList.add("Green Bay Packers");
			teamList.add("Houston Texans");
			teamList.add("Indianapolis Colts");
			teamList.add("Jacksonville Jaguars");
			teamList.add("Kansas City Chiefs");
			teamList.add("Miami Dolphins");
			teamList.add("Minnesota Vikings");
			teamList.add("New England Patriots");
			teamList.add("New Orleans Saints");
			teamList.add("New York Giants");
			teamList.add("New York Jets");
			teamList.add("Oakland Raiders");
			teamList.add("Philadelphia Eagles");
			teamList.add("Pittsburgh Steelers");
			teamList.add("San Diego Chargers");
			teamList.add("San Francisco 49ers");
			teamList.add("Seattle Seahawks");
			teamList.add("St. Louis Rams");
			teamList.add("Tampa Bay Buccaneers");
			teamList.add("Tennessee Titans");
			teamList.add("Washington Redskins");
		}
		if(watchList.size() == 0)
		{
			watchList = ReadFromFile.readWatchList(context);
		}
	}
	
	/**
	 * handles relavent filter dialog
	 * @param dialog2
	 */
	public static void filterTopics(final Dialog dialog2) 
	{
		final Dialog dialog = new Dialog(context, R.style.RoundCornersFull);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.rankings_filter);
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
	    lp.copyFrom(dialog.getWindow().getAttributes());
	    lp.width = WindowManager.LayoutParams.FILL_PARENT;
	    dialog.getWindow().setAttributes(lp);
		Button cancel = (Button)dialog.findViewById(R.id.rankings_filter_close);
		cancel.setOnClickListener(new OnClickListener() 
		{
			public void onClick(View v) {
				dialog.dismiss();
	    	}	
		});
		posList.clear();
		posList.add("All Positions");
		posList.add("QB");
		posList.add("RB");
		posList.add("WR");
		posList.add("TE");
		posList.add("D/ST");
		posList.add("K");
		teamList.clear();
		teamList.add("All Teams");
		teamList.add("Arizona Cardinals");
		teamList.add("Atlanta Falcons");
		teamList.add("Baltimore Ravens");
		teamList.add("Buffalo Bills");
		teamList.add("Carolina Panthers");
		teamList.add("Chicago Bears");
		teamList.add("Cincinnati Bengals");
		teamList.add("Cleveland Browns");
		teamList.add("Dallas Cowboys");
		teamList.add("Denver Broncos");
		teamList.add("Detroit Lions");
		teamList.add("Green Bay Packers");
		teamList.add("Houston Texans");
		teamList.add("Indianapolis Colts");
		teamList.add("Jacksonville Jaguars");
		teamList.add("Kansas City Chiefs");
		teamList.add("Miami Dolphins");
		teamList.add("Minnesota Vikings");
		teamList.add("New England Patriots");
		teamList.add("New Orleans Saints");
		teamList.add("New York Giants");
		teamList.add("New York Jets");
		teamList.add("Oakland Raiders");
		teamList.add("Philadelphia Eagles");
		teamList.add("Pittsburgh Steelers");
		teamList.add("San Diego Chargers");
		teamList.add("San Francisco 49ers");
		teamList.add("Seattle Seahawks");
		teamList.add("St. Louis Rams");
		teamList.add("Tampa Bay Buccaneers");
		teamList.add("Tennessee Titans");
		teamList.add("Washington Redskins");
		if(teamFilter.length() < 3)
		{
			teamFilter = "All Teams";
		}
		if(posFilter.length() < 2 && !posFilter.equals("K"))
		{
			posFilter = "All Teams";
		}
		final Spinner pos = (Spinner)dialog.findViewById(R.id.position_spinner);
		final Spinner teams = (Spinner)dialog.findViewById(R.id.team_spinner);
		ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(context, 
				android.R.layout.simple_spinner_dropdown_item, posList);
		pos.setAdapter(spinnerArrayAdapter);
		ArrayAdapter<String> spinnerArrayAdapte2r = new ArrayAdapter<String>(context, 
				android.R.layout.simple_spinner_dropdown_item, teamList);
		teams.setAdapter(spinnerArrayAdapte2r);
		teams.setSelection(teamList.indexOf(teamFilter));
		pos.setSelection(posList.indexOf(posFilter));

		//Actual non-initialization work
		Button submit = (Button)dialog.findViewById(R.id.filter_rankings_submit);
		submit.setOnClickListener(new OnClickListener() 
		{
			public void onClick(View v) 
			{
				teamFilter = teams.getSelectedItem().toString();
				posFilter = pos.getSelectedItem().toString();
				if(!teamFilter.contains("All"))
				{
					teamList.clear();
					teamList.add(teamFilter);
				}
				if(!posFilter.contains("All"))
				{
					posList.clear();
					posList.add(posFilter);
				}
				intermediateHandleRankings((Rankings)context);
				dialog.dismiss();
	    	}	
		});
		dialog.show();
	}

	/**
	 * Handles the refreshing of the rankings/user input to do so
	 * @param dialog
	 */
	public void refreshRanks(final Dialog dialog)
	{
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.refresh); 
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
	    lp.copyFrom(dialog.getWindow().getAttributes());
	    lp.width = WindowManager.LayoutParams.FILL_PARENT;
	    dialog.getWindow().setAttributes(lp);
		Button refreshDraft = (Button)dialog.findViewById(R.id.reset_draft);
		refreshDraft.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Draft.resetDraft(holder.draft, holder, context);	
				dialog.dismiss();
			}
		});
		Button refreshDismiss = (Button)dialog.findViewById(R.id.refresh_cancel);
		refreshDismiss.setOnClickListener(new OnClickListener() 
		{
			public void onClick(View v) {
				dialog.dismiss();
	    	}	
		});
		Button refreshSubmit = (Button)dialog.findViewById(R.id.refresh_confirm);
		refreshSubmit.setOnClickListener(new OnClickListener() 
		{
			public void onClick(View v) {
				refreshed = true;
				listview.setAdapter(null);
				dialog.dismiss();
				try {
					ParseRankings.runRankings(holder, cont);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (XPatherException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
	    	}	
		});
    	dialog.show();	
	}
	
	/**
	 * Handles the possible loading of the players
	 */
	public void handleRefresh()
	{
		View v = findViewById(android.R.id.home);
		v.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				listview.smoothScrollToPosition(0);		
			}
		});
		ReadFromFile.fetchNamesBackEnd(holder, cont);
		SharedPreferences prefs = cont.getSharedPreferences("FFR", 0); 
    	String checkExists = prefs.getString("Player Values", "Not Set");
    	if(!checkExists.equals("Not Set"))
    	{
			try {
				/**
				 * HERE
				 */
				ReadFromFile.fetchPlayers(holder,cont, true);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (XPatherException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	else
    	{
    		try {
				ParseRankings.runRankings(holder, cont);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (XPatherException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
	}
	
	/**
	 * Sets onclick of the button bar
	 */
	public void handleOnClickButtons()
	{
		dialog = new Dialog(cont, R.style.RoundCornersFull);
		//Handle the moreinfo click
		info.setOnClickListener(new View.OnClickListener() 
	    {
	          @Override
	          public void onClick(View v) 
	          {
	        	  moreInfo(new Dialog(context, R.style.RoundCornersFull));
	          }
	    });    
		//Handle the search onclick
		search.setOnClickListener(new View.OnClickListener() 
	    {
			
	          @Override
	          public void onClick(View v) 
	          {
	        	  try {
	        		  PlayerInfo.searchCalled(cont);
	        	  } catch (IOException e) {
						// TODO Auto-generated catch block
	        		  e.printStackTrace();
	        	  }
	          }
	    });  
		compare.setOnClickListener(new View.OnClickListener()
		{
	          @Override
	          public void onClick(View v) 
	          {
	        	  ComparatorHandling.handleComparingInit(holder, cont);
	          }
		});
		
		//Calculator pop up on click
		calc.setOnClickListener(new View.OnClickListener() 
	    {
	          @Override
	          public void onClick(View v) 
	          {
	        	  TradeHandling.handleTradeInit(holder, cont);
	          }
	    }); 
		//sort pop up on click
		sort.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				SortHandler.initialPopUp(cont, holder);
			}
		});
	}
	
	/**
	 * Calls the function that handles filtering 
	 * quantity size
	 */
	public void filterQuantity()
	{
		if(sizeOutput == -1)
		{
			sizeOutput = holder.players.size();
		}
		ManageInput.filterQuantity(cont,"Rankings", sizeOutput);		
	}
	
	/**
     * Handle the action of the button being clicked
     */
    public void speakButtonClicked(View v)
    {
        startVoiceRecognitionActivity();
    }
    
    /**
     * Fire an intent to start the voice recognition activity.
     */
    private void startVoiceRecognitionActivity()
    {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Enter the player you'd like to search for");
        startActivityForResult(intent, REQUEST_CODE);
    }
    
    /**
     * Handle the results from the voice recognition activity.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK)
        {
            // Populate the wordsList with the String values the recognition engine thought it heard
            ArrayList<String> matches = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            matchedPlayers = ManageInput.voiceInput(matches, newCont, holder, textView);
            if(matchedPlayers.size() != 0)
            {
            	double maxVal = 0.0;
            	String maxPlayer = "";
            	for(String player : matchedPlayers)
            	{
            		for(int i = 0; i < holder.players.size(); i++)
            		{
            			PlayerObject playerIter = holder.players.get(i);
            			if(playerIter.info.name.equals(player))
            			{
            				if(playerIter.values.worth > maxVal)
            				{
            					maxPlayer = playerIter.info.name;
            					maxVal = playerIter.values.worth;
            				}
                			break;
            			}
            		}
            	}            	
            	textView.setText(maxPlayer);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    
    /**
     * Sets the dialog to handle the salary/value information
     * @param dialog
     */
    public static void moreInfo(final Dialog dialog)
    {
    	DecimalFormat df = new DecimalFormat("#.##");
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.value_salary); 
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
	    lp.copyFrom(dialog.getWindow().getAttributes());
	    lp.width = WindowManager.LayoutParams.FILL_PARENT;
	    dialog.getWindow().setAttributes(lp);
		String salRem = Integer.toString(holder.draft.remainingSalary);
		String value = Integer.toString((int)holder.draft.value); 
		TextView remSalary = (TextView)dialog.findViewById(R.id.remSalary);
		TextView draftVal = (TextView)dialog.findViewById(R.id.draftValue);
		TextView paaView = (TextView)dialog.findViewById(R.id.draft_paa);
		TextView paapdView = (TextView)dialog.findViewById(R.id.draft_paapd);
		double paa = 0.0;
		double paapd = 0.0;
		if(holder.draft.remainingSalary != 200)
		{
			draftVal.setVisibility(View.VISIBLE);
			paa = Draft.paaTotal(holder.draft);
			paapd = paa / (200 - holder.draft.remainingSalary);
			paaView.setVisibility(View.VISIBLE);
			paaView.setText("PAA total: " + df.format(paa));
			paapdView.setVisibility(View.VISIBLE);
			paapdView.setText("PAA per dollar: " + df.format(paapd));
		}
		else
		{
			paaView.setVisibility(View.GONE);
			paapdView.setVisibility(View.GONE);
			draftVal.setVisibility(View.GONE);
		}
		ProgressBar salBar = (ProgressBar)dialog.findViewById(R.id.progressBar1);
		remSalary.setText("Salary Left: $" + salRem);
		draftVal.setText("Value Thus Far: $" + value);
		salBar.setProgress(Integer.parseInt(salRem));		
		Button svDismiss = (Button)dialog.findViewById(R.id.salValDismiss);
		svDismiss.setOnClickListener(new OnClickListener() 
		{
			public void onClick(View v) {
				dialog.dismiss();
	    	}	
		});
		Button svInfo = (Button)dialog.findViewById(R.id.more_info);
		svInfo.setOnClickListener(new OnClickListener() 
		{
			public void onClick(View v) {
				dialog.dismiss();
				handleInfo(new Dialog(context, R.style.RoundCornersFull));
	    	}	
		});
		Button unDraft = (Button)dialog.findViewById(R.id.undraft);
		unDraft.setOnClickListener(new OnClickListener() 
		{
			public void onClick(View v) {
				dialog.dismiss();
				Draft.undraft(new Dialog(context), holder, context);
	    	}	
		});
		Button valLeft = (Button)dialog.findViewById(R.id.value_left);
		valLeft.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				posValLeft(new Dialog(context, R.style.RoundCornersFull), holder, context);
			}
		});
		if(!isAuction)
		{
			remSalary.setVisibility(View.GONE);
			draftVal.setVisibility(View.GONE);
			paapdView.setVisibility(View.GONE);
			salBar.setVisibility(View.GONE);
		}
    	dialog.show();
    }

	public static void posValLeft(final Dialog dialog, Storage holder, final Context context) 
	{
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.paa_pos_left); 
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
	    lp.copyFrom(dialog.getWindow().getAttributes());
	    lp.width = WindowManager.LayoutParams.FILL_PARENT;
	    dialog.getWindow().setAttributes(lp);
	    dialog.show();
	    TextView qbLeft = (TextView)dialog.findViewById(R.id.qb_paa_left);
	    TextView rbLeft = (TextView)dialog.findViewById(R.id.rb_paa_left);
	    TextView wrLeft = (TextView)dialog.findViewById(R.id.wr_paa_left);
	    TextView teLeft = (TextView)dialog.findViewById(R.id.te_paa_left);
	    DecimalFormat df = new DecimalFormat("#.##");
	    double val3 = valLeft(holder, "QB", 3);
	    double val5 = valLeft(holder, "QB", 5);
	    double val10 = valLeft(holder, "QB", 10);
	    qbLeft.setText("QB: " + df.format(val3) + ", " + df.format(val5) + ", " + df.format(val10));
	    val3 = valLeft(holder, "RB", 3);
	    val5 = valLeft(holder, "RB", 5);
	    val10 = valLeft(holder, "RB", 10);
	    rbLeft.setText("RB: " + df.format(val3) + ", " + df.format(val5) + ", " + df.format(val10));
	    val3 = valLeft(holder, "WR", 3);
	    val5 = valLeft(holder, "WR", 5);
	    val10 = valLeft(holder, "WR", 10);
	    wrLeft.setText("WR: " + df.format(val3) + ", " + df.format(val5) + ", " + df.format(val10));
	    val3 = valLeft(holder, "TE", 3);
	    val5 = valLeft(holder, "TE", 5);
	    val10 = valLeft(holder, "TE", 10);
	    teLeft.setText("TE: " + df.format(val3) + ", " + df.format(val5) + ", " + df.format(val10));
	    Button back = (Button)dialog.findViewById(R.id.val_left_back);
	    back.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				moreInfo(new Dialog(context, R.style.RoundCornersFull));
			}
	    });
	}
	
	/**
	 * Calculates the paa left of the top x players at position y
	 */
	public static double valLeft(Storage holder, String pos, int max)
	{
		double total = 0.0;
		int counter = 0;
		for(PlayerObject player : holder.players)
		{
			if(player.info.position.equals(pos) && !Draft.isDrafted(player.info.name, holder.draft) && counter < max)
			{
				counter++;
				total += player.values.paa;
			}
		}
		return total;
	}
	
    /**
     * Sets the dialog to hold the selected players
     * then shows it.
     * @param dialog
     */
    public static void handleInfo(final Dialog dialog)
    {
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.draft_team_status);
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
	    lp.copyFrom(dialog.getWindow().getAttributes());
	    lp.width = WindowManager.LayoutParams.FILL_PARENT;
	    dialog.getWindow().setAttributes(lp);
    	String qbs = handleDraftParsing(holder.draft.qb);
    	String rbs = handleDraftParsing(holder.draft.rb);
    	String wrs = handleDraftParsing(holder.draft.wr);
    	String tes = handleDraftParsing(holder.draft.te);
    	String ds = handleDraftParsing(holder.draft.def);
    	String ks = handleDraftParsing(holder.draft.k);
    	TextView qb = (TextView)dialog.findViewById(R.id.qb_header);
    	TextView rb = (TextView)dialog.findViewById(R.id.rb_header);
    	TextView wr = (TextView)dialog.findViewById(R.id.wr_header);
    	TextView te = (TextView)dialog.findViewById(R.id.te_header);
    	TextView d = (TextView)dialog.findViewById(R.id.d_header);
    	TextView k = (TextView)dialog.findViewById(R.id.k_header);
    	qb.setText("Quarterbacks: " + qbs);
    	rb.setText("Running Backs: " + rbs);
    	wr.setText("Wide Receivers: " + wrs);
    	te.setText("Tight Ends: " + tes);
    	d.setText("D/ST: " + ds);
    	k.setText("Kickers: " + ks);
    	dialog.show();
    	Button back = (Button)dialog.findViewById(R.id.back);
		back.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v) {
				dialog.dismiss();
				moreInfo(new Dialog(context, R.style.RoundCornersFull));
			}
		});
    }
    
    /**
     * Handles parsing of the draft data
     */
    private static String handleDraftParsing(List<PlayerObject> nameList) {
    	String names = "";
    	for(PlayerObject po: nameList)
    	{
    		names += po.info.name + ", ";
    	}
    	if(names.equals(""))
    	{
    		names = "None selected.";
    	}
    	else
    	{
    		names = names.substring(0, names.length()-2);
    	}
    	return names;
	}

	/**
	 * Handles the middle ground before setting the listView
	 * @param holder
	 * @param cont
	 */
	public static void intermediateHandleRankings(Activity cont)
	{ 
		int maxSize = ReadFromFile.readFilterQuantitySize((Context)cont, "Rankings");
		PriorityQueue<PlayerObject>inter = null;
		if(isAuction)
		{
			inter = new PriorityQueue<PlayerObject>(300, new Comparator<PlayerObject>() 
			{
				@Override
				public int compare(PlayerObject a, PlayerObject b) 
				{
					if (a.values.worth > b.values.worth)
				    {
				        return -1;
				    }
				    if (a.values.worth < b.values.worth)
				    {
				    	return 1;
				    }
				    return 0;
				}
			});
		}
		else
		{
			inter = new PriorityQueue<PlayerObject>(300, new Comparator<PlayerObject>() 
			{
						@Override
						public int compare(PlayerObject a, PlayerObject b) 
						{
							if (a.values.ecr > b.values.ecr)
						    {
						        return 1;
						    }
						    if (a.values.ecr < b.values.ecr)
						    {
						    	return -1;
						    }
						    return 0;
						}
			});
		}
		PriorityQueue<PlayerObject> totalList = null;
		if(isAuction)
		{
			totalList = new PriorityQueue<PlayerObject>(300, new Comparator<PlayerObject>() 
			{
				@Override
				public int compare(PlayerObject a, PlayerObject b) 
				{
					if (a.values.worth > b.values.worth)
				    {
				        return -1;
				    }
				    if (a.values.worth < b.values.worth)
				    {
				    	return 1;
				    }
				    return 0;
				}
			});
		}
		else
		{
			totalList = new PriorityQueue<PlayerObject>(300, new Comparator<PlayerObject>() 
			{
				@Override
				public int compare(PlayerObject a, PlayerObject b) 
				{
					if (a.values.ecr > b.values.ecr)
				    {
				        return 1;
				    }
				    if (a.values.ecr < b.values.ecr)
				    {
				    	return -1;
				    }
				    return 0;
				}
			});
		}
		if(posList.size() > 1)
		{
			posFilter = "All Positions";
		}
		else
		{
			posFilter = posList.get(0);
		}
		if(teamList.size() > 1)
		{
			teamFilter = "All Positions";
		}
		else
		{
			teamFilter = teamList.get(0);
		}
		for(int i = 0; i < holder.players.size(); i++)
		{
			PlayerObject player = holder.players.get(i);
			if(posList.contains(player.info.position) && teamList.contains(player.info.team)
					&& !holder.draft.ignore.contains(player.info.name))
			{
				if(isAuction || (!isAuction && player.values.ecr > 0.0))
				{
					inter.add(player);
				}
			}
		}
		int total = inter.size();
		double fraction = (double)maxSize * 0.01;
		double newSize = total * fraction;
		for(int i = 0; i < newSize; i++)
		{
			totalList.add(inter.poll());
		}
		rankingsFetched(totalList, cont);
	}
	
	/**
     * The function that handles what happens when
     * the rankings are all fetched
     * @param holder 
     */
    public static void rankingsFetched(PriorityQueue<PlayerObject> playerList, Activity cont)
    {
	    listview = (ListView) cont.findViewById(R.id.listview_rankings);
	    listview.setAdapter(null);
	    data = new ArrayList<Map<String, String>>();
	    handleRankingsClick(holder, cont, listview);
	    List<String> rankings = new ArrayList<String>(400);
	    while(!playerList.isEmpty())
	    {
	    	PlayerObject elem = playerList.poll();
	        DecimalFormat df = new DecimalFormat("#.##");
	        Map<String, String> datum = new HashMap<String, String>(2);
	        if(isAuction)
	        {
	        	datum.put("main", df.format(elem.values.worth) + ":  " + elem.info.name);
	        }
	        else
	        {
	        	datum.put("main", df.format(elem.values.ecr)+ ":  " + elem.info.name );
	        }
	        //datum.put("sub", "");
	        if(elem.info.team.length() > 2 && elem.info.position.length() > 0)
	        {
	        	datum.put("sub", elem.info.position + " - " + elem.info.team + "\n" + "Bye: " + elem.info.bye);
	        }
	        else
	        {
	        	datum.put("sub", "");
	        }
	        data.add(datum);
	    	rankings.add(df.format(elem.values.worth) + ":   " + elem.info.name);
	    } 
	    if(refreshed)
	    {
	    	WriteToFile.storeRankingsAsync(holder, (Context)cont);
	    	refreshed = false;
	    }
    	WriteToFile.storeListRankings(rankings, cont);
    	adapter = new SimpleAdapter(cont, data, 
	    		android.R.layout.simple_list_item_2, 
	    		new String[] {"main", "sub"}, 
	    		new int[] {android.R.id.text1, 
	    			android.R.id.text2});
	    listview.setAdapter(adapter);
	    //adapter = ManageInput.handleArray(rankings, listview, cont);
	}
    
    /**
     * Handles rankings onclick (dialog)
     */
    public static void handleRankingsClick(final Storage holder, final Activity cont, final ListView listview)
    {
    	 listview.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				listview.setSelection(arg2);
				String selected = ((TwoLineListItem)arg1).getText1().getText().toString();
				selected = selected.split(":  ")[1];
				PlayerInfo.outputResults(selected, true, (Rankings)context, holder, false, true);
			}
    	 });
    	 listview.setOnItemLongClickListener(new OnItemLongClickListener(){

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				String namePlayer = ((TwoLineListItem)arg1).getText1().getText().toString().split(":  ")[1];
				int i = -1;
				for(String name : watchList)
				{
					if(name.equals(namePlayer))
					{
						i = 1;
						break;
					}
				}
				if(i == -1)
				{
					watchList.add(namePlayer);
					WriteToFile.writeWatchList(context, watchList);
					Toast.makeText(context, namePlayer + " added to watch list", Toast.LENGTH_SHORT).show();
				}
				else
				{
					Toast.makeText(context, namePlayer + " already in watch list", Toast.LENGTH_SHORT).show();
				}
				return true;
			}
    	 });
    	 touchListener =
                 new SwipeDismissListViewTouchListener(
                         listview,
                         new SwipeDismissListViewTouchListener.OnDismissCallback() {
                             @Override
                             public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                                 listview.setOnTouchListener(null);
                                 listview.setOnScrollListener(null);
                            	 String name = "";
                                 int index = 0;
                                 Map<String, String> view = null;
                                 for (int position : reverseSortedPositions) {
                                	 name = data.get(position).get("main").split(":  ")[1];
                                	 view = data.get(position);
                                	 data.remove(position);
                                	 index = position;
                                 }
                                 adapter.notifyDataSetChanged();
                                 handleDrafted(view, holder, cont, null, index);
                                 
                             }
                         });
         listview.setOnTouchListener(touchListener);
         listview.setOnScrollListener(touchListener.makeScrollListener());
    }
    
    /**
     * Handles the drafted dialog
     */
    public static void handleDrafted(final Map<String, String> view, final Storage holder, final Activity cont, final Dialog dialog, 
    		final int index)
    { 
    	listview.setOnTouchListener(touchListener);
        listview.setOnScrollListener(touchListener.makeScrollListener());
    	final Dialog popup = new Dialog(cont, R.style.RoundCornersFull);
    	popup.setCancelable(false);
		popup.requestWindowFeature(Window.FEATURE_NO_TITLE);
    	popup.setContentView(R.layout.draft_by_who);
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
	    lp.copyFrom(popup.getWindow().getAttributes());
	    lp.width = WindowManager.LayoutParams.FILL_PARENT;
	    popup.getWindow().setAttributes(lp);
    	TextView header = (TextView)popup.findViewById(R.id.name_header);
    	String name = view.get("main");
    	final String adapt = name;
    	if(view.get("main").contains(":"))
    	{
    		name = view.get("main").split(":  ")[1];
    		header.setText("Who drafted " + name + "?");
    	}
    	else
    	{
    		name = view.get("main");
    		header.setText("Who drafted " + name + "?");
    	}
    	if(Draft.isDrafted(name, holder.draft))
    	{
    		Toast.makeText(cont, name + " is already drafted", Toast.LENGTH_SHORT).show();
    		return;
    	}
    	final String d = name;
    	popup.show();
    	Button close = (Button)popup.findViewById(R.id.draft_who_close);
    	close.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				data.add(index, view);
				adapter.notifyDataSetChanged();
				popup.dismiss();
				return;
			}
    	});
    	Button someone = (Button)popup.findViewById(R.id.drafted_by_someone);
    	someone.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
		    	holder.draft.ignore.add(d);
				WriteToFile.writeDraft(holder.draft, cont);
				popup.dismiss();
				if(dialog != null)
				{
					dialog.dismiss();
				}
				Toast.makeText(cont, "Removing " + d + " from the list", Toast.LENGTH_SHORT).show();
			}
    	});
    	
    	Button me = (Button)popup.findViewById(R.id.drafted_by_me);
    	me.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if(isAuction)
				{
					draftedByMe(d, view, holder, cont, listview, popup, dialog, index);
				}
				else
				{
					for(PlayerObject player : holder.players)
					{
						if(player.info.name.equals(d))
						{
							holder.draft.draftPlayer(player, holder.draft, 1, cont);
							Toast.makeText(cont, "Drafting " + d, Toast.LENGTH_SHORT).show();
							holder.draft.ignore.add(d);
							WriteToFile.writeDraft(holder.draft, cont);
							
							popup.dismiss();
						}
					}
				}
			}
    	});
    }
    
    /**
     * Handles the 'drafted by me' dialog
     */
    public static void draftedByMe(final String name, final Map<String, String> view, final Storage holder, final Activity cont,
    		final ListView listview, final Dialog popup, final Dialog dialog, final int index)
    {
    	popup.setContentView(R.layout.draft_by_me);
    	TextView header = (TextView)popup.findViewById(R.id.name_header);
    	header.setText("How much did " + name + " cost?");
    	Button back = (Button)popup.findViewById(R.id.draft_who_close);
    	back.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				popup.dismiss();
				handleDrafted(view, holder, cont, dialog, index);
			}
    	});
    	List<String> possResults = new ArrayList<String>();
    	for(int i = 1; i < 201; i++)
    	{
    		possResults.add(String.valueOf(i));
    	}
    	AutoCompleteTextView price = (AutoCompleteTextView)popup.findViewById(R.id.amount_paid);
    	ArrayAdapter<String> doubleAdapter = new ArrayAdapter<String>(cont,
                android.R.layout.simple_dropdown_item_1line, possResults);
    	price.setAdapter(doubleAdapter);
    	price.setThreshold(1);
    	price.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				int val = Integer.parseInt(((TextView)arg1).getText().toString());
				for(PlayerObject player : holder.players)
				{
					if(player.info.name.equals(name))
					{
						if(dialog != null)
						{
							dialog.dismiss();
						}
						if(val <= holder.draft.remainingSalary)
						{
							holder.draft.draftPlayer(player, holder.draft, val, cont);
							Toast.makeText(cont, "Drafting " + name, Toast.LENGTH_SHORT).show();
							holder.draft.ignore.add(name);
							WriteToFile.writeDraft(holder.draft, cont);
						}
						else
						{
							Toast.makeText(cont, "Not enough salary left", Toast.LENGTH_SHORT).show();
						}
						break;
					}
				}
				popup.dismiss();
			}
    	});
    }
}
