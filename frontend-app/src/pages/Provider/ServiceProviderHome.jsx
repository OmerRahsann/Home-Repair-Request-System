import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import NavBarProvider from '../../components/Navbar/NavBarProvider'
import { checkIsServiceProviderLoggedIn } from '../../AuthContext'
import SearchBar from '../../components/ServiceProviderHome/SearchBar'
import axios from 'axios'


const ServiceProviderHome = ({ component: Component, ...rest }) => {
  const [type, setType] = useState([])
  const navigate = useNavigate()
  const [searchKeyword, setSearchKeyword] = useState('')
  const [loggedIn, setLoggedIn] = useState(false) // Initialize loggedIn with a default value

  useEffect(() => {
    const fetchData = async () => {
      try {
        const isLoggedIn = await checkIsServiceProviderLoggedIn()
        setLoggedIn(isLoggedIn) // Set the loggedIn state based on the result of the Promise
        console.log(isLoggedIn)
        if (!isLoggedIn) {
          alert(
            'You do not have access to this page. Please create a Service Provider Account to access this page.',
          )
          navigate('/provider/login') // Redirect to the login page
        }
      } catch (error) {
        console.error('Error checking if customer is logged in:', error)
      }
    }

    fetchData() // Call the function when the component mounts

    // Define the checkIsCustomerLoggedIn function here or import it from where it's defined
  }, [])

  if (!loggedIn) {
    // Do not render anything if not logged in and redirected
    return null
  }

  return (
    <div>
      <NavBarProvider isLoggedIn={loggedIn} />
      <div>
        <SearchBar onSearch={setSearchKeyword} />
        
      </div>
    </div>
  )
}
export default ServiceProviderHome
