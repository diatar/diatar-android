package diatar.eu;

import android.app.*;
import android.os.*;
import android.widget.*;
import android.view.*;

public class SetAbout extends Activity {
	@Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(diatar.eu.R.layout.about);
		TextView tx = findViewById(diatar.eu.R.id.aboutVer);
		tx.setText("Verzió: "+G.getVersion(this));
	}
	
	public void onOk(View v) {
		finish();
	}
}
