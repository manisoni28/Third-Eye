package com.Appic.ThirdEye;

import android.app.*;
import android.content.*;
import android.media.*;
import android.net.Uri;
import android.os.*;
import android.speech.tts.*;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.telephony.*;
import android.view.*;
import android.widget.*;
import java.util.*;

/**
 * Created By Mani
 */
public class Main extends Activity implements TextToSpeech.OnInitListener
	{
	private static boolean speakOnResume = false;
	public static TextView messages;
	public static TextView calls;
	private TextView contacts;
	private TextView internet;
	private TextView settings;
	private TextView status;
	private boolean okToFinish = false;
	
    @Override protected void onCreate(Bundle savedInstanceState)
    	{
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.main);
		GlobalVars.lastActivity = Main.class;
		speakOnResume=false;
		messages = (TextView) findViewById(R.id.messages);
		calls = (TextView) findViewById(R.id.calls);
		contacts = (TextView) findViewById(R.id.contacts);
		internet = (TextView) findViewById(R.id.browser);
		settings = (TextView) findViewById(R.id.settings);
		status = (TextView) findViewById(R.id.status);
		GlobalVars.activityItemLocation=0;
		GlobalVars.activityItemLimit=6;
		
		GlobalVars.contextApp = getApplicationContext();
		
		GlobalVars.context = this;
		GlobalVars.startTTS(GlobalVars.tts);
		GlobalVars.tts = new TextToSpeech(this,this);
		GlobalVars.tts.setPitch((float) 1.0);
		
		//SETS PROFILE TO NORMAL
		AudioManager audioManager = (AudioManager)getSystemService(AUDIO_SERVICE);
		audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);

		
		//READ WEB BOOKMARKS DATABASE
		GlobalVars.readBookmarksDatabase();
		
		if (GlobalVars.deviceIsAPhone()==true)
			{
			messages.setText(GlobalVars.context.getResources().getString(R.string.mainMessages) + " (" + String.valueOf(GlobalVars.getMessagesUnreadCount()) + ")");
			}
			else
			{
			messages.setText(GlobalVars.context.getResources().getString(R.string.mainMessages) + " (0)");
			}
		
		if (GlobalVars.deviceIsAPhone()==true)
			{
			calls.setText(GlobalVars.context.getResources().getString(R.string.mainCalls) + " (" + String.valueOf(GlobalVars.getCallsMissedCount()) + ")");
			}
			else
			{
			calls.setText(GlobalVars.context.getResources().getString(R.string.mainCalls) + " (0)");
			}

		//GETS EVERY ALARM TONE
		try
			{
			RingtoneManager manager = new RingtoneManager(this);
			manager.setType(RingtoneManager.TYPE_ALARM);
			GlobalVars.cursor = manager.getCursor();
			while (GlobalVars.cursor.moveToNext())
				{
				GlobalVars.settingsToneAlarmTitle.add(GlobalVars.cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX));
				GlobalVars.settingsToneAlarmUri.add(GlobalVars.cursor.getString(RingtoneManager.URI_COLUMN_INDEX));
				GlobalVars.settingsToneAlarmID.add(GlobalVars.cursor.getString(RingtoneManager.ID_COLUMN_INDEX));
				}
			}
			catch(NullPointerException e)
			{
			}
			catch(Exception e)
			{
			}
			
		//GETS EVERY NOTIFICATION TONE
		try
			{
			RingtoneManager manager = new RingtoneManager(this);
			manager.setType(RingtoneManager.TYPE_NOTIFICATION);
			GlobalVars.cursor = manager.getCursor();
			while (GlobalVars.cursor.moveToNext())
				{
				GlobalVars.settingsToneNotificationTitle.add(GlobalVars.cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX));
				GlobalVars.settingsToneNotificationUri.add(GlobalVars.cursor.getString(RingtoneManager.URI_COLUMN_INDEX));
				GlobalVars.settingsToneNotificationID.add(GlobalVars.cursor.getString(RingtoneManager.ID_COLUMN_INDEX));
				}
			}
			catch(NullPointerException e)
			{
			}
			catch(Exception e)
			{
			}
		
		//GETS EVERY CALL TONE
		try
			{
			RingtoneManager manager = new RingtoneManager(this);
			manager.setType(RingtoneManager.TYPE_RINGTONE);
			GlobalVars.cursor = manager.getCursor();
			while (GlobalVars.cursor.moveToNext())
				{
			    GlobalVars.settingsToneCallTitle.add(GlobalVars.cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX));
			    GlobalVars.settingsToneCallUri.add(GlobalVars.cursor.getString(RingtoneManager.URI_COLUMN_INDEX));
			    GlobalVars.settingsToneCallID.add(GlobalVars.cursor.getString(RingtoneManager.ID_COLUMN_INDEX));
				}
			}
			catch(NullPointerException e)
			{
			}
			catch(Exception e)
			{
			}
			
		//GETS READING SPEED VALUE
		String readingSpeedString = GlobalVars.readFile("readingspeed.cfg");
		if (readingSpeedString=="")
			{
			GlobalVars.settingsTTSReadingSpeed = 1;
			GlobalVars.tts.setSpeechRate(GlobalVars.settingsTTSReadingSpeed);
			GlobalVars.writeFile("readingspeed.cfg",String.valueOf(GlobalVars.settingsTTSReadingSpeed));
			}
			else
			{
			try
				{
				GlobalVars.settingsTTSReadingSpeed = Integer.valueOf(readingSpeedString);
				GlobalVars.tts.setSpeechRate(GlobalVars.settingsTTSReadingSpeed);
				}
				catch(Exception e)
				{
				GlobalVars.settingsTTSReadingSpeed = 1;
				GlobalVars.tts.setSpeechRate(GlobalVars.settingsTTSReadingSpeed);
				GlobalVars.writeFile("readingspeed.cfg",String.valueOf(GlobalVars.settingsTTSReadingSpeed));
				}
			}
		
		//GETS INPUT MODE VALUE
		String inputModeString = GlobalVars.readFile("inputmode.cfg");
		if (inputModeString=="")
			{
			GlobalVars.inputMode = GlobalVars.INPUT_KEYBOARD;
			GlobalVars.writeFile("inputmode.cfg",String.valueOf(GlobalVars.INPUT_KEYBOARD));
			}
			else
			{
			try
				{
				GlobalVars.inputMode = Integer.valueOf(inputModeString);
				}
				catch(Exception e)
				{
				GlobalVars.inputMode = GlobalVars.INPUT_KEYBOARD;
				GlobalVars.writeFile("inputmode.cfg",String.valueOf(GlobalVars.INPUT_KEYBOARD));
				}
			}
		
		//GETS SCREEN TIMEOUT POSSIBLE VALUES
		int[] arr = getResources().getIntArray(R.array.screenTimeOutSeconds);
			for(int i=0;i<arr.length;i++)
			{
			GlobalVars.settingsScreenTimeOutValues.add(String.valueOf(arr[i]));
			}

		//GETS SCREEN TIMEOUT VALUE
		String screenTimeOutString = GlobalVars.readFile("screentimeout.cfg");
		if (screenTimeOutString=="")
			{
			GlobalVars.settingsScreenTimeOut = Integer.valueOf(GlobalVars.settingsScreenTimeOutValues.get(GlobalVars.settingsScreenTimeOutValues.size() -1));
			GlobalVars.writeFile("screentimeout.cfg",String.valueOf(GlobalVars.settingsScreenTimeOut));
			}
			else
			{
			try
				{
				GlobalVars.settingsScreenTimeOut = Integer.valueOf(screenTimeOutString);
				}
				catch(Exception e)
				{
				GlobalVars.settingsScreenTimeOut = Integer.valueOf(GlobalVars.settingsScreenTimeOutValues.get(GlobalVars.settingsScreenTimeOutValues.size() -1));
				GlobalVars.writeFile("screentimeout.cfg",String.valueOf(GlobalVars.settingsScreenTimeOut));
				}
			}
		
		//SETS BLUETOOTH VALUE STATE
		GlobalVars.bluetoothEnabled = GlobalVars.isBluetoothEnabled();
		
		Handler handler = new Handler();
		handler.postDelayed(new Runnable()
			{
			@Override public void run()
				{
				speakOnResume = true;
				}
			}, 2000);
		
		GlobalVars.startService(BlindCommunicatorService.class);
    	}
		
    @Override protected void onDestroy()
    	{
		shutdownEverything();
    	super.onDestroy();
    	}
		
	@Override public void onResume()
		{
		super.onResume();
		GlobalVars.lastActivity = Main.class;
		GlobalVars.activityItemLocation=0;
		GlobalVars.activityItemLimit=6;
		GlobalVars.selectTextView(messages,false);
		GlobalVars.selectTextView(calls,false);
		GlobalVars.selectTextView(contacts,false);
		GlobalVars.selectTextView(internet,false);
		GlobalVars.selectTextView(settings,false);
		GlobalVars.selectTextView(status,false);
		
		//UPDATE ALARM COUNTER
	//	GlobalVars.setText(alarms,false, getResources().getString(R.string.mainAlarms) + " (" + GlobalVars.getPendingAlarmsForTodayCount() + ")");
		
		if (GlobalVars.deviceIsAPhone()==true)
			{
			messages.setText(GlobalVars.context.getResources().getString(R.string.mainMessages) + " (" + String.valueOf(GlobalVars.getMessagesUnreadCount()) + ")");
			}
		if (speakOnResume==true)
			{
			GlobalVars.talk(getResources().getString(R.string.layoutMainOnResume));
			}
		
		if (GlobalVars.deviceIsAPhone()==true)
			{
			calls.setText(GlobalVars.context.getResources().getString(R.string.mainCalls) + " (" + String.valueOf(GlobalVars.getCallsMissedCount()) + ")");
			}
			else
			{
			calls.setText(GlobalVars.context.getResources().getString(R.string.mainCalls) + " (0)");
			}
		}
    
    @Override
	public void onInit(int status)
    	{
		if (status == TextToSpeech.SUCCESS)
			{
			GlobalVars.talk(getResources().getString(R.string.mainWelcome));
			GlobalVars.tts.setOnUtteranceCompletedListener(new OnUtteranceCompletedListener()
				{
				@Override public void onUtteranceCompleted(String utteranceId)
					{
					try
   						{

   						}
						catch(NullPointerException e)
						{
						}
						catch(Exception e)
						{
						}
					}
				});
			}
			else
			{
			new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.mainNoTTSInstalledTitle)).setMessage(getResources().getString(R.string.mainNoTTSInstalledMessage)).setPositiveButton(getResources().getString(R.string.mainNoTTSInstalledButton),new DialogInterface.OnClickListener()
				{
				@Override
				public void onClick(DialogInterface dialog,int which)
					{
					try
						{
					    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=id=com.google.android.tts")));
						}
						catch (ActivityNotFoundException e)
						{
						try
							{
						    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.tts")));
							}
							catch (ActivityNotFoundException e2)
							{
							}
						}
					}
				}).show();
			}
		}

	public void shutdownEverything()
		{
		try
			{
			GlobalVars.cursor.close();
			}
			catch(NullPointerException e)
			{
			}
			catch(IllegalStateException e)
			{
			}
			catch(Exception e)
			{
			}
		try
			{
			GlobalVars.tts.shutdown();
			}
			catch(Exception e)
			{
			}

		try
			{
			GlobalVars.stopService(BlindCommunicatorService.class);
			}
			catch(Exception e)
			{
			}
		}
		
	public void select()
		{
		switch (GlobalVars.activityItemLocation)
			{
			case 1: //MESSAGES
			if (GlobalVars.deviceIsAPhone()==true)
				{
				int smsUnread = GlobalVars.getMessagesUnreadCount();
				if (smsUnread==0)
					{
					GlobalVars.talk(getResources().getString(R.string.mainMessagesNoNew));
					}
				else if (smsUnread==1)
					{
					GlobalVars.talk(getResources().getString(R.string.mainMessagesOneNew));
					}
				else
					{
					GlobalVars.talk(getResources().getString(R.string.mainMessages) + ". " + smsUnread + " " + getResources().getString(R.string.mainMessagesNew));
					}
				}
				else
				{
				GlobalVars.talk(getResources().getString(R.string.mainMessagesNotAvailable));
				}
			GlobalVars.selectTextView(messages,true);
			GlobalVars.selectTextView(calls,false);
			GlobalVars.selectTextView(status,false);
			break;
			
			case 2: //CALLS
			if (GlobalVars.deviceIsAPhone()==true)
				{
				int missedCalls = GlobalVars.getCallsMissedCount();
				if (missedCalls==0)
					{
					GlobalVars.talk(getResources().getString(R.string.mainCallsNoMissed));
					}
				else if (missedCalls==1)
					{
					GlobalVars.talk(getResources().getString(R.string.mainCallsOneMissed));
					}
				else
					{
					GlobalVars.talk(getResources().getString(R.string.mainCalls) + ". " + missedCalls + " " + getResources().getString(R.string.mainCallsMissed));
					}
				}
				else
				{
				GlobalVars.talk(getResources().getString(R.string.mainCallsNotAvailable));
				}
			GlobalVars.selectTextView(calls, true);
			GlobalVars.selectTextView(messages,false);
			GlobalVars.selectTextView(contacts,false);
			break;
			
			case 3: //CONTACTS
			GlobalVars.selectTextView(contacts,true);
			GlobalVars.selectTextView(calls,false);
			GlobalVars.selectTextView(internet,false);
			GlobalVars.talk(getResources().getString(R.string.mainContacts));
			break;

			case 4: //INTERNET
			GlobalVars.selectTextView(internet,true);
			GlobalVars.selectTextView(contacts,false);
			GlobalVars.selectTextView(settings,false);
			GlobalVars.talk(getResources().getString(R.string.mainBrowser));
			break;

			case 5: //SETTINGS
			GlobalVars.selectTextView(settings,true);
			GlobalVars.selectTextView(internet,false);
			GlobalVars.selectTextView(status,false);
			GlobalVars.talk(getResources().getString(R.string.mainSettings));
			break;
			
			case 6: //STATUS
			GlobalVars.selectTextView(status,true);
			GlobalVars.selectTextView(messages,false);
			GlobalVars.selectTextView(settings,false);
			GlobalVars.talk(getResources().getString(R.string.mainStatus));
			break;
			}
		}
		
	public void execute()
		{
		switch (GlobalVars.activityItemLocation)
			{
			case 1: //MESSAGES
			if (GlobalVars.deviceIsAPhone()==true)
				{
				GlobalVars.startActivity(Messages.class);
				}
				else
				{
				GlobalVars.talk(getResources().getString(R.string.mainNotAvailable));
				}
			break;
			
			case 2: //CALLS
			if (GlobalVars.deviceIsAPhone()==true)
				{
				GlobalVars.startActivity(Calls.class);
				}
				else
				{
				GlobalVars.talk(getResources().getString(R.string.mainNotAvailable));
				}
			break;
			
			case 3: //CONTACTS
			GlobalVars.startActivity(Contacts.class);
			break;

			case 4: //INTERNET
			GlobalVars.startActivity(Browser.class);
			break;

			case 5: //SETTINGS
			GlobalVars.startActivity(Settings.class);
			break;
			
			case 6: //STATUS
			GlobalVars.talk(getDeviceStatus());
			break;
			}
		}
		
	@Override public boolean onTouchEvent(MotionEvent event)
		{
		int result = GlobalVars.detectMovement(event);
		switch (result)
			{
			case GlobalVars.ACTION_SELECT:
			select();
			break;

			case GlobalVars.ACTION_EXECUTE:
			execute();
			break;
			}
		return super.onTouchEvent(event);
		}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event)
		{
		int result = GlobalVars.detectKeyUp(keyCode);
		switch (result)
			{
			case GlobalVars.ACTION_SELECT:
			select();
			break;

			case GlobalVars.ACTION_EXECUTE:
			execute();
			break;
			}
		
		if (keyCode== KeyEvent.KEYCODE_BACK)
			{
			if (GlobalVars.isBCTheDefaultLauncher()==false)
				{
				if (okToFinish==false)
					{
					okToFinish=true;
					Handler handler = new Handler();
					handler.postDelayed(new Runnable()
						{
						@Override public void run()
							{
							okToFinish = false;
						}
						}, 3000);
					GlobalVars.talk(getResources().getString(R.string.mainPressBack));
					return false;
					}
					else
					{
					shutdownEverything();
					this.finish();
					}
				}
				else
				{
				return false;
				}
			}
		return super.onKeyUp(keyCode, event);
		}

	private String getDeviceStatus()
		{
		String year = GlobalVars.getYear();
		String month = GlobalVars.getMonthName(Integer.valueOf(GlobalVars.getMonth()));
		String day = Integer.toString(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
		String dayname = GlobalVars.getDayName(Calendar.getInstance().get(Calendar.DAY_OF_WEEK));
		String hour = Integer.toString(Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
		String minutes = Integer.toString(Calendar.getInstance().get(Calendar.MINUTE));

		String textStatus = "";

		textStatus = textStatus +
					 getResources().getString(R.string.mainBatteryChargedAt) +
					 String.valueOf(batteryLevel() +
					 getResources().getString(R.string.mainPercentAndTime) +
					 hour + getResources().getString(R.string.mainHours) +
					 minutes + getResources().getString(R.string.mainMinutesAndDate) +
					 dayname + " " + day + getResources().getString(R.string.mainOf) +
					 month + getResources().getString(R.string.mainOf) + year);

		if (GlobalVars.batteryAt100==true)
			{
			textStatus = textStatus + getResources().getString(R.string.deviceChargedStatus);
			}
		else if (GlobalVars.batteryIsCharging==true)
			{
			textStatus = textStatus + getResources().getString(R.string.deviceChargingStatus);
			}

		if (GlobalVars.deviceIsAPhone()==true)
			{
			if (GlobalVars.deviceIsConnectedToMobileNetwork()==true)
				{
				textStatus = textStatus + getResources().getString(R.string.mainCarrierIs) + getCarrier();
				}
				else
				{
				textStatus = textStatus + getResources().getString(R.string.mainNoSignal);
				}
			}
		
		AudioManager audioManager = (AudioManager)getSystemService(AUDIO_SERVICE);
		switch(audioManager.getRingerMode())
			{
			case AudioManager.RINGER_MODE_NORMAL:
			textStatus = textStatus + getResources().getString(R.string.mainProfileIsNormal);
			break;

			case AudioManager.RINGER_MODE_SILENT:
			textStatus = textStatus + getResources().getString(R.string.mainProfileIsSilent);
			break;

			case AudioManager.RINGER_MODE_VIBRATE:
			textStatus = textStatus + getResources().getString(R.string.mainProfileIsVibrate);
			break;
			}
			
		if (GlobalVars.isWifiEnabled())
			{
			String name = GlobalVars.getWifiSSID();
			if (name=="")
				{
				textStatus = textStatus + getResources().getString(R.string.mainWifiOnWithoutNetwork);
				}
				else
				{
				textStatus = textStatus + getResources().getString(R.string.mainWifiOnWithNetwork) + name + ".";
				}
			}
			else
			{
			textStatus = textStatus + getResources().getString(R.string.mainWifiOff);
			}
		return textStatus;
		}
		
	private int batteryLevel()
		{
    	Intent batteryIntent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    	int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
    	int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
    	if(level == -1 || scale == -1)
			{
    		return (int)50.0f;
			}
    	return (int)(((float)level / (float)scale) * 100.0f); 
		}
		
	private String getCarrier()
		{
		try
			{
			TelephonyManager telephonyManager = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE));
			String carrier;
			carrier = telephonyManager.getSimOperatorName();
			if (carrier==null | carrier=="")
				{
				return getResources().getString(R.string.mainCarrierNotAvailable);
				}
				else
				{
				return carrier;
				}
			}
			catch(Exception e)
			{
			return getResources().getString(R.string.mainCarrierNotAvailable);
			}
		}
	}
