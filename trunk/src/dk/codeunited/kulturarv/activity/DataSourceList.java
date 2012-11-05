package dk.codeunited.kulturarv.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import dk.codeunited.kulturarv.R;
import dk.codeunited.kulturarv.data.DataSource;
import dk.codeunited.kulturarv.data.DataSourceStorage;

/**
 * @author mixare
 * @author Kostas Rutkauskas
 */
public class DataSourceList extends ListActivity {

	private static DataSourceAdapter dataSourceAdapter;

	private static final int MENU_EDIT_MAX_OBJECTS = Menu.FIRST;

	/**
	 * Limit on how many "max objects" may be set.
	 */
	private static final int MAX_OBJECTS_LIMIT = 100;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(R.string.data_sources_list_activity_title);
	}

	@Override
	protected void onResume() {
		super.onResume();

		dataSourceAdapter = new DataSourceAdapter();
		for (DataSource dataSource : DataSourceStorage.getDataSources()) {
			dataSourceAdapter.addItem(dataSource);
		}
		setListAdapter(dataSourceAdapter);

		ListView lv = getListView();
		registerForContextMenu(lv);
	}

	// TODO: check if it's really needed
	public static String getDataSourcesStringList() {
		String ret = "";
		boolean first = true;

		for (int i = 0; i < dataSourceAdapter.getCount(); i++) {
			if (dataSourceAdapter.getItemEnabled(i)) {
				if (!first) {
					ret += ", ";
				}
				ret += dataSourceAdapter.getItemName(i);
				first = false;
			}
		}

		return ret;
	}

	private class DataSourceAdapter extends BaseAdapter implements
			OnCheckedChangeListener {

		private List<DataSource> mDataSource = new ArrayList<DataSource>();
		private LayoutInflater mInflater;

		public DataSourceAdapter() {
			mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public boolean getItemEnabled(int k) {
			return mDataSource.get(k).isEnabled();
		}

		public String getItemName(int k) {
			return mDataSource.get(k).getName();

		}

		public void addItem(final DataSource item) {
			mDataSource.add(item);
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return mDataSource.size();
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;

			convertView = mInflater.inflate(R.layout.datasourcelist, null);
			holder = new ViewHolder();
			holder.color = (LinearLayout) convertView
					.findViewById(R.id.datasource_color);
			holder.text = (TextView) convertView.findViewById(R.id.list_text);
			holder.maxObjects = (TextView) convertView
					.findViewById(R.id.txt_max_objects);
			holder.checkbox = (CheckBox) convertView
					.findViewById(R.id.list_checkbox);
			holder.checkbox.setTag(position);

			holder.checkbox.setOnCheckedChangeListener(this);

			convertView.setTag(holder);

			holder.color.setBackgroundColor(mDataSource.get(position)
					.getColor());
			holder.text.setText(mDataSource.get(position).getName());
			holder.maxObjects.setText(getString(R.string.max_objects) + ": "
					+ +mDataSource.get(position).getMaxObjects());

			holder.checkbox.setChecked(mDataSource.get(position).isEnabled());
			holder.checkbox.setEnabled(mDataSource.get(position)
					.getCanBeDisabled());

			return convertView;
		}

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			int position = (Integer) buttonView.getTag();
			mDataSource.get(position).setEnabled(isChecked);
		}

		private class ViewHolder {
			public ViewHolder() {
				//
			}

			LinearLayout color;
			TextView text;
			TextView maxObjects;
			CheckBox checkbox;
		}

		@Override
		public Object getItem(int position) {
			return mDataSource.get(position);
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		menu.add(MENU_EDIT_MAX_OBJECTS, MENU_EDIT_MAX_OBJECTS,
				MENU_EDIT_MAX_OBJECTS, R.string.data_source_edit_max_objects);
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info;
		try {
			info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		} catch (ClassCastException e) {
			return false;
		}
		DataSource selectedDataSource = (DataSource) getListAdapter().getItem(
				info.position);
		switch (item.getItemId()) {
		case MENU_EDIT_MAX_OBJECTS:
			if (selectedDataSource.isMaxObjectsEditable()) {
				createMaxObjectsDialog(selectedDataSource).show();
			} else {
				Toast.makeText(DataSourceList.this,
						getString(R.string.max_objects_for_this_datasource),
						Toast.LENGTH_LONG).show();
			}
			break;
		}
		return super.onContextItemSelected(item);
	}

	private Dialog createMaxObjectsDialog(final DataSource dataSource) {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
		alertDialog.setTitle(getString(R.string.max_objects_for) + " "
				+ dataSource.getName());

		final EditText txtMaxObjects = new EditText(this);
		txtMaxObjects.setInputType(InputType.TYPE_CLASS_NUMBER);
		txtMaxObjects.setText(dataSource.getMaxObjects() + "");

		alertDialog.setView(txtMaxObjects);

		alertDialog.setPositiveButton(getString(R.string.save),
				new OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						int maxObjects = dataSource.getMaxObjects();
						try {
							int inputMaxObjects = Integer
									.parseInt(txtMaxObjects.getText()
											.toString());
							if (inputMaxObjects <= 0) {
								Toast.makeText(
										DataSourceList.this,
										getString(R.string.use_valid_positive_integer),
										Toast.LENGTH_LONG).show();
							} else if (inputMaxObjects > MAX_OBJECTS_LIMIT) {
								Toast.makeText(
										DataSourceList.this,
										getString(R.string.max_objects_should_be_less_than_or_equal)
												+ " " + MAX_OBJECTS_LIMIT,
										Toast.LENGTH_LONG).show();
							} else {
								maxObjects = inputMaxObjects;
							}
						} catch (Exception e) {
							Toast.makeText(
									DataSourceList.this,
									getString(R.string.use_valid_positive_integer),
									Toast.LENGTH_LONG).show();
						} finally {
							dataSource.setMaxObjects(maxObjects);
							DataSourceStorage.setMaxObjects(dataSource.getId(),
									maxObjects);
							dataSourceAdapter.notifyDataSetChanged();
						}
					}
				});
		return alertDialog.create();
	}
}