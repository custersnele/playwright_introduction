// Function to get URL parameters
function getUrlParameter(name) {
	const urlParams = new URLSearchParams(window.location.search);
	return urlParams.get(name);
}

// Get the event ID from the URL
const eventId = getUrlParameter('id');

// Find the event from the global event list
const event = events.find(e => e.id == eventId);

if (event) {
	// Populate the event details on the page
	document.getElementById('event-image').src = event.image;
	document.getElementById('event-name').innerText = event.name;
	document.getElementById('event-artist').innerText = event.artist;
	document.getElementById('event-date').innerText = event.date;
	document.getElementById('event-description').innerText = event.description;
	document.getElementById('event-price').innerText = event.price;
}

// Add an event listener for the "Place Order" button
document.getElementById('place-order').addEventListener('click', function () {
	const quantity = document.getElementById('quantity').value;

	// Store the selected event details and quantity in localStorage
	const orderDetails = {
		eventId: event.id,
		eventName: event.name,
		eventArtist: event.artist,
		eventDate: event.date,
		eventPrice: event.price,
		eventQuantity: quantity
	};

	localStorage.setItem('orderDetails', JSON.stringify(orderDetails));

	// Redirect to basket.html
	window.location.href = 'basket.html';
});

// Show the number of tickets in the basket (stored by "Place Order") in the header
(function showBasketCount() {
	const stored = localStorage.getItem('orderDetails');
	if (stored) {
		document.getElementById('ticket-count').innerText = JSON.parse(stored).eventQuantity;
	}
})();
