package diatar.eu;

import android.Manifest;
import android.app.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.*;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.widget.*;
import android.view.*;
import android.content.*;

import androidx.core.app.ActivityCompat;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;

public class SetDownload extends Activity
{
	private Spinner mDownLst;
	//private EditText mDtx2DirEd;
	private CheckBox mAlwaysOn;

	private static final int REQUEST_IMPORT = 110;
	private static final int REQUEST_DTX2DIR = 111;

	@Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

		setContentView(R.layout.setdownload);
		setTitle("Program beállítása");
		
		mDownLst = findViewById(R.id.setDownLst);
		//mDtx2DirEd = findViewById(R.id.setDownDtx2DirEd);
		mAlwaysOn = findViewById(R.id.setDownAlwaysOn);
		
		int dwhen = G.sDownWhen;
		long dlast = G.sDownLastDay;
		//String sdtx = TxTar.Get().getDtx2Dir();
		boolean aon = G.sAlwaysOn;
		if (savedInstanceState!=null) {
			dwhen=savedInstanceState.getInt(G.idDOWNWHEN);
			//sdtx=savedInstanceState.getString(G.idDTX2DIR);
			aon=savedInstanceState.getBoolean(G.idALWAYSON);
		}
		mDownLst.setSelection(dwhen);
		//mDtx2DirEd.setText(sdtx);
		mAlwaysOn.setChecked(aon);
		TextView tx = findViewById(R.id.setDownLastDay);
		long diff = System.currentTimeMillis()/(24*60*60*1000)-dlast;
		tx.setText("(utoljára ellenőrizve: "+
			(dlast==0 ? "Nem volt még" :
			(diff<=0 ? "MA" :
			(diff<2 ? "Tegnap" :
				Long.toString(diff)+" napja"
			)))+")");
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putInt(G.idDOWNWHEN,mDownLst.getSelectedItemPosition());
		//outState.putString(G.idDTX2DIR,mDtx2DirEd.getText().toString());
		outState.putBoolean(G.idALWAYSON,mAlwaysOn.isChecked());
	}
	
	public void onOk(View v) {
		G.sDownWhen=mDownLst.getSelectedItemPosition();
		//String s = mDtx2DirEd.getText().toString();
		//if (s==TxTar.Get().docdir) s="";
		//G.sDtx2Dir=s;
		G.sAlwaysOn=mAlwaysOn.isChecked();
		setResult(RESULT_OK);
		finish();
	}

	public void onDownNowBtn(View v) {
		G.sDownWhen=mDownLst.getSelectedItemPosition();
		setResult(RESULT_FIRST_USER);
		finish();
	}
	
	public void onCancel(View v) {
		setResult(RESULT_CANCELED);
		finish();
	}
}
