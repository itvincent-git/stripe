package net.kotlin.ex.sample

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val activities = packageManager.getPackageInfo(getPackageName(), PackageManager.GET_ACTIVITIES).activities
                .filter { !it.name.contains(this@MainActivity.localClassName) }
        rv_activities.layoutManager = LinearLayoutManager(this)
        rv_activities.adapter = object: RecyclerView.Adapter<ActivitiesViewHolder?>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivitiesViewHolder {
                val linearLayout = LinearLayout(parent.context)
                linearLayout.minimumHeight = 100
                linearLayout.addView(TextView(parent.context).apply { id = R.id.tv })
                return ActivitiesViewHolder(linearLayout)
            }

            override fun getItemCount(): Int {
                return activities.size
            }

            override fun onBindViewHolder(holder: ActivitiesViewHolder, position: Int) {
                holder.textView.text = activities[position].name
                holder.linearLayout.setOnClickListener {
                    startActivity(Intent(this@MainActivity, Class.forName(activities[position].name)))
                }
            }
        }


    }

    class ActivitiesViewHolder(val linearLayout: LinearLayout): RecyclerView.ViewHolder(linearLayout) {
        val textView = linearLayout.findViewById<TextView>(R.id.tv)
    }
}
