package com.psp.android_bluetooth_library;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Layout;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.psp.android_bluetooth_library.utils.HexUtils;
import com.psp.bluetoothlibrary.BluetoothListener;
import com.psp.bluetoothlibrary.SendReceive;

/**
 * Author: prasad-psp
 */
public class SendReceiveActivity extends AppCompatActivity {

    private final String TAG = "psp.SendRecAct";

    // UI
    private Button btnSend;
    private EditText edtMessage;
    private TextView txtDisplay;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_receive);
        init();

        // Receive listner
        SendReceive.getInstance().setOnReceiveListener(new BluetoothListener.onReceiveListener() {
            @Override
            public void onReceived(String receivedData) {
//                logMsg("[RX] "+receivedData);
//                txtDisplay.append("\n[RX] "+receivedData);
//                setDisplayMessageScrollBottom();
            }

            @Override
            public void onReceived(String receivedData, byte[] receivedDataInBytes) {
            byte[] filteredBytes = HexUtils.filterNonZeroBytes(receivedDataInBytes);
            String displayMsg = HexUtils.convertBytesToFormattedHex(filteredBytes);
            logMsg("[RX] "+displayMsg);
            txtDisplay.append("\n[RX] "+displayMsg);
            setDisplayMessageScrollBottom();

                // OR

//                logMsg("[RX] "+receivedData);
//                txtDisplay.append("\n[RX] "+receivedData);
//                setDisplayMessageScrollBottom();
            }
        });


        // Send data
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = edtMessage.getText().toString().trim();

                if(msg.isEmpty()) {
                    return;
                }

                if(SendReceive.getInstance().send(msg)) {
                    logMsg("[TX] "+msg);
                    txtDisplay.append("\n[TX] "+msg);
                    setDisplayMessageScrollBottom();
                }
                else {
                    logMsg("[TX] Failed "+msg);
                    txtDisplay.append("\n[TX] Failed "+msg);
                    setDisplayMessageScrollBottom();
                }
            }
        });

    }

    private void init() {
        btnSend = findViewById(R.id.btnSendSendRec);
        edtMessage = findViewById(R.id.edtMessageSendRec);
        txtDisplay = findViewById(R.id.txtDisplaySendRec);

        txtDisplay.setMovementMethod(new ScrollingMovementMethod());
    }

    private void setDisplayMessageScrollBottom() {
        final Layout layout = txtDisplay.getLayout();
        if(layout != null){
            int scrollDelta = layout.getLineBottom(txtDisplay.getLineCount() - 1)
                    - txtDisplay.getScrollY() - txtDisplay.getHeight();
            if(scrollDelta > 0)
                txtDisplay.scrollBy(0, scrollDelta);
        }
    }

    private void logMsg(String msg) {
        Log.d(TAG,msg);
    }
}