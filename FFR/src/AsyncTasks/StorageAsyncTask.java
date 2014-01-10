package AsyncTasks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import FileIO.ReadFromFile;
import FileIO.WriteToFile;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;

import com.example.fantasyfootballrankings.ClassFiles.HighLevel;
import com.example.fantasyfootballrankings.ClassFiles.ManageInput;
import com.example.fantasyfootballrankings.ClassFiles.LittleStorage.NewsObjects;
import com.example.fantasyfootballrankings.ClassFiles.LittleStorage.Post;
import com.example.fantasyfootballrankings.ClassFiles.StorageClasses.PlayerObject;
import com.example.fantasyfootballrankings.ClassFiles.StorageClasses.Storage;
import com.example.fantasyfootballrankings.Pages.Home;
import com.example.fantasyfootballrankings.Pages.News;
import com.example.fantasyfootballrankings.Pages.Rankings;
import com.example.fantasyfootballrankings.Pages.Trending;
/**
 * A library to hold all the asynctasks relevant to storing/reading to/from file
 * @author Jeff
 *
 */
public class StorageAsyncTask
{
	
	/**
	 * This handles the running of the rankings in the background
	 * such that the user can't do anything until they're fetched
	 * @author Jeff
	 *
	 */
	public class WriteDraft extends AsyncTask<Object, Void, Void> 
	{
		ProgressDialog pdia;
	    public WriteDraft(Activity activity) 
	    {
	    	pdia = new ProgressDialog(activity);
	        pdia.setCancelable(false);
	    }

	    @Override
		protected void onPreExecute(){ 
		   super.onPreExecute();
		        pdia.setMessage("Please wait, saving the rankings...");
		        pdia.show();    
		}
	    
		@Override
		protected void onPostExecute(Void result){
			pdia.dismiss();
		   super.onPostExecute(result);
		}

	    @Override
	    protected Void doInBackground(Object... data) 
	    {
	    	Storage holder = (Storage)data[0];
	    	Context cont = (Context)data[1];
	    	WriteToFile.writeTeamData(holder, cont);
	    	SharedPreferences.Editor editor = cont.getSharedPreferences("FFR", 0).edit();
	    	//Rankings work
	    	Set<String> playerData = new HashSet<String>();
	    	for (PlayerObject player : holder.players)
	    	{
		    	StringBuilder players = new StringBuilder(10000);
	    		players.append(Double.toString(player.values.worth));
	    		players.append("&&");
	    		players.append(Double.toString(player.values.count));
	    		players.append("&&");
	    		players.append(player.info.name);
	    		players.append("&&");
	    		players.append(player.info.team);
	    		players.append("&&");
	    		players.append(player.info.position);
	    		players.append("&&");
	    		players.append(player.info.adp);
	    		players.append("&&");
	    		players.append(player.info.contractStatus);
	    		players.append("&&");
	    		players.append(player.info.age);
	    		players.append("&&");
	    		players.append(player.stats);
	    		players.append("&&");
	    		players.append(player.injuryStatus);
	    		players.append("&&");
	    		players.append(player.values.ecr);
	    		players.append("&&");
	    		players.append(player.risk);
	    		players.append("&&");
	    		players.append(player.values.points);
	    		players.append("&&");
	    		players.append(player.values.paa);
	    		players.append("&&");
	    		players.append(player.values.rosRank);
	    		players.append("&&");
	    		players.append(player.note);
	    		players.append("&&");
	    		players.append(player.values.startDists.get("Bad") + "," + player.values.startDists.get("Good") + "," + player.values.startDists.get("Great"));
	    		playerData.add(players.toString());
	    	}
	    	editor.putStringSet("Parsed Player Names", new HashSet<String>(holder.parsedPlayers));
	    	editor.putStringSet("Player Values", playerData).commit();
	    	editor.commit();
			return null;
	    }
	}

	/**
	 * Writes the posts to file 
	 * @author Jeff
	 *
	 */
	public class WritePostsListAsync extends AsyncTask<Object, Void, Void> 
	{
		ProgressDialog pdia;
	    public WritePostsListAsync(Activity activity) 
	    {
	    	pdia = new ProgressDialog(activity);
	        pdia.setCancelable(false);
	    }

	    @Override
		protected void onPreExecute(){ 
		   super.onPreExecute();
		        pdia.setMessage("Please wait, saving the trending players...");
		        pdia.show();    
		}
	    
		@Override
		protected void onPostExecute(Void result){
			pdia.dismiss();
		   super.onPostExecute(result);
		}

	    @Override
	    protected Void doInBackground(Object... data) 
	    {
	    	List<String> trendingPlayers = (List<String>)data[0];
	    	Context cont = (Context)data[1];
	    	SharedPreferences.Editor editor = cont.getSharedPreferences("FFR", 0).edit();
	    	StringBuilder posts = new StringBuilder(10000);
	    	for(int i = 0; i < trendingPlayers.size(); i++)
	    	{
	    		posts.append(trendingPlayers.get(i) + "##");
	    	}
	    	editor.putString("Posted Players", posts.toString());
	    	editor.commit();
			return null;
	    }
	}

	/**
	 * Writes new PAA to file after calculating it
	 * @author Jeff
	 *
	 */
	public class WriteNewPAA extends AsyncTask<Object, Void, Void> 
	{
		Context cont;
		ProgressDialog pdia;
		Boolean flag;
	    public WriteNewPAA(Context c, Boolean sw) 
	    {
	    	pdia = new ProgressDialog(c);
	    	pdia.setCancelable(false);
	        cont = c;
	        flag = sw;
	    }

		@Override
		protected void onPreExecute(){ 
			if(flag)
			{
				pdia.setMessage("Please wait, saving the updated rankings...");
			}
			else
			{
				pdia.setMessage("Please wait, saving the note...");
			}
	        pdia.show();
		   super.onPreExecute();   
		}

		@Override
		protected void onPostExecute(Void result){
			pdia.dismiss();
			((Activity) cont).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
		   super.onPostExecute(result);
		}

	    @Override
	    protected Void doInBackground(Object... data) 
	    {
	    	Context cont = (Context)data[1];
	    	Storage holder = (Storage)data[0];
	    	HighLevel.getPAA(holder, cont);
	    	WriteToFile.writeTeamData(holder, cont);
	    	SharedPreferences.Editor editor = cont.getSharedPreferences("FFR", 0).edit();
	    	//Rankings work
	    	Set<String> playerData = new HashSet<String>();
	    	for (PlayerObject player : holder.players)
	    	{
		    	StringBuilder players = new StringBuilder(10000);
	    		players.append(Double.toString(player.values.worth));
	    		players.append("&&");
	    		players.append(Double.toString(player.values.count));
	    		players.append("&&");
	    		players.append(player.info.name);
	    		players.append("&&");
	    		players.append(player.info.team);
	    		players.append("&&");
	    		players.append(player.info.position);
	    		players.append("&&");
	    		players.append(player.info.adp);
	    		players.append("&&");
	    		players.append(player.info.contractStatus);
	    		players.append("&&");
	    		players.append(player.info.age);
	    		players.append("&&");
	    		players.append(player.stats);
	    		players.append("&&");
	    		players.append(player.injuryStatus);
	    		players.append("&&");
	    		players.append(player.values.ecr);
	    		players.append("&&");
	    		players.append(player.risk);
	    		players.append("&&");
	    		players.append(player.values.points);
	    		players.append("&&");
	    		players.append(player.values.paa);
	    		players.append("&&");
	    		players.append(player.values.rosRank);
	    		players.append("&&");
	    		players.append(player.note);
	    		players.append("&&");
	    		players.append(player.values.startDists.get("Bad") + "," + player.values.startDists.get("Good") + "," + player.values.startDists.get("Great"));
	    		playerData.add(players.toString());
	    	}
	    	editor.putStringSet("Parsed Player Names", new HashSet<String>(holder.parsedPlayers));
	    	editor.putStringSet("Player Values", playerData).commit();
			return null;
	    }
	}

	  /**
     * Fetches the names list from file in the background
     * @author Jeff
     *
     */
	public class ReadNamesList extends AsyncTask<Object, Void, Void> 
	{
	    public ReadNamesList() 
	    {
	    }

		@Override
		protected void onPreExecute(){ 
		   super.onPreExecute();
  
		}

		@Override
		protected void onPostExecute(Void result){

		}

	    protected Void doInBackground(Object... data) 
	    {
	    	Storage holder = (Storage) data[0];
	    	Context cont = (Context) data[1];
	    	try {
				ReadFromFile.fetchNames(holder, cont);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
    		return null;
	    }
	  }



	/**
	 * This handles the reading of the draft data
	 * in the background
	 * @author Jeff
	 *
	 */
	public class ReadDraft extends AsyncTask<Object, Void, Storage> 
	{
		Activity act;
		int flag;
	    public ReadDraft(Activity activity, int i) 
	    {
	        act = activity;
	        flag = i;
	    }

		@Override
		protected void onPreExecute(){ 
		   super.onPreExecute();
		}
		@Override
		protected void onPostExecute(Storage result){
			if(result.maxProj() < 65.0)
			{
				result.isRegularSeason = true;
			}
			if(flag == 0)
			{
				((Rankings)act).intermediateHandleRankings(act);
			}
			else if(flag == 1)
			{
				((Home)act).seeIfInvalid();
			}
			else if(flag == 2)
			{
				Trending.setNoInfo(act, result);
			}
		}
	    protected Storage doInBackground(Object... data) 
	    {
	    	Storage holder = (Storage) data[0];
	    	Context cont = (Context) data[1];
	    	long start = (Long)data[2];
	    	Set<String> checkExists = (Set<String>)data[3];
	   		SharedPreferences prefs = cont.getSharedPreferences("FFR", 0); 
    		holder.players = new ArrayList<PlayerObject>();
    		holder.parsedPlayers = new ArrayList<String>();
    		ReadFromFile.readTeamData(holder, cont);
    		double aucFactor = ReadFromFile.readAucFactor(cont);
	   		for(String st : checkExists)
	   		{  
	   			String[] allData = ManageInput.tokenize(st, '&', 2);
	   			PlayerObject newPlayer = new PlayerObject(allData[2], allData[3], allData[4], 0);
	   			String[] distSet = allData[16].split(",");
	   			newPlayer.values.startDists.put("Bad", Integer.parseInt(distSet[0]));
	   			newPlayer.values.startDists.put("Good", Integer.parseInt(distSet[1]));
	   			newPlayer.values.startDists.put("Great", Integer.parseInt(distSet[2]));
	   			newPlayer.note = allData[15];
	   			newPlayer.values.rosRank = Integer.parseInt(allData[14]);
	   			newPlayer.values.paa = Double.parseDouble(allData[13]);
	   			newPlayer.values.points = Double.parseDouble(allData[12]);
	   			newPlayer.risk = Double.parseDouble(allData[11]);
	   			newPlayer.values.ecr = Double.parseDouble(allData[10]);
	   			newPlayer.injuryStatus = allData[9];
	   			newPlayer.stats = allData[8];
	   			newPlayer.info.age = allData[7];
	   			newPlayer.info.contractStatus = allData[6];
	   			newPlayer.info.adp = allData[5];
	   			newPlayer.values.count = Double.parseDouble(allData[1]);
	   			newPlayer.values.worth = Double.parseDouble(allData[0]);
	   			newPlayer.values.secWorth = newPlayer.values.worth / aucFactor;
	   			holder.players.add(newPlayer);
	   		}
	   		holder.parsedPlayers = new ArrayList<String>(prefs.getStringSet("Parsed Player Names", null));
	   		String set = prefs.getString("Draft Information", "Doesn't matter");
			String[] perSet = ManageInput.tokenize(set, '@', 1);
			String[][] individual = new String[perSet.length][];
			for(int j = 0; j < perSet.length; j++)
			{
				individual[j] = perSet[j].split("~");
			}
			if(!perSet[0].equals("Doesn't matter") && individual.length > 4)
			{
				//Qb fetching
				ReadFromFile.handleDraftReading(individual[0], holder.draft.qb, holder);
				//Rb fetching
				ReadFromFile.handleDraftReading(individual[1], holder.draft.rb, holder);
				//Wr fetching
				ReadFromFile.handleDraftReading(individual[2], holder.draft.wr, holder);
				//Te fetching
				ReadFromFile.handleDraftReading(individual[3], holder.draft.te, holder);
				//Def fetching
				ReadFromFile.handleDraftReading(individual[4], holder.draft.def, holder);
				//K fetching
				ReadFromFile.handleDraftReading(individual[5], holder.draft.k, holder);
				//Ignore fetching
				holder.draft.ignore.clear();
				for(String name : individual[6])
				{
					if(name.length() > 3 && !holder.draft.ignore.contains(name))
					{
						holder.draft.ignore.add(name);
					}
				}
				//Values 
				holder.draft.remainingSalary = Integer.parseInt(individual[7][0]);
				holder.draft.value = Double.parseDouble(individual[8][0]);
			}
			System.out.println(System.nanoTime() - start + " to read from file");
			return holder;
	    }
	  }
	

	/**
	 * In the back-end fetches the posts
	 * @author Jeff
	 *
	 */
	public class ReadPosts extends AsyncTask<Object, Void, Storage> 
	{
		Context cont;
	    public ReadPosts() 
	    {
	    }

		@Override
		protected void onPreExecute(){ 
		   super.onPreExecute();
		}
		@Override
		protected void onPostExecute(Storage result){
			Trending.setHolder(result);		 

		}
	    protected Storage doInBackground(Object... data) 
	    {
	    	Storage holder = (Storage) data[0];
	    	cont = (Context) data[1];
	   		//Get the aggregate rankings
	   		SharedPreferences prefs = cont.getSharedPreferences("FFR", 0); 
			String checkExists = prefs.getString("Posts", "Not Set");
			String[] perPost = checkExists.split("@@@");//ManageInput.tokenize(checkExists, '@', 3);
			String[][] split = new String[perPost.length][];
			for(int i = 0; i < perPost.length; i++)
			{
				split[i] = ManageInput.tokenize(perPost[i], '~', 3);
				Post newPost = new Post(split[i][0], split[i][1]);
				holder.posts.add(newPost);
			}
			System.out.println("In read async: " + holder.posts.size());
			return holder;
	    }
	  }

	/**
	 * In the back-end fetches the news
	 * @author Jeff
	 *
	 */
	public class ReadRotoNews extends AsyncTask<Object, Void, List<NewsObjects>> 
	{
		Activity act;
	    public ReadRotoNews(Context cont) 
	    {
	    	act = (Activity)cont;
	    }

		@Override
		protected void onPreExecute(){ 
		   super.onPreExecute();
		}
		@Override
		protected void onPostExecute(List<NewsObjects> result)
		{
			((News)act).handleNewsListView(result, act);
		}

	    protected List<NewsObjects> doInBackground(Object... data) 
	    {
	    	Context cont = (Context) data[0];
	   		List<NewsObjects> news = ReadFromFile.readNewsRoto(cont);
			return news;
	    }
	  }
}