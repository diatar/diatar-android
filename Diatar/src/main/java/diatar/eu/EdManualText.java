package diatar.eu;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;

public class EdManualText extends Activity
{
	private EditText mCim;
	private EditText mTxt;

	@Override
	protected void onCreate(Bundle bd) {
		super.onCreate(bd);

		setContentView(R.layout.edmanualtext);
		setTitle("Szöveg");
		mCim = findViewById(R.id.edManualTextCim);
		mTxt = findViewById(R.id.edManualTextSzoveg);

		String knev;
		String txt;
		if (bd==null) {
			Intent it = getIntent();
			knev=it.getStringExtra(G.idCIM);
			txt=it.getStringExtra(G.idTXT);
			if (knev==null) knev = "";
			if (txt==null) txt = "";
		} else{
			knev=bd.getString(G.idCIM);
			txt=bd.getString(G.idTXT);
		}
		mCim.setText(knev);
		mTxt.setText(txt);
	}
	
	@Override
	protected void onSaveInstanceState(@NonNull Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putString(G.idCIM,mCim.getText().toString());
		outState.putString(G.idTXT,mTxt.getText().toString());
	}

	public void onCancel(View v) {
		setResult(RESULT_CANCELED);
		finish();
	}
	
	public void onOk(View v) {
		Intent it = new Intent();
		it.putExtra(G.idCIM, mCim.getText().toString());
		it.putExtra(G.idTXT, mTxt.getText().toString());
		setResult(RESULT_OK,it);
		finish();
	}
}
