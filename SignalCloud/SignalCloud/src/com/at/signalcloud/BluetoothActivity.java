package com.at.signalcloud;
/**
 * Created by Aman Tugnawat on 13-06-2013.
 */
import java.io.File;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class BluetoothActivity extends Activity implements OnItemClickListener {

	public static final UUID MY_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");

	protected static final int SUCCESS_CONNECT = 0;
	protected static final int MESSAGE_READ = 1;

	ArrayAdapter<String> listAdapter;

	public boolean runThread; // This is very important variable, Thread starts
								// and stops receiving data, according to value
								// of this thread.

	public boolean writeToFile; // If writeToFile == "True" , input data will be
								// write to file
								// If writeToFile == "False", input data will be
								// not write to file

	BluetoothSocket write_Socket; // This socket will be used to write any
									// message(or send data)using bluetooth
									// channel

	ListView listView;
	BluetoothAdapter btAdapter;
	Set<BluetoothDevice> devicesArray; // A Set is a data structure which does
										// not allow duplicate elements.
	ArrayList<String> pairedDevices;
	ArrayList<BluetoothDevice> devices;
	IntentFilter filter;
	BroadcastReceiver receiver;
	FileOutputStream fOut;
	
	String mac_add=null;

	
	private MyCountDownTimer countDownTimer; // MyCountDownTimer is the user
												// defined class in the program.
												// Defined in this code
	private long timeElapsed;
	private boolean timerHasStarted = false; // this boolean variable is used to
												// change text from START to
												// RESET
	private TextView text;
	private TextView timeElapsedView;

	private final long startTime = 12000; // Value for which countdowntimer to
											// run.
	private final long interval = 1 * 1000; // Value of the time intervals after
											// which TickOn is called, and do
											// the work. I this example
	// time is changed after every 2 seconds, "interval" is the deciding factor
	// for this

	public File sdcard;
	public File btFile;
	ConnectedThread connectedThread;

	CheckedTextView tv1, tv2;
	Button b1;

	String send_confirm = "A";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bluetooth_list);
		
		

		tv1 = (CheckedTextView) findViewById(R.id.connection);
		tv2 = (CheckedTextView) findViewById(R.id.confirm);
		b1 = (Button) findViewById(R.id.button1);
		

		// String mUserName
		// =mDbHelper.fetchUser(getIntent().getExtras().getLong(SqlAdapter.KEY_ROWID)).getString(1);
		sdcard = Environment.getExternalStorageDirectory();
		btFile = new File(sdcard, "Data_set.txt");
		btFile.delete();
		btFile = new File(sdcard, "Data_set.txt");


		text = (TextView) this.findViewById(R.id.timer);
		timeElapsedView = (TextView) this.findViewById(R.id.timeElapsed);
		countDownTimer = new MyCountDownTimer(startTime, interval);
		text.setText(text.getText() + String.valueOf(startTime));


		b1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				connectedThread.write(send_confirm.getBytes());

			}
		});
		

		init();
		if (btAdapter == null) {
			Toast.makeText(getApplicationContext(), "No Bluetooth Detected",
					Toast.LENGTH_LONG).show();
			finish();
		} else {
			if (!(btAdapter.isEnabled())) {
				turnOnBt();
			}
		}

		runThread = true;
		writeToFile = false;

		getPairedDevices();
		startDiscovery();

	}

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {

			case SUCCESS_CONNECT:
				String s = "Successfully Connected";

				Toast.makeText(getApplicationContext(),
						"Connected Successfully", Toast.LENGTH_SHORT).show();
				connectedThread = new ConnectedThread((BluetoothSocket) msg.obj);

				write_Socket = (BluetoothSocket) msg.obj;
				
				tv1.setText("Connected to Device.");
				tv1.setChecked(true);
				// connectedThread.write(ecg_uc_send.getBytes());
				// timerHasStarted = true;
				// countDownTimer.start();
				// writeToFile = true;
				b1.setEnabled(true);

				connectedThread.write(s.getBytes());
				connectedThread.start(); // this will start receiving Data i.e.
											// it will start the receiving
											// thread.(because receiving thread
											// contains run() ).
				break;

			case MESSAGE_READ:
				try {
					byte[] readBuf = (byte[]) msg.obj;
					char b = (char) readBuf[0];

					if (b == 'H' && timerHasStarted) {
						// connectedThread.write(ecg_uc_send.getBytes());
						// timerHasStarted = true;
						// countDownTimer.start();
						// writeToFile = true;
						tv1.setText("Recording Signal...");

					}

					else if (b == 'H' && !(timerHasStarted)) {
						//tv1.setText("Connected to Device.");
						//tv1.setChecked(true);
						// connectedThread.write(ecg_uc_send.getBytes());
						// timerHasStarted = true;
						// countDownTimer.start();
						// writeToFile = true;
						//b2.setEnabled(true);
						//b1.setEnabled(true);

					}

					else if (b == 'B' && !(timerHasStarted))

					{tv1.setText("Recording Signal...");
						timerHasStarted = true;
						countDownTimer.start();
						writeToFile = true;
					}

					else if (writeToFile) {

						fOut = new FileOutputStream(btFile, true);
						fOut.write(readBuf);
						// String string = new String(readBuf);
						// Toast.makeText(getApplicationContext(), string,
						// Toast.LENGTH_SHORT).show();
						fOut.close();

					}

					else {
						// Toast.makeText(getApplicationContext(), "send 'H' ",
						// Toast.LENGTH_SHORT).show();
					}

					break;
				} catch (FileNotFoundException e) {
					Toast.makeText(getBaseContext(), e.getMessage(),
							Toast.LENGTH_LONG).show();
				} catch (Exception e) {
					Toast.makeText(getBaseContext(), e.getMessage(),
							Toast.LENGTH_LONG).show();
				}

			}
		}

	};

	private void startDiscovery() {
		btAdapter.cancelDiscovery();
		btAdapter.startDiscovery();
	}

	private void turnOnBt() {
		Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		startActivityForResult(intent, 1);
		Toast.makeText(getApplicationContext(),
				"You need to Enable Bluetooth...Select 'Allow' when Prompted",
				Toast.LENGTH_LONG).show();

	}

	private void getPairedDevices() {
		devicesArray = btAdapter.getBondedDevices();
		if (devicesArray.size() > 0) {
			for (BluetoothDevice device : devicesArray) {
				pairedDevices.add(device.getName());
			}
		}

	}

	private void init() {
		try {
			btFile.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		listView = (ListView) findViewById(R.id.listView1);
		listView.setOnItemClickListener(this);
		listAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, 0);
		listView.setAdapter(listAdapter);
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		pairedDevices = new ArrayList<String>();
		devices = new ArrayList<BluetoothDevice>();
		filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();

				if (BluetoothDevice.ACTION_FOUND.equals(action)) {
					BluetoothDevice device = intent
							.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					devices.add(device);

					String s = "";
					for (int a = 0; a < pairedDevices.size(); a++) {
						if (device.getName().equals(pairedDevices.get(a))) {
							s = "(Paired)";
							break;
						}
					}
					listAdapter.add(device.getName() + " " + s + " " + "\n"
							+ device.getAddress());
				} else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED
						.equals(action)) {

				} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
						.equals(action)) {

				} else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
					if (btAdapter.getState() == BluetoothAdapter.STATE_OFF) {
						turnOnBt();
					}
				}

			}

		};
		registerReceiver(receiver, filter);
		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		registerReceiver(receiver, filter);
		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		registerReceiver(receiver, filter);
		filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
		registerReceiver(receiver, filter);
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (btAdapter != null) {
			unregisterReceiver(receiver);
		}
		if (connectedThread != null)
		connectedThread.cancel();
	}

	protected void onResume() {
		super.onResume();
		filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(receiver, filter);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_CANCELED) {
			Toast.makeText(getApplicationContext(),
					"Bluetooth Must be ENABLED to continue", Toast.LENGTH_SHORT)
					.show();
			finish();
		}else {
			getPairedDevices();
			startDiscovery();
			}
	}


	public class MyCountDownTimer extends CountDownTimer {

		public MyCountDownTimer(long startTime, long interval) {
			super(startTime, interval);
		}

		@Override
		public void onFinish() {
			text.setText("Time's up!");
			timeElapsedView.setText("Time Elapsed: "
					+ String.valueOf(startTime));

			// countDownTimer.cancel();
			timerHasStarted = false;
			//tv2.setText("Signal Recorded");
			plotgraph();
		}

		private void plotgraph() {
			String file_path;

			runThread = false;
			writeToFile = false;

			file_path = btFile.getAbsolutePath();// this will display the
													// size of file. For testing
													// purpose.
			Toast.makeText(getApplicationContext(), "Signal has been Successfully saved to a File...Plot Activity Starting",
					Toast.LENGTH_SHORT).show();
			connectedThread.cancel();

			Intent plot = new Intent(BluetoothActivity.this, PlotGraph.class);

			plot.putExtra("file_path", file_path);

			startActivity(plot);
			finish();
		}

		@Override
		public void onTick(long millisUntilFinished) {
			text.setText("Remaining Time(in ms): " + millisUntilFinished); // This
																			// shows
																			// the
																			// remaining
																			// time
			timeElapsed = startTime - millisUntilFinished; // This shows the
															// time elapsed
															// after start.
			timeElapsedView.setText("Time Elapsed: "
					+ String.valueOf(timeElapsed));
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		if (btAdapter.isDiscovering()) {
			btAdapter.cancelDiscovery();
		}

		if (listAdapter.getItem(arg2).contains("Paired")) {
			mac_add= devices.get(arg2).toString();
			BluetoothDevice selectedDevice = devices.get(arg2);
			ConnectThread connect = new ConnectThread(selectedDevice);
			connect.start();
		}

		else {
			Toast.makeText(getApplicationContext(), "device is not paired",
					Toast.LENGTH_SHORT).show();
		}
	}

	private class ConnectThread extends Thread {

		private final BluetoothSocket mmSocket;

		private final BluetoothDevice mmDevice;

		public ConnectThread(BluetoothDevice device) {
			// Use a temporary object that is later assigned to mmSocket,
			// because mmSocket is final
			BluetoothSocket tmp = null;
			mmDevice = device;

			// Get a BluetoothSocket to connect with the given BluetoothDevice
			try {
				tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
			} catch (IOException e) {
			}

			mmSocket = tmp;
		}

		public void run() {
			// Cancel discovery because it will slow down the connection
			btAdapter.cancelDiscovery();

			try {
				// Connect the device through the socket. This will block
				// until it succeeds or throws an exception
				mmSocket.connect();
			} catch (IOException connectException) {
				// Unable to connect; close the socket and get out
				try {
					mmSocket.close();
				} catch (IOException closeException) {
				}
				return;
			}

			// Do work to manage the connection (in a separate thread)

			mHandler.obtainMessage(SUCCESS_CONNECT, mmSocket).sendToTarget();
		}

		/** Will cancel an in-progress connection, and close the socket */
		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
			}
		}
	}

	private class ConnectedThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final InputStream mmInStream;
		private final OutputStream mmOutStream;

		public ConnectedThread(BluetoothSocket socket) {
			mmSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			// Get the input and output streams, using temp objects because
			// member streams are final
			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
			}

			mmInStream = tmpIn;
			mmOutStream = tmpOut;
		}

		public void run() {
			byte[] buffer; // buffer store for the stream
			// int bytes; // bytes returned from read()

			// Keep listening to the InputStream until runThread = true
			while (runThread) {
				buffer = new byte[1]; // Size of array is set to 1 to save
										// memory and plot graph smoothly, as
										// stated above
				// Read from the InputStream

				try {

					mmInStream.read(buffer);
				} catch (IOException e) {
					e.printStackTrace();
				}

				if (!(buffer == null)) {
					// Send the obtained bytes to the UI activity
					mHandler.obtainMessage(MESSAGE_READ, buffer).sendToTarget();

					buffer = null;
				}
			}
		}

		/* Call this from the main activity to send data to the remote device */
		public void write(byte[] bytes) {
			try {
				mmOutStream.write(bytes);
			} catch (IOException e) {
			}
		}

		/* Call this from the main activity to shutdown the connection */
		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
			}
		}
	}
}
