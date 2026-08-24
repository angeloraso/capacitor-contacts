package ar.com.anura.plugins.contacts;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class CreateContactActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        String number = intent.getStringExtra("number");

        ActivityResultLauncher<Intent> createContactActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            (result) -> {
                setResult(result.getResultCode(), result.getData());
                finish();
            }
        );

        if (savedInstanceState != null) {
            return;
        }

        if (number == null || number.trim().isEmpty()) {
            setResult(Activity.RESULT_CANCELED);
            finish();
            return;
        }

        Intent phoneBookIntent = new Intent(ContactsContract.Intents.Insert.ACTION);
        phoneBookIntent.setType(ContactsContract.RawContacts.CONTENT_TYPE);
        phoneBookIntent.putExtra(ContactsContract.Intents.Insert.PHONE, number);
        phoneBookIntent.putExtra(ContactsContract.Intents.Insert.PHONE_TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_WORK);
        if (name != null && !name.trim().isEmpty()) {
            phoneBookIntent.putExtra(ContactsContract.Intents.Insert.NAME, name);
        }

        try {
            createContactActivity.launch(phoneBookIntent);
        } catch (ActivityNotFoundException exception) {
            setResult(Activity.RESULT_CANCELED);
            finish();
        }
    }
}
