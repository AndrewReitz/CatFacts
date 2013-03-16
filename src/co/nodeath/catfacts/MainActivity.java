/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package co.nodeath.catfacts;

import com.actionbarsherlock.app.SherlockActivity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.List;

public class MainActivity extends SherlockActivity {

    private static final int PICK_CONTACT = 0;

    Context mContext;
    Button mButtonAddContact;
    ImageButton mButtonFindContact;
    EditText mEditTextName;
    EditText mEditTextPhone;
    ListView mViewPickedContacts;
    List<String> mConstactList = new LinkedList<String>();

    // ACTION EVENTS
    OnClickListener onClickListener_find_contact = new OnClickListener() {
        public void onClick(View v) {
            startActivityForResult(new Intent(Intent.ACTION_PICK,
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI), PICK_CONTACT);
        }
    };

    OnClickListener onClickListener_add_contact = new OnClickListener() {
        public void onClick(View v) {
            String name = mEditTextName.getText().toString();
            String phone = mEditTextPhone.getText().toString();

            StringBuilder sbError = new StringBuilder();
            if (!(name.length() > 0)) {
                sbError.append("Please provide a name.\n");
            }
            if (!(phone.length() > 9)) {
                sbError.append("Please privde a valid phone number");
            }

            if (sbError.toString().equals("")) {
                StringBuilder sbNumber = new StringBuilder();
                for (int i = 0; i < phone.length(); i++) {
                    char tmp = phone.charAt(i);
                    if (Character.isDigit(tmp)) {
                        sbNumber.append(tmp);
                    }
                }

                CatFactsSQLiteHelper sqlHelper = new CatFactsSQLiteHelper(mContext);
                sqlHelper.insertPerson(name, sbNumber.toString());
                sqlHelper.close();
                mConstactList.add(name);
                mViewPickedContacts.setAdapter(
                        new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1,
                                mConstactList));
                mEditTextName.setText("");
                mEditTextPhone.setText("");
            } else {
                Toast.makeText(mContext, sbError.toString(), Toast.LENGTH_LONG).show();
            }
        }
    };

    OnItemClickListener onClickListener_contact = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // go to ContactActivity passing persons name
            Bundle bundle = new Bundle();
            bundle.putString("person", ((TextView) view).getText().toString());
            startActivity(new Intent(mContext, ContactActivity.class).putExtras(bundle));

        }
    };

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mContext = this;

        // hide the keyboard (it's being annoying)
        this.getWindow()
                .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        // register views
        mButtonAddContact = (Button) findViewById(R.id.main_button_add_contact);
        mButtonFindContact = (ImageButton) findViewById(R.id.main_button_find_contact);
        mEditTextName = (EditText) findViewById(R.id.main_edit_text_name);
        mEditTextPhone = (EditText) findViewById(R.id.main_edit_text_phone);
        mViewPickedContacts = (ListView) findViewById(R.id.main_listview_subscribed_contacts);

        // register events
        mButtonFindContact.setOnClickListener(onClickListener_find_contact);
        mButtonAddContact.setOnClickListener(onClickListener_add_contact);
        mViewPickedContacts.setOnItemClickListener(onClickListener_contact);

        addContactsToList();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            Uri contactData = data.getData();
            if (contactData != null) {
                Cursor c = null;
                try {
                    c = getContentResolver().query(contactData, null, null, null, null);
                    if (c != null && c.moveToFirst()) {
                        String name = c.getString(c.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                        String phone = c.getString(
                                c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                        mEditTextName.setText(name);
                        mEditTextPhone.setText(phone);
                    }
                } finally {
                    if (c != null) {
                        c.close();
                    }
                }
            }
        }
    }

    @Override
    protected void onResume() {
        addContactsToList();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void addContactsToList() {
        // handle database
        mConstactList = new LinkedList<String>();
        CatFactsSQLiteHelper sqlHelper = new CatFactsSQLiteHelper(mContext);

        // get all people names
        Cursor c = sqlHelper.getPersons();
        if (c.moveToFirst()) {
            while (!c.isAfterLast()) {
                mConstactList.add(c.getString(
                        c.getColumnIndex(CatFactsSQLiteHelper.TABLE_CONTACTS_COL_NAME)));
                c.moveToNext();
            }
        }
        c.close();
        sqlHelper.close();

        // display the names in the listview
        mViewPickedContacts.setAdapter(
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mConstactList));
    }
}
