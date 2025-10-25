document.addEventListener('DOMContentLoaded', () => {
    // --- Element Cache ---
    const allElements = {
        searchInput: document.getElementById('movieSearchInput'),
        autocompleteResults: document.getElementById('autocompleteResults'),
        detailsView: document.getElementById('details-view'),
        moviePoster: document.getElementById('moviePoster'),
        movieTitle: document.getElementById('movieTitle'),
        movieMeta: document.getElementById('movieMeta'),
        movieDescription: document.getElementById('movieDescription'),
        castList: document.getElementById('castList'),
        similarMoviesList: document.getElementById('similarMoviesList'),
        externalReviewsList: document.getElementById('externalReviewsList'),
        reviewForm: document.getElementById('reviewForm'),
        reviewText: document.getElementById('reviewText'),
        latestResult: document.getElementById('latest-result'),
        myReviewsList: document.getElementById('myReviewsList'),
        myReviewSummary: document.getElementById('myReviewSummary'),
        starRatingInput: document.querySelector('.star-rating-input'),
        stars: document.querySelectorAll('.star-rating-input .star'),
    };

    // --- State & Config ---
    const API_BASE_URL = '/api'; // Relative URL because frontend and backend are on the same server
    const IMAGE_BASE_URL = 'https://image.tmdb.org/t/p/';
    let selectedMovie = null;
    let currentRating = 0;
    let debounceTimer;

    // --- Main Data Fetching & Rendering Logic ---

    const selectMovie = async (movie) => {
        selectedMovie = movie;
        allElements.searchInput.value = ''; // Clear search bar
        allElements.autocompleteResults.style.display = 'none'; // Hide suggestions

        // Render main details immediately from the search result object
        renderMovieDetails(movie);
        
        // Asynchronously fetch and render all supplementary data
        fetchAndRenderCast(movie.filmid);
        fetchAndRenderExternalReviews(movie.filmid);
        fetchAndRenderSimilarMovies(movie.filmid);
        fetchMyReviewStats(movie.title);
        
        // Show the main details view
        allElements.detailsView.classList.add('visible');
    };

    const renderMovieDetails = (details) => {
        allElements.movieTitle.textContent = details.title;
        allElements.moviePoster.src = details.posterPath ? `${IMAGE_BASE_URL}w500${details.posterPath}` : 'placeholder.png';
        allElements.movieDescription.textContent = details.overview;
        const releaseYear = details.releaseDate ? details.releaseDate.substring(0, 4) : 'N/A';
        allElements.movieMeta.innerHTML = `<span>${releaseYear}</span> &bull; <span>Vote Average: ${details.voteAverage.toFixed(1)}/10</span>`;
    };

    const fetchAndRenderCast = async (movieId) => {
        allElements.castList.innerHTML = '';
        try {
            const response = await fetch(`${API_BASE_URL}/movies/${movieId}/cast`);
            const cast = await response.json();
            if (cast && cast.length > 0) {
                cast.forEach(member => {
                    const profileImage = member.profilePath ? `${IMAGE_BASE_URL}w185${member.profilePath}` : 'avatar.png';
                    allElements.castList.innerHTML += `
                        <div class="cast-member">
                            <img src="${profileImage}" alt="${member.name}">
                            <div class="name">${member.name}</div>
                            <div class="character">${member.character}</div>
                        </div>`;
                });
            } else {
                 allElements.castList.innerHTML = '<p>No cast information available in local data.</p>';
            }
        } catch (error) { console.error('Error fetching cast:', error); }
    };

    const fetchAndRenderExternalReviews = async (movieId) => {
        allElements.externalReviewsList.innerHTML = '';
        try {
            const response = await fetch(`${API_BASE_URL}/movies/${movieId}/external-reviews`);
            const reviews = await response.json();
            if (reviews && reviews.length > 0) {
                reviews.forEach(review => {
                    allElements.externalReviewsList.innerHTML += `
                        <div class="review-item">
                            <h3>By: ${review.author} (Sentiment: ${review.sentiment})</h3>
                            <p>${review.content}</p>
                        </div>`;
                });
            } else {
                allElements.externalReviewsList.innerHTML = '<p>No external reviews found in our local database.</p>';
            }
        } catch (error) { console.error('Error fetching external reviews:', error); }
    };

    const fetchAndRenderSimilarMovies = async (movieId) => {
        allElements.similarMoviesList.innerHTML = '';
        try {
            const response = await fetch(`${API_BASE_URL}/movies/${movieId}/similar`);
            const similarMovies = await response.json();
            if (similarMovies && similarMovies.length > 0) {
                similarMovies.forEach(movie => {
                    const poster = movie.posterPath ? `${IMAGE_BASE_URL}w185${movie.posterPath}` : 'placeholder.png';
                    const similarMovieEl = document.createElement('div');
                    similarMovieEl.className = 'similar-movie';
                    similarMovieEl.innerHTML = `<img src="${poster}" alt="${movie.title}"><div class="title">${movie.title}</div>`;
                    similarMovieEl.addEventListener('click', () => selectMovie(movie)); // Allows clicking on similar movies
                    allElements.similarMoviesList.appendChild(similarMovieEl);
                });
            } else {
                 allElements.similarMoviesList.innerHTML = '<p>No similar movies found in local data.</p>';
            }
        } catch (error) { console.error('Error fetching similar movies:', error); }
    };

    const fetchMyReviewStats = async (title) => {
        try {
            const response = await fetch(`${API_BASE_URL}/reviews/stats?title=${encodeURIComponent(title)}`);
            const stats = await response.json();
            
            allElements.myReviewSummary.innerHTML = `
                <div class="summary-item"><div class="value">${stats.averageRating.toFixed(1)}</div><div class="label">Avg Rating</div></div>
                <div class="summary-item"><div class="value">${stats.totalReviews}</div><div class="label">Total Reviews</div></div>
                <div class="summary-item"><div class="value">${stats.dominantSentiment}</div><div class="label">Overall Sentiment</div></div>`;

            allElements.myReviewsList.innerHTML = '';
            if (stats.reviews && stats.reviews.length > 0) {
                stats.reviews.forEach(review => {
                    allElements.myReviewsList.innerHTML += `
                        <div class="review-item">
                            ${generateStarDisplay(review.rating)}
                            <p>${review.reviewText}</p>
                            <strong>SENTIMENT: ${review.sentiment.toUpperCase()}</strong>
                        </div>`;
                });
            } else {
                allElements.myReviewsList.innerHTML = '<p>You have not reviewed this movie yet.</p>';
            }
        } catch (error) { console.error("Failed to fetch my review stats", error); }
    };

    // --- Star Rating Logic ---
    const generateStarDisplay = (rating) => {
        let starHTML = '';
        for (let i = 1; i <= 5; i++) {
            let starClass = 'star';
            if (rating >= i) starClass += ' filled';
            else if (rating >= i - 0.5) starClass += ' half';
            starHTML += `<span class="${starClass}">&#9733;</span>`;
        }
        return `<div class="star-rating-display">${starHTML}</div>`;
    };

    const highlightStars = (rating) => {
        allElements.stars.forEach(star => {
            const starValue = parseFloat(star.dataset.value);
            star.classList.remove('filled', 'half');
            if (rating >= starValue) star.classList.add('filled');
            else if (rating >= starValue - 0.5) star.classList.add('half');
        });
    };

    const resetStars = () => { currentRating = 0; highlightStars(0); };
    
    // --- Event Listeners ---
    allElements.searchInput.addEventListener('input', () => {
        clearTimeout(debounceTimer);
        debounceTimer = setTimeout(async () => {
            const query = allElements.searchInput.value;
            if (query.length < 3) {
                allElements.autocompleteResults.style.display = 'none';
                return;
            }
            try {
                const response = await fetch(`${API_BASE_URL}/movies/search?query=${encodeURIComponent(query)}`);
                const movies = await response.json();
                allElements.autocompleteResults.innerHTML = '';
                if (movies.length > 0) {
                    allElements.autocompleteResults.style.display = 'block';
                    movies.forEach(movie => {
                        const item = document.createElement('div');
                        item.className = 'autocomplete-item';
                        const year = movie.releaseDate ? ` (${movie.releaseDate.substring(0, 4)})` : '';
                        item.textContent = `${movie.title}${year}`;
                        item.addEventListener('click', () => selectMovie(movie));
                        allElements.autocompleteResults.appendChild(item);
                    });
                }
            } catch (error) { console.error('Autocomplete error:', error); }
        }, 300); // 300ms debounce delay
    });

    allElements.reviewForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        if (!selectedMovie || currentRating === 0) {
            alert("Please select a movie and provide a star rating.");
            return;
        }
        try {
            const response = await fetch(`${API_BASE_URL}/reviews`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ movieTitle: selectedMovie.title, reviewText: allElements.reviewText.value, rating: currentRating }),
            });
            const newReview = await response.json();
            allElements.latestResult.innerHTML = `Submitted! Your review's sentiment: <strong>${newReview.sentiment.toUpperCase()}</strong>`;
            allElements.reviewText.value = '';
            resetStars();
            fetchMyReviewStats(selectedMovie.title);
        } catch (error) { console.error('Error submitting review:', error); }
    });
    
    allElements.stars.forEach(star => {
        star.addEventListener('mousemove', (e) => {
            const rect = star.getBoundingClientRect();
            const isHalf = e.clientX - rect.left < rect.width / 2;
            highlightStars(parseFloat(star.dataset.value) - (isHalf ? 0.5 : 0));
        });
        star.addEventListener('click', (e) => {
            const rect = star.getBoundingClientRect();
            const isHalf = e.clientX - rect.left < rect.width / 2;
            currentRating = parseFloat(star.dataset.value) - (isHalf ? 0.5 : 0);
            highlightStars(currentRating);
        });
    });
    
    allElements.starRatingInput.addEventListener('mouseout', () => highlightStars(currentRating));
    
    document.addEventListener('click', (e) => {
        if (!allElements.searchInput.contains(e.target)) {
            allElements.autocompleteResults.style.display = 'none';
        }
    });
});