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
import java.util.ArrayList;
import java.util.HashMap;

public class Contacts {
  private final String TAG = "Contacts";

  private static final String CONTACT_ID = "id";
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

  private static final String GROUP_ID = "id";
  private static final String GROUP_TITLE = "title";
  private static final String GROUP_SYSTEM_ID = "systemId";
  private static final String GROUP_NOTES = "notes";
  private static final String GROUP_ACCOUNT_TYPE = "accountType";
  private static final String GROUP_ACCOUNT_NAME = "accountName";

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
            jsContact.put(CONTACT_ID, contactId);

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
            if (settings.name()) {
              String displayName;

              if (displayNameIndex >= 0) {
                displayName = contactsCursor.getString(displayNameIndex);
                jsContact.put(DISPLAY_NAME, displayName);
              } else {
                Log.e(TAG, "Column not found: " + ContactsContract.Contacts.DISPLAY_NAME);
              }
            }

            String mimeType = "";
            if (mimeTypeIndex >= 0) {
              mimeType = contactsCursor.getString(mimeTypeIndex);
            } else {
              Log.e(TAG, "Column not found: " + ContactsContract.Data.MIMETYPE);
            }

            String data = "";
            if (dataIndex >= 0) {
              data = contactsCursor.getString(dataIndex);
            } else {
              Log.e(TAG, "Column not found: " + ContactsContract.CommonDataKinds.Contactables.DATA);
            }

            int type = 0;
            if (typeIndex >= 0) {
              type = contactsCursor.getInt(typeIndex);
            } else {
              Log.e(TAG, "Column not found: " + ContactsContract.CommonDataKinds.Contactables.TYPE);
            }

            String label = "";
            if (labelIndex >= 0) {
              label = contactsCursor.getString(labelIndex);
            } else {
              Log.e(TAG, "Column not found: " + ContactsContract.CommonDataKinds.Contactables.LABEL);
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
                String organizationRole = "";
                if (titleIndex >= 0) {
                  organizationRole = contactsCursor.getString(titleIndex);
                } else {
                  Log.e(TAG, "Column not found: " + ContactsContract.CommonDataKinds.Organization.TITLE);
                }

                if (organizationRole != null) {
                  jsContact.put(ORGANIZATION_ROLE, organizationRole);
                }
              }
            }
            else if (settings.photo() && mimeType.equals(ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)) {
              byte[] thumbnailPhoto = new byte[0];
              if (photoIndex >= 0) {
                thumbnailPhoto = contactsCursor.getBlob(photoIndex);
              } else {
                Log.e(TAG, "Column not found: " + ContactsContract.Contacts.Photo.PHOTO);
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

  public JSONArray getGroups(GroupSettings settings) {
    JSONArray jsGroups = new JSONArray();
    ContentResolver contentResolver = mContext.getContentResolver();
    Cursor groupsCursor = null;

    ArrayList<String> projectionList = new ArrayList<>();
    projectionList.add(ContactsContract.Groups._ID);

    if (settings.title()) {
      projectionList.add(ContactsContract.Groups.TITLE);
    }
    if (settings.systemId()) {
      projectionList.add(ContactsContract.Groups.SYSTEM_ID);
    }
    if (settings.notes()) {
      projectionList.add(ContactsContract.Groups.NOTES);
    }
    if (settings.accountType()) {
      projectionList.add(ContactsContract.Groups.ACCOUNT_TYPE);
    }
    if (settings.accountName()) {
      projectionList.add(ContactsContract.Groups.ACCOUNT_NAME);
    }

    String[] projection = projectionList.toArray(new String[0]);

    try {
      groupsCursor = contentResolver.query(
        ContactsContract.Groups.CONTENT_URI,
        projection,
        null,
        null,
        ContactsContract.Groups.TITLE + " COLLATE LOCALIZED ASC"
      );

      if (groupsCursor != null && groupsCursor.getCount() > 0) {
        int groupIdIndex = groupsCursor.getColumnIndex(ContactsContract.Groups._ID);
        int titleIndex = settings.title() ? groupsCursor.getColumnIndex(ContactsContract.Groups.TITLE) : -1;
        int systemIdIndex = settings.systemId() ? groupsCursor.getColumnIndex(ContactsContract.Groups.SYSTEM_ID) : -1;
        int notesIndex = settings.notes() ? groupsCursor.getColumnIndex(ContactsContract.Groups.NOTES) : -1;
        int accountTypeIndex = settings.accountType() ? groupsCursor.getColumnIndex(ContactsContract.Groups.ACCOUNT_TYPE) : -1;
        int accountNameIndex = settings.accountName() ? groupsCursor.getColumnIndex(ContactsContract.Groups.ACCOUNT_NAME) : -1;

        while (groupsCursor.moveToNext()) {
          JSONObject jsGroup = new JSONObject();
          try {
            if (groupIdIndex >= 0) {
              jsGroup.put(GROUP_ID, groupsCursor.getLong(groupIdIndex));
            } else {
              Log.e(TAG, "Column not found: " + ContactsContract.Groups._ID);
              continue;
            }

            if (titleIndex >= 0) {
              jsGroup.put(GROUP_TITLE, groupsCursor.getString(titleIndex));
            } else {
              Log.e(TAG, "Column not found: " + ContactsContract.Groups.TITLE);
            }

            if (systemIdIndex >= 0) {
              jsGroup.put(GROUP_SYSTEM_ID, groupsCursor.getString(systemIdIndex));
            } else {
              Log.e(TAG, "Column not found: " + ContactsContract.Groups.SYSTEM_ID);
            }

            if (notesIndex >= 0) {
              jsGroup.put(GROUP_NOTES, groupsCursor.getString(notesIndex));
            } else {
              Log.e(TAG, "Column not found: " + ContactsContract.Groups.NOTES);
            }

            if (accountTypeIndex >= 0) {
              jsGroup.put(GROUP_ACCOUNT_TYPE, groupsCursor.getString(accountTypeIndex));
            } else {
              Log.e(TAG, "Column not found: " + ContactsContract.Groups.ACCOUNT_TYPE);
            }

            if (accountNameIndex >= 0) {
              jsGroup.put(GROUP_ACCOUNT_NAME, groupsCursor.getString(accountNameIndex));
            } else {
              Log.e(TAG, "Column not found: " + ContactsContract.Groups.ACCOUNT_NAME);
            }

            jsGroups.put(jsGroup);
          } catch (JSONException e) {
            Log.e(TAG, "Get groups error", e);
          }
        }
      }
    } catch (SecurityException e) {
      Log.e(TAG, "Permission denied to read contacts groups", e);
    } finally {
      if (groupsCursor != null) {
        groupsCursor.close();
      }
    }

    return jsGroups;
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
