package diatar.eu;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import androidx.annotation.NonNull;

import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;

import diatar.eu.net.TcpClient;
import eu.diatar.library.MqttInterface;

public class SetWeb extends Activity {
    private Switch mWebOn;
    private EditText mUser;
    private EditText mPsw;

    private Button mOkBtn;

    @Override
    protected void onCreate(Bundle bd) {
        super.onCreate(bd);

        setContentView(diatar.eu.R.layout.setweb);
        setTitle("Internet beállítás");
        mWebOn=findViewById(R.id.setWebOn);
        mUser=findViewById(R.id.setWebName);
        mPsw=findViewById(R.id.setWebPsw);
        mOkBtn=findViewById(R.id.setWebOk);

        mUser.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                mWebOn.setChecked(true);
            }
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        });
        mPsw.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                mWebOn.setChecked(true);
            }
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        });

        load(bd);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        save(outState);
    }

    private void load(Bundle bd) {
        String u,p;
        if (bd == null) {
            u=G.sUser;
            p=G.sPsw;
        } else {
            u = bd.getString(G.idUSER, "");
            p = bd.getString(G.idPSW, "");
        }
        mUser.setText(u); mPsw.setText(p);
        mWebOn.setChecked(!u.isEmpty() && !p.isEmpty());
    }

    private void save(Bundle bd) {
        if (mWebOn.isChecked()) {
            bd.putString(G.idUSER, mUser.getText().toString());
            bd.putString(G.idPSW, mPsw.getText().toString());
        }
    }

    private void disableAll() {
        mWebOn.setEnabled(false);
        mUser.setEnabled(false);
        mPsw.setEnabled(false);
        mOkBtn.setEnabled(false);
    }

    private void enableAll() {
        mWebOn.setEnabled(true);
        mUser.setEnabled(true);
        mPsw.setEnabled(true);
        mOkBtn.setEnabled(true);
    }

    private void checkLogin() {
        String u = mUser.getText().toString(), p = mPsw.getText().toString();
        MqttInterface mqtt = MqttInterface.getInstance();
        TcpClient tcp = TcpClient.getMe();

        disableAll();

        mqtt.setErrCallback(txt -> {
            mqtt.setErrCallback(null);
            mqtt.setCompletedCallback(null);
            tcp.Err("Sikertelen csatlakozás! Jó a név és jelszó?");
            enableAll();
        });
        mqtt.setCompletedCallback(v -> {
            mqtt.setErrCallback(null);
            mqtt.setCompletedCallback(null);
            tcp.Msg("'"+u+"' csatlakoztatva.");
            G.sUser=u;
            G.sPsw=p;
            setResult(RESULT_OK);
            finish();
        });
        mqtt.chkLogin(u,p);
    }

    public void onOk(View v) {
        if (!mWebOn.isChecked()) {
            G.sUser="";
            G.sPsw="";
            setResult(RESULT_OK);
            finish();
        } else {
            checkLogin();
        }
    }

    public void onCancel(View v) {
        setResult(RESULT_CANCELED);
        finish();
    }
}
