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

	public void onImportBtn(View v) {
		Intent it = new Intent(Intent.ACTION_OPEN_DOCUMENT);
		it.addCategory(Intent.CATEGORY_OPENABLE);
		it.setType("*/*");
		startActivityForResult(it, REQUEST_IMPORT);
	}
	
	public void onDownNowBtn(View v) {
		G.sDownWhen=mDownLst.getSelectedItemPosition();
		setResult(RESULT_FIRST_USER);
		finish();
	}
	
	public void onDtx2DirBtn(View v) {
		Intent it = new Intent(this, FileSelectorActivity.class);
		it.putExtra(G.idFTYPE,FileSelectorActivity.ftDIR);
		//it.putExtra(G.idDIR,mDtx2DirEd.getText().toString());
		startActivityForResult(it,REQUEST_DTX2DIR);
	}

	public void onCancel(View v) {
		setResult(RESULT_CANCELED);
		finish();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode==REQUEST_DTX2DIR) reqDtx2Dir(resultCode,data);
		if (requestCode==REQUEST_IMPORT) reqImport(resultCode,data);
	}

	private void reqDtx2Dir(int resultCode, Intent data) {
		if (resultCode!=RESULT_OK) return;
		//mDtx2DirEd.setText(data.getStringExtra(G.idDIR));
	}

	private void reqImport(int resultCode, Intent data) {
		if (data == null) return;
		Uri uri = data.getData();
		String fname = "???";
		String fpath = "???";
		Cursor cr = getContentResolver().query(uri,null,null,null,null,null);
		try {
			if (cr!=null && cr.moveToFirst()) {
				fpath = cr.getString(0); //cr.getString(cr.getColumnIndex(OpenableColumns.DISPLAY_NAME));
				int idx = cr.getColumnIndex(OpenableColumns.DISPLAY_NAME);
				if (idx>=0) fname = cr.getString(idx);
			}
		} finally {
			cr.close();
		}
		BufferedInputStream bin = null;
		BufferedOutputStream bout = null;
		ActivityCompat.requestPermissions(
				this,
				new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
				1);
		try {
			//FileInputStream sin = new FileInputStream(FileChooser.getPath(this, uri));
			InputStream sin = getContentResolver().openInputStream(uri);
			FileOutputStream sout = new FileOutputStream(new File(TxTar.Get().getDtx2Dir(), fname));
			bin = new BufferedInputStream(sin);
			bout = new BufferedOutputStream(sout);
			byte[] buf = new byte[1024];
			while (bin.read(buf) != -1) bout.write(buf);
		} catch (Exception e) {
			TxTar.Msg(this, e.getLocalizedMessage());
		} finally {
			try {
				if (bin != null) bin.close();
				if (bout != null) bout.close();
			} catch (Exception e) {
				TxTar.Msg(this, e.getLocalizedMessage());
			}
		}
	}
}
