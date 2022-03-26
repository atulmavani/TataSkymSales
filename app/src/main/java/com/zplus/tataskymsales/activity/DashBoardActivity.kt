package com.zplus.tataskymsales.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v4.widget.DrawerLayout
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.zplus.tataskymsales.R
import com.zplus.tataskymsales.api.ApiCall
import com.zplus.tataskymsales.fragment.Homefragment
import com.zplus.tataskymsales.fragment.LogFragment
import com.zplus.tataskymsales.model.zplusresponse.MainResponse
import com.zplus.tataskymsales.service.RechargeService
import com.zplus.tataskymsales.utility.NetworkAvailable
import com.zplus.tataskymsales.utility.SharedPreference
import com.zplus.tataskymsales.utility.StaticUtility
import kotlinx.android.synthetic.main.activity_dash_board.*

class DashBoardActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    var doubleBackToExitPressedOnce = false
    lateinit var loginHandler: Handler
    var mContext =  this@DashBoardActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dash_board)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener(this)

        setonclicklistner()
        StaticUtility.addFragmenttoActivity(supportFragmentManager,Homefragment(),R.id.frame,"")

    }

    //region for setonclick listner
    private fun setonclicklistner() {
        ll_logout.setOnClickListener(this)
        ll_home.setOnClickListener(this)
        ll_log.setOnClickListener(this)
    }
    //endregion

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.ll_home -> {
                StaticUtility.addFragmenttoActivity(supportFragmentManager,Homefragment(),R.id.frame,"")
            }
            R.id.ll_logout -> {
                DoLogout()
                NetworkAvailable(loginHandler).execute()
            }
            R.id.ll_log -> {
                StaticUtility.addFragmenttoActivity(supportFragmentManager, LogFragment(),R.id.frame,Homefragment::class.java.name)
            }
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)
    }

    override fun onBackPressed() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            if(supportFragmentManager.backStackEntryCount >1){
                super.onBackPressed()
            }else{
                if (doubleBackToExitPressedOnce) {
                    //super.onBackPressed()
                    System.exit(0)
                    return
                }
                this.doubleBackToExitPressedOnce = true
                Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show()
                Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
            }

        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {


        }

        return true
    }

    //region for get sim list
    fun DoLogout(){
        loginHandler = Handler(Handler.Callback { msg ->
            if(msg.arg1 == 1){
                if(msg.obj as Boolean) {
                    ApiCall.DoLogoutcall(loginHandler, mContext)
                }else
                    StaticUtility.showMessage(mContext,getString(R.string.network_error))
            }else if(msg.arg1 == 0){
                var respo = msg.obj as MainResponse
                if(respo.code == "200"){
                    val stopServiceIntent = Intent(mContext, RechargeService::class.java)
                    stopService(stopServiceIntent)
                    SharedPreference.ClearPreference(mContext, StaticUtility.LOGINPREFERENCE)
                    SharedPreference.ClearPreference(mContext, StaticUtility.DATA)
                    startActivity(Intent(mContext, LoginActivity::class.java))
                    finish()
                }
            }
            true
        })
    }
    //endregion

}
