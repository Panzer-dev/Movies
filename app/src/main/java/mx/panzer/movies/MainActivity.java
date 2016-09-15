package mx.panzer.movies;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Movie;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import moviedbAPI.Movies;

import static moviedbAPI.Utils.LoadImageFromWebOperations;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    ListView lv;
    SwipeRefreshLayout swipeContainer;
    Handler moviesHandler;
    private Handler moviesPageHandler;
    private Integer PAGE = 1;
    private List<Movies.Movie> moviesList;
    boolean END_REACHED = false;
    private MoviesAdapter mAdapter;
    private View footerView;
    private Boolean isLoading = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lv = (ListView) findViewById(R.id.feedlist);
        View fv = ((LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.footer_list, null, false);
        footerView =  fv;

        lv.setOnScrollListener(new AbsListView.OnScrollListener() {

            private int SCROLL_STATE;

            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
                SCROLL_STATE = i;
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                Log.i("ITEMV", String.valueOf(lv.getLastVisiblePosition()+1));
                Log.i("ITEMT", String.valueOf(totalItemCount));
                if(lv.getLastVisiblePosition()+1 < totalItemCount){
                    END_REACHED = false;
                }else{
                    END_REACHED = true ;
                }
                Log.i("END", String.valueOf(END_REACHED));
                if (firstVisibleItem > 0 && this.SCROLL_STATE == SCROLL_STATE_TOUCH_SCROLL && END_REACHED == true && !isLoading ) {
                    END_REACHED = true;
                    addPage(PAGE);
                    lv.addFooterView(footerView);
                }
            }
        });
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        fab.setVisibility(View.GONE);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        final SwipeRefreshLayout swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);


        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                PAGE =1;
                fillData();
            }

        });

        // Configure the refreshing colors

        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        swipeContainer.setRefreshing(true);
        fillData();
        moviesHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                moviesList = (List<Movies.Movie>) msg.obj;
                mAdapter = new MoviesAdapter(getApplicationContext(), moviesList);
                lv.setAdapter(mAdapter);
                isLoading = false;
                PAGE +=1;
                try {
                    swipeContainer.setRefreshing(false);
                } catch (Exception ex) {
                }

            }
        };
        moviesPageHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                List<Movies.Movie> m = (List<Movies.Movie>) msg.obj;
                for(int x =0;x<m.size();x++){
                    moviesList.add(m.get(x));
                }
                mAdapter.notifyDataSetChanged();
                isLoading = false;
                PAGE += 1;
                lv.removeFooterView(footerView);

            }
        };


    }


    public void fillData() {


        List<Movies.Movie> moviesList = new ArrayList<Movies.Movie>();
        MoviesAdapter ma = new MoviesAdapter(getApplicationContext(), moviesList);
        lv.setAdapter(ma);
        final Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                Message msg = new Message();
                Movies m = new Movies();
                isLoading = true;
                try {
                    List<Movies.Movie> latestMovies = m.getLatestMovies(1);
                    for (Movies.Movie movie : latestMovies) {
                        Log.i("MOVAPI", movie.getPoster_path());
                    }

                    msg.obj = latestMovies;
                    moviesHandler.sendMessage(msg);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();

    }

    public void addPage(final Integer page) {
        final Thread addPageThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Message msg = new Message();
                Movies m = new Movies();
                isLoading = true;
                try {
                    List<Movies.Movie> latestMovies = m.getLatestMovies(page);

                    msg.obj = latestMovies;
                    moviesPageHandler.sendMessage(msg);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        addPageThread.start();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    class MoviesAdapter extends ArrayAdapter<Movies.Movie> {
        private final Context context;
        public final List<Movies.Movie> values;

        public MoviesAdapter(Context context, List<Movies.Movie> values) {
            super(context, -1, values);
            this.context = context;
            this.values = values;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View rowView = inflater.inflate(R.layout.movie_item, parent, false);
            final Movies.Movie m = values.get(position);

            TextView textView = (TextView) rowView.findViewById(R.id.firstLine);
            TextView textView2 = (TextView) rowView.findViewById(R.id.secondLine);
            TextView tv_adult = (TextView) rowView.findViewById(R.id.tv_adult);
            if(!m.getAdult()){
                tv_adult.setVisibility(View.GONE);
            }

            RatingBar rb = (RatingBar) rowView.findViewById(R.id.movieRating);
            final ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
            textView.setText(m.getTitle()+ " ( " +m.getReleaseDate().substring(0,4) +" )");
            textView2.setText(m.getOverview());

            int noStars = (int) (m.getPopularity() / 2);
            rb.setNumStars(noStars);
            rb.getProgressDrawable().setColorFilter(Color.rgb(255, 153, 0), PorterDuff.Mode.SRC_ATOP);
            final List<Drawable> images = new ArrayList<Drawable>(values.size());
            for (int x = 0; x < values.size(); x++) {
                images.add(null);
            }
            final Handler handleImage = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    if (msg.what == 0) {
                        imageView.setImageDrawable((Drawable) msg.obj);
                    }
                }
            };
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    Message msg = new Message();
                    Drawable d = LoadImageFromWebOperations(m.getPoster_path());
                    msg.obj = d;
                    msg.what = 0;
                    handleImage.sendMessage(msg);
                    images.set(position, d);
                }
            });

            //try to get the image from cache else catch it from internet.
            if (images.get(position) != null) {
                //imageView.setImageDrawable(images.get(position));
            } else {
                t.start();
            }


            return rowView;
        }

    }


}
