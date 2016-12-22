package com.aaronstacy.thetext.ui;

import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.aaronstacy.thetext.R;

public class MainActivity
    extends AppCompatActivity
    implements BottomNavigationView.OnNavigationItemSelectedListener {

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    if (savedInstanceState == null) {
      getSupportFragmentManager().beginTransaction()
          .replace(R.id.main, ReadFragment.newInstance())
          .commit();

    }

    BottomNavigationView nav = (BottomNavigationView) findViewById(R.id.nav);
    nav.setOnNavigationItemSelectedListener(this);
  }

  @Override public boolean onNavigationItemSelected(@NonNull MenuItem item) {
    // TODO: don't navigate if we're already there
    switch (item.getItemId()) {
      case (R.id.nav_read):
        getSupportFragmentManager().beginTransaction()
            .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left)
            .replace(R.id.main, ReadFragment.newInstance())
            .addToBackStack(null)
            .commit();
        break;
      case (R.id.nav_search):
        getSupportFragmentManager().beginTransaction()
            .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right)
            .replace(R.id.main, SearchFragment.newInstance())
            .addToBackStack(null)
            .commit();
        break;
    }
    return true;
  }

  // TODO: animate navigation on back button press
}
