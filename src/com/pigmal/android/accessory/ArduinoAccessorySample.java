package com.pigmal.android.accessory;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;

public class ArduinoAccessorySample extends OpenAccessoryBaseActivity {
	private static final String TAG = "OpenAccessoryBaseActivity";
	
	private ToggleButton[] mButtons;
	private SeekBar[] mSeekBars;
	private TextView[] mSwitchStatuses;
	private TextView[] mAnalogInValues;
	 
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
 
		setContentView(R.layout.main);
		/**
		 * ディジタル出力
		 */
		mButtons = new ToggleButton[4];
		mButtons[0] = (ToggleButton) findViewById(R.id.digitalOut1);
		mButtons[1] = (ToggleButton) findViewById(R.id.digitalOut2);
		mButtons[2] = (ToggleButton) findViewById(R.id.digitalOut3);
		mButtons[3] = (ToggleButton) findViewById(R.id.digitalOut4);
		for (int i = 0; i < mButtons.length; i++) {
			mButtons[i].setOnCheckedChangeListener(checkedListener);
		}
		
		/**
		 * アナログ出力
		 */
		mSeekBars = new SeekBar[3];
		mSeekBars[0] = (SeekBar)findViewById(R.id.analogOut1);
		mSeekBars[1] = (SeekBar)findViewById(R.id.analogOut2);
		mSeekBars[2] = (SeekBar)findViewById(R.id.analogOut3);
		for (int i = 0; i < mSeekBars.length; i++) {
			mSeekBars[i].setMax(255);
			mSeekBars[i].setProgress(0);
			mSeekBars[i].setOnSeekBarChangeListener(seekBarListener);
		}
		
		/**
		 * ディジタル入力表示用View
		 */
		mSwitchStatuses = new TextView[4];
		mSwitchStatuses[0] = (TextView)findViewById(R.id.digitalIn1);
		mSwitchStatuses[1] = (TextView)findViewById(R.id.digitalIn2);
		mSwitchStatuses[2] = (TextView)findViewById(R.id.digitalIn3);
		mSwitchStatuses[3] = (TextView)findViewById(R.id.digitalIn4);
		
		/**
		 * アナログ入力表示用View
		 */
		mAnalogInValues = new TextView[4];
		mAnalogInValues[0] = (TextView)findViewById(R.id.analogIn1);
		mAnalogInValues[1] = (TextView)findViewById(R.id.analogIn2);
		mAnalogInValues[2] = (TextView)findViewById(R.id.analogIn3);
		mAnalogInValues[3] = (TextView)findViewById(R.id.analogIn4);
		
		/**
		 * メッセージハンドラ
		 * UIスレッドで処理を行う
		 */
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				byte[] data = (byte[])msg.obj;
				switch (msg.what) {
				case ArduinoAccessory.UPDATE_DIGITAL_STATE:
					if (data[1] == 1) {
						mSwitchStatuses[data[0]].setBackgroundColor(Color.RED);
					} else {
						mSwitchStatuses[data[0]].setBackgroundColor(Color.BLACK);
					}
					break;
				case ArduinoAccessory.UPDATE_ANALOG_STATE:
					final int value = composeInt(data[1], data[2]);
					Log.v(TAG, "Analog id, value = " + data[0] + ", " + value);
					mAnalogInValues[data[0]].setText(String.valueOf(value));
					break;
				default:
					break;
				}
			}
		};
	}

	@Override
	public void onResume() {
		super.onResume();
	}
 
	@Override
	public void onPause() {
		super.onPause();
	}
 
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	private int composeInt(byte hi, byte lo) {
		return ((hi & 0xff) << 8) + (lo & 0xff);
	}
	
	/**
	 * Digital Out
	 */
	private OnCheckedChangeListener checkedListener = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if (mArduinoAccessory == null) {
				return ;
			}
			switch (buttonView.getId()) {
			case R.id.digitalOut1:
				mArduinoAccessory.digitalWrite(0, isChecked);
				break;
			case R.id.digitalOut2:
				mArduinoAccessory.digitalWrite(1, isChecked);
				break;
			case R.id.digitalOut3:
				mArduinoAccessory.digitalWrite(2, isChecked);
				break;
			case R.id.digitalOut4:
				mArduinoAccessory.digitalWrite(3, isChecked);
				break;
			default:
				break;
			}
		}
	};
	
	private OnSeekBarChangeListener seekBarListener = new OnSeekBarChangeListener() {
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			if (mArduinoAccessory == null) {
				return ;
			}
			
			switch (seekBar.getId()) {
			case R.id.analogOut1:
				mArduinoAccessory.analogWrite(0, progress);
				break;
			case R.id.analogOut2:
				mArduinoAccessory.analogWrite(1, progress);
				break;
			case R.id.analogOut3:
				mArduinoAccessory.analogWrite(2, progress);
				break;
			default:
				break;
			}
			
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// do nothing
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// do nothing
		}
	};
}
