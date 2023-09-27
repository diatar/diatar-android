package diatar.eu;

import android.app.*;
import android.os.*;
import android.widget.*;
import android.view.*;
import android.widget.AdapterView.*;
import android.content.*;
import java.lang.*;
import java.io.*;
import java.util.*;
import android.*;
import diatar.eu.utils.*;

import androidx.core.app.ActivityCompat;

public class FileSelectorActivity extends Activity
	implements PopupMenu.OnMenuItemClickListener
		, CopyTree.ICallback
{
	public static final int ftDIA = 1;
	public static final int ftPIC = 2;
	public static final int ftDIR = 3;
	
	private ListView Lst;
	private TextView DirTxt;
	private String mCurrDir;
	private EditText Editor;
	private Button OkBtn;
	private boolean mSaveMode;  //false=load
	private int mFType;
	static private int mActItem;
	private String mCopyPath;
	private boolean mCutMode;	//true=cut, false=copy
	private ArrayList<String> mDirLst;
	//private ArrayAdapter<String> LstAdapter;
	private FSAdapter LstAdapter;
	
	//list types
	private static final char ltROOT = 'A';
	private static final char ltDOCS = 'B';
	private static final char ltCOMMON = 'C';
	private static final char ltEXTERN = 'D';
	private static final char ltAPPSPEC = 'E';
	private static final char ltUPDIR = 'F';
	private static final char ltDIR = 'G';
	private static final char ltDIA = 'H';
	private static final char ltPIC = 'I';
	
	public String[] LstIcons;
	
	private static final String[] sExts = {
		".BMP", ".GIF", ".JPG", ".JPEG", ".PNG", ".WEBP",
		".HEIC", ".HEIF"
	};
	
	private boolean filterExt(String fname) {
		if (mFType==ftDIR) return false;
		String ufname = fname.toUpperCase();
		if (mFType==ftDIA) {
			return ufname.endsWith(".DIA");
		}
		//ftPIC
		for (String ext : sExts)
			if (ufname.endsWith(ext)) return true;
		return false;
	}
	
	static String AddExt(String s, String ext) {
		if (s.toUpperCase().endsWith(ext)) return s;
		return s+ext;
	}
	
	static String SubExt(String s, String ext) {
		if (s.toUpperCase().endsWith(ext)) return s.substring(0,s.length()-ext.length());
		return s;
	}
	
	static String AddDiaExt(String s) {
		return AddExt(s,".DIA");
	}
	
	static String SubDiaExt(String s) {
		return SubExt(s,".DIA");
	}
	
	@Override
    protected void onCreate(Bundle bd) {
        super.onCreate(bd);

		ActivityCompat.requestPermissions(
			this,
			new String[]{
				Manifest.permission.WRITE_EXTERNAL_STORAGE,
				Manifest.permission.MANAGE_EXTERNAL_STORAGE
			},
			1);
		
		setContentView(R.layout.fileselector);
		Lst = findViewById(R.id.fsLst);
		DirTxt = findViewById(R.id.fsDir);
		Editor = findViewById(R.id.fsEdit);
		OkBtn = findViewById(R.id.fsOkBtn);
		
		if (bd==null) bd=getIntent().getExtras();
		mActItem=bd.getInt(G.idINDEX,0);
		mFType=bd.getInt(G.idFTYPE,ftDIA);
		mSaveMode=bd.getBoolean(G.idSAVEMODE,false);
		mCutMode=bd.getBoolean(G.idTYPE,false);
		mCopyPath=bd.getString(G.idSEL);
		fillIcons();
		String origdir = bd.getString(G.idDIR);
		if (mSaveMode) {
			String fname = bd.getString(G.idFNAME);
			if (mFType==ftDIA) fname=SubDiaExt(fname);
			Editor.setText(fname,TextView.BufferType.EDITABLE);
		} else {
			Editor.setVisibility(View.GONE);
			LinearLayout ll = findViewById(R.id.fsFnameBox);
			ll.setVisibility(View.GONE);
			if (mFType!=ftDIR)
				OkBtn.setVisibility(View.GONE);
		}
		
		mCurrDir="";
		mDirLst = new ArrayList<String>();
		mDirLst.add("#");
		LstAdapter = new FSAdapter(this,mDirLst);
		Lst.setAdapter(LstAdapter);
		Lst.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> arg0, View v, int pos, long id) {
					onLstClick(pos);
				}
			});
		Lst.setOnItemLongClickListener(new OnItemLongClickListener() {
				@Override
				public boolean onItemLongClick(AdapterView<?> arg0, View v, int pos, long id) {
					onLstLongClick(v,pos);
					return true;
				}
			});
			
		if (!setDir(origdir) && !setDir("/")) {
			File f = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
			f.mkdirs();
			setDir(f.getAbsolutePath()+"/");
		}
	};
	
	@Override
	protected void onSaveInstanceState(Bundle bd)
	{
		bd.putBoolean(G.idSAVEMODE,mSaveMode);
		bd.putInt(G.idINDEX,mActItem);
		bd.putInt(G.idFTYPE,mFType);
		bd.putString(G.idDIR,mCurrDir);
		bd.putString(G.idFNAME,Editor.getText().toString());
		bd.putBoolean(G.idTYPE,mCutMode);
		bd.putString(G.idSEL,mCopyPath);
	}
	
	private void fillIcons() {
		LstIcons = new String[256];
		LstIcons[ltROOT] = new String(Character.toChars(0x26EA));
		LstIcons[ltDOCS] = new String(Character.toChars(0x270F));
		LstIcons[ltCOMMON] = new String(Character.toChars(0x263A));
		LstIcons[ltEXTERN] = new String(Character.toChars(0x1F4BE/*0x2708*/));
		LstIcons[ltAPPSPEC] = new String(Character.toChars(0x1F4BC));
		LstIcons[ltUPDIR] = new String(Character.toChars(0x261D));
		LstIcons[ltDIR] = new String(Character.toChars(0x1F4C1/*0x27AA*/));
		LstIcons[ltDIA] = new String(Character.toChars(0x2627));
		LstIcons[ltPIC] = new String(Character.toChars(0x26F1 /*0x1F304*/ ));
	}
	
	private void HappyEnd(String fname) {
		Intent it = new Intent();
		it.putExtra(G.idDIR,mCurrDir);
		it.putExtra(G.idFNAME,fname);
		setResult(RESULT_OK,it);
		finish();
	}
	
	private final static String fnCOMMON = ltCOMMON+" <Közös fájlok>";
	private final static String fnDOCS = ltDOCS+" <Dokumentumok>";
	private final static String fnAPPSPEC = ltAPPSPEC+" <App.spec fájlok>";
	
	private boolean setDir(String newdir) {
		//Editor.setText("");
		if (!newdir.endsWith("/")) newdir=newdir+"/";
		File dir = new File(newdir);
		String[] flst = dir.list();
		if (flst==null) {
			TxTar.Msg(this,"Nem hozzáférhető a könyvtár:\n"+newdir);
			return false;
		}
		mCurrDir=newdir;
		DirTxt.setText(mCurrDir);
		mDirLst.clear();
		if (mCurrDir.equals("/")) {
			mDirLst.add(fnCOMMON);
			mDirLst.add(fnDOCS);
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
				mDirLst.add(fnAPPSPEC);
			String xstor = System.getenv("SECONDARY_STORAGE");
			if (xstor!=null) {
				String[] xlst = xstor.split(File.pathSeparator);
				for (String x : xlst) {
					mDirLst.add(ltEXTERN+" <"+x+">");	//\u2708
				}
			}
		} else {
			mDirLst.add(ltROOT+" [/]");	//\u26EA
			mDirLst.add(ltUPDIR+" [..]");	//\u261D
		}
		for (String fname : flst) {
			File f = new File(dir,fname);
			if (f.isDirectory()) {
				mDirLst.add(ltDIR+" ["+fname+"]");	//\u27AA
			}
		}
		if (mFType!=ftDIR)
			for (String fname : flst) {
				if (!filterExt(fname)) continue;
				File f = new File(dir,fname);
				if (f.isDirectory()) continue;
				mDirLst.add((mFType==ftDIA ? ltDIA : ltPIC)+" "+fname);	//\u2627
			}
		LstAdapter.notifyDataSetChanged();
		return true;
	}
	
	private void onLstClick(int ix) {
		String line = mDirLst.get(ix);
		String s=line.substring(2);
		if (s.charAt(0)=='[') {
			s=s.substring(1,s.length()-1);
			if (s.equals("/")) {
				setDir("/");
			} else if (s.equals("..")) {
				s=mCurrDir.substring(0,mCurrDir.length()-1);
				ix=s.lastIndexOf('/');
				if (ix<0) s="/"; else s=s.substring(0,ix+1);
				setDir(s);
			} else {
				setDir(mCurrDir+s+"/");
			}
			return;
		}
		if (line.equals(fnCOMMON)) {
			File f = Environment.getExternalStorageDirectory();
			f.mkdirs();
			setDir(f.getAbsolutePath()+"/");
			return;
		} else if (line.equals(fnDOCS)) {
			File f = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
			f.mkdirs();
			setDir(f.getAbsolutePath()+"/");
			return;
		} else if (line.equals(fnAPPSPEC)) {
			setDir(TxTar.Get().appspecdir);
			return;
		} else if (s.startsWith("<")) {
			s=s.substring(1,s.length()-1);
			setDir(s);
			return;
		}
		if (mSaveMode) {
			Editor.setText(SubDiaExt(s));
			return;
		}
		HappyEnd(s);
	}
	
	private void onLstLongClick(View v,int ix) {
		mActItem=ix;
		PopupMenu pm = new PopupMenu(this,v);
		pm.inflate(R.menu.filelongmenu);
		pm.setOnMenuItemClickListener(this);
		pm.show();
	}
		
	private void doRen() {
		String line = mDirLst.get(mActItem);
		String s=line.substring(2);
		boolean isdir = (s.charAt(0)=='[');
		if (isdir) {
			s=s.substring(1,s.length()-1);
			if (s.equals("/") || s.equals("..")) return;
		} else {
			if (line.equals(fnCOMMON)) return;
			if (line.equals(fnDOCS)) return;
			if (s.startsWith("<")) return;
			if (mFType==ftDIA) s=SubDiaExt(s);
		}
		Intent it = new Intent(this, RenameFile.class);
		it.putExtra(G.idFNAME,s);
		it.putExtra(G.idISDIR,isdir);
		startActivityForResult(it,REQUEST_RENAME);
		return;
	}
	
	private void doNew() {
		Intent it = new Intent(this, RenameFile.class);
		startActivityForResult(it,REQUEST_NEW);
		return;
	}
	
	private void doDel() {
		String line = mDirLst.get(mActItem);
		String s=line.substring(2);
		boolean isdir = (s.charAt(0)=='[');
		if (isdir) {
			String sd=s.substring(1,s.length()-1);
			if (sd.equals("/") || sd.equals("..")) return;
			Intent it = new Intent(this,AskYesNo.class);
			it.putExtra(G.idTXT,s+" könyvtár mindenestől törölhető?");
			startActivityForResult(it,REQUEST_ASKDEL);
			return;
		}
		if (line.equals(fnCOMMON)) return;
		if (line.equals(fnDOCS)) return;
		if (s.startsWith("<")) return;
		//if (mFType==ftDIA) s=SubDiaExt(s);
		File f = new File(mCurrDir,s);
		if (f.delete()) {
			TxTar.Msg(this,s+" törölve.");
			mDirLst.remove(mActItem);
			LstAdapter.notifyDataSetChanged();
		} else
			TxTar.Msg(this,s+" törlése nem sikerült!");
	}
	
	///////////////////
	
	private String commonCutCopy() {
		if (mActItem <0 || mActItem>=mDirLst.size()) return "Hibás index!";
		String line = mDirLst.get(mActItem);
		String s=line.substring(2);
		boolean isdir = (s.charAt(0)=='[');
		if (isdir) {
			String sd=s.substring(1,s.length()-1);
			if (sd.equals("/") || sd.equals("..")) return "Spec.könyvtár!";
			mCopyPath=mCurrDir+sd+"/";
			return "";
		}
		if (line.equals(fnCOMMON) ||
		    line.equals(fnDOCS) ||
		    s.startsWith("<")) return "Spec.könyvtár!";
		mCopyPath=mCurrDir+s;
		return "";
	}
	
	private void doCutCopy(boolean cutmode, String txt) {
		String err=commonCutCopy();
		if (!err.isEmpty()) {
			TxTar.Get().Msg(this,txt+" sikertelen:\n"+err);
			return;
		}
		mCutMode=cutmode;
		TxTar.Get().Msg(this,txt+" előkészítve:\n"+mCopyPath);
	}
	
	private void doCut() { doCutCopy(true,"Áthelyezés"); }
	
	private void doCopy() { doCutCopy(false,"Másolás"); }
	
	private void doInsert() {
		if (mCopyPath==null || mCopyPath.isEmpty()) {
			TxTar.Get().Msg(this,"Nincs mit beilleszteni!");
			return;
		}
		CopyTree ct = new CopyTree(this);
		ct.mCutMode=mCutMode;
		ct.mFromPath=mCopyPath;
		ct.mToPath=mCurrDir;
		ct.execute();
		if (mCutMode) mCopyPath="";
	}
	
	//callback
	public void CutCopyFinished(String txt) {
		setDir(mCurrDir);
		TxTar Dtx = TxTar.Get();
		Dtx.OkBox(this,txt,"fájl");
	}

	///////////////////
	
	private final int REQUEST_OVERWRITE = 1;
	private final int REQUEST_RENAME = 2;
	private final int REQUEST_ASKDEL = 3;
	private final int REQUEST_NEW = 4;
	
	public void onOkBtn(View v) {
		if (mFType==ftDIR) {
			HappyEnd("");
			return;
		}
		String s = SubDiaExt(Editor.getText().toString());
		if (s.isEmpty()) {
			TxTar.Msg(this,"Adjon meg nevet!");
			return;
		}
		File f = new File(mCurrDir,AddDiaExt(s));
		if (f.exists()) {
			Intent it = new Intent(this, AskYesNo.class);
			it.putExtra(G.idTXT,AddDiaExt(s)+"\nlétezik. Felülírható?");
			startActivityForResult(it,REQUEST_OVERWRITE);
			return;
		}
		HappyEnd(AddDiaExt(s));
	}

	public void onCancel(View v) {
		setResult(RESULT_CANCELED);
		finish();
	}
	
	@Override
	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.mnFileLongNew:
			doNew();
			return true;
		case R.id.mnFileLongDel:
			doDel();
			return true;
		case R.id.mnFileLongRen:
			doRen();
			return true;
		case R.id.mnFileLongCut:
			doCut();
			return true;
		case R.id.mnFileLongCopy:
			doCopy();
			return true;
		case R.id.mnFileLongInsert:
			doInsert();
			return true;
		}
		return false;
	}
	
	////////////////////
	// requests
	////////////////////
	
	private void reqOverwrite(int resultCode, Intent data) {
		if (resultCode!=RESULT_OK) return;
		String s = AddDiaExt(Editor.getText().toString());
		HappyEnd(s);
	}
	
	private void reqRename(int resultCode, Intent data) {
		if (resultCode!=RESULT_OK) return;
		boolean isdir = data.getBooleanExtra(G.idISDIR,false);
		String oname = data.getStringExtra(G.idORIG);
		String nname = data.getStringExtra(G.idFNAME);
		if (!isdir && mFType==ftDIA) {
			oname=AddDiaExt(oname);
			nname=AddDiaExt(nname);
		}
		File forig = new File(mCurrDir,oname);
		File fnew = new File(mCurrDir,nname);
		if (forig.renameTo(fnew)) {
			TxTar.Msg(this,"Átnevezve: "+nname);
			mDirLst.set(mActItem,mDirLst.get(mActItem).substring(0,2)
				+ (isdir ? "[" : "") + nname + (isdir ? "]" : ""));
			LstAdapter.notifyDataSetChanged();
		} else {
			TxTar.Msg(this,"Átnevezés nem sikerült!!!\n");
		}
		return;
	}
	
	private boolean delDir(String dirname) {
		if (!dirname.endsWith("/")) dirname+="/";
		File dir = new File(dirname);
		String[] lst = dir.list();
		for (String fn : lst) {
			if (fn=="." || fn=="..") continue;
			File f = new File(dirname,fn);
			if (f.isDirectory()) {
				if (!delDir(dirname+fn))
					return false;
			} else {
				if (!f.delete())
					return false;
			}
		}
		return dir.delete();
	}
	
	private void reqDel(int resultCode, Intent data) {
		if (resultCode!=RESULT_OK) return;
		String line = mDirLst.get(mActItem);
		String s=line.substring(2);
		String sd=s.substring(1,s.length()-1);
		File f = new File(mCurrDir+sd);
		if (delDir(mCurrDir+sd)) {
			TxTar.Msg(this,s+" törölve.");
			mDirLst.remove(mActItem);
			LstAdapter.notifyDataSetChanged();
		} else
			TxTar.Msg(this,s+" törlése nem sikerült!");
	};
	
	private void reqNew(int resultCode, Intent data) {
		if (resultCode!=RESULT_OK) return;
		String s = data.getStringExtra(G.idFNAME);
		if (s.isEmpty())
			return;
		File f = new File(mCurrDir,s);
		if (f.exists() || f.isFile() || f.isDirectory()) {
			TxTar.Msg(this,"Fájl/mappa létezik!");
			return;
		}
		f = new File(mCurrDir+s);
		if (!f.mkdirs()) {
			TxTar.Msg(this,"Nem hozható létre!");
			return;
		}
		setDir(mCurrDir+s+"/");
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode==REQUEST_OVERWRITE) reqOverwrite(resultCode,data);
		if (requestCode==REQUEST_RENAME) reqRename(resultCode,data);
		if (requestCode==REQUEST_NEW) reqNew(resultCode,data);
		if (requestCode==REQUEST_ASKDEL) reqDel(resultCode,data);
	}
}

////////////////////////
////////////////////////

class FSAdapter extends BaseAdapter {
	private FileSelectorActivity mCtx;
	private ArrayList<String> mData;
	
	public FSAdapter(FileSelectorActivity ctx, ArrayList<String> data) {
		mCtx=ctx;
		mData=data;
	}
	
	public int getCount() {
		return mData.size();
	}
	
	public Object getItem(int arg) {
		return null;
	}
	
	public long getItemId(int pos) {
		return pos;
	}
	
	public View getView(int pos, View cv, ViewGroup parent) {
		if (cv==null) {
			LayoutInflater li = (LayoutInflater)mCtx.getSystemService(mCtx.LAYOUT_INFLATER_SERVICE);
			cv = li.inflate(R.layout.fslistitem,parent,false);
		}
		TextView t1 = cv.findViewById(R.id.fsRowType);
		TextView t2 = cv.findViewById(R.id.fsRowText);
		String s = mData.get(pos);
		char ch = s.charAt(0);
		String icon = "?";
		if (ch<256) icon=mCtx.LstIcons[ch];
		t1.setText(icon);
		t2.setText(s.substring(1));
		return cv;
	}
}
