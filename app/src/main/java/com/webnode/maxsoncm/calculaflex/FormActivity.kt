package com.webnode.maxsoncm.calculaflex

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.crashlytics.android.Crashlytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.webnode.maxsoncm.calculaflex.model.CarData
import com.webnode.maxsoncm.calculaflex.utils.CalculaFlexTracker
import com.webnode.maxsoncm.calculaflex.utils.DatabaseUtil
import com.webnode.maxsoncm.calculaflex.utils.RemoteConfig
import com.webnode.maxsoncm.calculaflex.watchers.DecimalTextWatcher
import kotlinx.android.synthetic.main.activity_form.*

class FormActivity : BaseActivity() {

    private lateinit var userId: String
    private lateinit var mAuth: FirebaseAuth
    private val firebaseReferenceNode = "CarData"
    private val defaultClearValueText = "0.0"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form)

        mAuth = FirebaseAuth.getInstance()
        userId = FirebaseAuth.getInstance().currentUser!!.uid

        listenerFirebaseRealtime()

        etGasPrice.addTextChangedListener(DecimalTextWatcher(etGasPrice))
        etEthanolPrice.addTextChangedListener(DecimalTextWatcher(etEthanolPrice))
        etGasAverage.addTextChangedListener(DecimalTextWatcher(etGasAverage))
        etEthanolAverage.addTextChangedListener(DecimalTextWatcher(etEthanolAverage))

        loadBanner()

        btCalculate.setOnClickListener{
            /*Crashlytics.log("Exemplo de mensagem de erro.")
            Crashlytics.getInstance().crash()*/

            saveCarData()
            val proximatela = Intent(this@FormActivity, ResultActivity::class.java)
            proximatela.putExtra("GAS_PRICE", etGasPrice.text.toString().toDouble())
            proximatela.putExtra("ETHANOL_PRICE", etEthanolPrice.text.toString().toDouble())
            proximatela.putExtra("GAS_AVARAGE", etGasAverage.text.toString().toDouble())
            proximatela.putExtra("ETHANOL_AVERAGE", etEthanolAverage.text.toString().toDouble())

            sendDataToAnalytics()

            startActivity(proximatela)
        }

    }

    private fun saveCarData() {
        val carData = CarData(
            etGasPrice.text.toString().toDouble(),
            etEthanolPrice.text.toString().toDouble(),
            etGasAverage.text.toString().toDouble(),
            etEthanolAverage.text.toString().toDouble()
        )
        FirebaseDatabase.getInstance().getReference(firebaseReferenceNode)
            .child(userId)
            .setValue(carData)
            //.addOnCompleteListener{}
    }

    private fun listenerFirebaseRealtime() {
        DatabaseUtil.getDatabase().getReference(firebaseReferenceNode)
            .child(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val carData = dataSnapshot.getValue(CarData::class.java)
                    etGasPrice.setText(carData?.gasPrice.toString())
                    etEthanolPrice.setText(carData?.ethanolPrice.toString())
                    etGasAverage.setText(carData?.gasAverage.toString())
                    etEthanolAverage.setText(carData?.ethanolAverage.toString())
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.form_menu, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            R.id.action_clear -> {
                clearData()
                return true
            }
            R.id.action_logout -> {
                logout()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun logout() {
        mAuth.signOut()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }


    private fun clearData() {
        etGasPrice.setText(defaultClearValueText)
        etEthanolPrice.setText(defaultClearValueText)
        etGasAverage.setText(defaultClearValueText)
        etEthanolAverage.setText(defaultClearValueText)
    }

    private fun sendDataToAnalytics() {
        val bundle = Bundle()
        bundle.putString("EVENT_NAME", "CALCULATION")
        bundle.putDouble("GAS_PRICE", etGasPrice.text.toString().toDouble())
        bundle.putDouble("ETHANOL_PRICE", etEthanolPrice.text.toString().toDouble())
        bundle.putDouble("GAS_AVERAGE", etGasAverage.text.toString().toDouble())
        bundle.putDouble("ETHANOL_AVERAGE", etEthanolAverage.text.toString().toDouble())

        CalculaFlexTracker.trackEvent(this, bundle)
    }

    private fun loadBanner() {
        val loginBanner =  RemoteConfig.getFirebaseRemoteConfig()
            .getString("banner_image")
        Picasso.get().load(loginBanner).into(ivBanner)
    }

}
