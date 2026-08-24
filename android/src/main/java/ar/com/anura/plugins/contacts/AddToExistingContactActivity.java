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

public class AddToExistingContactActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String phoneNumber = getIntent().getStringExtra("number");

        ActivityResultLauncher<Intent> editContactActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            (result) -> {
                setResult(result.getResultCode(), result.getData());
                finish();
            }
        );

        ActivityResultLauncher<Intent> addToExistingContactActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            (result) -> {
                Intent resultData = result.getData();
                if (result.getResultCode() != Activity.RESULT_OK) {
                    setResult(result.getResultCode(), resultData);
                    finish();
                    return;
                }
                if (resultData == null || resultData.getData() == null) {
                    setResult(Activity.RESULT_CANCELED);
                    finish();
                    return;
                }

                Uri selectedContactUri = ContactsContract.Contacts.getLookupUri(getContentResolver(), resultData.getData());
                if (selectedContactUri == null) {
                    setResult(Activity.RESULT_CANCELED);
                    finish();
                    return;
                }

                Intent editIntent = new Intent(this, EditContactActivity.class);
                editIntent.putExtra("contactUri", selectedContactUri.toString());
                editIntent.putExtra("number", phoneNumber);
                try {
                    editContactActivity.launch(editIntent);
                } catch (ActivityNotFoundException exception) {
                    setResult(Activity.RESULT_CANCELED);
                    finish();
                }
            }
        );

        if (savedInstanceState != null) {
            return;
        }

        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            setResult(Activity.RESULT_CANCELED);
            finish();
            return;
        }

        Intent phoneBookIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        try {
            addToExistingContactActivity.launch(phoneBookIntent);
        } catch (ActivityNotFoundException exception) {
            setResult(Activity.RESULT_CANCELED);
            finish();
        }
    }
}
