package ar.com.anura.plugins.contacts;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.JsonWriter;
import android.util.Log;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class Contacts {
    private final String TAG = "Contacts";

    private final Context mContext;

    Contacts(final Context context) {
        mContext = context;
    }

    public JSObject getContacts() {
      ContentResolver contentResolver = mContext.getContentResolver();

      String[] projection = new String[] {
        ContactsContract.Data.MIMETYPE,
        ContactsContract.CommonDataKinds.Organization.TITLE,
        ContactsContract.Contacts._ID,
        ContactsContract.Data.CONTACT_ID,
        ContactsContract.Contacts.DISPLAY_NAME,
        ContactsContract.Contacts.Photo.PHOTO,
        ContactsContract.CommonDataKinds.Contactables.DATA,
        ContactsContract.CommonDataKinds.Contactables.TYPE,
        ContactsContract.CommonDataKinds.Contactables.LABEL
      };

      String selection = ContactsContract.Data.MIMETYPE + " in (?, ?, ?, ?, ?)";
      String[] selectionArgs = new String[] {
        ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE,
        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
        ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE,
        ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE,
        ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE
      };

      Cursor cursor = contentResolver.query(ContactsContract.Data.CONTENT_URI, projection, selection, selectionArgs, null);

      String fileName = "contacts.json";
      Integer count = 0;
      File file = new File(mContext.getCacheDir(), fileName);

      try (FileWriter writer = new FileWriter(file);
           JsonWriter jsonWriter = new JsonWriter(writer)) {

        jsonWriter.setIndent("  ");
        jsonWriter.beginArray(); // Start contacts array

        HashMap<String, ContactWrapper> contactMap = new HashMap<>();

        while (cursor != null && cursor.moveToNext()) {
          int contactIdIndex = cursor.getColumnIndex(ContactsContract.Data.CONTACT_ID);
          int displayNameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
          int mimeTypeIndex = cursor.getColumnIndex(ContactsContract.Data.MIMETYPE);
          int dataIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Contactables.DATA);
          int typeIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Contactables.TYPE);
          int labelIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Contactables.LABEL);

          while (cursor.moveToNext()) {
            String contactId = (contactIdIndex >= 0 && !cursor.isNull(contactIdIndex)) ? cursor.getString(contactIdIndex) : null;
            String displayName = (displayNameIndex >= 0 && !cursor.isNull(displayNameIndex)) ? cursor.getString(displayNameIndex) : null;
            String mimeType = (mimeTypeIndex >= 0 && !cursor.isNull(mimeTypeIndex)) ? cursor.getString(mimeTypeIndex) : null;
            String data = (dataIndex >= 0 && !cursor.isNull(dataIndex)) ? cursor.getString(dataIndex) : null;
            int type = (typeIndex >= 0 && !cursor.isNull(typeIndex)) ? cursor.getInt(typeIndex) : -1;
            String label = (labelIndex >= 0 && !cursor.isNull(labelIndex)) ? cursor.getString(labelIndex) : null;

            ContactWrapper contact = contactMap.getOrDefault(contactId, new ContactWrapper(contactId, displayName));
            assert contact != null;
            assert mimeType != null;
            contact.updateWithMimeType(mimeType, data, type, label, cursor);
            contactMap.put(contactId, contact);
          }
        }

        if (cursor != null) {
          cursor.close();
        }

        count = contactMap.values().size();

        // Write all contacts to JSON
        for (ContactWrapper contact : contactMap.values()) {
          contact.writeToJson(jsonWriter);
        }

        jsonWriter.endArray(); // End contacts array
        jsonWriter.flush();
      } catch (IOException e) {
        Log.e(TAG, "Error writing contacts to file", e);
      }

      JSObject result = new JSObject();
      result.put("fileName", fileName);
      result.put("count", count);

      return result;
    }

    public void deleteContact(String contactId) {
        Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, contactId);
        mContext.getContentResolver().delete(uri, null, null);
    }

  public JSObject getGroups() throws IOException {
    JSArray groups = new JSArray();
    String fileName = "contacts_groups.json";

    Cursor dataCursor = mContext.getContentResolver().query(
      ContactsContract.Groups.CONTENT_URI,
      null,
      null,
      null,
      null
    );

    if (dataCursor != null) {
      int groupIdIndex = dataCursor.getColumnIndex(ContactsContract.Groups._ID);
      int accountTypeIndex = dataCursor.getColumnIndex(ContactsContract.Groups.ACCOUNT_TYPE);
      int accountNameIndex = dataCursor.getColumnIndex(ContactsContract.Groups.ACCOUNT_NAME);
      int titleIndex = dataCursor.getColumnIndex(ContactsContract.Groups.TITLE);

      while (dataCursor.moveToNext()) {
        JSObject group = new JSObject();

        if (groupIdIndex >= 0 && !dataCursor.isNull(groupIdIndex)) {
          group.put("groupId", dataCursor.getString(groupIdIndex));
        } else {
          continue;
        }

        if (accountTypeIndex >= 0 && !dataCursor.isNull(accountTypeIndex)) {
          group.put("accountType", dataCursor.getString(accountTypeIndex));
        }

        if (accountNameIndex >= 0 && !dataCursor.isNull(accountNameIndex)) {
          group.put("accountName", dataCursor.getString(accountNameIndex));
        }

        if (titleIndex >= 0 && !dataCursor.isNull(titleIndex)) {
          group.put("title", dataCursor.getString(titleIndex));
        }

        groups.put(group);
      }

      dataCursor.close();
    }

    // Write JSON to file
    File cacheDir = mContext.getCacheDir();
    File outputFile = new File(cacheDir, "contact_groups.json");

    FileWriter writer = new FileWriter(outputFile);
    writer.write(groups.toString());
    writer.close();

    // Return file path and count
    JSObject result = new JSObject();
    result.put("fileName", fileName);
    result.put("count", groups.length());

    return result;
  }
}
