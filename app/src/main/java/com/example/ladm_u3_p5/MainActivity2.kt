package com.example.ladm_u3_p5

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main2.*

class MainActivity2 : AppCompatActivity() {
    var baseDatos= baseDatos(this,"basedatos1", null,1)
    var id = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        btnActualizar.setOnClickListener {
            var extra=intent.extras
            id=extra?.getString("idactualizar")!!

        }

    }
}