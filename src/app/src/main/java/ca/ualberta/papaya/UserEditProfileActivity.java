package ca.ualberta.papaya;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import ca.ualberta.papaya.controllers.UserEditProfileController;
import ca.ualberta.papaya.interfaces.Observer;

public class UserEditProfileActivity extends AppCompatActivity implements Observer {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_edit_profile);

        EditText userInfoEditText = (EditText) findViewById(R.id.edit_user_profile_edit_contact_information);

        Button saveButton = (Button) findViewById(R.id.edit_user_profile_save);
        saveButton.setOnClickListener(UserEditProfileController.getInstance().getSaveOnClickListener(userInfoEditText));

        Button cancelButton = (Button) findViewById(R.id.edit_user_profile_cancel);
        cancelButton.setOnClickListener(UserEditProfileController.getInstance().getCancelOnClickListener());
    }

    @Override
    public void update() {

    }
}