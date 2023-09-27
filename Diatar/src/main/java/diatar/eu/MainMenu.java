package diatar.eu;

import androidx.fragment.app.*;

import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.view.*;
import android.content.*;

public class MainMenu extends FragmentActivity
{
	////////////////////
	// menu
	////////////////////
	
	protected void onMenuLoad() { doMenuLoad(); }
	protected void onMenuEdit() { doMenuEdit(); }
	protected void onMenuSave() {}
	protected void onMenuSetDownload() { doMenuDown(); }
	protected void onMenuSetNet() { doMenuNet(); }
	protected void onMenuSetProject() { doMenuProj(); }
	protected void onMenuSetAbout() { doMenuAbout(); }
	protected void onMenuStopProg() {}
	protected void onMenuStopShutdown() {}
	protected void onMenuExit() {}
	
	////////////////////
	// vegrehajtas utan
	////////////////////
	
	protected void whenLoadSelected(String dir, String fname) {
		if (G.OPEN_DIA_BY_FILESELECTOR) {
			G.sLoadDir = dir;
			G.sDiaFname = "";
			G.Save(this);
		}
		doLoading(fname);
	}
	
	protected void whenLoaded(boolean success, String fname) {}
	
	protected void whenSetNeted() {}
	
	protected void whenSetDowned(boolean downloadnow ) {}
	
	protected void whenSetProjed() {}
	
	protected void whenEdited() {}
	
	////////////////////
	// feldolgozas
	////////////////////
	
	private final int REQUEST_FILELOAD = 101;
	private final int REQUEST_LOADING = 102;
	private final int REQUEST_NETING  = 104;
	private final int REQUEST_DOWNING  = 105;
	private final int REQUEST_PROJING  = 106;
	private final int REQUEST_EDITING  = 107;
	
	private void doMenuLoad() {
		if (G.OPEN_DIA_BY_FILESELECTOR) {
			Intent it = new Intent(this, FileSelectorActivity.class);
			it.putExtra(G.idDIR, G.sLoadDir);
			startActivityForResult(it, REQUEST_FILELOAD);
		} else {
			Intent it = new Intent(Intent.ACTION_OPEN_DOCUMENT);
			it.addCategory(Intent.CATEGORY_OPENABLE);
			it.setType("*/*");
			startActivityForResult(it, REQUEST_FILELOAD);
		}
	}
	
	private void doLoading(String fname) {
		Intent it = new Intent(this,DiaLoader.class);
		it.putExtra(DiaLoader.itDIR,G.sLoadDir);
		it.putExtra(DiaLoader.itFNAME,fname);
		startActivityForResult(it,REQUEST_LOADING);
	}
	
	private void doMenuNet() {
		Intent it = new Intent(this,SetNet.class);
		startActivityForResult(it,REQUEST_NETING);
	}
	
	private void doMenuDown() {
		Intent it = new Intent(this,SetDownload.class);
		startActivityForResult(it,REQUEST_DOWNING);
	}
	
	private void doMenuProj() {
		Intent it = new Intent(this,SetProj.class);
		startActivityForResult(it,REQUEST_PROJING);
	}
	
	private void doMenuAbout() {
		Intent it = new Intent(this,SetAbout.class);
		startActivity(it);
	}
	
	private void doMenuEdit() {
		Intent it = new Intent(this,EditActivity.class);
		startActivityForResult(it,REQUEST_EDITING);
	}
	
	////////////////////
	// requests
	////////////////////
	
	private void reqFileload(int resultCode, Intent data) {
		if (resultCode!=RESULT_OK) return;
		if (G.OPEN_DIA_BY_FILESELECTOR) {
			whenLoadSelected(data.getStringExtra(G.idDIR), data.getStringExtra(G.idFNAME));
		} else {
			if (data != null) {
				G.sLoadUri = data.getData();
				String fname = "???";
				Cursor cr = getContentResolver().query(G.sLoadUri,null,null,null,null,null);
				try {
					if (cr!=null && cr.moveToFirst()) {
						fname = cr.getString(cr.getColumnIndex(OpenableColumns.DISPLAY_NAME));
					}
				} finally {
					cr.close();
				}
				whenLoadSelected(null, fname);
			}
		}
	}
	
	private void reqLoading(int resultCode, Intent data) {
		boolean ok = (resultCode==RESULT_OK);
		whenLoaded(ok, ok ? data.getStringExtra(DiaLoader.itFNAME) : "");
	}
	
	private void reqNeting(int resultCode, Intent data) {
		if (resultCode==RESULT_OK) whenSetNeted();
	}
	
	private void reqDowning(int resultCode, Intent data) {
		if (resultCode==RESULT_CANCELED) return;
		whenSetDowned(resultCode==RESULT_FIRST_USER);
	}
	
	private void reqProjing(int resultCode, Intent data) {
		if (resultCode==RESULT_OK) whenSetProjed();
	}
	
	private void reqEditing(int resultCode, Intent data) {
		whenEdited();
	}
	
	////////////////////
	// hivo rurinok
	////////////////////
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inf = getMenuInflater();
		inf.inflate(R.menu.mainmenu,menu);
		MenuItem m;
		//m=menu.findItem(R.id.mnEdit);
		//m.setEnabled(false);
		m=menu.findItem(R.id.mnSave);
		m.setEnabled(false);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId()) {
			case R.id.mnLoad:
				onMenuLoad();
				return true;
			case R.id.mnEdit:
				onMenuEdit();
				return true;
			case R.id.mnSave:
				onMenuSave();
				return true;
			case R.id.mnSetDownload:
				onMenuSetDownload();
				return true;
			case R.id.mnSetNet:
				onMenuSetNet();
				return true;
			case R.id.mnSetProject:
				onMenuSetProject();
				return true;
			case R.id.mnSetAbout:
				onMenuSetAbout();
				return true;
			case R.id.mnStopProg:
				onMenuStopProg();
				return true;
			case R.id.mnStopShutdown:
				onMenuStopShutdown();
				return true;
			case R.id.mnExit:
				onMenuExit();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode==REQUEST_FILELOAD) reqFileload(resultCode, data);
		if (requestCode==REQUEST_LOADING) reqLoading(resultCode,data);
		if (requestCode==REQUEST_NETING) reqNeting(resultCode,data);
		if (requestCode==REQUEST_DOWNING) reqDowning(resultCode,data);
		if (requestCode==REQUEST_PROJING) reqProjing(resultCode,data);
		if (requestCode==REQUEST_EDITING) reqEditing(resultCode,data);
	}
}
