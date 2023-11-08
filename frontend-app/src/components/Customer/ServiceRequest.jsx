import React, { useState, useEffect, useRef, Fragment } from 'react'
import image from '../../Pictures/banner.jpeg'
import ServiceRequestModal from './ServiceRequestModal'
import ServiceRequestForm from './ServiceRequestForm'
import { checkIsCustomerLoggedIn } from '../../AuthContext'

function ServiceRequestBanner() {
  const [imageLoaded, setImageLoaded] = useState(false)
  const [showModal, setShowModal] = useState(false)

  const [position, setPosition] = useState({ top: 0, left: 0 })
  const imageRef = useRef(null)

  const handleCreateRequestClick = async () => {
    const isCustomerLoggedIn = await checkIsCustomerLoggedIn()

    if (isCustomerLoggedIn) {
      setShowModal(true)
    } else {
      // Display a message to prompt the user to log in or create an account
      alert('Please log in or create an account to get started.')
    }
  }

  useEffect(() => {
    const centerContent = () => {
      if (imageRef.current) {
        const image = imageRef.current
        const content = imageRef.current.nextSibling
        const top = (image.clientHeight - content.clientHeight) / 3
        const left = (image.clientWidth - content.clientWidth) / 2
        setPosition({ top, left })
      }
    }

    const handleResize = () => {
      if (imageRef.current) {
        centerContent()
      }
    }

    // Add an event listener for the image load event
    if (imageRef.current) {
      imageRef.current.addEventListener('load', () => {
        centerContent()
        setImageLoaded(true) // Set imageLoaded to true when the image has loaded
      })
    }

    window.addEventListener('resize', handleResize)
    centerContent()

    return () => {
      window.removeEventListener('resize', handleResize)
      // Remove the load event listener to prevent memory leaks
      if (imageRef.current) {
        imageRef.current.removeEventListener('load', () => {
          centerContent()
        })
      }
    }
  }, [])

  return (
    <Fragment>
      <div className="bg-gradient-to-r from-[#000000]  w-full relative">
        <img
          ref={imageRef}
          src={image}
          alt="Banner Image"
          className="w-full h-auto opacity-30"
        />
        <div
          className="absolute text-white"
          style={{
            top: `${position.top}px`,
            left: `${position.left}px`,
          }}
        >
          <h1 className="text-[3vw] font-bold pl-10">Request a Home Repair</h1>
          <h2 className="text-[1.5vw] font-bold pl-10 pt-5">
            To Create a Service Request:
          </h2>
          <p className="text-[1.2vw] pl-10 max-w-[40%] ">
            simply tell us about the home repair or improvement project you need
            assistance with. We'll match you with qualified service providers in
            your area who can help you get the job done. Whether it's a plumbing
            issue, electrical work, remodeling, or any other home-related task,
            our platform makes it easy to get started. Just provide the details,
            and we'll take care of the rest.
          </p>

          <div className="pl-10 pt-5">
            <button
              onClick={handleCreateRequestClick}
              type="submit"
              className="font-bold text-white bg-custom-tan hover:bg-custom-maroon focus:ring-4 focus:outline-none focus:ring-primary-300 text-[1.5vw] px-5 py-2.5 text-center dark:bg-primary-600 dark:hover:bg-primary-700 dark:focus:ring-primary-800"
            >
              CREATE REQUEST
            </button>
          </div>
        </div>
      </div>
      <ServiceRequestModal
        isVisible={showModal}
        onClose={() => setShowModal(false)}
      >
        <ServiceRequestForm />
      </ServiceRequestModal>
    </Fragment>
  )
}

export default ServiceRequestBanner
