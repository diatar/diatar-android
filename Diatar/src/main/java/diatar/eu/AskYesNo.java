package diatar.eu;

import android.app.*;
import android.os.*;
import android.widget.*;
import android.view.*;
import android.content.*;
import java.lang.*;

public class AskYesNo extends Activity {

	private TextView Txt;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		setContentView(R.layout.askyesno);
		Txt = findViewById(R.id.ynTxt);

		Intent it = getIntent();
		String s = it.getStringExtra(G.idTXT);
		Txt.setText(s);
	}

	public void onYes(View v) {
		setResult(RESULT_OK);
		finish();
	}

	public void onNo(View v) {
		setResult(RESULT_CANCELED);
		finish();
	}
}
