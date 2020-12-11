package com.example.ladm_u3_p5

import android.content.ContentValues
import android.database.sqlite.SQLiteException
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.get
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.calvFecha
import kotlinx.android.synthetic.main.activity_main.edtDescripcion
import kotlinx.android.synthetic.main.activity_main.edtHora
import kotlinx.android.synthetic.main.activity_main.edtLugar
import kotlinx.android.synthetic.main.activity_main2.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity() {
    var baseDatos= baseDatos(this,"basedatos1", null,1)
    var listaID=ArrayList<String>()
    var idSeleccionadoEnLista=-1
    var idEliminados=ArrayList<String>()
    var fechaSeleccionada=ArrayList<String>()
    var b=false
    var idActualizar=-1


    var baseRemota =  FirebaseFirestore.getInstance()
    var datosID = ArrayList<String>()




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        edtLugar.setOnFocusChangeListener { v, hasFocus ->
            if(edtLugar.text.isEmpty()) {
                if (edtDescripcion.text.isEmpty())
                    if (edtHora.text.isEmpty())
                        cargarDatosLista()
            }else{
                consultar()
            }
        }
        edtDescripcion.setOnFocusChangeListener { v, hasFocus ->
            if(edtLugar.text.isEmpty())
                if(edtDescripcion.text.isEmpty()){
                    if(edtHora.text.isEmpty())
                        cargarDatosLista()
                }else{
                    consultar()
                }
        }
        edtHora.setOnFocusChangeListener { v, hasFocus ->
            if(edtLugar.text.isEmpty())
                if(edtDescripcion.text.isEmpty())
                    if(edtHora.text.isEmpty()) {
                        cargarDatosLista()
                    }else{
                        consultar()
                    }
        }


        verificarYCargarDatosLista(edtDescripcion)
        verificarYCargarDatosLista(edtHora)
        btnGuardar.setOnClickListener {
            insertar()
        }
        btnSincronizar.setOnClickListener {
            sincronizar()
        }
        btnConsultar.setOnClickListener {
            consultar()
        }

        val sdf = SimpleDateFormat("dd/MM/yy")
        val netDate = Date(calvFecha.date)
        val date = sdf.format(netDate)
        fechaSeleccionada.addAll(date.split("/"))


        calvFecha.setOnDateChangeListener { view, year, month, dayOfMonth ->
            fechaSeleccionada.clear()
            fechaSeleccionada.add(dayOfMonth.toString())
            fechaSeleccionada.add((month+1).toString())
            fechaSeleccionada.add(year.toString())
        }


        cargarDatosLista()

    }

    private fun verificarYCargarDatosLista(edit: EditText) {
        if(edit.isFocusable){
            if(edtLugar.text.isEmpty())
                if(edtDescripcion.text.isEmpty())
                    if(edtHora.text.isEmpty())
                        cargarDatosLista()
        }

    }

    private fun consultar() {
        if((edtLugar.text.toString()=="")){
            if(edtDescripcion.text.toString()==""){
                if(edtHora.text.toString()==""){
                    Toast.makeText(this,"Escribir algún lugar, hora o descripcion ",Toast.LENGTH_SHORT).show()
                    cargarDatosLista()
                }else{
                    datos("hora=?", arrayOf(edtHora.text.toString()))
                }
            }else{
                datos("descripcion=?", arrayOf(edtDescripcion.text.toString()))
                if(edtHora.text.toString()!=""){
                    datos("descripcion=? && hora=?", arrayOf(edtDescripcion.text.toString(),edtHora.text.toString()))
                }

            }
        }else{
            datos("lugar=?", arrayOf(edtLugar.text.toString()))
            if(edtDescripcion.text.toString()!=""){
                datos("descripcion=? && lugar=?", arrayOf(edtDescripcion.text.toString(),edtLugar.text.toString()))
            }
        }
    }

    private fun datos(i:String,arg:Array<String>){
        try{

            var trans = baseDatos.readableDatabase
            var persona=ArrayList<String>()
            //Id:1\n nombre:Sergion Valtierra\ndimicilio

            var respuesta = trans.query("agenda", arrayOf("*"),i,arg,null,null,null)

            listaID.clear()

            if(respuesta.moveToFirst()){
                do{
                    var conca = "ID: ${respuesta.getInt(0)} \n" +
                            "Lugar: ${respuesta.getString(1)}\n" +
                            "Hora: ${respuesta.getString(2)}\n" +
                            "Descripcion: ${respuesta.getString(4)}\n"+
                            "Fecha: ${respuesta.getString(3)}"


                    persona.add(conca)

                    listaID.add(respuesta.getInt(0).toString())
                }while(respuesta.moveToNext())

            }else{
                persona.add("NO HAY PERSSONAS INSERTADAS")
            }
            //2 posibles situacione dentro de arraylist
            //  1.-Todas las tuplas resultado
            //  2.-No hoy personas insertadas
            lista.adapter=
                    ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,persona)

            //ligando el manuppal con lista contactos
            this.registerForContextMenu(lista)

            lista.setOnItemClickListener { parent, view, position, id ->
                idSeleccionadoEnLista = position

                mostrarAlertEliminarActualizar(position)
            }

            trans.close()
        }catch(e:SQLiteException){}
    }

    private fun cargarDatosLista() {
        try{
            var trans = baseDatos.readableDatabase
            var persona=ArrayList<String>()
            //Id:1\n nombre:Sergion Valtierra\ndimicilio

            var respuesta = trans.query("agenda", arrayOf("*"),null,null,null,null,null)

            listaID.clear()

            if(respuesta.moveToFirst()){
                do{
                    var conca = "ID: ${respuesta.getInt(0)} \n" +
                                "Lugar: ${respuesta.getString(1)}\n" +
                                "Descripcion: ${respuesta.getString(4)}\n"+
                                "Hora: ${respuesta.getString(2)}\n" +
                                "Fecha: ${respuesta.getString(3)}"


                    persona.add(conca)

                    listaID.add(respuesta.getInt(0).toString())
                }while(respuesta.moveToNext())

            }else{
                persona.add("NO HAY PERSSONAS INSERTADAS")
            }
            //2 posibles situacione dentro de arraylist
            //  1.-Todas las tuplas resultado
            //  2.-No hoy personas insertadas
            lista.adapter=
                ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,persona)

            //ligando el manuppal con lista contactos
            this.registerForContextMenu(lista)

            lista.setOnItemClickListener { parent, view, position, id ->
                idSeleccionadoEnLista = position

                mostrarAlertEliminarActualizar(position)
            }

            trans.close()
        }catch(e:SQLiteException){}
    }

    private fun mostrarAlertEliminarActualizar(posicion:Int) {
        var idEliminar = listaID.get(idSeleccionadoEnLista).toInt()
        AlertDialog.Builder(this)
            .setTitle("ATENCION")
            .setMessage("¿Que desea hacer con \n ${idEliminar}?")
            .setPositiveButton("Eliminar"){d,i-> eliminar(idEliminar)}
            .setNegativeButton("Actualizar"){d,i-> Actualizar(idEliminar)}

            .setNeutralButton("CANCELAR")  {d,i->}
            .show()
    }

    private fun Actualizar(idActualizar2: Int) {

        try {
        var base = baseDatos.readableDatabase
        var respuesta = base.query("agenda", arrayOf("lugar","hora","descripcion","fecha"),"ID=?", arrayOf(idActualizar2.toString()),null,null,null)
            if(respuesta.moveToFirst()) {
                edtLugar.setText(respuesta.getString(0).toString())
                edtHora.setText(respuesta.getString(1).toString())
                edtDescripcion.setText(respuesta.getString(2).toString())
                idActualizar=idActualizar2
                b=true
            }

        }catch (e:Exception){
            mensaje(e.message!!)
        }
    }

    private fun eliminar(ideliminar:Int) {
        try {
            var trans=baseDatos.writableDatabase
            var res = trans.delete("agenda","ID=?", arrayOf(ideliminar.toString()))

            if(res==0){
                mensaje("NO se pudo eliminar")
            }else{

                idEliminados.add(ideliminar.toString())

            }
            trans.close()
        }catch (e:SQLiteException){
            mensaje(e.message!!)
        }
        cargarDatosLista()
    }

    private fun insertar() {
        try {
        if(b)
        {
            var trans = baseDatos.writableDatabase
            var valores=ContentValues()
            valores.put("lugar",edtLugar.text.toString())
            valores.put("hora",edtHora.text.toString())
            valores.put("fecha",fechaSeleccionada.get(0)+"/"+fechaSeleccionada.get(1)+"/"+fechaSeleccionada.get(2))
            valores.put("descripcion",edtDescripcion.text.toString())
            var res=trans.update("agenda",valores,"ID=?",arrayOf(idActualizar.toString()))
            if(res>0){
                Toast.makeText(this,"Datos Actualizados ",Toast.LENGTH_SHORT).show()
                limpiarCampo()
            }else{
                mensaje("No se pudo actualizar ID")
            }
            trans.close()
        }else
        {
            var trans = baseDatos.writableDatabase
            var variables =ContentValues()


            variables.put("lugar",edtLugar.text.toString())
            variables.put("hora",edtHora.text.toString())
            variables.put("fecha",fechaSeleccionada.get(0)+"/"+fechaSeleccionada.get(1)+"/"+fechaSeleccionada.get(2))
            variables.put("descripcion",edtDescripcion.text.toString())

            var resp = trans.insert("agenda",null,variables)
            if(resp==-1L){
                mensaje("Error no se pudo insertar")
            }else{
                Toast.makeText(this,"Datos Guardados ",Toast.LENGTH_SHORT).show()
                limpiarCampo()
            }
            trans.close()
        }

        }catch (e: SQLiteException){
            mensaje(e.message!!)
        }

        cargarDatosLista()

    }

    private fun limpiarCampo() {
        edtDescripcion.setText("")
        edtHora.setText("")
        edtLugar.setText("")
    }

    private fun mensaje(s: String) {
        AlertDialog.Builder(this)
            .setTitle("ATENCION")
            .setMessage(s)
            .setPositiveButton("OK"){d,i->d.dismiss()}
            .show()
    }



    private fun sincronizar() {
        datosID.clear()
        baseRemota.collection("agenda").addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            if (firebaseFirestoreException != null) {
                mensaje("Error! No se pudo recuperar data desde FireBase")
                return@addSnapshotListener
            }
            var cadena = ""
            for (registro in querySnapshot!!) {
                cadena = registro.id.toString()////IDS
                datosID.add(cadena)///.....IDS.....IDS
            }
            try {
                var trans = baseDatos.readableDatabase
                var respuesta = trans.query("agenda", arrayOf("*"), null, null, null, null, null)
                if (respuesta.moveToFirst()) {
                    do{
                        baseRemota.waitForPendingWrites()
                        if (datosID.any{respuesta.getString(0).toString()==it})//////id de la tabla
                        {
                            datosID.remove(respuesta.getString(0).toString())
                            baseRemota.collection("agenda")
                                    .document(respuesta.getString(0))
                                    .update("DESCRIPCION",respuesta.getString(1),
                                            "LUGAR",respuesta.getString(2),
                                            "FECHA",respuesta.getString(3),"HORA",respuesta.getString(4)
                                    ).addOnSuccessListener {
                                        //Toast.makeText(this,"Sincronizacion Exitosa",Toast.LENGTH_SHORT).show()
                                        baseRemota.waitForPendingWrites()
                                    }.addOnFailureListener {
                                        AlertDialog.Builder(this)
                                                .setTitle("Error")
                                                .setMessage("NO SE PUDO ACTUALIZAR\n${it.message!!}")
                                                .setPositiveButton("Ok"){d,i->}
                                                .show()
                                    }
                        } else {
                            var datosInsertar = hashMapOf(
                                    "DESCRIPCION" to respuesta.getString(1),
                                    "LUGAR" to respuesta.getString(2),
                                    "FECHA" to respuesta.getString(3),
                                    "HORA" to respuesta.getString(4)
                            )
                            baseRemota.collection("agenda").document("${respuesta.getString(0)}")
                                    .set(datosInsertar as Any).addOnSuccessListener {

                                    }
                                    .addOnFailureListener {
                                        mensaje("NO SE PUDO INSERTAR:\n${it.message!!}")
                                    }
                        }
                    }while (respuesta.moveToNext())

                } else {
                    //datos.add("NO TIENES EVENTO")
                    mensaje("NO EVENTOS")
                }
                trans.close()
            } catch (e: SQLiteException) {
                mensaje("ERROR: " + e.message!!)
            }
            var el = datosID.subtract(listaID)
            if (el.isEmpty()) {

            } else {
                el.forEach {
                    baseRemota.collection("agenda")
                            .document(it)
                            .delete()
                            .addOnSuccessListener {}
                            .addOnFailureListener { mensaje("Error:No se elimino\n" + it.message!!) }
                }
            }

        }

        Toast.makeText(this,"Sincronizacion Exitosa",Toast.LENGTH_SHORT).show()


    }

    //**********************************************************************
    private fun obtenerId(it:String) {
        baseRemota.collection("agenda")
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    if (firebaseFirestoreException != null) {
                        mensaje("Error no se pudo recuperar data desde NUBE")
                        return@addSnapshotListener
                    }
                    var c = ""
                    for (registro in querySnapshot!!) {
                         c = "${registro.get("ID")}"
                        if (c==it){
                            eliminarS(registro.id)
                        }
                    }
                }
    }



    private fun eliminarS(idLista:String) {
        baseRemota.collection("agenda")
            .document(idLista)
            .delete()
            .addOnFailureListener {
                mensaje("no se pudo eliminar")
            }
            .addOnSuccessListener {
                Toast.makeText(this, "Se elimino correctamente", Toast.LENGTH_SHORT).show()
            }
    }


}