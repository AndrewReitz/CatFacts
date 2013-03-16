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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class CatFactsSQLiteHelper extends SQLiteOpenHelper {

    // static vars
    public static final int DATABASE_VERSION = 2;
    public static final String DB_NAME = "CatFacts.db";

    // contact table
    public static final String TABLE_CONTACTS = "Contacts";
    public static final String TABlE_CONTACTS_COL_ID = "_id";
    public static final String TABLE_CONTACTS_COL_NAME = "Name";
    public static final String TABLE_CONTACTS_COL_PHONENUMBER = "PhoneNumber";
    private static final String CREATE_TABLE_CONTACTS = "CREATE TABLE " + TABLE_CONTACTS + " ("
            + TABlE_CONTACTS_COL_ID + " integer primary key autoincrement, "
            + TABLE_CONTACTS_COL_NAME + " text not null, " + TABLE_CONTACTS_COL_PHONENUMBER
            + " text not null);";

    public static final String TABLE_AUTO_RESPOND = "AutoRespond";
    public static final String TABLE_AUTO_RESPOND_COL_PHONENUMBER = "PHONE";
    private static final String CREATE_AUTO_RESPOND_TABLE = "CREATE TABLE " + TABLE_AUTO_RESPOND
            + " (" + TABLE_AUTO_RESPOND_COL_PHONENUMBER + " text not null);";

    public CatFactsSQLiteHelper(Context context) {
        super(context, DB_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_CONTACTS);
        db.execSQL(CREATE_AUTO_RESPOND_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int odVersion, int newVersion) {
        db.execSQL(CREATE_AUTO_RESPOND_TABLE);
        //db.execSQL("drop table if exists " + TABLE_CONTACTS);
        //onCreate(db);
    }

    public void insertAutoRespond(String phoneNumber) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(TABLE_AUTO_RESPOND_COL_PHONENUMBER, phoneNumber);
        db.insert(TABLE_AUTO_RESPOND, null, cv);
        db.close();
    }

    public boolean autoRespond(String phoneNumber) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cur = db.query(TABLE_AUTO_RESPOND, new String[]{TABLE_AUTO_RESPOND_COL_PHONENUMBER},
                TABLE_AUTO_RESPOND_COL_PHONENUMBER + "=?", new String[]{phoneNumber}, null, null,
                null);

        boolean retvalue = false;
        if (cur.moveToFirst()) {
            retvalue = true;
        }
        cur.close();
        db.close();

        return retvalue;
    }

    public void insertPerson(String name, String phoneNumber) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(TABLE_CONTACTS_COL_NAME, name);
        cv.put(TABLE_CONTACTS_COL_PHONENUMBER, phoneNumber);
        db.insert(TABLE_CONTACTS, TABlE_CONTACTS_COL_ID, cv);
        db.close();
    }

    public void removePerson(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CONTACTS, TABLE_CONTACTS_COL_NAME + "=?", new String[]{name});
    }

    public Cursor getPersons() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cur = db.query(TABLE_CONTACTS, new String[]
                {TABLE_CONTACTS_COL_NAME, TABLE_CONTACTS_COL_PHONENUMBER}, null, null, null, null,
                null);
        return cur;
    }

    public String getPhoneNumber(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cur = db.query(TABLE_CONTACTS, new String[]{TABLE_CONTACTS_COL_PHONENUMBER},
                TABLE_CONTACTS_COL_NAME + "=?", new String[]{name}, null, null, null);

        String retString = null;
        if (cur.moveToFirst()) {
            retString = cur.getString(cur.getColumnIndex(TABLE_CONTACTS_COL_PHONENUMBER));
        }
        cur.close();
        db.close();

        return retString;
    }

}
