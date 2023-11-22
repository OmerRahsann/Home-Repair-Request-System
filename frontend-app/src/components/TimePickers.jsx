import React, { useState } from 'react'
import ServiceRequestModal from './Customer/ServiceRequestModal'
import TimePicker from 'react-time-picker'
import 'react-time-picker/dist/TimePicker.css'

const TimePickers = ({ onConfirm, setShowModal, day }) => {
  const [startTime, setStartTime] = useState(null)
  const [endTime, setEndTime] = useState(null)
  const [value, onChange] = useState('10:00')
  console.log(startTime)

  const handleConfirm = () => {
    // Pass the selected start and end times to the parent component
    if (endTime && startTime && endTime.isBefore(startTime)) {
      // Show an error or handle the invalid case as needed
      window.alert('End time cannot be before the start time.')
      return
    }
    onConfirm && onConfirm({ startTime, endTime })

    // Close the modal
    setShowModal(false)
  }

  return (
    <div>
      <div className="flex flex-row justify-between p-2">
        <div className="flex flex-col">
          <label>Start Time</label>
          <TimePicker
            clockIcon={false}
            disableClock
            value={startTime}
            onChange={setStartTime}
          />
        </div>
        <div className="flex flex-col">
          <label>End Time</label>
          <TimePicker
            clockIcon={false}
            disableClock
            value={endTime}
            onChange={setEndTime}
          />
        </div>
      </div>
      <button
        onClick={handleConfirm}
        className="w-full text-white bg-custom-maroon hover:bg-primary-700 focus:ring-4 focus:outline-none focus:ring-primary-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center dark:bg-primary-600 dark:hover:bg-primary-700 dark:focus:ring-primary-800"
      >
        Confirm
      </button>
    </div>
  )
}

export default TimePickers
