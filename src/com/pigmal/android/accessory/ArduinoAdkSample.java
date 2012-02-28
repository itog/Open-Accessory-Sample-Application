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

public class ArduinoAdkSample extends OpenAccessoryBaseActivity {
	private static final String TAG = "HelloArduinoADK";
	
	private ToggleButton mButton;
	private SeekBar mSeekBar;
	private TextView mSwitchStatus;
	private TextView mAnalogValueText;
	 
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
 
		setContentView(R.layout.arduino_adk_base);
		/**
		 * ディジタル出力
		 */
		mButton = (ToggleButton) findViewById(R.id.ledToggleButton);
		mButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mArduinoAccessory.digitalWrite(0, isChecked);
			}
		});
		
		/**
		 * アナログ出力
		 */
		mSeekBar = (SeekBar)findViewById(R.id.seekBar1);
		mSeekBar.setMax(255);
		mSeekBar.setProgress(0);
		mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				mArduinoAccessory.analogWrite(0, progress);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// do nothing
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// do nothing
			}
		});
		
		/**
		 * ディジタル入力表示用View
		 */
		mSwitchStatus = (TextView)findViewById(R.id.statusTextView);
		
		/**
		 * アナログ入力表示用View
		 */
		mAnalogValueText = (TextView)findViewById(R.id.analogValueText);
		
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
					switch (data[0]) {
					case 0:
						if (data[1] == 1) {
							mSwitchStatus.setBackgroundColor(Color.RED);
						} else {
							mSwitchStatus.setBackgroundColor(Color.BLACK);
						}
						break;
					default:
						break;
					}
					break;
				case ArduinoAccessory.UPDATE_ANALOG_STATE:
					switch (data[0]) {
					case 0:
						final int value = composeInt(data[1], data[2]);
						Log.v(TAG, "Analog value = " + value + "(" + data[1] + "," + data[2] + ")");
						mAnalogValueText.setText(String.valueOf(value));
						break;
					default:
						break;
					}
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
}
