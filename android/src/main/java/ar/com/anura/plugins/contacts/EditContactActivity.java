package ar.com.anura.plugins.contacts;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class EditContactActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String selectedContactUri = getIntent().getStringExtra("contactUri");
        String number = getIntent().getStringExtra("number");

        // Creates a new Intent to edit a contact
        Intent editIntent = new Intent(Intent.ACTION_EDIT);
        /*
         * Sets the contact URI to edit, and the data type that the
         * Intent must match
         */
        editIntent.setDataAndType(Uri.parse(selectedContactUri), ContactsContract.Contacts.CONTENT_ITEM_TYPE);

        editIntent.putExtra(ContactsContract.Intents.Insert.PHONE, number);
        editIntent.putExtra(ContactsContract.Intents.Insert.PHONE_TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_WORK);

        ActivityResultLauncher<Intent> editView = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    setResult(Activity.RESULT_OK, result.getData());
                    finishActivity();
                }
            }
        );

        editView.launch(editIntent);
    }

    private void finishActivity() {
        finish();
    }
}
