package sagi.shchori.asiotechapp.ui.fragments.favorites

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import sagi.shchori.asiotechapp.R
import sagi.shchori.asiotechapp.databinding.ItemFavoriteMovieBinding
import sagi.shchori.asiotechapp.ui.models.Movie

interface OnFavoriteMovieClickListener {
    fun onFavoriteMovieClicked(movie: Movie)
    fun onToggleFavoriteClicked(movie: Movie)
}

class FavoritesAdapter(
    private var movies: List<Movie>,
    private val listener: OnFavoriteMovieClickListener
) : RecyclerView.Adapter<FavoritesAdapter.FavoriteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val binding = ItemFavoriteMovieBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return FavoriteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        val currentMovie = movies[position]
        holder.bind(currentMovie)
    }

    override fun getItemCount(): Int {
        return movies.size
    }


    fun submitList(newMovies: List<Movie>) {
        movies = newMovies

        notifyDataSetChanged()
    }

    inner class FavoriteViewHolder(private val binding: ItemFavoriteMovieBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val movie = movies[position]
                    listener.onFavoriteMovieClicked(movie)
                }
            }

            binding.favoriteIconToggle.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val movie = movies[position]
                    listener.onToggleFavoriteClicked(movie)
                }
            }
        }

        fun bind(movie: Movie) {
            binding.apply {
                favoriteMovieTitle.text = movie.title
                favoriteMovieYear.text = movie.year

                Glide.with(itemView.context)
                    .load(movie.poster)
                    .placeholder(R.drawable.ic_image_not_supported_100)
                    .error(R.drawable.ic_image_not_supported_100)
                    .into(favoriteMoviePoster)

                favoriteIconToggle.setImageResource(R.drawable.ic_favorite_24)
            }
        }
    }
}