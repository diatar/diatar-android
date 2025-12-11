package eu.diatar.library;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.datatypes.MqttTopic;
import com.hivemq.client.mqtt.datatypes.MqttTopicFilter;
import com.hivemq.client.mqtt.datatypes.MqttUtf8String;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAck;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class MqttInterface {
    private final Mqtt3AsyncClient client;

    private Consumer<String> OnErrorCallback;
    private Consumer<Void> OnCompletedCallback;
    private Consumer<RecState> OnRecStateCallback;
    private Consumer<RecPic> OnRecPicCallback;
    private Consumer<RecBlank> OnRecBlankCallback;
    private Consumer<RecText> OnRecTextCallback;

    public static class UserArray {
        public String UserName; //felhasznalonev
        public String Email;  //email-cim
        public String[] Channels = new String[10]; //kuldesi csatornak
        public boolean SentForDetails; //nev elkuldve a reszletek lekerdezesere
        public boolean SendersGroup; //normal felhasznalo
    }

    private UserArray[] users;

    public String Username, Password, Channel;
    private String topicGroup, topicMask, topicBlank, topicState, topicDia;
    private boolean isOpen;
    public enum OpenMode {
        //normal user mod:
        omRECEIVER,                //vetel
        omSENDER,                  //kuldes

        //adminisztracio:
        omFIRST_ADMIN,
        omUSERLIST,                //felhasznaloi lista
        omCREATEUSER,              //uj felhasznalo letrehozasa
        omCHKLOGIN,                //belepesi kepesseg ellenorzese
        omRENCHANNEL,              //csatorna atnevezese
        omNEWPSW,                  //uj jelszo
        omNEWEMAIL,                //uj email
        omMODUSER,                 //nevvaltoztatas
        omDELUSER,                 //felhasznalo torlese
        omLAST_ADMIN,
    }
    private OpenMode openMode;

    //singleton
    static private MqttInterface me = null;

    static public MqttInterface getInstance() {
        if (me==null) me = new MqttInterface();
        return me;
    }

    //ctor-dtor
    public MqttInterface() {
        client = MqttClient.builder()
                .useMqttVersion3()
                .identifier(UUID.randomUUID().toString())
                .serverHost("mqtt.diatar.eu")
                .serverPort(1883)
                .automaticReconnect()
                    .initialDelay(1, java.util.concurrent.TimeUnit.SECONDS)
                    .maxDelay(30, java.util.concurrent.TimeUnit.SECONDS)
                    .applyAutomaticReconnect()
                .addConnectedListener(context -> {
                    registerPublishHandler();
                    subscribeTopics();
                })
                .buildAsync();
    }

    public void Destroy() {
        close();
        me=null;
    }

    //********************************************************
    // dynsec valaszok
    //********************************************************

    private void messageReceived(String txt) {
        try {
            JSONObject jAll = new JSONObject(txt);
            JSONArray jResp = jAll.getJSONArray("responses");
            boolean iscont = false;
            for (int idx = 0; idx < jResp.length(); idx++)
                if (processResponse(jResp.getJSONObject(idx), iscont)) iscont=true;
            if (!iscont) {
                doOnCompleted(null);
                close();
            }
        } catch (Exception e) {
            doOnError("Hibás input!");
        }
    }

    //********************************************************
    // dynsec input feldolgozasa
    //********************************************************

    // TRUE = ujabb kerest kuldtunk, folytatas kell
    private boolean processResponse(JSONObject jResp, boolean iscont) {
        JSONObject jErr = jResp.optJSONObject("error");
        if (jErr != null) {
            //hiba tortent
            doOnError("Adminisztrációs hiba: "+jErr.toString());
            return false;
        }
        String cmd = jResp.optString("command").toUpperCase();
        if (cmd.equals("LISTCLIENTS")) {
            return processLISTCLIENTS(jResp, iscont);
        } else if (cmd.equals("GETCLIENT")) {
            return processGETCLIENT(jResp, iscont);
        } else if (cmd.equals("CREATECLIENT")) {
            return processCREATECLIENT(jResp, iscont);
        } else if (cmd.equals("MODIFYCLIENT")) {
            return processMODIFYCLIENT(jResp, iscont);
        } else if (cmd.equals("DELETECLIENT")) {
            return processDELETECLIENT(jResp, iscont);
        } else if (cmd.equals("SETCLIENTPASSWORD")) {
            return processSETCLIENTPASSWORD(jResp, iscont);
        }
        return false;
    }

    private boolean processLISTCLIENTS(@NonNull JSONObject jResp, boolean iscont) {
        JSONObject jData = jResp.optJSONObject("data");
        if (jData == null) {
            //hianyzik a 'data'
            return false;
        }
        JSONArray jClients = jData.optJSONArray("clients");
        if (jClients == null) {
            //hianyzik a 'client' tomb
            return false;
        }
        int len = jClients.length();
        UserArray[] uarr = new UserArray[len];
        for (int i=0; i<len; i++) {
            uarr[i] = new UserArray();
            uarr[i].UserName = jClients.optString(i);
        }
        users = uarr;

        return iscont || sendUserDetails();
    }

    private boolean processGETCLIENT(JSONObject jResp, boolean iscont) {
        JSONObject jData = jResp.optJSONObject("data");
        if (jData == null) {
            //hianyzik a 'data'
            return iscont || sendUserDetails();
        }
        JSONObject jClient = jData.optJSONObject("client");
        if (jClient == null) {
            //hianyzik a 'client'
            return iscont || sendUserDetails();
        }
        String uname = jClient.optString("username");
        if (uname.isEmpty()) {
            //hianyzik a nev
            return iscont || sendUserDetails();
        }

        //megkeressuk a nevet a tombben
        int idx = users.length;
        while (idx-->0 && !users[idx].UserName.equals(uname)) continue;
        if (idx<0) {
            //ez elvileg nem lehet, de...
            idx=users.length;
            users = Arrays.copyOf(users, idx+1);
            users[idx].UserName=uname;
            users[idx].SentForDetails=true;
        }

        //email es kuldesi csatornak
        users[idx].Email = jClient.optString("textname");
        fillChannels(idx, jClient.optString("textdescription"));

        //kuldo felhasznalo?
        JSONArray jRoles = jClient.optJSONArray("roles");
        if (jRoles != null) {
            for (int i = 0; i < jRoles.length(); i++) {
                String rname = jRoles.optJSONObject(i).optString("rolename");
                if (rname.startsWith("s-")) {
                    users[idx].SendersGroup=true;
                    break;
                }
            }
        }

        return iscont || sendUserDetails();
    }

    private boolean processCREATECLIENT(JSONObject jResp, boolean iscont) {
        return false;
    }

    private boolean processMODIFYCLIENT(JSONObject jResp, boolean iscont) {
        return false;
    }

    private boolean processDELETECLIENT(JSONObject jResp, boolean iscont) {
        return false;
    }

    private boolean processSETCLIENTPASSWORD(JSONObject jResp, boolean iscont) {
        return false;
    }

    //**************************************************************
    // dynsec kuldesi rutinok
    //**************************************************************

    //TRUE kikuldott egy rekordot
    private boolean sendUserDetails() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"commands\": [");
        int cnt=0;
        for (int i=0; i<users.length; i++) {
            if (users[i].SentForDetails) continue;
            users[i].SentForDetails=true;
            if (cnt>0) sb.append(", ");
            sb.append("{\"command\": \"getClient\", \"username\": \"");
            sb.append(users[i].UserName);
            sb.append("\"}");
            cnt++;
            if (cnt>=10) break;
        }
        if (cnt<=0) return false;
        sb.append("]}");
        //send command
        client.publishWith()
                .topic(MqttPsw.DYNSECTOPIC)
                .payload(sb.toString().getBytes())
                .send()
                .exceptionally(throwable -> {
                    doOnError("Adatküldési hiba: "+throwable.getLocalizedMessage());
                    return null;
                });
        return true;
    }

    //**************************************************************
    // vetitesi rutinok
    //**************************************************************

    private void projectionReceived(@NonNull Mqtt3Publish publish) {
        MqttTopic topic = publish.getTopic();
        if (topic.compareTo(MqttUtf8String.of(topicBlank))==0) {
            processBlank(publish.getPayloadAsBytes());
        } else if (topic.compareTo(MqttUtf8String.of(topicState))==0) {
            processState(publish.getPayloadAsBytes());
        } else if (topic.compareTo(MqttUtf8String.of(topicDia))==0) {
            processDia(publish.getPayloadAsBytes());
        }
    }

    private void processBlank(@NonNull byte[] buf) {
        RecBlank rec = new RecBlank(buf.length);
        rec.buf=buf;
        rec.len=buf.length;
        doOnRecBlank(rec);
    }

    private void processState(@NonNull byte[] buf) {
        RecState rec = new RecState();
        rec.buf=buf;
        rec.len=buf.length;
        doOnRecState(rec);
    }

    private void processDia(@NonNull byte[] buf) {
        if (buf.length<2) return;
        if (buf[0]==(byte)'P') {
            RecPic rp = new RecPic(buf.length-1);
            rp.buf=Arrays.copyOfRange(buf,1,buf.length);
            rp.len=buf.length-1;
            doOnRecPic(rp);
        } else if (buf[0]==(byte)'T') {
            RecText rt = new RecText(buf.length-1);
            rt.buf=Arrays.copyOfRange(buf,1,buf.length);
            rt.len=buf.length-1;
            doOnRecText(rt);
        }
    }

    //**************************************************************
    // belső rutinok
    //**************************************************************

    private void fillChannels(int idx, @NonNull String txt) {
        int arridx = 0, len = txt.length();
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<len; i++) {
            char ch=txt.charAt(i);
            if (ch=='|') {
                if (i+1<len && txt.charAt(i+1)=='|') {
                    sb.append('|');
                    i++;
                    continue;
                }
                users[idx].Channels[arridx]=sb.toString().trim();
                sb.setLength(0);
                arridx++;
                if (arridx>=10) return;
                continue;
            }
            sb.append(ch);
        }
    }

    //ekezet nelkuli es nagybetus
    private String unaccent(String txt) {
        String ret = Normalizer.normalize(txt, Normalizer.Form.NFD);
        return ret.replaceAll("\\p{M}", "").toUpperCase();
    }

    private CompletableFuture<Void> ensureDisconnected() {
        if (client.getState().isConnected()) {
            return client.disconnect().thenCompose(v -> {
                isOpen=false;
                return CompletableFuture.completedFuture(null);
            });
        } else {
            return CompletableFuture.completedFuture(null);
        }
    }

    private CompletableFuture<Mqtt3ConnAck> connectAdmin() {
        return ensureDisconnected().thenCompose(v ->
            client.connectWith()
                .simpleAuth()
                .username(decodePsw(MqttPsw.DYNSECSUPERUSER))
                .password(decodePsw(MqttPsw.DYNSECSUPERPSW).getBytes())
                .applySimpleAuth()
                .send()
        );
    }

    private void executeAdmin(String cmd) {
        connectAdmin().thenCompose(connAck ->
            client.subscribeWith()
                .topicFilter(MqttPsw.DYNSECTOPICRESPONSE)
                .send()
            .thenCompose(subAck -> {
                registerPublishHandler();
                return client.publishWith()
                        .topic(MqttPsw.DYNSECTOPIC)
                        .payload(cmd.getBytes())
                        .send();
            }).exceptionally(throwable -> {
                close();
                doOnError("Internet hiba: "+throwable.getLocalizedMessage());
                return null;
            })
        );
    }

    private void registerPublishHandler() {
        if (openMode.ordinal() < OpenMode.omFIRST_ADMIN.ordinal()) {
            client.publishes(MqttGlobalPublishFilter.ALL, this::projectionReceived);
        } else {
            client.publishes(MqttGlobalPublishFilter.ALL, publish -> {
                String msg = new String(publish.getPayloadAsBytes(), StandardCharsets.UTF_8);
                messageReceived(msg);
            });
        }
    }
    private void subscribeTopics() {
        String filter = (openMode.ordinal() < OpenMode.omFIRST_ADMIN.ordinal()
                ? topicMask : MqttPsw.DYNSECTOPICRESPONSE);
        client.subscribeWith()
                .topicFilter(filter)
                .send()
            .whenComplete((sack,err) -> {
                if (err != null) {
                    close();
                    doOnError("Internet hiba: " + err.getLocalizedMessage());
                }
            });
    }

    //***********************************************
    // publikus rutinok
    //***********************************************

    static public String decodePsw(String secretstr) {
        if (secretstr==null || secretstr.isEmpty()) return secretstr;
        byte[] ret = new byte[secretstr.length()];
        byte[] bytes = secretstr.getBytes(StandardCharsets.UTF_8);
        int i=0, p=0;
        while (i<bytes.length) {
            byte b1 = bytes[i++];
            if (b1=='A' || b1=='a') continue;
            if (i>=bytes.length) break;
            byte b2 = bytes[i++];
            byte c1 = (byte)(((char)b1)>='c' ? b1-'c' : b1-'B');
            byte c2 = (byte)(((char)b2)>='c' ? b2-'c'-4 : b2-'B'-4);
            ret[p++]=((byte)((c1 & 15) + ((c2 & 15) << 4)));
        }
        byte[] ret2 = Arrays.copyOf(ret,p);
        return new String(ret2, StandardCharsets.UTF_8);
    }

    @NonNull
    static public String encodePsw(@NonNull String publicstr) {
        StringBuilder sb = new StringBuilder();
        byte[] bytes = publicstr.getBytes(StandardCharsets.UTF_8);
        for (int i=0; i<bytes.length;) {
            byte b = bytes[i++];
            if (((i ^ b) & 7) == 0) sb.append((i & 1) == 0 ? 'A' : 'a');
            char base = ((i ^ b) & 1) == 0 ? 'B' : 'c';
            sb.append((char)(base+(b & 15)));
            sb.append((char)(base+4+(b >> 4)));
        }
        return sb.toString();
    }

    public void close() {
        ensureDisconnected()
                .exceptionally(throwable -> {
                    return null;
                });
        isOpen=false;
    }

    public void openSender() {
        Channel="1";
        if (Username.isEmpty() || Channel.isEmpty() || Password.isEmpty()) {
            close();
            return;
        }
        topicGroup="Diatar/"+Username+"/"+Channel+"/";
        topicMask=topicGroup+"#";
        topicState=topicGroup+"state";
        topicBlank=topicGroup+"blank";
        topicDia=topicGroup+"dia";
        openMode=OpenMode.omSENDER;
        isOpen=true;
        ensureDisconnected().thenCompose(v ->
                client.connectWith()
                    .simpleAuth()
                    .username("receiver")
                    .password("receiverpsw".getBytes())
                    .applySimpleAuth()
                    .send()
                    ).exceptionally(throwable -> {
                        close();
                        doOnError("Megnyitási hiba: "+throwable.getLocalizedMessage());
                        return null;
                    });
    }

    public void openReceiver() {
        Channel="1";
        if (Username.isEmpty() || Channel.isEmpty()) {
            close();
            return;
        }
        topicGroup="Diatar/"+Username+"/"+Channel+"/";
        topicMask=topicGroup+"#";
        topicState=topicGroup+"state";
        topicBlank=topicGroup+"blank";
        topicDia=topicGroup+"dia";
        openMode=OpenMode.omRECEIVER;
        isOpen=true;
        ensureDisconnected().thenCompose(v ->
            client.connectWith()
                .simpleAuth()
                .username("receiver")
                .password("receiverpsw".getBytes())
                .applySimpleAuth()
                .send()

                .thenCompose(connAck -> {
                    registerPublishHandler();
                    return client.subscribeWith()
                            .topicFilter(topicMask)
                            .send();
                }).exceptionally(throwable -> {
                    close();
                    doOnError("Megnyitási hiba: "+throwable.getLocalizedMessage());
                    return null;
                }));
    }

    public void fillUserList() {
        openMode=OpenMode.omUSERLIST;
        executeAdmin("{\"commands\": [{\"command\": \"listClients\"}]}");
    }

    //**************************************************************
    // kuldo rutinok
    //**************************************************************
    public void sendState(RecState rec) {
        if (!isOpen) return;
        client.publishWith()
            .topic(topicState)
            .payload(rec.buf)
            .send()
            .exceptionally(throwable -> {
                doOnError("Küldési hiba: "+throwable.getLocalizedMessage());
                return null;
            });
    }

    public void sendTxt(RecText rec) {
        if (!isOpen) return;
        byte[] sendbuf = new byte[1+rec.buf.length];
        sendbuf[0]='T';
        System.arraycopy(rec.buf, 0,sendbuf, 1, rec.buf.length);
        client.publishWith()
            .topic(topicDia)
            .payload(sendbuf)
            .send()
            .exceptionally(throwable -> {
                doOnError("Küldési hiba: "+throwable.getLocalizedMessage());
                return null;
            });
    }

    public void sendPic(RecPic rec) {
        if (!isOpen) return;
        byte[] sendbuf = new byte[1+rec.buf.length];
        sendbuf[0]='P';
        System.arraycopy(rec.buf, 0,sendbuf, 1, rec.buf.length);
        client.publishWith()
            .topic(topicDia)
            .payload(sendbuf)
            .send()
            .exceptionally(throwable -> {
                doOnError("Küldési hiba: "+throwable.getLocalizedMessage());
                return null;
            });
    }

    public void sendBlank(RecBlank rec) {
        if (!isOpen || rec==null) return;
        client.publishWith()
            .topic(topicBlank)
            .payload(rec.buf)
            .send()
            .exceptionally(throwable -> {
                doOnError("Küldési hiba: "+throwable.getLocalizedMessage());
                return null;
            });
    }

    //**************************************************************
    // callback kezeles
    //**************************************************************

    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    private static void runInMainThread(Runnable action) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            action.run();
        } else {
            mainHandler.post(action);
        }
    }

    private void doOnError(String txt) {
        if (OnErrorCallback!=null)
            runInMainThread(() -> OnErrorCallback.accept(txt));
    }

    private void doOnCompleted(Void v) {
        if (OnCompletedCallback!=null)
            runInMainThread(() -> OnCompletedCallback.accept(v));
    }

    private void doOnRecState(RecState rec) {
        if (OnRecStateCallback!=null)
            runInMainThread(() -> OnRecStateCallback.accept(rec));
    }

    private void doOnRecPic(RecPic rec) {
        if (OnRecPicCallback!=null) OnRecPicCallback.accept(rec);
    }

    private void doOnRecBlank(RecBlank rec) {
        if (OnRecBlankCallback!=null)
            runInMainThread(() -> OnRecBlankCallback.accept(rec));
    }

    private void doOnRecText(RecText rec) {
        if (OnRecTextCallback!=null)
            runInMainThread(() -> OnRecTextCallback.accept(rec));
    }

    public void setErrCallback(Consumer<String> onError) {
        OnErrorCallback=onError;
    }

    public void setCompletedCallback(Consumer<Void> onCompleted) {
        OnCompletedCallback=onCompleted;
    }

    public void setRecStateCallback(Consumer<RecState> onRecState) {
        OnRecStateCallback=onRecState;
    }

    public void setRecPicCallback(Consumer<RecPic> onRecPic) {
        OnRecPicCallback=onRecPic;
    }

    public void setRecBlankCallback(Consumer<RecBlank> onRecBlank) {
        OnRecBlankCallback=onRecBlank;
    }

    public void setRecTextCallback(Consumer<RecText> onRecText) {
        OnRecTextCallback=onRecText;
    }

    //**************************************************************
    // user kezeles
    //**************************************************************

    //egy adott nevhez hasonlo felhasznalok
    public List<String> usersLike(String mask) {
        List<String> res = new ArrayList<>();

        String mymask = unaccent(mask);
        if (mymask.isEmpty()) return res;

        UserArray[] uarr = users;
        if (uarr==null) return res;
        for (UserArray u : uarr) {
            if (!u.SendersGroup) continue;
            String myuser = unaccent(u.UserName);
            if (mymask.length()==1 ? myuser.startsWith(mymask) : myuser.contains(mymask)) {
                res.add(u.UserName);
            }
        }

        return res;
    }

    public UserArray getUser(String uname) {
        String myname = unaccent(uname);
        UserArray[] uarr = users;
        if (uarr==null) return null;
        for (UserArray u : uarr) {
            if (!u.SendersGroup) continue;
            String myuser = unaccent(u.UserName);
            if (myuser.equals(myname)) return u;
        }
        return null;
    }
}
