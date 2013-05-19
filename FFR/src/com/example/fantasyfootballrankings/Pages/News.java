package com.example.fantasyfootballrankings.Pages;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import com.example.fantasyfootballrankings.R;
import com.example.fantasyfootballrankings.R.layout;
import com.example.fantasyfootballrankings.R.menu;
import com.example.fantasyfootballrankings.ClassFiles.ManageInput;
import com.example.fantasyfootballrankings.ClassFiles.NewsObjects;
import com.example.fantasyfootballrankings.ClassFiles.ParseFiles.ParseNews;

import FileIO.WriteToFile;
import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.BufferType;

public class News extends Activity {
	public static Context cont;
	public Dialog dialog;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_news);
		cont = this;
		handleInitialLoading();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.news, menu);
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
			case R.id.refresh_news:
				refreshNewsDialog();
				return true;
			case R.id.twitter_feeds:
				twitterFeedsDialog();
				return true;
			//New page opens up entirely for going home
			case R.id.go_home:
				Intent home_intent = new Intent(cont, Home.class);
				cont.startActivity(home_intent);		
		        return true;
			case R.id.view_rankings:
		        Intent intent_ranking = new Intent(cont, Rankings.class);
		        cont.startActivity(intent_ranking);		
 		        return true;
		    //New page opens up entirely for viewing the team page
			case R.id.view_team:
		        Intent intent = new Intent(cont, Team.class);
		        cont.startActivity(intent);		
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
	 * Handles conditional loading of the news
	 */
	public static void handleInitialLoading()
	{
		SharedPreferences prefs = cont.getSharedPreferences("FFR", 0); 
		String newsWhole = prefs.getString("News RotoWorld", "Not Set");
		if(newsWhole.equals("Not Set"))
		{
			ParseNews.startNewsAsync(cont, true, false, false, false, false);
		}
		else
		{
			ParseNews.startNewsReading(cont);
		}
	}

	/**
	 * Handles conditional refreshing of news
	 */
	public static void refreshNewsDialog()
	{
		final Dialog dialog = new Dialog(cont);
		dialog.setContentView(R.layout.news_topics_dialog);
		SharedPreferences prefs = cont.getSharedPreferences("FFR", 0); 
		boolean rhCheck = prefs.getBoolean("Use Headlines", false);
		boolean rpCheck = prefs.getBoolean("Use Player News", false);
		boolean thCheck = prefs.getBoolean("Use The Huddle", false);
		boolean cCheck = prefs.getBoolean("Use CBS News", false);
		boolean siCheck = prefs.getBoolean("Use SI News", false);
		if(!rpCheck && !rhCheck && !thCheck)
		{
			rhCheck = true;
		}
    	final RadioButton rh = (RadioButton)dialog.findViewById(R.id.rotoworld_headlines);
    	final RadioButton rp = (RadioButton)dialog.findViewById(R.id.rotoworld_player_news);
    	final RadioButton th = (RadioButton)dialog.findViewById(R.id.the_huddle_news);
    	final RadioButton cbs = (RadioButton)dialog.findViewById(R.id.cbs_news);
    	final RadioButton si = (RadioButton)dialog.findViewById(R.id.si_news);
    	rh.setChecked(rhCheck);
    	rp.setChecked(rpCheck);
    	th.setChecked(thCheck);
    	cbs.setChecked(cCheck);
    	si.setChecked(siCheck);
		Button submit = (Button)dialog.findViewById(R.id.button_news_confirm);
		submit.setOnClickListener(new View.OnClickListener()
		{	
            @Override
            public void onClick(View v) 
            {
            	ParseNews.startNewsAsync(cont, rh.isChecked(), rp.isChecked(), th.isChecked(),
            			cbs.isChecked(), si.isChecked());
            	dialog.dismiss();
            }
		});
		Button cancel = (Button)dialog.findViewById(R.id.button_news_cancel);
		cancel.setOnClickListener(new View.OnClickListener()
		{	
            @Override
            public void onClick(View v) 
            {
            	dialog.dismiss();
            }
		});
		dialog.show();
	}
	
	/**
	 * Handles conditional refreshing of news
	 */
	public static void twitterFeedsDialog()
	{
		final Dialog dialog = new Dialog(cont);
		dialog.setContentView(R.layout.twitter_feeds);
		List<String> spinnerList = new ArrayList<String>();
		spinnerList.add("Adam Schefter (NFL News)");
		spinnerList.add("Chris Mortenson (NFL News)");
		spinnerList.add("Jason LaCanfora (NFL News)");
		spinnerList.add("Jay Glazer (NFL News)");
		spinnerList.add("Brad Evans (Fantasy)");
		spinnerList.add("Mike Clay (Fantasy)");
		spinnerList.add("Eric Mack (Fantasy)");
		spinnerList.add("Fantasy Douche (Fantasy)");
		spinnerList.add("Late Round QB (Fantasy)");
		spinnerList.add("Chris Wesseling (Fantasy)");
		Spinner feeds = (Spinner)dialog.findViewById(R.id.spinner_feeds);
		ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(cont, 
				android.R.layout.simple_spinner_dropdown_item, spinnerList);
		feeds.setAdapter(spinnerArrayAdapter);
		SharedPreferences prefs = cont.getSharedPreferences("FFR", 0);
		//Still need to determine what the last loaded was and set spinner to that
    	
		Button submit = (Button)dialog.findViewById(R.id.twitter_submit);
		submit.setOnClickListener(new View.OnClickListener()
		{	
            @Override
            public void onClick(View v) 
            {
            	//Call async function here
            	dialog.dismiss();
            }
		});
		Button cancel = (Button)dialog.findViewById(R.id.twitter_cancel);
		cancel.setOnClickListener(new View.OnClickListener()
		{	
            @Override
            public void onClick(View v) 
            {
            	dialog.dismiss();
            }
		});
		dialog.show();
	}
	/**
	 * Handles the showing of the listview of news
	 * @param result
	 * @param cont
	 */
	public static void handleNewsListView(List<NewsObjects> result, Activity cont) 
	{
		ListView listview = (ListView)cont.findViewById(R.id.listview_news);
	    List<String> news = new ArrayList<String>(10000);
	    for(NewsObjects newsObj : result)
	    {
	    	StringBuilder newsBuilder = new StringBuilder(1000);
	    	newsBuilder.append(newsObj.news + "\n\n" + newsObj.impact + "\n\n"
	    			 + "Date: " + newsObj.date + "\n");
	    	news.add(newsBuilder.toString());
	    }
	    ManageInput.handleArray(news, listview, cont);
	}

}
