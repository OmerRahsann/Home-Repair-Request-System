import React, { useState, useRef, useCallback, useEffect, useMemo } from 'react'
import axios from 'axios'
import { Calendar, momentLocalizer, Views } from 'react-big-calendar'
import moment from 'moment'
import 'react-big-calendar/lib/css/react-big-calendar.css'
import './ProviderCalendar.scss' // Import your SASS file
import ServiceRequestModal from 'components/Customer/ServiceRequestModal'
import TimePickers from 'components/TimePickers'

const ProviderCalendar = ({ customerView, request, setDate, isQuote }) => {
  const localizer = momentLocalizer(moment)
  const [events, setEvents] = useState([])
  const [showEvent, setShowEvent] = useState(false)
  const [eventContent, setEventContent] = useState([])
  const [year, setYear] = useState(new Date().getFullYear())
  const [view, setView] = useState(Views.WEEK)

  const [month, setMonth] = useState(new Date().getMonth() + 1)

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

  const onView = useCallback((newView) => setView(newView), [setView])

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
      // Filter events based on the selected date and time
      const selectedEvents = events.filter(
        (event) =>
          moment(event.start).isSame(calEvent.start, 'day') &&
          moment(event.end).isSame(calEvent.end, 'day')
      );
  
      setEventContent(selectedEvents);
      console.log(selectedEvents)
      setShowEvent(true);
    }, 250);
  }, [events]);

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
        views: [Views.WEEK, Views.MONTH, Views.AGENDA],
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
  }, [month, views])

  const onRangeChange = useCallback((range) => {
    if (range.start == undefined) {
      setMonth((new Date(range[0]).getMonth() % 12) + 1)
    } else {
      setYear(new Date(range.start).getFullYear())
      const adjustedMonth = new Date(range.start)
      adjustedMonth.setDate(adjustedMonth.getDate() + 6)
      console.log(adjustedMonth)
      setMonth((adjustedMonth.getMonth() % 12) + 1)
    }
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
        onView={onView}
        views={views}
        view={view}
        onRangeChange={onRangeChange}
      />
      {showEvent && (
        <ServiceRequestModal
          isVisible={showEvent}
          onClose={() => setShowEvent(false)}
        >
          {eventContent.length !== 0 && (
  <>
    {eventContent.map((event, index) => (
      <div key={index} className="border p-4 mb-4 rounded-md bg-white shadow-md">
        <p className="text-xl font-bold mb-2">{event.title}</p>
        
        <div className="flex flex-row mb-2">
         📞
         
          <p className="text-gray-700">{event.customerInfoModel.phoneNumber}</p>
        </div>
        
        <p className="text-gray-700 mb-2">
          {new Date(event.start).toLocaleString('en-US', {
            month: 'long',
          })}{' '}
          {new Date(event.start).getDate()},{' '}
          {new Date(event.start).getFullYear()}
        </p>
        <p className="text-gray-700">
          {formatDateIn12HourFormat(new Date(event.start))} -{' '}
          {formatDateIn12HourFormat(new Date(event.end))}
        </p>
      </div>
    ))}
  </>
)}
        </ServiceRequestModal>
      )}
    </div>
  )
}

export default ProviderCalendar
