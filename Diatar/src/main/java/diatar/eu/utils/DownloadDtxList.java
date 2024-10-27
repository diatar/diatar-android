package diatar.eu.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import diatar.eu.TxTar;

public abstract class DownloadDtxList extends AsyncTask<Void, String, Void> {

    public DtxParams[] fparams = null;

    private final boolean mVerbose;
    private ProgressDialog mDlg;
    private final Context mCtx;

    public DownloadDtxList(Context ctx, boolean verbose) {
        mCtx = ctx;
        mVerbose = verbose;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (!mVerbose) return;
        mDlg = ProgressDialog.show(mCtx,
                "Énektárak keresése", "https://diatar.eu",
                true, true,
                new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        cancel(false);
                    }
                });
    }

    @Override
    protected Void doInBackground(Void... voids) {
        StringBuilder sb = new StringBuilder();
        try {
            URL url = new URL("https://diatar.eu/downloads/enektarak/_list.php");
            URLConnection conection = url.openConnection();
            conection.connect();
            InputStream input = new BufferedInputStream(url.openStream(), 8192);
            BufferedReader r = new BufferedReader(new InputStreamReader(input));
            byte[] data = new byte[1024];

            for (String line; (line = r.readLine()) != null; ) {
                sb.append(line).append('\n');
            }

            input.close();

        } catch (Exception e) {
            publishProgress("Internet probléma: " + e.getLocalizedMessage());
            return null;
        }

        ArrayList<DtxParams> fp = new ArrayList<>();

        //feldolgozas
        int p1=0, len = sb.length();
        while (p1<len) {
            int p2=p1;
            while(p2<len) {
                char ch=sb.charAt(p2);
                if (ch==13 || ch==10) break;
                p2++;
            }
            if (p2>p1) ProcessLine(fp,sb.substring(p1,p2));
            p1=p2+1;
        }

        //sort...
        Collections.sort(fp, new DtxParams.DtxComparator());

        if (fp.size()>0) {
            fparams = new DtxParams[fp.size()];
            fp.toArray(fparams);
        }

        return null;
    }

    private void ProcessLine(ArrayList<DtxParams> fp, String line) {
        int p1 = line.indexOf(',');
        if (p1<0) return;
        String fname = line.substring(0, p1);
        if (!fname.toLowerCase().endsWith(".dtx")) return;

        int p2 = line.indexOf(',', p1+1);
        if (p2<0) return;
        long fsize;
        try {
            fsize = Long.parseLong(line.substring(p1+1, p2));
        } catch (Exception e) {
            return;
        }

        int p3 = line.indexOf(',', p2+1);
        if (p3<0) return;
        String tstamp = line.substring(p2+1,p3);

        int p4=p3;
        int len = line.length();
        boolean inquote = false;
        StringBuilder sb = new StringBuilder();
        while (++p4<len) {
            char ch = line.charAt(p4);
            if (!inquote && ch==',') break;
            if (ch=='"') {
                if (!inquote && p4>p3+1) sb.append(ch);
                inquote=!inquote;
                continue;
            }
            if (inquote) sb.append(ch);
        }
        if (p4>=len) return;
        String grpname = sb.toString();

        int p5 = line.indexOf(',', p4+1);
        if (p5<0) return;
        int order;
        try {
            order = Integer.parseInt(line.substring(p4+1,p5));
        } catch (Exception e) {
            return;
        }

        int p6=p5;
        inquote = false;
        sb = new StringBuilder();
        while (++p6<len) {
            char ch = line.charAt(p6);
            if (!inquote && ch==',') break;
            if (ch=='"') {
                if (!inquote && p6>p5+1) sb.append(ch);
                inquote=!inquote;
                continue;
            }
            if (inquote) sb.append(ch);
        }
        String title = sb.toString();

        int p7=p6;
        inquote = false;
        sb = new StringBuilder();
        while (++p7<len) {
            char ch = line.charAt(p7);
            if (!inquote && ch==',') break;
            if (ch=='"') {
                if (!inquote && p7>p6+1) sb.append(ch);
                inquote=!inquote;
                continue;
            }
            if (inquote) sb.append(ch);
        }
        String shorttitle = sb.toString();

        DtxParams tmp = new DtxParams(fname, title, shorttitle, grpname, order, fsize, tstamp);
        fp.add(tmp);
    }

    @Override
    protected void onProgressUpdate(String[] values) {
        super.onProgressUpdate(values);
        TxTar Dtx = TxTar.Get();
        if (mVerbose) {
            mDlg.dismiss();
            TxTar.OkBox(mCtx, values[0], "Internet");
        } else
            TxTar.Msg(mCtx, values[0], 5000);
    }

    public abstract void Finished();

    @Override
    protected void onPostExecute(Void x) {
        if (mVerbose) mDlg.dismiss();
        Finished();
    }

}
