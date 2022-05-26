package com.odom.exchangecalculation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private val API_KEY = "RGjXctwBsedGzwJXfibpnV4iu77JCDJs"
    private var timeStamp = 0L
    private var currencyKr = 0.0
    private var currencyJp = 0.0
    private var currencyPh = 0.0
    private val decFormat = DecimalFormat("#,###,###.##")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        et_inputMoney.setOnEditorActionListener { _, _, _ ->
            if(et_inputMoney.text.toString() != ""){
                val inputMoney = et_inputMoney.text.toString().toDouble()
                calculate(inputMoney)
            }

            false
        }

        rb_kr.setOnClickListener {
            recipientCountry.text = getString(R.string.korea)
            tv_rate.text =  decFormat.format(currencyKr).toString()
            tv_unit.text = getString(R.string.KRWUSD)
        }
        rb_jp.setOnClickListener {
            recipientCountry.text = getString(R.string.japan)
            tv_rate.text = decFormat.format(currencyJp).toString()
            tv_unit.text = getString(R.string.JPYUSD)
        }
        rb_ph.setOnClickListener {
            recipientCountry.text = getString(R.string.philippines)
            tv_rate.text = decFormat.format(currencyPh).toString()
            tv_unit.text = getString(R.string.PHPUSD)
        }


    }

    override fun onResume() {
        super.onResume()
        jobGetInfo().onJoin
    }

    private fun jobGetInfo() = CoroutineScope(Dispatchers.Main).launch {

        withContext(Dispatchers.IO){

            try {
                val jsonObject = readData()
                val time = jsonObject.getString("timestamp")
                timeStamp = time.toLong()

                val quotes = jsonObject.getJSONObject("quotes")
                currencyKr = String.format("%.2f", quotes.getDouble("USDKRW")).toDouble()
                currencyJp = String.format("%.2f", quotes.getDouble("USDJPY")).toDouble()
                currencyPh = String.format("%.2f", quotes.getDouble("USDPHP")).toDouble()

            }catch (e:Exception){
                e.printStackTrace()
            }

        }

        // api에서 가져오는 시간은 자릿수부족으로 정확하지 않음
        //val date = Date(timeStamp)

        val date = Date(System.currentTimeMillis())
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale("ko", "KR"))
        val stringTime = dateFormat.format(date)
        tv_time.text = stringTime.toString()

        recipientCountry.text = getString(R.string.korea)
        tv_rate.text =  decFormat.format(currencyKr).toString()
        tv_unit.text = getString(R.string.KRWUSD)
    }

    private fun readData() : JSONObject {
        val url = URL(
           "https://api.apilayer.com/currency_data/live?apikey=${API_KEY}"
        )

        val connection = url.openConnection()
        val data = connection.getInputStream().readBytes().toString(charset("UTF-8"))

        return JSONObject(data)
    }

    private fun calculate(money : Double){

        if(money < 0 || money > 10000){
            Toast.makeText(this, "송금액이 바르지 않습니다", Toast.LENGTH_SHORT).show()

        }else{
            tv_result.text = when {
                rb_kr.isChecked -> String.format(getString(R.string.result_kr), currencyKr * money)
                rb_jp.isChecked -> String.format(getString(R.string.result_jp), currencyJp * money)
                rb_ph.isChecked -> String.format(getString(R.string.result_ph), currencyPh * money)
                else -> ""
            }

        }

    }
}