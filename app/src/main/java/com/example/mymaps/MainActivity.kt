package com.example.mymaps

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mymaps.model.Place
import com.example.mymaps.model.UserMap
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.*

const val EXTRA_USER_MAP = "EXTRA_USER_MAP"
const val EXTRA_MAP_TITLE = "EXTRA_MAP_TITLE"
const val FILENAME = "UserGenerated.data"
class MainActivity : AppCompatActivity() {

    private lateinit var userMaps: MutableList<UserMap>
    private lateinit var rvMaps: RecyclerView

    private val contract = registerForActivityResult(Contract()) {
        Log.i("IntoContract", "Received ${it.title}")
        userMaps.add(it)
        rvMaps.adapter?.notifyItemInserted(userMaps.size - 1)
        serializeData(this, userMaps)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        userMaps = deserializeData(this).toMutableList()
        rvMaps = findViewById<RecyclerView>(R.id.rvMaps)
        rvMaps.layoutManager = LinearLayoutManager(this)
        rvMaps.adapter = MapsAdapter(this, userMaps, object : MapsAdapter.OnClickListener {
            override fun onItemClick(position: Int) {
                Log.i("MainActivity", "Tapped on $position")
                val intent = Intent(this@MainActivity, DisplayMapActivity::class.java)
                intent.putExtra(EXTRA_USER_MAP, userMaps[position])
                startActivity(intent)
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
            }
        })

        val fabCreateMap = findViewById<FloatingActionButton>(R.id.fabCreateMap)
        fabCreateMap.setOnClickListener {
            showAlertDialog()
        }
    }

    private fun showAlertDialog() {
        val placeFormView = LayoutInflater.from(this).inflate(R.layout.dialog_create_title, null)
        val dialog = AlertDialog.Builder(this)
            .setTitle("Map Title")
            .setView(placeFormView)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("OK", null)
            .show()

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
            val title = placeFormView.findViewById<EditText>(R.id.etTitleForMap).text.toString()
            if (title.trim().isEmpty()) {
                Toast.makeText(this, "Map title must be non-empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            dialog.dismiss()
            contract.launch(title)
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        }
    }

    private fun serializeData(context: Context, userMaps: List<UserMap>) {
        ObjectOutputStream(FileOutputStream(getFile(context))).use {
            it.writeObject(userMaps)
        }
    }

    private fun deserializeData(context: Context): List<UserMap> {
        val file = getFile(context)
        if (!file.exists()) {
            return generateSampleData()
        }
        ObjectInputStream(FileInputStream(file)).use {
            return it.readObject() as List<UserMap>
        }
    }

    private fun getFile(context: Context): File {
        return File(context.filesDir, FILENAME)
    }
    private fun generateSampleData(): List<UserMap> {
        return listOf(
            UserMap(
                "Memories from University",
                listOf(
                    Place("Branner Hall", "Best dorm at Stanford", 37.426, -122.163),
                    Place("Gates CS building", "Many long nights in this basement", 37.430, -122.173),
                    Place("Pinkberry", "First date with my wife", 37.444, -122.170)
                )
            ),
            UserMap("January vacation planning!",
                listOf(
                    Place("Tokyo", "Overnight layover", 35.67, 139.65),
                    Place("Ranchi", "Family visit + wedding!", 23.34, 85.31),
                    Place("Singapore", "Inspired by \"Crazy Rich Asians\"", 1.35, 103.82)
                )),
            UserMap("Singapore travel itinerary",
                listOf(
                    Place("Gardens by the Bay", "Amazing urban nature park", 1.282, 103.864),
                    Place("Jurong Bird Park", "Family-friendly park with many varieties of birds", 1.319, 103.706),
                    Place("Sentosa", "Island resort with panoramic views", 1.249, 103.830),
                    Place("Botanic Gardens", "One of the world's greatest tropical gardens", 1.3138, 103.8159)
                )
            ),
            UserMap("My favorite places in the Midwest",
                listOf(
                    Place("Chicago", "Urban center of the midwest, the \"Windy City\"", 41.878, -87.630),
                    Place("Rochester, Michigan", "The best of Detroit suburbia", 42.681, -83.134),
                    Place("Mackinaw City", "The entrance into the Upper Peninsula", 45.777, -84.727),
                    Place("Michigan State University", "Home to the Spartans", 42.701, -84.482),
                    Place("University of Michigan", "Home to the Wolverines", 42.278, -83.738)
                )
            ),
            UserMap("Restaurants to try",
                listOf(
                    Place("Champ's Diner", "Retro diner in Brooklyn", 40.709, -73.941),
                    Place("Althea", "Chicago upscale dining with an amazing view", 41.895, -87.625),
                    Place("Shizen", "Elegant sushi in San Francisco", 37.768, -122.422),
                    Place("Citizen Eatery", "Bright cafe in Austin with a pink rabbit", 30.322, -97.739),
                    Place("Kati Thai", "Authentic Portland Thai food, served with love", 45.505, -122.635)
                )
            )
        )
    }
}