import React, { useState, useEffect } from 'react'
import axios from 'axios'
import Select from 'react-select'
import { Autocomplete } from '@react-google-maps/api'

function ServiceRequestForm() {
  const [formSubmitted, setFormSubmitted] = useState(false)
  const [notification, setNotification] = useState('')

  const category = [
    { value: 'plumbing', label: 'Plumbing' },
    { value: 'yardwork', label: 'Yardwork' },
    { value: 'roofing', label: 'Roofing' },
  ]
  const [autoComplete, setAutoComplete] = useState(null)
  const [selectedCategory, setSelectedCategory] = useState()
  const [serviceRequestModel, setServiceRequestModel] = useState({
    title: '',
    description: '',
    dollars: null,
    address: '',
  })

  const [images, setImages] = useState([])

  const onPlaceChanged = () => {
    if (autoComplete) {
      const place = autoComplete.getPlace()
      const address = place.formatted_address
      const lat = place.geometry.location.lat()
      const lng = place.geometry.location.lng()

      setServiceRequestModel((prevModel) => ({
        ...prevModel,
        address: address,
      }))
    }
  }

  const onLoad = (autoC) => setAutoComplete(autoC)

  async function createServiceRequest(event) {
    event.preventDefault()
    try {
      const response = await axios.post(
        'http://localhost:8080/api/customer/service_request/create',
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
            `http://localhost:8080/api/customer/service_request/${newServiceRequestId}/attach`,
            imageFormData,
            { withCredentials: true },
          )
          .then((res) => {
            console.log(res.data)
          })
          .catch((fail) => {
            alert(
              'There was an error attaching an image to your service request. Please try again.',
            )
            console.error(fail) // Error!
          })
      }

      setNotification('Request created successfully.')
      setFormSubmitted(true)

      // After creating the request, you can clear the form or take any other action.
      setServiceRequestModel({
        title: '',
        description: '',
        dollars: null,
        address: '',
      })
      setImages([])
      // Fetch the updated list of service requests
      // getServiceRequests();
    } catch (error) {
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
            <div className="">
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

            <div className>
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

            <Autocomplete onLoad={onLoad} onPlaceChanged={onPlaceChanged}>
              <div>
                <label className="font-bold">Project Location</label>
                <input className="border border-gray-100 text-gray-900 sm:text-sm rounded-lg focus:ring-primary-600 focus:border-primary-600 block w-full p-2.5 dark:border-gray-600 dark:placeholder-gray-400" />
              </div>
            </Autocomplete>
            <div className="">
              <label className="font-bold">Project Category</label>
              <Select
                options={category}
                placeholder="Choose the Category this Project Falls Under"
                value={selectedCategory}
                onChange={handleCategoryChage}
                isSearchable={true}
                styles={{ backgroundColor: 'red' }}
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
              <label className="font-bold">Maximum Quote</label>

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
              onSubmit={createServiceRequest}
              className="text-white w-full bg-custom-maroon hover:bg-primary-700 focus:ring-4 focus:outline-none focus:ring-primary-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center dark:bg-primary-600 dark:hover:bg-primary-700 dark:focus:ring-primary-800"
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
