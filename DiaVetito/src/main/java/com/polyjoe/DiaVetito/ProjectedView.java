package com.polyjoe.DiaVetito;

import android.view.View;
import android.graphics.Canvas;
import android.content.*;

public class ProjectedView extends View
{
	private TcpServer mTcp;
	
	ProjectedView(Context context) {
		super(context);
		mTcp = TcpServer.getMe();
		setBackground(null);
	}
	
	public void Recalc() {
		DiaBase dia = (mTcp.G.Projecting ? mTcp.Dia : mTcp.Blank );
		if (dia!=null) dia.OnRecalc();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		DiaBase dia = (mTcp.G.Projecting ? mTcp.Dia : mTcp.Blank );
		canvas.drawColor(0xFF000000);
		if (dia!=null) {
			dia.OnDraw(canvas);
		}
	}

}
