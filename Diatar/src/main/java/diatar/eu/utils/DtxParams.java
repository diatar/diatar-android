package diatar.eu.utils;

public final class DtxParams {
    private final String fname;
    private final String title;
    private final String group;
    private final int order;
    private final long size;
    private final String timestamp;

    public String fname() { return fname; }
    public String title() { return title; }
    public String group() { return group; }
    public int order() { return order; }
    public long size() { return size; }
    public String timestamp() { return timestamp; }
    public DtxParams(String _fname, String _title, String _group, int _order, long _size, String _timestamp) {
        fname = _fname;
        title = _title;
        group = _group;
        order = _order;
        size = _size;
        timestamp = _timestamp;
    }
}
