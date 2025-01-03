package diatar.eu.utils;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

import diatar.eu.DiaItem;

public class DecodeBreviar {
  private final HtmlParser.HtmlTag mTag0;
  private DiaItem mCurrLit;
  private HtmlParser.HtmlTag mCurrTag;
  private int mPsalmState = 0;        //0=nem zsoltar, 1=antifona, 2=versek
  private HtmlParser.TagAndTxt mFound;
  private String mVersNev;
  private int mOlvCnt = 0, mZsoltCnt = 0, mHymnCnt = 0, mRespCnt = 0;
  private int mOlvMax;   //olvasmany tordeleshez ennyi keruljon egy diara
  private boolean isKonyorges = false;

  public DecodeBreviar(HtmlParser.HtmlTag tag0) {
    mTag0=tag0;
    mOlvMax=300;
    DiaItem.Clear();
  }

  private int minX(int i1, int i2) {
    if (i1<0) return i2;
    if (i2<0) return i1;
    return Math.min(i1, i2);
  }

  private void addLine(String txt) {
    if (mCurrLit==null) startVers("???");
    int p0=0;
    ArrayList<String> lit;
    lit = ( mCurrLit.mTxt==null ? new ArrayList<>() : new ArrayList<>(Arrays.asList(mCurrLit.mTxt)) );
    while (p0<txt.length()) {
      int p=minX(txt.indexOf('\n',p0), txt.indexOf('\r', p0));   /* 240101_0c Dicsoseg... */
      if (p<0) break;
      String s=txt.substring(p0,p);
      if (!s.isEmpty()) lit.add(s);
      p0=p+1;
    }
    String s=txt.substring(p0);
    if (!s.isEmpty()) lit.add(s);

    mCurrLit.mTxt = new String[lit.size()];
    lit.toArray(mCurrLit.mTxt);
  }

  private void startVers(String txt) {
    mVersNev=txt;
    mPsalmState=0;
    mCurrLit = new DiaItem(DiaItem.ditTXT);
    mCurrLit.mKnev=txt;
    mCurrLit.AppendMe();
  }

  private void startVszak(String txt) {
    int ps=mPsalmState;
    if (mCurrLit==null) startVers("???");
    if (mCurrLit.mTxt!=null && mCurrLit.mTxt.length>0) startVers(mVersNev);
    mPsalmState=ps;
    mCurrLit.mKnev=mVersNev+"/"+txt;
  }

  private String allTxt(HtmlParser.HtmlTag tag) {
    StringBuilder sb = new StringBuilder();
    boolean first=true;
    for (HtmlParser.TagAndTxt tat : tag.subTags) {
      HtmlParser.HtmlTag t = tat.tag;
      if (!first && (t==null || !t.getNameStr().equals("!--"))) sb.append(' ');
      if (t!=null && !t.getNameStr().equals("!--")) {
        String cls=t.getClassName();
        if (cls.startsWith("tts_pause") || !cls.contains("red")) {  /* 240101_0r respons */
          sb.append(allTxt(t)); sb.append(' ');
        }
      }
      if (tat.txt!=null) sb.append(tat.txt);
      first=false;
    }
    return sb.toString();
  }

//////////////////////////////////////////////

  public void decode() {
    boolean afterhdr=false;
    mFound= mTag0.traverse();
    isKonyorges=false;

    while(mFound!=null) {
      mCurrTag=mFound.tag; if (mCurrTag==null) break;
      String cls=mCurrTag.getClassName();
      Log.d("DecodeBreviar", mCurrTag.getNameStr()+": class='"+cls+"'");
      if (!afterhdr) {
        if (cls.equals("tts_heading")) afterhdr=true;
      } else //tts_heading utan vagyunk
      if (cls.equals("respons")) {
        doRespons();
      } else /* 240108_0r konyorges */
      if (cls.equals("strong") || cls.equals("par")) {
        doStrongs();
      } else if (cls.equals("hymn")) {
        doHymn();
      } else if (cls.startsWith("antiphon")) {
        if (mPsalmState<2 || cls.contains("begin")) mPsalmState=0;
        doAnt(mCurrTag);
      } else if (cls.contains("psalm")) {
        if (mPsalmState>=2) mPsalmState=0;
        doPsalm();
      } else /* 240101_0r */
      if (cls.equals("bibleref")) {
        doBibleref();
      } else /* 240101_0c */
      if (cls.equals("reading")) {
        doReading();
      } else /* 240101_0r */
      if (cls.equals("preces")) {
        doPreces();
      } else /* 240101_0c */
      if (cls.equals("ending")) {
        mFound=mFound.tag.traverse(mTag0); //belemegyunk
        continue;
      } else //felesleges dolgok
      if (mCurrTag.getNameStr().equals("!--")) {
        //megjegyzes
      } else //piros feliratok
      if (cls.contains("red")) {
        if (!mCurrTag.subTags.isEmpty() && mCurrTag.subTags.get(0).txt.equals("KÖNYÖRGÉS")) {
          isKonyorges=true;  /* 240101_0r */
        }
      } else //szekcio kezdete
      if (cls.equals("tts_section")) {
        mPsalmState=0;
        isKonyorges=false;
      } else //kereszthivatkozas
      if (mCurrTag.getNameStr().equals("a")) {
        //
      } else //engedely felirat
      if (cls.equals("patka")) {
        //
      } else //navigacio
      if (cls.equals("nav")) {
        //
      } else //ez egy instrukcio
      if (cls.equals("rubric-always-display")) {
        /* 240416_0c */
      } else //formatum vagy elvalaszto csoport... vagymi
      if (cls.isEmpty() && mCurrTag.getNameStr().equals("div")) { /* 240101_09 <div style> */
        doEmptyDIV();
      } else //ures paragrafus... vagymi
      if (cls.isEmpty() && mCurrTag.getNameStr().equals("p")) { /* 240101_09 <div style> */
        doEmptyP();
      } else //kozepre irt szoveg (pl. "Vagy:")
      if (cls.equals("center rubric")) {
        /* 240101_0i */
      } else { //barmi egyeb
        mPsalmState=0;
        startVers("??? <"+mCurrTag.getNameStr()+" class='"+cls+"'>");
      }
      if (mFound==null) return; //mFound valtozhatott!
      mFound=mFound.tag.traverse(mTag0,afterhdr);
    }
  }

  private void startXXX(int cnt, String txt) {
    startVers(cnt>1 ? cnt+"."+txt : txt);
  }

  private void startHymn() {
    startXXX(++mHymnCnt,"Himnusz");
  }

  private void startOlv() {
    startXXX(++mOlvCnt,"Olvasmány");
  }

  private void startZsolt() {
    startXXX(++mZsoltCnt,"Zsoltár");
  }

  private void startResp() {
    startXXX(++mRespCnt,"Responsorium");
  }

  private void doRespons() {
    startResp();
    HtmlParser.TagAndTxt f= mCurrTag.traverse(mCurrTag);
    while (f!=null) {
      boolean skip=false;
      if (f.tag.getNameStr().equals("p")) {
        String s=allTxt(f.tag);
        String cls=f.tag.getClassName();
        if (cls.equals("respV")) s="V: "+s;
        else if (cls.equals("respF")) s="F: "+s;
        addLine(s);
        skip=true;
      }
      f=f.tag.traverse(mCurrTag,skip);
    }
  }

  private void doHymn() {
    boolean first=true, last=false;
    int vszak=1, lcnt=0;
    HtmlParser.TagAndTxt f=mCurrTag.traverse(mCurrTag);
    while (f!=null) {
      boolean skip=false;
      if (f.tag.getNameStr().equals("p")) {
        String cls=f.tag.getClassName();
        if (cls.startsWith("rubric")) {  //alternativ himnusz kezdodik
          if (vszak>1 || lcnt>0) startHymn();
          first=true; last=false; vszak=1; lcnt=0;
          f.tag.traverse(mCurrTag,true);
          continue;
        }
        if (first || lcnt>=6) {
          if (!first) vszak++;
          startVszak(vszak+"");
          lcnt=0;
        }
        if (cls.contains("first")) {
          if (!first) vszak++;
          first=true;
        } else if (cls.contains("last")) {
          vszak++;
          last=true;
        }
        addLine(allTxt(f.tag));
        lcnt++;
        first=last; last=false;
        skip=true;
      }
      f=f.tag.traverse(mCurrTag,skip);
    }
  }

  private void doAnt(HtmlParser.HtmlTag tag) {
    if (mPsalmState==0) startZsolt();
    mPsalmState=1;
    startVszak("Ant");
    StringBuilder sb = new StringBuilder();
    for (HtmlParser.TagAndTxt tat : tag.subTags) {
      if (tat.tag!=null) {
        if (tat.tag.getClassName().contains("red")) continue;
        if (tat.tag.getNameStr().equals("p")) { sb.append(' '); sb.append(allTxt(tat.tag)); }
      }
      sb.append(' '); if (tat.txt!=null) sb.append(tat.txt);
    }
    addLine("Ant: "+sb.toString().trim());
  }

  private void doPsalm() {
    if (mPsalmState==0) {
      if (mCurrTag.getClassName().startsWith("tedeum"))
        startVers("Te Deum");
      else
        startZsolt();
    }
    mPsalmState=2;
    HtmlParser.TagAndTxt f=mCurrTag.traverse(mCurrTag);
    int vszak=0, lcnt=9999;
    while (f!=null) {
      boolean skip=false;
      String cls=f.tag.getClassName();
      if (cls.contains("red") || cls.equals("bibleref")) {
        f=f.tag.traverse(mCurrTag,true);
        continue;
      }
      if (f.tag.getNameStr().equals("p")) {
        if (lcnt>9 || (cls.startsWith("verse") && cls.contains("start"))) {
          if (lcnt>=4 || cls.contains("first")) {
            vszak++;
            startVszak(vszak+"");
            lcnt=0;
          }
        }
        addLine(allTxt(f.tag));
        lcnt++;
      } else if (f.tag.getNameStr().equals("div") && cls.startsWith("antiphon")) {
        doAnt(f.tag);
        mPsalmState=2;
        lcnt=9999;
        skip=true;
      }
      f=f.tag.traverse(mCurrTag,skip);
    }
  }

  private void doBibleref() {
    startVers("Rövid olvasmány");
    mFound= mCurrTag.traverse(mTag0,true);
    if (mFound==null) return;
    addLine(allTxt(mFound.tag));
  }

  private HtmlParser.TagAndTxt goNextTag() {
    HtmlParser.TagAndTxt res=mFound;
    do {
      res=res.tag.traverse(mTag0,true);
      if (res==null) return res;
    } while (res.tag.getNameStr().equals("!--"));
    return res;
  }

  private void doStrongs() {
    String s=allTxt(mCurrTag);
    if (s.startsWith("Dicsőség az")) {
      startVers("Dicsőség");
      addLine(s);
      return;
    }
    if (isKonyorges
            || s.startsWith("Könyörögjünk!")
            || s.startsWith("Istenünk,")
            || s.startsWith("Kérünk")
    ) {
      startVers("Könyörgés");
      addLine(s);
      mFound=goNextTag();
      if (mFound==null) return;
      addLine(allTxt(mFound.tag));
      HtmlParser.TagAndTxt f=goNextTag();
      if (f==null || !f.tag.getClassName().equals("respF")) return;
      addLine("F: "+allTxt(f.tag));
      mFound=f;
      return;
    }
    if (s.startsWith("Mondjunk áldást")
            || s.startsWith("Az Úr áldjon")
            || s.startsWith("A nyugodalmas")
    ) {
      startVers("Áldás");
      addLine(s);
      HtmlParser.TagAndTxt f=goNextTag();
      if (f==null || !f.tag.getClassName().equals("respF")) return;
      addLine("F: "+allTxt(f.tag));
      mFound=f;
      return;
    }
    startVers("Ima");
    addLine(s);
  }

  private int tordeles(String txt, int startpos, int vszak) {
    int len=txt.length();
    if (startpos+len<=mOlvMax) {
      addLine(txt);
      return vszak;
    }
    int pgcnt=1+((startpos+len)/mOlvMax);
    int p0=(len/pgcnt) - startpos;
    int psp=9999, ptor=9999;
    for (int i=0; i<=50; i++) {
      if (p0+i<len) {
        char c=txt.charAt(p0+i);
        if (c==' ' && psp==9999) psp=i;
        if (c=='\n' || c=='\r' || c=='.' || c==',' || c==';' || c=='?' || c=='!') {
          ptor=i;
          break;
        }
      }
      if (p0-i>0) {
        char c=txt.charAt(p0-i);
        if (c==' ' && psp==9999) psp=-i;
        if (c=='\n' || c=='\r' || c=='.' || c==',' || c==';' || c=='?' || c=='!') {
          ptor=-i;
          break;
        }
      }
    }
    if (ptor==9999) ptor=psp; //nem volt irasjel a kornyeken...
    if (ptor==9999) ptor=0;   //szokoz se :(
    addLine(txt.substring(0,p0+ptor));
    vszak++;
    startVszak(vszak+"");
    return tordeles(txt.substring(p0+ptor+1,len),0, vszak);
  }

  private void doReading() {
    startOlv();
    int vszak=0;
    String sectionpar="";
    for (HtmlParser.TagAndTxt tat : mCurrTag.subTags) {
      if (tat.tag==null) continue;
      if (tat.tag.getNameStr().equals("!--")) continue; //megjegyzes
      String cls=tat.tag.getClassName();
      if (cls.equals("heading")) continue;
      if (cls.equals("bibleref")) continue;
      if (cls.equals("reading-title")) continue;
      if (cls.equals("reading-source")) continue;
      if (cls.equals("resp")) {
        startVszak("Resp");
        continue;
      }
      if (cls.equals("respV")) {
        addLine("V: "+allTxt(tat.tag));
        continue;
      }
      if (cls.equals("respF")) {
        addLine("F: "+allTxt(tat.tag));
        continue;
      }
      if (cls.equals("section par")) {
        sectionpar=allTxt(tat.tag);
        continue;
      }
      if (cls.equals("par")) {
        vszak++;
        startVszak(vszak+"");
        if (!sectionpar.isEmpty()) vszak=tordeles(sectionpar,0,vszak);
        if (sectionpar.length()>20) {
          vszak++;
          startVszak(vszak+"");
          sectionpar="";
        }
        vszak=tordeles(allTxt(tat.tag), sectionpar.length(), vszak);
        sectionpar="";
        continue;
      }
      addLine("??? <p class='"+cls+"'>");
    }
    if (!sectionpar.isEmpty()) {
      vszak++;
      startVszak(vszak+"");
      addLine(sectionpar);
    }
  }

  private void doPreces() {
    startVers("Fohászok");
    boolean first=true;
    int vszak=0;
    for (HtmlParser.TagAndTxt tat : mCurrTag.subTags) {
      if (tat.tag==null) continue;
      if (tat.tag.getNameStr().equals("!--")) continue;
      String cls=tat.tag.getClassName();
      if (cls.equals("intro")) {
        startVszak("Bev");
        first=false;
        addLine(allTxt(tat.tag));
        continue;
      }
      if (cls.contains("resp")) {
        if (first) startVszak("???");
        addLine("F: "+allTxt(tat.tag));
        first=true;
        continue;
      }
      if (first) {
        vszak++;
        startVszak(vszak+"");
        first=false;
      }
      if (cls.equals("partR")) {
        addLine("R: "+allTxt(tat.tag));
        continue;
      }
      if (cls.equals("partV")) {
        addLine("V: "+allTxt(tat.tag));
        continue;
      }
      addLine("??? <"+tat.tag.getNameStr()+" class='"+cls+"'>");
    }
  }

  private void doEmptyDIV() {
    if (mCurrTag.subTags.isEmpty()) return;
    addLine("??? üres DIV ???");
  }

  private void doEmptyP() {
    String s=allTxt(mCurrTag).trim();
    if (s.startsWith("Mi Atyánk")) {
      startVers("Miatyánk");
      int p=s.indexOf("Mindennapi");
      if (p<0) p=150; //korulbelul...
      startVszak("1");
      addLine(s.substring(0,p));
      startVszak("2");
      addLine(s.substring(p));
      return;
    }
    if (!s.isEmpty()) addLine("??? üres P: '"+s+"'");
  }

}
