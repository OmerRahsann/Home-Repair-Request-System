import { useState, useEffect } from 'react'
import ServiceRequest from '../../components/Customer/ServiceRequest'
import Navbar from '../../components/Navbar/NavBar'
import { checkIsCustomerLoggedIn } from '../../AuthContext'
import { InlineWidget } from "react-calendly";
import { Calendar, momentLocalizer } from 'react-big-calendar'

function Home() {
  const [serviceRequests, setServiceRequests] = useState([])
  const [loggedIn, setLoggedIn] = useState(false) // Initialize loggedIn with a default value

  useEffect(() => {
    const fetchData = async () => {
      try {
        const isLoggedIn = await checkIsCustomerLoggedIn()
        setLoggedIn(isLoggedIn) // Set the loggedIn state based on the result of the Promise
      } catch (error) {
        console.error('Error checking if customer is logged in:', error)
      }
    }

    fetchData() // Call the function when the component mounts
    // Define the checkIsCustomerLoggedIn function here or import it from where it's defined
  }, [])

  return (
    <div>
      <div>
        <Navbar isLoggedIn={loggedIn} />
      </div>
      <div>
        <ServiceRequest />
      </div>
      
      
      


    </div>
  )
}
export default Home
