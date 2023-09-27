package diatar.eu;
import android.app.*;
import android.widget.*;
import android.os.*;
import android.content.*;
import android.view.*;

public class EdSep extends Activity
{
	private EditText mTxt;
	
	@Override
    protected void onCreate(Bundle bd) {
        super.onCreate(bd);

		setContentView(R.layout.edsep);
		setTitle("Elválasztó");
		mTxt = findViewById(R.id.edsepTxt);

		String s;
		if (bd==null) {
			Intent it = getIntent();
			s=it.getStringExtra(G.idTXT);
			if (s==null || s.isEmpty()) s="elválasztó";
		} else
			s=bd.getString(G.idTXT);
		mTxt.setText(s);
	};
	
	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putString(G.idTXT,mTxt.getText().toString());
	}

	public void onCancel(View v) {
		setResult(RESULT_CANCELED);
		finish();
	}
	
	public void onOk(View v) {
		Intent it = new Intent();
		it.putExtra(G.idTXT, mTxt.getText().toString());
		setResult(RESULT_OK,it);
		finish();
	}
}
