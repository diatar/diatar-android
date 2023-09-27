package com.polyjoe.DiaVetito;

import android.graphics.*;

public class DiaBase
{
	protected TcpServer mTcp;
	
	public DiaBase() {
		mTcp = TcpServer.getMe();
	}
	
	private int getWH(Canvas canvas, boolean first) {
		if (first)
			return (int)(canvas.getWidth()-mTcp.ClipLpx-mTcp.ClipRpx);
		else
			return (int)(canvas.getHeight()-mTcp.ClipTpx-mTcp.ClipBpx);
	}
	
	public int getWidth(Canvas canvas) {
		return getWH(canvas, mTcp.mRotate==0 || mTcp.mRotate==2);
	}
	
	public int getHeight(Canvas canvas) {
		return getWH(canvas, mTcp.mRotate==1 || mTcp.mRotate==3);
	}
	
	private int getRB(Canvas canvas, boolean first) {
		if (first)
			return (int)(canvas.getWidth()-mTcp.ClipRpx);
		else
			return (int)(canvas.getHeight()-mTcp.ClipBpx);
	}
	
	public int getRight(Canvas canvas) {
		return getRB(canvas, true); // mTcp.mRotate==1 || mTcp.mRotate==3);
	}

	public int getBottom(Canvas canvas) {
		return getRB(canvas, false); // mTcp.mRotate==0 || mTcp.mRotate==2);
	}
	
	public boolean RecalcIfNeeded() { return false; }
	public void OnRecalc() {}
	public void OnDrawClipped(Canvas canvas) {}
	public void OnDraw(Canvas canvas) {
		canvas.save();
		float l=mTcp.ClipLpx, t=mTcp.ClipTpx;
		float r=getRB(canvas,true), b=getRB(canvas,false);
		canvas.clipRect(l,t,r,b);
		canvas.translate(l,t);
		float w2=(r-l)/2.0f, h2=(b-t)/2.0f;
		if (mTcp.mMirror) canvas.scale(-1.0f,1.0f,w2,0f);
		switch (mTcp.mRotate) {
			case 1: canvas.rotate(90.0f,w2,w2); break;
			case 2: canvas.rotate(180.0f,w2,h2); break;
			case 3: canvas.rotate(270.0f,h2,h2); break;
		}
		OnDrawClipped(canvas);
		canvas.restore();
	}
}
