package diatar.eu.utils;

import android.content.*;
import java.io.*;
import java.lang.*;
import android.os.*;
import android.app.*;

public class CopyTree extends AsyncTask<Void, Void, String>
{
	private ProgressDialog mDlg;
	private String mMsgTxt,mErrTxt;
	private int perc0,perc1,percf,max0;
	private Context mCtx;
	public String mFromPath,mToPath;
	public boolean mCutMode;
	private String mModeTxt;
	private int mLevel;

	public interface ICallback {
		public void CutCopyFinished(String txt);
	}
	
	public CopyTree(Context context) {
		super();
		mCtx=context;
	}

	@Override
	protected void onPreExecute()
	{
		// TODO: Implement this method
		super.onPreExecute();
		mModeTxt=(mCutMode ? "Áthelyezés" : "Másolás");
		mDlg = ProgressDialog.show(mCtx,
			mModeTxt,"",
			false,true,
			new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					cancel(false);
				}
			});
			//mDlg.setProgressStyle(mDlg.STYLE_HORIZONTAL);
		mDlg.setMax(100); mDlg.setProgress(0);
		mDlg.setIndeterminate(false);
	}

	@Override
	protected String doInBackground(Void... x) {
		if (mToPath.startsWith(mFromPath))
			return "Nem tehető saját maga alá!";
		File fromFile = new File(mFromPath);
		File toFile = new File(mToPath);
		boolean res;
		if (fromFile.isFile())
			res=copyFile(fromFile,toFile);
		else
			res=recursiveCopy(fromFile,new File(toFile,fromFile.getName()));
		if (res) return mModeTxt+" sikeres.";
		return mErrTxt;
	}

	@Override
	protected void onProgressUpdate(Void... X509CRL)
	{
		super.onProgressUpdate();
		if (max0==0) max0=1;
		mDlg.setMessage(	//Integer.toString(perc0+perc1/max0)+"% "+
			mMsgTxt+" "+Integer.toString(percf)+"%");
		mDlg.setProgress(perc0+perc1/max0);
		//mDlg.setMax(100);
		//mDlg.setSecondaryProgress(percf);
	}

	@Override
	protected void onPostExecute(String result) {
		finishit(result);
	}

	@Override
	protected void onCancelled(String result) {
		finishit("Letöltés megszakítva.");
	}
	
	//////////////
	
	private void finishit(String message) {
		mDlg.dismiss();
		((ICallback)(mCtx)).CutCopyFinished(message);
	}
	
	private boolean copyFile(File fromFile, File toDir) {
		try {
			mMsgTxt=fromFile.getName();
			File newFile = new File(toDir,mMsgTxt);
			if (newFile.exists()) {
				if (!newFile.delete()) {
					mErrTxt="Korábbi fájl nem törölhető!";
					return false;
				}
			}
			if (mCutMode && fromFile.renameTo(newFile)) {
				if (mLevel<=3) publishProgress();
				return true;
			}
			//gyors modszer nem sikerult
			long fpos = 0, flen = fromFile.length();
			InputStream in = new FileInputStream(fromFile);
			OutputStream out = new FileOutputStream(newFile);
			byte[] buf = new byte[8192];
			int len;
			perc0=0; perc1=0; max0=1;
			while ((len = in.read(buf)) > 0) {
				fpos+=len;
				out.write(buf, 0, len);
				percf=(int)((100*fpos)/flen);
				publishProgress();
			}
			in.close();
			out.close();
			if (mCutMode && !fromFile.delete()) {
				mErrTxt="Fájl törlési hiba!";
				return false;
			}
		} catch (Exception e) {
			mErrTxt="Hiba: "+e.getLocalizedMessage();
			return false;
		}
		return true;
	}
	
	private boolean recursiveCopy(File fromDir, File toDir) {
		if (!toDir.exists()) {
			if (!toDir.mkdirs()) {
				mErrTxt="Mappa nem hozható létre!";
				return false;
			}
		}
		File [] flist = fromDir.listFiles();
		if (flist==null) {
			mErrTxt="Hozzáférési hiba!";
			return false;
		}
		int n = flist.length;
		if (mLevel==0) max0=n;
		mLevel++;
		try {
			for (int i=0; i<n; i++) {
				File f = flist[i];
				if (mLevel==1) perc0=(100*i)/n; else
				if (mLevel==2) perc1=(100*i)/n; else
				if (mLevel==3) percf=(100*i)/n;
				if (f.isDirectory()) {
					File newDir = new File(toDir, f.getName());
					if (!recursiveCopy(f, newDir)) return false;
				} else {
					if (!copyFile(f,toDir)) return false;
				}
			}
			if (mCutMode && !fromDir.delete()) {
				mErrTxt="Régi mappa nem törölhető!";
				return false;
			}
		} finally {
			mLevel--;
		}
		return true;
	}
}
