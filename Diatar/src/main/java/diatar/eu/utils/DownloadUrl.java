package diatar.eu.utils;

import java.util.*;
import android.content.*;
import java.io.*;
import java.lang.*;
import java.net.*;
import android.os.*;
import android.app.*;
import android.widget.ListView;

import diatar.eu.*;

/**
 * Background Async Task to download file
 * */
 
public class DownloadUrl extends AsyncTask<Void, String, Void>
{
	public boolean verbose;
	
	private ProgressDialog dlg;
	private Context ctx;
	private boolean vannaktarak;
	
	public DownloadUrl(Context context) {
		super();
		ctx=context;
	}

	@Override
	protected void onPreExecute()
	{
		super.onPreExecute();
		if (!verbose) return;
		dlg=ProgressDialog.show(ctx,
			"Énektárak keresése","https://diatar.eu",
			true,true,
			new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					cancel(false);
				}
			});
	}
	
	 private ArrayList<String> fnames,fdates;

	/**
	 * Downloading file in background thread
	* */
	@Override
	protected Void doInBackground(Void... x) {
		int count;
		vannaktarak=false;
		TxTar Dtx = TxTar.Get();
		StringBuilder sb = new StringBuilder();
		try {
			URL url = new URL("https://diatar.eu/downloads/enektarak");
			URLConnection conection = url.openConnection();
			conection.connect();

			// this will be useful so that you can show a tipical 0-100%
			// progress bar
			//int lenghtOfFile = conection.getContentLength();

			// download the file
			InputStream input = new BufferedInputStream(url.openStream(),
					8192);
			byte data[] = new byte[1024];

			while ((count = input.read(data)) != -1) {
				if (isCancelled()) break;
				for (int i=0; i<count; i++) {
					char ch = (char)data[i];
					sb.append(ch);
				}
			}

			// closing streams
			input.close();

		} catch (Exception e) {
			publishProgress("Internet probléma: "+e.getLocalizedMessage());
			return null;
		}
		
		fnames= new ArrayList<String>();
		fdates = new ArrayList<String>();
		SharedPreferences sp = ctx.getSharedPreferences("dtxs",Context.MODE_PRIVATE);
		int p=0, len=sb.length();
		while(p<len) {
			p=sb.indexOf("<a href=\"",p);
			if (p<0) break;
			p+=9;
			int p2=sb.indexOf("\"",p);
			if (p2<0) break;
			String fn=sb.substring(p,p2);
			if (fn.contains("\"")) continue;
			if (!fn.toLowerCase().endsWith(".dtx")) continue;
			p2=sb.indexOf("</a>",p2);
			if (p2<0) break;
			p2+=4;
			//while (p2<len && sb.charAt(p2)==' ') p2++;
			while(p2<len-15) {
				if (Character.isDigit(sb.charAt(p2))
					&& Character.isDigit(sb.charAt(p2+1))
					&& Character.isDigit(sb.charAt(p2+2))
					&& Character.isDigit(sb.charAt(p2+3))
					&& sb.charAt(p2+4)=='-'
					&& Character.isDigit(sb.charAt(p2+5))
					&& Character.isDigit(sb.charAt(p2+6))
					&& sb.charAt(p2+7)=='-'
					&& Character.isDigit(sb.charAt(p2+8))
					&& Character.isDigit(sb.charAt(p2+9))
					&& sb.charAt(p2+10)==' '
					&& Character.isDigit(sb.charAt(p2+11))
					&& Character.isDigit(sb.charAt(p2+12))
					&& sb.charAt(p2+13)==':'
					&& Character.isDigit(sb.charAt(p2+14))
					&& Character.isDigit(sb.charAt(p2+15))
				)
					break;
				p2++;
			}
			if (p2>=len-15) break;
			String dn=sb.substring(p2,p2+16);
			vannaktarak=true;
			if (!Dtx.hasFile(fn) ||
				!sp.getString(fn,"").equals(dn))
			{
				fnames.add(fn); fdates.add(dn);
			}
			p=p2+16;
		}
		
		return null;
	}

	@Override
	protected void onProgressUpdate(String[] values)
	{
		// TODO: Implement this method
		super.onProgressUpdate(values);
		TxTar Dtx = TxTar.Get();
		if (verbose) {
			dlg.dismiss();
			Dtx.OkBox(ctx,values[0],"Internet");
		} else
			Dtx.Msg(ctx,values[0],5000);
	}

///////////////////

	private boolean chk[];
	
	private void afterDlg() {
		fnames.remove(0);
		int i=fnames.size();
		while(i-->0) {
			if (!chk[i+1]) { fnames.remove(i); fdates.remove(i); }
		}
		if (fnames.size()<=0) {
			TxTar.Get().OkBox(ctx,"Nem maradt letöltendő!","Internet");
			return;
		}
		DownloadFiles df = new DownloadFiles(ctx);
		df.fnames=fnames; df.fdates=fdates;
		df.verbose=verbose;
		df.execute();
	}
	
	@Override
	protected void onPostExecute(Void x) {
		TxTar Dtx = TxTar.Get();
		if (verbose) dlg.dismiss();
		if (fnames==null || fnames.size()<=0) {
			if (verbose) Dtx.OkBox(ctx,
				vannaktarak ? "Minden énektár friss." : "Nincsenek letölthető énektárak",
				"");
			if (vannaktarak) {
				MainActivity ma = (MainActivity)ctx;
				ma.EndNetRefresh();
			}
			return;
		}
		fnames.add(0,"[mind / egy se]");
		String[] fn = new String[fnames.size()];
		fnames.toArray(fn);
		chk = new boolean[fnames.size()];
		for (int i=0; i<fnames.size(); i++) chk[i]=true;
		AlertDialog.Builder bld = new AlertDialog.Builder(ctx);
		bld.setPositiveButton("Letöltés", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dlg, int id) {
				//ok
				afterDlg();
			}});
		bld.setNegativeButton("Mégsem", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dlg, int id) {
				//cancel
			}});
		bld.setMultiChoiceItems(fn,chk,
			new DialogInterface.OnMultiChoiceClickListener() {
				@Override
				public void onClick(DialogInterface dlg, int ix, boolean on) {
					AlertDialog ad = (AlertDialog)dlg;
					ListView lv = ad.getListView();
					if (ix>0) {
						boolean b = true;
						for (int i=1; i<fnames.size(); i++) b = b && chk[i];
						chk[0]=b;
						lv.setItemChecked(0,b);
					} else {
						for (int i=1; i< fnames.size(); i++)
							if (chk[i]!=on) {
								chk[i]=on;
								lv.setItemChecked(i,on);
							}
					}
				}
			});
		AlertDialog dlg = bld.create();
		dlg.setTitle("Énektár frissítések elérhetők");
		dlg.show();
	}

}

