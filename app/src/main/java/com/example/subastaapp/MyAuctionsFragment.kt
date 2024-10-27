package com.example.subastaapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MyAuctionsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var auctionAdapter: AuctionAdapter
    private lateinit var auctionList: ArrayList<Auction>
    private lateinit var databaseReference: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflar el layout para el fragmento
        val view = inflater.inflate(R.layout.fragment_my_auctions, container, false)

        // Inicializar el RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewMyAuctions)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        auctionList = ArrayList()
        auctionAdapter = AuctionAdapter(auctionList)
        recyclerView.adapter = auctionAdapter

        // Conectar a Firebase (filtrar subastas por el usuario que las creó)
        databaseReference = FirebaseDatabase.getInstance().getReference("auctions")

        // Aquí deberías asegurarte de filtrar las subastas según el usuario actual (su ID)
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        databaseReference.orderByChild("userId").equalTo(currentUserId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                auctionList.clear()
                if (snapshot.exists()) {
                    for (auctionSnapshot in snapshot.children) {
                        val auction = auctionSnapshot.getValue(Auction::class.java)
                        auction?.let { auctionList.add(it) }
                    }
                    auctionAdapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(requireContext(), "No tienes subastas creadas", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error al cargar las subastas", Toast.LENGTH_SHORT).show()
            }
        })

        return view
    }
}
