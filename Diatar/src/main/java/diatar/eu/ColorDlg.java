package diatar.eu;

import android.app.*;
import android.os.*;
import android.widget.*;
import android.view.*;
import android.widget.AdapterView.*;
import android.content.*;
import android.widget.SeekBar.*;

public class ColorDlg extends Activity {
	
	private TextView ColorOut,Rnum,Gnum,Bnum;
	private SeekBar Rseek,Gseek,Bseek;
	private Spinner PercLst;
	private int result;
	private int Ppos;
	private boolean InOnPerc;
	
	@Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

		setContentView(R.layout.colordlg);
		setTitle("Szín választása");
		
		ColorOut = (TextView)findViewById(R.id.cdColor);
		Rnum = (TextView)findViewById(R.id.cdRnum);
		Gnum = (TextView)findViewById(R.id.cdGnum);
		Bnum = (TextView)findViewById(R.id.cdBnum);
		Rseek = (SeekBar)findViewById(R.id.cdRseek);
		Gseek = (SeekBar)findViewById(R.id.cdGseek);
		Bseek = (SeekBar)findViewById(R.id.cdBseek);
		PercLst = (Spinner)findViewById(R.id.cdPercLst);
		
		Rseek.setMax(255); Gseek.setMax(255); Bseek.setMax(255);
	
		if (savedInstanceState!=null) {
			setColor(savedInstanceState.getInt(G.idCOLOR));
		} else {
			Intent it = getIntent();
			setColor(it.getIntExtra(G.idCOLOR,0));
		}

		PercLst.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View v, int pos, long id) {
				onPercLst();
			}
			public void onNothingSelected(AdapterView<?> arg0) {}
		});
			
		Rseek.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar sb, int pos, boolean isuser) {
				if (isuser) onSeek();
			}
			public void onStartTrackingTouch(SeekBar sb) {}
			public void onStopTrackingTouch(SeekBar sb) {}
		});
		Gseek.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar sb, int pos, boolean isuser) {
				if (isuser) onSeek();
			}
			public void onStartTrackingTouch(SeekBar sb) {}
			public void onStopTrackingTouch(SeekBar sb) {}
		});
		Bseek.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar sb, int pos, boolean isuser) {
				if (isuser) onSeek();
			}
			public void onStartTrackingTouch(SeekBar sb) {}
			public void onStopTrackingTouch(SeekBar sb) {}
		});

	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putInt(G.idCOLOR,result);
	}
	
	public static int getComplementer(int color) {
		int r=((color>>16)&0xFF), g=((color>>8)&0xFF), b=(color&0xFF);
		int res=(color&0xFF000000);
		if (r+g+b<128*3) res|=0x00FFFFFF;
		return res;
	}
	
	private void setColor(int NewValue) {
		NewValue&=0x00FFFFFF;
		int r=(NewValue>>16), g=((NewValue>>8)&0xFF), b=(NewValue&0xFF);
		result=NewValue;
		ColorOut.setBackgroundColor(NewValue+0xFF000000);
		ColorOut.setTextColor(getComplementer(NewValue)|0xFF000000);
		//String s = "000000"+Integer.toHexString(NewValue).toUpperCase();
		ColorOut.setText(String.format("#%06X",NewValue));   //"#"+s.substring(s.length()-6));
		Rnum.setText(String.format("%03d",r));
		Gnum.setText(String.format("%03d",g));
		Bnum.setText(String.format("%03d",b));
		Rseek.setProgress(r); Gseek.setProgress(g); Bseek.setProgress(b);
		if (!InOnPerc) {
			InOnPerc=true;
			int v = 0, c = 0;
			if (r!=0) { v+=r; c++; }
			if (g!=0) { v+=g; c++; }
			if (b!=0) { v+=b; c++; }
			if (c>1) v/=c;
			v=(10*v+128)/256;
			if (v<1) v=1; else if (v>10) v=10;
			PercLst.setSelection(v-1);
			Ppos=v-1;
			InOnPerc=false;
		}
	}
	
	public void onColorBtn(View v) {
		int c=0;
		int id=v.getId();
		if (id==R.id.cdFbtn) c=0x000000;
		else if (id==R.id.cdKbtn) c=0x0000FF;
		else if (id==R.id.cdZbtn) c=0x00FF00;
		else if (id==R.id.cdCbtn) c=0x00FFFF;
		else if (id==R.id.cdPbtn) c=0xFF0000;
		else if (id==R.id.cdLbtn) c=0xFF00FF;
		else if (id==R.id.cdSbtn) c=0xFFFF00;
		else if (id==R.id.cdHbtn) c=0xFFFFFF;
		result=c;
		if (!calcPerc()) setColor(c);
	}
	
	public void onAddSubBtn(View v) {
		int msk;
		boolean pos=true;
		int id=v.getId();
		if (id==R.id.cdRadd || id==R.id.cdRsub)
			msk=0x00FF0000;
		else if (id==R.id.cdGadd || id==R.id.cdGsub)
			msk=0x0000FF00;
		else
			msk=0x000000FF;
		if (id==R.id.cdRsub || id==R.id.cdGsub || id==R.id.cdBsub)
			pos=false;
		int add = (0x010101 & msk);
		if (pos ? (result & msk)==msk : (result & msk)==0) return;
		if (pos) result+=add; else result-=add;
		setColor(result);
	}
	
	public boolean onPercLst() {
		int ix = PercLst.getSelectedItemPosition();
		if (Ppos==ix) return false;
		Ppos=ix;
		return calcPerc();
	}
	
	public boolean calcPerc() {
		if (InOnPerc) return false;
		int ix = PercLst.getSelectedItemPosition()+1;
		int r=(result>>16), g=((result>>8)&0xFF), b=(result&0xFF);
		//ami nem nulla (fekete), annak egyszinunek kell lennie
		if (r!=0) {
			if (g!=0 && r!=g) return false;
			if (b!=0 && r!=b) return false;
		}
		if (g!=0 && b!=0 && g!=b) return false;
		InOnPerc=true;
		int perc=(255*ix)/10;
		if (r!=0) r=perc;
		if (g!=0) g=perc;
		if (b!=0) b=perc;
		setColor((r<<16)+(g<<8)+b);
		InOnPerc=false;
		return true;
	}
	
	public void onFinishBtn(View v) {
		Intent it = new Intent();
		it.putExtra(G.idCOLOR,result);
		setResult(
			v.getId()==R.id.cdOkBtn ? RESULT_OK : RESULT_CANCELED,
			it);
		finish();
	}
	
	private void onSeek() {
		setColor((Rseek.getProgress()<<16)+(Gseek.getProgress()<<8)+(Bseek.getProgress()));
	}
}
