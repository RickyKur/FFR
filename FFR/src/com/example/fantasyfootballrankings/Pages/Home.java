package com.example.fantasyfootballrankings.Pages;



import java.io.IOException;
import java.io.Serializable;



import java.net.MalformedURLException;
import java.util.Iterator;

import org.htmlcleaner.XPatherException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.example.fantasyfootballrankings.R;
import com.example.fantasyfootballrankings.R.id;
import com.example.fantasyfootballrankings.R.layout;
import com.example.fantasyfootballrankings.R.menu;
import com.example.fantasyfootballrankings.ClassFiles.HandleBasicQueries;
import com.example.fantasyfootballrankings.ClassFiles.HighLevel;
import com.example.fantasyfootballrankings.ClassFiles.ParseRankings;
import com.example.fantasyfootballrankings.ClassFiles.PlayerObject;
import com.example.fantasyfootballrankings.ClassFiles.Storage;
import com.example.fantasyfootballrankings.ClassFiles.ParseFiles.Parse4for4;
import com.example.fantasyfootballrankings.ClassFiles.ParseFiles.ParseBrokenTackles;
import com.example.fantasyfootballrankings.ClassFiles.ParseFiles.ParseCBS;
import com.example.fantasyfootballrankings.ClassFiles.ParseFiles.ParseDraft;
import com.example.fantasyfootballrankings.ClassFiles.ParseFiles.ParseESPNadv;
import com.example.fantasyfootballrankings.ClassFiles.ParseFiles.ParseGE;
import com.example.fantasyfootballrankings.ClassFiles.ParseFiles.ParseInjuries;
import com.example.fantasyfootballrankings.ClassFiles.ParseFiles.ParseMyFantasyLeague;
import com.example.fantasyfootballrankings.ClassFiles.ParseFiles.ParseNews;
import com.example.fantasyfootballrankings.ClassFiles.ParseFiles.ParsePermanentData;
import com.example.fantasyfootballrankings.ClassFiles.ParseFiles.ParsePlayerNames;
import com.example.fantasyfootballrankings.ClassFiles.ParseFiles.ParseStats;
import com.example.fantasyfootballrankings.ClassFiles.ParseFiles.ParseWF;

import AsyncTasks.ParsingAsyncTask;
import AsyncTasks.ParsingAsyncTask.ParseNames;
import AsyncTasks.ParsingAsyncTask.ParsePermanentDataSets;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.SyncStateContract.Constants;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
 
/**
 * The home class, sets up the three main buttons to go to 
 * trending players, team view, and/or rankings themselves
 * @author Jeff
 *
 */ 
public class Home extends Activity implements Serializable{
	//Some global variables, context and a few buttons
	final Context cont = this;
	Dialog dialog;
	Button rankings;
	Button trending;
	Button news;
	
	/** 
	 * Makes the buttons and sets the listeners for them
	 */
	@Override  
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
        rankings = (Button)findViewById(R.id.rankings);
        rankings.setOnClickListener(rankHandler);
        trending = (Button)findViewById(R.id.trending);
        trending.setOnClickListener(trendHandler);
        news = (Button)findViewById(R.id.news_button);
        news.setOnClickListener(newsHandler);
        handleInitialRefresh();
        /*
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy); 
        try {
			ParseESPNadv.parseESPNAggregate(new Storage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XPatherException e) {
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
		dialog = new Dialog(cont);
		switch (item.getItemId()) 
		{
			//Refresh the names list
			case R.id.refresh_names:
				nameRefresh(dialog);
		    	return true;			
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	/**
	 * Handles the initial fetching of player names
	 * and permanent data if they haven't been
	 * fetched (initial opening of the app)
	 */
	public void handleInitialRefresh()
	{
		SharedPreferences prefs = cont.getSharedPreferences("FFR", 0); 
		String checkExists = prefs.getString("O Line Ranks", "Not Set");
		if(checkExists.equals("Not Set"))
		{
			final ParsingAsyncTask stupid = new ParsingAsyncTask();
			ParseNames task = stupid.new ParseNames((Activity)cont);
		    task.execute(cont);	
		    
		    ParsePermanentDataSets advancedData = stupid.new ParsePermanentDataSets((Activity) cont);
		    advancedData.execute(cont, new Storage());
		}
	}
	
	/**
	 * Handles the name refreshing given the prompt
	 * @param dialog
	 */
	public void nameRefresh(final Dialog dialog)
	{
		dialog.setContentView(R.layout.refresh_list);
		dialog.show();
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
}
