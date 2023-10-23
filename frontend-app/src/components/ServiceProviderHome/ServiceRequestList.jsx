import React, { useState, useEffect } from 'react'
import ServiceRequestCard from './ServiceRequestCard' // Adjust the path accordingly
function ServiceRequestList({ keyword }) {
  const [requests, setRequests] = useState([])

  useEffect(() => {
    const endpoint = keyword
      ? `/api/service-requests/search?keyword=${keyword}`
      : '/api/service-requests'

    fetch(endpoint)
      .then((response) => response.json())
      .then((data) => setRequests(data))
      .catch((error) =>
        console.error('Error fetching service requests:', error),
      )
  }, [keyword])

  return (
    <div className="service-request-list">
      {requests.map((request) => (
        <ServiceRequestCard key={request.requestId} request={request} />
      ))}
    </div>
  )
}

export default ServiceRequestList
