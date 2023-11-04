import React, { useState, useEffect, createRef } from 'react'
import Select from 'react-select'
import RequestDetails from '../Customer/RequestDetails'
import ServiceRequestCard from './ServiceRequestCard'
import { CircularProgress, Typography } from '@material-ui/core'
import './scrollbar.css'
import { Autocomplete } from '@react-google-maps/api'
import { InputBase } from '@material-ui/core'
import useStyles from './styles.js'
import { FaSearch } from 'react-icons/fa'

function RequestList({
  onSearch,
  requests,
  isLoading,
  selectedCardIndex,
  onLoad,
  onRequestChanged,
  locationName,
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

  const category = [
    { value: 'plumbing', label: 'Plumbing' },
    { value: 'yardwork', label: 'Yardwork' },
    { value: 'roofing', label: 'Roofing' },
  ]

  const priceRange = [
    { value: '500', label: '$0-500' },
    { value: '1000', label: '500-$1000' },
    { value: '2500', label: '$1000-10,000' },
  ]

  const [categoryChange, setCategoryChange] = useState([])
  const [priceRangeChange, setPriceRangeChange] = useState([])

  function handleCategoryChage(data) {
    setCategoryChange(data)
  }

  function handlePriceSelect(data) {
    setPriceRangeChange(data)
  }

  return (
    <div className="">
      {isLoading ? (
        <div className="flex justify-center items-center p-2">
          <CircularProgress size="5rem" />
        </div>
      ) : (
        <>
          <div className="w-full">
            <Autocomplete onLoad={onLoad} onPlaceChanged={onRequestChanged}>
              <div className="flex flex-row p-2 border rounded-lg shadow-sm bg-custom-gray items-center">
                <FaSearch className="text-gray-500 mr-2" />
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
          </div>

          <div className="flex flex-col md:flex-row justify-center md:justify-between  md:pb-2 md:pt-2">
            <Select
              options={category}
              placeholder="Category"
              value={categoryChange}
              onChange={handleCategoryChage}
              isSearchable={true}
              isMulti
              className="bg-custom-gray w-full md:w-1/2 md:mr-4"
            />

            <Select
              options={priceRange}
              placeholder="Price Range"
              value={priceRangeChange}
              onChange={handlePriceSelect}
              isSearchable={true}
              isMulti
              className="bg-custom-gray w-full md:w-1/2"
            />
          </div>

          <div className="h-[75vh] overflow-y-auto custom-scrollbar">
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
