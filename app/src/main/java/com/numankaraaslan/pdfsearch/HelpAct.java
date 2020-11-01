package com.numankaraaslan.pdfsearch;

import android.content.*;
import android.net.*;
import android.os.*;
import android.view.*;
import android.widget.*;

import androidx.appcompat.app.*;

public class HelpAct extends AppCompatActivity
{
	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help);
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null)
		{
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
		ImageView img_blog = findViewById(R.id.img_blog);
		ImageView img_linkedin = findViewById(R.id.img_linkedin);
		img_blog.setOnClickListener(img_click("blog"));
		img_linkedin.setOnClickListener(img_click("linkedin"));
	}

	private View.OnClickListener img_click(String where)
	{
		return view -> {
			if (where.equals("blog"))
			{
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://numankaraaslan.com")));
			}
			else if (where.equals("linkedin"))
			{
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.linkedin.com/in/numan-karaaslan-1053b9b3")));
			}
		};
	}

	@Override public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == android.R.id.home)
		{
			finish();
		}
		return true;
	}
}