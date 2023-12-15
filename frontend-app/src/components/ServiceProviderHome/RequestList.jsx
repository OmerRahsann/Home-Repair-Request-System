import React, { useState, useEffect, createRef } from 'react'
import Select from 'react-select'
import ServiceRequestCard from './ServiceRequestCard'
import { CircularProgress } from '@material-ui/core'
import './scrollbar.css'
import { Autocomplete } from '@react-google-maps/api'
import { InputBase } from '@material-ui/core'
import useStyles from './styles.js'
import { FaSearch } from 'react-icons/fa'
import { getServices } from '../../Helpers/helpers'
import myLocation from '../../Pictures/myLocation.png'

function RequestList({
  onSearch,
  requests,
  isLoading,
  selectedCardIndex,
  onLoad,
  onRequestChanged,
  onCenterLocation,
  setCategoryChange,
  setPriceRangeChange,
}) {
  const [elRefs, setElRefs] = useState([])
  const classes = useStyles()

  useEffect(() => {
    setElRefs((refs) =>
      Array(requests.length)
        .fill()
        .map((_, i) => refs[i] || createRef()),
    )
  }, [requests])

  const [services, setServices] = useState([])

  useEffect(() => {
    // Call the getServices function and update the state with the returned data
    getServices()
      .then((transformedServices) => {
        setServices(transformedServices)
      })
      .catch((error) => {
        console.error('Error:', error)
      })
  }, [])

  const priceRange = [
    { value: [0, 50], label: '$0-50' },
    { value: [51, 100], label: '$51-100' },
    { value: [101, 200], label: '$101-200' },
    { value: [201, 500], label: '$201-500' },
    { value: [501, 1000], label: '$501-1000' },
    { value: [1001, 2500], label: '$1001-2500' },
    { value: [2501, 10000], label: '$2501-10000' },
  ]

  return (
    <div className="">
      {isLoading ? (
        <div className="flex justify-center items-center p-2">
          <CircularProgress size="5rem" />
        </div>
      ) : (
        <>
          <div className="w-full flex flex-col  sm:flex-row">
            <Autocomplete
              onLoad={onLoad}
              onPlaceChanged={onRequestChanged}
              className="flex-grow "
            >
              <div className="flex flex-row p-2 border rounded-lg shadow-sm bg-custom-gray items-center">
                <FaSearch className="text-gray-500" />
                <InputBase
                  classes={{
                    root: classes.inputRoot,
                    input: classes.inputInput,
                  }}
                  fullWidth="true"
                  placeholder="Search by location..."
                />
              </div>
            </Autocomplete>
            <button
              className={`text-white bg-custom-grain hover:bg-primary-700 focus:ring-4 items-center focus:outline-none focus:ring-primary-300 text-2xl rounded-lg text-center dark:bg-primary-600 dark:hover-bg-primary-700 dark:focus-ring-primary-800 m-[.25em]`}
              onClick={onCenterLocation}
            >
              <img
                src={myLocation}
                alt="My Location"
                className="w-10 h-10 p-1 items-center mx-auto"
              />
            </button>
          </div>

          <div className="flex flex-col md:flex-row justify-center md:justify-between  md:pb-2 md:pt-2">
            <Select
              options={services}
              placeholder="Category"
              onChange={setCategoryChange}
              isSearchable={true}
              className="bg-custom-gray w-full md:w-1/2 md:mr-4"
              isClearable
            />

            <Select
              options={priceRange}
              placeholder="Price Range"
              onChange={setPriceRangeChange}
              isSearchable={true}
              className="bg-custom-gray w-full md:w-1/2"
              isClearable
            />
          </div>

          <div className="h-[70vh] overflow-y-auto custom-scrollbar">
            <div className="flex flex-wrap gap-4">
              {requests?.map((request, i) => (
                <div className="w-full p-2 " key={i} ref={elRefs[i]}>
                  <ServiceRequestCard
                    request={request}
                    selected={Number(selectedCardIndex) === i}
                    refProp={elRefs[i]}
                  />
                </div>
              ))}
            </div>
          </div>
        </>
      )}
    </div>
  )
}

export default RequestList
