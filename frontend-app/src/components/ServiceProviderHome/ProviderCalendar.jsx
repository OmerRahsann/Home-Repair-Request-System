import React, { useState, useRef, useCallback, useEffect, useMemo } from 'react'
import axios from 'axios'
import { Calendar, momentLocalizer, Views } from 'react-big-calendar'
import moment from 'moment'
import 'react-big-calendar/lib/css/react-big-calendar.css'
import './ProviderCalendar.scss' // Import your SASS file
import ServiceRequestModal from 'components/Customer/ServiceRequestModal'
import TimePickers from 'components/TimePickers'

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

const ProviderCalendar = ({ customerView, request, setDate, isQuote }) => {
  const localizer = momentLocalizer(moment)
  const [events, setEvents] = useState(initialEvents)
  const [showEvent, setShowEvent] = useState(false)
  const [eventContent, setEventContent] = useState([])
  const [year, setYear] = useState(2023)
  const [month, setMonth] = useState(12)

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

  const getEvents = async () => {
    try {
      const response = await axios.get(
        `${process.env.REACT_APP_API_URL}/api/provider/appointments`,
        {
          params: {
            year: year,
            month: month,
            zone_id: 'America/New_York',
          },
          withCredentials: true,
        },
      )

      response.data.forEach((x) => {
        x.title =
          x.customerInfoModel.firstName + ' ' + x.customerInfoModel.lastName
        x.start = new Date(x.startTime)
        x.end = new Date(x.endTime)
      })
      setEvents(response.data)
    } catch (error) {
      console.error('Error fetching service requests:', error)
    }
  }

  const onSelectSlot = useCallback(
    (calEvent) => {
      console.log(buildMessage(calEvent))
      window.clearTimeout(clickRef?.current)

      // Set a timeout to detect whether it's a single or double click
      clickRef.current = window.setTimeout(() => {
        // Single click logic
        const shouldCreateNewEvent = window.confirm(
          `Do you want to create a new appointment?`,
        )

        if (shouldCreateNewEvent) {
          if (isQuote) {
            const tempEvent = {
              title: request.request.title,
              start: calEvent.start, // Adjust the format as needed
              end: calEvent.end, // Adjust the format as needed
              // Assign a unique ID, adjust as needed
            }

            const newEvent = {
              start: calEvent.start, // Adjust the format as needed
              end: calEvent.end, // Adjust the format as needed
              // Assign a unique ID, adjust as needed
            }

            console.log(newEvent)

            if (setDate != null) setDate(newEvent)

            setEvents((prevEvents) => [...prevEvents, tempEvent])
          } else {
            //TODO Here
          }
        }
      }, 250)
    },
    [setDate],
  )

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
        defaultDate: new Date(),
        views: [Views.MONTH],
      }
    } else if (isQuote) {
      return {
        defaultDate: new Date(),
        views: [Views.WEEK, Views.MONTH],
      }
    } else {
      return {
        defaultDate: new Date(),
        views: [Views.MONTH, Views.DAY, Views.AGENDA, Views.WEEK],
      }
    }
  }, [])

  // const handleTimeSelection = ({ startTime, endTime }) => {

  //   console.log(startTime.toISOString())
  //   console.log(moment(endTime).format("hh:mm:ss A"));
  //   const startEnd = moment(month).set("hour", moment(startTime).hour()).set("minutes", moment(startTime).minute())
  //   const finalEnd = moment(month).set("hour", moment(endTime).hour()).set("minutes", moment(endTime).minute())

  //   console.log(finalEnd)

  // const newEvent = {
  //   title: request.request.title,
  //   start: startTime, // Adjust the format as needed
  //   end: endTime,     // Adjust the format as needed
  //   // Assign a unique ID, adjust as needed
  // };

  // console.log(newEvent);

  //   if (setDate != null) setDate(newEvent)

  //   setEvents((prevEvents) => [...prevEvents, newEvent])

  //   // Close the modal when you are done processing
  //   setShowModal(false)
  // }

  // ...

  useEffect(() => {
    /**
     * What Is This?
     * This is to prevent a memory leak, in the off chance that you
     * teardown your interface prior to the timed method being called.
     *
     */
    getEvents()
    return () => {
      window.clearTimeout(clickRef?.current)
    }
  }, [])

  const onRangeChange = useCallback((range) => {
    //  window.alert(buildMessage(range))
  }, [])

  return (
    <div>
      <Calendar
        localizer={localizer}
        events={events}
        defaultDate={defaultDate}
        startAccessor="start"
        endAccessor="end"
        onSelectSlot={onSelectSlot}
        style={{ height: 500 }}
        onSelectEvent={onSelectEvent}
        selectable
        views={views}
        onRangeChange={onRangeChange}
      />
      {showEvent && (
        <ServiceRequestModal
          isVisible={showEvent}
          onClose={() => setShowEvent(false)}
        >
          {events.length !== 0 &&
            events.map((event) => (
              <>
                <p>{event.title}</p>
                <p>
                  {console.log(events)}
                  {new Date(event.start).toLocaleString('en-US', {
                    month: 'long',
                  })}{' '}
                  {new Date(event.start).getDate()},{' '}
                  {new Date(event.start).getFullYear()}
                </p>
                <p>
                  {formatDateIn12HourFormat(new Date(event.start))} -{' '}
                  {formatDateIn12HourFormat(new Date(event.end))}
                </p>
              </>
            ))}
        </ServiceRequestModal>
      )}
    </div>
  )
}

export default ProviderCalendar
