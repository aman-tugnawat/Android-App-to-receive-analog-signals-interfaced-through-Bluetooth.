package com.at.signalcloud;
/**
 * Created by Aman Tugnawat on 13-06-2013.
 */

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class MainActivity extends ListActivity {
	private static final int ACTIVITY_CREATE = 0;
	private static final int ACTIVITY_EDIT = 1;

	private SqlAdapter mDbHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		mDbHelper = new SqlAdapter(this);
		mDbHelper.open();
		fillData();
		registerForContextMenu(getListView());
	}

	private void fillData() {
		Cursor userCursor = mDbHelper.fetchAllUsers();
		startManagingCursor(userCursor);

		// Create an array to specify the fields we want to display in the list
		// (only TITLE)
		String[] from = new String[] { SqlAdapter.KEY_TITLE };

		// and an array of the fields we want to bind those fields to (in this
		// case just text1)
		int[] to = new int[] { R.id.text1 };

		// Now create a simple cursor adapter and set it to display
		SimpleCursorAdapter users = new SimpleCursorAdapter(this, R.layout.row,
				userCursor, from, to);
		setListAdapter(users);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		MenuInflater mi = getMenuInflater();
		mi.inflate(R.menu.list_menu, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_insert:
			createUser();
			return true;
		}

		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater mi = getMenuInflater();
		mi.inflate(R.menu.list_menu_longpress, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		/*
		 * This line of code utilizes the getMenuInfo() method of the item that
		 * was clicked to obtain an instance of AdapterContextMenuInfo. This
		 * class exposes various bits of information about the menu item and
		 * item that was long-pressed in the list view.
		 */
		switch (item.getItemId()) {
		case R.id.menu_delete:
			mDbHelper.deleteUser(info.id);
			fillData();
			break;

		case R.id.menu_record_new_signal:
			Intent openBT = new Intent(this, BluetoothActivity.class);
			openBT.putExtra(SqlAdapter.KEY_ROWID,info.id);
			startActivity(openBT);
			break;
			
		case R.id.menu_update_user_info:
			Intent update = new Intent(this, UpdateUserInfo.class);
			update.putExtra(SqlAdapter.KEY_ROWID,info.id);
			startActivityForResult(update, ACTIVITY_EDIT);
			return true;
		}
		return super.onContextItemSelected(item);
	}

	private void createUser() {
		Intent i = new Intent(this, UpdateUserInfo.class);
		startActivityForResult(i, ACTIVITY_CREATE);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Intent openBT = new Intent(this, BluetoothActivity.class);
		openBT.putExtra(SqlAdapter.KEY_ROWID, id);
		startActivity(openBT);
		
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		fillData();
	}
}
