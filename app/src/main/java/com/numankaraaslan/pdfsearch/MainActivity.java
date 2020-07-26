package com.numankaraaslan.pdfsearch;

import android.content.*;
import android.os.*;
import android.widget.*;

import com.google.android.material.tabs.*;

import androidx.appcompat.app.*;
import androidx.viewpager.widget.*;

public class MainActivity extends AppCompatActivity
{
	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Prefs.set_statics(this);
		SearchPagerAdapter sectionsPagerAdapter = new SearchPagerAdapter(this, getSupportFragmentManager());
		ViewPager viewPager = findViewById(R.id.view_pager);
		viewPager.setAdapter(sectionsPagerAdapter);
		TabLayout tabs = findViewById(R.id.tabs);
		tabs.setupWithViewPager(viewPager);
		ImageButton btn_help = findViewById(R.id.btn_help);
		btn_help.setOnClickListener(v -> showHelp());
	}

	private void showHelp()
	{
		Intent help_intent = new Intent(this, HelpAct.class);
		help_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(help_intent);
	}
}