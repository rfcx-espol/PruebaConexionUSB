package app.recorder.arduinotest.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import java.util.Map;

public class Identifiers {
    //INTERVALO ENTRE EL INICIO DE CADA GRABACIÓN
    public static int recordingAudioInterval;
    //INTERVALO ENTRE EL INICIO DE CADA REGISTRO DE TEMPERATURA
    public static int registerTemperatureInterval;
    //DURACIÓN DEL AUDIO
    public static int audioDuration;
    //NÚMERO DE MUESTRAS
    public static int samplingRate;
    //TASA DE BITS
    public static int bitRate;
    //NÚMERO DE CANALES
    public static int channels;
    //FORMATO DEL ARCHIVO DE AUDIO
    //public static String format;
    //UNIDAD EN LA QUE SE MIDE LA TEMPERATURA
    public static String format;
    //SERVICIO EN EJECUCIÓN
    public static boolean onService = false;
    //INTENT EN EJECUCIÓN
    public static PendingIntent pendingIntent;
    //ALARMA EN EJECUCIÓN
    public static AlarmManager alarmManager;

    //ESTABLECE LAS PREFERENCIAS DEL SERVICIO
    public static void setPreferencesApplications(Context context){
        SharedPreferences prefSettings = PreferenceManager.getDefaultSharedPreferences(context);
        //audioDuration = Integer.parseInt(prefSettings.getString("audio_duration","90")) * 1000;
        //recordingAudioInterval = Integer.parseInt(prefSettings.getString("recording_audio_interval", "210")) * 1000;
        registerTemperatureInterval = Integer.parseInt(prefSettings.getString("DeltaTemperature", "1800")) * 1000;
        //samplingRate = Integer.parseInt(prefSettings.getString("audio_sample_rate","48000"));
        //bitRate = Integer.parseInt(prefSettings.getString("audio_encode_bitrate","250")) * 1000;
        //channels = Integer.parseInt(prefSettings.getString("channels","2"));
        format = prefSettings.getString("Temp_unit","Kelvin");

        for ( Map.Entry<String,?> pref : prefSettings.getAll().entrySet() ) {
            Log.d("CONFIGURACIÓN ACTUAL" , pref.getKey() + ": " + pref.getValue().toString());
        }
    }
}