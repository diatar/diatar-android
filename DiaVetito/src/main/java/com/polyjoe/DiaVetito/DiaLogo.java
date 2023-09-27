package com.polyjoe.DiaVetito;

import android.graphics.*;
import android.os.*;
import android.view.*;

public class DiaLogo extends DiaBase
{
	private static final String LogoTxt = "Diatár Vetítő";
	
	private Paint Pt1, Pt2;
	private float x1,y1,x2,y2;
	private int BkColor;
	private int phase;
	private String VerTxt;
	
	private View view;
	
	private Handler hnd;
	private Runnable run;
	
	public DiaLogo(View v) {
		view = v;
		
		BkColor = 0xFF000000;
		VerTxt=TcpServer.getMe().getVerTxt(true);
		
		TcpServer.getMe().Dia=this;
		hnd = new Handler();
		run = new Runnable() {
			@Override
			public void run() {
				if (phase<16) {
					//varunk
				} else if (phase<32) {
					int i = phase-16;
					BkColor = 0xFF000000+
						((0x4B*i/16)<<16) +
						((0xEF*i/16)<<8) +
						((0x96*i/16));
				} else if (phase<48) {
					//varunk
				} else if (phase<64) {
					int i = 64-phase;
					BkColor = 0xFF000000+
						((0x4B*i/16)<<16) +
						((0xEF*i/16)<<8) +
						((0x96*i/16));
				} else if (phase<80) {
					//varunk
				} else {
					StopMe();
					return;
				}
				if (!CheckMe()) {
					StopMe();
					return;
				}
				phase++;
				view.invalidate();
				hnd.postDelayed(this,100);
			}
		};
		hnd.postDelayed(run,0);
	}
	
	private void Setup(Canvas canvas) {
		int w = canvas.getWidth(), h = canvas.getHeight();
		Pt1 = new Paint();
		Typeface tf = Typeface.create(Typeface.SERIF,Typeface.BOLD_ITALIC);
		Pt1.setTypeface(tf);
		Pt1.setShadowLayer(10f,5f,5f,0xFF404040);
		Pt1.setColor(0xFF0000FF);
		Pt1.setTextSize(100f);
		Pt1.setTextSize(
			100f*(w*0.9f)/
				Pt1.measureText(LogoTxt));
		x1=(w-Pt1.measureText(LogoTxt))/2;
		y1=h/2f+Pt1.ascent();
		
		Pt2 = new Paint();
		Pt2.setTextSize(Pt1.getTextSize()/2);
		Pt2.setColor(0xFFFFFFFF);
		x2=(w-Pt2.measureText(VerTxt))/2;
		y2=y1+Pt1.descent()-Pt1.ascent();
	}
	
	public boolean CheckMe() {
		return TcpServer.getMe().Dia==this;
	}
	
	public void StopMe() {
		TcpServer t = TcpServer.getMe();
		if (t.Dia==this) {
			t.Dia = null;
			t.G.Projecting=false;
		}
		view.invalidate();
		hnd.removeCallbacks(run);
		view=null; hnd=null; run=null;
	}

	@Override
	public void OnDraw(Canvas canvas)
	{
		if (Pt1==null) Setup(canvas);
		canvas.drawColor(BkColor);
		canvas.drawText(LogoTxt,x1,y1,Pt1);
		canvas.drawText(VerTxt,x2,y2,Pt2);
	}
}
