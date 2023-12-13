import { useState, useEffect } from 'react'
import RequestList from '../../components/ServiceProviderHome/RequestList'
import NavBarProvider from '../../components/Navbar/NavBarProvider'
import ProviderMap from '../../components/Map/ProviderMap'
import axios from 'axios'
import { checkIsServiceProviderLoggedIn } from '../../AuthContext'
import { useNavigate } from 'react-router-dom'

// Rowan University's coordinates
const DEFAULT_COORDS = { lat: 39.71, lng: -75.1192 }

function RequestView() {
  const [requests, setRequests] = useState([])
  const [coords, setCoords] = useState(DEFAULT_COORDS)
  const [bounds, setBounds] = useState(null)
  const [selectedCardIndex, setSelectedCardIndex] = useState(0)
  const [isLoading, setIsLoading] = useState(true)
  const [autocomplete, setAutocomplete] = useState(null)
  const [categoryChange, setCategoryChange] = useState(null)
  const [priceRangeChange, setPriceRangeChange] = useState(null)
  const navigate = useNavigate()

  const handleCardClick = (index) => {
    setSelectedCardIndex(index)
  }

  const [loggedIn, setLoggedIn] = useState(false)

  useEffect(() => {
    const fetchData = async () => {
      try {
        const isLoggedIn = await checkIsServiceProviderLoggedIn()
        setLoggedIn(isLoggedIn)
        console.log(isLoggedIn)
        if (!isLoggedIn) {
          navigate('/provider/login')
          alert(
            'You do not have access to this page. Please create a Service Provider Account to access this page.',
          )
        }
      } catch (error) {
        console.error('Error checking if customer is logged in:', error)
      }
    }

    fetchData()
  }, [])

  useEffect(() => {
    if (bounds) {
      const { ne, sw } = bounds

      let filters = {}
      if (categoryChange) {
        filters = {
          serviceType: categoryChange.value,
          ...filters,
        }
      }
      if (priceRangeChange) {
        filters = {
          lowerDollarRange: priceRangeChange.value[0],
          upperDollarRange: priceRangeChange.value[1],
          ...filters,
        }
      }

      // Make the GET request using Axios
      axios
        .get('/api/provider/service_requests/nearby', {
          withCredentials: true,
          params: {
            latitudeS: sw.lat,
            longitudeW: sw.lng,
            latitudeN: ne.lat,
            longitudeE: ne.lng,
            ...filters,
          },
        })
        .then((response) => {
          // Handle the response and set the serviceRequests state
          setRequests(response?.data)
          // Hide initial spinners
          setIsLoading(false)
        })
        .catch((error) => {
          console.error('Error:', error)
        })
    }
  }, [bounds, categoryChange, priceRangeChange])

  const onRequestChanged = () => {
    if (autocomplete) {
      const place = autocomplete.getPlace()
      if (place && place.geometry && place.formatted_address) {
        const lat = place.geometry.location.lat()
        const lng = place.geometry.location.lng()
        setCoords({ lat, lng })
      } else {
        window.alert('Please Enter a Valid Address.')
      }
    }
  }

  const onLoad = (autoC) => setAutocomplete(autoC)

  const centerOnLocation = () => {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        ({ coords: { latitude, longitude } }) => {
          setCoords({ lat: latitude, lng: longitude })
        },
      )
    }
  }

  return (
    <div>
      <NavBarProvider />

      <div className="flex">
        <div className="w-1/3">
          <RequestList
            requests={requests}
            selectedCardIndex={selectedCardIndex}
            isLoading={isLoading}
            onLoad={onLoad}
            onRequestChanged={onRequestChanged}
            onCenterLocation={centerOnLocation}
            setCategoryChange={setCategoryChange}
            setPriceRangeChange={setPriceRangeChange}
          />
        </div>
        <div className="w-2/3">
          <ProviderMap
            setBounds={setBounds}
            setCoords={setCoords}
            coords={coords}
            defaultCoords={DEFAULT_COORDS}
            onCardClicked={handleCardClick}
            requests={requests}
            selectedCardIndex={selectedCardIndex}
          />
        </div>
      </div>
    </div>
  )
}
export default RequestView
