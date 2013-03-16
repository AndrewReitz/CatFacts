package co.nodeath.catfacts;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.telephony.SmsManager;

import com.WazaBe.HoloEverywhere.app.Activity;
import com.WazaBe.HoloEverywhere.app.Toast;

public class SmsSender {

	private static final Uri SMSURI = Uri.parse("content://sms");
	private final Context mContext;

	public SmsSender(Context context) {
		mContext = context;
	}

	public void sendSMSwithIntent(final String phoneNumber, final String message) {
		Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(String.format("smsto:%s", phoneNumber)));
		intent.putExtra("sms_body", message);
		mContext.startActivity(intent);
	}
	
	public void sendSMSwithGoogleVoice(final String message) {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.putExtra("android.intent.extra.SUBJECT", "Cat Fact");
		intent.putExtra("android.intent.extra.TEXT", message);
		mContext.startActivity(intent);
	}

	public void sendSMS(final String phoneNumber, final String message) {
		new SmsSenderAsyncTask(mContext).execute(new CatFactTextObject(message, phoneNumber));
	}

	public class CatFactTextObject {
		public String message;
		public String phoneNumber;
		
		public CatFactTextObject(final String message, final String phoneNumber) {
			this.message = message;
			this.phoneNumber = phoneNumber;
		}
	}

	class SmsSenderAsyncTask extends AsyncTask<CatFactTextObject, Void, Void> {

		static final String SENT = "SMS_SENT";
		static final String DELIVERED = "SMS_DELIVERED";
		final Context appContext;
		
		public SmsSenderAsyncTask(Context context) {
			appContext = context.getApplicationContext();
		}

		@Override
		protected Void doInBackground(CatFactTextObject... CatFactsTextObjects) {
			
			PendingIntent sentPI = PendingIntent.getBroadcast(appContext, 0, new Intent(SENT), 0);
			PendingIntent deliveredPI = PendingIntent.getBroadcast(appContext, 0, new Intent(DELIVERED), 0);

			appContext.registerReceiver(new BroadcastReceiver() {
				@Override
				public void onReceive(Context arg0, Intent arg1) {
					switch (getResultCode()) {
					case Activity.RESULT_OK:
						Toast.makeText(appContext, "SMS sent", Toast.LENGTH_SHORT).show();
						break;
					case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
						Toast.makeText(appContext, "Generic failure", Toast.LENGTH_SHORT).show();
						break;
					case SmsManager.RESULT_ERROR_NO_SERVICE:
						Toast.makeText(appContext, "No service", Toast.LENGTH_SHORT).show();
						break;
					case SmsManager.RESULT_ERROR_NULL_PDU:
						Toast.makeText(appContext, "Null PDU", Toast.LENGTH_SHORT).show();
						break;
					case SmsManager.RESULT_ERROR_RADIO_OFF:
						Toast.makeText(appContext, "Radio off", Toast.LENGTH_SHORT).show();
						break;
					}
				}
			}, new IntentFilter(SENT));

			appContext.registerReceiver(new BroadcastReceiver() {
				@Override
				public void onReceive(Context arg0, Intent arg1) {
					switch (getResultCode()) {
					case Activity.RESULT_OK:
						Toast.makeText(appContext, "SMS delivered", Toast.LENGTH_SHORT).show();
						break;
					case Activity.RESULT_CANCELED:
						Toast.makeText(appContext, "SMS not delivered", Toast.LENGTH_SHORT).show();
						break;
					}
				}
			}, new IntentFilter(DELIVERED));

			CatFactTextObject localCatFactTextObject = CatFactsTextObjects[0];
			SmsManager sms = SmsManager.getDefault();
			sms.sendTextMessage(localCatFactTextObject.phoneNumber, null, localCatFactTextObject.message, sentPI, deliveredPI);
			
			insertMessageToDatabase(localCatFactTextObject.phoneNumber, localCatFactTextObject.message);
			return null;
		}

		private void insertMessageToDatabase(String paramString1, String paramString2) {
			ContentValues localContentValues = new ContentValues();
			localContentValues.put("address", paramString1);
			localContentValues.put("body", paramString2);
			localContentValues.put("type", "2");
			appContext.getContentResolver().insert(SMSURI, localContentValues);
		}

	}
}
