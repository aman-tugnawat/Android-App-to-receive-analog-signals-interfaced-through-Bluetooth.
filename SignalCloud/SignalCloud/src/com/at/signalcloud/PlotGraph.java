package com.at.signalcloud;
/**
 * Created by Aman Tugnawat on 13-06-2013.
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.SeriesSelection;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class PlotGraph extends Activity {
	/** The main dataset that includes all the series that go into a chart. */
	private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
	/** The main renderer that includes all the renderers customizing a chart. */
	private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
	/** The most recently added series. */
	private XYSeries mCurrentSeries;
	/** The most recently created renderer, customizing the current series. */
	private XYSeriesRenderer mCurrentRenderer;
	/** Button for creating a new series of data. */
	// private Button mNewSeries;
	/** Button for creating a new file plot of data. */
	private Button mNewFilePlot;
	/** Button for adding entered data to the current series. */
	private Button mAdd;
	/** Edit text field for entering the X value of the data to be added. */
	private EditText mX;
	/** Edit text field for entering the Y value of the data to be added. */
	private EditText mY;
	/** The chart view that displays the data. */
	private GraphicalView mChartView;

	// File Path
	String mFilePath;
	// Random
	Random rand = new Random();
	// Buffered File Reader
	private BufferedReader br;
	// debug text
	TextView tv1;
	//
	boolean plot_running = false;

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		// save the current data, for instance when changing screen orientation
		outState.putSerializable("dataset", mDataset);
		outState.putSerializable("renderer", mRenderer);
		outState.putSerializable("current_series", mCurrentSeries);
		outState.putSerializable("current_renderer", mCurrentRenderer);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedState) {
		super.onRestoreInstanceState(savedState);
		// restore the current data, for instance when changing the screen
		// orientation
		mDataset = (XYMultipleSeriesDataset) savedState
				.getSerializable("dataset");
		mRenderer = (XYMultipleSeriesRenderer) savedState
				.getSerializable("renderer");
		mCurrentSeries = (XYSeries) savedState
				.getSerializable("current_series");
		mCurrentRenderer = (XYSeriesRenderer) savedState
				.getSerializable("current_renderer");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.plot_activity);
		mFilePath = getIntent().getExtras().getString("file_path");

		// the top part of the UI components for adding new data points
		mX = (EditText) findViewById(R.id.xValue);
		mY = (EditText) findViewById(R.id.yValue);
		mAdd = (Button) findViewById(R.id.add);

		tv1 = (TextView) findViewById(R.id.textView1);

		// set some properties on the main renderer
		mRenderer.setApplyBackgroundColor(true);
		mRenderer.setBackgroundColor(Color.WHITE);
		mRenderer.setAxisTitleTextSize(16);
		mRenderer.setChartTitleTextSize(20);
		mRenderer.setLabelsTextSize(15);
		mRenderer.setLegendTextSize(15);
		mRenderer.setMargins(new int[] { 20, 30, 15, 0 });
		mRenderer.setZoomButtonsVisible(true);
		mRenderer.setPointSize(5);
		mRenderer.setXAxisMin(-500);
		mRenderer.setXAxisMax(20000);
		mRenderer.setYAxisMin(-10);
		mRenderer.setYAxisMax(1100);

		
		// the button that handles the new series of data creation
		mNewFilePlot = (Button) findViewById(R.id.button1);
		mNewFilePlot.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (!plot_running) {
					String seriesTitle = "File Plot "
							+ (mDataset.getSeriesCount() + 1);
					// create a new series of data
					XYSeries series = new XYSeries(seriesTitle);
					mDataset.addSeries(series);
					mCurrentSeries = series;
					// create a new renderer for the new series
					XYSeriesRenderer renderer = new XYSeriesRenderer();
					mRenderer.addSeriesRenderer(renderer);
					// set some renderer properties
					renderer.setPointStyle(PointStyle.POINT);
					renderer.setFillPoints(true);
					renderer.setDisplayChartValues(true);
					renderer.setDisplayChartValuesDistance(10);
					// random Color
					// generate the random integers for r, g and b value
					int r = rand.nextInt(255);
					int g = rand.nextInt(255);
					int b = rand.nextInt(255);
					int randomColor = Color.rgb(r, g, b);
					renderer.setColor(randomColor);

					mCurrentRenderer = renderer;
					setSeriesWidgetsEnabled(true);
					mChartView.repaint();
					plotGraph();
				} else
					Toast.makeText(getApplicationContext(),
							"Another plot already running Please wait...",
							Toast.LENGTH_SHORT).show();
			}
		});

		mAdd.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				double x = 0;
				double y = 0;
				try {
					x = Double.parseDouble(mX.getText().toString());
				} catch (NumberFormatException e) {
					mX.requestFocus();
					return;
				}
				try {
					y = Double.parseDouble(mY.getText().toString());
				} catch (NumberFormatException e) {
					mY.requestFocus();
					return;
				}
				// add a new data point to the current series
				mCurrentSeries.add(x, y);
				mX.setText("");
				mY.setText("");
				mX.requestFocus();
				// repaint the chart such as the newly added point to be visible
				mChartView.repaint();
			}
		});
		//init_plot();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mChartView == null) {
			LinearLayout layout = (LinearLayout) findViewById(R.id.chart);
			mChartView = ChartFactory.getLineChartView(this, mDataset,
					mRenderer);
			// enable the chart click events
			mRenderer.setClickEnabled(true);
			mRenderer.setSelectableBuffer(10);
			mChartView.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					// handle the click event on the chart
					SeriesSelection seriesSelection = mChartView
							.getCurrentSeriesAndPoint();
					if (seriesSelection == null) {
						// Toast.makeText(MainActivity.this, "No chart element",
						// Toast.LENGTH_SHORT).show();
					} else {
						// display information of the clicked point
						Toast.makeText(
								PlotGraph.this,
								"Chart element in series index "
										+ seriesSelection.getSeriesIndex()
										+ " data point index "
										+ seriesSelection.getPointIndex()
										+ " was clicked"
										+ " closest point value X="
										+ seriesSelection.getXValue() + ", Y="
										+ seriesSelection.getValue(),
								Toast.LENGTH_SHORT).show();
					}
				}
			});
			layout.addView(mChartView, new LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
			boolean enabled = mDataset.getSeriesCount() > 0;
			setSeriesWidgetsEnabled(enabled);
		} else {
			mChartView.repaint();
		}
	}
	void init_plot(){
		if (!plot_running) {
			String seriesTitle = "File Plot "
					+ (mDataset.getSeriesCount() + 1);
			// create a new series of data
			XYSeries series = new XYSeries(seriesTitle);
			mDataset.addSeries(series);
			mCurrentSeries = series;
			// create a new renderer for the new series
			XYSeriesRenderer renderer = new XYSeriesRenderer();
			mRenderer.addSeriesRenderer(renderer);
			// set some renderer properties
			renderer.setPointStyle(PointStyle.POINT);
			renderer.setFillPoints(true);
			renderer.setDisplayChartValues(true);
			renderer.setDisplayChartValuesDistance(10);
			// random Color
			// generate the random integers for r, g and b value
			int r = rand.nextInt(255);
			int g = rand.nextInt(255);
			int b = rand.nextInt(255);
			int randomColor = Color.rgb(r, g, b);
			renderer.setColor(randomColor);

			mCurrentRenderer = renderer;
			setSeriesWidgetsEnabled(true);
			mChartView.repaint();
			plotGraph();
		} else
			Toast.makeText(getApplicationContext(),
					"Another plot already running Please wait...",
					Toast.LENGTH_SHORT).show();
	};

	/**
	 * Enable or disable the add data to series widgets
	 * 
	 * @param enabled
	 *            the enabled state
	 */
	private void setSeriesWidgetsEnabled(boolean enabled) {
		mX.setEnabled(enabled);
		mY.setEnabled(enabled);
		mAdd.setEnabled(enabled);
	}

	private void plotGraph() {

		// Find the directory for the SD Card using the API
		// *Don't* hardcode "/sdcard"
		// File sdcard = Environment.getExternalStorageDirectory();

		// Get the text file
		File file = new File(mFilePath);

		// Read text from file
		// StringBuilder text = new StringBuilder();
		Double[] X;
		Double[] Y;

		try {
			br = new BufferedReader(new FileReader(file));
			String line;
			int lengthOfVector = 0;

			while ((line = br.readLine()) != null) {
				Log.i("Y[" + Integer.toString(lengthOfVector) + "]", line);
				lengthOfVector++;
			}
			Log.i("lengthOfVector", Integer.toString(lengthOfVector));

			X = new Double[lengthOfVector];
			Y = new Double[lengthOfVector];
			br = new BufferedReader(new FileReader(file));
			line = null;
			for (int i = 0; (line = br.readLine()) != null; i++) {
				try {

					Y[i] = Double.parseDouble(line);
					X[i] = Double.parseDouble(Integer.toString(10 * i));
				} catch (NumberFormatException e) {
					e.printStackTrace();
					return;
				}
			}
			br.close();

			plotData(X, Y, lengthOfVector);
		} catch (IOException e) {
			// You'll need to add proper error handling here
			Toast.makeText(getBaseContext(), "Error " + e.getMessage(),
					Toast.LENGTH_LONG).show();
		}

	}

	public void plotData(final Double[] X, final Double[] Y, final int length) {
		final String TAG = "Data plot Asynctask";
		new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {
				Log.i(TAG, "Do in Background.");
				String msg = "";
				try {
					plot_running = true;
					Log.i(TAG, "Do in Background. Try...");
					for (int i = 0; i < length; i++) {
						// add a new data point to the current series
						mCurrentSeries.add(X[i], Y[i]);
						// repaint the chart such as the newly added point to be
						// visible
						mChartView.repaint();
						Thread.sleep(2);
					}

					Log.i(TAG, "Plotting Done...");
					msg = "success";
					return msg;

				} catch (Exception ex) {
					msg = "Error :" + ex.getMessage();
					// If there is an error, don't just keep trying to register.
					// Require the user to click a button again, or perform
					// exponential back-off.
					Log.i(TAG, msg);
				}
				return msg;
			}

			@Override
			protected void onPostExecute(String msg) {
				Log.i(TAG, "..do in background..post execute..");
				tv1.setText(msg);
				plot_running = false;
			}
		}.execute(null, null, null);
	}

}
