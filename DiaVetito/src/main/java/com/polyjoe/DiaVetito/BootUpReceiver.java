package com.polyjoe.DiaVetito;

import android.content.*;

public class BootUpReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context ctx, Intent intent) {
		SharedPreferences sp = ctx.getSharedPreferences("settings",Context.MODE_PRIVATE);
		boolean boot = sp.getBoolean(MainActivity.spBOOT,false);
		if (!boot) return;
		Intent it = new Intent(ctx, MainActivity.class);
		it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		ctx.startActivity(it);
	}
}
