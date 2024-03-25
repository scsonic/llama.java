package com.rnllamaexample;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class ShellActivity extends AppCompatActivity {

  TextView tvShellOutput ;
  EditText etInput ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shell);

        tvShellOutput = findViewById(R.id.tvShellOutput) ;
        etInput = findViewById(R.id.etInput) ;

      etInput.setOnKeyListener(new View.OnKeyListener() {
        @Override
        public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
          if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN) &&
            (keyCode == KeyEvent.KEYCODE_ENTER)) {
            // Perform action on key press
            String line = etInput.getText().toString();
            etInput.setText("",null);
            String result = ShellHelper.executeCommand(line);
            Log.e("@@", result) ;
            tvShellOutput.setText(result);
            return true;
          }
          return false;
        }
      });
    }
}
