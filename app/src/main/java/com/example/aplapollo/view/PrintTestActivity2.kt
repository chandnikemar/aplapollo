    package com.example.aplapollo.view

    import android.Manifest
    import android.bluetooth.BluetoothAdapter
    import android.content.Intent
    import android.content.pm.PackageManager
    import android.os.Build
    import android.os.Bundle
    import android.util.Log
    import android.widget.Button
    import android.widget.Toast
    import androidx.appcompat.app.AppCompatActivity
    import androidx.core.app.ActivityCompat
    import androidx.core.content.ContextCompat
    import com.example.apolloapl.R
    import com.zebra.sdk.comm.BluetoothConnection
    import com.zebra.sdk.comm.Connection


    class PrintTestActivity2 : AppCompatActivity() {


        companion object {
            const val REQUEST_BT_PERMISSION = 2001
            private const val REQUEST_ENABLE_BT = 1001
        }

        // 👉 Replace with your ZQ320 MAC Address
        private val PRINTER_MAC = "AC:3F:A4:CF:89:AD"

        private var connection: Connection? = null

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_print_test2)

            findViewById<Button>(R.id.btnTestPrint).setOnClickListener {
                checkPermissionAndPrint()
            }
        }

        // ===============================
        // 1️⃣ Permission Check (Android 12+)
        // ===============================
        private fun checkPermissionAndPrint() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {

                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                        REQUEST_BT_PERMISSION
                    )
                    return
                }
            }
            enableBluetooth()
        }

        // ===============================
        // 2️⃣ Enable Bluetooth
        // ===============================
        private fun enableBluetooth() {
                val adapter = BluetoothAdapter.getDefaultAdapter()
                val pairedDevices = adapter.bondedDevices
                for (device in pairedDevices) {
                    Log.d("BT_DEVICE", "${device.name} -> ${device.address}")
                    Log.e("BT_PAIRED", "Name=${device.name}, MAC=${device.address}")
                }
            if (adapter == null) {
                Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show()
                return
            }
            if (!adapter.isEnabled) {

                // Android 12+ permission check
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                        REQUEST_BT_PERMISSION
                    )
                    return
                }

                // SYSTEM dialog to enable Bluetooth
                val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(intent, REQUEST_ENABLE_BT)

            } else {
                connectAndPrint()
            }

        }

        // ===============================
        // 3️⃣ Connect Zebra ZQ320
        // ===============================
        private fun connectAndPrint() {

            Thread {
                try {
                    val connection = BluetoothConnection(PRINTER_MAC)
                    connection.open()

                    val zpl = """
                   
    ^XA
    ~TA000
    ~JSN
    ^LT0
    ^MNN
    ^MTD
    ^PON
    ^PMN
    ^LH0,0
    ^JMA
    ^PR5,5
    ~SD10
    ^JUS
    ^LRN
    ^CI27
    ^PA0,1,1,0
    ^XZ
    ^XA
    ^MMT
    ^PW609
    ^LL812
    ^LS0
    ^FO6,5^GB596,800,8^FS
    ^FT32,150^A0N,28,28^FH\^CI28^FDSupplier Batch #^FS^CI27
    ^FT32,196^A0N,28,28^FH\^CI28^FDSupplier Name^FS^CI27
    ^FT32,231^A0N,28,28^FH\^CI28^FDGrade^FS^CI27
    ^FT29,270^A0N,28,28^FH\^CI28^FDThickness^FS^CI27
    ^FT32,305^A0N,28,28^FH\^CI28^FDWidth^FS^CI27
    ^FT32,346^A0N,28,28^FH\^CI28^FDGRN Number^FS^CI27
    ^FT32,392^A0N,28,28^FH\^CI28^FDGRN Date^FS^CI27
    ^FT32,427^A0N,28,28^FH\^CI28^FDMaterial Code^FS^CI27
    ^FT274,150^A0N,28,28^FH\^CI28^FD:^FS^CI27
    ^FT274,196^A0N,28,28^FH\^CI28^FD:^FS^CI27
    ^FT274,231^A0N,28,28^FH\^CI28^FD:^FS^CI27
    ^FT274,266^A0N,28,28^FH\^CI28^FD:^FS^CI27
    ^FT274,301^A0N,28,28^FH\^CI28^FD:^FS^CI27
    ^FT274,340^A0N,28,28^FH\^CI28^FD:^FS^CI27
    ^FT274,392^A0N,28,28^FH\^CI28^FD:^FS^CI27
    ^FT274,427^A0N,28,28^FH\^CI28^FD:^FS^CI27
    ^FT313,150^A0N,28,28^FH\^CI28^FDSB-002^FS^CI27
    ^FT313,196^A0N,28,28^FH\^CI28^FDKemar Steel Ptv LTD^FS^CI27
    ^FT313,231^A0N,28,28^FH\^CI28^FDG300^FS^CI27
    ^FT313,266^A0N,28,28^FH\^CI28^FD1.0^FS^CI27
    ^FT313,305^A0N,28,28^FH\^CI28^FD800.0^FS^CI27
    ^FT313,346^A0N,28,28^FH\^CI28^FDGRN-778900^FS^CI27
    ^FT313,392^A0N,28,28^FH\^CI28^FD2025-12-14^FS^CI27
    ^FT313,430^A0N,28,28^FH\^CI28^FDMTR-9002^FS^CI27
    ^FT385,707^BQN,2,7
    ^FH\^FDLA,123456789012^FS
    ^FO313,30^GFA,445,1220,20,:Z64:eJyNlL1qxTAMhWVTL55qaHdzJ+OnSKHZMyTvo7HkKYQno6esnEvhWnaghvwgPk6OjuUAwFlhWPs+lDIz6VoIYeBYliqZdV2/Vc03DhUX3sFM5Fg5PJq7beDIKk7k9LKXmFf2lhkn5mxn0IzNtjYa03FuDE/SG1Oe2JtyTmd3cWThYUFucsX20vSCCX2AkkpGPtETV67Vo+jvG3yC2pDcuFLQxhgpJQuUn3qg9TARMfqUstw8kgeJb1m+VqWHkbiAYA/hrBWu9Tv6w1rFX6pc0NdL79jE4uK0P65kMXMUa3/+jMyf1iu1kpdOE3DNrWlp1YjDPsUsn8tkOdoIOeZH8vHf+/Fxv7/xtXYzL+2BrzU3nb+CmjM384xwdtzz9L714PR8NINKM0/Om5Nhccrj/PyG0eIoJwke+6Jrs//LdEv8jyr8AlhxgAo=:12B7
    ^FO70,34^GFA,609,1320,24,:Z64:eJx1lLGKwzAMhmVfBMEHIYV4LzeFDDeXTh2uewrx+5hOJUOeIXQKHvoAeZqO5YY8w8mO216LosEQ5fOfX4rsFPhIFvLK8nlc4gd+T7Jb5r8YHmsAwfDZLyjH8Q1fg3JQVUweDUDB6U/WuQW+4fRHeR54XtSsvnOepx6pFNKytA//uIPQVyygqIu4+dM510srz8RXUJVlH/kdaGqQ8HuojhwinxFv5VVaezq5UzqktxPYwO8bohHRoG5I3yShvcr5cnt1bMd2O243m7GVgYcVKdYocIVaF3l+KETUJ/tDpjroBtWV5S0LfLI3ZKUuMCH/6P0/9GktK9k6VUlH/tVH0F/l3nkuyA/xeR550qfX2U0eXTfIy3DXRy8PSY0g9m/61BHVy1a2WzluNr2KfB2qFniI/lE//aur6qbuMnXf5aWbZt78QGhQ6E9SaP/H7/15idm/1vlbfvb/Gwz9C+mifrN7xWf98cnL9cyH/2sO5v0DQT/Oj4/UrtN7nmwbdj6dehawpnjwQi/Ov40Ptrfz9IT5NIbTH6F6nsf1I0+84Phsig15C39+Of6Tiq04vubvoOzK3z/+cHH3ifILw/8B36WFgg==:D942
    ^BY2,3,122^FT36,681^BCN,,Y,N
    ^FH\^FD>:BR0>5091209987637^FS
    ^FO32,772^GFA,193,320,16,:Z64:eJxjYIAACWYIbd+Ayjc8gMo3QOcnoPETfyTz2DG332D+cQCk1OBwT3JPMh9fBXNPIshow+NsyTMO9shYMLOB+fZsbOlAvoQElG/AxmZ24z+Yb8wA4x/gQebbfIDw5aB8GRDfAsZn7pEpSOzhAdoH4TP+kEiwZ2AHuoeHARWg8yUI8A3Q+B9ABAAc9Co5:5EA2
    ^FO129,777^GFA,21,10,1,:Z64:eJyTY4ACOQABVAA9:DE13
    ^FO139,772^GFA,197,210,14,:Z64:eJxjYGxgAIIEIOZhYGD/COUVMPYABRiReRJwlSCeAQqvgPEPf/MP+eQfBoz9D2oSGM9JHJ5j+XiOAWNfwbEExjaGwzwFj3kMGHsM2EA8Zp6EZDCPAchjPwzj2QF5MnC5BwaMbQYwOcM2CwgPaAozj2GbDGObZTOPIYR3hv1jmzwzjzxQpRyPwQ8A0YAzTw==:7AF7
    ^FO288,772^GFA,177,240,16,:Z64:eJxjYIAACSht34DKNzyAyjdA5yeg8RN/JPPYMbffYPxh38AD5B/uSe5J5uOrYO6xPADkGx5nS55xsEfGgp0NbJQ9G1s6kC8hwQbhG7Cxmd34j8Y/wIPMt/mAypcB8S1gfOYemYLEHh6gfQYg+wwYf0gk2DOwA90D5gMAvyItVg==:0096
    ^FO408,773^GFA,313,336,24,:Z64:eJxdzjFKxFAQxvHvMfDSDJt2goG9wqRaC9GrTGcb+wVTrY2HmiUQm4BXeN5AsNkiuE5cWMT2x5/5ZktlXxWctsDx8zzCqvKNfT08vXTTBmnuBhqFaxT0nqbWMQm1SAfzPDHfhKuDxDALCShbFMyULq7/nczVJTy9St75XzeXfPG7cvVslS8Ir1jaW6PcOcaWiQ19RqvGLKqPX3n7Xo7LrytB1YTFNHbh8LgTDwrB1JTF1a7+cFo9xHIziKVDGuDCkUKrFGJVN7SeZkJUq8/9pgl5Hj/O9YDTPTCW1d98aUJ+ANeKXMw=:5DC3
    ^FT274,470^A0N,28,41^FH\^CI28^FD:^FS^CI27
    ^FT313,475^A0N,28,28^FH\^CI28^FD1100.75^FS^CI27
    ^FT32,470^A0N,28,28^FH\^CI28^FDNET.Weight(kg)^FS^CI27
    ^PQ1,0,1,Y
    ^XZ
    
                      """

                    connection.write(zpl.toByteArray())
                    connection.close()

                    runOnUiThread {
                        Toast.makeText(this, "Print Success", Toast.LENGTH_SHORT).show()
                    }

                } catch (e: Exception) {
                    Log.e("ZEBRA", "Connection error", e)
                    runOnUiThread {
                        Toast.makeText(this, "Printer connection failed", Toast.LENGTH_LONG).show()
                    }
                }
            }.start()
        }


        // ===============================
        // 4️⃣ Print ZPL Label
        // ===============================
        private fun printLabel() {
            val zpl = """
                ^XA
                ^CF0,40
                ^FO30,30^FDZQ320 Test Print^FS
                ^FO30,80^FDKemar Automation^FS
                ^FO30,130^BCN,80,Y,N,N
                ^FDQC251216142615282^FS
                ^XZ
            """

            try {
                connection?.write(zpl.toByteArray())
                Toast.makeText(this, "Print Success", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("ZEBRA", "Print failed", e)
            } finally {
                try {
                    connection?.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        // ===============================
        // 5️⃣ Permission Result
        // ===============================
        override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
        ) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)

            if (requestCode == REQUEST_BT_PERMISSION) {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    enableBluetooth()
                } else {
                    Toast.makeText(
                        this,
                        "Bluetooth permission required to print",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        // ===============================
        // 6️⃣ Bluetooth Enable Result
        // ===============================
        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)

            if (requestCode == REQUEST_ENABLE_BT) {
                if (resultCode == RESULT_OK) {
                    connectAndPrint()
                } else {
                    Toast.makeText(this, "Bluetooth not enabled", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
