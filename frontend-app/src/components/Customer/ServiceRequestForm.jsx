import React, { useState, useEffect } from 'react'
import axios from 'axios'
import Select from 'react-select'
import { Autocomplete } from '@react-google-maps/api'

function ServiceRequestForm() {
  const [formSubmitted, setFormSubmitted] = useState(false)
  const [notification, setNotification] = useState('')
  const [autoComplete, setAutoComplete] = useState(null)
  const [selectedCategory, setSelectedCategory] = useState()
  const [serviceRequestModel, setServiceRequestModel] = useState({
    title: '',
    description: '',
    dollars: 0,
    address: '',
    service: '',
  })

  const [services, setServices] = useState([])
  // ... other state variables and functions

  useEffect(() => {
    // Fetch services when the component mounts
    getServices()
  }, [])

  async function getServices() {
    try {
      const response = await axios.get(
        `${process.env.REACT_APP_API_URL}/api/customer/service_request/services`,
        {
          withCredentials: true,
        },
      )

      // Extract the services from the response
      const servicesData = response.data

      // Transform the servicesData into the desired format (label and value are the same)
      const transformedServices = servicesData.map((service) => ({
        label: service,
        value: service,
      }))

      // Update the state with the transformed services
      setServices(transformedServices)
    } catch (error) {
      console.error('Error:', error)
    }
  }

  const [images, setImages] = useState([])
  const [complete, setComplete] = useState(false)

  const onPlaceChanged = () => {
    if (autoComplete) {
      const place = autoComplete.getPlace()

      if (place && place.geometry && place.formatted_address) {
        const address = place.formatted_address
        const lat = place.geometry.location.lat()
        const lng = place.geometry.location.lng()

        setServiceRequestModel((prevModel) => ({
          ...prevModel,
          address: address,
        }))
        setComplete(true)
      } else {
        setComplete(false)
        window.alert('Please enter a valid address.')
      }
    }
  }

  const onLoad = (autoC) => setAutoComplete(autoC)

  async function createServiceRequest(event) {
    event.preventDefault()
    try {
      const response = await axios.post(
        `${process.env.REACT_APP_API_URL}/api/customer/service_request/create`,
        serviceRequestModel,
        { withCredentials: true },
      )

      // Extract the new service request ID from the response
      const newServiceRequestId = response.data
      for (const imageDataUrl of images) {
        // Convert the data URL to a Blob
        const imageBlob = dataURLtoBlob(imageDataUrl)

        // Create form data for the image
        const imageFormData = new FormData()
        imageFormData.append('file', imageBlob)

        // Make a POST request to attach the image to the service request
        await axios
          .post(
            `${process.env.REACT_APP_API_URL}/api/customer/service_request/${newServiceRequestId}/attach`,
            imageFormData,
            { withCredentials: true },
          )
          .then((res) => {
            console.log(res.data)
            // After creating the request, you can clear the form or take any other action.
            setServiceRequestModel({
              title: '',
              description: '',
              dollars: null,
              address: '',
              service: '',
            })
            setImages([])
          })
          .catch((fail) => {
            alert(
              'There was an error attaching an image to your service request. Please try again.',
            )
            console.error(fail) // Error!
          })
      }
      setFormSubmitted(true)
      setNotification('Request created successfully.')
      // Fetch the updated list of service requests
      // getServiceRequests();
    } catch (error) {
      alert(
        'There was an error creating your service request. Please try again.',
      )
      console.error('Error creating service request:', error)
    }
  }

  function dataURLtoBlob(dataURL) {
    const byteString = atob(dataURL.split(',')[1])
    const ab = new ArrayBuffer(byteString.length)
    const ia = new Uint8Array(ab)
    for (let i = 0; i < byteString.length; i++) {
      ia[i] = byteString.charCodeAt(i)
    }
    return new Blob([ab], { type: 'image/jpeg' })
  }

  const handleChange = (evt) => {
    const value = evt.target.value
    setServiceRequestModel({
      ...serviceRequestModel,
      [evt.target.name]: value,
    })
  }

  const handleDescriptionChange = (event) => {
    const text = event.target.value
    setServiceRequestModel({
      ...serviceRequestModel,
      description: text,
    })
  }

  function handleCategoryChage(data) {
    setSelectedCategory(data)
    const value = data.value
    console.log(value)
    setServiceRequestModel((prevModel) => {
      return { ...prevModel, service: value }
    })
  }

  const handleImageUpload = (event) => {
    const files = event.target.files
    const uploadedImages = []

    for (let i = 0; i < files.length; i++) {
      const file = files[i]
      const reader = new FileReader()

      reader.onload = (e) => {
        uploadedImages.push(e.target.result)

        if (uploadedImages.length === files.length) {
          setImages(uploadedImages) // Update the images state
        }
      }

      reader.readAsDataURL(file)
    }
  }

  return (
    <div className="p-6 space-y-4 md:space-y-6 sm:p-8">
      {formSubmitted ? (
        <div className="text-green-600 font-semibold text-center">
          {notification}
        </div>
      ) : (
        <>
          <h1 className="text-xl font-bold leading-tight tracking-tight text-gray-900 md:text-2xl text-center">
            Create a Service Request
          </h1>
          <form
            className="space-y-4 "
            action="#"
            onSubmit={createServiceRequest}
          >
            <div>
              <label className="font-bold ">Project Title</label>
              <input
                type="text"
                name="title"
                value={serviceRequestModel.title}
                onChange={handleChange}
                placeholder="ex: Gutter Cleanup"
                required
                className="border border-gray-100 text-gray-900 sm:text-sm rounded-lg focus:ring-primary-600 focus:border-primary-600 block w-full p-2.5 dark:border-gray-600 dark:placeholder-gray-400 "
              />
              <div className="p-2"></div>
            </div>

            <div>
              <label className="font-bold ">Project Description</label>
              <textarea
                value={serviceRequestModel.description}
                onChange={handleDescriptionChange}
                placeholder="ex: Hello, I'm in need of a professional gutter cleanup for my home. Over time, leaves, debris, and dirt have accumulated in the gutters, causing water to overflow and potentially leading to damage. I'm looking for an experienced service provider to clean and clear out the gutters, ensuring they function properly and prevent any water-related issues"
                className=" border border-gray-300 text-gray-900 sm:text-sm rounded-lg focus:ring-primary-600 focus:border-primary-600 block w-full p-2.5  dark:border-gray-600 dark:placeholder-gray-400 "
                maxLength={500}
                rows={8}
                spellCheck
                style={{ resize: 'none' }}
                required
              />
            </div>

            <label className="font-bold">Project Location</label>
            <Autocomplete onLoad={onLoad} onPlaceChanged={onPlaceChanged}>
              <div>
                <input
                  className="border border-gray-100 text-gray-900 sm:text-sm rounded-lg focus:ring-primary-600 focus:border-primary-600 block w-full p-2.5 dark:border-gray-600 dark:placeholder-gray-400"
                  required
                />
              </div>
            </Autocomplete>
            <div>
              <label className="font-bold">Project Category</label>
              <Select
                options={services}
                placeholder="Choose the Category this Project Falls Under"
                value={selectedCategory}
                onChange={handleCategoryChage}
                isSearchable={true}
                styles={{ backgroundColor: 'red' }}
                required
              />
            </div>
            <div>
              <label className="font-bold">Upload Project Pictures</label>
              <input
                type="file"
                name="pictures"
                accept="image/*"
                multiple
                onChange={handleImageUpload}
                className="border border-gray-100 text-gray-900 sm:text-sm rounded-lg focus:ring-primary-600 focus:border-primary-600 block w-full p-2.5 dark:border-gray-600 dark:placeholder-gray-400"
              />
            </div>

            <div>
              <label className="font-bold">Desired Price</label>

              <input
                name="dollars"
                value={serviceRequestModel.dollars}
                placeholder="ex: 250"
                onChange={handleChange}
                className="bg-white border border-gray-300 text-gray-900 sm:text-sm rounded-lg focus:ring-primary-600 focus:border-primary-600 block w-full p-2.5  dark:border-gray-600 dark:placeholder-gray-400 "
                required
              />
            </div>

            <button
              type="submit"
              className="text-white w-full bg-custom-maroon hover:bg-primary-700 focus:ring-4 focus:outline-none focus:ring-primary-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center disabled:bg-gray-300 dark:bg-primary-600 dark:hover:bg-primary-700 dark:focus:ring-primary-800"
              disabled={!complete}
            >
              SUBMIT REQUEST
            </button>
          </form>
        </>
      )}
    </div>
  )
}

export default ServiceRequestForm
