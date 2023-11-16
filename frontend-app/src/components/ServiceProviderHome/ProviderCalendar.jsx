import React, { useState, useRef, useCallback, useEffect, useMemo } from 'react';
import axios from 'axios';
import { Calendar, momentLocalizer, Views } from 'react-big-calendar'
import moment from 'moment'
import 'react-big-calendar/lib/css/react-big-calendar.css';
import './ProviderCalendar.scss'; // Import your SASS file



const ProviderCalendar = ({customerView}) => {
    const localizer = momentLocalizer(moment)
const events = [
    {
      title: 'Slot 1',
      start: new Date(2023, 10, 1, 10, 0),
      end: new Date(2023, 10, 1, 12, 0),

    },
    {
      title: 'Slot 2',
      start: new Date(2023, 10, 5, 14, 0),
      end: new Date(2023, 10, 5, 16, 0),
      id: 1
    },
    // Add more events as needed
  ];

  const clickRef = useRef(null)
  

  function buildMessage(calEvent, eventType) {
    console.log(calEvent)
    let message = `Event type: ${eventType}\n`;
  
    // Check if the event has an 'id' property
    if ('id' in calEvent) {
      message += `Event ID: ${calEvent.id}\n`;
    }
  
    // Include other details of the event
    message += `Event details: ${JSON.stringify(calEvent)}`;
  
    return message;
  }
  
  


  const onSelectSlot = useCallback((slotInfo) => {
    /**
     * Here we are waiting 250 milliseconds (use what you want) prior to firing
     * our method. Why? Because both 'click' and 'doubleClick'
     * would fire, in the event of a 'doubleClick'. By doing
     * this, the 'click' handler is overridden by the 'doubleClick'
     * action.
     */
    window.clearTimeout(clickRef?.current)
    clickRef.current = window.setTimeout(() => {
      window.alert((slotInfo))
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
      window.alert(buildMessage(calEvent, 'onSelectEvent'))
    }, 250)
  }, [])

     
  const { defaultDate, views } = useMemo(() => {
    if (customerView) {
      return {
        defaultDate: new Date(2015, 3, 1),
        views: [Views.MONTH],
      };
    } else {
      return {
        defaultDate: new Date(2015, 3, 1),
        views: [Views.MONTH, Views.DAY, Views.AGENDA, Views.WEEK],
      };
    }
  }, []);


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
 
  return (
    <div>
        <Calendar
      localizer={localizer}
     events={events}
      startAccessor="start"
      endAccessor="end"
      onSelectSlot={onSelectSlot}
      style={{ height: 500 }}
      onSelectEvent={onSelectEvent}
      selectable
      views={views}
      
    />
     
    </div>
  );
};

export default ProviderCalendar;
