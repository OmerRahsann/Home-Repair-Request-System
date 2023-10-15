// ServiceRequestCard.jsx
import React from 'react';

function ServiceRequestCard({ request }) {
    return (
        <div className="service-request-card bg-white shadow-md p-4 mb-4 rounded">
            <h3 className="text-xl font-bold mb-2">{request.requestDescription}</h3>
            <p><strong>Request Date:</strong> {request.requestDate}</p>
            <p><strong>Status:</strong> {request.requestStatus}</p>
            <p><strong>Location:</strong> {request.geoLocation}</p>
            {/* Add more details as needed */}
        </div>
    );
}

export default ServiceRequestCard;