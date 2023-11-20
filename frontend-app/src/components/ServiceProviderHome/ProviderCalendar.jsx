import React, { useState, useRef, useCallback, useEffect, useMemo } from 'react'
import axios from 'axios'
import { Calendar, momentLocalizer, Views } from 'react-big-calendar'
import moment from 'moment'
import 'react-big-calendar/lib/css/react-big-calendar.css'
import './ProviderCalendar.scss' // Import your SASS file
import ServiceRequestModal from 'components/Customer/ServiceRequestModal'

const initialEvents = [
  {
    title: 'Slot 1',
    start: new Date(2023, 10, 1, 10, 0),
    end: new Date(2023, 10, 1, 12, 0),
  },
  {
    title: 'Slot 2',
    start: new Date(2023, 10, 5, 14, 0),
    end: new Date(2023, 10, 5, 16, 0),
    id: 1,
  },
  // Add more events as needed
]

const ProviderCalendar = ({ customerView }) => {
  const localizer = momentLocalizer(moment)
  const [events, setEvents] = useState(initialEvents)
  const [showEvent, setShowEvent] = useState(false)
  const [eventContent, setEventContent] = useState([])
  const [appointments, setAppointments] = useState([])
  const [year, setYear] = useState(2023)
  const [month, setMonth] = useState(1)

  const clickRef = useRef(null)

  const formatDateIn12HourFormat = (date) => {
    return date.toLocaleTimeString('en-US', {
      hour: 'numeric',
      minute: 'numeric',
      hour12: true,
    })
  }

  function buildMessage(calEvent, eventType) {
    console.log(calEvent)
    let message = `Event type: ${eventType}\n`

    // Check if the event has an 'id' property
    if ('id' in calEvent) {
      message += `Event ID: ${calEvent.id}\n`
    }

    // Include other details of the event
    message += `Event details: ${JSON.stringify(calEvent)}`

    return message
  }

  const getAppointments = async () => {
    try {
      const response = await axios.get(
        `${process.env.REACT_APP_API_URL}/api/provider/appointments`,
        {
          params: {
            year: year,
            month: month,
          },
          withCredentials: true,
        },
      )
      setAppointments(response.data)
      console.log(response.data)
    } catch (error) {
      console.error('Error fetching service requests:', error)
    }
  }

  const onSelectSlot = useCallback((calEvent) => {
    window.clearTimeout(clickRef?.current)

    // Set a timeout to detect whether it's a single or double click
    clickRef.current = window.setTimeout(() => {
      // Single click logic
      const shouldCreateNewEvent = window.confirm(
        `Do you want to create a new event at ${calEvent.start}?`,
      )

      if (shouldCreateNewEvent) {
        const eventTitle = prompt('Enter the title for the new event:')
        if (eventTitle) {
          // If a title is provided, update the events array with the new event
          const newEvent = {
            title: eventTitle,
            start: calEvent.start,
            end: calEvent.end, // You may want to adjust the end time accordingly
            id: events.length + 1, // Assign a unique ID, adjust as needed
          }

          setEvents((prevEvents) => [...prevEvents, newEvent])
        }
      }
    }, 250)
  }, [])

  const onSelectEvent = useCallback((calEvent) => {
    /**
     * Here we are waiting 250 milliseconds (use what you want) prior to firing
     * our method. Why? Because both 'click' and 'doubleClick'
     * would fire, in the event of a 'doubleClick'. By doing
     * this, the 'click' handler is overridden by the 'doubleClick'
     * action.
     */

    window.clearTimeout(clickRef?.current)
    clickRef.current = window.setTimeout(() => {
      setShowEvent(true)
      setEventContent(calEvent)
    }, 250)
  }, [])

  const { defaultDate, views } = useMemo(() => {
    if (customerView) {
      return {
        defaultDate: new Date(2015, 3, 1),
        views: [Views.MONTH],
      }
    } else {
      return {
        defaultDate: new Date(2015, 3, 1),
        views: [Views.MONTH, Views.DAY, Views.AGENDA, Views.WEEK],
      }
    }
  }, [])

  useEffect(() => {
    /**
     * What Is This?
     * This is to prevent a memory leak, in the off chance that you
     * teardown your interface prior to the timed method being called.
     */
    return () => {
      window.clearTimeout(clickRef?.current)
    }
  }, [])

  useEffect(() => {})

  return (
    <div>
      <Calendar
        localizer={localizer}
        events={appointments}
        startAccessor="start"
        endAccessor="end"
        onSelectSlot={onSelectSlot}
        style={{ height: 500 }}
        onSelectEvent={onSelectEvent}
        selectable
        views={views}
      />
      {showEvent && (
        <ServiceRequestModal
          isVisible={showEvent}
          onClose={() => setShowEvent(false)}
        >
          <p>{eventContent.title}</p>
          <p>
            {new Date(eventContent.start).toLocaleString('en-US', {
              month: 'long',
            })}{' '}
            {new Date(eventContent.start).getDay()},{' '}
            {new Date(eventContent.start).getFullYear()}
          </p>

          <p>
            {formatDateIn12HourFormat(new Date(eventContent.start))} -{' '}
            {formatDateIn12HourFormat(new Date(eventContent.end))}
          </p>
        </ServiceRequestModal>
      )}
    </div>
  )
}

export default ProviderCalendar
