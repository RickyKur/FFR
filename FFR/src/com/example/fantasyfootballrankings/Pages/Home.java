package com.example.fantasyfootballrankings.Pages;



import java.io.IOException;











import java.util.Random;
import java.util.concurrent.ExecutionException;

import org.htmlcleaner.XPatherException;

import jeff.isawesome.fantasyfootballrankings.R;

import com.example.fantasyfootballrankings.ClassFiles.HandleExport;
import com.example.fantasyfootballrankings.ClassFiles.HighLevel;
import com.example.fantasyfootballrankings.ClassFiles.ManageInput;
import com.example.fantasyfootballrankings.ClassFiles.Storage;
import com.example.fantasyfootballrankings.ClassFiles.LittleStorage.Scoring;
import com.example.fantasyfootballrankings.ClassFiles.ParseFiles.ParseCBS;
import com.example.fantasyfootballrankings.ClassFiles.ParseFiles.ParseJuanElway;
import com.example.fantasyfootballrankings.ClassFiles.ParseFiles.ParseFFTB;
import com.example.fantasyfootballrankings.ClassFiles.ParseFiles.ParseNFL;
import com.example.fantasyfootballrankings.ClassFiles.ParseFiles.ParseSI;
import com.example.fantasyfootballrankings.ClassFiles.ParseFiles.ParseTheFakeFootball;

import AsyncTasks.ParsingAsyncTask;
import AsyncTasks.ParsingAsyncTask.ParseNames;
import FileIO.ReadFromFile;
import FileIO.WriteToFile;
import android.os.Bundle;
import android.os.StrictMode;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
 
/**
 * The home class, sets up the three main buttons to go to 
 * trending players, team view, and/or rankings themselves
 * @author Jeff
 *
 */ 
public class Home extends Activity{
	//Some global variables, context and a few buttons
	Storage holder = new Storage();
	final Context cont = this;
	Dialog dialog;
	Button rankings;
	Button trending;
	Button news;
	Button drafts;
	
	long start; 
	
	  
	/**  
	 * Makes the buttons and sets the listeners for them
	 */   
	@Override  
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		ActionBar ab = getActionBar();
		ab.setDisplayShowHomeEnabled(false);
		ab.setDisplayShowTitleEnabled(false);
        rankings = (Button)findViewById(R.id.rankings);
        rankings.setOnClickListener(rankHandler);
        trending = (Button)findViewById(R.id.trending);
        trending.setOnClickListener(trendHandler);
        news = (Button)findViewById(R.id.news_button); 
        news.setOnClickListener(newsHandler);
        drafts = (Button)findViewById(R.id.draft_history);
        drafts.setOnClickListener(draftHandler);
        WindowManager wm = (WindowManager) cont.getSystemService(Context.WINDOW_SERVICE);
		 Display display = wm.getDefaultDisplay();
		 Resources r = cont.getResources();
		 int width = display.getWidth(); 
		 int height = display.getHeight();
		 int newWidth = 0;
		 newWidth = (width*2)/13;
		 float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, newWidth, r.getDisplayMetrics());
		 		 
		ImageView pic = (ImageView)findViewById(R.id.football_icon_home);
		android.view.ViewGroup.LayoutParams picParams = pic.getLayoutParams();
		picParams.width = (width * 2)/5;
		picParams.height = (height * 2) / 5;
		pic.setLayoutParams(picParams);
		final Random message = new Random();
		pic.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				int random = message.nextInt();
				if(random%2 == 0)
				{
					Toast.makeText(cont, "Thanks for downloading the app!", Toast.LENGTH_SHORT).show();
				}
				else if(random%3 == 0)
				{
					Toast.makeText(cont, "Stop clicking the image.", Toast.LENGTH_SHORT).show();
				}
				else
				{
					Toast.makeText(cont, "Clicking this image does nothing.", Toast.LENGTH_SHORT).show();
				}
			}
		});
		 android.view.ViewGroup.LayoutParams params1 = rankings.getLayoutParams();
		 params1.width = (int) px;
		 rankings.setLayoutParams(params1);
		 android.view.ViewGroup.LayoutParams params2 = trending.getLayoutParams();
		 params2.width = (int) px;
		 trending.setLayoutParams(params2);
		 android.view.ViewGroup.LayoutParams params3 = news.getLayoutParams();
		 params3.width = (int) px;
		 news.setLayoutParams(params3);
		 android.view.ViewGroup.LayoutParams params4 = drafts.getLayoutParams();
		 params4.width = (int) px;
		 drafts.setLayoutParams(params4);
        start = System.nanoTime();
        handleInitialRefresh();
        if(ReadFromFile.readFirstOpen(cont))
        {
        	helpPopUp();
        	WriteToFile.writeFirstOpen(cont);
        }
        /*StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy); 
        try { 
        	ParseRazzball.parseRazzballWrapper(new Storage());
		} catch (IOException e) { 
			// TODO Auto-generated catch block
			e.printStackTrace(); 
		}*/
	}  
	 
	/**
	 * Sets up the menu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.home, menu);
		return true;
	}
	
	/**
	 * Runs the on selection part of the menu
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{  
		dialog = new Dialog(cont, R.style.RoundCornersFull);
		boolean isStored = false;
		if(holder.players.size() > 0)
		{
			isStored = true;
		}
		switch (item.getItemId()) 
		{
			case R.id.export_names:
				if(isStored)
				{ 
					callExport();
				}
				else
				{
					Toast.makeText(cont, "Can't export rankings until they are fetched", Toast.LENGTH_SHORT).show(); 
				}
				return true;
			case R.id.refresh_names:
				nameRefresh(dialog);
		    	return true;			
			case R.id.credit_home:
				helpDialog(cont);
				return true;
			case R.id.start_scoring:
				ManageInput.passSettings(cont, new Scoring(), isStored, holder);
		    	return true;	
			case R.id.start_roster:
				ManageInput.getRoster(cont, isStored, holder);
				return true;
			case R.id.auction_or_snake:
				ManageInput.isAuctionOrSnake(cont, holder);
				return true;
			case R.id.help_home:
				helpPopUp();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	private void helpDialog(Context cont2) {
		final Dialog dialog = new Dialog(cont, R.style.RoundCornersFull);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.home_credit);
		dialog.show();
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
	    lp.copyFrom(dialog.getWindow().getAttributes());
	    lp.width = WindowManager.LayoutParams.FILL_PARENT;
	    dialog.getWindow().setAttributes(lp);
		Button close = (Button)dialog.findViewById(R.id.credit_close);
		close.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
	}

	/**
	 * Handles the help dialog popup
	 */
	public void helpPopUp()
	{
		final Dialog dialog = new Dialog(cont, R.style.RoundCornersFull);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.help_home);
		dialog.show();
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
	    lp.copyFrom(dialog.getWindow().getAttributes());
	    lp.width = WindowManager.LayoutParams.FILL_PARENT;
	    dialog.getWindow().setAttributes(lp);
		Button close = (Button)dialog.findViewById(R.id.help_home_close);
		close.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
	}
	
	/**
	 * Calls the handle export fn
	 */
	public void callExport()
	{
		HandleExport.driveInit(HandleExport.orderPlayers(holder, cont), dialog, cont, holder);
	}
	
	/**
	 * Handles the initial fetching of player names
	 * and permanent data if they haven't been
	 * fetched (initial opening of the app)
	 */
	public void handleInitialRefresh()
	{
		SharedPreferences prefs = cont.getSharedPreferences("FFR", 0); 
		String checkExists = prefs.getString("Player Values", "Not Set");
		if(!checkExists.equals("Not Set"))
		{
			ReadFromFile.fetchPlayers(checkExists, holder,cont, 1);
		}
		if(holder.playerNames == null || holder.playerNames.size() < 10)
		{
			String checkExists2 = prefs.getString("Player Names", "Not Set");
			if(checkExists2.equals("Not Set"))
			{
				final ParsingAsyncTask stupid = new ParsingAsyncTask();
				ParseNames task = stupid.new ParseNames((Activity)cont);
			    task.execute(cont);
			}
		}
	}
	
	/**
	 * Handles the name refreshing given the prompt
	 * @param dialog
	 */
	public void nameRefresh(final Dialog dialog)
	{
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.refresh_list);
		dialog.show();
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
	    lp.copyFrom(dialog.getWindow().getAttributes());
	    lp.width = WindowManager.LayoutParams.FILL_PARENT;
	    dialog.getWindow().setAttributes(lp);
		Button cancel = (Button)dialog.findViewById(R.id.cancel_list_refresh);
		Button submit = (Button)dialog.findViewById(R.id.confirm_list_refresh);
		final ParsingAsyncTask stupid = new ParsingAsyncTask();
		cancel.setOnClickListener(new OnClickListener() 
		{
			public void onClick(View v) {
				dialog.dismiss();
	    	}	
		});
		submit.setOnClickListener(new OnClickListener() 
		{
			public void onClick(View v) {
				dialog.dismiss();
				ParseNames task = stupid.new ParseNames((Activity)cont);
			    task.execute(cont);	
	    	}	
		});	
	}
	

	
	/**
	 * Sends the rank button to the ranking page
	 */
	View.OnClickListener rankHandler = new View.OnClickListener() 
	{
		public void onClick(View v) 
		{
	        Intent intent = new Intent(cont, Rankings.class);
	        cont.startActivity(intent);		
		}
	};	
	
	/**
	 * Sends the trending button to the ranking page
	 */
	View.OnClickListener trendHandler = new View.OnClickListener() 
	{
		public void onClick(View v) 
		{
	        Intent intent = new Intent(cont, Trending.class);
	        cont.startActivity(intent);		
		}	
	};
	
	/**
	 * Sends the news button to the news page
	 */
	View.OnClickListener newsHandler = new View.OnClickListener() 
	{
		public void onClick(View v) 
		{
	        Intent intent = new Intent(cont, News.class);
	        cont.startActivity(intent);		
		}	
	};
	
	View.OnClickListener draftHandler = new View.OnClickListener() 
	{
		public void onClick(View v) 
		{
	        Intent intent = new Intent(cont, DraftHistory.class);
	        cont.startActivity(intent);		
		}	
	};
}
