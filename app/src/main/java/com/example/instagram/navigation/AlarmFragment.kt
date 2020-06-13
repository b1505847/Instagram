package com.example.instagram.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

import com.example.instagram.R
import com.example.instagram.navigation.model.AlarmDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_alarm.view.*
import kotlinx.android.synthetic.main.item_comment.view.*

class AlarmFragment :Fragment(){

    val firestore = FirebaseFirestore.getInstance()
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_alarm,container,false)

        view.alarm_recycleview.adapter =  AlarmRecycleViewAdapter()
        view.alarm_recycleview.layoutManager = LinearLayoutManager(activity)

        return view
    }
    inner class AlarmRecycleViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        val alarmDTOs :ArrayList<AlarmDTO> = arrayListOf()
        init {
            firestore.collection("alarms").whereEqualTo("destinationUid",uid).addSnapshotListener{querySnapshot, firebaseFirestoreException ->
                alarmDTOs.clear()
                if (querySnapshot == null) return@addSnapshotListener
                for (snapshot in querySnapshot.documents){
                    alarmDTOs.add(snapshot.toObject(AlarmDTO::class.java)!!)
                }
                notifyDataSetChanged()
            }

        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment,parent,false)
            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view : View) :RecyclerView.ViewHolder(view)
        override fun getItemCount(): Int {
            return alarmDTOs.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var view = holder.itemView
            when(alarmDTOs[position].kind){
                0 ->{
                    val str_0 = alarmDTOs[position].userId + " " + getString(R.string.alarm_favorite)
                    view.commentviewitem_textview_profile.text = str_0
                }

                1 ->{
                    val str_1 = alarmDTOs[position].userId + " " + getString(R.string.alarm_comment) + " of " + alarmDTOs[position].message
                    view.commentviewitem_textview_profile.text = str_1
                }

                2 ->{
                    val str_2 = alarmDTOs[position].userId + " "  + getString(R.string.alarm_follow)
                    view.commentviewitem_textview_profile.text = str_2
                }
            }
            view.commentviewitem_textview_comment.visibility = View.INVISIBLE
            FirebaseFirestore.getInstance()
                .collection("profileImages")
                .document(alarmDTOs[position].uid!!)
                .get()
                .addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        var url = task.result!!["image"]
                        Glide.with(holder.itemView.context).load(url).apply(RequestOptions().circleCrop()).into(view.commentviewitem_imageview_profile)
                }
                }
        }
    }
}