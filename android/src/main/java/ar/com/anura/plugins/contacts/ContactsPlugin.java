package ar.com.anura.plugins.contacts;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import androidx.activity.result.ActivityResult;
import com.getcapacitor.JSObject;
import com.getcapacitor.PermissionState;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.ActivityCallback;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;

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

    static final String CONTACTS_PERMISSION = "display";

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

        JSObject groups = contacts.getContacts();
        JSObject res = new JSObject();
        res.put("path", groups.getString("fileName"));
        res.put("count", groups.getInteger("count"));
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
        res.put("path", groups.getString("fileName"));
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
}
