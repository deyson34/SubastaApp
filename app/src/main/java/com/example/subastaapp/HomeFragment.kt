package com.example.subastaapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var auctionAdapter: AuctionAdapter
    private lateinit var auctionList: ArrayList<Auction>
    private lateinit var filteredList: ArrayList<Auction>
    private lateinit var databaseReference: DatabaseReference
    private lateinit var searchView: SearchView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Inicializar el RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewAuctions)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        auctionList = ArrayList()
        filteredList = ArrayList()
        auctionAdapter = AuctionAdapter(filteredList)
        recyclerView.adapter = auctionAdapter

        // Inicializar el SearchView
        searchView = view.findViewById(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterAuctions(newText ?: "")
                return true
            }
        })

        // Conectar a Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("auctions")

        // Escuchar los cambios en Firebase
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                auctionList.clear()
                if (snapshot.exists()) {
                    for (auctionSnapshot in snapshot.children) {
                        val auction = auctionSnapshot.getValue(Auction::class.java)
                        auction?.let { auctionList.add(it) }
                    }
                    filteredList.addAll(auctionList) // Inicialmente mostrar todas
                    auctionAdapter.notifyDataSetChanged()
                } else {
                    Log.d("HomeFragment", "No se encontraron subastas")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error al cargar las subastas", Toast.LENGTH_SHORT).show()
                Log.e("HomeFragment", error.message)
            }
        })

        return view
    }

    // Filtrar subastas según el texto de búsqueda
    private fun filterAuctions(query: String) {
        filteredList.clear()
        if (query.isEmpty()) {
            filteredList.addAll(auctionList)
        } else {
            for (auction in auctionList) {
                if (auction.title?.contains(query, ignoreCase = true) == true ||
                    auction.description?.contains(query, ignoreCase = true) == true) {
                    filteredList.add(auction)
                }
            }
        }
        auctionAdapter.notifyDataSetChanged()
    }
}
