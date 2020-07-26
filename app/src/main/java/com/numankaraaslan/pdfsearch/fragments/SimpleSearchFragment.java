package com.numankaraaslan.pdfsearch.fragments;

import android.*;
import android.annotation.*;
import android.content.*;
import android.content.pm.*;
import android.database.*;
import android.net.*;
import android.os.*;
import android.provider.*;
import android.text.*;
import android.text.style.*;
import android.view.*;
import android.widget.*;

import com.google.android.material.button.*;
import com.google.android.material.checkbox.*;
import com.google.android.material.dialog.*;
import com.google.android.material.radiobutton.*;
import com.google.android.material.textfield.*;
import com.google.android.material.textview.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.parser.*;
import com.numankaraaslan.pdfsearch.R;
import com.numankaraaslan.pdfsearch.*;

import java.io.*;
import java.util.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.*;
import androidx.appcompat.widget.*;
import androidx.core.app.*;
import androidx.fragment.app.*;

public class SimpleSearchFragment extends Fragment
{
	private MaterialTextView txt_page;
	private MaterialTextView txt_selected_file;
	private ListView listview_pages;
	private LinearLayout ll_right;
	private TextInputEditText edittext_search;
	private RadioGroup radio_connector;
	private HashMap<Integer, String> pdf_contents;
	private ArrayList<String> pages;
	private ArrayAdapter<String> listview_pages_adapter;
	private MaterialCheckBox checkbox_search_multiple;
	private MaterialRadioButton radio_search_and;
	private MaterialRadioButton radio_search_or;
	private MaterialButton btn_search;
	private MaterialButton btn_select_file;
	private ProgressBar progress_thing;
	private Context context;

	@Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View root = inflater.inflate(R.layout.fragment_simple_search, container, false);
		context = root.getContext();
		txt_page = root.findViewById(R.id.txt_page);
		txt_selected_file = root.findViewById(R.id.txt_selected_file);
		progress_thing = root.findViewById(R.id.progress_thing);
		btn_select_file = root.findViewById(R.id.btn_select_file);
		btn_search = root.findViewById(R.id.btn_search);
		listview_pages = root.findViewById(R.id.listview_pages);
		ll_right = root.findViewById(R.id.list_right);
		radio_connector = root.findViewById(R.id.radio_connector);
		radio_search_and = root.findViewById(R.id.radio_search_and);
		radio_search_or = root.findViewById(R.id.radio_search_or);
		checkbox_search_multiple = root.findViewById(R.id.checkbox_search_type);
		edittext_search = root.findViewById(R.id.edittext_search);
		pages = new ArrayList<>();
		listview_pages_adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_single_choice, pages);
		listview_pages.setAdapter(listview_pages_adapter);
		listview_pages.setOnItemClickListener(list_item_clicked());
		listview_pages.setOnItemSelectedListener(list_item_selected());
		btn_select_file.setOnClickListener(btn_select_file_clicked());
		btn_search.setOnClickListener(btn_search_clicked());
		checkbox_search_multiple.setOnCheckedChangeListener(checkbox_search_type_checked());
		return root;
	}

	private AdapterView.OnItemSelectedListener list_item_selected()
	{
		return new AdapterView.OnItemSelectedListener()
		{
			@Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
			{
				if (view != null)
				{
					listview_item_selected((AppCompatCheckedTextView) view);
				}
			}

			@Override public void onNothingSelected(AdapterView<?> parent)
			{
			}
		};
	}

	private AdapterView.OnItemClickListener list_item_clicked()
	{
		return (parent, view, position, id) -> listview_item_selected((AppCompatCheckedTextView) view);
	}

	private void listview_item_selected(AppCompatCheckedTextView view)
	{
		String page_text = view.getText().toString();
		try
		{
			Integer page = Integer.parseInt(page_text.substring(page_text.indexOf(" ") + 1));
			txt_page.setText(pdf_contents.get(page));
			if (edittext_search.getText() != null && !edittext_search.getText().toString().trim().equals(""))
			{
				span_it();
			}
		}
		catch (Exception ex)
		{
			show_exception(ex);
		}
	}

	private CompoundButton.OnCheckedChangeListener checkbox_search_type_checked()
	{
		return (buttonView, isChecked) -> {
			radio_connector.setVisibility(isChecked ? View.VISIBLE : View.GONE);
			if (isChecked)
			{
				edittext_search.setHint(getString((R.string.multiple_search_hint)));
			}
			else
			{
				edittext_search.setHint(getString((R.string.singular_search_hint)));
			}
		};
	}

	private View.OnClickListener btn_select_file_clicked()
	{
		return v -> {
			if (check_permissons())
			{
				select_file();
			}
		};
	}

	private boolean check_permissons()
	{
		try
		{
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
			{
				boolean res;
				res = ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
				if (!res)
				{
					requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 4635);
				}
				return res;
			}
			else
			{
				return true;
			}
		}
		catch (Exception ex)
		{
			show_exception(ex);
			return false;
		}
	}

	private void select_file()
	{
		try
		{
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
			intent.addCategory(Intent.CATEGORY_OPENABLE);
			intent.setType("*/*");
			String[] extraMimeTypes = {"application/pdf"};
			intent.putExtra(Intent.EXTRA_MIME_TYPES, extraMimeTypes);
			intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
			intent.putExtra(Intent.EXTRA_TITLE, true);
			startActivityForResult(Intent.createChooser(intent, "PDFSearch"), 4634);
		}
		catch (Exception ex)
		{
			show_exception(ex);
		}
	}

	private View.OnClickListener btn_search_clicked()
	{
		return v -> {
			progress_thing.setVisibility(View.VISIBLE);
			pages.clear();
			txt_page.setText("");
			listview_pages_adapter.notifyDataSetChanged();
			String search_text = edittext_search.getText() != null ? edittext_search.getText().toString().toLowerCase(Locale.getDefault()) : "";
			boolean first = true;
			for (Map.Entry<Integer, String> page_content : pdf_contents.entrySet())
			{
				if (checkbox_search_multiple.isChecked())
				{
					if (radio_search_and.isChecked())
					{
						// and
						String[] splitted = search_text.split(",");
						boolean found = true;
						for (String string : splitted)
						{
							if (!page_content.getValue().toLowerCase(Locale.getDefault()).contains(string))
							{
								found = false;
								break;
							}
						}
						if (found)
						{
							pages.add(page_content.getKey() + "");
							listview_pages_adapter.notifyDataSetChanged();
							if (first)
							{
								first = false;
								txt_page.setText(pdf_contents.get(page_content.getKey()));
								listview_pages.requestFocusFromTouch();
								listview_pages.setSelection(0);
								listview_pages.setItemChecked(0, true);
								ll_right.setVisibility(View.VISIBLE);
							}
						}
					}
					else if (radio_search_or.isChecked())
					{
						// or
						String[] splitted = search_text.split(",");
						boolean found = false;
						for (String string : splitted)
						{
							if (page_content.getValue().toLowerCase(Locale.getDefault()).contains(string))
							{
								found = true;
								break;
							}
						}
						if (found)
						{
							pages.add(page_content.getKey() + "");
							listview_pages_adapter.notifyDataSetChanged();
							if (first)
							{
								first = false;
								txt_page.setText(pdf_contents.get(page_content.getKey()));
								listview_pages.requestFocusFromTouch();
								listview_pages.setSelection(0);
								listview_pages.setItemChecked(0, true);
								ll_right.setVisibility(View.VISIBLE);
							}
						}
					}
				}
				else if (page_content.getValue() != null && page_content.getValue().toLowerCase(Locale.getDefault()).contains(search_text))
				{
					pages.add(page_content.getKey() + "");
					listview_pages_adapter.notifyDataSetChanged();
					if (first)
					{
						first = false;
						txt_page.setText(pdf_contents.get(page_content.getKey()));
						listview_pages.requestFocusFromTouch();
						listview_pages.setSelection(0);
						listview_pages.setItemChecked(0, true);
						ll_right.setVisibility(View.VISIBLE);
					}
				}
			}
			if (first)
			{
				ll_right.setVisibility(View.GONE);
				txt_page.setText(R.string.no_result_found);
			}
			progress_thing.setVisibility(View.GONE);
		};
	}

	@Override public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		try
		{
			if (requestCode == 4634)
			{
				if (data != null && data.getData() != null)
				{
					if (data.getData().getPath() != null)
					{
						System.err.println("DATA = " + data.getData());
						read_content(data.getData(), checkbox_search_multiple.isChecked());
					}
				}
			}
		}
		catch (Exception ex)
		{
			show_exception(ex);
		}
	}

	@Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
	{
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == 4635)
		{
			boolean res = true;
			for (int resp : grantResults)
			{
				if (resp != PackageManager.PERMISSION_GRANTED)
				{
					res = false;
					break;
				}
			}
			if (!res)
			{
				new MaterialAlertDialogBuilder(context).setCancelable(false).setTitle(R.string.error).setMessage(R.string.permissons_required).setPositiveButton(R.string.ok, (dialog, which) -> {
					if (((AlertDialog) dialog).isShowing())
					{
						dialog.dismiss();
					}
				}).create().show();
			}
			else
			{
				super.onRequestPermissionsResult(requestCode, permissions, grantResults);
				select_file();
			}
		}
	}

	private void span_it()
	{
		if (edittext_search.getText() != null)
		{
			String search_text = edittext_search.getText().toString().toLowerCase(Locale.getDefault());
			String content = txt_page.getText().toString();
			if (checkbox_search_multiple.isChecked())
			{
				if (!content.equals(""))
				{
					String[] splitted = search_text.split(",");
					Spannable wordToSpan = new SpannableString(content);
					for (String word : splitted)
					{
						ArrayList<Integer> indexes = get_indexes_of(content.toLowerCase(Locale.getDefault()), word);
						for (int index : indexes)
						{
							if (index != -1)
							{
								wordToSpan.setSpan(new BackgroundColorSpan(0xFFFFFF00), index, index + word.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
							}
						}
					}
					txt_page.setText(wordToSpan, TextView.BufferType.SPANNABLE);
				}
			}
			else
			{
				if (!content.equals(""))
				{
					Spannable wordToSpan = new SpannableString(content);
					ArrayList<Integer> indexes = get_indexes_of(content.toLowerCase(Locale.getDefault()), search_text);
					for (int index : indexes)
					{
						wordToSpan.setSpan(new BackgroundColorSpan(0xFFFFFF00), index, index + search_text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					}
					txt_page.setText(wordToSpan, TextView.BufferType.SPANNABLE);
				}
			}
		}
	}

	private ArrayList<Integer> get_indexes_of(String page_content, String search)
	{
		try
		{
			ArrayList<Integer> res = new ArrayList<>();
			String substring = page_content;
			int last = 0;
			do
			{
				res.add(substring.indexOf(search) + last);
				last = substring.indexOf(search) + last + search.length();
				substring = substring.substring(substring.indexOf(search) + search.length());
			}
			while (substring.contains(search));
			return res;
		}
		catch (Exception ex)
		{
			show_exception(ex);
			return new ArrayList<>();
		}
	}

	@SuppressLint("StaticFieldLeak") private void read_content(Uri uri, boolean is_multiple)
	{
		new AsyncTask<Void, String, Boolean>()
		{
			private boolean first = true;
			private String search_text = "";
			private int search_method = 0;
			private String path = "";
			private byte[] byte_array = null;

			@Override protected void onPreExecute()
			{
				super.onPreExecute();
				try
				{
					progress_thing.setVisibility(View.VISIBLE);
					btn_select_file.setEnabled(false);
					btn_search.setEnabled(false);
					pdf_contents = new HashMap<>();
					ll_right.setVisibility(View.GONE);
					pages.clear();
					txt_page.setText("");
					listview_pages_adapter.notifyDataSetChanged();
					String url = uri.getPath();
					System.err.println("URI   = " + url);
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
					{
						InputStream inStream = context.getContentResolver().openInputStream(uri);
						if (inStream != null)
						{
							byte_array = new byte[inStream.available()];
							inStream.read(byte_array);
						}
					}
					if (url != null && url.contains("raw:"))
					{
						path = url.substring(url.indexOf(":") + 1);
					}
					else
					{
						if (url != null)
						{
							path = Environment.getExternalStorageDirectory().getPath() + "/" + (url.contains("home:") ? "Documents/" : "") + url.substring(url.indexOf(":") + 1);
						}
					}
					System.err.println("PATH  = " + path);
					File file = new File(path);
					ContentResolver contentResolver = context.getContentResolver();
					Cursor cursor = contentResolver.query(uri, null, null, null, null);
					String file_name = "";
					try
					{
						if (cursor != null && cursor.moveToFirst())
						{
							file_name = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
							cursor.close();
						}
					}
					catch (Exception ignored)
					{
					}
					System.err.println("FNAME = " + file_name);
					txt_selected_file.setText(file_name);
					System.err.println("EXISTS = " + file.exists());
					search_text = edittext_search.getText() != null ? edittext_search.getText().toString().toLowerCase(Locale.getDefault()) : "";
					if (is_multiple)
					{
						search_method = radio_search_and.isChecked() ? 1 : 2;
					}
				}
				catch (Exception ex)
				{
					show_exception(ex);
				}
			}

			@Override protected Boolean doInBackground(Void... Void)
			{
				PdfReader reader;
				try
				{
					if (byte_array != null)
					{
						System.err.println("FROM = stream");
						reader = new PdfReader(byte_array);
					}
					else
					{
						System.err.println("FROM = path");
						reader = new PdfReader(path);
					}
				}
				catch (Exception ex)
				{
					System.err.println(ex.getMessage());
					return false;
				}
				int n = reader.getNumberOfPages();
				for (int i = 1; i <= n; i++)
				{
					TextExtractionStrategy strategy;
					strategy = new SimpleTextExtractionStrategy();
					try
					{
						String page_content = PdfTextExtractor.getTextFromPage(reader, i, strategy);
						pdf_contents.put(i, page_content);
						if (!"".equals(search_text))
						{
							if (!is_multiple)
							{
								if (page_content.toLowerCase(Locale.getDefault()).contains(search_text))
								{
									publishProgress(i + "");
								}
							}
							else
							{
								if (search_method == 1)
								{
									// and
									String[] splitted = search_text.split(",");
									boolean found = true;
									for (String string : splitted)
									{
										if (!page_content.toLowerCase(Locale.getDefault()).contains(string))
										{
											found = false;
											break;
										}
									}
									if (found)
									{
										publishProgress(i + "");
									}
								}
								else if (search_method == 2)
								{
									// or
									String[] splitted = search_text.split(",");
									boolean found = false;
									for (String string : splitted)
									{
										if (page_content.toLowerCase(Locale.getDefault()).contains(string))
										{
											found = true;
											break;
										}
									}
									if (found)
									{
										publishProgress(i + "");
									}
								}
							}
						}
						else
						{
							publishProgress(i + "");
						}
					}
					catch (Exception ignored)
					{
					}
				}
				return true;
			}

			@Override protected void onProgressUpdate(String... values)
			{
				super.onPreExecute();
				try
				{
					pages.add(values[0]);
					listview_pages_adapter.notifyDataSetChanged();
					if (first)
					{
						first = false;
						txt_page.setText(pdf_contents.get(Integer.parseInt(values[0].substring(values[0].indexOf(" ") + 1))));
						listview_pages.setItemChecked(0, true);
						listview_item_selected((AppCompatCheckedTextView) listview_pages_adapter.getView(0, null, listview_pages));
						ll_right.setVisibility(View.VISIBLE);
					}
				}
				catch (Exception ex)
				{
					show_exception(ex);
				}
			}

			@Override protected void onPostExecute(Boolean booo)
			{
				super.onPostExecute(booo);
				try
				{
					progress_thing.setVisibility(View.GONE);
					if (!booo)
					{
						txt_page.setText(R.string.error_reading_file);
					}
					else
					{
						if (first)
						{
							txt_page.setText(R.string.no_result_found);
						}
					}
					btn_search.setEnabled(true);
					btn_select_file.setEnabled(true);
				}
				catch (Exception ex)
				{
					show_exception(ex);
				}
			}
		}.execute();
	}

	public void find_in_file(Uri uri, String expression, boolean is_multiple, int search_method)
	{
		if (is_multiple)
		{
			checkbox_search_multiple.setChecked(true);
			if (search_method == 1)
			{
				radio_search_and.setChecked(true);
			}
			else
			{
				radio_search_or.setChecked(true);
			}
		}
		edittext_search.setText(expression);
		read_content(uri, is_multiple);
	}

	private void show_exception(Exception ex)
	{
		StringBuilder message = new StringBuilder();
		StackTraceElement elem = ex.getStackTrace()[0];
		String classss = elem.getClassName();
		classss = classss.substring(classss.lastIndexOf(".") + 1);
		message.append(classss).append(" - ").append(elem.getMethodName()).append(" - ").append(elem.getLineNumber()).append("\n");
		if (BuildConfig.DEBUG)
		{
			new MaterialAlertDialogBuilder(context).setCancelable(false).setTitle(R.string.error).setMessage(message.toString()).setPositiveButton(R.string.ok, (dialog, which) -> {
				if (((AlertDialog) dialog).isShowing())
				{
					dialog.dismiss();
				}
			}).create().show();
		}
	}
}
