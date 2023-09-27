package eu.diatar.library;

import android.graphics.*;
import android.graphics.drawable.*;
import android.content.res.*;
import java.util.*;

public class Kotta
{
	public class KottaState {
		public byte Vonalszam = 5;
		public char Kulcs = ' ';
		public int Elojegy = 0; //-7..+7
		public char Modosito = ' ';
		public char Ritmus = '4';
		public boolean Pontozott = false;
		public boolean Tomor = false;
		public boolean Gerenda = false;
		public boolean Szaaratlan = false;
		public char Agogika = ' ';
		public float NeumaX =0.0f, NeumaY = 0.0f;
		public float IvStartX = 0f, IvStartY = 0f;
		public float IvEndX = 0f, IvEndY = 0f;
		public float IvBalX = -1f, IvMaxY = -1f;
		public char IvTipus = ' ', IvTipusLesz = ' ';
		public boolean TriLe = false;
		public char TriTipus = ' ';
		public List<PointF> TriPos = new ArrayList<PointF>();
	}
	
	private Resources mRes;
	private Canvas mCanvas;
	private Paint mPaint;
	private float mX0, mY0;
	public float mX;
	public float mPostX; //elojegyzes, kulcs, metrum utan
	private int mColor;
	
	private float mMinWidth;
	private float mRat;
	private int mVonalSzel;
	public KottaState mState = new KottaState();
	
	private class Gerenda {
		public RectF mFej;
		public boolean mLe;
		public char mRitmus;
	}
	private static final int MAXGERENDA = 4;
	private Gerenda mGer[] = new Gerenda[MAXGERENDA];
	private int mGerIdx;
	
	//0 felso hatar, 1-2 felso pot, 3-7 vonal
	// 8-9 also pot, 10 also hatar
	private float mLineY[] = new float[11];
	
	public Kotta() {
		for (int i=0; i<MAXGERENDA; i++)
			mGer[i] = new Gerenda();
	}
	
	public void setHeight(float height) {
		for (int i=0; i<11; i++)
			mLineY[i]=i*height/10.0f;
		mMinWidth=KottaConst.Hang4fejWIDTH*mLineY[1]/
			(KottaConst.Hang4Vonal2aY-KottaConst.Hang4Vonal2fY);
		mRat=mLineY[1]/(KottaConst.Hang4Vonal2aY-KottaConst.Hang4Vonal2fY);
		mVonalSzel=(int)(1.0f+mRat*KottaConst.ZaszloSzelesseg);
	}
	
	public void reset() {
		KottaState oldstate = mState;
		mState = new KottaState();
		mState.Kulcs=oldstate.Kulcs;
		mState.Elojegy=oldstate.Elojegy;
		mState.Vonalszam=oldstate.Vonalszam;
	}
	
	public float getWidth(String s) {
		int l = s.length();
		float res=0.0f;
		mPostX=0f;
		for (int i=1; i<l; i+=2) {
			char c1=s.charAt(i-1), c2=s.charAt(i);
			res+=widthOf(c1,c2);
			if (c1=='k'||c1=='e'||c1=='E'||c1=='u'||c1=='U')
				mPostX=res;
		}
		return res;
	}
	
	public float widthOf(char c1, char c2) {
		switch(c1) {
			case 'k': //kulcs
				mState.Kulcs=c2;
				switch(c2) {
					case 'G': return mMinWidth+KottaConst.GkulcsWIDTH*
						(mLineY[7]-mLineY[3])/
						(KottaConst.GkulcsVonal1Y-KottaConst.GkulcsVonal5Y);
					case 'F': return mMinWidth+KottaConst.FkulcsWIDTH*
						(mLineY[7]-mLineY[3])/
						(KottaConst.FkulcsVonal1Y-KottaConst.FkulcsVonal5Y);
					case '1':
					case '2':
					case '3':
					case '4':
					case '5':
						return mMinWidth+KottaConst.CkulcsWIDTH*
							(mLineY[7]-mLineY[3])/
							(KottaConst.CkulcsVonal1Y-KottaConst.CkulcsVonal5Y);
					case 'a': case 'b': case 'c':
					case 'd': case 'e': case 'f':
					case 'g': case 'h': case 'i':
						return mMinWidth+KottaConst.DkulcsWIDTH*
							(mLineY[7]-mLineY[6])/
							(KottaConst.DkulcsVonal1Y-KottaConst.DkulcsVonal2Y);
				}
				return 0.0f;
			case 'e': //bes elojegyzes
				mState.Elojegy=(byte)('0'-c2);
				return (1+c2-'0')*KottaConst.BeWIDTH*
					mLineY[1]/(KottaConst.BeVonal2aY-KottaConst.BeVonal2fY)/1.5f;
			case 'E': //keresztes
				mState.Elojegy=(byte)(c2-'0');
				return (1+c2-'0')*KottaConst.KeresztWIDTH*
					mLineY[1]/(KottaConst.KeresztVonal2aY-KottaConst.KeresztVonal2fY)/1.5f;
			case 'u': //utemjel
			case 'U': //spec.utemjel
				return mMinWidth+KottaConst.U22WIDTH*
					(mLineY[7]-mLineY[3])/KottaConst.U22HEIGHT;
			case '|': //utemvonal
				switch(c2) {
					case ':': return 3*mMinWidth;
					case '<':
					case '>':
						return 2*mMinWidth;
				}
				return mMinWidth;
			case 'm': //modositojel
				mState.Modosito=c2;
				return 0.0f;
			case 's': //szunet
				mState.Modosito=' ';
				switch(c2) {
					case '1': return mMinWidth+KottaConst.Szunet1WIDTH*
						mLineY[1]/KottaConst.Szunet1VonalTav;
					case '2': return mMinWidth+KottaConst.Szunet2WIDTH*
						mLineY[1]/KottaConst.Szunet2VonalTav;
					case '4': return mMinWidth+KottaConst.Szunet4WIDTH*
						mLineY[2]/(KottaConst.Szunet4Vonal2Y-KottaConst.Szunet4Vonal4Y);
					case '8': return mMinWidth+KottaConst.Szunet8WIDTH*
							mLineY[2]/(KottaConst.Szunet8Vonal2Y-KottaConst.Szunet8Vonal4Y);
					case '6': return mMinWidth+KottaConst.Szunet16WIDTH*
							mLineY[2]/(KottaConst.Szunet16Vonal2Y-KottaConst.Szunet16Vonal4Y);
				}
				return 0.0f;
			case 'S': //pontozott szunet
				mState.Modosito=' ';
				switch(c2) {
					case '1': return 1.5f*mMinWidth+KottaConst.Szunet1WIDTH*
							mLineY[1]/KottaConst.Szunet1VonalTav;
					case '2': return 1.5f*mMinWidth+KottaConst.Szunet2WIDTH*
							mLineY[1]/KottaConst.Szunet2VonalTav;
					case '4': return 1.5f*mMinWidth+KottaConst.Szunet4WIDTH*
							mLineY[2]/(KottaConst.Szunet4Vonal2Y-KottaConst.Szunet4Vonal4Y);
					case '8': return 1.5f*mMinWidth+KottaConst.Szunet8WIDTH*
							mLineY[2]/(KottaConst.Szunet8Vonal2Y-KottaConst.Szunet8Vonal4Y);
					case '6': return 1.5f*mMinWidth+KottaConst.Szunet16WIDTH*
							mLineY[2]/(KottaConst.Szunet16Vonal2Y-KottaConst.Szunet16Vonal4Y);
				}
				return 0.0f;
			case 'r': //ritmus
				if (c2=='t') {
					mState.Tomor=false;
					return 0.0f;
				}
				mState.Ritmus=c2;
				mState.Pontozott=false;
				return 0.0f;
			case 'R': //pontozott ritmus
				if (c2=='t') {
					mState.Tomor=true;
					return 0.0f;
				}
				mState.Ritmus=c2;
				mState.Pontozott=true;
				return 0.0f;
			case '1': //hamgok
			case '2':
			case '3':
			{
				float res=(mState.Tomor ? 0.0f : mMinWidth);
				switch(mState.Modosito) {
					case '0':
						res+=1.25f*KottaConst.FeloldoWIDTH*mLineY[1]/
							(KottaConst.FeloldoVonal2aY-KottaConst.FeloldoVonal2fY);
						break;
					case 'k':
						res+=1.25f*KottaConst.KeresztWIDTH*mLineY[1]/
							(KottaConst.KeresztVonal2aY-KottaConst.KeresztVonal2fY);
						break;
					case 'K':
						res+=1.25f*KottaConst.KettosKeresztWIDTH*mLineY[1]/
							(KottaConst.KettosKeresztVonal2aY-KottaConst.KettosKeresztVonal2fY);
						break;
					case 'b':
						res+=1.25f*KottaConst.BeWIDTH*mLineY[1]/
							(KottaConst.BeVonal2aY-KottaConst.BeVonal2fY);
						break;
					case 'B':
						res+=1.25f*KottaConst.BeBeWIDTH*mLineY[1]/
							(KottaConst.BeBeVonal2aY-KottaConst.BeBeVonal2fY);
						break;
				}
				mState.Modosito=' ';
				switch(mState.Ritmus) {
					case 'l':
						res+=KottaConst.Hang0WIDTH*mLineY[1]/
							(KottaConst.Hang0Vonal2aY-KottaConst.Hang0Vonal2fY);
						break;
					case 'b':
						res+=KottaConst.HangBrevis1WIDTH*mLineY[1]/
							(KottaConst.HangBrevis1Vonal2aY-KottaConst.HangBrevis1Vonal2fY);
						break;
					case 's':
						res+=KottaConst.HangBrevis2WIDTH*mLineY[1]/
							(KottaConst.HangBrevis2Vonal2aY-KottaConst.HangBrevis2Vonal2fY);
						break;
					case '1':
						res+=KottaConst.Hang1WIDTH*mLineY[1]/
							(KottaConst.Hang1Vonal2aY-KottaConst.Hang1Vonal2fY);
						break;
					case '2':
						res+=KottaConst.Hang2fejWIDTH*mLineY[1]/
							(KottaConst.Hang2Vonal2aY-KottaConst.Hang2Vonal2fY);
						break;
					default:
						res+=KottaConst.Hang4fejWIDTH*mLineY[1]/
							(KottaConst.Hang4Vonal2aY-KottaConst.Hang4Vonal2fY);
						break;
				}
				if (mState.Pontozott) {
					res+=(mState.Tomor ? 0.0f : mMinWidth/8.0f);
					res+=KottaConst.PontWIDTH*mLineY[1]/KottaConst.PontVonalTav;
				}
				return res;
			}
			case '[': //gerenda
				return 0.0f;
			case ']':
				return 0.0f;
			case 'a': //agogika
				return 0.0f;
			case '(': //iv
			case ')':
				return 0.0f;
			case '-': //kottavonalak
				return 0.0f;
		}
		return 0.0f;
	}
	
	/////////////////////////////
	
	public void startDraw(Resources res, Canvas canvas, float x0, float y0, int color) {
		mRes=res; mCanvas=canvas; mX0=x0; mY0=y0;
		mX=x0; mColor=color;
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setColor(color);
	}
	
	//kulcs es elojegyzes
	public float startLine() {
		String s =
			(mState.Kulcs!=' ' ? "k"+mState.Kulcs : "")+
			(mState.Elojegy<0 ? "e"+(char)('0'+-mState.Elojegy) : "")+
			(mState.Elojegy>0 ? "E"+(char)('0'+mState.Elojegy) : "");
		if (s.isEmpty()) return 0.0f;
		if (mCanvas==null) return getWidth(s);
		float x0=mX;
		Draw(s);
		return mX-x0;
	}
	
	public void endDraw(float xend) {
		if (mX<xend) mX=xend;
		endGer();
		endTri();
		endIv('?');
		//vonalak
		for (int i=0; i<mState.Vonalszam; i++) {
			float y = mY0 + mLineY[7-i];
			for (int j=0; j<mVonalSzel; j++) {
				mCanvas.drawLine(mX0,y,mX,y,mPaint);
				y++;
			}
		}
		mRes=null; mCanvas=null; mPaint=null;
	}
	
	public void Draw(String kotta) {
		int l=kotta.length();
		mPostX=mX;
		if (mState.IvBalX<0f) mState.IvBalX=mX;
		for (int i=1; i<l; i+=2) {
			char c1=kotta.charAt(i-1);
			char c2=kotta.charAt(i);
			drawOne(c1,c2);
			mX+=widthOf(c1,c2);
			if (c1=='k'||c1=='e'||c1=='E'||c1=='u'||c1=='U')
				mPostX=mX;
		}
	}
	
	/////////////////////////////
	
	public void drawOne(char c1, char c2) {
		switch(c1) {
			case 'k': //kulcs
				mState.Kulcs=c2;
				drawKulcs(c2);
				return;
			case 'e': //bes elojegyzes
			case 'E': //keresztes
				drawElojegy(c2,c1=='e');
				return;
			case 'u': //utemjel
			case 'U': //spec.utemjel
				drawUtemjel(c2,c1=='U');
				return;
			case '|': //utemvonal
				drawUtemvonal(c2);
				return;
			case 'm': //modositojel
				mState.Modosito=c2;
				return;
			case 's': //szunet
			case 'S': //pontozott szunet
				drawSzunet(c2,c1=='S');
				return;
			case 'r': //ritmus
			case 'R': //pontozott ritmus
				if (c2=='t') {
					mState.Tomor=(c1=='R');
					return;
				}
				mState.Ritmus=c2;
				mState.Pontozott=(c1=='R');
				return;
			case '1': //hamgok
			case '2':
			case '3':
				drawHang(c2,c1);
				return;
			case '[': //gerenda
				if (c2=='0') mState.Szaaratlan=true;
				else if (c2=='1') mState.Szaaratlan=false;
				else if (c2=='3' || c2=='5') startTri(c2);
				else mState.Gerenda=true;
				return;
			case ']':
				endGer();
				mState.Gerenda=false;
				if (c2=='3' || c2=='5') {
					endTri();
					return;
				}
				return;
			case 'a': //agogika
				mState.Agogika=c2;
				return;
			case '(': //iv
			case ')':
				drawIv(c2,c1=='(');
				return;
			case '-': //kottavonalak
				mState.Vonalszam=(byte)(c2-'0');
				return;
		}
		return;
	}
	
	private void drawKulcs(char c2) {
		endGer();
		int id;
		float w,h,v1,v5;
		float y0=mLineY[3];  //kezdo vonal
		if (c2=='G') {
			id=R.drawable.gkulcs;   //R.drawable.gkulcs;
			w=KottaConst.GkulcsWIDTH;
			h=KottaConst.GkulcsHEIGHT;
			v1=KottaConst.GkulcsVonal1Y;
			v5=KottaConst.GkulcsVonal5Y;
		} else if (c2=='F') {
			id=R.drawable.fkulcs;
			w=KottaConst.FkulcsWIDTH;
			h=KottaConst.FkulcsHEIGHT;
			v1=KottaConst.FkulcsVonal1Y;
			v5=KottaConst.FkulcsVonal5Y;
		} else if (c2>='1' && c2 <='5') {
			id=R.drawable.ckulcs;
			w=KottaConst.CkulcsWIDTH;
			h=KottaConst.CkulcsHEIGHT;
			v1=KottaConst.CkulcsVonal1Y;
			v5=KottaConst.CkulcsVonal5Y;
			y0=mLineY[6-(c2-'0')];
		} else if (c2>='a' && c2<='i') {
			id=R.drawable.dkulcs;
			w=KottaConst.DkulcsWIDTH;
			h=KottaConst.DkulcsHEIGHT;
			v5=KottaConst.DkulcsVonal2Y;
			v1=v5+(KottaConst.DkulcsVonal1Y-v5)*4;
			y0=mLineY[7]-mLineY[1]*0.5f*(1+c2-'a');
		} else
			return;
			
		Drawable d =  mRes.getDrawable(id);
		float rat=(mLineY[7]-mLineY[3])/(v1-v5);
		float y1=mY0+y0-v5*rat;
		float y2=y1+h*rat;
		float x2=mX+w*rat;
		d.setBounds((int)mX,(int)y1,(int)x2,(int)y2);
		d.setTint(mColor);
		d.draw(mCanvas);
		return;
	}
	
	private void drawElojegy(char c2, boolean bes) {
		endGer();
		int id;
		float w,h,v1,v2a,v2f;
		String t;
		if (bes) {
			id=R.drawable.be;
			w=KottaConst.BeWIDTH; h=KottaConst.BeHEIGHT;
			v1=KottaConst.BeVonal1Y;
			v2a=KottaConst.BeVonal2aY;
			v2f=KottaConst.BeVonal2fY;
			switch(mState.Kulcs) {
				case 'G': t="3-4=2=4-2-3=1="; break;
				case 'F': t="2-3=1=3-1-2=0="; break;
				case '1': t="4-2-3=1=3-1-2="; break;
				case '2': t="5-3-4=2=4-2-3="; break;
				case '4': t="3=5-3-4=2=4-2-"; break;
				case '5': t="4=2=4-2-3=1=3-"; break;
				default : t="2=4-2-3=1=3-1-"; break;
			}
		} else {
			id=R.drawable.kereszt;
			w=KottaConst.KeresztWIDTH; h=KottaConst.KeresztHEIGHT;
			v1=KottaConst.KeresztVonal1Y;
			v2a=KottaConst.KeresztVonal2aY;
			v2f=KottaConst.KeresztVonal2fY;
			switch(mState.Kulcs) {
				case 'G': t="5-3=5=4-2=4=3-"; break;
				case 'F': t="4-2=4=3-5-3=5="; break;
				case '1': t="2=1-3-1=3=2-4-"; break;
				case '2': t="3=2-4-2=4=3-5-"; break;
				case '4': t="5=4-2=4=3-5-3="; break;
				case '5': t="3-5-3=5=4-2=4="; break;
				default : t="4=3-5-3=5=4-2="; break;
			}
		}
		int mul=c2-'0';
		if (mul<-7) mul=-7; else if (mul>7) mul=7;
		float rat=mLineY[1]/(v2a-v2f);
		Drawable d=mRes.getDrawable(id);
		d.setTint(mColor);
		int p=0;
		float x1=mX;
		while(mul-->0) {
			float x2=x1+w*rat;
			char t1=t.charAt(p), t2=t.charAt(p+1);
			float y1=mY0+mLineY[7-(t1-'1')];
			if (t2=='-') y1-=v1*rat; else y1-=v2a*rat;
			float y2=y1+h*rat;
			d.setBounds((int)x1,(int)y1,(int)x2,(int)y2);
			d.draw(mCanvas);
			p+=2;
			x1+=w*rat/1.5f;
		}
	}
	
	private void drawUtemjel(char c2,boolean spec) {
		//endGer();
		int id;
		switch(c2) {
			case '2': id=(spec ? R.drawable.u22 : R.drawable.u24); break;
			case '3': id=(spec ? R.drawable.u32 : R.drawable.u34); break;
			case '4': id=R.drawable.u44; break;
			case '5': id=R.drawable.u54; break;
			case '6': id=(spec ? R.drawable.u68 : R.drawable.u64); break;
			case '8': id=R.drawable.u38; break;
			default: return;
		}
		
		//jelenleg mind egyforma szeles es magas
		Drawable d =  mRes.getDrawable(id);
		float rat=(mLineY[7]-mLineY[3])/KottaConst.U22HEIGHT;
		float x2=mX+KottaConst.U22WIDTH*rat;
		d.setBounds((int)mX,(int)(mY0+mLineY[3]),(int)x2,(int)(mY0+mLineY[7]));
		d.setTint(mColor);
		d.draw(mCanvas);
		return;
	}
	
	private void rajzVekonyVonal(float x, float y1, float y2) {
		mCanvas.drawLine(x,y1,x,y2,mPaint);
	}
	
	private void rajzVastagVonal(float x, float y1, float y2, int w) {
		while(w-->0) {
			rajzVekonyVonal(x,y1,y2);
			x+=1.0f;
		}
	}
	
	private void rajzKetPont(float x, float y) {
		float rat = (mLineY[7]-mLineY[3])/(4.0f*KottaConst.PontVonalTav);
		float w=KottaConst.PontWIDTH*rat;
		float h=KottaConst.PontHEIGHT*rat;
		float x1=x-w/2.0f;
		Drawable d = mRes.getDrawable(R.drawable.pont);
		d.setTint(mColor);
		y-=(mLineY[1]+h)*0.5f;
		d.setBounds((int)x1,(int)y,(int)(x1+w),(int)(y+h));
		d.draw(mCanvas);
		y+=mLineY[1];
		d.setBounds((int)x1,(int)y,(int)(x1+w),(int)(y+h));
		d.draw(mCanvas);
	}
	
	private void rajzVekonyabbVonal(float x, float y1, float y2) {
		rajzVastagVonal(x,y1,y2,mVonalSzel);
	}
	
	private void drawUtemvonal(char c2) {
		endGer();
		float w = mMinWidth/4.0f;
		if (w<2.0f) w=2.0f;
		float x = mX+w+w;
		float y1 = mY0+mLineY[8-mState.Vonalszam];
		float y2 = mY0+mLineY[7];
		if (mState.Vonalszam<=1) {
			y2+=mLineY[1]*0.5f;
			y1=y2-mLineY[1];
		}
		switch(c2) {
			case '1': rajzVekonyVonal(x,y1,y2); break;
			case '|':
				x-=w/2.0f;
				rajzVekonyabbVonal(x,y1,y2);
				rajzVekonyabbVonal(x+w,y1,y2);
				break;
			case '.':
				x-=w/2.0f;
				rajzVekonyabbVonal(x,y1,y2);
				rajzVastagVonal(x+w,y1,y2,(int)w);
				break;
			case '\'':
				y1-=mLineY[1]*0.5f; y2=y1+mLineY[1];
				for (int j=0; j<mVonalSzel; j++) {
					mCanvas.drawLine(x,y1,x,y2,mPaint);
					x+=1.0f;
				}
				break;
			case '!':
				y1-=mLineY[1]*0.5f; if (mState.Vonalszam<=2) y1-=mLineY[1];
				y2=y1+mLineY[2];
				for (int j=0; j<mVonalSzel; j++) {
					mCanvas.drawLine(x,y1,x,y2,mPaint);
					x+=1.0f;
				}
				break;
			case '>':
				rajzVekonyabbVonal(x,y1,y2);
				rajzVastagVonal(x-w-w,y1,y2,(int)w);
				rajzKetPont(x+w+w,(y1+y2)*0.5f);
				break;
			case '<':
				rajzVekonyabbVonal(x,y1,y2);
				rajzVastagVonal(x+w,y1,y2,(int)w);
				rajzKetPont(x-w-w,(y1+y2)*0.5f);
				break;
			case ':':
				x-=w/2.0f;
				rajzVastagVonal(x,y1,y2,(int)w);
				rajzVekonyabbVonal(x-w,y1,y2);
				rajzVekonyabbVonal(x+w+w,y1,y2);
				y1=(y1+y2)*0.5f;
				rajzKetPont(x-w-w-w,y1);
				rajzKetPont(x+w+w+w+w,y1);
				break;
			default: return;
		}
	}
	
	private void drawSzunet(char c2,boolean pontozott) {
		endGer();
		int id;
		float w,h,v2,v4;
		switch(c2) {
			case '1':
				id=R.drawable.szunet1;
				w=KottaConst.Szunet1WIDTH; h=KottaConst.Szunet1HEIGHT;
				v4=0.0f; v2=2.0f*KottaConst.Szunet1VonalTav;
				break;
			case '2':
				id=R.drawable.szunet2;
				w=KottaConst.Szunet2WIDTH; h=KottaConst.Szunet2HEIGHT;
				v4=-KottaConst.Szunet2VonalTav*0.5f; v2=v4+2.0f*KottaConst.Szunet2VonalTav;
				break;
			case '4':
				id=R.drawable.szunet4;
				w=KottaConst.Szunet4WIDTH; h=KottaConst.Szunet4HEIGHT;
				v2=KottaConst.Szunet4Vonal2Y; v4=KottaConst.Szunet4Vonal4Y;
				break;
			case '8':
				id=R.drawable.szunet8;
				w=KottaConst.Szunet8WIDTH; h=KottaConst.Szunet8HEIGHT;
				v2=KottaConst.Szunet8Vonal2Y; v4=KottaConst.Szunet8Vonal4Y;
				break;
			case '6':
				id=R.drawable.szunet16;
				w=KottaConst.Szunet16WIDTH; h=KottaConst.Szunet16HEIGHT;
				v2=KottaConst.Szunet16Vonal2Y; v4=KottaConst.Szunet16Vonal4Y;
				break;
			default: return;
		}
		float rat = (mLineY[6]-mLineY[4])/(v2-v4);
		Drawable d = mRes.getDrawable(id);
		float x1=mX+0.5f*w*rat;
		if (pontozott) x1-=mMinWidth/4.0f;
		float x2=x1+w*rat;
		float y1=mY0+mLineY[4]-v4*rat;
		float y2=y1+h*rat;
		d.setBounds((int)x1,(int)y1,(int)x2,(int)y2);
		d.setTint(mColor);
		d.draw(mCanvas);
		if (pontozott) {
			rat=mLineY[1]/KottaConst.PontVonalTav;
			x1=x2+mMinWidth/2.0f;
			x2=x1+KottaConst.PontWIDTH*rat;
			h=KottaConst.PontHEIGHT*rat;
			y1=mY0+(mLineY[4]+mLineY[5])/2.0f-h/2.0f;
			y2=y1+h;
			d=mRes.getDrawable(R.drawable.pont);
			d.setBounds((int)x1,(int)y1,(int)x2,(int)y2);
			d.setTint(mColor);
			d.draw(mCanvas);
		}
	}
	
	private void drawHang(char c2, char oktav) {
		int id;
		float w,h,va,vf,vv;
		float rat,mw,x1,x2,y1,y2;
		Drawable d;
		
		mw=(mState.Tomor ? 0.0f : mMinWidth);
		x1=mX+mw*0.5f;
		
		int l1,l2;
		switch(c2) {
			case 'g': case 'G': l1=10; l2=9; break;
			case 'a': case 'A': l1=9; l2=9; break;
			case 'h': case 'H': l1=9; l2=8; break;
			case 'c': case 'C': l1=8; l2=8; break;
			case 'd': case 'D': l1=8; l2=7; break;
			case 'e': case 'E': l1=7; l2=7; break;
			case 'f': case 'F': l1=7; l2=6; break;
			default: return;
		}
		if (oktav=='2') {
			if (l1==l2) { l1-=3; l2-=4; } else { l1-=4; l2-=3; }
		} else if (oktav=='3') {
			l1-=7; l2-=7;
			if (l1<=0) return;
		}
		
		if (mState.Modosito!=' ') {
			switch(mState.Modosito) {
				case '0':
					id=R.drawable.feloldo;
					w=KottaConst.FeloldoWIDTH; h=KottaConst.FeloldoHEIGHT;
					vv=KottaConst.FeloldoVonal1Y;
					va=KottaConst.FeloldoVonal2aY; vf=KottaConst.FeloldoVonal2fY;
					break;
				case 'k':
					id=R.drawable.kereszt;
					w=KottaConst.KeresztWIDTH; h=KottaConst.KeresztHEIGHT;
					vv=KottaConst.KeresztVonal1Y;
					va=KottaConst.KeresztVonal2aY; vf=KottaConst.KeresztVonal2fY;
					break;
				case 'K':
					id=R.drawable.kettoskereszt;
					w=KottaConst.KettosKeresztWIDTH; h=KottaConst.KettosKeresztHEIGHT;
					vv=KottaConst.KettosKeresztVonal1Y;
					va=KottaConst.KettosKeresztVonal2aY; vf=KottaConst.KettosKeresztVonal2fY;
					break;
				case 'b':
					id=R.drawable.be;
					w=KottaConst.BeWIDTH; h=KottaConst.BeHEIGHT;
					vv=KottaConst.BeVonal1Y;
					va=KottaConst.BeVonal2aY; vf=KottaConst.BeVonal2fY;
					break;
				case 'B':
					id=R.drawable.bebe;
					w=KottaConst.BeBeWIDTH; h=KottaConst.BeBeHEIGHT;
					vv=KottaConst.BeBeVonal1Y;
					va=KottaConst.BeBeVonal2aY; vf=KottaConst.BeBeVonal2fY;
					break;
				default: return;
			}
			rat=mLineY[1]/(va-vf);
			x2=x1+w*rat;
			y1=mY0+mLineY[l1]-(l1==l2 ? vv : va)*rat;
			y2=y1+h*rat;
			d=mRes.getDrawable(id);
			d.setTint(mColor);
			d.setBounds((int)x1,(int)y1,(int)x2,(int)y2);
			d.draw(mCanvas);
			x1=x2+w*rat*0.25f;
		}
		
		switch (mState.Ritmus) {
			case 'l':
				id=R.drawable.hang0;
				w=KottaConst.Hang0WIDTH; h=KottaConst.Hang0HEIGHT;
				vv=KottaConst.Hang0Vonal1Y;
				va=KottaConst.Hang0Vonal2aY; vf=KottaConst.Hang0Vonal2fY;
				break;
			case 'b':
				id=R.drawable.hangbrevis1;
				w=KottaConst.HangBrevis1WIDTH; h=KottaConst.HangBrevis1HEIGHT;
				vv=KottaConst.HangBrevis1Vonal1Y;
				va=KottaConst.HangBrevis1Vonal2aY; vf=KottaConst.HangBrevis1Vonal2fY;
				break;
			case 's':
				id=R.drawable.hangbrevis2;
				w=KottaConst.HangBrevis2WIDTH; h=KottaConst.HangBrevis2HEIGHT;
				vv=KottaConst.HangBrevis2Vonal1Y;
				va=KottaConst.HangBrevis2Vonal2aY; vf=KottaConst.HangBrevis2Vonal2fY;
				break;
			case '1':
				id=R.drawable.hang1;
				w=KottaConst.Hang1WIDTH; h=KottaConst.Hang1HEIGHT;
				vv=KottaConst.Hang1Vonal1Y;
				va=KottaConst.Hang1Vonal2aY; vf=KottaConst.Hang1Vonal2fY;
				break;
			case '2':
				id=R.drawable.hang2fej;
				w=KottaConst.Hang2fejWIDTH; h=KottaConst.Hang2fejHEIGHT;
				vv=KottaConst.Hang2Vonal1Y;
				va=KottaConst.Hang2Vonal2aY; vf=KottaConst.Hang2Vonal2fY;
				break;
			case '4':
			case '8':
			case '6':
				id=R.drawable.hang4fej;
				w=KottaConst.Hang4fejWIDTH; h=KottaConst.Hang4fejHEIGHT;
				vv=KottaConst.Hang4Vonal1Y;
				va=KottaConst.Hang4Vonal2aY; vf=KottaConst.Hang4Vonal2fY;
				break;
			default: return;
		}
		rat=mLineY[1]/(va-vf);
		x2=x1+w*rat;
		y1=mY0+mLineY[l1]-(l1==l2 ? vv : va)*rat;
		y2=y1+h*rat;
		d=mRes.getDrawable(id);
		d.setTint(mColor);
		d.setBounds((int)x1,(int)y1,(int)x2,(int)y2);
		d.draw(mCanvas);
		RectF r = new RectF(x1,y1,x2,y2);
		addSzaar(r,c2<'a');
		addIv(r,c2<'a'); mState.IvTipus=mState.IvTipusLesz;
		addTri(r,c2<'a');
			
		//neuma vonal: ha a hang szorosan az elozonel
		float ym = (y1+y2)/2.0f;
		if (x1==mState.NeumaX)
			mCanvas.drawLine(x1,mState.NeumaY,x1,ym,mPaint);
		mState.NeumaX=x2; mState.NeumaY=ym;

		//potvonalak
		float w4 = w*rat/4.0f;
		if (l2>=8) {
			mCanvas.drawLine((int)(x1-w4),(int)(mY0+mLineY[8]),(int)(x2+w4),(int)(mY0+mLineY[8]),mPaint);
			if (l2>=9)
				mCanvas.drawLine((int)(x1-w4),(int)(mY0+mLineY[9]),(int)(x2+w4),(int)(mY0+mLineY[9]),mPaint);
		}
		for (int i=l1; i<=7-mState.Vonalszam; i++)
			mCanvas.drawLine((int)(x1-w4),(int)(mY0+mLineY[i]),(int)(x2+w4),(int)(mY0+mLineY[i]),mPaint);
			
		//agogika
		if (mState.Agogika!=' ') {
			vv=0.0f; vf=0.0f;
			switch(mState.Agogika) {
				case '-':
					id=R.drawable.tenuto;
					w=KottaConst.TenutoWIDTH; h=KottaConst.TenutoHEIGHT;
					va=KottaConst.TenutoVonalTav;
					break;
				case '.':
					id=R.drawable.pont;
					w=KottaConst.PontWIDTH; h=KottaConst.PontHEIGHT;
					va=KottaConst.PontVonalTav;
					break;
				case '>':
					id=R.drawable.marcato1;
					w=KottaConst.Marcato1WIDTH; h=KottaConst.Marcato1HEIGHT;
					//vv=KottaConst.Marcato1Vonal1Y;
					va=KottaConst.Marcato1Vonal2aY; vf=KottaConst.Marcato1Vonal2fY;
					break;
				case '^':
					id=R.drawable.marcato2;
					w=KottaConst.Marcato2WIDTH; h=KottaConst.Marcato2HEIGHT;
					//vv=KottaConst.Marcato2Vonal1Y;
					va=KottaConst.Marcato2Vonal2aY; vf=KottaConst.Marcato2Vonal2fY;
					break;
				case 'K':
					if (c2<'a') {
						id=R.drawable.koronale;
						w=KottaConst.KoronaLeWIDTH; h=KottaConst.KoronaLeHEIGHT;
						//vv=KottaConst.KoronaLeVonal1Y;
						va=KottaConst.KoronaLeVonal2aY; vf=KottaConst.KoronaLeVonal2fY;
					} else {
						id=R.drawable.koronafel;
						w=KottaConst.KoronaFelWIDTH; h=KottaConst.KoronaFelHEIGHT;
						//vv=KottaConst.KoronaFelVonal1Y;
						va=KottaConst.KoronaFelVonal2aY; vf=KottaConst.KoronaFelVonal2fY;
					}
					break;
				case 'm':
					id=R.drawable.mordent1;
					w=KottaConst.Mordent1WIDTH; h=KottaConst.Mordent1HEIGHT;
					//vv=KottaConst.Mordent1Vonal1Y;
					va=KottaConst.Mordent1Vonal2aY; vf=KottaConst.Mordent1Vonal2fY;
					break;
				case 'M':
					id=R.drawable.mordent2;
					w=KottaConst.Mordent2WIDTH; h=KottaConst.Mordent2HEIGHT;
					//vv=KottaConst.Mordent2Vonal1Y;
					va=KottaConst.Mordent2Vonal2aY; vf=KottaConst.Mordent2Vonal2fY;
					break;
				case 't':
					id=R.drawable.trilla1;
					w=KottaConst.Trilla1WIDTH; h=KottaConst.Trilla1HEIGHT;
					//vv=KottaConst.Trilla1Vonal1Y;
					va=KottaConst.Trilla1Vonal2aY; vf=KottaConst.Trilla1Vonal2fY;
					break;
				case 'T':
					id=R.drawable.trilla2;
					w=KottaConst.Trilla2WIDTH; h=KottaConst.Trilla2HEIGHT;
					//vv=KottaConst.Trilla2Vonal1Y;
					va=KottaConst.Trilla2Vonal2aY; vf=KottaConst.Trilla2Vonal2fY;
					break;
				default: return;
			}
			rat=mLineY[1]/(va-vf);
			float ax1=(x1+x2-w*rat)/2.0f;
			float ax2=ax1+w*rat;
			float ay1=(y1+y2-h*rat)/2.0f+(c2<'a' ? -mLineY[1] : mLineY[1]);
			if (l1==l2 && (mState.Agogika=='-' || mState.Agogika=='.'))
				ay1+=h*rat;
			float ay2=ay1+h*rat;
			d=mRes.getDrawable(id);
			d.setTint(mColor);
			d.setBounds((int)ax1,(int)ay1,(int)ax2,(int)ay2);
			d.draw(mCanvas);
		}
		if (mState.Pontozott) {
			rat=mLineY[1]/KottaConst.PontVonalTav;
			x1=x2+mw/8.0f;
			x2=x1+KottaConst.PontWIDTH*rat;
			h=KottaConst.PontHEIGHT*rat;
			y1=ym-(l1==l2 ? h : h/2.0f);
			y2=y1+h;
			d=mRes.getDrawable(R.drawable.pont);
			d.setTint(mColor);
			d.setBounds((int)x1,(int)y1,(int)x2,(int)y2);
			d.draw(mCanvas);
		}
	}
	
	private void drawIv(char c2,boolean start) {
		if (start) {
			mState.IvTipusLesz=c2;
			return;
		}
		endIv(c2);
	}
	
	private void rajzSzaar(RectF fej, boolean lefele, char ritmus) {
		if (mState.Szaaratlan) return;
		if (ritmus!='2' && ritmus!='4' && ritmus!='8' && ritmus!='6') return;
		float szel=KottaConst.ZaszloSzelesseg*mRat;
		float sx1,sy1,sx2,sy2;
		float zw=0.0f,zh=0.0f,zy1,zy2;
		int id=0;
		sy1=(fej.top+fej.bottom)/2.0f;
		if (lefele) {
			sx1=fej.left;
			sy2=sy1+mLineY[3];
			if (ritmus=='8') {
				id=R.drawable.zaszlo8le;
				zw=KottaConst.Zaszlo8leWIDTH; zh=KottaConst.Zaszlo8leHEIGHT;
			} else if (ritmus=='6') {
				id=R.drawable.zaszlo16le;
				zw=KottaConst.Zaszlo8leWIDTH; zh=KottaConst.Zaszlo16leHEIGHT;
			}
			zy2=sy2; zy1=sy2-zh*mRat;
		} else {
			sx1=fej.right-szel;
			sy2=sy1-mLineY[3];
			if (ritmus=='8') {
				id=R.drawable.zaszlo8fel;
				zw=KottaConst.Zaszlo8felWIDTH; zh=KottaConst.Zaszlo8felHEIGHT;
			} else if (ritmus=='6') {
				id=R.drawable.zaszlo16fel;
				zw=KottaConst.Zaszlo8felWIDTH; zh=KottaConst.Zaszlo16felHEIGHT;
			}
			zy1=sy2; zy2=sy2+zh*mRat;
		}
		if (id!=0) {
			Drawable d=mRes.getDrawable(id);
			d.setBounds((int)sx1,(int)zy1,(int)(sx1+zw*mRat),(int)zy2);
			d.setTint(mColor);
			d.draw(mCanvas);
		}
		sx2=sx1+szel;
		while (sx1<sx2) {
			mCanvas.drawLine(sx1,sy1,sx1,sy2,mPaint);
			sx1+=1.0f;
		}
	}
	
	private void addSzaar(RectF fej, boolean lefele) {
		if (!pushGer(fej,lefele)) rajzSzaar(fej,lefele,mState.Ritmus);
	}
	
	//TRUE=mentette
	private boolean pushGer(RectF fej, boolean lefele) {
		if (!mState.Gerenda) return false;
		if (mState.Szaaratlan || (mState.Ritmus!='8' && mState.Ritmus!='6')) {
			endGer();
			return false;
		}
		mGer[mGerIdx].mFej=fej;
		mGer[mGerIdx].mLe=lefele;
		mGer[mGerIdx].mRitmus=mState.Ritmus;
		mGerIdx++;
		if (mGerIdx>=MAXGERENDA) endGer();
		return true;
	}
	
	private void endGer() {
		if (mGerIdx<2) {
			if (mGerIdx>0) rajzSzaar(mGer[0].mFej,mGer[0].mLe,mGer[0].mRitmus);
			mGerIdx=0;
			return;
		}
		int mg1=mGerIdx-1;
		float X0[] = new float[MAXGERENDA];
		float Y0[] = new float[MAXGERENDA];
		float Y1[] = new float[MAXGERENDA];
		float vonalvast=mLineY[1]/2.0f;
		float kozvast=vonalvast/2.0f;
		float minlen=mLineY[2], normlen=mLineY[3];
		{
			float m=mGer[0].mFej.height()+vonalvast+vonalvast+kozvast;
			if (m>minlen) minlen=m;
			if (m>normlen) normlen=m;
		}
		float rat=(mGer[0].mFej.bottom-mGer[0].mFej.top)/(KottaConst.Hang4Vonal2aY-KottaConst.Hang4Vonal2fY);
		float szel=KottaConst.ZaszloSzelesseg*rat;
		
		for (int i=0; i<mGerIdx; i++) {
			X0[i]=(mGer[i].mLe ? mGer[i].mFej.left : mGer[i].mFej.right-szel);
			Y0[i]=mGer[i].mFej.centerY();
			Y1[i]=Y0[i]+(mGer[i].mLe ? minlen : -minlen);
		}
		
		//find lenghts
		int i=1; boolean le1=mGer[0].mLe;
		for (int cnt=0; cnt<100; cnt++) {
			if (i>=mGerIdx) break;
			float y1=Y1[0]+(X0[i]-X0[0])*(Y1[mg1]-Y1[0])/(X0[mg1]-X0[0]);
			boolean lei=mGer[i].mLe;
			if (le1!=lei) {
				Y1[i++]=y1;
				continue;
			}
			if (lei) {
				if (y1>=Y0[i]+minlen) {
					Y1[i++]=y1;
					continue;
				}
				float m=Y0[i]+minlen-y1;
				Y1[0]+=m;
				Y1[mg1]+=m;
				i=1;
			} else {
				if (y1<=Y0[i]-minlen) {
					Y1[i++]=y1;
					continue;
				}
				float m=y1-(Y0[i]-minlen);
				Y1[0]-=m;
				Y1[mg1]-=m;
				i=1;
			}
		}
		
		//draw vert
		for (i=0; i<mGerIdx; i++) {
			float vx1=X0[i], vy0=Y0[i], vy1=Y1[i];
			float vx2=vx1+szel;
			while(vx1<vx2) {
				mCanvas.drawLine(vx1,vy0,vx1,vy1,mPaint);
				vx1+=1.0f;
			}
		}
		//horiz
		float vx1=X0[0], vx2=X0[mg1];
		float vy1=Y1[0], vy2=Y1[mg1];
		adjustTri(vx1,vy1,vx2,vy2);
		if (mGer[0].mLe) vy1-=vonalvast;
		if (mGer[mg1].mLe) vy2-=vonalvast;
		float vyend=vy1+vonalvast;
		while (vy1<vyend) {
			mCanvas.drawLine(vx1,vy1,vx2,vy2,mPaint);
			vy1+=1.0f; vy2+=1.0f;
		}
			
		vy1=Y1[0]-vonalvast-kozvast-vonalvast;
		float vy1f=Y1[0]+vonalvast+kozvast;
		vy2=Y1[mg1]-vonalvast-kozvast-vonalvast;
		float vy2f=Y1[mg1]+vonalvast+kozvast;
		float w=mGer[0].mFej.width()/2.0f;
		boolean prev16=true;
		for (i=0; i<mGerIdx; i++) {
			boolean is16=(mGer[i].mRitmus=='6');
			if (!is16) {
				prev16=false;
				continue;
			}
			if (i==mg1 && prev16) break;
			float x1,y1,x2,y2;
			prev16=(i==mg1 || mGer[i+1].mRitmus=='6');
			if (i!=mg1 && mGer[i].mLe!=mGer[i+1].mLe) prev16=false;
			if (prev16) {
				x1=X0[i]; x2=(i==mg1 ? x1-w : X0[i+1]);
			} else {
				x1=X0[i]-w; x2=X0[i]+w;
				if (i==0) x1=X0[0];
			}
			float ty1=vy1f, ty2=vy2f;
			if (mGer[i].mLe) { ty1=vy1; ty2=vy2; }
			y1=ty1+(ty2-ty1)*(x1-X0[0])/(X0[mg1]-X0[0]);
			y2=ty1+(ty2-ty1)*(x2-X0[0])/(X0[mg1]-X0[0]);
			float y1end=y1+vonalvast;
			while (y1<y1end) {
				mCanvas.drawLine(x1,y1,x2,y2,mPaint);
				y1+=1.0f; y2+=1.0f;
			}
		}
		//vege
		mGerIdx=0;
	}
	
	/////////////////////
	// kotoiv
	/////////////////////
	
	private void addIv(RectF r, boolean also) {
		if (mState.IvTipus=='a' || mState.IvTipus=='f') {  //kotoiven belul
			mState.IvEndX=r.centerX(); mState.IvEndY=r.centerY();
			if (mState.IvTipus=='a') {
				if (mState.IvEndY>mState.IvMaxY) mState.IvMaxY=mState.IvEndY;
			} else {
				if (mState.IvMaxY<0f) mState.IvMaxY=mState.IvEndY;
				else if (mState.IvEndY<mState.IvMaxY) mState.IvMaxY=mState.IvEndY;
				if (mState.IvMaxY<0f) mState.IvMaxY=0f;
			}
		} else {    //kotoiven kivul
			mState.IvStartX=r.centerX(); mState.IvStartY=r.centerY();
			mState.IvMaxY=mState.IvStartY;
		}
	}
	
	private void endIv(char endtipus) {
		float fX[] = new float[12];
		float fY[] = new float[12];
// 0                                         1
//  \\                                     //
//   \10--------6-------3------7---------11/
//    \                                   /
//     8 -------4-------2------5-------- 9

		boolean vaneleje=(mState.IvTipus=='a' || mState.IvTipus=='f');
		boolean vanvege=(endtipus=='a' || endtipus=='f');
		if (mState.IvBalX<0f || mState.IvMaxY<0f) return;
		if (!vaneleje && !vanvege) return;
		boolean also=(vanvege ? endtipus=='a' : mState.IvTipus=='a');
		float Ykoz=mLineY[1]-mLineY[0];
		float aYkoz = (also ? Ykoz : -Ykoz);
		
		//1.bal sarok
		fX[0]=(vaneleje ? mState.IvStartX : mState.IvBalX);
		fY[0]=(vaneleje ? mState.IvStartY : mState.IvMaxY);
		
		//2.jobb sarok
		fX[1]=mState.IvEndX; fY[1]=mState.IvEndY;
		
		//eltolas a kottafejtol
		fY[0]+=aYkoz; fY[1]+=aYkoz;
		
		//iv hossza
		float xlen=fX[1]-fX[0], ylen=fY[1]-fY[0];
		float r=(float)Math.sqrt(xlen*xlen+ylen*ylen);
		float xlenperr=xlen/r, ylenperr=ylen/r;
		
		//3..4.kozep, belso kozep
		fX[2]=(fX[0]+fX[1])/2f;
		fY[2]=(fY[0]+fY[1])/2f;
		fX[3]=fX[2]; fY[3]=fY[2];
		fX[2]=fX[2]-aYkoz*ylenperr;
		fY[2]=fY[2]+aYkoz*xlenperr;
		fX[3]=fX[3]-aYkoz*0.75f*ylenperr;
		fY[3]=fY[3]+aYkoz*0.75f*xlenperr;
		
		//5..8.kozepek iranya
		float v=r/4f;
		float vx=v*xlenperr, vy=v*ylenperr;
		fX[4]=fX[2]-vx; fY[4]=fY[2]-vy;
		fX[5]=fX[2]+vx; fY[5]=fY[2]+vy;
		fX[6]=fX[3]-vx; fY[6]=fY[3]-vy;
		fX[7]=fX[3]+vx; fY[7]=fY[3]+vy;
		
		//9..10.szelek iranya
		vx=Ykoz*xlenperr-aYkoz*ylenperr;
		vy=Ykoz*ylenperr+aYkoz*xlenperr;
		fX[8]=fX[0]+vx; fY[8]=fY[0]+vy;
		fX[9]=fX[1]-Ykoz*xlenperr-aYkoz*ylenperr;
		fY[9]=fY[1]-Ykoz*ylenperr+aYkoz*xlenperr;
		
		//11..12.belso szelek iranya
		vx=Ykoz*xlenperr-aYkoz*ylenperr*0.75f;
		vy=Ykoz*ylenperr+aYkoz*xlenperr*0.75f;
		fX[10]=fX[0]+vx; fY[10]=fY[0]+vy;
		fX[11]=fX[1]-Ykoz*xlenperr-aYkoz*ylenperr*0.75f;
		fY[11]=fY[1]-Ykoz*ylenperr+aYkoz*xlenperr*0.75f;

		//itt rajzolunk
// 0                                         1
//  \\                                     //
//   \10--------6-------3------7---------11/
//    \                                   /
//     8 -------4-------2------5-------- 9
		
		Path pt = new Path();
		//baloldalrol
		if (vaneleje) {
			pt.moveTo(fX[0],fY[0]); 	//bal
			pt.cubicTo(fX[8],fY[8],		//balszel iranyultsag
						fX[4],fY[4],	//kozep bal irany
						fX[2],fY[2]);	//kozep
		} else {
			pt.moveTo(fX[2],fY[3]);		//kozep
		}
		//kozepponttol
		if (vanvege) {
			pt.cubicTo(fX[5],fY[5],		//kozep jobb irany
						fX[9],fY[9],	//jobb irany
						fX[1],fY[1]);	//jobb
			pt.cubicTo(fX[11],fY[11],	//jobb bel irany
						fX[7],fY[7],	//kozepalja irany
						fX[3],fY[3]);	//kozepalja
		} else {
			pt.cubicTo(fX[3],fY[3],		//kozep irany
						fX[2],fY[2],	//kozepalja irany
						fX[3],fY[3]);	//kozepalja
		}
		//vissza balszelre
		if (vaneleje) {
			pt.cubicTo(fX[6],fY[6],		//kozepalja irany
						fX[10],fY[10],	//bal also irany
						fX[0],fY[0]);   //bal
		} else {
			pt.cubicTo(fX[2],fY[2],		//kozepalja irany
						fX[3],fY[3],	//kozep irany
						fX[2],fY[2]);   //vegpont
		}

		//rajzoljunk!
		mCanvas.drawPath(pt,mPaint);
		
		//kotoiv vege
		mState.IvTipus=' '; mState.IvTipusLesz=' ';
	}
	
	private void startTri(char c2) {
		mState.TriTipus=c2;
		mState.TriPos.clear();
	}
	
	private void endTri() {
		boolean tri = (mState.TriTipus=='3');
		if (!tri && mState.TriTipus!='5') return;
		mState.TriTipus=' ';
		
		if (mState.TriPos.size()<2) {
			mState.TriPos.clear();
			return;
		}
		PointF lp=mState.TriPos.get(0), rp=mState.TriPos.get(mState.TriPos.size()-1);
		float w=(rp.x-lp.x);
		if (w<=0f) {
			mState.TriPos.clear();
			return;
		}
		for (PointF p : mState.TriPos) {
			float y=lp.y+(rp.x-p.x)*(rp.y-lp.y)/w;
			float dif=y-p.y;
			if (mState.TriLe ? dif<0f : dif>0f) {
				lp.y-=dif; rp.y-=dif;
			}
		}
		float half = mLineY[1]*0.5f;
		if (mState.TriLe) {
			lp.y+=half; rp.y+=half;
		} else {
			lp.y-=half; rp.y-=half;
		}
		lp.x-=half; rp.x+=half;
		
		//number
		int id;
		float nw,nh;
		if (mState.TriTipus=='3') {
			id=R.drawable.triola;
			nw=KottaConst.TriolaWIDTH;
			nh=KottaConst.TriolaHEIGHT;
		} else {
			id=R.drawable.pentola;
			nw=KottaConst.PentolaWIDTH;
			nh=KottaConst.PentolaHEIGHT;
		}
		Drawable d=mRes.getDrawable(id);
		float nx=(rp.x+lp.x)*0.5f;
		float ny=(rp.y+lp.y)*0.5f;
		nw=nw*mLineY[1]/nh; nh=mLineY[1];
		nx-=nw*0.5f;
		ny+=(mState.TriLe ? half : -half-mLineY[1]);
		d.setBounds((int)nx,(int)ny,(int)(nx+nw),(int)(ny+nh));
		d.setTint(mColor);
		d.draw(mCanvas);
		
		//vonalak
		float x1=lp.x, y1=lp.y, x2=rp.x, y2=rp.y;
		float y1a=(mState.TriLe ? y1-half : y1+half);
		float y2a=(mState.TriLe ? y2-half : y2+half);
		for (int i=0; i<mVonalSzel; i++) {
			mCanvas.drawLine(lp.x,y1,rp.x,y2,mPaint);
			mCanvas.drawLine(x1,lp.y,x1,y1a,mPaint);
			mCanvas.drawLine(x2,rp.y,x2,y2a,mPaint);
			y1++; y2++; x1++; x2--;
		}
		
		
	}
	
	private void addTri(RectF r, boolean lefele) {
		if (mState.TriTipus!='3' && mState.TriTipus!='5') return;
		if (mState.TriPos.size()>0) lefele=mState.TriLe;
		mState.TriLe=lefele;
		float x,y;
		if (lefele) {
			x=r.left; y=r.centerY()+mLineY[3];
		} else {
			x=r.right; y=r.centerY()-mLineY[3];
		}
		mState.TriPos.add(new PointF(x,y));
	}
	
	private void adjustTri(float x1, float y1, float x2, float y2) {
		float xd = x2-x1;
		if (xd<=0f) return;
		for (PointF p : mState.TriPos) {
			if (p.x<x1) continue;
			if (p.x>x2) break;
			float y = y1+(y2-y1)*(x2-p.x)/xd;
			if (mState.TriLe ? y<p.y : y>p.y) p.y=y;
		}
	}
	
	
}
