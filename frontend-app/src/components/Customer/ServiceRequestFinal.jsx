import React, { useState } from 'react'
import Select from 'react-select'
import ImageSlider from '../ImageSlider'
import RequestEdit from './RequestEdit'
import { AiFillEdit } from 'react-icons/ai'
import { BsFillTrash3Fill } from 'react-icons/bs'
import Map from '../Map/Map'
import axios from 'axios'

function ServiceRequestFinal({ request }) {
  const [editMode, setEditMode] = useState(false)
  const [editedRequest, setEditedRequest] = useState({ ...request })

  const handleEditClick = () => {
    setEditMode(true)
  }

  const handleDeleteClick = async () => {
    // Use the window.confirm method to ask for confirmation
    const confirm = window.confirm(
      'Are you sure you want to delete this request?',
    )

    if (confirm) {
      // User confirmed, proceed with the deletion
      try {
        const response = await axios.delete(
          `http://localhost:8080/api/customer/service_request/${request.id}`,
          { withCredentials: true },
        )
        setTimeout(() => {
          window.location.reload()
        }, 250)
        // Handle the response or any further actions here
      } catch (error) {
        // Handle any errors that occur during the deletion
      }
    } else {
      // User canceled the deletion, no action needed
    }
  }

  const handleSaveClick = () => {
    // Handle saving the edited request data here, you can make a PUT request.
    // Then, set editMode to false and update the editedRequest.

    // Example:
    // axios.put(`http://localhost:8080/api/customer/service_request/${request.id}`, editedRequest).then(response => {
    //     setEditedRequest(response.data);
    //     setEditMode(false);
    // });

    setEditMode(false)
  }

  const handleChange = (event) => {
    const { name, value } = event.target
    setEditedRequest({ ...editedRequest, [name]: value })
  }

  return (
    <div>
      <div>
        {editMode ? (
          <>
            <RequestEdit request={request} />
            {/* Render other editable fields similarly */}
          </>
        ) : (
          <>
            <div className="p-2 ">
              <ImageSlider images={request.pictures} />
              <div className="p-2 flex flex-col border border-gray-800 border-5 border-t-0">
                <div className="flex flex-row justify-between">
                  <h1 className="font-bold text-[3.5vh]">{request.title}</h1>
                  <div className="flex flex-row ">
                    <AiFillEdit onClick={handleEditClick} />
                    <div className="p-1"></div>
                    <BsFillTrash3Fill onClick={handleDeleteClick} />
                  </div>
                </div>
                <p>
                  <strong className="font-semibold">
                    Project Description:
                  </strong>{' '}
                  {request.description}
                </p>
                <h2>
                  <strong className="font-semibold">Price Range: </strong> $
                  {request.dollars}
                </h2>
                <h2>
                  <strong className="font-semibold">Location:</strong>{' '}
                  {request.address}
                </h2>
              </div>
              <div>
                <Map address={request.address} />
              </div>
            </div>
            {/* Render other non-editable fields */}
          </>
        )}
      </div>
    </div>
  )
}

export default ServiceRequestFinal
