package cai.lib.kvstore

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val view = findViewById<TextView>(R.id.testView)
        val view2 = findViewById<TextView>(R.id.testView2)

        val data = KVStore.load(AppStore::class.java)
        val data2 = KVStore.load(AppStore::class.java)

        view.text = data.value + data.name + data.str
        view2.text = data2.value + data2.name + data2.str

        view.setOnClickListener {
            data.value = "111"
            data.name = "222"
            data.str = "333"
            data.apply()

            startActivity(Intent(this, MainActivity::class.java))
        }

    }
}