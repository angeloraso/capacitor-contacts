package ar.com.anura.plugins.contacts;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Base64;
import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

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
        JSArray jsContacts = new JSArray();
        ContentResolver contentResolver = mContext.getContentResolver();

        String[] projection = new String[] {
            ContactsContract.Data.MIMETYPE,
            ContactsContract.CommonDataKinds.Organization.TITLE,
            ContactsContract.Data.CONTACT_ID,
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.Contacts.Photo.PHOTO,
            ContactsContract.CommonDataKinds.Contactables.DATA,
            ContactsContract.CommonDataKinds.Contactables.TYPE,
            ContactsContract.CommonDataKinds.Contactables.LABEL
        };
        String selection = ContactsContract.Data.MIMETYPE + " in (?, ?, ?, ?, ?, ?)";
        String[] selectionArgs = new String[] {
            ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE,
            ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE,
            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
            ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE,
            ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE,
            ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE
        };

        try (Cursor contactsCursor = contentResolver.query(ContactsContract.Data.CONTENT_URI, projection, selection, selectionArgs, null)) {
            if (contactsCursor == null) {
                return jsContacts;
            }

            Map<String, JSObject> contactsById = new LinkedHashMap<>();
            Map<String, JSArray> phoneNumbersByContactId = new LinkedHashMap<>();
            Map<String, JSArray> emailsByContactId = new LinkedHashMap<>();

            int mimeTypeColumn = contactsCursor.getColumnIndexOrThrow(ContactsContract.Data.MIMETYPE);
            int contactIdColumn = contactsCursor.getColumnIndexOrThrow(ContactsContract.Data.CONTACT_ID);
            int displayNameColumn = contactsCursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME);
            int dataColumn = contactsCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Contactables.DATA);
            int typeColumn = contactsCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Contactables.TYPE);
            int labelColumn = contactsCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Contactables.LABEL);
            int organizationTitleColumn = contactsCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Organization.TITLE);
            int photoColumn = contactsCursor.getColumnIndexOrThrow(ContactsContract.Contacts.Photo.PHOTO);

            while (contactsCursor.moveToNext()) {
                String contactId = contactsCursor.getString(contactIdColumn);
                JSObject jsContact = contactsById.get(contactId);

                if (jsContact == null) {
                    jsContact = new JSObject();
                    jsContact.put(CONTACT_ID, contactId);
                    jsContact.put(DISPLAY_NAME, contactsCursor.getString(displayNameColumn));

                    JSArray jsPhoneNumbers = new JSArray();
                    jsContact.put(PHONE_NUMBERS, jsPhoneNumbers);
                    phoneNumbersByContactId.put(contactId, jsPhoneNumbers);

                    JSArray jsEmailAddresses = new JSArray();
                    jsContact.put(EMAILS, jsEmailAddresses);
                    emailsByContactId.put(contactId, jsEmailAddresses);

                    contactsById.put(contactId, jsContact);
                    jsContacts.put(jsContact);
                }

                String mimeType = contactsCursor.getString(mimeTypeColumn);
                String data = contactsCursor.getString(dataColumn);
                int type = contactsCursor.getInt(typeColumn);
                String label = contactsCursor.getString(labelColumn);

                if (ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE.equals(mimeType)) {
                    JSObject jsEmail = new JSObject();
                    jsEmail.put(EMAIL_LABEL, mapEmailTypeToLabel(type, label));
                    jsEmail.put(EMAIL_ADDRESS, data);
                    emailsByContactId.get(contactId).put(jsEmail);
                } else if (ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE.equals(mimeType)) {
                    JSObject jsPhone = new JSObject();
                    jsPhone.put(PHONE_LABEL, mapPhoneTypeToLabel(type, label));
                    jsPhone.put(PHONE_NUMBER, data);
                    phoneNumbersByContactId.get(contactId).put(jsPhone);
                } else if (ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE.equals(mimeType)) {
                    if (type == ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY) {
                        jsContact.put(BIRTHDAY, data);
                    }
                } else if (ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE.equals(mimeType)) {
                    jsContact.put(ORGANIZATION_NAME, data);
                    String organizationRole = contactsCursor.getString(organizationTitleColumn);
                    if (organizationRole != null) {
                        jsContact.put(ORGANIZATION_ROLE, organizationRole);
                    }
                } else if (ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE.equals(mimeType)) {
                    byte[] thumbnailPhoto = contactsCursor.getBlob(photoColumn);
                    if (thumbnailPhoto != null) {
                        String encodedThumbnailPhoto = Base64.encodeToString(thumbnailPhoto, Base64.NO_WRAP);
                        jsContact.put(PHOTO_THUMBNAIL, "data:image/png;base64," + encodedThumbnailPhoto);
                    }
                }
            }
        }

        return jsContacts;
    }

    public int deleteContact(long contactId) {
        return mContext
            .getContentResolver()
            .delete(ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId), null, null);
    }

    public JSArray getGroups() {
        JSArray groups = new JSArray();

        String[] projection = new String[] {
            ContactsContract.Groups._ID,
            ContactsContract.Groups.ACCOUNT_TYPE,
            ContactsContract.Groups.ACCOUNT_NAME,
            ContactsContract.Groups.TITLE
        };

        try (Cursor dataCursor = mContext.getContentResolver().query(ContactsContract.Groups.CONTENT_URI, projection, null, null, null)) {
            if (dataCursor == null) {
                return groups;
            }

            int groupIdColumn = dataCursor.getColumnIndexOrThrow(ContactsContract.Groups._ID);
            int accountTypeColumn = dataCursor.getColumnIndexOrThrow(ContactsContract.Groups.ACCOUNT_TYPE);
            int accountNameColumn = dataCursor.getColumnIndexOrThrow(ContactsContract.Groups.ACCOUNT_NAME);
            int titleColumn = dataCursor.getColumnIndexOrThrow(ContactsContract.Groups.TITLE);

            while (dataCursor.moveToNext()) {
                JSObject group = new JSObject();
                group.put("groupId", dataCursor.getString(groupIdColumn));
                group.put("accountType", dataCursor.getString(accountTypeColumn));
                group.put("accountName", dataCursor.getString(accountNameColumn));
                group.put("title", dataCursor.getString(titleColumn));
                groups.put(group);
            }
        }

        return groups;
    }

    public Map<String, Set<String>> getContactGroups() {
        Map<String, Set<String>> contact2GroupMap = new LinkedHashMap<>();

        try (
            Cursor dataCursor = mContext
                .getContentResolver()
                .query(
                    ContactsContract.Data.CONTENT_URI,
                    new String[] { ContactsContract.Data.CONTACT_ID, ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID },
                    ContactsContract.Data.MIMETYPE + "=?",
                    new String[] { ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE },
                    null
                )
        ) {
            if (dataCursor == null) {
                return contact2GroupMap;
            }

            while (dataCursor.moveToNext()) {
                String contactId = dataCursor.getString(0);
                String groupId = dataCursor.getString(1);
                contact2GroupMap.computeIfAbsent(contactId, (ignored) -> new LinkedHashSet<>()).add(groupId);
            }
        }

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
