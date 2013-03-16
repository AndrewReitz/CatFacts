package co.nodeath.catfacts;

import java.util.Calendar;
import java.util.Random;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.WazaBe.HoloEverywhere.app.Toast;
import com.actionbarsherlock.app.SherlockActivity;

public class ContactActivity extends SherlockActivity {
	private static final String ALPHABET = "1234567890qwertyuiop[]asdfghjkl;'zxcvbnm,./?><MNBVCXZ:LKJHGFDSA}{POIUYTREWQ+_)(*&^%$#@!~)";
	private static final String SHARED_PREFS_CHECKBOX_VIEWSMS = "checkboxviewsms";
	private Random mRandom;
	private String[] mCatFacts;

	private Context mContext;
	private Button mButtonRemove;
	private Button mButtonCatFact;
	private Button mButtonWelcome;
	private Button mButtonNotRecognized;
	private Button mButtonUnsubscribe;
	private TextView mTextViewName;
	private TextView mTextViewPhone;
	private TextView mTextViewExplanation;
	private CheckBox mCheckBoxViewSMS;
	// private CheckBox mCheckBoxAutoRespond;
	private String mName;
	private String mPhone;
	private SmsSender mSmsSender;

	// Button Click Events
	OnClickListener mButtonRemoveClickEvent = new OnClickListener() {
		public void onClick(View v) {
			CatFactsSQLiteHelper sqlHelper = new CatFactsSQLiteHelper(mContext);
			sqlHelper.removePerson(mName);
			finish();
		}
	};

	OnClickListener mButtonWelcomeClickEvent = new OnClickListener() {
		public void onClick(View v) {
			messageHandler(mPhone, getString(R.string.welcome_message));
		}
	};

	OnClickListener mButtonUnsubscribeClickEvent = new OnClickListener() {
		public void onClick(View v) {
			StringBuilder code = new StringBuilder();
			code.append(getString(R.string.unsubscribe_message));
			code.append(" ");
			for (int i = 0; i < 16; i++) {
				code.append(ALPHABET.charAt(mRandom.nextInt(ALPHABET.length())));
			}

			messageHandler(mPhone, code.toString());
		}
	};

	OnClickListener mButtonCatFactClickEvent = new OnClickListener() {
		public void onClick(View v) {
			messageHandler(mPhone, "Cat Facts: " + mCatFacts[mRandom.nextInt(mCatFacts.length)]);
		}
	};

	OnClickListener mButtonNotRecognizedClickEvent = new OnClickListener() {
		public void onClick(View v) {
			messageHandler(mPhone, getString(R.string.not_recognized_message));
		}
	};

	OnCheckedChangeListener mCheckBoxAutoReplyCheckEvent = new OnCheckedChangeListener() {
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if (isChecked) {
				new AsyncTask<Void, Void, Void>() {
					@Override
					protected Void doInBackground(Void... params) {
						CatFactsSQLiteHelper sqlHelper = new CatFactsSQLiteHelper(mContext);
						sqlHelper.autoRespond(mPhone);
						sqlHelper.close();
						return null;
					}
				};
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contactactivity);

		mRandom = new Random(Calendar.getInstance().getTimeInMillis());
		mCatFacts = getResources().getStringArray(R.array.cat_facts_array);
		mContext = this;

		mSmsSender = new SmsSender(this);

		mButtonRemove = (Button) findViewById(R.id.contact_button_remove);
		mButtonCatFact = (Button) findViewById(R.id.contact_button_catfact);
		mButtonWelcome = (Button) findViewById(R.id.contact_button_welcome);
		mButtonUnsubscribe = (Button) findViewById(R.id.contact_button_unsubscribe);
		mTextViewName = (TextView) findViewById(R.id.contact_textView_name);
		mTextViewPhone = (TextView) findViewById(R.id.contact_textView_phone);
		mTextViewExplanation = (TextView) findViewById(R.id.contact_textview_explaination);
		mCheckBoxViewSMS = (CheckBox) findViewById(R.id.contact_checkBox_viewmms);
		mButtonNotRecognized = (Button) findViewById(R.id.contact_button_not_recognized);
		// mCheckBoxAutoRespond = (CheckBox)
		// findViewById(R.id.contact_auto_respond_checkbox);

		// reload the state of the checkbox
		SharedPreferences settings = getSharedPreferences(CatFactsHelper.SHARED_PREFS, 0);
		mCheckBoxViewSMS.setChecked(settings.getBoolean(SHARED_PREFS_CHECKBOX_VIEWSMS, false));

		// set explanation text
		mTextViewExplanation.setText(String.format(getString(R.string.contact_explanation1), getString(R.string.welcome_message)) + String.format(getString(R.string.contact_explanation2), getString(R.string.unsubscribe_message)));

		// register click events
		mButtonRemove.setOnClickListener(mButtonRemoveClickEvent);
		mButtonWelcome.setOnClickListener(mButtonWelcomeClickEvent);
		mButtonUnsubscribe.setOnClickListener(mButtonUnsubscribeClickEvent);
		mButtonCatFact.setOnClickListener(mButtonCatFactClickEvent);
		mButtonNotRecognized.setOnClickListener(mButtonNotRecognizedClickEvent);

		// set check event
		// mCheckBoxAutoRespond.setOnCheckedChangeListener(mCheckBoxAutoReplyCheckEvent);

		// display name and phone number
		mName = this.getIntent().getExtras().getString("person");
		CatFactsSQLiteHelper sqlHelper = new CatFactsSQLiteHelper(this);
		mPhone = sqlHelper.getPhoneNumber(mName);
		mTextViewPhone.setText(mPhone);
		mTextViewName.setText(mName);
		sqlHelper.close();

	}

	@Override
	public void onDestroy() {
		SharedPreferences settings = getSharedPreferences(CatFactsHelper.SHARED_PREFS, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(SHARED_PREFS_CHECKBOX_VIEWSMS, mCheckBoxViewSMS.isChecked());
		editor.commit();

		super.onDestroy();
	}

	private void messageHandler(String phoneNumber, String message) {
		try {
			if (mCheckBoxViewSMS.isChecked()) {
				mSmsSender.sendSMSwithIntent(mPhone, message);
			} else {
				mSmsSender.sendSMS(mPhone, message);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(this.mContext, "Oh no! Something went wrong", Toast.LENGTH_LONG).show();
		}
	}
}
