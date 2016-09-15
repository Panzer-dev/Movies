package moviedbAPI;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by darien on 9/13/16.
 */
public class Movies {
    String DISCOVER_MOVIES_URL = "/discover/movie?primary_release_date.gte=2016-09-01&primary_release_date.lte=2016-09-30";
    public int TOTAL_PAGES;
    public List<Movie> getLatestMovies(Integer _page) throws JSONException {

        String PAGE = Config.PAGE.replace("?",_page.toString());
        String URL = Config.API_BASE_URL + DISCOVER_MOVIES_URL + Config.API_KEY + PAGE;
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(URL)
                .build();



        Log.i("URL",URL);
        Response response = null;
        List<Movie> moviesList = new ArrayList<Movie>();
        try {
            response = client.newCall(request).execute();
            JSONObject responseJ = new JSONObject(response.body().string());
            JSONArray results_list = responseJ.getJSONArray("results");
            TOTAL_PAGES = responseJ.getInt("total_pages");

            for(int x=0;x<results_list.length();x++){
                JSONObject jmovie = (JSONObject) results_list.get(x);
                Movie m = new Movie(jmovie.getInt("id"),jmovie.getString("poster_path"),jmovie.getBoolean("adult"),jmovie.getString("overview"),jmovie.getString("release_date"),
                        jmovie.getString("original_title"),jmovie.getString("title"),jmovie.getString("backdrop_path"),jmovie.getLong("popularity"),jmovie.getInt("vote_count"),
                        jmovie.getBoolean("video"),jmovie.getLong("vote_average"));
                moviesList.add(m);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return moviesList;
    }
    public class Movie{

        int id = -1;
        String poster_path;
        Boolean adult;
        String overview;
        String releaseDate;
        String original_title;
        String title;
        String backdrop_path;
        float popularity;
        int vote_count;
        boolean video;
        float vote_avg;

        public int getId() {
            return id;
        }

        public String getPoster_path() {
            return poster_path;
        }

        public Boolean getAdult() {
            return adult;
        }

        public String getOverview() {
            return overview;
        }

        public String getReleaseDate() {
            return releaseDate;
        }

        public String getOriginal_title() {
            return original_title;
        }

        public String getTitle() {
            return title;
        }

        public String getBackdrop_path() {
            return backdrop_path;
        }

        public float getPopularity() {
            return popularity;
        }

        public int getVote_count() {
            return vote_count;
        }

        public boolean isVideo() {
            return video;
        }

        public float getVote_avg() {
            return vote_avg;
        }

        Movie(int id, String poster_path, Boolean adult, String overview, String releaseDate, String original_title, String title, String backdrop_path, float popularity, int vote_count, boolean video, float vote_avg){
            this.id = id;
            this.poster_path = "http://image.tmdb.org/t/p/w300" + poster_path;
            this.adult = adult;
            this.overview = overview;
            this.releaseDate = releaseDate;
            this.original_title = original_title;
            this.title = title;
            this.backdrop_path = "http://image.tmdb.org/t/p/w300" + backdrop_path;
            this.popularity = popularity;
            this.vote_count = vote_count;
            this.video = video;
            this.vote_avg = vote_avg;


        }
    }

}


