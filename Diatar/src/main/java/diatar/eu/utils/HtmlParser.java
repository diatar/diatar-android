package diatar.eu.utils;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class HtmlParser {
    static public class HtmlProperty {
        public String nameStr;
        public String valueStr;
    }

    //subtagok listaja: subtag + utana levo szoveg
    // - ha a body szoveggel kezdodik, az elso Tag ures
    // - ha subtaggel er veget, az utolso Txt ures
    // - ha ket subtag kozott nincs szoveg, az adott Txt ures
    static public class TagAndTxt {
        public HtmlTag tag;
        public String txt;
    }

    static public class HtmlTag {
        private final HtmlTag parentTag;
        private String nameStr;
        public ArrayList<HtmlProperty> htmlProps = new ArrayList<>();
        public ArrayList<TagAndTxt> subTags = new ArrayList<>();

        public HtmlTag(HtmlTag parent) {
            parentTag=parent;
        }

        public String getNameStr() { return nameStr; }
        public HtmlProperty findProperty(String name) {
            for (HtmlProperty p : htmlProps)
                if (p.nameStr.equals(name)) return p;
            return null;
        }
        public String getPropVal(String key) {
            HtmlProperty p = findProperty(key);
            return (p==null ? "" : p.valueStr);
        }
        public String getClassName() {
            return getPropVal("class");
        }
        public int indexOf(HtmlTag subtag) {
            int idx=subTags.size()-1;
            while (idx>=0 && subTags.get(idx).tag!=subtag) idx--;
            return idx;
        }

        public TagAndTxt traverse() { return traverse(null, false); }
        public TagAndTxt traverse(HtmlTag basetag) { return traverse(basetag, false); }
        public TagAndTxt traverse(HtmlTag basetag, boolean skipsubtags) {
            int idx;
            HtmlTag p;
            if (!skipsubtags) {
                idx=0;
                while (idx<subTags.size() && subTags.get(idx).tag==null) idx++;
                if (idx<subTags.size()) return subTags.get(idx);
            }
            //addig lepkedunk feljebb amig valahol egy kovetkezo subtaget talalunk
            p=this;
            do {
                if (p==basetag) return null;        //ez a bazis, nincs tovabb
                if (p.parentTag==null) return null; //ez hiba, de jatsszunk szepen!
                idx=p.parentTag.indexOf(p);
                if (idx<0) return null;             //ez is hiba: nem vagyunk a szulonk gyereke
                p=p.parentTag;
                do {
                    idx++;
                } while (idx<p.subTags.size() && p.subTags.get(idx).tag==null);
            } while (idx>=p.subTags.size());
            return p.subTags.get(idx);
        }

        private String entityToChar(@NonNull String txt) {
            if (txt.equals("nbsp")) return " "; //#160);
            if (txt.equals("lt")) return "<";
            if (txt.equals("gt")) return ">";
            if (txt.equals("amp")) return "&";
            if (txt.equals("quot")) return "\"";
            if (txt.equals("cent")) return "¢";
            if (txt.equals("pound")) return "£";
            if (txt.equals("yen")) return "¥";
            if (txt.equals("euro")) return "€";
            if (txt.equals("copy")) return "©";
            if (txt.equals("reg")) return "®";
            return "&"+txt+";";
        }

        //p = tagname utan, vissza: '>' vagy '/>' pozicion
        private int parseProperties(@NonNull String html, int p) {
            int len=html.length();
            while (p<len) {
                //szokozok atlepese
                while (p<len && html.charAt(p)==' ') p++;
                if (p>=len || html.charAt(p)=='/' || html.charAt(p)=='>') return p;

                //nev keresese
                int p0=p;
                while (p<len) {
                    char ch = html.charAt(p);
                    if (ch == ' ' || ch == '=' || ch == '/' || ch == '>') break;
                    p++;
                }
                HtmlProperty newprop = new HtmlProperty();
                htmlProps.add(newprop);
                newprop.nameStr=html.substring(p0,p);

                //szokozok atlepese
                while (p<len && html.charAt(p)==' ') p++;
                if (p>=len || html.charAt(p)=='/' || html.charAt(p)=='>') return p;

                if (html.charAt(p)!='=') continue;  //nincs ertek
                p++;

                //szokozok atlepese
                while (p<len && html.charAt(p)==' ') p++;
                if (p>=len || html.charAt(p)=='/' || html.charAt(p)=='>') return p;

                p0=p;
                if (html.charAt(p)=='"') {  //doublequoted
                    p0++; p++;
                    while (p<len && html.charAt(p)!='"') p++;
                } else if (html.charAt(p)=='\'') {  //singlequoted
                    p0++; p++;
                    while (p<len && html.charAt(p)!='\'') p++;
                } else {    //nincs idezojel, egyetlen szo
                    while (p<len) {
                        char ch=html.charAt(p);
                        if (ch==' ' || ch=='/' || ch=='>') break;
                        p++;
                    }
                }
                if (p0<p) newprop.valueStr=html.substring(p0,p);
                if (p>=len) return p;
                if (html.charAt(p)=='\'' || html.charAt(p)=='"') p++;
            }
            return p;
        }

        //p = '<' pozicioja, vissza: '>' pozicio utan
        public int parseTag(@NonNull String html, int p) {
            int len=html.length();

            //comment spec.kezelese
            if (html.startsWith("<!--", p)) {
                nameStr="!--";
                int p2=html.indexOf("-->", p + 4);
                if (p2<0) p2=len;
                TagAndTxt tat = new TagAndTxt();
                tat.txt=html.substring(p+4,p2);
                tat.tag=null;
                subTags.add(tat);
                return p2+3;
            }

            //fejlec dekodolasa
            int p2=p++;
            while (p<len && html.charAt(p)!=' ' && html.charAt(p)!='>') p++;
            nameStr=html.substring(p2,p);
            p=parseProperties(html,p);
            if (p>=len) return p;
            if (html.charAt(p)=='/') return p+2;  // <xxxx/> tipusu tag
            p++;

            //torzsresz dekodolasa
            StringBuilder s= new StringBuilder();
            while (p<len) {
                if (html.charAt(p) == '&') {
                    p2 = p;
                    while (p2 < len && html.charAt(p2) != ';') p2++;
                    if (p2 >= len) break;
                    //utf8 chars
                    if (p + 1 < len && html.charAt(p + 1) == '#') {
                        int uch;
                        if (p + 2 < len && html.charAt(p + 2) == 'x')
                            uch = Integer.parseInt(html.substring(p + 3, p2), 16);
                        else
                            uch = Integer.parseInt(html.substring(p + 2, p2), 10);
                        s.append(new String(Character.toChars(uch)));
                        p = p2 + 1;
                        continue;
                    }
                    s.append(entityToChar(html.substring(p + 1, p2)));
                    p = p2 + 1;
                    continue;
                }

                //tags
                if (html.charAt(p) == '<') {
                    // <br>, <br/>, <br />
                    if (html.startsWith("<br", p) && p + 3 < len) {
                        char ch = html.charAt(p + 3);
                        if (ch == ' ' || ch == '/' || ch == '>') {
                            s.append("\n");
                            while (p < len && html.charAt(p) != '>') p++;
                            p++;
                            continue;
                        }
                    }


                    // </xxxx>  = zaro formula
                    if (p + 1 < len && html.charAt(p + 1) == '/') {
                        if (html.startsWith("</" + nameStr + ">", p)) {
                            p += nameStr.length() + 3;
                            break;
                        }
                        //nem passzolo zaro tag
                        while (p < len && html.charAt(p) != '>') p++;
                        p++;
                        continue;
                    }

                    //igazi subtag kezdodik
                    if (nameStr.equals("p") && (html.startsWith("<p>", p) || html.startsWith("<p ", p))) {
                        //hianyzo paragrafus-zaras
                        break;
                    }
                    p2 = subTags.size();
                    if (s.length() > 0) {
                        if (p2 == 0) {    //szoveggel kezdodott a body
                            p2++;
                            TagAndTxt tat = new TagAndTxt();
                            subTags.add(tat);
                            tat.tag = null;
                        }
                        subTags.get(p2 - 1).txt = s.toString();
                        s = new StringBuilder();
                    }
                    TagAndTxt tat = new TagAndTxt();
                    subTags.add(tat);
                    tat.tag = new HtmlTag(this);
                    p = tat.tag.parseTag(html, p);
                    continue;
                }
                s.append(html.charAt(p));
                p++;
            }
            //dekodolas vege
            if (s.length() > 0) {
                p2 = subTags.size();
                if (p2 == 0) {    //csak szoveg volt
                    p2++;
                    TagAndTxt tat = new TagAndTxt();
                    subTags.add(tat);
                    tat.tag = null;
                }
                subTags.get(p2 - 1).txt = s.toString();
            }

            return p;
        }
    }

    static public HtmlTag parseHtml(String html) {
        int p=html.indexOf("<html");
        if (p<0) return null;

        HtmlTag ret = new HtmlTag(null);
        ret.parseTag(html,p);
        return ret;
    }
}
