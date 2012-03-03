package com.pigmal.android.accessory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * class Arduino Accessory
 * Arduinoベースのアクセサリとの接続プロトコル依存部
 *
 */
public class ArduinoAccessory {
	static final String TAG = "ArduinoAccessory";
	
	/**
	 * アクセサリとの通信プロトコル
	 * [startbyte][cmd][size][data...]
	 */
	static final byte START_BYTE = 0x7f;
	static final byte CMD_DIGITAL_WRITE = 0x00;
	static final byte CMD_ANALOG_WRITE = 0x01;
	static final byte CMD_SERVO_WRITE = 0x02;
	static final byte UPDATE_DIGITAL_STATE = 0x40;
	static final byte UPDATE_ANALOG_STATE = 0x41;
	
	static final int HEADER_SIZE = 3;
	static final int DIGITA_WRITE_DATA_SIZE = 2;
	static final int ANALOG_WRITE_DATA_SIZE = 2;
	
	static final int MAX_BUF_SIZE = 1024;
	
	private OutputStream mOutputStream;
	private InputStream mInputStream;
	private Handler mHandler;
	private boolean mThreadRunning;
	
	public ArduinoAccessory(OutputStream os, InputStream is, Handler h) {
		mOutputStream = os;
		mInputStream = is;
		mHandler = h;
		
		if (is != null && h != null) {
			Thread thread = new Thread(mAccessoryReceiver, "AccessoryListen");
	        thread.start();
		}
	}
	
	public void requestStop() {
		mThreadRunning = false;
	}

	/**
	 * 
	 * @param id
	 * @param value
	 */
	void digitalWrite(int id, boolean value) {
		byte[] buffer = new byte[HEADER_SIZE + DIGITA_WRITE_DATA_SIZE];
		buffer[0] = START_BYTE;
		buffer[1] = CMD_DIGITAL_WRITE;
		buffer[2] = DIGITA_WRITE_DATA_SIZE;
		buffer[3] = (byte)id;
		if(value) {
			buffer[4]=(byte)1;
		} else {
			buffer[4]=(byte)0;
		}
		/**
		 * アクセサリに出力する
		 */
		if (mOutputStream != null) {
			try {
				mOutputStream.write(buffer);
			} catch (IOException e) {
				Log.e(TAG, "write failed", e);
			}
		}
	}
	
	void analogWrite(int id, int progress) {
		byte[] buffer = new byte[HEADER_SIZE + ANALOG_WRITE_DATA_SIZE];
		buffer[0] = START_BYTE;
		buffer[1] = CMD_ANALOG_WRITE;
		buffer[2] = ANALOG_WRITE_DATA_SIZE;
		buffer[3] = (byte)id;
		buffer[4] = (byte) progress;
		/**
		 * アクセサリに出力する
		 */
		if (mOutputStream != null) {
			try {
				mOutputStream.write(buffer);
			} catch (IOException e) {
				Log.e(TAG, "write failed", e);
			}
		}
	}

	/**
	 * Accessoryからメッセージを受けた時のコールバック
	 * プロトコルの解析を行い、処理をHandlerに渡す
	 * @param data
	 */
	public void onAccessoryMessage(byte[] data) {
		int i = 0;
		int length = data.length;
		
		if (mHandler == null) {
			return;
		}
		
		while (i < length && data[i] == START_BYTE) {
			int rest = length - i;
			Log.v(TAG, "receive:[" + data[1] +"][" + data[2] + "]");			
			
			int size = 0;
			switch (data[i + 1]) {
			case UPDATE_DIGITAL_STATE:
				size = data[i + 2];
				if (rest >= size + HEADER_SIZE) {
					byte[] d = new byte[size];
					System.arraycopy(data, i + HEADER_SIZE, d, 0, size);
					Message m = Message.obtain(mHandler, UPDATE_DIGITAL_STATE, d);
					mHandler.sendMessage(m);
					i += size;
					break;
				}
			case UPDATE_ANALOG_STATE:
				size = data[i + 2];
				if (rest >= size + HEADER_SIZE) {
					byte[] d = new byte[size];
					System.arraycopy(data, i + HEADER_SIZE, d, 0, size);
					Message m = Message.obtain(mHandler, UPDATE_ANALOG_STATE, d);
					mHandler.sendMessage(m);
					i += size;
					break;
				}
			default:
				i++;
				break;
			}
		}
	}
	
	/**
	 * アクセサリからのデータを待ち受けるスレッド
	 */
	private Runnable mAccessoryReceiver = new Runnable() {
		@Override
		public void run() {
			int length = 0;
			byte[] buffer = new byte[MAX_BUF_SIZE];
			byte[] data;

			mThreadRunning = true;
			while (length >= 0 && mThreadRunning) {
				try {
					length = mInputStream.read(buffer);
				} catch (IOException e) {
					break;
				}

				data = new byte[length];
				System.arraycopy(buffer, 0, data, 0, length);
				onAccessoryMessage(data);
			}
			mInputStream = null;
			mThreadRunning = false;
		}
	};
}
