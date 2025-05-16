package ar.com.anura.plugins.contacts;

import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Base64;
import android.util.JsonWriter;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ContactWrapper {
  private final String TAG = "ContactWrapper";
  String id;
  String name;
  List<JSONObject> phones = new ArrayList<>();
  List<JSONObject> emails = new ArrayList<>();
  String birthday;
  String organization;
  String organizationRole;
  String photoBase64;

  ContactWrapper(String id, String name) {
    this.id = id;
    this.name = name;
  }

  void updateWithMimeType(String mimeType, String data, int type, String label, Cursor cursor) {
    try {
      if (mimeType.equals(ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)) {
        JSONObject email = new JSONObject();
        email.put("label", mapEmailTypeToLabel(type, label));
        email.put("address", data);
        emails.add(email);
      } else if (mimeType.equals(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)) {
        JSONObject phone = new JSONObject();
        phone.put("label", mapPhoneTypeToLabel(type, label));
        phone.put("number", data);
        phones.add(phone);
      } else if (mimeType.equals(ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE) &&
        type == ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY) {
        birthday = data;
      } else if (mimeType.equals(ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)) {
        organization = data;
        int orgTitleIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Organization.TITLE);
        if (orgTitleIndex >= 0 && !cursor.isNull(orgTitleIndex)) {
          organizationRole = cursor.getString(orgTitleIndex);
        }
      } else if (mimeType.equals(ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)) {
        byte[] photo = null;
        int photoIndex = cursor.getColumnIndex(ContactsContract.Contacts.Photo.PHOTO);
        if (photoIndex != -1 && !cursor.isNull(photoIndex)) {
          photo = cursor.getBlob(photoIndex);
        }

        if (photo != null) {
          photoBase64 = Base64.encodeToString(photo, Base64.NO_WRAP);
        }
      }
    } catch (JSONException e) {
      Log.e(TAG, "updateWithMimeType error", e);
    }
  }

  void writeToJson(JsonWriter writer) throws IOException {
    writer.beginObject();
    writer.name("id").value(id);
    writer.name("name").value(name);

    writer.name("phones").beginArray();
    for (JSONObject phone : phones) {
      writer.beginObject();
      writer.name("label").value(phone.optString("label"));
      writer.name("number").value(phone.optString("number"));
      writer.endObject();
    }
    writer.endArray();

    writer.name("emails").beginArray();
    for (JSONObject email : emails) {
      writer.beginObject();
      writer.name("label").value(email.optString("label"));
      writer.name("address").value(email.optString("address"));
      writer.endObject();
    }
    writer.endArray();

    if (birthday != null) writer.name("birthday").value(birthday);
    if (organization != null) writer.name("organization").value(organization);
    if (organizationRole != null) writer.name("role").value(organizationRole);
    if (photoBase64 != null) writer.name("photo").value("data:image/png;base64," + photoBase64);

    writer.endObject();
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
