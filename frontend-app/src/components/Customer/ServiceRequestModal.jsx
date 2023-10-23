import React, { useState, useEffect, useRef } from 'react'
import axios from 'axios'
import image from '../../Pictures/banner.jpeg'

function ServiceRequestModal({ isVisible, onClose, children, size }) {
  const [serviceRequestModel, setServiceRequestModel] = useState({
    title: '',
    description: '',
    dollars: 0,
  })

  const [serviceRequests, setServiceRequests] = useState([])
  const [imageLoaded, setImageLoaded] = useState(false)
  const [isFormOpen, setIsFormOpen] = useState(false)

  const handleClose = (e) => {
    if (e.target.id === 'wrapper') onClose()
  }

  if (!isVisible) return null
  return (
    <div
      className="fixed inset-0 bg-black bg-opacity-25 backdrop-blur-sm items-center flex justify-center z-10"
      id="wrapper"
      onClick={handleClose}
    >
      <div className={`w-[${size || 80}vh]`}>
        <button
          className="text-white text-xl place-self-end"
          onClick={() => onClose()}
        >
          X
        </button>
        <div className="bg-white p-2 overflow-auto max-h-[70vh] rounded-lg">
          {children}
        </div>
      </div>
    </div>
  )
}

export default ServiceRequestModal
