package com.simats.financetrack

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.simats.financetrack.models.User
import java.text.SimpleDateFormat
import java.util.*

class UserSelectionAdapter(
    private val users: List<User>,
    private val onUserClick: (User) -> Unit
) : RecyclerView.Adapter<UserSelectionAdapter.UserViewHolder>() {

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivProfileImage: ImageView = itemView.findViewById(R.id.ivProfileImage)
        val tvDisplayName: TextView = itemView.findViewById(R.id.tvDisplayName)
        val tvUsername: TextView = itemView.findViewById(R.id.tvUsername)
        val tvLastLogin: TextView = itemView.findViewById(R.id.tvLastLogin)

        fun bind(user: User) {
            tvDisplayName.text = user.displayName
            tvUsername.text = "@${user.username}"
            
            // Format last login time
            if (user.lastLoginAt != null) {
                try {
                    val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    val outputFormat = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
                    val date = inputFormat.parse(user.lastLoginAt)
                    tvLastLogin.text = "Last login: ${outputFormat.format(date!!)}"
                } catch (e: Exception) {
                    tvLastLogin.text = "Last login: ${user.lastLoginAt}"
                }
            } else {
                tvLastLogin.text = "New account"
            }

            // Set profile image - for now use a default drawable
            // In a real app, you'd load the image from user.profileImage
            ivProfileImage.setImageResource(R.drawable.ic_account_circle_24)

            itemView.setOnClickListener {
                onUserClick(user)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_selection, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(users[position])
    }

    override fun getItemCount(): Int = users.size
}