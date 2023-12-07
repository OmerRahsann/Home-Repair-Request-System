import React, { useState, useEffect } from 'react'
import { getNotifications } from '../../Helpers/helpers'

const NotificationSlider = () => {
  const [notifications, setNotifications] = useState([])
  const [isVisible, setIsVisible] = useState(false)

  const fetchNotifications = async () => {
    try {
      const notificationsData = await getNotifications()

      if (!notificationsData) {
        return
      }

      const latestTimestamp =
        notificationsData.length > 0 ? notificationsData[0].timestamp : null

      // Retrieve the last stored timestamp from local storage
      const lastNotificationTimestamp = localStorage.getItem(
        'lastNotificationTimestamp',
      )

      // Check if the latest timestamp is different from the last stored timestamp
      const newNotifications =
        !lastNotificationTimestamp ||
        lastNotificationTimestamp !== latestTimestamp

      if (!newNotifications) {
        return
      }

      // Store the latest timestamp in local storage
      localStorage.setItem('lastNotificationTimestamp', latestTimestamp)

      setNotifications(notificationsData)
      console.log(notificationsData)

      // Show the notification when new notifications are fetched
      setIsVisible(true)

      // Hide the notification after 5 seconds
      const timeout = setTimeout(() => {
        setIsVisible(false)
      }, 7500)

      return () => clearTimeout(timeout)
    } catch (error) {
      console.log(error)
    }
  }

  useEffect(() => {
    // Fetch notifications every 30 seconds
    const intervalId = setInterval(() => {
      fetchNotifications()
    }, 3000)

    // Clear the interval when the component is unmounted
    return () => clearInterval(intervalId)
  }, [notifications])

  return (
    <div>
      {' '}
      {notifications.length > 0 && !notifications[0].read && isVisible && (
        <div
          className={`fixed top-5 right-5 bg-custom-grain text-custom-black p-4 rounded-md ${
            isVisible
              ? 'opacity-100 transform translate-x-0 z-20 transition-transform duration-500 ease-in-out'
              : 'opacity-0 transform translate-x-full transition-transform duration-500 ease-in-out'
          }`}
        >
          <div>
            <div className="bg-white p-4 mb-4 rounded-md shadow-md">
              <div className="flex flex-col">
                <div
                  onClick={() => setIsVisible(false)}
                  className="text-red-500 fixed top-1 right-1 text-[2.5vh]"
                >
                  x
                </div>
                <div className="p-1"></div>
                <div className="font-bold">🆕 {notifications[0].title}</div>
                <div>{notifications[0].message}</div>
                <div className="text-sm text-gray-500">
                  {new Date(notifications[0].timestamp).toLocaleDateString(
                    undefined,
                    {
                      year: 'numeric',
                      month: 'numeric',
                      day: 'numeric',
                    },
                  )}{' '}
                  {new Date(notifications[0].timestamp).toLocaleTimeString(
                    undefined,
                    {
                      hour: 'numeric',
                      minute: 'numeric',
                      second: 'numeric',
                    },
                  )}
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

export default NotificationSlider
