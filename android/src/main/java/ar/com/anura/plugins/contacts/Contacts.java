package ar.com.anura.plugins.contacts;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Base64;
import android.util.Log;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class Contacts {
    private final String TAG = "Contacts";

    private static final String DISPLAY_NAME = "name";
    private static final String PHONE_NUMBERS = "phones";
    private static final String EMAILS = "emails";
    private static final String BIRTHDAY = "birthday";
    private static final String ORGANIZATION_NAME = "organization";
    private static final String ORGANIZATION_ROLE = "role";
    private static final String PHOTO_THUMBNAIL = "photo";
    private static final String PHONE_NUMBER = "number";
    private static final String PHONE_LABEL = "label";
    private static final String EMAIL_ADDRESS = "address";
    private static final String EMAIL_LABEL = "label";

    private final Context mContext;

    Contacts(final Context context) {
        mContext = context;
    }

  public JSONArray getContacts(ContactSettings settings) {
    JSONArray jsContacts = new JSONArray();

    ContentResolver contentResolver = mContext.getContentResolver();

    String[] projection = new String[]{
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
    String[] selectionArgs = new String[]{
      ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE,
      ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
      ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE,
      ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE,
      ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE
    };

    Cursor contactsCursor = contentResolver.query(ContactsContract.Data.CONTENT_URI, projection, selection, selectionArgs, null);

    if (contactsCursor != null && contactsCursor.getCount() > 0) {
      HashMap<String, JSONObject> contactsById = new HashMap<>();
      int contactIdIndex = contactsCursor.getColumnIndex(ContactsContract.Data.CONTACT_ID);
      int displayNameIndex = contactsCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
      int mimeTypeIndex = contactsCursor.getColumnIndex(ContactsContract.Data.MIMETYPE);
      int dataIndex = contactsCursor.getColumnIndex(ContactsContract.CommonDataKinds.Contactables.DATA);
      int typeIndex = contactsCursor.getColumnIndex(ContactsContract.CommonDataKinds.Contactables.TYPE);
      int labelIndex = contactsCursor.getColumnIndex(ContactsContract.CommonDataKinds.Contactables.LABEL);
      int titleIndex = contactsCursor.getColumnIndex(ContactsContract.CommonDataKinds.Organization.TITLE);
      int photoIndex = contactsCursor.getColumnIndex(ContactsContract.Contacts.Photo.PHOTO);

      try {
        while (contactsCursor.moveToNext()) {
          String contactId;
          if (contactIdIndex >= 0) {
            contactId = contactsCursor.getString(contactIdIndex);
          } else {
            Log.e(TAG, "Column not found: " + ContactsContract.Data.CONTACT_ID);
            continue;
          }

          JSONObject jsContact;

          if (!contactsById.containsKey(contactId)) {
            jsContact = new JSONObject();
            jsContact.put(ContactsContract.Data.CONTACT_ID, contactId);
            if (settings.name()) {
              String displayName;
              if (displayNameIndex >= 0) {
                displayName = contactsCursor.getString(displayNameIndex);
                jsContact.put(DISPLAY_NAME, displayName);
              } else {
                Log.e(TAG, "Column not found: " + ContactsContract.Contacts.DISPLAY_NAME);
                continue;
              }
            }

            if (settings.phones()) {
              jsContact.put(PHONE_NUMBERS, new JSONArray());
            }
            if (settings.emails()) {
              jsContact.put(EMAILS, new JSONArray());
            }

            jsContacts.put(jsContact);
            contactsById.put(contactId, jsContact);
          } else {
            jsContact = contactsById.get(contactId);
          }

          if (jsContact != null) {
            String mimeType;
            if (mimeTypeIndex >= 0) {
              mimeType = contactsCursor.getString(mimeTypeIndex);
            } else {
              Log.e(TAG, "Column not found: " + ContactsContract.Data.MIMETYPE);
              continue;
            }

            String data;
            if (dataIndex >= 0) {
              data = contactsCursor.getString(dataIndex);
            } else {
              Log.e(TAG, "Column not found: " + ContactsContract.CommonDataKinds.Contactables.DATA);
              continue;
            }

            int type;
            if (typeIndex >= 0) {
              type = contactsCursor.getInt(typeIndex);
            } else {
              Log.e(TAG, "Column not found: " + ContactsContract.CommonDataKinds.Contactables.TYPE);
              continue;
            }

            String label;
            if (labelIndex >= 0) {
              label = contactsCursor.getString(labelIndex);
            } else {
              Log.e(TAG, "Column not found: " + ContactsContract.CommonDataKinds.Contactables.LABEL);
              continue;
            }

            if (settings.emails() && mimeType.equals(ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)) {
              JSONArray emailAddresses = jsContact.optJSONArray(EMAILS);
              if (emailAddresses != null) {
                JSONObject jsEmail = new JSONObject();
                jsEmail.put(EMAIL_LABEL, mapEmailTypeToLabel(type, label));
                jsEmail.put(EMAIL_ADDRESS, data);
                emailAddresses.put(jsEmail);
              }
            }
            else if (settings.phones() && mimeType.equals(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)) {
              JSONArray jsPhoneNumbers = jsContact.optJSONArray(PHONE_NUMBERS);
              if (jsPhoneNumbers != null) {
                JSONObject jsPhone = new JSONObject();
                jsPhone.put(PHONE_LABEL, mapPhoneTypeToLabel(type, label));
                jsPhone.put(PHONE_NUMBER, data);
                jsPhoneNumbers.put(jsPhone);
              }
            }
            else if (settings.birthday() && mimeType.equals(ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE)) {
              int eventType = contactsCursor.getInt(typeIndex);
              if (eventType == ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY) {
                jsContact.put(BIRTHDAY, data);
              }
            }
            else if (settings.organization() && mimeType.equals(ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)) {
              jsContact.put(ORGANIZATION_NAME, data);
              if (settings.role()) {
                String organizationRole;
                if (titleIndex >= 0) {
                  organizationRole = contactsCursor.getString(titleIndex);
                } else {
                  Log.e(TAG, "Column not found: " + ContactsContract.CommonDataKinds.Organization.TITLE);
                  continue;
                }

                if (organizationRole != null) {
                  jsContact.put(ORGANIZATION_ROLE, organizationRole);
                }
              }
            }
            else if (settings.photo() && mimeType.equals(ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)) {
              byte[] thumbnailPhoto;
              if (photoIndex >= 0) {
                thumbnailPhoto = contactsCursor.getBlob(photoIndex);
              } else {
                Log.e(TAG, "Column not found: " + ContactsContract.Contacts.Photo.PHOTO);
                continue;
              }

              if (thumbnailPhoto != null) {
                String encodedThumbnailPhoto = Base64.encodeToString(thumbnailPhoto, Base64.NO_WRAP);
                jsContact.put(PHOTO_THUMBNAIL, "data:image/png;base64," + encodedThumbnailPhoto);
              }
            }

            contactsById.put(contactId, jsContact);
          }
        }
      } catch (JSONException e) {
        Log.e(TAG, "Get contacts error: ", e);
      } finally {
        contactsCursor.close();
      }
    }

    return jsContacts;
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

  private String mapEmailTypeToLabel(int type, String defaultLabel) {
    return switch (type) {
      case ContactsContract.CommonDataKinds.Email.TYPE_HOME -> "home";
      case ContactsContract.CommonDataKinds.Email.TYPE_WORK -> "work";
      case ContactsContract.CommonDataKinds.Email.TYPE_OTHER -> "other";
      case ContactsContract.CommonDataKinds.Email.TYPE_MOBILE -> "mobile";
      default -> defaultLabel;
    };
  }

  private String mapPhoneTypeToLabel(int type, String defaultLabel) {
    return switch (type) {
      case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE -> "mobile";
      case ContactsContract.CommonDataKinds.Phone.TYPE_HOME -> "home";
      case ContactsContract.CommonDataKinds.Phone.TYPE_WORK -> "work";
      case ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK -> "fax work";
      case ContactsContract.CommonDataKinds.Phone.TYPE_FAX_HOME -> "fax home";
      case ContactsContract.CommonDataKinds.Phone.TYPE_PAGER -> "pager";
      case ContactsContract.CommonDataKinds.Phone.TYPE_OTHER -> "other";
      case ContactsContract.CommonDataKinds.Phone.TYPE_CALLBACK -> "callback";
      case ContactsContract.CommonDataKinds.Phone.TYPE_CAR -> "car";
      case ContactsContract.CommonDataKinds.Phone.TYPE_COMPANY_MAIN -> "company main";
      case ContactsContract.CommonDataKinds.Phone.TYPE_ISDN -> "isdn";
      case ContactsContract.CommonDataKinds.Phone.TYPE_MAIN -> "main";
      case ContactsContract.CommonDataKinds.Phone.TYPE_OTHER_FAX -> "other fax";
      case ContactsContract.CommonDataKinds.Phone.TYPE_RADIO -> "radio";
      case ContactsContract.CommonDataKinds.Phone.TYPE_TELEX -> "telex";
      case ContactsContract.CommonDataKinds.Phone.TYPE_TTY_TDD -> "tty";
      case ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE -> "work mobile";
      case ContactsContract.CommonDataKinds.Phone.TYPE_WORK_PAGER -> "work pager";
      case ContactsContract.CommonDataKinds.Phone.TYPE_ASSISTANT -> "assistant";
      case ContactsContract.CommonDataKinds.Phone.TYPE_MMS -> "mms";
      default -> defaultLabel;
    };
  }
}
