package diatar.eu;

import android.app.*;
import android.os.*;
import android.widget.*;
import android.view.*;
import android.content.*;
import java.lang.*;

public class RenameFile extends Activity {
	
	private EditText Editor;
	private String origname,origdir;
	private boolean isdir;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		setContentView(R.layout.renamefile);
		Editor = findViewById(R.id.rfEdit);

		Intent it = getIntent();
		origname = it.getStringExtra(G.idFNAME);
		origdir = it.getStringExtra(G.idDIR);
		Editor.setText(origname);
		isdir = it.getBooleanExtra(G.idISDIR,false);
		
		setTitle(origname==null ||origname.isEmpty() ? "Új könyvtár" : "Átnevezés");
	};

	public void onOk(View v) {
		Intent it = new Intent();
		it.putExtra(G.idFNAME,Editor.getText().toString());
		it.putExtra(G.idORIG,origname);
		it.putExtra(G.idISDIR,isdir);
		setResult(RESULT_OK,it);
		finish();
	}
	
	public void onCancel(View v) {
		setResult(RESULT_CANCELED);
		finish();
	}
	
}
