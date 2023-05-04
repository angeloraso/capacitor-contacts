package ar.com.anura.plugins.contacts;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Base64;
import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.json.JSONException;

public class Contacts {

    private final Context mContext;
    private static final String CONTACT_ID = "contactId";
    private static final String EMAILS = "emails";
    private static final String EMAIL_LABEL = "label";
    private static final String EMAIL_ADDRESS = "address";
    private static final String PHONE_NUMBERS = "phoneNumbers";
    private static final String PHONE_LABEL = "label";
    private static final String PHONE_NUMBER = "number";
    private static final String DISPLAY_NAME = "displayName";
    private static final String PHOTO_THUMBNAIL = "photoThumbnail";
    private static final String ORGANIZATION_NAME = "organizationName";
    private static final String ORGANIZATION_ROLE = "organizationRole";
    private static final String BIRTHDAY = "birthday";

    Contacts(final Context context) {
        mContext = context;
    }

    public JSArray getContacts() {
        // initialize array
        JSArray jsContacts = new JSArray();

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

        Cursor contactsCursor = contentResolver.query(ContactsContract.Data.CONTENT_URI, projection, selection, selectionArgs, null);

        if (contactsCursor != null && contactsCursor.getCount() > 0) {
            HashMap<Object, JSObject> contactsById = new HashMap<>();

            while (contactsCursor.moveToNext()) {
                String _id = contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.Contacts._ID));
                String contactId = contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.Data.CONTACT_ID));

                JSObject jsContact = new JSObject();

                if (!contactsById.containsKey(contactId)) {
                    // this contact does not yet exist in HashMap,
                    // so put it to the HashMap

                    jsContact.put(CONTACT_ID, contactId);
                    String displayName = contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                    jsContact.put(DISPLAY_NAME, displayName);
                    JSArray jsPhoneNumbers = new JSArray();
                    jsContact.put(PHONE_NUMBERS, jsPhoneNumbers);
                    JSArray jsEmailAddresses = new JSArray();
                    jsContact.put(EMAILS, jsEmailAddresses);

                    jsContacts.put(jsContact);
                } else {
                    // this contact already exists,
                    // retrieve it
                    jsContact = contactsById.get(contactId);
                }

                if (jsContact != null) {
                    String mimeType = contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.Data.MIMETYPE));
                    String data = contactsCursor.getString(
                        contactsCursor.getColumnIndex(ContactsContract.CommonDataKinds.Contactables.DATA)
                    );
                    int type = contactsCursor.getInt(contactsCursor.getColumnIndex(ContactsContract.CommonDataKinds.Contactables.TYPE));
                    String label = contactsCursor.getString(
                        contactsCursor.getColumnIndex(ContactsContract.CommonDataKinds.Contactables.LABEL)
                    );

                    // email
                    if (mimeType.equals(ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)) {
                        try {
                            // add this email to the list
                            JSArray emailAddresses = (JSArray) jsContact.get(EMAILS);
                            JSObject jsEmail = new JSObject();
                            jsEmail.put(EMAIL_LABEL, mapEmailTypeToLabel(type, label));
                            jsEmail.put(EMAIL_ADDRESS, data);
                            emailAddresses.put(jsEmail);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    // phone
                    else if (mimeType.equals(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)) {
                        try {
                            // add this phone to the list
                            JSArray jsPhoneNumbers = (JSArray) jsContact.get(PHONE_NUMBERS);
                            JSObject jsPhone = new JSObject();
                            jsPhone.put(PHONE_LABEL, mapPhoneTypeToLabel(type, label));
                            jsPhone.put(PHONE_NUMBER, data);
                            jsPhoneNumbers.put(jsPhone);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    // birthday
                    else if (mimeType.equals(ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE)) {
                        int eventType = contactsCursor.getInt(
                            contactsCursor.getColumnIndex(ContactsContract.CommonDataKinds.Contactables.TYPE)
                        );
                        if (eventType == ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY) {
                            jsContact.put(BIRTHDAY, data);
                        }
                    }
                    // organization
                    else if (mimeType.equals(ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)) {
                        jsContact.put(ORGANIZATION_NAME, data);
                        String organizationRole = contactsCursor.getString(
                            contactsCursor.getColumnIndex(ContactsContract.CommonDataKinds.Organization.TITLE)
                        );
                        if (organizationRole != null) {
                            jsContact.put(ORGANIZATION_ROLE, organizationRole);
                        }
                    }
                    // photo
                    else if (mimeType.equals(ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)) {
                        byte[] thumbnailPhoto = contactsCursor.getBlob(
                            contactsCursor.getColumnIndex(ContactsContract.Contacts.Photo.PHOTO)
                        );
                        if (thumbnailPhoto != null) {
                            String encodedThumbnailPhoto = Base64.encodeToString(thumbnailPhoto, Base64.NO_WRAP);
                            jsContact.put(PHOTO_THUMBNAIL, "data:image/png;base64," + encodedThumbnailPhoto);
                        }
                    }

                    contactsById.put(contactId, jsContact);
                }
            }
        }
        if (contactsCursor != null) {
            contactsCursor.close();
        }

        return jsContacts;
    }

    public void deleteContact(String contactId) {
        Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, contactId);
        mContext.getContentResolver().delete(uri, null, null);
    }

    public JSArray getGroups() {
        JSArray groups = new JSArray();

        Cursor dataCursor = mContext.getContentResolver().query(ContactsContract.Groups.CONTENT_URI, null, null, null, null);

        while (dataCursor.moveToNext()) {
            JSObject group = new JSObject();
            String groupId = dataCursor.getString(dataCursor.getColumnIndex(ContactsContract.Groups._ID));
            group.put("groupId", groupId);
            group.put("accountType", dataCursor.getString(dataCursor.getColumnIndex(ContactsContract.Groups.ACCOUNT_TYPE)));
            group.put("accountName", dataCursor.getString(dataCursor.getColumnIndex(ContactsContract.Groups.ACCOUNT_NAME)));
            group.put("title", dataCursor.getString(dataCursor.getColumnIndex(ContactsContract.Groups.TITLE)));
            groups.put(group);
        }
        dataCursor.close();

        return groups;
    }

    public Map<String, Set<String>> getContactGroups() {
        Cursor dataCursor = mContext
            .getContentResolver()
            .query(
                ContactsContract.Data.CONTENT_URI,
                new String[] { ContactsContract.Data.CONTACT_ID, ContactsContract.Data.DATA1 },
                ContactsContract.Data.MIMETYPE + "=?",
                new String[] { ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE },
                null
            );

        Map<String, Set<String>> contact2GroupMap = new HashMap<>();
        while (dataCursor.moveToNext()) {
            String contact_id = dataCursor.getString(0);
            String group_id = dataCursor.getString(1);

            Set<String> groups = new HashSet<>();
            if (contact2GroupMap.containsKey(contact_id)) {
                groups = contact2GroupMap.get(contact_id);
            }
            groups.add(group_id);
            contact2GroupMap.put(contact_id, groups);
        }
        dataCursor.close();

        return contact2GroupMap;
    }

    private String mapEmailTypeToLabel(int type, String defaultLabel) {
        switch (type) {
            case ContactsContract.CommonDataKinds.Email.TYPE_HOME:
                return "home";
            case ContactsContract.CommonDataKinds.Email.TYPE_WORK:
                return "work";
            case ContactsContract.CommonDataKinds.Email.TYPE_OTHER:
                return "other";
            case ContactsContract.CommonDataKinds.Email.TYPE_MOBILE:
                return "mobile";
            default:
                return defaultLabel;
        }
    }

    private String mapPhoneTypeToLabel(int type, String defaultLabel) {
        switch (type) {
            case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                return "mobile";
            case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
                return "home";
            case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
                return "work";
            case ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK:
                return "fax work";
            case ContactsContract.CommonDataKinds.Phone.TYPE_FAX_HOME:
                return "fax home";
            case ContactsContract.CommonDataKinds.Phone.TYPE_PAGER:
                return "pager";
            case ContactsContract.CommonDataKinds.Phone.TYPE_OTHER:
                return "other";
            case ContactsContract.CommonDataKinds.Phone.TYPE_CALLBACK:
                return "callback";
            case ContactsContract.CommonDataKinds.Phone.TYPE_CAR:
                return "car";
            case ContactsContract.CommonDataKinds.Phone.TYPE_COMPANY_MAIN:
                return "company main";
            case ContactsContract.CommonDataKinds.Phone.TYPE_ISDN:
                return "isdn";
            case ContactsContract.CommonDataKinds.Phone.TYPE_MAIN:
                return "main";
            case ContactsContract.CommonDataKinds.Phone.TYPE_OTHER_FAX:
                return "other fax";
            case ContactsContract.CommonDataKinds.Phone.TYPE_RADIO:
                return "radio";
            case ContactsContract.CommonDataKinds.Phone.TYPE_TELEX:
                return "telex";
            case ContactsContract.CommonDataKinds.Phone.TYPE_TTY_TDD:
                return "tty";
            case ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE:
                return "work mobile";
            case ContactsContract.CommonDataKinds.Phone.TYPE_WORK_PAGER:
                return "work pager";
            case ContactsContract.CommonDataKinds.Phone.TYPE_ASSISTANT:
                return "assistant";
            case ContactsContract.CommonDataKinds.Phone.TYPE_MMS:
                return "mms";
            default:
                return defaultLabel;
        }
    }
}
