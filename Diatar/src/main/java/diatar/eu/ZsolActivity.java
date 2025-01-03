package diatar.eu;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.renderscript.ScriptGroup;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.text.Format;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import diatar.eu.utils.DecodeBreviar;
import diatar.eu.utils.HtmlParser;

public class ZsolActivity extends Activity
        implements ZsolLstAdapter.ItemClickListener, CalendarView.OnDateChangeListener
{
    static public class ItemData {
        public String mTitle;
        public String mFname;
    }

    private RecyclerView mZsolLst;
    private EditText mZsolState;
    private CalendarView mZsolDate;
    private ZsolLstAdapter mAdapter;
    private int mCurrYear;

    protected void onCreate(Bundle bd)
    {
        super.onCreate(bd);

        setContentView(R.layout.zsolozsma);
        mZsolState = findViewById(R.id.zsolState);
        mZsolDate = findViewById(R.id.zsolDate);
        mZsolLst = findViewById(R.id.zsolLst);

        mZsolDate.setOnDateChangeListener(this);

        mZsolLst.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new ZsolLstAdapter(this);
        mAdapter.setClickListener(this);
        mZsolLst.setAdapter(mAdapter);

        Calendar cal = Calendar.getInstance();
        mCurrYear = cal.get(Calendar.YEAR);
        mZsolDate.setDate(cal.getTimeInMillis());
        cal.set(mCurrYear-1,1,1);
        mZsolDate.setMinDate(cal.getTimeInMillis());
        cal.set(mCurrYear+1,12,12);
        mZsolDate.setMaxDate(cal.getTimeInMillis());

        FillZsolLst();
        CheckZipFiles();
    }

    //ZsoltarLst koppintas
    @Override
    public void onItemClick(View view, int position) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(mZsolDate.getDate());
        int year = cal.get(Calendar.YEAR), month = cal.get(Calendar.MONTH)+1, day = cal.get(Calendar.DAY_OF_MONTH);

        ItemData d = mAdapter.getItem(position);
        File fname = new File(TxTar.zsolozsmadir, year+".zip");
        try {
            ZipFile zip = new ZipFile(fname);
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.getName().endsWith(d.mFname)) {
                    //nap resze
                    ProcessEntry(zip.getInputStream(entry), d.mTitle);
                    return;
                }
            }
        } catch (Exception e) {
            mZsolState.setText("Hiba: "+e.getLocalizedMessage());
            //return;
        }
    }

    private void ProcessEntry(InputStream input, String title) throws IOException {
        HtmlParser.HtmlTag tag0 = HtmlParser.parseHtml(stream2string(input));
        if (tag0==null) {
            mZsolState.setText("Nem sikerült dekódolni!");
            return;
        }
        DecodeBreviar db = new DecodeBreviar(tag0);
        db.decode();
        G.sDiaFname="Zsolozsma - "+title;
        setResult(RESULT_OK);
        finish();
    }

    //DatetimePicker koppintas
    @Override
    public void onSelectedDayChange(CalendarView view, int year,
                                    int monthOfYear, int dayOfMonth) {
        FillZsolLst();
    }

    private void FillZsolLst() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(mZsolDate.getDate());
        int year = cal.get(Calendar.YEAR), month = cal.get(Calendar.MONTH)+1, day = cal.get(Calendar.DAY_OF_MONTH);

        File fname = new File(TxTar.zsolozsmadir, year+".zip");
        if (!fname.isFile() || !fname.exists()) {
            mAdapter.setData(new ArrayList<>());
            return;
        }

        //adott nap kiolvasasa
        String datestr = String.format("%02d%02d%02d.HTM", year % 100, month, day);
        try {
            ZipFile zip = new ZipFile(fname);
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.getName().toUpperCase().endsWith(datestr)) {
                    //nap resze
                    ProcessDay(zip.getInputStream(entry), datestr.substring(0,6));
                    return;
                }
            }
        } catch (Exception e) {
            mZsolState.setText("ZIP olvasási hiba: "+e.getLocalizedMessage());
            fname.delete();
            return;
        }
        mAdapter.setData(new ArrayList<>());
    }

    private String stream2string(InputStream input) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(input));
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line).append("\n");
        }
        br.close();
        return sb.toString();
    }

    private void ProcessDay(InputStream input, String yymmdd) throws IOException {
        ArrayList<ItemData> newdata = new ArrayList<>();

        HtmlParser.HtmlTag tag0 = HtmlParser.parseHtml(stream2string(input));
        if (tag0!=null) {
            HtmlParser.TagAndTxt found = tag0.traverse(tag0);
            while (found!=null && found.tag!=null) {
                if (found.tag.getNameStr().equals("a")) {
                    HtmlParser.HtmlProperty href = found.tag.findProperty("href");
                    if (href!=null && href.valueStr.contains(yymmdd)) {
                        ItemData d = new ItemData();
                        d.mFname=href.valueStr;
                        d.mTitle=(found.tag.subTags.size()>0 ? found.tag.subTags.get(0).txt : href.valueStr);
                        newdata.add(d);
                    }
                }
                found=found.tag.traverse(tag0);
            }
        }

        mAdapter.setData(newdata);
    }

    //idei, tavalyi es jovo ev kell, mas nem
    private void CheckZipFiles() {
        boolean[] yeartoread = { true, true, true }; //tavaly, iden, jovore
        //atnezzuk a zsolozsma konyvtarat: csak a harom eeee.zip fajl lehet ott, minden mast torlunk
        File dir = new File(TxTar.zsolozsmadir);
        dir.mkdirs();
        for (String fname : dir.list()) {
            boolean keep=(fname.length()==8);
            if (keep && !fname.endsWith(".zip")) keep=false;
            if (keep) {
                int ev = 0;
                try {
                    ev = Integer.parseInt(fname.substring(0, 4));
                } catch(NumberFormatException e) {
                    keep = false;
                }
                if (keep && (ev<mCurrYear-1 || ev>mCurrYear+1)) keep=false;
                if (keep) yeartoread[ev-(mCurrYear-1)]=false;
            }

            if (!keep) {
                File f = new File(fname);
                f.delete();
            }
        }

        //letoltjuk a szokseges eveket
        int[] AIDX = {1,0,2};
        for (int i=0; i<3; i++) {
            int idx = AIDX[i];
            if (!yeartoread[idx]) continue;
            DownloadZip dlz = new DownloadZip(mCurrYear-1+idx) {
                @Override
                public void Finished(String err, int year) {
                    if (!err.isEmpty()) {
                        if (year==mCurrYear) mZsolState.setText(err);
                        return;
                    }
                    FillZsolLst();
                }
            };
            dlz.execute();
        }
    }
}

////////////////////////////////////////////////////////////////

class ZsolLstAdapter extends RecyclerView.Adapter<ZsolLstAdapter.ViewHolder> {
    private LayoutInflater mInflater;
    private ArrayList<ZsolActivity.ItemData> mData = new ArrayList<>();
    private ItemClickListener mClickListener;

    //ez egy sor taroloja
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
        }

        public void setText(String txt) {
            TextView tv = itemView.findViewById(R.id.zsollstitemTxt);
            tv.setText(txt);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    ZsolLstAdapter(Context ctx) {
        mInflater=LayoutInflater.from(ctx);
    }

    //uj adatsor - nem kezdjuk ujra a listat, inkabb modositjuk
    public void setData(ArrayList<ZsolActivity.ItemData> data) {
        int srcsize = mData.size(), dstsize = data.size();
        int n = Math.max(srcsize, dstsize);
        mData = data;
        for (int i=0; i<n; i++) {
            if (i>=srcsize) notifyItemInserted(i);
            else if (i>=dstsize) notifyItemRemoved(i);
            else notifyItemChanged(i);
        }
    }

    //letrehozas
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.zsollstitem, parent, false);
        return new ViewHolder(view);
    }

    //feltoltes
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String title = mData.get(position).mTitle;
        holder.setText(title);
    }

    //tetelszam
    @Override
    public int getItemCount() {
        return mData.size();
    }

    //egy tetel
    public ZsolActivity.ItemData getItem(int idx) {
        return mData.get(idx);
    }

    //rakoppintas kezeloje
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    //ezt majd a szulo Activity-ben kezeljuk
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}

////////////////////////////////////////////////////////////////

abstract class DownloadZip extends AsyncTask<Void, Void, String> {
    private int mYear;

    DownloadZip(int year) {
        mYear=year;
    }
    @Override
    protected void onPreExecute() {}

    @Override
    protected String doInBackground(Void...x) {
        String yearstr = Integer.toString(mYear);
        String srcname = yearstr+"-hu-plain.zip";
        String dstname = yearstr+".zip";

        File fout = null;
        try {
            URL url = new URL("https://breviar.sk/download/" + srcname);
            URLConnection conection = url.openConnection();
            conection.connect();

            fout = new File(TxTar.zsolozsmadir,dstname);
            File ftmp = new File(TxTar.zsolozsmadir,"ziptemp"+yearstr);
            InputStream input = new BufferedInputStream(url.openStream(), 8192);
            FileOutputStream output = new FileOutputStream(ftmp);
            byte data[] = new byte[8192];
            int count;
            while ((count = input.read(data)) != -1) {
                output.write(data,0,count);
            }

            // closing streams
            output.close();
            input.close();

            fout.delete();
            ftmp.renameTo(fout);
            fout=null;
        } catch (Exception e) {
            if (fout!=null) fout.delete();
            return yearstr+" letöltése sikertelen: "+e.getLocalizedMessage();
        }
        return "";
    }

    @Override
    protected void onPostExecute(String result) {
        Finished(result, mYear);
    }

    public abstract void Finished(String err, int year);

}