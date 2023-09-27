package diatar.eu.utils;

import java.util.*;
import android.content.*;
import java.io.*;
import java.lang.*;
import java.net.*;
import android.os.*;
import android.app.*;
import diatar.eu.*;

public class DownloadFiles extends AsyncTask<Void, Void, String>
{
	public ArrayList<String> fnames,fdates;
	public boolean verbose;
	private ProgressDialog dlg;
	private String msgtxt;
	private int dlgpercent;
	private File dir;
	private Context ctx;
	
	public DownloadFiles(Context context) {
		super();
		ctx=context;
	}

	@Override
	protected void onPreExecute()
	{
		// TODO: Implement this method
		super.onPreExecute();
		dir = ctx.getFilesDir();
		dlg = ProgressDialog.show(ctx,
			"Énektárak letöltése","",
			true,true,
			new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					cancel(false);
				}
			});
		//dlg.setProgressStyle(dlg.STYLE_HORIZONTAL);
		//dlg.setIndeterminate(false);
		dlg.setMax(100); dlg.setProgress(0);
	}

	@Override
	protected String doInBackground(Void... x) {
		int n=fnames.size();
		for (int i=0; i<n; i++) {
			String fn=fnames.get(i);
			msgtxt=Integer.toString(i+1)+"/"+Integer.toString(n)+" "+fn;
			String res=down1File(fn,fdates.get(i));
			if (isCancelled()) return "";
			if (!res.isEmpty())
				return fn+" "+res;
		}
		return Integer.toString(n)+" fájl letöltve.";
	}
	
	private String down1File(String fname, String fdate) {
		try {
			URL url = new URL("https://diatar.eu/downloads/enektarak/"+fname);
			URLConnection conn = url.openConnection();
			conn.connect();

			File fonam = new File(dir,fname);
			File ftmp = new File(dir,"dtxtemp");
			InputStream input = new BufferedInputStream(url.openStream(),
														8192);
			FileOutputStream fops = new FileOutputStream(ftmp);
			byte data[] = new byte[8192];

			long total = 0; int count;
			long flen=conn.getContentLength();
			while ((count = input.read(data)) != -1) {
				total += count;
				dlgpercent=(int)((total*100)/flen);
				publishProgress();
				if (isCancelled()) return "X";
				fops.write(data,0,count);
			}

			// closing streams
			fops.close();
			input.close();
			
			fonam.delete();
			ftmp.renameTo(fonam);

			SharedPreferences sp = ctx.getSharedPreferences("dtxs",Context.MODE_PRIVATE);
			SharedPreferences.Editor ed = sp.edit();
			ed.putString(fname,fdate);
			ed.commit();
		} catch (Exception e) {
			return "Letöltési probléma: "+e.getLocalizedMessage();
		}
		
		return "";
	}
	
	@Override
	protected void onProgressUpdate(Void... X509CRL)
	{
		super.onProgressUpdate();
		dlg.setMessage(msgtxt+" "+Integer.toString(dlgpercent)+"%");
		dlg.setProgress(dlgpercent);
	}
	
	private void finishit(String message) {
		dlg.dismiss();
		TxTar Dtx = TxTar.Get();
		MainActivity ma = (MainActivity)(ctx);
		ma.EndNetRefresh(); ma.ReloadAll();
		if (verbose) Dtx.OkBox(ctx,message,"Internet"); else Dtx.Msg(ctx,message,5000);
	}
	
	@Override
	protected void onPostExecute(String result) {
		finishit(result);
	}

	@Override
	protected void onCancelled(String result) {
		finishit("Letöltés megszakítva.");
	}
}
