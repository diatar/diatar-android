package eu.diatar.library;

import android.graphics.*;

public class Akkord
{
	static private final byte hangIS = (byte)0x10;
	static private final byte hangES = (byte)0x20;
	static private final byte hangMOLL = (byte)0x40;
	
	private byte mHang1, mHang2;
	private int mModosito;
	
//akkord-modositok a diatarakban
	static private final String AkkordModArray[] = {
	    "",     "#",    "o",    "7",    "7+",   "o7",   "o7-",   "o7+",   "#7",
	    "#7+",  "6",    "79",   "79-",  "79+",  "#79",  "#79+",  "7+9",   "7+9+",
	    "#7+9", "#7+9+","o79",  "o79-", "9",    "9-",   "9+",    "#9",    "#9+",
	    "o9",   "o9-",  "4",    "2",    "47",   "49",   "49-",   "49+"
	};
	
//akkord-modositok a kepernyon
	static private final String AkkordOutputArray[] = {
	    "",     "+",    "o",    "7",    "7+",   "o/7",  "o/7-",  "o/7+",  "+/7",
	    "+/7+", "6",    "7/9",  "7/9-", "7/9+", "+/7/9","+/7/9+","7+/9",  "7+/9+",
	    "+/7+/9","+/7+/9+","o/7/9","o/7/9-","9","9-",   "9+",    "+/9",    "+/9+",
	    "o/9",  "o/9-", "4",    "2",    "4/7",  "4/9", "4/9-",  "4/9+"
	};
	
	private byte toHang(String s) {
		int res;
		if (s.length()==0) return (byte)0;
		switch(s.charAt(0)) {
			case 'c':
			case 'C':
				res=1;
				break;
			case 'd':
			case 'D':
				res=2;
				break;
			case 'e':
			case 'E':
				res=3;
				break;
			case 'f':
			case 'F':
				res=4;
				break;
			case 'g':
			case 'G':
				res=5;
				break;
			case 'a':
			case 'A':
				res=6;
				break;
			case 'b':
			case 'B':
			case 'h':
			case 'H':
				res=7;
				break;
			default:
				return (byte)0;
		}
		if (s.length()<2) return (byte)res;
		int p = 1;
		if (s.charAt(p)=='-') { res|=hangES; p++; } else
		if (s.charAt(p)=='+') { res|=hangIS; p++; }
		if (p<s.length() && s.charAt(p)=='m') { res|=hangMOLL; p++; }
		//if (p<s.length()) res=0;
		return (byte)res;
	}
	
	public Akkord(String str) {
		int p = str.indexOf('/');
		String s1 = str;
		mHang2=(byte)0;
		if (p>=0) {
			mHang2=toHang(str.substring(p+1));
			s1=str.substring(0,p);
		}
		mHang1=toHang(s1);
		mModosito=-1;
		if (mHang1==(byte)0) return;
		p=1;
		if ((mHang1 & (hangES|hangIS))!=0) p++;
		if ((mHang1 & hangMOLL)!=0) p++;
		s1=s1.substring(p);
		mModosito=AkkordModArray.length;
		while(mModosito-->0 &&
			AkkordModArray[mModosito].compareToIgnoreCase(s1)!=0)
		;
	}
	
	private String toStr(byte Hang) {
		final String abc = "CDEFGAH";
		
		int h = (Hang & 7);
		if (h==0) return "";
		String res = abc.substring(h-1,h);
		if ((Hang & hangES)!=0) {
			if (h==7) res="B";
			else if (h==3 || h==6) res+="s";
			else res+="es";
		} else if ((Hang & hangIS)!=0) {
			res+="is";
		}
		if ((Hang & hangMOLL)!=0)
			res = res.toLowerCase();
		
		return res;
	}
	
	public Akkord(Akkord src) {
		mHang1=src.mHang1;
		mHang2=src.mHang2;
		mModosito=src.mModosito;
	}
	
	public void Draw(Canvas canvas, Paint p, float x, float y)
	{
		String s1 = toStr(mHang1), s2 = toStr(mHang2);
		if (s1.isEmpty()) return;
		Typeface tf = p.getTypeface();
		p.setTypeface(Typeface.create(tf,Typeface.BOLD));
		canvas.drawText(s1,x,y,p);
		x+=p.measureText(s1);
		p.setTypeface(tf);
		if ((mHang1&hangMOLL)!=0) {
			canvas.drawText("m",x,y,p);
			x+=p.measureText("m");
		}
		if (mModosito>0) {
			Paint pup = new Paint(p);
			pup.setTextSize(pup.getTextSize()*0.7f);
			canvas.drawText(AkkordOutputArray[mModosito],
				x,y+p.ascent()-pup.ascent(),
				pup);
			x+=pup.measureText(AkkordOutputArray[mModosito]);
		}
		if (!s2.isEmpty()) {
			canvas.drawText("/"+s2,x,y,p);
		}
	}
	
	public float CalcWidth(float fsize) {
		String s1 = toStr(mHang1), s2 = toStr(mHang2);
		Paint p = new Paint();
		p.setTextSize(fsize);
		Typeface tf = p.getTypeface();
		p.setTypeface(Typeface.create(tf,Typeface.BOLD));
		float res = p.measureText(s1);
		p.setTypeface(tf);
		if ((mHang1&hangMOLL)!=0) res+=p.measureText("m");
		if (!s2.isEmpty())
			res += p.measureText("/")+p.measureText(s2);
		if (mModosito>0) {
			p.setTextSize(fsize*0.7f);
			res+=p.measureText(AkkordOutputArray[mModosito]);
		}
		return res;
	}
	
}
