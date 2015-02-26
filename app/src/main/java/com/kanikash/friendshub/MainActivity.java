package com.kanikash.friendshub;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.facebook.Request;
import com.facebook.model.GraphUser;
import com.kanikash.friendshub.Fragments.LoginFragment;


public class MainActivity extends ActionBarActivity implements LoginFragment.OnScreenActivityListener {
    private LoginFragment loginFragment;
    private static final String TAG = "MainFragment";
    private TextView tvWelcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setTitle("Friends Hub");
        if(savedInstanceState == null) {
            loginFragment = new LoginFragment();
            FragmentManager fm = getSupportFragmentManager();
            // Begin Transaction
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.fragment, loginFragment, "login");
            // Commit transaction
            ft.commit();
        } else {
            loginFragment = (LoginFragment) getSupportFragmentManager().findFragmentByTag("login");
        }
        tvWelcome = (TextView) findViewById(R.id.tvWelcome);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void userDetails(GraphUser user) {
        //tvWelcome.setText(user.getName());
        tvWelcome.setText(user.getProperty("email").toString());
        // Show the Map intent
        Intent i = new Intent(getBaseContext(), MapActivity.class);
        startActivity(i);
    }
}
