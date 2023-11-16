import React, { useState } from 'react'

function ServiceRequestModal({ isVisible, onClose, children, isFinal, last }) {
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
      <div className={isFinal ? 'w-[100vh] h-[80vh]' : 'w-[80vh]'}>
        <button
          className="text-white text-xl place-self-end"
          onClick={() => onClose()}
        >
          {last ? (
            <div className="p-1 mt-[-20px] rounded-md bg-gray-500 font-bold">
              BACK
            </div>
          ) : (
            'X'
          )}
        </button>
        <div className="bg-white p-2 overflow-auto max-h-[70vh] rounded-lg">
          {children}
        </div>
      </div>
    </div>
  )
}

export default ServiceRequestModal
