import React, { useState, useEffect } from 'react'
import { getNotifications, markReadNotifications } from '../../Helpers/helpers'
import { clearNotifications } from '../../Helpers/helpers'

const Notifications = ({ onClose, interval }) => {
  const [notifications, setNotifications] = useState([])

  const markRead = async () => {
    try {
      const response = await markReadNotifications()
      console.log(response)
    } catch (error) {
      console.log(error)
    }
  }

  const deleteNotifications = async () => {
    try {
      const response = await clearNotifications()
      console.log(response)
    } catch (error) {
      console.log(error)
    }
  }

  useEffect(() => {
    // Fetch notifications when the component mounts
    const fetchNotifications = async () => {
      try {
        const notificationsData = await getNotifications()
        setNotifications(notificationsData)
        console.log(notificationsData)
      } catch (error) {
        console.log(error)
      }
    }

    // Fetch notifications initially
    fetchNotifications()
  }, [])

  return (
    <div>
      <div>
        <div className="fixed top-0 right-1 text-[2.5vh] flex flex-row justify-between ">
          <button
            onClick={() => {
              deleteNotifications()
              onClose()
            }}
            className="text-custom-black hover:bg-primary-700 focus:ring-4 focus:outline-none focus:ring-primary-300 font-medium rounded-lg text-sm px-2 py-1.5 text-center dark:bg-primary-600 dark:hover:bg-primary-700 dark:focus:ring-primary-800"
          >
            Clear All
          </button>
          <div className="p-1"></div>
          <button
            onClick={() => {
              onClose()
              markRead()
            }}
            className="text-red-500  hover:bg-primary-700 focus:ring-4 focus:outline-none focus:ring-primary-300 font-medium rounded-lg text-sm px-2 py-1.5 text-center dark:bg-primary-600 dark:hover:bg-primary-700 dark:focus:ring-primary-800"
          >
            x{' '}
          </button>
        </div>
        <div className="p-3"></div>
        <div className="max-h-[50vh] overflow-y-auto">
          {notifications.length > 0 ? (
            notifications.map((notification, index) => (
              <div
                key={index}
                className="bg-white p-4 mb-4 rounded-md shadow-md"
              >
                {!notification.read && (
                  <div className="flex text-[2.0vh]">🆕</div>
                )}

                <div className="font-bold">{notification.title}</div>
                <div>{notification.message}</div>
                <div className="text-sm text-gray-500">
                  {new Date(notification.timestamp).toLocaleDateString(
                    undefined,
                    {
                      year: 'numeric',
                      month: 'numeric',
                      day: 'numeric',
                    },
                  )}{' '}
                  {new Date(notification.timestamp).toLocaleTimeString(
                    undefined,
                    {
                      hour: 'numeric',
                      minute: 'numeric',
                      second: 'numeric',
                    },
                  )}
                </div>
                {/* Additional rendering based on notification type */}
                {notification.type === 'NEW_EMAIL_REQUEST' && (
                  <div>
                    {' '}
                    <a
                      className="text-blue-400"
                      href="/customer/myappointments"
                    >
                      Click Here
                    </a>{' '}
                  </div>
                )}
              </div>
            ))
          ) : (
            <div className="text-custom-black text-center">
              No Notifications
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

export default Notifications
