package com.mikifus.padland;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class NewPadActivity extends PadLandActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newpad);

        Button btn = (Button) findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText textField = (EditText) findViewById(R.id.editText);

                String padName = (String) textField.getText().toString();
                String padServer = "https://pad.riseup.net/p/";
                String padUrl = "https://pad.riseup.net/p/" + padName;

                if (padName == "") {
                    return;
                }

                Intent padViewIntent =
                        new Intent(NewPadActivity.this, PadViewActivity.class);
                padViewIntent.putExtra("padName", padName);
                padViewIntent.putExtra("padServer", padServer);
                padViewIntent.putExtra("padUrl", padUrl);

                startActivity(padViewIntent);
            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu, R.menu.new_pad);
    }
}
