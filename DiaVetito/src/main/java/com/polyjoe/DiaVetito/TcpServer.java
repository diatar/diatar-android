package com.polyjoe.DiaVetito;

import java.io.*;
import java.net.*;
import java.util.*;
import android.util.*;
import android.content.pm.*;
import eu.diatar.library.*;

public class TcpServer extends Thread
{
	public Globals G;
	public DiaBase Dia;
	public DiaBlank Blank;
	public float density;
	public float ClipLdp,ClipTdp,ClipRdp,ClipBdp;
	public float ClipLpx,ClipTpx,ClipRpx,ClipBpx;
	public boolean mMirror;
	public int mRotate;
	
	private volatile ServerSocket server;
	private volatile Socket client;
	private volatile InputStream ins;
	private volatile int portnum;
	
	private volatile RecBase rectosend;
	private volatile byte rtstype;
	
	private boolean running;
	private MainActivity main;
	
	private RecHdr hdr;
	private RecBase rec;

	static private TcpServer me = null;
	
	private TcpServer() {
		G = new Globals();
		portnum=1024;
	}
	
	static public TcpServer get(MainActivity m) {
		if (me==null) {
			me = new TcpServer();
			me.main=m;
			me.start();
		} else
			me.main=m;
		return me;
	}
	
	public static TcpServer getMe() { return me; }
	
	//public static MainActivity getMain() { return main; }
	
	public void clearMain() { main=null; }
	
	public void Stop() {
		running=false;
		me=null;
	}
	
	public String getVersion(boolean withbuild) {
		String res = "???";
		try {
			PackageInfo pi = main.getPackageManager().getPackageInfo(main.getPackageName(), 0);
			res=pi.versionName;
			if (withbuild) res+=" ("+pi.versionCode+")";
		} catch(Exception e) {}
		return res;
	}

	public String getVerTxt(boolean withbuild) {
		return "Verzió: "+getVersion(withbuild);
	}
	
	public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':')<0;
						if (useIPv4) {
                            if (isIPv4) 
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim<0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) { } // for now eat exceptions
        return "";
    }
	
	private void Msg(String txt) {
		if (main==null) return;
		final String txx = txt;
		main.runOnUiThread(new Runnable() {
			@Override public void run() {
				main.Msg(txx);
			}
		});
	}
	
	private void Err(String txt) {
		if (main==null) return;
		final String txx = txt;
		main.runOnUiThread(new Runnable() {
				@Override public void run() {
					main.Err(txx);
				}
			});
	}
	
	static private String lasterr;
	
	private ServerSocket createServer() {
		try {
			ServerSocket res = new ServerSocket();
			res.setReuseAddress(true);
			res.bind(new InetSocketAddress(portnum));
			lasterr="";
			return res;
		} catch (Exception e) {
			String err = e.getLocalizedMessage();
			if (err!=lasterr) {
				Err("Error: "+err);
				System.out.println("S: Connect error");
				lasterr=err;
			}
			return null;
		}
	}
	
	private Socket createClient() {
		try {
			return server.accept();
		} catch (Exception e) {
			Err("Error: "+e.getLocalizedMessage());
			System.out.println("S: Accept error");
			return null;
		}
	}
	
	private void HdrArrived() {
		switch(hdr.getType()) {
			case RecHdr.itState:
				rec = new RecState();
				break;
			//case RecHdr.itScrSize:
			//	break;
			case RecHdr.itPic:
				rec = new RecPic(hdr.getSize());
				break;
			case RecHdr.itBlank:
				rec = new RecBlank(hdr.getSize());
				break;
			case RecHdr.itText:
				rec = new RecText(hdr.getSize());
				break;
			case RecHdr.itAskSize:
				if (main!=null)
					main.runOnUiThread(new Runnable() {
						@Override public void run() {
							main.OnAskSize();
						}
					});
				break;
			case RecHdr.itIdle:
				break;
		}
		if (rec==null) hdr.clear();
	}
	
	private void RecArrived() {
		if (main!=null)
			switch(hdr.getType()) {
			case RecHdr.itState:
			{
				final RecState r = (RecState)rec;
				main.runOnUiThread(new Runnable() {
						@Override public void run() {
							main.OnState(r);
						}
					});
			}
				break;
			//case RecHdr.itScrSize:
			//	break;
			case RecHdr.itPic:
			{
				final RecPic r = (RecPic)rec;
				main.runOnUiThread(new Runnable() {
						@Override public void run() {
							main.OnPic(r);
						}
					});
			}
				break;
			case RecHdr.itBlank:
			{
				final RecBlank r = (RecBlank)rec;
				main.runOnUiThread(new Runnable() {
						@Override public void run() {
							main.OnBlank(r);
						}
					});
			}
				break;
			case RecHdr.itText:
			{
				final RecText r = (RecText)rec;
				main.runOnUiThread(new Runnable() {
						@Override public void run() {
							main.OnText(r);
						}
					});
			}
				break;
			//case RecHdr.itAskSize:
			//	break;
			//case RecHdr.itIdle:
			//	break;
		}
		rec=null;
		hdr.clear();
	}
	
	private void MainCycle() {
		hdr = new RecHdr();
		try {
			if (server!=null) server.close();
		} catch(Exception e) {}
		server=null;
	
		long lastms = System.currentTimeMillis();
		
		running=true;
		do {
			try {
				if (server==null) {
					Log.d("TcpServer","Server...");
					client=null;
					server = createServer();
					if (server==null) {
						Log.d("TcpServer","server=null");
						sleep(10);
						continue;
					}
					Log.d("TcpServer","IPv4: "+getIPAddress(true));
					Log.d("TcpServer","IPv6: "+getIPAddress(false));
				}
				if (client==null) {
					Log.d("TcpServer","Client...");
					ins=null;
					client=createClient();
					if (client==null) {
						Log.d("TcpServer","client=null");
						sleep(10);
						continue;
					}
					lastms = System.currentTimeMillis();
					Msg("Kapcsolódva!!!");
				}
				if (!client.isConnected() || client.isClosed()) {
					Msg("Szétkapcsolva!");
					client=null;
					continue;
				}
				if (ins==null) {
					ins=client.getInputStream();
					hdr.clear();
					rec=null;
					rectosend=null;
				}
				//beolvasas
				if (ins.available()>0) {
					//Integer ib = b;
					//Debug("byte="+ib.toString());
					if (rec==null) {
						int rlen = ins.read(hdr.buf,hdr.len,hdr.getMaxlen()-hdr.len);
						if (rlen<=0) continue;
						hdr.len+=rlen;
						if (hdr.tryId() && hdr.isFull()) {
							//megjott a fejlec
							HdrArrived();
						}
					} else {
						int rlen = ins.read(rec.buf,rec.len,rec.getMaxlen()-rec.len);
						if (rlen<=0) continue;
						rec.len+=rlen;
						if (rec.isFull() || rec.len>=hdr.getSize()) {
							//megjott a rekord
							RecArrived();
						}
					}
					lastms = System.currentTimeMillis();
				}
				if (rectosend!=null) {
					OutputStream outs = client.getOutputStream();
					RecHdr rh = new RecHdr();
					rh.setID();
					rh.setType(rtstype);
					rh.setSize(rectosend.getMaxlen());
					outs.write(rh.buf);
					outs.write(rectosend.buf);
					outs.flush();
					rectosend=null;
					lastms = System.currentTimeMillis();
				}
				if (lastms < System.currentTimeMillis()-5000) {
					try {
						OutputStream outs = client.getOutputStream();
						RecHdr rh = new RecHdr();
						rh.setID();
						rh.setType(rh.itIdle);
						rh.setSize(0);
						outs.write(rh.buf);
						outs.flush();
					} catch(Exception e) {
						client=null;
						Msg("Szétkapcsolva...");
					}
					lastms = System.currentTimeMillis();
				}
			} catch(Exception e) {
				Err("Error: "+e.getLocalizedMessage());
				System.out.println("S: Error");
				e.printStackTrace();
			}
		} while(running);
	}
	
	public void sendRec(RecBase thisrec, byte rectype) {
		rtstype=rectype;
		rectosend=thisrec;
	}
	
	public void setPort(int newval) {
		if (newval==portnum) return;
		portnum=newval;
		try {
			if (server!=null) server.close();
		} catch(Exception e) {}
		server=null;
	}
	
	@Override
	public void run() {
		MainCycle();
	}
}
