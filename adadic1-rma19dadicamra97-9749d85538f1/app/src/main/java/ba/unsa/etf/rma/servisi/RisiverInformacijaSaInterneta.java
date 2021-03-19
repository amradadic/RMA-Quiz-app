package ba.unsa.etf.rma.servisi;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

public class RisiverInformacijaSaInterneta extends ResultReceiver
{
    private Receiver mReceiver;

    public RisiverInformacijaSaInterneta(Handler handler)
    {
        super(handler);
    }

    public void setReceiver(Receiver receiver)
    {
        mReceiver = receiver;
    }

    /* Deklaracija interfejsa koji će se trebati implementirati */
    public interface Receiver
    {
        public void onReceiveResult(int resultCode, Bundle resultData);
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData)
    {
        if (mReceiver != null)
        {
            mReceiver.onReceiveResult(resultCode, resultData);
        }
    }
}
