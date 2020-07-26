package com.numankaraaslan.pdfsearch.fragments;

import android.*;
import android.annotation.*;
import android.content.*;
import android.content.pm.*;
import android.database.*;
import android.net.*;
import android.os.*;
import android.provider.*;
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
import androidx.core.app.*;
import androidx.fragment.app.*;
import androidx.viewpager.widget.*;

public class MultipleSearchFragment extends Fragment
{
	//private MaterialTextView txt_selected_files;
	private TextInputEditText edittext_search;
	private RadioGroup radio_connector;
	private MaterialCheckBox checkbox_search_multiple;
	private MaterialRadioButton radio_search_and;
	private MaterialButton btn_search;
	private MaterialButton btn_select_file;
	private ArrayList<Uri> selected_file_uris;
	private ProgressBar progress_thing;
	private Context context;
	private CustomListAdapter listview_pages_adapter;

	@Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View root = inflater.inflate(R.layout.fragment_multiple_search, container, false);
		context = root.getContext();
		//txt_selected_files = root.findViewById(R.id.txt_selected_file);
		progress_thing = root.findViewById(R.id.progress_thing);
		btn_select_file = root.findViewById(R.id.btn_select_file);
		btn_select_file.setOnClickListener(btn_select_file_clicked());
		btn_search = root.findViewById(R.id.btn_search);
		btn_search.setOnClickListener(btn_search_clicked());
		ListView listview_files = root.findViewById(R.id.listview_files);
		listview_pages_adapter = new CustomListAdapter(context, R.layout.custom_list_view);
		listview_files.setAdapter(listview_pages_adapter);
		radio_connector = root.findViewById(R.id.radio_connector);
		radio_search_and = root.findViewById(R.id.radio_search_and);
		checkbox_search_multiple = root.findViewById(R.id.checkbox_search_type);
		edittext_search = root.findViewById(R.id.edittext_search);
		checkbox_search_multiple.setOnCheckedChangeListener(checkbox_search_type_checked());
		return root;
	}

	private class CustomListAdapter extends ArrayAdapter<SearchResult>
	{
		private LinkedList<SearchResult> search_results;

		public CustomListAdapter(@NonNull Context context, int resource)
		{
			super(context, resource);
			search_results = new LinkedList<>();
		}

		@Override public void add(@Nullable SearchResult object)
		{
			this.search_results.add(object);
			notifyDataSetChanged();
		}

		public void clearSearchResults()
		{
			this.search_results.clear();
			notifyDataSetChanged();
		}

		@SuppressLint("InflateParams") @NonNull @Override public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
		{
			RelativeLayout custom_list_item = (RelativeLayout) convertView;
			if (custom_list_item == null)
			{
				custom_list_item = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.custom_list_view, null);
				SearchResult res = getItem(position);
				if (res != null)
				{
					MaterialTextView txt_report = custom_list_item.findViewById(R.id.txt_report);
					MaterialButton btn_go_to_file = custom_list_item.findViewById(R.id.btn_go_to_file);
					if (res.isBooo())
					{
						btn_go_to_file.setOnClickListener(btn_go_to_file_clicked(res.getUri()));
						txt_report.setText(res.getMessage());
					}
					else
					{
						btn_go_to_file.setVisibility(View.GONE);
						String str = res.getMessage() + " - " + res.getUrl();
						txt_report.setText(str);
					}
				}
			}
			return custom_list_item;
		}

		@Nullable @Override public SearchResult getItem(int position)
		{
			return search_results.get(position);
		}

		@Override public int getCount()
		{
			return search_results.size();
		}
	}

	private static class SearchResult
	{
		private String url;
		private Uri uri;
		private String message;
		private boolean booo;

		public SearchResult(Uri uri, String url, String message, boolean booo)
		{
			this.message = message;
			this.booo = booo;
			this.url = url;
			this.uri = uri;
		}

		public String getMessage()
		{
			return message;
		}

		public boolean isBooo()
		{
			return booo;
		}

		public String getUrl()
		{
			return url;
		}

		public Uri getUri()
		{
			return uri;
		}
	}

	private View.OnClickListener btn_select_file_clicked()
	{
		return v -> {
			if (edittext_search.getText() != null && edittext_search.getText().toString().trim().equals(""))
			{
				new MaterialAlertDialogBuilder(context).setCancelable(false).setTitle(R.string.info).setMessage(R.string.search_text_warning).setPositiveButton(R.string.ok, (dialog, which) -> {
					if (((AlertDialog) dialog).isShowing())
					{
						dialog.dismiss();
					}
				}).create().show();
				return;
			}
			if (check_permissons())
			{
				select_file();
			}
		};
	}

	private void select_file()
	{
		if (Prefs.get_string("file_select_thing").equals(""))
		{
			new MaterialAlertDialogBuilder(context).setCancelable(false).setTitle(R.string.info).setMessage(R.string.file_selection_info).setPositiveButton(R.string.ok, (dialog, which) -> {
				if (((AlertDialog) dialog).isShowing())
				{
					Prefs.put_string("file_select_thing", "1");
					start_file_act();
					dialog.dismiss();
				}
			}).create().show();
		}
		else
		{
			start_file_act();
		}
	}

	private void start_file_act()
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
			intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
			intent.putExtra(Intent.EXTRA_TITLE, true);
			startActivityForResult(Intent.createChooser(intent, "PDFSearch"), 4634);
		}
		catch (Exception ex)
		{
			show_exception(ex);
		}
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

	@SuppressLint("StaticFieldLeak") private View.OnClickListener btn_search_clicked()
	{
		return new View.OnClickListener()
		{
			@Override public void onClick(View v)
			{
				if (edittext_search.getText() != null && edittext_search.getText().toString().trim().equals(""))
				{
					new MaterialAlertDialogBuilder(context).setCancelable(false).setTitle(R.string.info).setMessage(R.string.search_text_warning).setPositiveButton(R.string.ok, (dialog, which) -> {
						if (((AlertDialog) dialog).isShowing())
						{
							dialog.dismiss();
						}
					}).create().show();
					return;
				}
				new AsyncTask<Void, Object, Void>()
				{
					private String search_text;
					private int search_method = -1;
					private boolean is_multiple;

					@Override protected void onPreExecute()
					{
						super.onPreExecute();
						try
						{
							progress_thing.setVisibility(View.VISIBLE);
							btn_select_file.setEnabled(false);
							btn_search.setEnabled(false);
							listview_pages_adapter.clearSearchResults();
							search_text = edittext_search.getText() != null ? edittext_search.getText().toString().toLowerCase(Locale.getDefault()) : "";
							is_multiple = checkbox_search_multiple.isChecked();
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

					@Override protected Void doInBackground(Void... voids)
					{
						for (Uri uri : selected_file_uris)
						{
							PdfReader reader;
							String path = "";
							String url = uri.getPath();
							ContentResolver contentResolver = context.getContentResolver();
							Cursor cursor = contentResolver.query(uri, null, null, null, null);
							String file_name = "";
							try
							{
								if (cursor != null && cursor.moveToFirst())
								{
									file_name = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
								}
							}
							catch (Exception ignored)
							{
							}
							try
							{
								if (url.contains("raw:"))
								{
									path = url.substring(url.indexOf(":") + 1);
								}
								else
								{
									path = Environment.getExternalStorageDirectory().getPath() + "/" + (url.contains("home:") ? "Documents/" : "") + url.substring(url.indexOf(":") + 1);
								}
								reader = new PdfReader(path);
							}
							catch (Exception ex)
							{
								publishProgress(uri, url, file_name + "\n" + getString(R.string.error_reading_file), false);
								continue;
							}
							int n = reader.getNumberOfPages();
							int number_of_finds = 0;
							StringBuilder temp = new StringBuilder();
							File file = new File(path);
							temp.append("Found in ").append(file_name).append(" (number_of_finds)\n");
							for (int i = 1; i <= n; i++)
							{
								TextExtractionStrategy strategy;
								strategy = new SimpleTextExtractionStrategy();
								try
								{
									String page_content = PdfTextExtractor.getTextFromPage(reader, i, strategy);
									if (!is_multiple)
									{
										if (page_content.toLowerCase(Locale.getDefault()).contains(search_text))
										{
											number_of_finds++;
											temp.append(i).append(", ");
										}
									}
									else
									{
										if (search_method == 1)
										{
											// and
											String[] splitted = search_text.split(",");
											boolean got_it = true;
											for (String string : splitted)
											{
												if (!page_content.toLowerCase(Locale.getDefault()).contains(string))
												{
													got_it = false;
													break;
												}
											}
											if (got_it)
											{
												number_of_finds++;
												temp.append(i).append(", ");
											}
										}
										else if (search_method == 2)
										{
											// or
											String[] splitted = search_text.split(",");
											boolean got_it = false;
											for (String string : splitted)
											{
												if (page_content.toLowerCase(Locale.getDefault()).contains(string))
												{
													got_it = true;
													break;
												}
											}
											if (got_it)
											{
												number_of_finds++;
												temp.append(i).append(", ");
											}
										}
									}
								}
								catch (Exception ignored)
								{
								}
							}
							if (number_of_finds > 0)
							{
								String message = temp.toString().substring(0, temp.length() - 2);
								message = message.replace("number_of_finds", number_of_finds + "");
								publishProgress(uri, url, message, true);
							}
						}
						return null;
					}

					@Override protected void onProgressUpdate(Object... values)
					{
						SearchResult res = new SearchResult((Uri) values[0], (String) values[1], (String) values[2], (Boolean) values[3]);
						listview_pages_adapter.add(res);
					}

					@Override protected void onPostExecute(Void aVoid)
					{
						super.onPostExecute(aVoid);
						progress_thing.setVisibility(View.GONE);
						btn_select_file.setEnabled(true);
						btn_search.setEnabled(true);
					}
				}.execute();
			}
		};
	}

	private CompoundButton.OnCheckedChangeListener checkbox_search_type_checked()
	{
		return (buttonView, isChecked) -> {
			radio_connector.setVisibility(isChecked ? View.VISIBLE : View.GONE);
			if (isChecked)
			{
				edittext_search.setHint(R.string.multiple_search_hint);
			}
			else
			{
				edittext_search.setHint(R.string.singular_search_hint);
			}
		};
	}

	@Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
	{
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		try
		{
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
		catch (Exception ex)
		{
			show_exception(ex);
		}
	}

	@Override public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		try
		{
			if (requestCode == 4634)
			{
				if (data != null && data.getClipData() != null)
				{
					ClipData clipData = data.getClipData();
					read_content(clipData, checkbox_search_multiple.isChecked());
				}
			}
		}
		catch (Exception ex)
		{
			show_exception(ex);
		}
	}

	@SuppressLint("StaticFieldLeak") private void read_content(ClipData clipData, boolean is_multiple)
	{
		new AsyncTask<Void, Object, Void>()
		{
			private String search_text = "";
			private int search_method = 0;

			@Override protected void onPreExecute()
			{
				super.onPreExecute();
				try
				{
					progress_thing.setVisibility(View.VISIBLE);
					listview_pages_adapter.clearSearchResults();
					btn_select_file.setEnabled(false);
					btn_search.setEnabled(false);
					StringBuilder file_name = new StringBuilder();
					//file_name.append(getString(R.string.selected_filea)).append("\n");
					for (int i = 0; i < clipData.getItemCount(); i++)
					{
						ClipData.Item item = clipData.getItemAt(i);
						Uri uri = item.getUri();
						String url = uri.getPath();
						if (url == null)
						{
							continue;
						}
						System.err.println("URL   = " + url);
						File file = new File(Environment.getExternalStorageDirectory().getPath() + "/" + (url.contains("home:") ? "Documents/" : "") + url.substring(url.indexOf(":") + 1));
						System.err.println("FEXISTS " + file.exists());
						file_name.append("> ").append(file.getName());
						if (i < clipData.getItemCount() - 1)
						{
							file_name.append("\n");
						}
					}
					selected_file_uris = new ArrayList<>();
					//txt_selected_files.setText(file_name.toString());
					//txt_selected_files.setVisibility(View.VISIBLE);
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

			@Override protected Void doInBackground(Void... Void)
			{
				for (int d = 0; d < clipData.getItemCount(); d++)
				{
					StringBuilder temp = new StringBuilder();
					ClipData.Item item = clipData.getItemAt(d);
					Uri uri = item.getUri();
					byte[] byte_array = null;
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
					{
						try
						{
							InputStream inStream = context.getContentResolver().openInputStream(uri);
							if (inStream != null)
							{
								byte_array = new byte[inStream.available()];
								inStream.read(byte_array);
							}
						}
						catch (Exception ex)
						{
							continue;
						}
					}
					String url = uri.getPath();
					if (url == null)
					{
						continue;
					}
					String path = "";
					if (url.contains("raw:"))
					{
						path = url.substring(url.indexOf(":") + 1);
					}
					else
					{
						path = Environment.getExternalStorageDirectory().getPath() + "/" + (url.contains("home:") ? "Documents/" : "") + url.substring(url.indexOf(":") + 1);
					}
					PdfReader reader;
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
						selected_file_uris.add(uri);
					}
					catch (Exception ex)
					{
						publishProgress(uri, url, file_name + "\n" + getString(R.string.error_reading_file), false);
						continue;
					}
					int n = reader.getNumberOfPages();
					int number_of_finds = 0;
					File file = new File(path);
					temp.append("Found in ").append(file_name).append(" (number_of_finds)\n");
					for (int i = 1; i <= n; i++)
					{
						TextExtractionStrategy strategy;
						strategy = new SimpleTextExtractionStrategy();
						try
						{
							String page_content = PdfTextExtractor.getTextFromPage(reader, i, strategy).toLowerCase();
							if (!is_multiple)
							{
								if (page_content.toLowerCase(Locale.getDefault()).contains(search_text))
								{
									number_of_finds++;
									temp.append(i).append(", ");
								}
							}
							else
							{
								if (search_method == 1)
								{
									// and
									String[] splitted = search_text.split(",");
									boolean got_it = true;
									for (String string : splitted)
									{
										if (!page_content.toLowerCase(Locale.getDefault()).contains(string))
										{
											got_it = false;
											break;
										}
									}
									if (got_it)
									{
										number_of_finds++;
										temp.append(i).append(", ");
									}
								}
								else if (search_method == 2)
								{
									// or
									String[] splitted = search_text.split(",");
									boolean got_it = false;
									for (String string : splitted)
									{
										if (page_content.toLowerCase(Locale.getDefault()).contains(string))
										{
											got_it = true;
											break;
										}
									}
									if (got_it)
									{
										number_of_finds++;
										temp.append(i).append(", ");
									}
								}
							}
						}
						catch (Exception ignored)
						{
						}
					}
					if (number_of_finds > 0)
					{
						String message = temp.toString().substring(0, temp.length() - 2);
						message = message.replace("number_of_finds", number_of_finds + "");
						publishProgress(uri, url, message, true);
					}
				}
				return null;
			}

			@Override protected void onProgressUpdate(Object... values)
			{
				// url, message, result
				SearchResult res = new SearchResult((Uri) values[0], (String) values[1], (String) values[2], (Boolean) values[3]);
				listview_pages_adapter.add(res);
			}

			@Override protected void onPostExecute(Void aVoid)
			{
				super.onPostExecute(aVoid);
				progress_thing.setVisibility(View.GONE);
				btn_select_file.setEnabled(true);
				btn_search.setEnabled(true);
			}
		}.execute();
	}

	private View.OnClickListener btn_go_to_file_clicked(Uri uri)
	{
		return v -> {
			try
			{
				if (getView() != null)
				{
					ViewPager vp = (ViewPager) getView().getParent();
					SearchPagerAdapter adapter = (SearchPagerAdapter) vp.getAdapter();
					String expression = edittext_search.getText() != null ? edittext_search.getText().toString().toLowerCase(Locale.getDefault()) : "";
					boolean is_multiple = checkbox_search_multiple.isChecked();
					int search_method = radio_search_and.isChecked() ? 1 : 2;
					vp.setCurrentItem(0, true);
					if (adapter != null)
					{
						adapter.go_to_first_with_url(uri, expression, is_multiple, search_method);
					}
				}
			}
			catch (Exception ex)
			{
				show_exception(ex);
			}
		};
	}

	private void show_exception(Exception ex)
	{
		StringBuilder message = new StringBuilder();
		StackTraceElement elem = ex.getStackTrace()[0];
		String classss = elem.getClassName();
		classss = classss.substring(classss.lastIndexOf(".") + 1);
		message.append(classss).append(" - ").append(elem.getMethodName()).append(" - ").append(elem.getLineNumber()).append("\n");
		message.append(elem.toString());
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

