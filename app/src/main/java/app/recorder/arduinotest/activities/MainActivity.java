package app.recorder.arduinotest.activities;

        import java.nio.ByteBuffer;
        import java.util.HashMap;
        import java.util.Iterator;

        import android.app.Activity;
        import android.app.PendingIntent;
        import android.content.BroadcastReceiver;
        import android.content.Context;
        import android.content.Intent;
        import android.content.IntentFilter;
        import android.hardware.usb.UsbConstants;
        import android.hardware.usb.UsbDevice;
        import android.hardware.usb.UsbDeviceConnection;
        import android.hardware.usb.UsbEndpoint;
        import android.hardware.usb.UsbInterface;
        import android.hardware.usb.UsbManager;
        import android.hardware.usb.UsbRequest;
        import android.os.AsyncTask;
        import android.os.Bundle;
        import android.util.Log;
        import android.view.View;
        import android.widget.Button;
        import android.widget.TextView;

public class MainActivity extends Activity {

    private static final String TAG = "USB-Example";

    // Variables GUI
    Button btn;
    TextView textViewTemperatura;
    TextView textViewHumedad;

    // TODO: Variables USB
    UsbManager mUsbManager;
    UsbDevice mUsbDevice;
    PendingIntent mPermissionIntent;
    UsbDeviceConnection mUsbDeviceConnection;
    UsbEndpoint epIN = null;
    UsbEndpoint epOUT = null;

    // TODO: Al conectar a un dispositvo USB se solicita un permiso al usuario
    // este broadcast se encarga de recoger la respuesta del usuario.
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver(){
        public void onReceive(Context context, android.content.Intent intent) {
            String action = intent.getAction();

            // TODO: Al aceptar el permiso del usuario.
            if (ACTION_USB_PERMISSION.equals(action)){
                synchronized (this) {

                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)){
                        Log.d(TAG, "Permiso aceptado");
                        processComunicationUSB();
                    }else{
                        Log.e(TAG, "Permiso denegado");
                    }
                }
            }

            // TODO: Al desconectar el dispositivo USB cerramos las conexiones y liberamos la variables.
            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (device != null) {
                    // call your method that cleans up and closes communication with the device
                }
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(app.recorder.arduinotest.R.layout.activity_main);

        textViewTemperatura = (TextView) findViewById(app.recorder.arduinotest.R.id.textView1);
        textViewHumedad = (TextView) findViewById(app.recorder.arduinotest.R.id.textView2);

        // TODO: Boton Conectar.
        btn = (Button) findViewById(app.recorder.arduinotest.R.id.button1);
        btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                //TODO: Obtemos el Manager USB del sistema Android
                mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

                // TODO: Recuperamos todos los dispositvos USB detectados
                HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();

                //TODO: en nuestor ejemplo solo conectamos un disposito asi que sera
                // el unico que encontraremos.
                Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
                if(deviceIterator.hasNext()){
                    mUsbDevice = deviceIterator.next();
                    Log.d(TAG, "Name: " + mUsbDevice.getDeviceName());
                    Log.d(TAG, "Protocol: " + mUsbDevice.getDeviceProtocol());
                    //TODO: Solicitamos el permiso al usuario.
                    mUsbManager.requestPermission(mUsbDevice, mPermissionIntent);
                }else{
                    Log.e(TAG, "Dispositvo USB no detectado.");
                }

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        //TODO: Solicitamos permiso al usuario
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);

        //TODO: Registro del Broadcast
        registerReceiver(mUsbReceiver, new IntentFilter(ACTION_USB_PERMISSION));
        registerReceiver(mUsbReceiver, new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED));

    }

    @Override
    protected void onPause() {
        super.onPause();

        if (this.mUsbReceiver != null){
            unregisterReceiver(mUsbReceiver);
        }

    }

    protected void processComunicationUSB() {

        boolean forceClaim = true;

        mUsbDeviceConnection = mUsbManager.openDevice(mUsbDevice);
        if(mUsbDeviceConnection == null){
            Log.e(TAG, "No se ha podido conectar con el dispositivo USB.");
            finish();
        }

        // TODO: getInterfase(1) Obtiene el tipo de comunicacion CDC (USB_CLASS_CDC_DATA)
        UsbInterface mUsbInterface = mUsbDevice.getInterface(1);

        // TODO: Obtenemos los Endpoints de entrada y salida para el interface que hemos elegido.
        for (int i = 0; i < mUsbInterface.getEndpointCount(); i++) {
            if (mUsbInterface.getEndpoint(i).getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                if (mUsbInterface.getEndpoint(i).getDirection() == UsbConstants.USB_DIR_IN)
                    epIN = mUsbInterface.getEndpoint(i);
                else
                    epOUT = mUsbInterface.getEndpoint(i);
            }
        }

        mUsbDeviceConnection.claimInterface(mUsbInterface, forceClaim);

        // TODO: Mensaje de configuración para el Device.
        int baudRate = 9600;
        byte stopBitsByte = 1;
        byte parityBitesByte = 0;
        byte dataBits = 8;
        byte[] msg = {
                (byte) (baudRate & 0xff),
                (byte) ((baudRate >> 8) & 0xff),
                (byte) ((baudRate >> 16) & 0xff),
                (byte) ((baudRate >> 24) & 0xff),
                stopBitsByte,
                parityBitesByte,
                (byte) dataBits
        };

        mUsbDeviceConnection.controlTransfer(UsbConstants.USB_TYPE_CLASS | 0x01, 0x20, 0, 0, msg, msg.length, 5000);
        // (UsbConstants.USB_TYPE_CLASS | 0x01) 0x21 -> Indica que se envia un parametro/mensaje del Host al Device (movil a la placa leonardo)
        // 0x20 -> paramtro/mensaje SetLineCoding

        mUsbDeviceConnection.controlTransfer(UsbConstants.USB_TYPE_CLASS | 0x01, 0x22, 0x1, 0, null, 0, 0);
        // (UsbConstants.USB_TYPE_CLASS | 0x01) 0x21 -> Indica que se envia un parametro/mensaje del Host al Device (movil a la placa leonardo)
        // 0x22 -> paramtro/mensaje SET_CONTROL_LINE_STATE (DTR)
        // 0x1  -> Activado.
        // Mas info: http://www.usb.org/developers/devclass_docs/usbcdc11.pdf

        // TODO: Ejecutar en un hilo
        new UpdateHumidityTemperature().execute();

        //TODO: Otro metod de obtener datos Syncronhus
        //while (true) {
        //Arrays.fill(buffer, (byte) 0);
        //int ret = mUsbDeviceConnection.bulkTransfer(epIN, buffer, buffer.length, TIMEOUT);
        //Log.d("USB","Return BulkTransfer: " + ret);
        //Log.d("USB","Buffer BulkTransfer: " + new String(buffer));
        //}

    }

    private class UpdateHumidityTemperature extends AsyncTask<String, String, String>{

        @Override
        protected String doInBackground(String... params) {

            String line=new String();

            int bufferMaxLength=epIN.getMaxPacketSize();
            ByteBuffer mBuffer = ByteBuffer.allocate(bufferMaxLength);
            UsbRequest inRequest = new UsbRequest();
            inRequest.initialize(mUsbDeviceConnection, epIN);

            while(inRequest.queue(mBuffer, bufferMaxLength) == true){

                mUsbDeviceConnection.requestWait();

                try {

                    // Recogemos los datos que recibimos en un
                    line = line + new String(mBuffer.array(), "UTF-8").trim();;

                    if (line.length()>0){

                        char endLine = line.charAt(line.length()-1);
                        if (endLine == ';'){

                            Log.d(TAG, "Encontrada final de linea: " + line);

                            // TODO: Procesar Linea
                            String[] parts = line.split(",");
                            String humidity = parts[0].split(":")[1];
                            String temperature = parts[1].split(":")[1].replace(";", "");

                            // TODO: Actualizamos el GUI
                            publishProgress(humidity, temperature);

                            line = "";
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            textViewHumedad.setText("Humedad: " + values[0]);
            textViewTemperatura.setText("Temperatura:" +  values[1]);

        }

    };

}