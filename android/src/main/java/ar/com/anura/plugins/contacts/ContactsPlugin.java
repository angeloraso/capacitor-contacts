package ar.com.anura.plugins.contacts;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.Activity;
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
import java.util.Map;
import java.util.Set;

@CapacitorPlugin(
    name = "Contacts",
    permissions = {
        @Permission(strings = Manifest.permission.READ_CONTACTS, alias = ContactsPlugin.READ_CONTACTS_PERMISSION),
        @Permission(strings = Manifest.permission.WRITE_CONTACTS, alias = ContactsPlugin.WRITE_CONTACTS_PERMISSION)
    }
)
public class ContactsPlugin extends Plugin {

    private Contacts contacts;

    static final String READ_CONTACTS_PERMISSION = "readContacts";
    static final String WRITE_CONTACTS_PERMISSION = "writeContacts";

    @Override
    public void load() {
        Context context = getContext();
        contacts = new Contacts(context);
    }

    @PluginMethod
    public void getContacts(PluginCall call) {
        if (rejectIfPermissionMissing(call, READ_CONTACTS_PERMISSION, "Read contacts permission is required")) {
            return;
        }

        try {
            JSArray jsContacts = contacts.getContacts();
            JSObject res = new JSObject();
            res.put("contacts", jsContacts);
            call.resolve(res);
        } catch (RuntimeException exception) {
            call.reject("Unable to read contacts", exception);
        }
    }

    @PluginMethod
    public void createContact(PluginCall call) {
        if (rejectIfActivityUnavailable(call)) {
            return;
        }

        String contactNumber = call.getString("number");
        if (contactNumber == null || contactNumber.trim().isEmpty()) {
            call.reject("The number is required");
            return;
        }

        String contactName = call.getString("name");
        if (contactName == null) {
            contactName = "";
        }

        Intent intent = new Intent(getContext(), CreateContactActivity.class);

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
        if (rejectIfActivityUnavailable(call)) {
            return;
        }

        String contactNumber = call.getString("number");
        if (contactNumber == null || contactNumber.trim().isEmpty()) {
            call.reject("The number is required");
            return;
        }

        Intent intent = new Intent(getContext(), AddToExistingContactActivity.class);

        intent.putExtra("number", contactNumber);

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
    public void getGroups(PluginCall call) {
        if (rejectIfPermissionMissing(call, READ_CONTACTS_PERMISSION, "Read contacts permission is required")) {
            return;
        }

        try {
            JSArray groups = contacts.getGroups();
            JSObject res = new JSObject();
            res.put("groups", groups);
            call.resolve(res);
        } catch (RuntimeException exception) {
            call.reject("Unable to read contact groups", exception);
        }
    }

    @PluginMethod
    public void getContactGroups(PluginCall call) {
        if (rejectIfPermissionMissing(call, READ_CONTACTS_PERMISSION, "Read contacts permission is required")) {
            return;
        }

        try {
            Map<String, Set<String>> contactsGroup = contacts.getContactGroups();
            JSObject result = new JSObject();
            for (Map.Entry<String, Set<String>> entry : contactsGroup.entrySet()) {
                JSArray jsGroups = new JSArray();
                Set<String> groups = entry.getValue();
                for (String group : groups) {
                    jsGroups.put(group);
                }
                result.put(entry.getKey(), jsGroups);
            }

            call.resolve(result);
        } catch (RuntimeException exception) {
            call.reject("Unable to read contact group memberships", exception);
        }
    }

    @PluginMethod
    public void deleteContact(PluginCall call) {
        if (rejectIfPermissionMissing(call, WRITE_CONTACTS_PERMISSION, "Write contacts permission is required")) {
            return;
        }

        String contactId = call.getString("contactId");
        if (contactId == null || contactId.trim().isEmpty()) {
            call.reject("The contact id is required");
            return;
        }

        try {
            long parsedContactId = Long.parseLong(contactId.trim());
            int deletedRows = contacts.deleteContact(parsedContactId);
            if (deletedRows == 0) {
                call.reject("Contact was not found");
                return;
            }
            call.resolve();
        } catch (NumberFormatException exception) {
            call.reject("The contact id is invalid", exception);
        } catch (RuntimeException exception) {
            call.reject("Unable to delete contact", exception);
        }
    }

    private boolean rejectIfPermissionMissing(PluginCall call, String alias, String message) {
        if (getPermissionState(alias) == PermissionState.GRANTED) {
            return false;
        }

        call.reject(message);
        return true;
    }

    private boolean rejectIfActivityUnavailable(PluginCall call) {
        Activity activity = getActivity();
        if (activity != null && !activity.isFinishing()) {
            return false;
        }

        call.reject(getContext().getString(R.string.app_finishing));
        return true;
    }
}
