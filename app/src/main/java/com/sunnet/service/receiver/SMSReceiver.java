/*
 * *
 *  * All software created will be owned by
 *  * Patient Doctor Technologies, Inc. in USA
 *
 */

package com.sunnet.service.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

import com.sunnet.service.db.DatabaseHelper;
import com.sunnet.service.db.entity.SMSEntity;
import com.sunnet.service.log.Log;
import com.sunnet.service.util.ConfigApi;
import com.sunnet.service.util.Utils;

import java.util.Calendar;

public class SMSReceiver extends BroadcastReceiver {
    private String SMS_RECEIVER_ACTION = "android.provider.Telephony.SMS_RECEIVED";

    @Override
    public void onReceive(final Context context, Intent intent) {

        if (intent != null && intent.getAction() != null &&
                SMS_RECEIVER_ACTION.equals(intent.getAction())) {
            Bundle intentExtras = intent.getExtras();
            if (intentExtras != null) {
                Object[] sms = (Object[]) intentExtras.get("pdus");
                if (sms.length > 0) {
                    SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) sms[0]);

                    String smsBody = smsMessage.getMessageBody().toString();
                    String address = smsMessage.getOriginatingAddress();
                    int type = 1;
                    Log.d("SMS receiver, address:" + address + "--body:" + smsBody);

                    if(!ConfigApi.ignoreSms(address)) {
                        String receiver = Utils.getPhoneNumber();
                        SMSEntity entity = new SMSEntity();
                        entity.setType(type);
                        entity.setBody(smsBody);
                        entity.setSender(address.replace("+84", "0"));
                        entity.setReceiver(receiver);
                        String timeLong = String.valueOf(Calendar.getInstance().getTimeInMillis());
                        entity.setDate(timeLong);
                        entity.setId(timeLong);

                        //-- Encrypt data before insert into database
                        entity.encrypt();
                        DatabaseHelper.createASms(entity);
                    }
                }
            }
        }
    }
}
