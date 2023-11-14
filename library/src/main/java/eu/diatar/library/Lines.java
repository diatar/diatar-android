package eu.diatar.library;

import android.graphics.*;
import java.util.*;

class FontStyle {
	private static final byte fsBOLD = 1;
	private static final byte fsUNDERLINE = 2;
	private static final byte fsITALIC = 4;
	private static final byte fsSTRIKE = 8;
	
	private static final byte fsARC = 16;
	private static final byte fsABEGIN = 32;
	
	private byte mStyle;
	private int mColor;
	
	public FontStyle() {
		mStyle=0;
		mColor=0;
	}

	public FontStyle(FontStyle src) {
		set(src.getBold(), src.getUnderline(), src.getItalic(), src.getStrike());
		setColor(src.getColor());
		setABegin(src.getABegin());
		setArc(src.getArc());
	}
	
	public boolean getBold() { return (mStyle&fsBOLD)!=0; }
	public boolean getUnderline() { return (mStyle&fsUNDERLINE)!=0; }
	public boolean getItalic() { return (mStyle&fsITALIC)!=0; }
	public boolean getStrike() { return (mStyle&fsSTRIKE)!=0; }
	public void setBold(boolean newval) {
		if (newval) mStyle|=fsBOLD; else mStyle&=~fsBOLD;
	}
	public void setUndeline(boolean newval) { if (newval) mStyle|=fsUNDERLINE; else mStyle&=~fsUNDERLINE; }
	public void setItalic(boolean newval) {
		if (newval) mStyle|=fsITALIC; else mStyle&=~fsITALIC;
	}
	public void setStrike(boolean newval) {
		if (newval) mStyle|=fsSTRIKE; else mStyle&=~fsSTRIKE;
	}
	public void set(boolean bd, boolean ul, boolean it, boolean st) {
		setBold(bd); setUndeline(ul); setItalic(it); setStrike(st);
	}
	
	public boolean getABegin() { return (mStyle&fsABEGIN)!=0; }
	public boolean getArc() { return (mStyle&fsARC)!=0; }
	public void setABegin(boolean abegin) {
		if (abegin) mStyle|=fsABEGIN; else mStyle&=~fsABEGIN;
	}
	public void setArc(boolean arc) {
		if (arc) mStyle|=fsARC; else mStyle&=~fsARC;
	}
	public void setArcAll(boolean abegin, boolean arc) {
		setABegin(abegin); setArc(arc);
	}

	public int getColor() { return mColor; }
	public void setColor(int newvalue) {
		if (newvalue<=0x00FFFFFF) newvalue=0;
		mColor=newvalue;
	}
	public int decodeColor(String txt) {
		if (txt.isEmpty()) return 0;
		for (int i=0; i<txt.length(); i++) {
			char c=txt.charAt(i);
			if ((c<'0'||c>'9') && (c<'A'||c>'F') && (c<'a'||c>'f'))
				return 0;
		}
		int v = Integer.valueOf(txt,16);
		return v|0xFF000000;
	}
	
	public void setColor(String txt) { setColor(decodeColor(txt)); }
}

class Word {
	//endtype
	public static final char etEOL = '\n';
	public static final char etSPACE = ' ';
	public static final char etHYPH = '-';
	public static final char etCONT = '=';
	public static final char etBRK = '.';

	public String txt;
	public float width,txtw;
	public FontStyle style;
	public char endtype;
	public Akkord akkord;
	public String kotta;
	public float keloW;  //kotta elojegyzes
	
	private static final Paint[] pcache = new Paint[8];
	private static float psize = 0f;

	public Word(String aTxt,
				FontStyle aStyle,
				char aEnd,
				Akkord aAkkord,
				String aKotta) {
		txt=aTxt;
		style = new FontStyle(aStyle);
		endtype=aEnd;
		if (aAkkord!=null) akkord = new Akkord(aAkkord);
		kotta=aKotta;
	}

	public Paint getPaint(float fsize, int fcolor, boolean allbold) {
		if (fsize!=psize) {
			for (int i=0; i<8; i++) pcache[i]=null;
			psize=fsize;
		}
		int idx = (style.getBold() || allbold ? 1 : 0)
			+(style.getItalic() ? 2 : 0)
			+(style.getUnderline() ? 4 : 0);
		if (pcache[idx]!=null) {
			Paint p=pcache[idx];
			p.setColor(fcolor);
			if (style.getStrike())
				p.setFlags(p.getFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
			else
				p.setFlags(p.getFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
			return p;
		}
		Paint p = new Paint();
		p.setTextSize(fsize);
		p.setColor(fcolor);
		int st = (style.getBold()
			? (style.getItalic() ? Typeface.BOLD_ITALIC : Typeface.BOLD)
			: (style.getItalic() ? Typeface.ITALIC : Typeface.NORMAL));
		Typeface tf = Typeface.create(p.getTypeface(),st);
		p.setTypeface(tf);
		p.setUnderlineText(style.getUnderline());
		pcache[idx]=p;
		if (style.getStrike())
			p.setFlags(p.getFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
		else
			p.setFlags(p.getFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
		return p;
	}
}

class WordGroup {
	public ArrayList<Word> mData = new ArrayList<>();
	public float width;
	public char endtype;
	public boolean brk;
	public float keloW;

	public Word add(String aTxt,
					   FontStyle aStyle,
					   char aEnd,
					   Akkord aAkkord,
					   String aKotta)
	{
		Word w = new Word(aTxt, aStyle, aEnd, aAkkord, aKotta);
		mData.add(w);
		return w;
	}
}

class Line {
	public ArrayList<WordGroup> mData = new ArrayList<>();
	public boolean mHasBrk;
}

public class Lines {
	public ArrayList<Line> mData = new ArrayList<>();
	public boolean mHasAkkord;
	public boolean mHasKotta;
	
	private static class AkkordFactory {
		private Akkord a;
		
		public void add(String s) {
			a = new Akkord(s);
		}
		
		public Akkord get() {
			Akkord res = a;
			a=null;
			return res;
		}
	}
	
	private static class KottaFactory {
		private String k;

		public void add(String s) {
			k = s;
		}

		public String get() {
			String res = k;
			k=null;
			return res;
		}
	}
	
	private static class WordFactory {
		private WordGroup wg;
		
		public void add(String aTxt,
						FontStyle aStyle,
						char aEnd,
						Akkord aAkkord,
						String aKotta)
		{
			if (wg==null) wg = new WordGroup();
			Word w = wg.add(aTxt,aStyle,aEnd,aAkkord,aKotta);
			wg.endtype=w.endtype;
		}
		
		public WordGroup get() {
			WordGroup res = wg;
			wg=null;
			return res;
		}
	}
	
	//ret: word count
	public int addLine(String src) {
		int wcnt=0;
		Line dst = new Line();
		FontStyle fs = new FontStyle();
		AkkordFactory af = new AkkordFactory();
		WordFactory wf = new WordFactory();
		KottaFactory kf = new KottaFactory();
		int p0=0;
		boolean esc = false;
		int i=0, len=src.length();
		while (i<len) {
			char ch = src.charAt(i++);
			if (esc) {
				esc=false;
				boolean bui=false;
				FontStyle ofs = new FontStyle(fs);
				if (ch=='B') { fs.setBold(true); bui=true; } else
				if (ch=='b') { fs.setBold(false); bui=true; } else
				if (ch=='U') { fs.setUndeline(true); bui=true; } else
				if (ch=='u') { fs.setUndeline(false); bui=true; } else
				if (ch=='I') { fs.setItalic(true); bui=true; } else
				if (ch=='i') { fs.setItalic(false); bui=true; } else
				if (ch=='S') { fs.setStrike(true); bui=true; } else
				if (ch=='s') { fs.setStrike(false); bui=true; }
				if (bui) {
					if (i>p0+2) {
						wf.add(src.substring(p0,i-2),ofs,Word.etCONT,af.get(),kf.get());
						fs.setABegin(false);
					}
					p0=i;
					continue;
				}
				if (ch=='(' || ch==')') {
					if (i>p0+2)
						wf.add(src.substring(p0,i-2),fs,Word.etCONT,af.get(),kf.get());
					if (ch=='(') fs.setArcAll(true,true); else fs.setArcAll(false,false);
					p0=i;
					continue;
				}
				if (ch=='-') { //felt.koto
					wf.add(src.substring(p0,i-2),fs,Word.etHYPH,af.get(),kf.get());
					fs.setABegin(false);
					dst.mData.add(wf.get());
					p0=i;
					continue;
				}
				if (ch=='_' || ch==' ') {//nemtorh.
					if (ch=='_') ch='-';
					wf.add(src.substring(p0,i-2)+ch,fs,Word.etCONT,af.get(),kf.get());
					fs.setABegin(false);
					p0=i;
					continue;
				}
				if (ch=='.') {//sortores
					wf.add(src.substring(p0,i-2),fs,Word.etBRK,af.get(),kf.get());
					fs.setABegin(false);
					dst.mData.add(wf.get()); dst.mHasBrk=true;
					p0=i;
					wcnt++;
					continue;
				}
				if (ch=='?' || ch=='G' || ch=='K') {
					if (i>p0+2)
						wf.add(src.substring(p0,i-2),fs,Word.etCONT,af.get(),kf.get());
					fs.setABegin(false);
					if (ch=='?' && i<len) ch=src.charAt(i++);
					p0=i;
					while (i<len && src.charAt(i)!=';') i++;
					if (ch=='G') {
						af.add(src.substring(p0,i));
						mHasAkkord=true;
					} else if (ch=='K') {
						kf.add(src.substring(p0,i));
						mHasKotta=true;
					} else if (ch=='C') {
						fs.setColor(src.substring(p0,i));
					}
					p0=++i;
					continue;
				}
				//minden mas karakter normalkent
				wf.add(src.substring(p0,i-2),fs,Word.etCONT,af.get(),kf.get());
				fs.setABegin(false);
				p0=i-1;
				continue;
			}
			if (ch=='\\') {
				esc=true;
				continue;
			}
			if (ch==' ') {
				wf.add(src.substring(p0,i),fs,Word.etSPACE,af.get(),kf.get());
				fs.setABegin(false);
				dst.mData.add(wf.get());
				p0=i;
				wcnt++;
				//continue;
			}
		}//for
		wf.add(src.substring(p0),fs,Word.etEOL,af.get(),kf.get());
		dst.mData.add(wf.get());
		mData.add(dst);
		if (len>0) wcnt++;
		return wcnt;
	}
}
