package ar.com.anura.plugins.contacts;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class EditContactActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String selectedContactUri = getIntent().getStringExtra("contactUri");
        String number = getIntent().getStringExtra("number");

        ActivityResultLauncher<Intent> editView = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            (result) -> {
                setResult(result.getResultCode(), result.getData());
                finish();
            }
        );

        if (savedInstanceState != null) {
            return;
        }

        if (selectedContactUri == null || number == null || number.trim().isEmpty()) {
            setResult(Activity.RESULT_CANCELED);
            finish();
            return;
        }

        Intent editIntent = new Intent(Intent.ACTION_EDIT);
        editIntent.setDataAndType(Uri.parse(selectedContactUri), ContactsContract.Contacts.CONTENT_ITEM_TYPE);
        editIntent.putExtra(ContactsContract.Intents.Insert.PHONE, number);
        editIntent.putExtra(ContactsContract.Intents.Insert.PHONE_TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_WORK);

        try {
            editView.launch(editIntent);
        } catch (ActivityNotFoundException exception) {
            setResult(Activity.RESULT_CANCELED);
            finish();
        }
    }
}
