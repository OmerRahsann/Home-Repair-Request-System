import { useState, useEffect } from 'react'
import Navbar from '../../components/Navbar/NavBar'
import axios from 'axios'
import { checkIsCustomerLoggedIn } from '../../AuthContext'
import RequestDetails from '../../components/Customer/RequestDetails'

function MyRequests() {
  const [serviceRequests, setServiceRequests] = useState([])
  const [loggedIn, setLoggedIn] = useState(false) // Initialize loggedIn with a default value
  const [showModal, setShowModal] = useState(false)

  const getServiceRequests = async () => {
    try {
      const response = await axios.get(
        `${process.env.REACT_APP_API_URL}/api/customer/service_request`,
        { withCredentials: true },
      )
      setServiceRequests(response.data)
      console.log(response.data)
    } catch (error) {
      console.error('Error fetching service requests:', error)
    }
  }

  useEffect(() => {
    const fetchData = async () => {
      try {
        const isLoggedIn = await checkIsCustomerLoggedIn()
        setLoggedIn(isLoggedIn) // Set the loggedIn state based on the result of the Promise
        if (loggedIn) {
          return (
            <h1>
              You are not logged in. Please sign in to make a request and get on
              the Radar!
            </h1>
          )
        }
      } catch (error) {
        console.error('Error checking if customer is logged in:', error)
      }
    }

    fetchData() // Call the function when the component mounts
    getServiceRequests()
    console.log(serviceRequests)

    // Define the checkIsCustomerLoggedIn function here or import it from where it's defined
  }, [])

  return (
    <div>
      <div>
        <Navbar isLoggedIn={loggedIn} />
      </div>
      {serviceRequests.length !== 0 ? (
        <div className="p-1">
          <div className="grid grid-cols-2 gap-8 p-2 sm:p-4 md:p-6 lg:p-8 xl:p-10">
            {serviceRequests.map((request) => (
              <div key={request.id}>
                {/* Use the RequestDetails component to display request details */}
                <RequestDetails request={request} />
              </div>
            ))}
          </div>
        </div>
      ) : (
        <h1 className="font-bold text-center text-xl">
          You do not have any requests.
        </h1>
      )}
    </div>
  )
}

export default MyRequests
