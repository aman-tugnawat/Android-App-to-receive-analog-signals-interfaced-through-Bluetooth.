package com.at.signalcloud;
/**
 * Created by Aman Tugnawat on 13-06-2013.
 */

import java.text.SimpleDateFormat;
import java.util.Calendar;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TimePicker;
import android.widget.Toast;

public class UpdateUserInfo extends Activity {
	private static final int DATE_PICKER_DIALOG = 0;
	private static final int TIME_PICKER_DIALOG = 1;

	//
	// Date Format
	//

	private String mSex = null;

	private static final String DATE_FORMAT = "yyyy-MM-dd";
	private static final String TIME_FORMAT = "kk:mm";
	public static final String DATE_TIME_FORMAT = "yyyy-MM-dd kk:mm:ss";

	private EditText mTitleText;
	private EditText mBodyText;
	//private Button mDateButton;
	//private Button mTimeButton;
	private Button mConfirmButton;
	private Long mRowId;
	private SqlAdapter mDbHelper;
	private Calendar mCalendar;
	private EditText mAge;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mDbHelper = new SqlAdapter(this);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_info);

		mCalendar = Calendar.getInstance();
		mTitleText = (EditText) findViewById(R.id.name);
		mBodyText = (EditText) findViewById(R.id.body);
		mAge = (EditText) findViewById(R.id.age);
		//mDateButton = (Button) findViewById(R.id.ECG_date);
		//mTimeButton = (Button) findViewById(R.id.ECG_time);

		mConfirmButton = (Button) findViewById(R.id.confirm);

		mRowId = savedInstanceState != null ? savedInstanceState
				.getLong(SqlAdapter.KEY_ROWID) : null;
		mSex = null;

		registerButtonListenersAndSetDefaultText();
	}

	private void setRowIdFromIntent() {
		if (mRowId == null) {
			Bundle extras = getIntent().getExtras();
			mRowId = extras != null ? extras.getLong(SqlAdapter.KEY_ROWID)
					: null;
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		mDbHelper.close();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mDbHelper.open();
		setRowIdFromIntent();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DATE_PICKER_DIALOG:
			return showDatePicker();
		case TIME_PICKER_DIALOG:
			return showTimePicker();
		}
		return super.onCreateDialog(id);
	}

	private DatePickerDialog showDatePicker() {

		DatePickerDialog datePicker = new DatePickerDialog(UpdateUserInfo.this,
				new DatePickerDialog.OnDateSetListener() {

					@Override
					public void onDateSet(DatePicker view, int year,
							int monthOfYear, int dayOfMonth) {
						mCalendar.set(Calendar.YEAR, year);
						mCalendar.set(Calendar.MONTH, monthOfYear);
						mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
						updateDateButtonText();
					}
				}, mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH),
				mCalendar.get(Calendar.DAY_OF_MONTH));
		return datePicker;
	}

	private TimePickerDialog showTimePicker() {

		TimePickerDialog timePicker = new TimePickerDialog(this,
				new TimePickerDialog.OnTimeSetListener() {

					@Override
					public void onTimeSet(TimePicker view, int hourOfDay,
							int minute) {
						mCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
						mCalendar.set(Calendar.MINUTE, minute);
						updateTimeButtonText();
					}
				}, mCalendar.get(Calendar.HOUR_OF_DAY),
				mCalendar.get(Calendar.MINUTE), true);

		return timePicker;
	}

	private void registerButtonListenersAndSetDefaultText() {

		/*mDateButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				showDialog(DATE_PICKER_DIALOG);
			}
		});*/

		/*mTimeButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				showDialog(TIME_PICKER_DIALOG);
			}
		});*/

		mConfirmButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				saveState();
				setResult(RESULT_OK);
				Toast.makeText(UpdateUserInfo.this,
						getString(R.string.user_saved_message),
						Toast.LENGTH_SHORT).show();
				finish();
			}

		});

		updateDateButtonText();
		updateTimeButtonText();
	}

	private void updateTimeButtonText() {
		// Set the time button text based upon the value from the database
		SimpleDateFormat timeFormat = new SimpleDateFormat(TIME_FORMAT);
		String timeForButton = timeFormat.format(mCalendar.getTime());
		//mTimeButton.setText(timeForButton);
	}

	private void updateDateButtonText() {
		// Set the date button text based upon the value from the database
		SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
		String dateForButton = dateFormat.format(mCalendar.getTime());
		//mDateButton.setText(dateForButton);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong(SqlAdapter.KEY_ROWID, mRowId);
	}

	/* Radio button listener */
	public void onRadioButtonClicked(View view) {
		// Is the button now checked?
		boolean checked = ((RadioButton) view).isChecked();

		// Check which radio button was clicked
		switch (view.getId()) {
		case R.id.male:
			if (checked)
				// Sex is male
				mSex = "Male";
			break;
		case R.id.female:
			if (checked)
				// Sex is female
				mSex = "Female";
			break;
		}
	}

	/* Save state */

	private void saveState() {
		String title = mTitleText.getText().toString();
		String body = mBodyText.getText().toString();
		String age = mAge.getText().toString();
		String sex = mSex;

		SimpleDateFormat dateTimeFormat = new SimpleDateFormat(DATE_TIME_FORMAT);
		String reminderDateTime = dateTimeFormat.format(mCalendar.getTime());

		if (mRowId == null) {

			long id = mDbHelper.createUser(title, sex, age, body,
					reminderDateTime);
			if (id > 0) {
				mRowId = id;
			}
		} else {
			mDbHelper.updateUser(mRowId, title, sex, age, body,
					reminderDateTime);
		}
	}

}
