package valdez.albert.com.beep;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button btnStart;
    Button btnStop;
    Intent serviceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnStart = (Button) findViewById(R.id.btnStart);
        btnStop = (Button) findViewById(R.id.btnStop);

        serviceIntent = new Intent(this, BeepService.class);
        startService(serviceIntent);
    }

    public void startService(View view) {
        startService(serviceIntent);
    }

    public void stopService(View view) {
        stopService(serviceIntent);
    }
}
