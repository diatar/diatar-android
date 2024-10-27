package diatar.eu.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import android.text.format.DateFormat;

public final class DtxParams {
    private final String fname;
    private final String title;
    private final String nick;
    private final String group;
    private final int order;
    private final long size;
    private final String timestamp;

    public String fname() { return fname; }
    public String title() { return title; }
    public String nick() { return nick; }
    public String group() { return group; }
    public int order() { return order; }
    public long size() { return size; }
    public String timestamp() { return timestamp; }
    public DtxParams(String _fname, String _title, String _nick, String _group, int _order, long _size, String _timestamp) {
        fname = _fname;
        title = _title;
        nick = _nick;
        group = _group;
        order = _order;
        size = _size;
        timestamp = _timestamp;
    }

    public static class DtxComparator implements java.util.Comparator<DtxParams> {
        public int compare(DtxParams left, DtxParams right) {
            if (!left.group().equals(right.group())) return left.group().compareTo(right.group());
            if (left.order()!=0) {
                if (right.order()!=0) return (left.order()<right.order() ? -1 : +1);
                return -1;  //csak baloldalon van order, az jon elobb
            } else
            if (right.order()!=0) return +1;    //csak jobboldalon van order, az jon elobb
            return left.title().compareTo(right.title());
        }
    }

    public static DtxParams calcParamsOf(File f) throws IOException {
        FileInputStream fis = new FileInputStream(f);
        BufferedReader rd = new BufferedReader(new InputStreamReader(fis));

        String fname = f.getName();
        int order=0;
        String title = fname;
        String nick = fname;
        String grp = "";
        long fsize = f.length();
        String fstamp = DateFormat.format("YYYYMMDDHHmmss", f.lastModified()).toString();

        String ln = rd.readLine();
        while(ln!=null) {
            if (ln.startsWith(">")) break;
            if (ln.startsWith("S")) order=Integer.valueOf(ln.substring(1));
            if (ln.startsWith("N")) title=ln.substring(1);
            if (ln.startsWith("R")) nick=ln.substring(1);
            if (ln.startsWith("C")) grp=ln.substring(1);
            ln = rd.readLine();
        }

        return new DtxParams(f.getName(), title, nick, grp, order, fsize, fstamp);
    }

}
