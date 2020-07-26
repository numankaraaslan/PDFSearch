package com.numankaraaslan.pdfsearch;

import android.content.*;
import android.content.res.*;
import android.net.*;

import com.numankaraaslan.pdfsearch.fragments.*;

import androidx.annotation.*;
import androidx.fragment.app.*;

public class SearchPagerAdapter extends FragmentPagerAdapter
{
	private final Context mContext;
	private SimpleSearchFragment simple_fragment;
	private MultipleSearchFragment multiple_fragment;

	public SearchPagerAdapter(Context context, FragmentManager fm)
	{
		super(fm);
		this.mContext = context;
		simple_fragment = new SimpleSearchFragment();
		multiple_fragment = new MultipleSearchFragment();
	}

	@NonNull @Override public Fragment getItem(int position)
	{
		if (position == 0)
		{
			return simple_fragment;
		}
		else
		{
			return multiple_fragment;
		}
	}

	@Nullable @Override public CharSequence getPageTitle(int position)
	{
		Resources resources = mContext.getResources();
		if (position == 0)
		{
			return resources.getString(R.string.tab_simple_search);
		}
		else
		{
			return resources.getString(R.string.tab_advanced_search);
		}
	}

	@Override public int getCount()
	{
		return 2;
	}

	public void go_to_first_with_url(Uri uri, String expression, boolean is_multiple, int search_method)
	{
		simple_fragment.find_in_file(uri, expression, is_multiple, search_method);
	}
}