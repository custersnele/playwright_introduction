

// Function to generate and display events
function displayEvents(eventList) {
	const eventTableBody = document.getElementById('event-list');
	eventTableBody.innerHTML = ''; // Clear the table before populating
	eventList.forEach(event => {
		const row = `
      <tr>
        <td class="event-image">
          <img src="${event.image}" alt="${event.name}" />
        </td>
        <td class="event-date" data-testid="event-date-${event.id}">${event.date}</td>
        <td class="event-name" data-testid="event-name-${event.id}">${event.name}</td>
        <td class="event-artist" data-testid="event-artist-${event.id}">${event.artist}</td>
        <td class="event-price" data-testid="event-price-${event.id}">${event.price}</td>
        <td class="event-purchase-button">
          <a href="details.html?id=${event.id}" data-testid="purchase-link-${event.id}">
            <button data-testid="purchase-button-${event.id}">Purchase Details</button>
          </a>
        </td>
      </tr>
    `;
		eventTableBody.insertAdjacentHTML('beforeend', row);
	});
}

// Event filtering based on dropdown selection
document.getElementById('filter-button').addEventListener('click', function() {
	const filterText = document.getElementById('filter-text').value;
	const filteredEvents = filterText === ''
		? events
		: events.filter((event => event.name.toLowerCase().includes(filterText) || event.artist.toLowerCase().includes(filterText)));

	displayEvents(filteredEvents);
});

// Initial display of all events
displayEvents(events);

// Show the number of tickets in the basket (stored by "Place Order") in the header
(function showBasketCount() {
	const stored = localStorage.getItem('orderDetails');
	if (stored) {
		document.getElementById('ticket-count').innerText = JSON.parse(stored).eventQuantity;
	}
})();
