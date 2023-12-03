import React, { useState, useEffect } from 'react'

const CustomerNotification = ({ isAppointment, isEmailRequest }) => {
  const [isVisible, setIsVisible] = useState(true)

  useEffect(() => {
    const timeout = setTimeout(() => {
      setIsVisible(false)
    }, 5000)

    return () => clearTimeout(timeout)
  }, [])

  return (
    <div
      className={`fixed top-5 right-5 bg-custom-grain text-custom-black p-4 rounded-md ${
        isVisible
          ? 'opacity-100 transform translate-x-0'
          : 'opacity-0 transform translate-x-full'
      } transition-all duration-500 ease-in-out`}
    >
      <div className="flex flex-col">
        <div
          onClick={() => setIsVisible(false)}
          className=" text-red-500 fixed top-1 right-1 text-[2.5vh]"
        >
          x
        </div>
        <div className="p-1"></div>
        <div>You have received a new email request.</div>
        <div>
          Visit{' '}
          <a className="text-blue-400" href="/customer/myappointments">
            here
          </a>{' '}
          to see your updated request.
        </div>
      </div>
      {/* Your notification content goes here */}
      {isAppointment && <p>This is an appointment notification.</p>}
      {isEmailRequest && <p>This is an email request notification.</p>}
    </div>
  )
}

export default CustomerNotification
