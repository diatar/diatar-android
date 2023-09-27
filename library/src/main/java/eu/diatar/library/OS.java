package eu.diatar.library;
import android.content.*;
import android.os.*;
import android.widget.*;

public class OS
{
	
	static public void Abort() {
		android.os.Process.killProcess(android.os.Process.myPid());
		System.exit(1);
	}
	
	static public void Shutdown(Context ctx) {
		try {
			//Intent intent = new Intent("android.intent.action.ACTION_REQUEST_SHUTDOWN");
			//intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			//intent.putExtra("android.intent.extra.KEY_CONFIRM", true);
			//ctx.startActivity(intent);
			//java.lang.Runtime.getRuntime().exec(new String[] { "/system/xbin/su", "-c", "reboot -p" });
			java.lang.Process proc = Runtime.getRuntime().exec(new String[]{ "su", "-c", "reboot -p" });
			proc.waitFor();
		} catch (Exception e) {
			Toast.makeText(
				ctx,
				"Leállítás hiba: "+e.getLocalizedMessage(),
				Toast.LENGTH_LONG
			).show();
		}
	}
	
	static public void Restart(Context ctx) {
		try {
			//PowerManager pm = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
			//pm.reboot("Diatar apk");
			//java.lang.Runtime.getRuntime().exec(new String[] { "/system/xbin/su", "-c", "reboot" });
			java.lang.Process proc = Runtime.getRuntime().exec(new String[]{ "su", "-c", "reboot" });
			proc.waitFor();
		} catch (Exception e) {
			Toast.makeText(
				ctx,
				"Újraindítás hiba: "+e.getLocalizedMessage(),
				Toast.LENGTH_LONG
			).show();
		}
	}
}
