package com.example.semaforo_kotlin

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat.Builder
import com.example.anoopm.mqtt.manager.MQTTConnectionParams
import com.example.anoopm.mqtt.manager.MQTTmanager
import com.example.anoopm.mqtt.protocols.UIUpdaterInterface
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() , UIUpdaterInterface {

    var mqttManager: MQTTmanager? = null

    lateinit var notificacionManager : NotificationManager
    lateinit var notificacionChannel : NotificationChannel
    lateinit var builder : Notification
    private var channelID = "com.example.anoopm.mqtt"
    private var description = "Text Notification"

    // Interface methods
    override fun resetUIWithConnection(status: Boolean) {

      ipAddressField.isEnabled = !status
      connectBtn.isEnabled = !status
      sendBtn.isEnabled = status

        // Update the status label.
        if (status) {
            updateStatusViewWith("Connected")
        } else {
            updateStatusViewWith("Disconnected")
        }
    }

    override fun updateStatusViewWith(status: String) {
        statusLabl.text = status
    }


    override fun update(message: String) {

        if (!(message.isNullOrEmpty())) {

            val data = message.split('-')

            if (data[0].equals("Red") && (data[1].equals("15") || data[1].equals("10") || data[1].equals("5") || data[1].equals("0"))){

                val message = "Time Left: " + data[1] + "s, to across the street."

                notificacionManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                val intent = Intent(this, LauncherActivity::class.java)
                val pendingIntent : PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                val soundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

                builder = Builder(this, channelID)
                    .setContentTitle("Semaforo")
                    .setContentText(message)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setLargeIcon(BitmapFactory.decodeResource(this.resources, R.drawable.ic_launcher_round))
                    .setContentIntent(pendingIntent)
                    .setSound(soundUri)
                    .setAutoCancel(true)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .setContentText(message).build()

                notificacionManager.notify((Math.random()*100).toInt(), builder)

            }


           
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Enable send button and message textfield only after connection
        resetUIWithConnection(false)
    }

    fun connect(view: View) {

        if (!(ipAddressField.text.isNullOrEmpty())) {
            var host = "tcp://" + ipAddressField.text.toString() + ":1883"
            var topic = "avenue/app-pedestrian"
            var topic2 = "avenue/light-trafic"
            var connectionParams = MQTTConnectionParams("MQTTSample", host, topic, topic2, "", "")
            mqttManager = MQTTmanager(connectionParams, applicationContext, this)
            mqttManager?.connect()
        } else {
            updateStatusViewWith("Please enter the IP Address of Broker")
        }

    }

    fun sendMessage(view: View) {
        mqttManager?.publish("true")
    }
}