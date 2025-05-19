package ar.com.anura.plugins.contacts;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import androidx.activity.result.ActivityResult;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.PermissionState;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.ActivityCallback;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;

import org.json.JSONArray;

import java.io.IOException;

@CapacitorPlugin(
    name = "Contacts",
    permissions = @Permission(
        strings = { Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS },
        alias = ContactsPlugin.CONTACTS_PERMISSION
    )
)
public class ContactsPlugin extends Plugin {

    private Contacts contacts;

    static final String CONTACTS_PERMISSION = "contacts";

    public void load() {
        Context context = getContext();
        contacts = new Contacts(context);
    }

    @PluginMethod
    public void checkPermissions(PluginCall call) {
        JSObject permissionsResultJSON = new JSObject();
        permissionsResultJSON.put(CONTACTS_PERMISSION, getNotificationPermissionText());
        call.resolve(permissionsResultJSON);
    }

    @PluginMethod
    public void requestPermissions(PluginCall call) {
        if (getPermissionState(CONTACTS_PERMISSION) != PermissionState.GRANTED) {
            requestPermissionForAlias(CONTACTS_PERMISSION, call, "permissionsCallback");
        }
    }

    @PermissionCallback
    private void permissionsCallback(PluginCall call) {
        JSObject permissionsResultJSON = new JSObject();
        permissionsResultJSON.put(CONTACTS_PERMISSION, getNotificationPermissionText());
        call.resolve(permissionsResultJSON);
    }

    private String getNotificationPermissionText() {
        if (getPermissionState(CONTACTS_PERMISSION) == PermissionState.GRANTED) {
            return "granted";
        } else {
            return "denied";
        }
    }

    @PluginMethod
    public void getContacts(PluginCall call) {
        if (getActivity().isFinishing()) {
            String appFinishingMsg = getActivity().getString(R.string.app_finishing);
            call.reject(appFinishingMsg);
            return;
        }

        ContactSettings settings = getContactSettings(call);

        JSONArray result = contacts.getContacts(settings);
        JSObject res = new JSObject();
        res.put("contacts", JSArray.from(result));
        call.resolve(res);
    }

    @PluginMethod
    public void createContact(PluginCall call) {
        if (getActivity().isFinishing()) {
            String appFinishingMsg = getActivity().getString(R.string.app_finishing);
            call.reject(appFinishingMsg);
            return;
        }

        String contactNumber = call.getString("number");
        if (contactNumber == null) {
            call.reject("The number is required");
            return;
        }

        String contactName = call.getString("name");
        if (contactName == null) {
            contactName = "";
        }

        Intent intent = new Intent("android.intent.action.CREATE_CONTACT_ACTIVITY");
        intent.setPackage(getContext().getPackageName());

        intent.putExtra("number", contactNumber);
        intent.putExtra("name", contactName);

        startActivityForResult(call, intent, "createContactResult");
    }

    @ActivityCallback
    private void createContactResult(PluginCall call, ActivityResult result) {
        if (call == null) {
            return;
        }

        if (result.getResultCode() == RESULT_OK) {
            call.resolve();
        } else {
            call.reject("Contact was not saved");
        }
    }

    @PluginMethod
    public void addToExistingContact(PluginCall call) {
        if (getActivity().isFinishing()) {
            String appFinishingMsg = getActivity().getString(R.string.app_finishing);
            call.reject(appFinishingMsg);
            return;
        }

        String contactNumber = call.getString("number");
        if (contactNumber == null) {
            call.reject("The number is required");
            return;
        }

        Intent intent = new Intent("android.intent.action.ADD_TO_EXISTING_CONTACT_ACTIVITY");
        intent.setPackage(getContext().getPackageName());

        intent.putExtra("number", call.getString("number"));

        startActivityForResult(call, intent, "addToExistingContactResult");
    }

    @ActivityCallback
    private void addToExistingContactResult(PluginCall call, ActivityResult result) {
        if (call == null) {
            return;
        }

        if (result.getResultCode() == RESULT_OK) {
            call.resolve();
        } else {
            call.reject("Contact was not saved");
        }
    }

    @PluginMethod
    public void getGroups(PluginCall call) throws IOException {
        if (getActivity().isFinishing()) {
            String appFinishingMsg = getActivity().getString(R.string.app_finishing);
            call.reject(appFinishingMsg);
            return;
        }

        JSObject groups = contacts.getGroups();
        JSObject res = new JSObject();
        res.put("fileName", groups.getString("fileName"));
        res.put("count", groups.getInteger("count"));
        call.resolve(res);
    }

    @PluginMethod
    public void deleteContact(PluginCall call) {
        String contactId = call.getString("contactId");
        if (contactId == null) {
            call.reject("The contact id is required");
            return;
        }

        contacts.deleteContact(contactId);
        call.resolve();
    }

  private ContactSettings getContactSettings(PluginCall call) {
    boolean name = Boolean.TRUE.equals(call.getBoolean("name"));
    boolean phones = Boolean.TRUE.equals(call.getBoolean("phones"));
    boolean emails = Boolean.TRUE.equals(call.getBoolean("emails"));
    boolean birthday = Boolean.TRUE.equals(call.getBoolean("birthday"));
    boolean organization = Boolean.TRUE.equals(call.getBoolean("organization"));
    boolean role = Boolean.TRUE.equals(call.getBoolean("role"));
    boolean photo = Boolean.TRUE.equals(call.getBoolean("photo"));

    return new ContactSettings.Builder()
      .name(name)
      .phones(phones)
      .emails(emails)
      .birthday(birthday)
      .organization(organization)
      .role(role)
      .photo(photo)
      .build();
  }
}
