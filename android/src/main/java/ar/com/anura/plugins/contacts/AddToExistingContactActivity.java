package ar.com.anura.plugins.contacts;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class AddToExistingContactActivity extends AppCompatActivity {

    static final int PICK_CONTACT = -1;
    public static Cursor cursor;
    // A content URI pointing to the contact
    private Uri selectedContactUri;
    private String phoneNumber;
    private ActivityResultLauncher<Intent> editContactActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        phoneNumber = intent.getStringExtra("number");

        Intent phoneBookIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);

        editContactActivity =
            registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        setResult(Activity.RESULT_OK, result.getData());
                        finishActivity();
                    }
                }
            );

        ActivityResultLauncher<Intent> addToExistingContactActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == PICK_CONTACT) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Uri contactsData = result.getData().getData();
                            // The Cursor that contains the Contact row
                            AddToExistingContactActivity.cursor = getContentResolver().query(contactsData, null, null, null, null);
                            if (null != AddToExistingContactActivity.cursor && AddToExistingContactActivity.cursor.getCount() > 0) {
                                AddToExistingContactActivity.cursor.moveToFirst();
                            }

                            if (AddToExistingContactActivity.cursor.getCount() > 0) {
                                int idIndex = AddToExistingContactActivity.cursor.getColumnIndex(ContactsContract.Contacts._ID);
                                // Gets the lookup key column index
                                int lookupKeyIndex = AddToExistingContactActivity.cursor.getColumnIndex(
                                    ContactsContract.Contacts.LOOKUP_KEY
                                );
                                // Gets the lookup key value
                                String currentLookupKey = AddToExistingContactActivity.cursor.getString(lookupKeyIndex);
                                // Gets the _ID column index
                                long currentId = AddToExistingContactActivity.cursor.getInt(idIndex);
                                selectedContactUri = ContactsContract.Contacts.getLookupUri(currentId, currentLookupKey);

                                openEditContactActivity(selectedContactUri.toString(), phoneNumber);
                            }
                        }
                    }
                }
            }
        );

        addToExistingContactActivity.launch(phoneBookIntent);
    }

    private void openEditContactActivity(String contactUri, String number) {
        Intent editIntent = new Intent("android.intent.action.EDIT_CONTACT_ACTIVITY");
        editIntent.putExtra("contactUri", contactUri);
        editIntent.putExtra("number", number);
        editIntent.setPackage(getPackageName());

        editContactActivity.launch(editIntent);
    }

    private void finishActivity() {
        finish();
    }
}
